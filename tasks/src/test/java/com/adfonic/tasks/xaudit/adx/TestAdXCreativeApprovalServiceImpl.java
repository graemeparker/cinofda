package com.adfonic.tasks.xaudit.adx;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.jmock.Expectations;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.adfonic.domain.Advertiser;
import com.adfonic.domain.AssetBundle;
import com.adfonic.domain.ContentSpec;
import com.adfonic.domain.Destination;
import com.adfonic.domain.DisplayType;
import com.adfonic.domain.Format;
import com.adfonic.domain.Publisher;
import com.adfonic.domain.PublisherAuditedCreative;
import com.adfonic.tasks.xaudit.RenderingService;
import com.adfonic.test.AbstractAdfonicTest;
import com.byyd.middleware.account.service.PublisherManager;
import com.byyd.middleware.creative.service.CreativeManager;
import com.google.api.services.adexchangebuyer.model.Creative;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;

@Ignore
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/h2-jpa-context.xml" })
public class TestAdXCreativeApprovalServiceImpl extends AbstractAdfonicTest {

    AdXAuditService service;
    PublisherManager publisherManager;
    CreativeManager creativeManager;
    RenderingService renderer;
    AdXCreativeApiManager adXCreativeApiManager;

    @Before
    public void setUp() throws Exception {
        Set<String> sizes = Sets.newHashSet("320x50");
        service = new AdXAuditService(1, sizes, true, 8400000);

        publisherManager = mock(PublisherManager.class);
        creativeManager = mock(CreativeManager.class);
        renderer = mock(RenderingService.class);
        adXCreativeApiManager = mock(AdXCreativeApiManager.class);
        //adXApiManager.setAccountId(71846476);
        inject(adXCreativeApiManager, "accountId", 71846476);
        inject(service, "publisherManager", publisherManager);
        inject(service, "renderingService", renderer);
        inject(service, "adXCreativeApiManager", adXCreativeApiManager);
    }

    @Test
    public void checkAndUpdateCreativeWithStatusUnknown() throws Exception {
        // given
        final Long creativeId = randomLong();
        final Long publisherId = randomLong();
        final com.adfonic.domain.Creative creative = mock(com.adfonic.domain.Creative.class);
        final Publisher publisher = mock(Publisher.class);
        final PublisherAuditedCreative pac = new PublisherAuditedCreative(publisher, creative);
        pac.setStatus(PublisherAuditedCreative.Status.PENDING);
        final String assetExternalId = "byydCreative1111";
        pac.setExternalReference(assetExternalId);
        final Creative adXCreative = new Creative();
        adXCreative.setOpenAuctionStatus("UNKNOWN");

        expect(new Expectations() {
            {
                atLeast(1).of(creativeManager).getCreativeById(creativeId, AdXAuditService.CREATIVE_FETCH_STRATEGY);
                will(returnValue(creative));
                oneOf(publisherManager).getPublisherById(publisherId);
                will(returnValue(publisher));
                oneOf(publisherManager).getPublisherAuditedCreativeByPublisherAndCreative(publisher, creative);
                will(returnValue(pac));
                oneOf(adXCreativeApiManager).getAdxCreative(assetExternalId);
                will(returnValue(adXCreative));
                oneOf(publisherManager).update(pac);
                oneOf(creative).getAssetBundleMap();
                oneOf(creative).getFormat();
            }
        });

        // when
        service.onScheduledCheck(creativeId);

        // then
        assertThat(pac.getStatus(), equalTo(PublisherAuditedCreative.Status.PENDING));
    }

    @Test
    public void checkAndUpdateCreativeWithStatusNotChecked() {
        // given
        final Long creativeId = randomLong();
        final Long publisherId = randomLong();
        final String buyerCreativeId = "2d25e539-93dc-4d8d-ac74-2b44adc5029f";
        //9e8a7962-09c1-4dd8-9711-0078bb411520, 501c8e19-62ce-42d4-84ab-aafde5959103, d7b6d76e-ab0b-4188-a196-87b42baae481
        final Publisher publisher = mock(Publisher.class);
        final com.adfonic.domain.Creative creative = mock(com.adfonic.domain.Creative.class);
        final PublisherAuditedCreative pac = new PublisherAuditedCreative(publisher, creative);
        pac.setExternalReference(buyerCreativeId);
        pac.setMessageCount(1l);
        pac.setStatus(PublisherAuditedCreative.Status.PENDING);
        expect(new Expectations() {
            {
                oneOf(creativeManager).getCreativeById(creativeId, AdXAuditService.CREATIVE_FETCH_STRATEGY);
                will(returnValue(creative));
                oneOf(publisherManager).getPublisherById(publisherId);
                will(returnValue(publisher));
                oneOf(publisherManager).getPublisherAuditedCreativeByPublisherAndCreative(publisher, creative);
                will(returnValue(pac));
                oneOf(publisherManager).update(pac);
            }
        });

        // when
        service.onNewCreative(creativeId);
        final Creative adXCreative = new Creative();
        adXCreative.setOpenAuctionStatus("NOT_CHECKED");

        // then
        assertThat(pac.getMessageCount(), equalTo(2l));
        assertThat(pac.getStatus(), equalTo(PublisherAuditedCreative.Status.PENDING));
        //assertThat(AdXCreativeStatus.valueOf(adXCreative.getOpenAuctionStatus()).getAuditedCreativeStatus(), equalTo(PublisherAuditedCreative.Status.PENDING));
    }

