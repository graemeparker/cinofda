package com.adfonic.webservices.service.impl;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.adfonic.domain.Asset;
import com.adfonic.domain.AssetBundle;
import com.adfonic.domain.AssetBundle_;
import com.adfonic.domain.Asset_;
import com.adfonic.domain.Component;
import com.adfonic.domain.ContentSpec;
import com.adfonic.domain.ContentSpec_;
import com.adfonic.domain.ContentType;
import com.adfonic.domain.Creative;
import com.adfonic.domain.Creative_;
import com.adfonic.domain.DisplayType;
import com.adfonic.domain.User;
import com.adfonic.util.MediaUtils;
import com.adfonic.util.MediaUtils.ImageInfo;
import com.adfonic.webservices.ErrorCode;
import com.adfonic.webservices.exception.AuthorizationException;
import com.adfonic.webservices.exception.ServiceException;
import com.adfonic.webservices.exception.ValidationException;
import com.adfonic.webservices.service.IAssetService;
import com.adfonic.webservices.service.ICreativeService;
import com.adfonic.webservices.service.IUtilService;
import com.byyd.middleware.common.service.CommonManager;
import com.byyd.middleware.creative.service.AssetManager;
import com.byyd.middleware.creative.service.CreativeManager;
import com.byyd.middleware.iface.dao.FetchStrategy;
import com.byyd.middleware.iface.dao.jpa.FetchStrategyBuilder;

@Service
public class AssetService implements IAssetService {
    private static final FetchStrategy ASSET_FETCH_STRATEGY = new FetchStrategyBuilder()
        .addInner(Asset_.creative)
        .addLeft(Creative_.assetBundleMap)
        .addLeft(AssetBundle_.assetMap)
        .nonRecursive(AssetBundle_.assetMap)
        .build();

    private static final FetchStrategy CONTENT_SPEC_FETCH_STRATEGY = new FetchStrategyBuilder()
                                        .addLeft(ContentSpec_.contentTypes)
                                        .build();

    @Autowired
    private ICreativeService creativeService;

    @Autowired
    private IUtilService utilService;

    @Autowired
    private AssetManager assetManager;
    
    @Autowired
    private CreativeManager creativeManager;
    
    @Autowired
    private CommonManager commonManager;


    public void authorize(User user, Asset asset) {
        creativeService.authorize(user, asset.getCreative());
    }


    public void validate(Asset asset) {
    }


    public Asset createAsset(User user, String creativeID, String contentSpecName, String mimeType, byte[] data) {
        utilService.validatePresence("content-spec", contentSpecName)
                   .validatePresence("mime-type", mimeType)
                   .validatePresence("data", data);

        ContentSpec contentSpec = commonManager.getContentSpecByName(contentSpecName, CONTENT_SPEC_FETCH_STRATEGY);
        ContentType contentType = identifyContentType(mimeType);
        validate(contentType, contentSpec, data);

        Creative creative = creativeService.findbyExternalID(user, creativeID);

        Asset asset = creativeService.getNewAsset(creative, contentSpec, contentType);
        return doUpdate(asset, data);
    }


    public Asset update(Asset asset, byte[] data) {
        utilService.validatePresence("data", data);

        // TODO - if this method is ever used, write code to get current content-spec; currently writing to throw an NPE
        ContentSpec contentSpec = null;
        validate(asset.getContentType(), contentSpec, data);

        return (doUpdate(asset, data));
    }


    private Asset doUpdate(Asset asset, byte[] data) {
        asset.setData(data);
        asset = assetManager.update(asset);
        asset = assetManager.getAssetById(asset.getId(), ASSET_FETCH_STRATEGY);
        creativeService.notifyUpdate(asset.getCreative());
        return (asset);
    }


    public void deleteAsset(User user, String externalID) {
        Asset asset = findbyExternalID(user, externalID);

        Creative creative = asset.getCreative();// notify upfront is bad as jtracutils is not transactional
        deleteAssetInAnyState(asset);
        creativeService.notifyUpdate(creative);
    }

