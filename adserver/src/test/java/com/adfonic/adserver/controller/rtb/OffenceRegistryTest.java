package com.adfonic.adserver.controller.rtb;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import com.adfonic.adserver.AdServerFeatureFlag;
import com.adfonic.adserver.AdSrvCounter;
import com.adfonic.adserver.offence.OffenceRegistry;
import com.adfonic.adserver.offence.OffenceRegistry.BidExceptionStats;
import com.adfonic.adserver.offence.OffenceSection;
import com.adfonic.adserver.offence.TroubledBidRequest;
import com.adfonic.adserver.rtb.NoBidException;
import com.adfonic.adserver.rtb.NoBidReason;
import com.adfonic.adserver.rtb.nativ.ByydRequest;

public class OffenceRegistryTest {

    private RtbExecutionContext<?, ?> buildContext(String publisherExternalId, ByydRequest byydRequest) {
        RtbHttpContext httpContext = new RtbHttpContext(RtbEndpoint.ORTBv2, publisherExternalId, new MockHttpServletRequest(), new MockHttpServletResponse(), "/rtb/win/");
        RtbExecutionContext<?, ?> execContext = new RtbExecutionContext(httpContext, true);
        execContext.setByydRequest(byydRequest);
        return execContext;
    }

    @Before
    public void before() {
        AdServerFeatureFlag.OFFENCE_REGISTRY.setEnabled(true);
    }

