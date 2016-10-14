package com.adfonic.beans;

import static org.junit.Assert.assertEquals;

import org.jmock.Expectations;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.adfonic.domain.Campaign;
import com.adfonic.domain.Creative;
import com.adfonic.domain.Format;
import com.adfonic.domain.Publication;
import com.adfonic.reporting.EntityResolver;
import com.adfonic.test.AbstractAdfonicTest;
import com.byyd.middleware.campaign.service.CampaignManager;
import com.byyd.middleware.creative.service.CreativeManager;
import com.byyd.middleware.iface.dao.FetchStrategy;
import com.byyd.middleware.publication.service.PublicationManager;

// The JPA metamodel state must be initialized before use, and that requires
// that we activate the persistence context.  The simplest way to do that is
// with a simple EntityManagerFactory config with an H2 in-memory db.
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/h2-jpa-context.xml"})
public class TestBaseBean extends AbstractAdfonicTest {
    private CampaignManager campaignManager;
    private CreativeManager creativeManager;
    private PublicationManager publicationManager;
    private BaseBean baseBean;

    @Before
    public void setup() {
		campaignManager = mock(CampaignManager.class);
		creativeManager = mock(CreativeManager.class);
		publicationManager = mock(PublicationManager.class);
        
        baseBean = new BaseBean();
        inject(baseBean, "campaignManager", campaignManager);
        inject(baseBean, "creativeManager", creativeManager);
        inject(baseBean, "publicationManager", publicationManager);
	}
	
    @Test
    public void testCampaignResolverWithFetchStrategy() {
        final FetchStrategy fs = mock(FetchStrategy.class, "fs");
        final Campaign campaign1 = mock(Campaign.class, "campaign1");
        final long campaignId1 = uniqueLong("campaignId");
        final Campaign campaign2 = mock(Campaign.class, "campaign2");
        final long campaignId2 = uniqueLong("campaignId");

		expect(new Expectations() {{
            // Expect one and only one lookup per id
            oneOf (campaignManager).getObjectById(Campaign.class, campaignId1, fs); will(returnValue(campaign1));
            oneOf (campaignManager).getObjectById(Campaign.class, campaignId2, fs); will(returnValue(campaign2));
        }});

        EntityResolver<Campaign> campaignResolver = baseBean.getCampaignResolver(fs);

        // Do lookups 1000 times, and it should only perform a given lookup once
        for (int k = 0; k < 1000; ++k) {
            assertEquals(campaign1, campaignResolver.getEntityById(campaignId1));
            assertEquals(campaign2, campaignResolver.getEntityById(campaignId2));
        }
    }
	
    @Test
    public void testCampaignResolverWithoutFetchStrategy() {
        final Campaign campaign1 = mock(Campaign.class, "campaign1");
        final long campaignId1 = uniqueLong("campaignId");
        final Campaign campaign2 = mock(Campaign.class, "campaign2");
        final long campaignId2 = uniqueLong("campaignId");

		expect(new Expectations() {{
            // Expect one and only one lookup per id
            oneOf (campaignManager).getObjectById(Campaign.class, campaignId1); will(returnValue(campaign1));
            oneOf (campaignManager).getObjectById(Campaign.class, campaignId2); will(returnValue(campaign2));
        }});

        EntityResolver<Campaign> campaignResolver = baseBean.getCampaignResolver();

        // Do lookups 1000 times, and it should only perform a given lookup once
        for (int k = 0; k < 1000; ++k) {
            assertEquals(campaign1, campaignResolver.getEntityById(campaignId1));
            assertEquals(campaign2, campaignResolver.getEntityById(campaignId2));
        }
    }
	
    @Test
    public void testCreativeResolverWithFetchStrategy() {
        final FetchStrategy fs = mock(FetchStrategy.class, "fs");
        final Creative creative1 = mock(Creative.class, "creative1");
        final long creativeId1 = uniqueLong("creativeId");
        final Creative creative2 = mock(Creative.class, "creative2");
        final long creativeId2 = uniqueLong("creativeId");

		expect(new Expectations() {{
            // Expect one and only one lookup per id
            oneOf (creativeManager).getObjectById(Creative.class, creativeId1, fs); will(returnValue(creative1));
            oneOf (creativeManager).getObjectById(Creative.class, creativeId2, fs); will(returnValue(creative2));
        }});

        EntityResolver<Creative> creativeResolver = baseBean.getCreativeResolver(fs);

        // Do lookups 1000 times, and it should only perform a given lookup once
        for (int k = 0; k < 1000; ++k) {
            assertEquals(creative1, creativeResolver.getEntityById(creativeId1));
            assertEquals(creative2, creativeResolver.getEntityById(creativeId2));
        }
    }
	
