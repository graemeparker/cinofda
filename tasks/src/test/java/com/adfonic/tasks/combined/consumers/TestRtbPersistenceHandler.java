package com.adfonic.tasks.combined.consumers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jmock.Expectations;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.adfonic.domain.AdSpace;
import com.adfonic.domain.Format;
import com.adfonic.domain.Publication;
import com.adfonic.domain.PublicationType;
import com.adfonic.domain.Publisher;
import com.adfonic.test.AbstractAdfonicTest;
import com.byyd.middleware.account.service.PublisherManager;
import com.byyd.middleware.common.service.CommonManager;
import com.byyd.middleware.iface.dao.FetchStrategy;
import com.byyd.middleware.publication.service.PublicationManager;

// The JPA metamodel state must be initialized before use, and that requires
// that we activate the persistence context.  The simplest way to do that is
// with a simple EntityManagerFactory config with an H2 in-memory db.
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/h2-jpa-context.xml"})
public class TestRtbPersistenceHandler extends AbstractAdfonicTest {
    private PublicationManager publicationManager;
    private PublisherManager publisherManager;
    private CommonManager commonManager;
    private DormantAdSpaceReactivator dormantAdSpaceReactivator;
    private RtbPersistenceHandler rtbPersistenceHandler;
    
    @Before
    public void runBeforeEachTest() {
        publicationManager = mock(PublicationManager.class);
        publisherManager = mock(PublisherManager.class);
        commonManager = mock(CommonManager.class);
        dormantAdSpaceReactivator = mock(DormantAdSpaceReactivator.class);
        
        rtbPersistenceHandler = new RtbPersistenceHandler(10000);
        inject(rtbPersistenceHandler, "publicationManager", publicationManager);
        inject(rtbPersistenceHandler, "commonManager", commonManager);
        inject(rtbPersistenceHandler, "dormantAdSpaceReactivator", dormantAdSpaceReactivator);
    }

    @Test
    @Ignore
    public void testOnRtbPublicationPersistenceRequest() {
        final long publisherId = randomLong();
        final String rtbId = randomAlphaNumericString(10);
        final String name = randomAlphaNumericString(10);
        final String publicationTypeSystemName = randomAlphaNumericString(10);
        final Publisher publisher = mock(Publisher.class);
        final PublicationType publicationType = mock(PublicationType.class);
        final Publication publication = mock(Publication.class);
        final AdSpace adSpace = mock(AdSpace.class);

        expect(new Expectations() {{
            allowing (publisher).getId(); will(returnValue(publisherId));
            allowing (publisher).getName(); will(returnValue(randomAlphaNumericString(10)));
            allowing (publication).getId(); will(returnValue(randomLong()));
            allowing (adSpace).getStatus(); will(returnValue(AdSpace.Status.VERIFIED));
            
            // Test 3: throw an exception on publisher lookup (for code coverage)
            oneOf (publisherManager).getPublisherById(with(publisherId), with(any(FetchStrategy.class))); will(throwException(new RuntimeException("bummer")));
            
            // Test 4: publisher not found
            oneOf (publisherManager).getPublisherById(with(publisherId), with(any(FetchStrategy.class))); will(returnValue(null));

            // Test 5: publicationType not found
            allowing (publisherManager).getPublisherById(with(publisherId), with(any(FetchStrategy.class))); will(returnValue(publisher));
            oneOf (publicationManager).getPublicationTypeBySystemName(publicationTypeSystemName); will(returnValue(null));

            // Test 6 onward: all dependencies found
            allowing (publicationManager).getPublicationTypeBySystemName(publicationTypeSystemName); will(returnValue(publicationType));
            allowing (publicationManager).getPublicationByPublisherAndRtbId(with(publisher), with(rtbId), with(any(FetchStrategy.class))); will(returnValue(publication));
            allowing (publicationManager).getAllAdSpacesForPublication(publication); will(returnValue(Collections.singletonList(adSpace)));
        }});

        Map msg;
        
        // Test 1: jsonBidRequest missing from the message map
        msg = new HashMap();
        rtbPersistenceHandler.onRtbPublicationPersistenceRequest(msg);

        // Test 2: invalid JSON in jsonBidRequest
        msg = new HashMap() {{
            put("jsonBidRequest", "not valid JSON can't parse this");
        }};
        rtbPersistenceHandler.onRtbPublicationPersistenceRequest(msg);

        // valid JSON and fully populated message map from here on out
        msg = new HashMap() {{
            put("jsonBidRequest", "{}");
            put("publisher.id", publisherId);
            put("publication.rtbId", rtbId);
            put("publication.name", name);
            put("publication.publicationType.systemName", publicationTypeSystemName);
        }};

        // Test 3: exception thrown
        rtbPersistenceHandler.lruLookupMap.clear();
        rtbPersistenceHandler.onRtbPublicationPersistenceRequest(msg);

        // Test 4: publisher not found
        rtbPersistenceHandler.lruLookupMap.clear();
        rtbPersistenceHandler.onRtbPublicationPersistenceRequest(msg);
        
        // Test 5: publicationType not found
        rtbPersistenceHandler.lruLookupMap.clear();
        rtbPersistenceHandler.onRtbPublicationPersistenceRequest(msg);

        // Test 6: all dependencies found
        rtbPersistenceHandler.lruLookupMap.clear();
        rtbPersistenceHandler.onRtbPublicationPersistenceRequest(msg);
        
        // Test 7: already handled (don't clear the lru lookup map)
        rtbPersistenceHandler.onRtbPublicationPersistenceRequest(msg);
    }

