package com.byyd.middleware.creative.service;

import java.util.List;

import com.adfonic.domain.AdSpace;
import com.adfonic.domain.Creative;
import com.adfonic.domain.Publication;

public interface CreativeEligibilityManager {
    /**
     * This method checks to see if a Creative is eligible for a given AdSpace.
     * If the Creative is not eligible against the AdSpace, the supplied list
     * will be populated with all reasons why not.
     * @param creative the Creative in question
     * @param adSpace the AdSpace in question
     * @param reasonsWhyNot the list (output) that will have inelgibility reasons
     * added to it
     */
    boolean isCreativeEligible(Creative creative, AdSpace adSpace, List<String> reasonsWhyNot);

    /**
     * This method checks to see if a Creative is eligible for a given Publication.
     * If the Creative is not eligible against the Publication, the supplied list
     * will be populated with all reasons why not.
     * @param creative the Creative in question
     * @param pub the Publication in question
     * @param reasonsWhyNot the list (output) that will have inelgibility reasons
     * added to it
     */
    boolean isCreativeEligible(Creative creative, Publication pub, List<String> reasonsWhyNot);
}
