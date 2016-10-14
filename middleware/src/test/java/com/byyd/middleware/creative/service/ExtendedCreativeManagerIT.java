package com.byyd.middleware.creative.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.adfonic.domain.Campaign;
import com.adfonic.domain.ClickTokenReference;
import com.adfonic.domain.ContentForm;
import com.adfonic.domain.Creative;
import com.adfonic.domain.ExtendedCreativeTemplate;
import com.adfonic.domain.ExtendedCreativeTemplate_;
import com.adfonic.domain.ExtendedCreativeType;
import com.adfonic.domain.ExtendedCreativeTypeMacro;
import com.adfonic.domain.ExtendedCreativeType_;
import com.adfonic.domain.Feature;
import com.adfonic.domain.Format;
import com.adfonic.domain.MediaType;
import com.adfonic.domain.Segment;
import com.adfonic.test.AbstractAdfonicTest;
import com.byyd.middleware.campaign.service.CampaignManager;
import com.byyd.middleware.campaign.service.TargetingManager;
import com.byyd.middleware.common.service.CommonManager;
import com.byyd.middleware.creative.filter.ExtendedCreativeTypeFilter;
import com.byyd.middleware.creative.filter.ExtendedCreativeTypeFilter.VisibilityEnum;
import com.byyd.middleware.iface.dao.FetchStrategy;
import com.byyd.middleware.iface.dao.jpa.FetchStrategyBuilder;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/adfonic-springdata-hibernate-context.xml"})
@DirtiesContext
public class ExtendedCreativeManagerIT extends AbstractAdfonicTest {

    @Autowired
    private ExtendedCreativeManager extendedCreativeManager;
    
    @Autowired
    private CreativeManager creativeManager;
    
    @Autowired
    private CampaignManager campaignManager;
    
    @Autowired
    private TargetingManager targetingManager;
    
    @Autowired
    private CommonManager commonManager;
    
    //----------------------------------------------------------------------------------------------------------------

    @Test
    public void testExtendedCreativeType() {
        String name = "Testing" + System.currentTimeMillis();
        MediaType mediaType = MediaType.BANNER_HTML;
        ExtendedCreativeType contentSpec = null;
        try {
            contentSpec = extendedCreativeManager.newExtendedCreativeType(name, mediaType);
            assertNotNull(contentSpec);
            assertTrue(contentSpec.getId() > 0);

            assertEquals(contentSpec, extendedCreativeManager.getExtendedCreativeTypeById(contentSpec.getId()));
            assertEquals(contentSpec, extendedCreativeManager.getExtendedCreativeTypeById(Long.toString(contentSpec.getId())));

            List<ExtendedCreativeType> list = extendedCreativeManager.getAllExtendedCreativeTypes();
            assertNotNull(list);
            assertTrue(list.size() > 0);
            assertTrue(list.contains(contentSpec));

        } catch(Exception e) {
            String stackTrace = ExceptionUtils.getStackTrace(e);
            System.out.println(stackTrace);
            fail(stackTrace);
        } finally {
            extendedCreativeManager.delete(contentSpec);
            assertNull(extendedCreativeManager.getExtendedCreativeTypeById(contentSpec.getId()));
        }
    }
    
