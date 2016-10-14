package com.byyd.middleware.creative.filter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

import com.adfonic.domain.Feature;
import com.adfonic.domain.MediaType;

public class ExtendedCreativeTypeFilter {

    public enum VisibilityEnum {
        HIDDEN, NOT_HIDDEN
    }

    private Collection<Feature> featuresMustHave;
    private Collection<Feature> featuresMustNotHave;
    private VisibilityEnum visibility;
    private Collection<MediaType> mediaTypesMustHave;
    private Collection<MediaType> mediaTypesMustNotHave;
    private boolean richMedia;
    private boolean thirdParyStandard;

    public Collection<Feature> getFeaturesMustHave() {
        return featuresMustHave;
    }

    public ExtendedCreativeTypeFilter setFeaturesMustHave(Collection<Feature> featuresMustHave) {
        this.featuresMustHave = featuresMustHave;
        return this;
    }

    public Collection<Feature> getFeaturesMustNotHave() {
        return featuresMustNotHave;
    }

    public ExtendedCreativeTypeFilter setFeaturesMustNotHave(Collection<Feature> featuresMustNotHave) {
        this.featuresMustNotHave = featuresMustNotHave;
        return this;
    }

    public VisibilityEnum getVisibility() {
        return visibility;
    }

    public ExtendedCreativeTypeFilter setVisibility(VisibilityEnum visibility) {
        this.visibility = visibility;
        return this;
    }

    public Collection<MediaType> getMediaTypesMustHave() {
        return mediaTypesMustHave;
    }

    public Collection<MediaType> getMediaTypesMustNotHave() {
        return mediaTypesMustNotHave;
    }

    public ExtendedCreativeTypeFilter setRichMedia(boolean richMedia) {
        this.richMedia = richMedia;
        Collection<MediaType> richMediaMediaTypes = getRichMediaMediaTypes();
        if (richMedia) {
            addMediaTypesMustHave(richMediaMediaTypes);
        } else {
            addMediaTypesMustNotHave(richMediaMediaTypes);
        }
        return this;
    }

    public ExtendedCreativeTypeFilter setThirdParyStandard(boolean thirdParyStandard) {
        this.thirdParyStandard = thirdParyStandard;
        Collection<MediaType> thirdParyStandardMediaTypes = getThirdParyStandardMediaTypes();
        if (thirdParyStandard) {
            addMediaTypesMustHave(thirdParyStandardMediaTypes);
        } else {
            addMediaTypesMustNotHave(thirdParyStandardMediaTypes);
        }
        return this;
    }

    public ExtendedCreativeTypeFilter addMustHaveFeature(Feature feature) {
        if (featuresMustHave == null) {
            featuresMustHave = new HashSet<Feature>();
        }
        featuresMustHave.add(feature);
        return this;
    }

    public ExtendedCreativeTypeFilter addMustNotHaveFeature(Feature feature) {
        if (featuresMustNotHave == null) {
            featuresMustNotHave = new HashSet<Feature>();
        }
        featuresMustNotHave.add(feature);
        return this;
    }

    private ExtendedCreativeTypeFilter addMediaTypesMustHave(Collection<MediaType> mediaTypeEnums) {
        if (mediaTypesMustHave == null) {
            mediaTypesMustHave = new ArrayList<MediaType>();
        }
        mediaTypesMustHave.addAll(mediaTypeEnums);
        return this;
    }

    private ExtendedCreativeTypeFilter addMediaTypesMustNotHave(Collection<MediaType> mediaTypeEnums) {
        if (mediaTypesMustNotHave == null) {
            mediaTypesMustNotHave = new ArrayList<MediaType>();
        }
        mediaTypesMustNotHave.addAll(mediaTypeEnums);
        return this;
    }

    /**
     * MAD-3049 Redefined Richmedia
     */
    private Collection<MediaType> getRichMediaMediaTypes() {
        return Collections.singleton(MediaType.HTML_JS);
    }

    /**
     * MAD-3049 redefine 3rd Party Standard
     */
    private Collection<MediaType> getThirdParyStandardMediaTypes() {
        return Arrays.asList(MediaType.HTML, MediaType.STAND_JS);
    }
}
