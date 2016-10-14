package com.byyd.middleware.publication.service;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.jmock.Expectations;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.adfonic.domain.Publication;
import com.adfonic.test.AbstractAdfonicTest;
import com.byyd.middleware.publication.dao.AdSpaceDao;
import com.byyd.middleware.publication.filter.AdSpaceFilter;
import com.byyd.middleware.publication.service.jpa.PublicationManagerJpaImpl;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/h2-db-context.xml"})
public class TestPublicationManagerJpaImpl extends AbstractAdfonicTest {
    private AdSpaceDao adSpaceDao;
    private PublicationManagerJpaImpl publicationManagerJpaImpl;

    @Before
    public void runBeforeEachTest() {
        adSpaceDao = mock(AdSpaceDao.class);
        
        publicationManagerJpaImpl = new PublicationManagerJpaImpl();
        inject(publicationManagerJpaImpl, "adSpaceDao", adSpaceDao);
    }
    
    // Publication has been approved
    @Test
    public void testCanPublicationBePhysicallyDeleted01_approved() {
        final Publication publication = mock(Publication.class);
        expect(new Expectations() {{
            oneOf (publication).isApproved(); will(returnValue(true));
        }});
        assertFalse(publicationManagerJpaImpl.canPublicationBePhysicallyDeleted(publication));
    }

    // Publication has not been approved, no VERIFIED or DORMANT adspaces
    @Test
    public void testCanPublicationBePhysicallyDeleted02_notApproved_hasNone() {
        final Publication publication = mock(Publication.class);
        expect(new Expectations() {{
            oneOf (publication).isApproved(); will(returnValue(false));
            oneOf (adSpaceDao).countAll(with(any(AdSpaceFilter.class))); will(returnValue(0L));
        }});
        assertTrue(publicationManagerJpaImpl.canPublicationBePhysicallyDeleted(publication));
    }        

    // Publication has not been approved, has at least one VERIFIED or DORMANT adspace
    @Test
    public void testCanPublicationBePhysicallyDeleted03_notApproved_hasSome() {
        final Publication publication = mock(Publication.class);
        expect(new Expectations() {{
            oneOf (publication).isApproved(); will(returnValue(false));
            oneOf (adSpaceDao).countAll(with(any(AdSpaceFilter.class))); will(returnValue(1L));
        }});
        assertFalse(publicationManagerJpaImpl.canPublicationBePhysicallyDeleted(publication));
    }
}
