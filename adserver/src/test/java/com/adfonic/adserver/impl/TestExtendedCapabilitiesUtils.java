package com.adfonic.adserver.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.assertj.core.api.Assertions;
import org.junit.Test;

import com.adfonic.domain.ContentForm;
import com.adfonic.domain.MediaType;
import com.adfonic.domain.cache.dto.adserver.ExtendedCreativeTypeDto;
import com.adfonic.domain.cache.dto.adserver.IntegrationTypeDto;

public class TestExtendedCapabilitiesUtils {

    @Test
    public void testGetTransformTemplate() {
        String madviewTemplate = "celtra_madview.vtl";
        String ormmaLevel1Mm4rmTemplate = "celtra_ormma_level1_mm4rm.vtl";
        String mobileWebTemplate = "celtra_mobile_web.vtl";

        String etName = "Celtra " + System.currentTimeMillis();
        ExtendedCreativeTypeDto et = new ExtendedCreativeTypeDto();
        et.setName(etName);
        et.setMediaType(MediaType.HTML_JS);
        //et.getFeatures().add(Feature.RICH_MEDIA);
        et.getTemplateMap().put(ContentForm.MADVIEW, madviewTemplate);
        et.getTemplateMap().put(ContentForm.ORMMA_LEVEL1_MM4RM, ormmaLevel1Mm4rmTemplate);
        et.getTemplateMap().put(ContentForm.MOBILE_WEB, mobileWebTemplate);

        String itSystemName = "IntegrationType" + System.currentTimeMillis();
        IntegrationTypeDto it = new IntegrationTypeDto();
        it.setName(itSystemName);
        it.setSystemName(itSystemName);
        /*
        it.getSupportedFeatures().add(Feature.RICH_MEDIA);
        it.getSupportedBeaconModes().add(BeaconMode.MARKUP);
        it.getSupportedBeaconModes().add(BeaconMode.METADATA);
        */

        it.getContentFormsByMediaType().clear();
        assertNull(ExtendedCapabilitiesUtils.getTransformTemplate(et, it, null, null));

        it.getContentFormsByMediaType().clear();
        it.addSupportedContentForm(MediaType.HTML_JS, ContentForm.ORMMA_LEVEL1);
        assertNull(ExtendedCapabilitiesUtils.getTransformTemplate(et, it, null, null));

        it.getContentFormsByMediaType().clear();
        it.addSupportedContentForm(MediaType.HTML_JS, ContentForm.MADVIEW);
        assertEquals(madviewTemplate, ExtendedCapabilitiesUtils.getTransformTemplate(et, it, null, null));

        it.getContentFormsByMediaType().clear();
        it.addSupportedContentForm(MediaType.HTML_JS, ContentForm.ORMMA_LEVEL1_MM4RM);
        assertEquals(ormmaLevel1Mm4rmTemplate, ExtendedCapabilitiesUtils.getTransformTemplate(et, it, null, null));

        it.getContentFormsByMediaType().clear();
        it.addSupportedContentForm(MediaType.HTML_JS, ContentForm.MOBILE_WEB);
        assertEquals(mobileWebTemplate, ExtendedCapabilitiesUtils.getTransformTemplate(et, it, null, null));
    }

    @Test
    public void testGetTransformDynamicTemplate() {

        //todo test with new dynamic template creatives

        String madviewTemplate = "<script type=\"text/javascript\">madview</script>";
        String ormmaLevel1Mm4rmTemplate = "<script type=\"text/javascript\">ormma_level1_mm4rm</script>";
        String mobileWebTemplate = "<script type=\"text/javascript\">mobileWeb</script>";

        ExtendedCreativeTypeDto et = new ExtendedCreativeTypeDto();
        et.setName("Celtra " + System.currentTimeMillis());
        et.setMediaType(MediaType.HTML_JS);
        et.setUseDynamicTemplates(true);

        IntegrationTypeDto it = new IntegrationTypeDto();
        it.setName("IntegrationType" + System.currentTimeMillis());
        it.getContentFormsByMediaType().clear();
        it.addSupportedContentForm(MediaType.HTML_JS, ContentForm.MOBILE_WEB);

        Map<ContentForm, String> dynTemplates = new HashMap<>();
        dynTemplates.put(ContentForm.MADVIEW, madviewTemplate);
        dynTemplates.put(ContentForm.ORMMA_LEVEL1_MM4RM, ormmaLevel1Mm4rmTemplate);
        dynTemplates.put(ContentForm.MOBILE_WEB, mobileWebTemplate);
        assertEquals(mobileWebTemplate, ExtendedCapabilitiesUtils.getDynamicTemplate(dynTemplates, et, it, null, null));
    }

    @Test
    public void creative_template() {
        Map<ContentForm, String> creativeTemplateMap = new HashMap<ContentForm, String>();
        ExtendedCreativeTypeDto creativeExtendedType = new ExtendedCreativeTypeDto();
        creativeExtendedType.setId(1l);
        IntegrationTypeDto integrationType = new IntegrationTypeDto();
        integrationType.setId(1l);
        Set<ContentForm> bidContentForms = null;

        // When - all empty
        String dynamicTemplate = ExtendedCapabilitiesUtils.getDynamicTemplate(creativeTemplateMap, creativeExtendedType, integrationType, bidContentForms, null);
        // Then - just null, not exception
        Assertions.assertThat(dynamicTemplate).isNull();

        //And...

        //Given - integrate MRAID_1_0 and MediaType.HTML_JS (usual pattern)
        creativeTemplateMap.put(ContentForm.MRAID_1_0, "ContentForm.MRAID_1_0 MediaType.HTML_JS template content...");
        creativeExtendedType.setMediaType(MediaType.HTML_JS);
        integrationType.addSupportedContentForm(MediaType.HTML_JS, ContentForm.MRAID_1_0);
        // When 
        dynamicTemplate = ExtendedCapabilitiesUtils.getDynamicTemplate(creativeTemplateMap, creativeExtendedType, integrationType, bidContentForms, null);
        // Then
        Assertions.assertThat(dynamicTemplate).isEqualTo("ContentForm.MRAID_1_0 MediaType.HTML_JS template content...");

        //And...

        //Given - empty = restrict to no nothing
        bidContentForms = new HashSet<ContentForm>();
        // When 
        dynamicTemplate = ExtendedCapabilitiesUtils.getDynamicTemplate(creativeTemplateMap, creativeExtendedType, integrationType, bidContentForms, null);
        // Then
        Assertions.assertThat(dynamicTemplate).isNull();

        // And...
        // Allow Bid request to ContentForm.MOBILE_WEB
        bidContentForms.add(ContentForm.MOBILE_WEB);
        // When 
        dynamicTemplate = ExtendedCapabilitiesUtils.getDynamicTemplate(creativeTemplateMap, creativeExtendedType, integrationType, bidContentForms, null);
        // Then - nope
        Assertions.assertThat(dynamicTemplate).isNull();

        // And...
        // Allow Bid request to ContentForm.MRAID_1_0
        bidContentForms.add(ContentForm.MRAID_1_0);
        // When 
        dynamicTemplate = ExtendedCapabilitiesUtils.getDynamicTemplate(creativeTemplateMap, creativeExtendedType, integrationType, bidContentForms, null);
        // Then - yes!
        Assertions.assertThat(dynamicTemplate).isEqualTo("ContentForm.MRAID_1_0 MediaType.HTML_JS template content...");
    }

}