    @Test
    public void testOnRtbAdSpaceFormatRequest() {
        final long adSpaceId = randomLong();
        final String formatSystemName = randomAlphaNumericString(10);
        final Format format = mock(Format.class);
        final AdSpace adSpace = mock(AdSpace.class);
        final Set<Format> adSpaceFormats = new HashSet<Format>();
        final Map msg = new HashMap() {{
            put("adSpace.id", adSpaceId);
            put("format.systemName", formatSystemName);
        }};
        
        expect(new Expectations() {{
            allowing (format).getSystemName(); will(returnValue(formatSystemName));
            allowing (adSpace).getId(); will(returnValue(adSpaceId));
            allowing (adSpace).getFormats(); will(returnValue(adSpaceFormats));
            
            // Test 1: throw an exception
            oneOf (commonManager).getFormatBySystemName(formatSystemName); will(throwException(new RuntimeException("bummer")));

            // Test 2: format not found
            oneOf (commonManager).getFormatBySystemName(formatSystemName); will(returnValue(null));

            // Test 3: adSpace not found
            allowing (commonManager).getFormatBySystemName(formatSystemName); will(returnValue(format));
            oneOf (publicationManager).getAdSpaceById(adSpaceId); will(returnValue(null));

            // Test 4: adSpace.formats already contains format
            allowing (publicationManager).getAdSpaceById(adSpaceId); will(returnValue(adSpace));

            // Test 5: adSpace.formats doesn't already contain format
            oneOf (publicationManager).update(adSpace);
        }});

        // Test 1: throw an exception
        adSpaceFormats.clear();
        rtbPersistenceHandler.onRtbAdSpaceAddFormatRequest(msg);
        assertTrue(adSpaceFormats.isEmpty());
        
        // Test 2: format not found
        adSpaceFormats.clear();
        rtbPersistenceHandler.onRtbAdSpaceAddFormatRequest(msg);
        assertTrue(adSpaceFormats.isEmpty());

        // Test 3: adSpace not found
        adSpaceFormats.clear();
        rtbPersistenceHandler.onRtbAdSpaceAddFormatRequest(msg);
        assertTrue(adSpaceFormats.isEmpty());

        // Test 4: adSpace.formats already contains format
        adSpaceFormats.clear();
        adSpaceFormats.add(format);
        rtbPersistenceHandler.onRtbAdSpaceAddFormatRequest(msg);
        assertEquals(1, adSpaceFormats.size());

        // Test 5: adSpace.formats doesn't already contain format
        adSpaceFormats.clear();
        rtbPersistenceHandler.onRtbAdSpaceAddFormatRequest(msg);
        assertEquals(1, adSpaceFormats.size());
        assertTrue(adSpaceFormats.contains(format));
    }

    
    @Test
    public void testGetRtbAdSpaceByPublication() {
        final Publication publication = mock(Publication.class);
        final AdSpace adSpace1 = mock(AdSpace.class, "adSpace1");
        final AdSpace adSpace2 = mock(AdSpace.class, "adSpace2");
        final List<AdSpace> adSpaces = new ArrayList<AdSpace>();

        expect(new Expectations() {{
            allowing (publicationManager).getAllAdSpacesForPublication(publication); will(returnValue(adSpaces));
        }});

        // Test 1: empty
        assertNull(rtbPersistenceHandler.getRtbAdSpaceByPublication(publication));

        // Test 2: single entry
        adSpaces.add(adSpace1);
        assertEquals(adSpace1, rtbPersistenceHandler.getRtbAdSpaceByPublication(publication));

        // Test 3: multiple entries
        adSpaces.add(adSpace2);
        assertEquals(adSpace1, rtbPersistenceHandler.getRtbAdSpaceByPublication(publication));
    }
}
