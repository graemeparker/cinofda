package com.adfonic.webservices.controller;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.adfonic.domain.Asset;
import com.adfonic.domain.User;
import com.adfonic.webservices.WebServiceException;
import com.adfonic.webservices.dto.AssetDTO;
import com.adfonic.webservices.service.IAssetService;
import com.adfonic.webservices.service.ICopyService;

@Controller
public class AssetController extends AbstractAdfonicWebService {

    @Autowired
    private IAssetService assetService;

    @Autowired
    private ICopyService<AssetDTO, Asset> copyService;// do not need restricting copy since here we do just domain->dto


    @RequestMapping(value = { "/asset/create.{format}", "/asset.{format}" }, method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.CREATED)
    @ResponseBody
    public AssetDTO createAsset(@PathVariable 
    		                    String format, 
    		                    @RequestBody 
    		                    AssetDTO assetDTO, 
    		                    HttpServletRequest request) throws WebServiceException {
        User user = authenticate(request, format);

        //pm.currentTransaction().begin();

        Asset asset = assetService.createAsset(user, assetDTO.getCreativeId(), assetDTO.getContentSpec(), assetDTO.getContentType(), assetDTO.getData());

        //pm.currentTransaction().commit();
        return (copyService.copyFromDomain(asset, AssetDTO.class));
    }


    @RequestMapping(value = "/asset/{externalID}.{format}", method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public AssetDTO getAsset(@PathVariable String format, @PathVariable String externalID, HttpServletRequest request) throws WebServiceException {
        User user = authenticate(request, format);

        Asset asset = assetService.findbyExternalID(user, externalID);
        return (copyService.copyFromDomain(asset, AssetDTO.class));
    }


    // update() invalid for Asset as CDN caches making it immutable; TODO - update spec
    //@RequestMapping(value = "/asset/{externalID}.{format}", method = RequestMethod.PUT)
    //@ResponseStatus(HttpStatus.OK)
    //@ResponseBody
    public AssetDTO updateAsset(@PathVariable String format, @PathVariable String externalID, @RequestBody AssetDTO assetDTO, HttpServletRequest request) throws WebServiceException {
        User user = authenticate(request, format);

        //pm.currentTransaction().begin();

        Asset asset = assetService.findbyExternalID(user, externalID);
        assetService.update(asset, assetDTO.getData());

        //pm.currentTransaction().commit();
        return (copyService.copyFromDomain(asset, AssetDTO.class));
    }


    @RequestMapping(value = "/asset/{externalID}.{format}", method = RequestMethod.DELETE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAsset(@PathVariable String format, @PathVariable String externalID, HttpServletRequest request) throws WebServiceException {
        User user = authenticate(request, format);

        //pm.currentTransaction().begin();

        assetService.deleteAsset(user, externalID);
        //pm.currentTransaction().commit();
    }

}
