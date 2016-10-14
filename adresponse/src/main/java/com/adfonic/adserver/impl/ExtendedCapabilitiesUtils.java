package com.adfonic.adserver.impl;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.adfonic.adserver.TargetingContext;
import com.adfonic.domain.ContentForm;
import com.adfonic.domain.MediaType;
import com.adfonic.domain.cache.dto.adserver.ExtendedCreativeTypeDto;
import com.adfonic.domain.cache.dto.adserver.IntegrationTypeDto;

public final class ExtendedCapabilitiesUtils {
    private ExtendedCapabilitiesUtils() {
    }

    /**
     * Get the name of the transform template that can be used to render markup
     * for the given ExtendedCreativeType on the given IntegrationType.
     *
     * @param creativeExtendedType
     *            the ExtendedCreativeType that needs to be rendered
     * @param integrationType
     *            the IntegrationType requesting the ad
     * @param bidContentForms
     *            the set of ContentForms to restrict to
     * @param context
     * @return the name of the template (i.e. .vtl file name), or null if no
     *         transform is available
     */
    public static String getTransformTemplate(ExtendedCreativeTypeDto creativeExtendedType, IntegrationTypeDto integrationType, Set<ContentForm> bidContentForms,
            TargetingContext context) {
        // 1. from 
        Set<ContentForm> publicationContentForms = integrationType.getSupportedContentForms(creativeExtendedType.getMediaType());
        if (bidContentForms != null) {
            publicationContentForms = intersectionOf(publicationContentForms, bidContentForms);
        }
        // 3. from extended type map of 
        for (Map.Entry<ContentForm, String> entry : creativeExtendedType.getTemplateMap().entrySet()) {
            ContentForm contentForm = entry.getKey();
            if (publicationContentForms.contains(contentForm)) {
                if (context != null) {
                    context.setAttribute(TargetingContext.RENDERED_TRANSFORM, contentForm);
                }
                return entry.getValue();
            }
        }
        return null;
    }

    /**
     * Get the name of the transform template that can be used to render markup
     * for a creative with Dynamic ExtendedTemplates.
     *
     * @param publicationTemplates
     *            the map of ExtendedCreativeType for ContentForm
     * @param bidContentForms
     *            the set of ContentForms to restrict to
     * @param context
     * @return the template preprocessed, or null if no transform is available
     */
    public static String getDynamicTemplate(Map<ContentForm, String> creativeTemplateMap, ExtendedCreativeTypeDto creativeExtendedType, IntegrationTypeDto integrationType,
            Set<ContentForm> bidContentForms, TargetingContext context) {
        // 1. from extended/integration type
        MediaType mediaType = creativeExtendedType.getMediaType();
        Set<ContentForm> supportedContentForms = integrationType.getSupportedContentForms(mediaType);
        // 2. from bid request
        if (bidContentForms != null) {
            supportedContentForms = intersectionOf(supportedContentForms, bidContentForms);
        }
        // 3. from creative templates
        for (Map.Entry<ContentForm, String> entry : creativeTemplateMap.entrySet()) {
            ContentForm contentForm = entry.getKey();
            if (supportedContentForms.contains(contentForm)) {
                if (context != null) {
                    context.setAttribute(TargetingContext.RENDERED_TRANSFORM, contentForm);
                }
                return entry.getValue(); //return template markup
            }
        }
        return null;
    }

    /**
     * More like a generic utility set intersection method needed as
     * CollectionUtils.intersection returns ArrayList Providing the method here
     * as it is currently used for ContentForms which are ExtendedCapabilities
     * related
     *
     * @param ideallySmaller
     *            Set 1; when large sets are involved, it better be the smaller
     *            one
     * @param ideallyLarger
     *            Set 2; when large sets are involved, it better be the larger
     *            one
     * @return new intersection set
     */
    public static <T> Set<T> intersectionOf(Set<T> ideallyLarger, Set<T> ideallySmaller) {
        Set<T> intersectionSet = new HashSet<T>(ideallySmaller);
        intersectionSet.retainAll(ideallyLarger);
        return intersectionSet;
    }
}