    @Test
    public void testExtendedCreativeTypes() {
        try {
            FetchStrategy ectfs = new FetchStrategyBuilder()
                                  .addLeft(ExtendedCreativeType_.features)
                                  .build();
            List<ExtendedCreativeType> list = null;
             
            System.out.println("Beacon AND Rich Media");
            ExtendedCreativeTypeFilter filter = new ExtendedCreativeTypeFilter()
                .addMustHaveFeature(Feature.BEACON)
                .setRichMedia(true)
                .setVisibility(VisibilityEnum.NOT_HIDDEN);
            list = extendedCreativeManager.getAllExtendedCreativeTypes(filter, ectfs);
            for(ExtendedCreativeType t : list) {
                displayExtendedCreativeType(t);
                assertTrue(t.getFeatures().contains(Feature.BEACON) && t.getMediaType().equals(MediaType.HTML_JS));
                assertFalse(t.isHidden());
            }
            System.out.println("------------------------------------------------------------------------------------------");
            
            System.out.println("Beacon OR Rich Media");
            list = extendedCreativeManager.getAllExtendedCreativeTypes(
                        new ExtendedCreativeTypeFilter()
                        .addMustHaveFeature(Feature.BEACON)
                        .setRichMedia(true)
                        .setVisibility(VisibilityEnum.NOT_HIDDEN)
                    , ectfs);
            for(ExtendedCreativeType t : list) {
                displayExtendedCreativeType(t);
                assertTrue(t.getFeatures().contains(Feature.BEACON) || t.getMediaType().equals(MediaType.HTML_JS));
                assertFalse(t.isHidden());
            }
            System.out.println("------------------------------------------------------------------------------------------");

            System.out.println("Beacon AND NOT Rich Media");
            list = extendedCreativeManager.getAllExtendedCreativeTypes(
                        new ExtendedCreativeTypeFilter()
                        .addMustHaveFeature(Feature.BEACON)
                        .setRichMedia(false)
                        .setVisibility(VisibilityEnum.NOT_HIDDEN)
                    , ectfs);
            for(ExtendedCreativeType t : list) {
                displayExtendedCreativeType(t);
                assertTrue(t.getFeatures().contains(Feature.BEACON) && !t.getMediaType().equals(MediaType.HTML_JS));
                assertFalse(t.isHidden());
            }
            System.out.println("------------------------------------------------------------------------------------------");

            System.out.println("Rich Media");
            ExtendedCreativeTypeFilter richMediaFilter = new ExtendedCreativeTypeFilter().setRichMedia(true).setVisibility(VisibilityEnum.NOT_HIDDEN);
            list = extendedCreativeManager.getAllExtendedCreativeTypes(richMediaFilter, ectfs);
            for(ExtendedCreativeType t : list) {
                displayExtendedCreativeType(t);
                assertTrue(t.getMediaType().equals(MediaType.HTML_JS));
                assertFalse(t.isHidden());
            }
            System.out.println("------------------------------------------------------------------------------------------");
            
            System.out.println("NOT Rich Media");
            ExtendedCreativeTypeFilter notRichMediaFilter = new ExtendedCreativeTypeFilter().setRichMedia(false).setVisibility(VisibilityEnum.NOT_HIDDEN);
            list = extendedCreativeManager.getAllExtendedCreativeTypes(notRichMediaFilter, ectfs);
            for(ExtendedCreativeType t : list) {
                displayExtendedCreativeType(t);
                assertTrue(!t.getMediaType().equals(MediaType.HTML_JS));
                assertFalse(t.isHidden());
            }
            System.out.println("------------------------------------------------------------------------------------------");
        } catch(Exception e) {
            String stackTrace = ExceptionUtils.getStackTrace(e);
            System.out.println(stackTrace);
            fail(stackTrace);
        } finally {
        }
    }
    
    protected void displayExtendedCreativeType(ExtendedCreativeType t) {
        System.out.print(t.getName() + ": " + t.getMediaType().name() + " - ");
        for(Feature feature : t.getFeatures()) {
            System.out.print(feature.toString() + " - ");
        }
        System.out.println();
    }

    //----------------------------------------------------------------------------------------
    