    @Test
    public void differentExceptionAndSections() {

        OffenceRegistry registry = new OffenceRegistry(1, 2);

        NullPointerException nullPx1 = new NullPointerException("first");
        String publisherExtId1 = "x1" + System.currentTimeMillis();
        ByydRequest request1 = new ByydRequest(publisherExtId1, "request1-" + System.currentTimeMillis());
        RtbExecutionContext<?, ?> context = buildContext(publisherExtId1, request1);

        // When
        registry.record(nullPx1, context);
        // Then
        Collection<OffenceSection> publishers = registry.values();
        Assertions.assertThat(publishers).hasSize(1);
        OffenceSection publisher = registry.getSection(publisherExtId1);
        Assertions.assertThat(publisher.getSectionId()).isEqualTo(publisherExtId1);

        List<BidExceptionStats[]> publisherExceptions = publisher.values();
        Assertions.assertThat(publisherExceptions).hasSize(1);
        BidExceptionStats[] statsOfException = publisherExceptions.get(0);
        Assertions.assertThat(statsOfException).hasSize(1);
        Assertions.assertThat(statsOfException[0].getCount()).isEqualTo(1);
        Assertions.assertThat(statsOfException[0].getOffence()).isEqualTo(nullPx1);
        TroubledBidRequest[] snapshot = statsOfException[0].getSnapshot();
        Assertions.assertThat(snapshot).hasSize(1);
        Assertions.assertThat(snapshot[0].getPublisherExtId()).isEqualTo(publisherExtId1);
        Assertions.assertThat(snapshot[0].getExceptionMessage()).isEqualTo(nullPx1.getMessage());
        Assertions.assertThat(snapshot[0].getExecutionContext().getByydRequest()).isEqualTo(request1);

        //Second time with same Exception... second snapshot shifts first

        ByydRequest request2 = new ByydRequest(publisherExtId1, "request2-" + System.currentTimeMillis());
        context = buildContext(publisherExtId1, request2);
        // When
        registry.record(nullPx1, context);
        // Then
        publishers = registry.values();
        Assertions.assertThat(publishers).hasSize(1);
        publisher = registry.getSection(publisherExtId1);

        publisherExceptions = publisher.values();
        Assertions.assertThat(publisherExceptions).hasSize(1);
        statsOfException = publisherExceptions.get(0);
        Assertions.assertThat(statsOfException).hasSize(1);
        Assertions.assertThat(statsOfException[0].getOffence()).isEqualTo(nullPx1);
        Assertions.assertThat(statsOfException[0].getCount()).isEqualTo(2); //here
        snapshot = statsOfException[0].getSnapshot();
        Assertions.assertThat(snapshot).hasSize(2); //here
        Assertions.assertThat(snapshot[1].getPublisherExtId()).isEqualTo(publisherExtId1);
        Assertions.assertThat(snapshot[1].getExceptionMessage()).isEqualTo(nullPx1.getMessage());
        Assertions.assertThat(snapshot[1].getExecutionContext().getByydRequest()).isEqualTo(request2);//here
        Assertions.assertThat(snapshot[0].getPublisherExtId()).isEqualTo(publisherExtId1);
        Assertions.assertThat(snapshot[0].getExceptionMessage()).isEqualTo(nullPx1.getMessage());
        Assertions.assertThat(snapshot[0].getExecutionContext().getByydRequest()).isEqualTo(request1);//here

        //Third time with same Exception... third snapshot push off first one

        ByydRequest request3 = new ByydRequest(publisherExtId1, "request3-" + System.currentTimeMillis());
        context = buildContext(publisherExtId1, request3);
        // When
        registry.record(nullPx1, context);
        // Then 
        publishers = registry.values();
        Assertions.assertThat(publishers).hasSize(1);
        publisher = registry.getSection(publisherExtId1);

        publisherExceptions = publisher.values();
        Assertions.assertThat(publisherExceptions).hasSize(1);
        statsOfException = publisherExceptions.get(0);
        Assertions.assertThat(statsOfException).hasSize(1);
        Assertions.assertThat(statsOfException[0].getOffence()).isEqualTo(nullPx1);
        Assertions.assertThat(statsOfException[0].getCount()).isEqualTo(3); //here
        snapshot = statsOfException[0].getSnapshot();
        Assertions.assertThat(snapshot).hasSize(2);
        Assertions.assertThat(snapshot[1].getPublisherExtId()).isEqualTo(publisherExtId1);
        Assertions.assertThat(snapshot[1].getExceptionMessage()).isEqualTo(nullPx1.getMessage());
        Assertions.assertThat(snapshot[1].getExecutionContext().getByydRequest()).isEqualTo(request3);//here
        Assertions.assertThat(snapshot[0].getPublisherExtId()).isEqualTo(publisherExtId1);
        Assertions.assertThat(snapshot[0].getExceptionMessage()).isEqualTo(nullPx1.getMessage());
        Assertions.assertThat(snapshot[0].getExecutionContext().getByydRequest()).isEqualTo(request2);//here

        //4. Different NullPointerException... second instance push off first (nullPx1)
        NullPointerException nullPx2 = new NullPointerException("second");
        // When
        context = buildContext(publisherExtId1, request1);
        registry.record(nullPx2, context);
        // Then
        publishers = registry.values();
        Assertions.assertThat(publishers).hasSize(1);
        publisher = registry.getSection(publisherExtId1);

        publisherExceptions = publisher.values();
        Assertions.assertThat(publisherExceptions).hasSize(1);
        statsOfException = publisherExceptions.get(0);
        Assertions.assertThat(statsOfException).hasSize(1);
        Assertions.assertThat(statsOfException[0].getOffence()).isEqualTo(nullPx2);
        Assertions.assertThat(statsOfException[0].getCount()).isEqualTo(1); //here
        snapshot = statsOfException[0].getSnapshot();
        Assertions.assertThat(snapshot).hasSize(1);
        Assertions.assertThat(snapshot[0].getPublisherExtId()).isEqualTo(publisherExtId1);
        Assertions.assertThat(snapshot[0].getExceptionMessage()).isEqualTo(nullPx2.getMessage()); //here
        Assertions.assertThat(snapshot[0].getExecutionContext().getByydRequest()).isEqualTo(request1);//here

        //5. Different NumberFormatException... is tracked separately from NullPointerException

        NumberFormatException numFx = new NumberFormatException("baaad number baaaad");
        context = buildContext(publisherExtId1, request1);
        // When
        registry.record(numFx, context);
        // Then
        publishers = registry.values();
        Assertions.assertThat(publishers).hasSize(1);
        registry.getSection(publisherExtId1);

        publisherExceptions = publisher.values();
        Assertions.assertThat(publisherExceptions).hasSize(2); //diff
        //old section for NullPointerException
        statsOfException = publisher.getStats(NullPointerException.class);
        Assertions.assertThat(statsOfException).hasSize(1);
        Assertions.assertThat(statsOfException[0].getOffence()).isEqualTo(nullPx2);
        Assertions.assertThat(statsOfException[0].getCount()).isEqualTo(1);
        snapshot = statsOfException[0].getSnapshot();
        Assertions.assertThat(snapshot).hasSize(1);
        Assertions.assertThat(snapshot[0].getPublisherExtId()).isEqualTo(publisherExtId1);
        Assertions.assertThat(snapshot[0].getExceptionMessage()).isEqualTo(nullPx2.getMessage());
        Assertions.assertThat(snapshot[0].getExecutionContext().getByydRequest()).isEqualTo(request1);

        //new section for NumberFormatException
        statsOfException = publisher.getStats(NumberFormatException.class); //diff
        Assertions.assertThat(statsOfException).hasSize(1);
        Assertions.assertThat(statsOfException[0].getOffence()).isEqualTo(numFx);
        Assertions.assertThat(statsOfException[0].getCount()).isEqualTo(1);
        snapshot = statsOfException[0].getSnapshot();
        Assertions.assertThat(snapshot).hasSize(1);
        Assertions.assertThat(snapshot[0].getPublisherExtId()).isEqualTo(publisherExtId1);
        Assertions.assertThat(snapshot[0].getExceptionMessage()).isEqualTo(numFx.getMessage());
        Assertions.assertThat(snapshot[0].getExecutionContext().getByydRequest()).isEqualTo(request1);

        //6. Different Publisher 2... is completely separate from Publisher 1

        String publisherExtId2 = "publisher2-extid";
        context = buildContext(publisherExtId2, request1);
        // When
        registry.record(nullPx2, context);
        // Then
        publishers = registry.values();
        Assertions.assertThat(publishers).hasSize(2); //diff
        Iterator<OffenceSection> iterator = publishers.iterator();
        iterator.next(); //skip first
        publisher = registry.getSection(publisherExtId2);

        publisherExceptions = publisher.values();
        Assertions.assertThat(publisherExceptions).hasSize(1);
        statsOfException = publisherExceptions.get(0);
        Assertions.assertThat(statsOfException).hasSize(1);
        Assertions.assertThat(statsOfException[0].getOffence()).isEqualTo(nullPx2);
        Assertions.assertThat(statsOfException[0].getCount()).isEqualTo(1);
        snapshot = statsOfException[0].getSnapshot();
        Assertions.assertThat(snapshot).hasSize(1);
        Assertions.assertThat(snapshot[0].getPublisherExtId()).isEqualTo(publisherExtId2); //diff
        Assertions.assertThat(snapshot[0].getExceptionMessage()).isEqualTo(nullPx2.getMessage());
        Assertions.assertThat(snapshot[0].getExecutionContext().getByydRequest()).isEqualTo(request1);
    }

