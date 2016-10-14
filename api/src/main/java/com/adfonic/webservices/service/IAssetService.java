package com.adfonic.webservices.service;

import com.adfonic.domain.Asset;
import com.adfonic.domain.User;

public interface IAssetService extends IOwnedEntityService<Asset> {

    public Asset createAsset(User user, String creativeID, String contentSpec, String contentType, byte[] data);

    public Asset update(Asset asset, byte[] data);

    public void deleteAsset(User user, String externalID);

}