    @Test
    public void testSubmitCreative() throws IOException {
        // given
        final com.adfonic.domain.Creative creative = mock(com.adfonic.domain.Creative.class);
        final Publisher publisher = mock(Publisher.class);
        final DisplayType displayType = new DisplayType("xl", "Test standard banner", "someConstraints");
        final com.adfonic.domain.Component component = mock(com.adfonic.domain.Component.class);
        final Format format = mock(Format.class);
        final ContentSpec contentSpec = mock(ContentSpec.class);
        @SuppressWarnings("serial")
        final HashMap<String, String> manifestProps = new HashMap<String, String>() {
            {
                put("width", "300");
                put("height", "50");
            }
        };
        final Destination dest = mock(Destination.class);
        final Advertiser advertiser = mock(Advertiser.class);
        final String creativeExternalReference = randomAlphaNumericString(36);

        expect(new Expectations() {
            {
                oneOf(creative).newAssetBundle(displayType);
            }
        });
        final Map<DisplayType, AssetBundle> assetBundleMap = ImmutableMap.of(displayType, creative.newAssetBundle(displayType));

        expect(new Expectations() {
            {
                allowing(creative).getExternalID();
                will(returnValue(creativeExternalReference));
                oneOf(creative).getAssetBundleMap();
                will(returnValue(assetBundleMap));
                oneOf(creative).getFormat();
                will(returnValue(format));
                oneOf(format).getComponent(0);
                will(returnValue(component));
                oneOf(component).getContentSpec(displayType);
                will(returnValue(contentSpec));
                oneOf(contentSpec).getManifestProperties();
                will(returnValue(manifestProps));
                oneOf(renderer).renderContent(creative, publisher);
                will(returnValue(randomMultiLineString(40)));
                oneOf(creative).getDestination();
                will(returnValue(dest));
                oneOf(dest).getAdvertiser();
                will(returnValue(advertiser));
                oneOf(advertiser).getName();
                will(returnValue("testblah"));
                oneOf(creative).getDestination();
                will(returnValue(dest));
                oneOf(dest).getFinalDestination();
                will(returnValue("http://www.testblah.com"));
                allowing(creative).getId();
                will(returnValue(123456789L));
                //oneOf(adXClient).submitCreative(creative, creativeExternalReference, content);
            }
        });
        // when
        service.onNewCreative(creative.getId());

        // then
        //assertThat(status.getStatus(), equalTo(PublisherAuditedCreative.Status.PENDING));
    }

    @Test
    public void testSubmitCreativeWithIncorrectSize() throws Exception {
        // given
        final com.adfonic.domain.Creative creative = mock(com.adfonic.domain.Creative.class);
        final Publisher publisher = mock(Publisher.class);
        final DisplayType displayType = new DisplayType("s", "small", "someConstraints");

        expect(new Expectations() {
            {
                oneOf(creative).newAssetBundle(displayType);
            }
        });
        final Map<DisplayType, AssetBundle> assetBundleMap = ImmutableMap.of(displayType, creative.newAssetBundle(displayType));

        expect(new Expectations() {
            {
                oneOf(creative).getAssetBundleMap();
                will(returnValue(assetBundleMap));
            }
        });
        // when
        service.onNewCreative(creative.getId());

        // then
        //assertThat(status, equalTo(null));
    }

    @Test(expected = IOException.class)
    public void testSubmitCreativeWithRenderingIssues() throws Exception {
        // given
        final com.adfonic.domain.Creative creative = mock(com.adfonic.domain.Creative.class);
        final Publisher publisher = mock(Publisher.class);
        final DisplayType displayType = new DisplayType("xxl", "Test XL banner", "someConstraints");
        final com.adfonic.domain.Component component = mock(com.adfonic.domain.Component.class);
        final Format format = mock(Format.class);
        final ContentSpec contentSpec = mock(ContentSpec.class);
        @SuppressWarnings("serial")
        final HashMap<String, String> manifestProps = new HashMap<String, String>() {
            {
                put("width", "320");
                put("height", "50");
            }
        };

        expect(new Expectations() {
            {
                oneOf(creative).newAssetBundle(displayType);
            }
        });
        final Map<DisplayType, AssetBundle> assetBundleMap = ImmutableMap.of(displayType, creative.newAssetBundle(displayType));

        expect(new Expectations() {
            {
                oneOf(creative).getAssetBundleMap();
                will(returnValue(assetBundleMap));
                oneOf(creative).getFormat();
                will(returnValue(format));
                oneOf(format).getComponent(0);
                will(returnValue(component));
                oneOf(component).getContentSpec(displayType);
                will(returnValue(contentSpec));
                oneOf(contentSpec).getManifestProperties();
                will(returnValue(manifestProps));
                oneOf(renderer).renderContent(creative, publisher);
                will(throwException(new IOException()));
                //oneOf(adXClient).submitCreative(creative, creativeExternalReference, content);
            }
        });
        // when
        service.onNewCreative(creative.getId());

        // then
        //assertThat(status, equalTo(null));
    }
}
