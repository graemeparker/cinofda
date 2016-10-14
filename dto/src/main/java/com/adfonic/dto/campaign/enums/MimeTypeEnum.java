package com.adfonic.dto.campaign.enums;

public enum MimeTypeEnum {
    MIME_TYPE_PNG("image/png", "png"), MIME_TYPE_JPG("image/jpeg", "jpg"), MIME_TYPE_GIF("image/gif", "gif"), MIME_TYPE_TXT("text/plain", "txt");

    private String mimename = "";
    private String fileExtension = "";

    MimeTypeEnum(String mimename, String fileExtension) {
        this.mimename = mimename;
        this.fileExtension = fileExtension;
    }

    public String mimename() {
        return this.mimename;
    }

    public String fileExtension() {
        return this.fileExtension;
    }

    public static String fileExtension(String mimename) {
        for (MimeTypeEnum mimeType : MimeTypeEnum.values()) {
            if (mimeType.mimename.equals(mimename)) {
                return mimeType.fileExtension;
            }
        }
        return "";
    }
}