    @Test
    public void testExtendedCreativeTemplate() {
        FetchStrategy extendedCreativeTemplateFs = new FetchStrategyBuilder()
                           .addInner(ExtendedCreativeTemplate_.creative)
                           .build();
        FetchStrategy extendedCreativeTypeFs = new FetchStrategyBuilder()
                                               .addLeft(ExtendedCreativeType_.macros)
                                               .build();
        
        Campaign campaign = campaignManager.getCampaignById(1L);
        Segment segment = targetingManager.getSegmentById(2L);
        Format format = commonManager.getFormatById(1L);
        String creativeName = "TestCreative-" + randomHexString(20);
        String templateContent = null;
        ContentForm contentForm = ContentForm.ADFONIC_MACRO;
        assertTrue(creativeManager.isCreativeNameUnique(creativeName, campaign, null));
        Creative creative = null;
        try {
            List<ClickTokenReference> tokens = extendedCreativeManager.getAllClickTokenReferences();
            StringBuffer buffer = new StringBuffer();
            // We're doing this 3 times, to ensure this is indeed a replace all
            for(int i = 0;i < 3;i++) {
                for(ClickTokenReference token : tokens) {
                    String target = token.getToken();
                    buffer.append(randomHexString(20) + " " + target + " " + randomHexString(20) + "\n");
                }
            }
             
            creative = creativeManager.newCreative(campaign, segment, format, creativeName);
            ExtendedCreativeType extendedCreativeType = extendedCreativeManager.getExtendedCreativeTypeById(8L, extendedCreativeTypeFs);
            creative.setExtendedCreativeType(extendedCreativeType);
            creative = creativeManager.update(creative);
            
            for(int i = 0;i < 3;i++) {
                for(ExtendedCreativeTypeMacro macro : extendedCreativeType.getMacros()) {
                    String target = macro.getMatchString();
                    buffer.append(randomHexString(20) + " " + target + " " + randomHexString(20) + "\n");
                }
            }
            
            // add a few URLs too
            String url1 = "<A HREF='HTTP://bs.serving-sys.com/BurstingPipe/mobileRedirect.bs?cn=mbr&pli=2277468&PluID=0&ord=[timestamp]'><img src='http://bs.serving-sys.com/BurstingPipe/adServer.bs?cn=mb&c=26&pli=2277468&PluID=0&f=5&ord=[timestamp]'></a>";
            String expectedUrl1 = "<a href='${adComponents.destinationUrl}?redir=http%3A%2F%2Fbs.serving-sys.com%2FBurstingPipe%2FmobileRedirect.bs%3Fcn%3Dmbr%26pli%3D2277468%26PluID%3D0%26ord%3D${macros.timestamp}'><img src='http://bs.serving-sys.com/BurstingPipe/adServer.bs?cn=mb&c=26&pli=2277468&PluID=0&f=5&ord=${macros.timestamp}'></a>";
            String url2 = "<a href=\"http://bs.serving-sys.com/BurstingPipe/mobileRedirect.bs?cn=mbr&pli=2277468&PluID=0&ord=[timestamp]\"><img src=\"http://bs.serving-sys.com/BurstingPipe/adServer.bs?cn=mb&c=26&pli=2277468&PluID=0&f=5&ord=[timestamp]\"></a>";
            String expectedUrl2 = "<a href=\"${adComponents.destinationUrl}?redir=http%3A%2F%2Fbs.serving-sys.com%2FBurstingPipe%2FmobileRedirect.bs%3Fcn%3Dmbr%26pli%3D2277468%26PluID%3D0%26ord%3D${macros.timestamp}\"><img src=\"http://bs.serving-sys.com/BurstingPipe/adServer.bs?cn=mb&c=26&pli=2277468&PluID=0&f=5&ord=${macros.timestamp}\"></a>";
            
            buffer.append(url1 + "\n\n");
            buffer.append(url2 + "\n\n");

            assertTrue(extendedCreativeManager.getAllExtendedCreativeTemplatesForCreative(creative).isEmpty());
            
               templateContent = buffer.toString();
            
            System.out.println("Template content: " + templateContent);
            ExtendedCreativeTemplate template = extendedCreativeManager.newExtendedCreativeTemplate(creative, contentForm, templateContent);
            assertNotNull(template);
            assertTrue(template.getId() > 0);

            template = extendedCreativeManager.getExtendedCreativeTemplateById(template.getId(), extendedCreativeTemplateFs);
            System.out.println("Template preprocessed content: " + template.getTemplatePreprocessed());
            assertNotNull(template);
            assertEquals(template.getCreative(), creative);
            assertEquals(template.getContentForm(), contentForm);
            assertEquals(template.getTemplateOriginal(), templateContent);
            
            for(ClickTokenReference token : tokens) {
                String target = token.getToken();
                assertTrue(template.getTemplatePreprocessed().indexOf(target) == -1);
                String replacement = extendedCreativeManager.getSubstitutionStringForClickTokenReference(token);
                assertFalse(template.getTemplatePreprocessed().indexOf(replacement) == -1);
            }
            
            for(ExtendedCreativeTypeMacro macro : extendedCreativeType.getMacros()) {
                assertTrue(template.getTemplatePreprocessed().indexOf(macro.getMatchString()) == -1);
                assertFalse(template.getTemplatePreprocessed().indexOf(macro.getReplacementString()) == -1);
            }
            
            assertTrue(template.getTemplatePreprocessed().indexOf(url1) == -1);
            assertFalse(template.getTemplatePreprocessed().indexOf(expectedUrl1) == -1);
            assertTrue(template.getTemplatePreprocessed().indexOf(url2) == -1);
            assertFalse(template.getTemplatePreprocessed().indexOf(expectedUrl2) == -1);
           
            assertTrue(template.getTemplatePreprocessed().endsWith(ExtendedCreativeTemplate.beaconTemplate));
           
            assertEquals(1, extendedCreativeManager.getAllExtendedCreativeTemplatesForCreative(creative).size());
            assertTrue(extendedCreativeManager.getAllExtendedCreativeTemplatesForCreative(creative).contains(template));
                        
            String templateContentChanged = templateContent + "-Changed";
            template.setTemplateOriginal(templateContentChanged);
            template = extendedCreativeManager.update(template);
            template = extendedCreativeManager.getExtendedCreativeTemplateById(template.getId(), extendedCreativeTemplateFs);
            assertEquals(template.getTemplatePreprocessed(), extendedCreativeManager.processExtendedCreativeTemplateContent(template, creative));
            
            Map<ContentForm, ExtendedCreativeTemplate> map = extendedCreativeManager.getExtendedCreativeTemplatesMapForCreative(creative, extendedCreativeTemplateFs);
            assertEquals(map.get(contentForm), template);
            
            extendedCreativeManager.delete(template);
            assertNull(extendedCreativeManager.getExtendedCreativeTemplateById(template.getId()));
            assertEquals(0, extendedCreativeManager.getAllExtendedCreativeTemplatesForCreative(creative).size());
            assertFalse(extendedCreativeManager.getAllExtendedCreativeTemplatesForCreative(creative).contains(template));

        } finally {
            if (creative != null) {
                creativeManager.delete(creative);
            }
        }
    }
    
