package com.adfonic.webservices.view.dsp.builders;

import org.apache.commons.lang.StringUtils;

import com.adfonic.domain.Category;
import com.adfonic.domain.Medium;
import com.adfonic.domain.Publication;
import com.adfonic.domain.Publisher;
import com.adfonic.webservices.view.dsp.PublicationExtractor;

public class SerializedFormatBuilders {

    public static <T> T buildPublication(Publication pub, FlatObjectBuilder<T> builder) {
        Publisher publisher = pub.getPublisher();
        boolean isDisclosed = publisher.isDisclosed() || pub.isDisclosed();
        if (isDisclosed) {
            String friendlyName = pub.getFriendlyName();
            builder.set("name", StringUtils.isEmpty(friendlyName) ? pub.getName() : friendlyName);
        }
        
        builder.set("id", pub.getExternalID())
                   .set("publisher_id", publisher.getExternalID())
                   .set("publisher_name", publisher.getName());
        
        if (isDisclosed && StringUtils.isNotEmpty(pub.getDescription())) {
            builder.set("description", pub.getDescription());
        }
        builder.set("type", mediumPrintName(pub.getPublicationType().getMedium()));
        if (isDisclosed && StringUtils.isNotEmpty(pub.getURLString())) {
            builder.set("url", pub.getURLString());
        }
        Category category=pub.getCategory();// should not be null but no constraint to ensure that
        String iabId;
        if (category == null || !(iabId = category.getIabId()).startsWith("IAB")) {
            iabId = PublicationExtractor.ADFONIC_NOT_CATEGORIZED_CAT_IAB_ID;
        }
        builder.set("category", iabId);
        return builder.built();
    }
    
    private static String mediumPrintName(Medium medium) {
        return medium == Medium.SITE ? "site" : medium == Medium.APPLICATION ? "app" : "unknown";
    }

}
