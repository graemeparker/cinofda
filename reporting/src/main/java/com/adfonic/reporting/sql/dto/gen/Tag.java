package com.adfonic.reporting.sql.dto.gen;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * immutable class
 * The value field is a sort of carrier but have a say in identity
 *
 */
public final class Tag implements Serializable {

    private static final long serialVersionUID = 1L;


    public enum TAG {
        ROOT("none", "root", "id"),
        PUBLICATION("publication_external_id", "publication", "id"), 
        ADSLOT("ad_space_external_id", "adslot", "id"), 

        COUNTRY("country_iso_code", "country", "isocode"),

        CAMPAIGN("campaign", "campaign", "name"),
        CREATIVE("creative", "creative", "name"),
        
        DATE_YYYYMMDD("advertiser_date_id", "date", "day");

        private TAG(String columnName, String displayElementName, String displayIdentifier) {
            this.columnName = columnName;
            this.displayElementName = displayElementName;
            this.displayIdentifier = displayIdentifier;
        }

        private String columnName;
        private String displayElementName;
        private String displayIdentifier;


        public String column() {
            return columnName;
        }


        public String displayElement() {
            return displayElementName;
        }


        public String displayId() {
            return displayIdentifier;
        }

    }


    public Tag(TAG key, String value) {
        if (key == null) {
            throw new RuntimeException("No Tag without TAG!");
        }
        this.key = key;
        this.value = value;
    }


    public static Tag getTemplate(TAG key) {
        return new Tag(key, null);
    }
    
    public static List<Tag> getTemplateTags(TAG... tags) {
        List<Tag> tagList = new ArrayList<>(tags.length);
        for (TAG tag : tags) {
            tagList.add(getTemplate(tag));
        }
        return tagList;
    }

    public Tag cloneWith(String value) {
        return new Tag(getKey(), value);
    }


    private final TAG key;

    private final String value;


    public TAG getKey() {
        return key;
    }


    public String getValue() {
        return value;
    }
    
    @Override
    public boolean equals(Object o){
        try{
            Tag otherTag=(Tag)o;
            return otherTag.getKey() == key && (value == otherTag.getValue() || value.equals(otherTag.getValue()));//for nulls and .equals not really reqd
        }catch(RuntimeException e){//NPE, CCE
            return false;
        }
    }
    
    @Override
    public int hashCode() {
        int hc = 31 + key.hashCode();// key can't be null
        hc = 31 * hc + (value == null ? 0 : value.hashCode());
        return hc;
    }


}
