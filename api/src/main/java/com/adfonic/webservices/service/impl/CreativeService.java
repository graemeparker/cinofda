package com.adfonic.webservices.service.impl;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.adfonic.domain.Advertiser;
import com.adfonic.domain.Advertiser_;
import com.adfonic.domain.Asset;
import com.adfonic.domain.AssetBundle;
import com.adfonic.domain.AssetBundle_;
import com.adfonic.domain.Campaign;
import com.adfonic.domain.Campaign_;
import com.adfonic.domain.Component;
import com.adfonic.domain.Component_;
import com.adfonic.domain.ContentSpec;
import com.adfonic.domain.ContentType;
import com.adfonic.domain.Creative;
import com.adfonic.domain.Creative_;
import com.adfonic.domain.Destination;
import com.adfonic.domain.DestinationType;
import com.adfonic.domain.DisplayType;
import com.adfonic.domain.Format;
import com.adfonic.domain.Format_;
import com.adfonic.domain.User;
import com.adfonic.util.ValidationUtils;
import com.adfonic.webservices.ErrorCode;
import com.adfonic.webservices.exception.AuthorizationException;
import com.adfonic.webservices.exception.InvalidStateException;
import com.adfonic.webservices.exception.ServiceException;
import com.adfonic.webservices.exception.ValidationException;
import com.adfonic.webservices.service.ICampaignService;
import com.adfonic.webservices.service.ICreativeService;
import com.adfonic.webservices.service.IUtilService;
import com.byyd.middleware.common.service.CommonManager;
import com.byyd.middleware.creative.filter.DestinationFilter;
import com.byyd.middleware.creative.service.AssetManager;
import com.byyd.middleware.creative.service.CreativeManager;
import com.byyd.middleware.iface.dao.FetchStrategy;
import com.byyd.middleware.iface.dao.jpa.FetchStrategyBuilder;

@Service
public class CreativeService implements ICreativeService {
       private static final FetchStrategy ASSET_BUNDLE_FETCH_STRATEGY = new FetchStrategyBuilder()
           .addLeft(AssetBundle_.displayType)
           .addLeft(AssetBundle_.assetMap)
           .build();

       private static final FetchStrategy CREATIVE_FETCH_STRATEGY = new FetchStrategyBuilder()
        .addInner(Creative_.campaign)
        .addInner(Campaign_.advertiser)
        .addInner(Advertiser_.company)
        .addLeft(Creative_.assetBundleMap)
        .addLeft(Campaign_.defaultLanguage)
        .addLeft(Creative_.destination)
        .addLeft(AssetBundle_.assetMap)
        .build();

    private static final FetchStrategy FORMAT_FETCH_STRATEGY = new FetchStrategyBuilder()
        .addLeft(Format_.displayTypes)
        .build();

    private static final FetchStrategy COMPONENT_FETCH_STRATEGY = new FetchStrategyBuilder()
        .addLeft(Component_.contentSpecMap)
        .build();

    @Autowired
    private ICampaignService campaignService;

    @Autowired
    private IUtilService utilService;

    @Autowired
    private CreativeManager creativeManager;
    
    @Autowired
    private AssetManager assetManager;
    
    @Autowired
    private CommonManager commonManager;


    public void authorize(User user, Creative creative) {
        creative = creativeManager.getCreativeById(creative.getId(), CREATIVE_FETCH_STRATEGY);
        campaignService.authorize(user, creative.getCampaign());
    }


    public Creative createMinimalCreative(User user, String campaignID, String name, String format) {
        utilService.validatePresence("external-id", campaignID)
                   .validatePresence("name", name)
                   .validatePresence("format", format);

        Campaign campaign = campaignService.findbyExternalID(user, campaignID);

        verifyUniqueName(name, campaign);
        Creative creative = creativeManager.newCreative(campaign, campaign.getSegments().get(0), commonManager.getFormatBySystemName(format), name);

        creative.setLastUpdated(new Date());
        creative = creativeManager.update(creative);
        return (creative);
    }

