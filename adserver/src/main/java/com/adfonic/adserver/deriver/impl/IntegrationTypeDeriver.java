package com.adfonic.adserver.deriver.impl;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.adfonic.adserver.Parameters;
import com.adfonic.adserver.TargetingContext;
import com.adfonic.adserver.deriver.DeriverManager;
import com.adfonic.domain.cache.DomainCache;
import com.adfonic.domain.cache.dto.adserver.IntegrationTypeDto;
import com.adfonic.domain.cache.dto.adserver.PublicationTypeDto;
import com.adfonic.domain.cache.dto.adserver.adspace.PublicationDto;
import com.adfonic.util.IntegrationTypeUtils;
import com.adfonic.util.Range;

/** Derive the publisher's IntegrationTypeDto from the request */
@Component
public class IntegrationTypeDeriver extends BaseDeriver {

    private static final transient Logger LOG = Logger.getLogger(IntegrationTypeDeriver.class.getName());

    @Autowired
    public IntegrationTypeDeriver(DeriverManager deriverManager) {
        super(deriverManager, TargetingContext.INTEGRATION_TYPE);
    }

    @Override
    public Object getAttribute(String attribute, TargetingContext context) {
        if (!TargetingContext.INTEGRATION_TYPE.equals(attribute)) {
            LOG.warning("Cannot derive attribute: " + attribute);
            return null;
        }

        IntegrationTypeDto integrationType = null;

        // First see if it was specified explicitly on the request
        String systemName = context.getAttribute(Parameters.INTEGRATION_TYPE);
        DomainCache domainCache = context.getDomainCache();
        if (StringUtils.isNotEmpty(systemName)) {
            integrationType = domainCache.getIntegrationTypeBySystemName(systemName);
            if (integrationType == null) {
                // If we still haven't derived one, but the caller passed in r.client,
                // let's try to fall back on pattern matching.
                Matcher matcher = IntegrationTypeUtils.INTEGRATION_TYPE_PATTERN.matcher(systemName);
                if (matcher.matches()) {
                    // We know it's of the expected format, so separate out the prefix and version
                    String prefix = matcher.group(1);
                    String version = matcher.group(2);
                    integrationType = deriveIntegrationTypeBasedOnVersion(prefix, version, domainCache, systemName);
                }

                if (integrationType == null) {
                    LOG.warning("Invalid value for " + Parameters.INTEGRATION_TYPE + ": " + systemName);
                }
            }
        }

        if (integrationType == null && context.getAdSpace() != null) {
            // Fall back on the Publication.defaultIntegrationType, if specified
            PublicationDto pub = context.getAdSpace().getPublication();
            Long integrationTypeId = pub.getDefaultIntegrationTypeId();
            if (integrationTypeId == null) {
                // Fall back on the Publication.publisher.defaultIntegrationType, if specified
                integrationTypeId = pub.getPublisher().getDefaultIntegrationTypeId(pub.getPublicationTypeId());
                if (integrationTypeId == null) {
                    // Fall back on the PublicationType.defaultIntegrationType, if specified
                    PublicationTypeDto pubType = domainCache.getPublicationTypeById(pub.getPublicationTypeId());
                    integrationTypeId = pubType.getDefaultIntegrationTypeId();
                    if (integrationTypeId == null) {
                        LOG.severe("Your database is totally jacked up, dude (PublicationType.defaultIntegrationTypeDto not set?!)");
                    }
                }
            }
            if (integrationTypeId != null) {
                integrationType = domainCache.getIntegrationTypeById(integrationTypeId);
            }
        }

        return integrationType;
    }

    public static IntegrationTypeDto deriveIntegrationTypeBasedOnVersion(String prefix, String version, DomainCache domainCache, String systemName) {
        // See if the version parses out numerically...
        Integer versionValue = IntegrationTypeUtils.parseVersionValue(version);
        IntegrationTypeDto integrationType = null;
        if (versionValue != null) {
            // If we got here, the version parsed out properly
            Map<Range<Integer>, IntegrationTypeDto> rangeMap = domainCache.getIntegrationTypeVersionRangeMapByPrefix(prefix);
            if (rangeMap != null) {
                // Now we iterate, but we're only iterating a very small set (one entry in most cases)
                for (Map.Entry<Range<Integer>, IntegrationTypeDto> entry : rangeMap.entrySet()) {
                    // See if the Range contains the version value
                    if (entry.getKey().contains(versionValue)) {
                        integrationType = entry.getValue(); // it matches
                        if (LOG.isLoggable(Level.FINE)) {
                            LOG.fine("Derived IntegrationTypeDto \"" + integrationType.getSystemName() + "\" from \"" + systemName + "\" with prefix=" + prefix + ", versionValue="
                                    + versionValue);
                        }
                        break;
                    }
                }
            }
        }
        return integrationType;
    }
}
