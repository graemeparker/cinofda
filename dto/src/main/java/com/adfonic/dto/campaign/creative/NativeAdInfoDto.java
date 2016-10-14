package com.adfonic.dto.campaign.creative;

public class NativeAdInfoDto {
    private String title;
    private String description;
    private String clickToAction;
    private AssetInfoDto icon;
    private AssetInfoDto image;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getClickToAction() {
        return clickToAction;
    }

    public void setClickToAction(String clickToAction) {
        this.clickToAction = clickToAction;
    }

    public AssetInfoDto getIcon() {
        return icon;
    }

    public void setIcon(AssetInfoDto icon) {
        this.icon = icon;
    }

    public AssetInfoDto getImage() {
        return image;
    }

    public void setImage(AssetInfoDto image) {
        this.image = image;
    }
}