  //----------------------------------------------------------------------------------------------------------------

    @Test
    public void testClickTokenReference() {
        String token = "%testing" + System.currentTimeMillis() + "%";
        String type = "UUID";
        String exampleOutput = "11111111-1111-1111-1111-111111111111";
        String description = "This is a test";
        ClickTokenReference clickTokenReference = null;
        try {
            clickTokenReference = extendedCreativeManager.newClickTokenReference(token, type, exampleOutput, description); 
            assertNotNull(clickTokenReference);
            assertTrue(clickTokenReference.getId() > 0);
            assertEquals(clickTokenReference.getToken(), token);
            assertEquals(clickTokenReference.getType(), type);
            assertEquals(clickTokenReference.getExampleOutput(), exampleOutput);
            assertEquals(clickTokenReference.getDescription(), description);

            assertEquals(clickTokenReference, extendedCreativeManager.getClickTokenReferenceById(clickTokenReference.getId()));
            assertEquals(clickTokenReference, extendedCreativeManager.getClickTokenReferenceById(Long.toString(clickTokenReference.getId())));

            String newToken = token + "Changed";
            clickTokenReference.setToken(newToken);
            clickTokenReference = extendedCreativeManager.update(clickTokenReference);
            clickTokenReference = extendedCreativeManager.getClickTokenReferenceById(clickTokenReference.getId());
            assertEquals(clickTokenReference.getToken(), newToken);

            List<ClickTokenReference> list = extendedCreativeManager.getAllClickTokenReferences();
            assertNotNull(list);
            assertTrue(list.size() > 0);
            assertTrue(list.contains(clickTokenReference));

        } catch(Exception e) {
            String stackTrace = ExceptionUtils.getStackTrace(e);
            System.out.println(stackTrace);
            fail(stackTrace);
        } finally {
            extendedCreativeManager.delete(clickTokenReference);
            assertNull(extendedCreativeManager.getClickTokenReferenceById(clickTokenReference.getId()));
        }
    }
}