    private Destination getOrCreateDestination(Advertiser advertiser, DestinationType destinationType, String data) {
        utilService.validatePresence("destination-type", destinationType);

        DestinationFilter filter = new DestinationFilter()
            .setAdvertiser(advertiser)
            .setDestinationTypes(Collections.singleton(destinationType))
            .setData(data);
        List<Destination> found = creativeManager.getAllDestinations(filter);
        if (!found.isEmpty()) {
            return found.iterator().next();
        } else {
            return creativeManager.newDestination(advertiser, destinationType, data);
        }
    }

    public Creative setDestination(Creative creative, DestinationType type, String destination) {
        creative.setDestination(getOrCreateDestination(creative.getCampaign().getAdvertiser(), type, destination));
        return (creativeManager.update(creative));
    }


    public Creative copyCustomAttributes(Creative creative, String englishTranslation, Set<String> categories) {

        // TODO need checks to force if foreign language
        if (englishTranslation != null && !englishTranslation.trim().isEmpty()) {
            creative.setEnglishTranslation(englishTranslation);
        }

        creative.setLastUpdated(new Date());
        validate(creative);
        return creativeManager.update(creative);
    }


    public void submit(Creative creative) {
        if (!creative.isEditable()) {// Not required - || !creative.getCampaign().isEditable()) {
            throw new InvalidStateException();
        }

        try {
        	creative.getAssetBundleMap().size();
        } catch(Exception e) {
        	creative = creativeManager.getCreativeById(creative.getId(), CREATIVE_FETCH_STRATEGY);
        }
        
        if (creative.getAssetBundleMap().isEmpty()) {
            throw new ServiceException(ErrorCode.INVALID_STATE, "No assets found for creative!");
        }

        creative.setStatus(Creative.Status.PENDING);
        doActualSubmit(creative);
    }


    public void notifyUpdate(Creative creative) {
        creative.setLastUpdated(new Date());
        if (!creative.isEditable()) {

        	try {
            	creative.getAssetBundleMap().size();
            } catch(Exception e) {
            	creative = creativeManager.getCreativeById(creative.getId(), CREATIVE_FETCH_STRATEGY);
            }

            if (creative.getAssetBundleMap().isEmpty()) {// if its a deletion which left the creative empty
                throw new ServiceException(ErrorCode.INVALID_STATE, "Cannot leave this Creative empty of assets!");
            }

            Creative.Status currentStatus = creative.getStatus();
            if (currentStatus == Creative.Status.PAUSED) {
                creative.setStatus(Creative.Status.PENDING_PAUSED);
            }
            if (currentStatus != Creative.Status.PENDING_PAUSED) {
                creative.setStatus(Creative.Status.PENDING);// unnecessary for already PENDING, 's ok
            }

            doActualSubmit(creative);
        }
    }


    private void doActualSubmit(Creative creative) {
        if(creativeManager.isPersisted(creative)) {
            creative = creativeManager.getCreativeById(creative.getId(), CREATIVE_FETCH_STRATEGY);
        }
        validate(creative);

        // AO-146 - track submission time
        creative.setSubmissionTime(new Date());

        if (creativeManager.isPersisted(creative)) {
            creativeManager.update(creative);
        } else {
            creative = creativeManager.newCreative(creative);
            // TODO: determine if we need to get the creative back for its id
        }

        creative = creativeManager.getCreativeById(creative.getId(), CREATIVE_FETCH_STRATEGY);
    }


    public void validate(Creative creative) {
        if (!commonManager.getLanguageByIsoCode("en").equals(creative.getCampaign().getDefaultLanguage()) && creative.getEnglishTranslation() == null) {
            throw new ValidationException("No english translation found!");
        }

        //if (creative.getStatus() == Creative.Status.NEW) {// NEW_REVIEW and other states means it'll already be
        //    verifyUniqueName(creative.getName(), creative.getCampaign(), creative);// should pass; being defensive
        //}

        Destination destination = creative.getDestination();
        if (destination.getDestinationType() == DestinationType.URL && !ValidationUtils.isValidURL(destination.getData())) {
            throw new ValidationException("Invalid URL!");
        } else if (destination.getDestinationType() == DestinationType.CALL && !ValidationUtils.isValidClickToCallNumber(destination.getData())) {
            throw new ValidationException("Invalid click-to-call number!");
        } else {// TODO - Validate other types?
        }
    }