    @Test
    public void testCreativeResolverWithoutFetchStrategy() {
        final Creative creative1 = mock(Creative.class, "creative1");
        final long creativeId1 = uniqueLong("creativeId");
        final Creative creative2 = mock(Creative.class, "creative2");
        final long creativeId2 = uniqueLong("creativeId");

		expect(new Expectations() {{
            // Expect one and only one lookup per id
            oneOf (creativeManager).getObjectById(Creative.class, creativeId1); will(returnValue(creative1));
            oneOf (creativeManager).getObjectById(Creative.class, creativeId2); will(returnValue(creative2));
        }});

        EntityResolver<Creative> creativeResolver = baseBean.getCreativeResolver();

        // Do lookups 1000 times, and it should only perform a given lookup once
        for (int k = 0; k < 1000; ++k) {
            assertEquals(creative1, creativeResolver.getEntityById(creativeId1));
            assertEquals(creative2, creativeResolver.getEntityById(creativeId2));
        }
    }
	
    @Test
    public void testFormatResolverWithFetchStrategy() {
        final FetchStrategy fs = mock(FetchStrategy.class, "fs");
        final Format format1 = mock(Format.class, "format1");
        final long formatId1 = randomLong();
        final Format format2 = mock(Format.class, "format2");
        final long formatId2 = formatId1 + 1;

		expect(new Expectations() {{
            // Expect one and only one lookup per id
            oneOf (creativeManager).getObjectById(Format.class, formatId1, fs); will(returnValue(format1));
            oneOf (creativeManager).getObjectById(Format.class, formatId2, fs); will(returnValue(format2));
        }});

        EntityResolver<Format> formatResolver = baseBean.getFormatResolver(fs);

        // Do lookups 1000 times, and it should only perform a given lookup once
        for (int k = 0; k < 1000; ++k) {
            assertEquals(format1, formatResolver.getEntityById(formatId1));
            assertEquals(format2, formatResolver.getEntityById(formatId2));
        }
    }
	
    @Test
    public void testFormatResolverWithoutFetchStrategy() {
        final Format format1 = mock(Format.class, "format1");
        final long formatId1 = randomLong();
        final Format format2 = mock(Format.class, "format2");
        final long formatId2 = formatId1 + 1;

		expect(new Expectations() {{
            // Expect one and only one lookup per id
            oneOf (creativeManager).getObjectById(Format.class, formatId1); will(returnValue(format1));
            oneOf (creativeManager).getObjectById(Format.class, formatId2); will(returnValue(format2));
        }});

        EntityResolver<Format> formatResolver = baseBean.getFormatResolver();

        // Do lookups 1000 times, and it should only perform a given lookup once
        for (int k = 0; k < 1000; ++k) {
            assertEquals(format1, formatResolver.getEntityById(formatId1));
            assertEquals(format2, formatResolver.getEntityById(formatId2));
        }
    }
	
    @Test
    public void testPublicationResolverWithFetchStrategy() {
        final FetchStrategy fs = mock(FetchStrategy.class, "fs");
        final Publication publication1 = mock(Publication.class, "publication1");
        final long publicationId1 = randomLong();
        final Publication publication2 = mock(Publication.class, "publication2");
        final long publicationId2 = publicationId1 + 1;

		expect(new Expectations() {{
            // Expect one and only one lookup per id
            oneOf (publicationManager).getObjectById(Publication.class, publicationId1, fs); will(returnValue(publication1));
            oneOf (publicationManager).getObjectById(Publication.class, publicationId2, fs); will(returnValue(publication2));
        }});

        EntityResolver<Publication> publicationResolver = baseBean.getPublicationResolver(fs);

        // Do lookups 1000 times, and it should only perform a given lookup once
        for (int k = 0; k < 1000; ++k) {
            assertEquals(publication1, publicationResolver.getEntityById(publicationId1));
            assertEquals(publication2, publicationResolver.getEntityById(publicationId2));
        }
    }
	
    @Test
    public void testPublicationResolverWithoutFetchStrategy() {
        final Publication publication1 = mock(Publication.class, "publication1");
        final long publicationId1 = randomLong();
        final Publication publication2 = mock(Publication.class, "publication2");
        final long publicationId2 = publicationId1 + 1;

		expect(new Expectations() {{
            // Expect one and only one lookup per id
            oneOf (publicationManager).getObjectById(Publication.class, publicationId1); will(returnValue(publication1));
            oneOf (publicationManager).getObjectById(Publication.class, publicationId2); will(returnValue(publication2));
        }});

        EntityResolver<Publication> publicationResolver = baseBean.getPublicationResolver();

        // Do lookups 1000 times, and it should only perform a given lookup once
        for (int k = 0; k < 1000; ++k) {
            assertEquals(publication1, publicationResolver.getEntityById(publicationId1));
            assertEquals(publication2, publicationResolver.getEntityById(publicationId2));
        }
    }
}
