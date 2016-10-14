package com.adfonic.tasks.combined.consumers;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import com.adfonic.domain.IntegrationType;
import com.adfonic.domain.Medium;
import com.adfonic.domain.Publication;
import com.adfonic.domain.PublicationProvidedInfo;
import com.adfonic.domain.PublicationType;
import com.adfonic.domain.Publisher;
import com.byyd.middleware.common.service.CommonManager;
import com.byyd.middleware.publication.service.PublicationManager;

@RunWith(MockitoJUnitRunner.class)
public class RtbPersistenceHandlerTest {

    @Mock
    private PublicationManager publicationManager;
    @Mock
    private CommonManager commonManager;
    @Mock
    private DormantAdSpaceReactivator dormantAdSpaceReactivator;
    
    @InjectMocks
    private RtbPersistenceHandler testObj = new RtbPersistenceHandler(10);

    @Test
    public void testCreateRtbPublication() {
        
        Publisher publisher = Mockito.mock(Publisher.class);
        PublicationType publicationType = new PublicationType("myPublication", Medium.APPLICATION);
        String rtbId =  "rtbId1";
        String name = "name";
        String urlString = "urlString";
        List<String> iabIds = Arrays.asList("iabId1","iabId2");
        Integer sellerNetworkId = 666;
        
        IntegrationType integrationType = new IntegrationType("name", "systemName");
        Mockito.when(publisher.getDefaultIntegrationType(publicationType)).thenReturn(integrationType);
        
        Mockito.when(publicationManager.create(Mockito.any(Publication.class))).then(new Answer<Publication>() {

            @Override
            public com.adfonic.domain.Publication answer(InvocationOnMock invocation) throws Throwable {
                Object[] args = invocation.getArguments();
                Object arg0 = args[0];
                com.adfonic.domain.Publication p = (Publication) arg0;
                return p;
            }
        });
        
        Publication publication = testObj.createRtbPublication(publisher, publicationType, rtbId, name, urlString, iabIds, sellerNetworkId); 
        Set<PublicationProvidedInfo> ppInfos = publication.getPublicationProvidedInfos();
        Assert.assertEquals(1, ppInfos.size());
    }
    
}