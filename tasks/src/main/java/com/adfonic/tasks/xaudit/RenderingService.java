package com.adfonic.tasks.xaudit;

import java.io.IOException;

import com.adfonic.adserver.TargetingContext;
import com.adfonic.domain.Creative;
import com.adfonic.domain.Publisher;
import com.adfonic.tasks.xaudit.impl.AuditCreativeRenderer.CreativeAssetInfo;

public interface RenderingService {

    RenderedCreative renderContent(Creative creative, Publisher publisher) throws IOException;

    public static class RenderedCreative {

        private final String markup;

        private final String destinationUrl;

        private final CreativeAssetInfo assetInfo;

        private final TargetingContext context;

        public RenderedCreative(CreativeAssetInfo assetInfo, String markup, String destinationUrl, TargetingContext context) {
            this.assetInfo = assetInfo;
            this.markup = markup;
            this.destinationUrl = destinationUrl;
            this.context = context;
        }

        public CreativeAssetInfo getAssetInfo() {
            return assetInfo;
        }

        public String getMarkup() {
            return markup;
        }

        public String getDestinationUrl() {
            return destinationUrl;
        }

        public TargetingContext getContext() {
            return context;
        }

        @Override
        public String toString() {
            return "RenderedCreative {markup=" + markup + ", destinationUrl=" + destinationUrl + ", assetInfo=" + assetInfo + ", context=" + context + "}";
        }

    }
}