    private void deleteAssetInAnyState(Asset asset) {// TODO - verify because UI does not allow this stuff
        Creative creative = asset.getCreative();

        Map<DisplayType, AssetBundle> assetBundleMap = creative.getAssetBundleMap();
        for (Map.Entry<DisplayType, AssetBundle> assetBundleEntry : assetBundleMap.entrySet()) {
            AssetBundle assetBundle = assetBundleEntry.getValue();
            Map<Component, Asset> assetMap = assetBundle.getAssetMap();
            for (Map.Entry<Component, Asset> assetEntry : assetMap.entrySet()) {
                if (asset.equals(assetEntry.getValue())) {
                    assetMap.remove(assetEntry.getKey());
                    assetBundle = assetManager.update(assetBundle);
                    if (assetMap.isEmpty()) {
                        assetBundleMap.remove(assetBundleEntry.getKey());
                        creative = creativeManager.update(creative);
                    }
                    assetManager.delete(asset);
                    return;
                }
            }
        }

        throw new ServiceException(ErrorCode.GENERAL, "Asset not in place to delete!");
    }


    private ContentType identifyContentType(String mimeType) {
        return commonManager.getContentTypeByName(mimeType);
    }

    private void validate(ContentType contentType, ContentSpec contentSpec, byte[] content){
        if(!contentSpec.getContentTypes().contains(contentType)){
            throw new ValidationException("content spec type mismatch");
        }

        validate(contentSpec, contentType, content);
    }

    private void validate(ContentSpec contentSpec, ContentType contentType, byte[] data) {
        String mimeType = contentType.getMIMEType();
        if (mimeType.contains("text")) {
            int maxLength = Integer.parseInt(contentSpec.getManifestProperties().get("maxLength"));
            if (new String(data).length() > maxLength) {
                throw new ValidationException("Text length exceeds max allowed!");
            }
        } else {
            validateImage(contentSpec, contentType, data);
        }
    }

    // TODO - adaptation of CreativeBean.doSaveUploads()
    private void validateImage(ContentSpec contentSpec, ContentType contentType, byte[] data) {
        Map<String, String> constraints = contentSpec.getManifestProperties();

        int width = -1;
        int height = -1;
        int maxBytes = -1;
        for (Map.Entry<String, String> entry : constraints.entrySet()) {
            String key = entry.getKey();
            if ("width".equals(key)) {
                width = Integer.parseInt(entry.getValue());
            } else if ("height".equals(key)) {
                height = Integer.parseInt(entry.getValue());
            } else if ("maxBytes".equals(key)) {
                maxBytes = Integer.parseInt(entry.getValue());
            }
        }
        if (maxBytes != -1) {
            if (data.length > maxBytes) {
                throw new ValidationException("Content size exceeds the maximum allowed " + maxBytes + " bytes!");
            }
        }
        ImageInfo imageInfo = null;
        if (width != -1 && height != -1) {
            try {
                imageInfo = MediaUtils.getImageInfo(data);
            } catch (Exception e) {
                throw new ValidationException("Image extracting error!");

            }

            if (imageInfo.getWidth() != width || imageInfo.getHeight() != height) {
                throw new ValidationException("Was expecting dimensions of " + height + "x" + width);

            }
        }

    }

    public Asset findbyExternalID(String externalID) {
        return findbyExternalID(null, externalID);
    }
    
    public Asset findbyExternalID(User user, String externalID) {
        Asset asset = assetManager.getAssetByExternalId(externalID, ASSET_FETCH_STRATEGY);

        if (asset == null) {
            throw new ServiceException(ErrorCode.ENTITY_NOT_FOUND, "Asset not found");
        }

        try {// TODO - change this to auto translation
            authorize(user, asset);
        } catch (AuthorizationException e) {// Dont give the info away to user
                throw new ServiceException(ErrorCode.ENTITY_NOT_FOUND, "Asset not found");
        }

        return asset;
    }
}