    @Test
    public void testNoBidException() {

        OffenceRegistry registry = new OffenceRegistry(1, 2);

        String publisherExtId1 = "publisher-1";
        ByydRequest request1 = new ByydRequest(publisherExtId1, "request1-" + System.currentTimeMillis());
        NoBidException noBidException1 = new NoBidException(request1, NoBidReason.REQUEST_INVALID, AdSrvCounter.MISS_UA);

        RtbExecutionContext<?, ?> context = buildContext(publisherExtId1, request1);
        // When
        registry.record(noBidException1, context);
        // Then
        Collection<OffenceSection> publishers = registry.values();
        Assertions.assertThat(publishers).hasSize(1);
        OffenceSection publisher = registry.getSection(publisherExtId1);
        Assertions.assertThat(publisher.getSectionId()).isEqualTo(publisherExtId1);

        List<BidExceptionStats[]> publisherExceptions = publisher.values();
        Assertions.assertThat(publisherExceptions).hasSize(1);
        BidExceptionStats[] statsOfException = publisherExceptions.get(0);
        Assertions.assertThat(statsOfException).hasSize(1);
        Assertions.assertThat(statsOfException[0].getCount()).isEqualTo(1);
        Assertions.assertThat(statsOfException[0].getOffence()).isEqualTo(noBidException1);
        TroubledBidRequest[] snapshot = statsOfException[0].getSnapshot();
        Assertions.assertThat(snapshot).hasSize(1);
        Assertions.assertThat(snapshot[0].getPublisherExtId()).isEqualTo(publisherExtId1);
        Assertions.assertThat(snapshot[0].getExceptionMessage()).isEqualTo(noBidException1.getMessage());
        Assertions.assertThat(snapshot[0].getExecutionContext().getByydRequest()).isEqualTo(request1);

        //Second time with same Exception... second snapshot shifts first

        ByydRequest request2 = new ByydRequest(publisherExtId1, "request2-" + System.currentTimeMillis());
        context = buildContext(publisherExtId1, request2);
        // When
        registry.record(noBidException1, context);
        // Then
        publishers = registry.values();
        Assertions.assertThat(publishers).hasSize(1);
        publisher = registry.getSection(publisherExtId1);

        publisherExceptions = publisher.values();
        Assertions.assertThat(publisherExceptions).hasSize(1);
        statsOfException = publisherExceptions.get(0);
        Assertions.assertThat(statsOfException).hasSize(1);
        Assertions.assertThat(statsOfException[0].getOffence()).isEqualTo(noBidException1);
        Assertions.assertThat(statsOfException[0].getCount()).isEqualTo(2); //here
        snapshot = statsOfException[0].getSnapshot();
        Assertions.assertThat(snapshot).hasSize(2); //here
        Assertions.assertThat(snapshot[1].getPublisherExtId()).isEqualTo(publisherExtId1);
        Assertions.assertThat(snapshot[1].getExceptionMessage()).isEqualTo(noBidException1.getMessage());
        Assertions.assertThat(snapshot[1].getExecutionContext().getByydRequest()).isEqualTo(request2);//here
        Assertions.assertThat(snapshot[0].getPublisherExtId()).isEqualTo(publisherExtId1);
        Assertions.assertThat(snapshot[0].getExceptionMessage()).isEqualTo(noBidException1.getMessage());
        Assertions.assertThat(snapshot[0].getExecutionContext().getByydRequest()).isEqualTo(request1);//here

    }

}