    public void delete(Creative creative) {
        if (!creative.isEditable()) {// TODO - is it NEW only?
            throw new ServiceException(ErrorCode.INVALID_STATE, "Cannot delete submitted creative");
        }

        creativeManager.delete(creative);
    }

    public Asset getNewAsset(Creative creative, ContentSpec contentSpec, ContentType contentType) {
        Format format = commonManager.getFormatById(creative.getFormat().getId(), FORMAT_FETCH_STRATEGY);
        List<Component> components = assetManager.findAllComponentsForFormat(format);
        if(!CollectionUtils.isEmpty(components)) {
            try {
            	creative.getAssetBundleMap().size();
            } catch(Exception e) {
            	creative = creativeManager.getCreativeById(creative.getId(), CREATIVE_FETCH_STRATEGY);
            }
        }
        for (Component component : components) {
            component = assetManager.getComponentById(component.getId(), COMPONENT_FETCH_STRATEGY);
            for (DisplayType displayType : format.getDisplayTypes()) {
                if (component.getContentSpec(displayType).equals(contentSpec)) {
                    AssetBundle assetBundle = creative.getAssetBundle(displayType);
                    if (assetBundle == null) {
                        assetBundle = assetManager.newAssetBundle(creative, displayType, ASSET_BUNDLE_FETCH_STRATEGY);
                        creative.getAssetBundleMap().put(displayType, assetBundle);
                        creative = creativeManager.update(creative);
                    }
                    if (assetBundle.getAsset(component) != null) {
                        throw new ServiceException(ErrorCode.ENTITY_ALREADY_EXISTS, "Similar asset already exists for creative!");
                    }

                    Asset asset = assetManager.newAsset(creative, contentType);
                    assetBundle.putAsset(component, asset);
                    assetBundle = assetManager.update(assetBundle);
                    return asset;
                }
            }
        }
        throw new ServiceException(ErrorCode.INVALID_ARGUMENT, "ContentSpec not supported by creative!");
    }

    private void verifyUniqueName(String name, Campaign campaign) {
        verifyUniqueName(name, campaign, null);
    }

    private void verifyUniqueName(String name, Campaign campaign, Creative creative) {
        if (!isCreativeNameUnique(name, campaign, creative)) {
            throw new ValidationException("Creative with same name exists for campaign!");
        }
    }

    private boolean isCreativeNameUnique(String name, Campaign campaign, Creative creative) {
        return creativeManager.isCreativeNameUnique(name, campaign, creative);
    }

    public void start(Creative creative) {
        if (!creativeManager.startCreative(creative)) {
            throw new InvalidStateException("Cannot start from current state");
        }
    }


    public void pause(Creative creative) {
        if (!creativeManager.pauseCreative(creative)) {
            throw new InvalidStateException("Cannot pause from current state");
        }
    }


    public void stop(Creative creative) {
        if (!creativeManager.stopCreative(creative)) {
            throw new InvalidStateException("Cannot stop from current state");
        }
    }

    public Creative findbyExternalID(String externalID) {
        return findbyExternalID(null, externalID);
    }
    
    public Creative findbyExternalID(User user, String externalID) {
        Creative creative = creativeManager.getCreativeByExternalId(externalID, CREATIVE_FETCH_STRATEGY);

        if (creative == null) {
            throw new ServiceException(ErrorCode.ENTITY_NOT_FOUND, "Creative not found");
        }

        try {// TODO - change this to auto translation
            authorize(user, creative);
        } catch (AuthorizationException e) {// Dont give the info away to user
            throw new ServiceException(ErrorCode.ENTITY_NOT_FOUND, "Creative not found");
        }

        return creative;
    }
}
