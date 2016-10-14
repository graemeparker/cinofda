package com.adfonic.adserver.controller.dbg.dto;


/**
 * 
 * @author mvanek
 *
 */
public class DbgDomainCacheDto {

    private DbgCacheMetaDto metaData;

    /*
        private Collection<FormatDto> formats;

        private Collection<IntegrationTypeDto> integrationTypes;
    */
    public DbgCacheMetaDto getMetaData() {
        return metaData;
    }

    public void setMetaData(DbgCacheMetaDto metaData) {
        this.metaData = metaData;
    }
    /*
        public Collection<FormatDto> getFormats() {
            return formats;
        }

        public void setFormats(Collection<FormatDto> formats) {
            this.formats = formats;
        }

        public Collection<IntegrationTypeDto> getIntegrationTypes() {
            return integrationTypes;
        }

        public void setIntegrationTypes(Collection<IntegrationTypeDto> integrationTypes) {
            this.integrationTypes = integrationTypes;
        }
    */
}
