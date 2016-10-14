package com.adfonic.domain;

import java.util.Collections;
import java.util.List;

import com.adfonic.util.Described;
import com.adfonic.util.EnumUtils;

/**
 * A Content Form is a certain way of expressing an advertisement, such
 * as "ORMMA compliant", "MM4RM compliant" or "with Doubleclick macros".
 * It is used to describe possible targets for creatives and accepted
 * forms for publishers.
 */
public enum ContentForm implements Described {
    SIMPLE("Simple"),
    STRICT_XHTML("Strict XHTML"),
    ORMMA_LEVEL1("ORMMA Level 1"),
    ORMMA_LEVEL1_MM4RM("ORMMA Level 1 (MM4RM)"),
    MRAID_1_0("MRAID 1.0"),
    ADFONIC_MACRO("Adfonic Macro"),
    DOUBLECLICK_BANNER("Doubleclick Banner"),
    MEDIAMIND_BANNER("Mediamind Banner"),
    MADVIEW("Mobclix MadView"),
    JIWIRE("JiWire"),
    MOBILE_WEB("Mobile Web"),
    NEXAGE_MM4RM_IN_APP("Nexage MM4RM In-App"),
    NEXAGE_MM4RM_MOBILE_WEB("Nexage MM4RM Mobile Web"),
    ADMARVEL_IN_APP("AdMarvel In-App"),
    ADMARVEL_MOBILE_WEB("AdMarvel Mobile Web"),
    NEXAGE_MOBILE_WEB("Nexage Mobile Web"),
    VAST_2_0("VAST 2.0");
    
    private String description;
    
    private ContentForm(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
    
    public static void sortByDescription(List<ContentForm> contentForms) {
        Collections.sort(contentForms, EnumUtils.DESCRIPTION_COMPARATOR);
    }
}

