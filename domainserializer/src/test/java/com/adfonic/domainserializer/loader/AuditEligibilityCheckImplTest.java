package com.adfonic.domainserializer.loader;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.apache.commons.lang.RandomStringUtils;
import org.assertj.core.api.Assertions;
import org.joda.time.DateTime;
import org.junit.Ignore;
import org.junit.Test;

import com.adfonic.domain.PublisherAuditedCreative.Status;
import com.adfonic.domain.cache.dto.adserver.adspace.AdSpaceDto;
import com.adfonic.domain.cache.dto.adserver.adspace.PublicationDto;
import com.adfonic.domain.cache.dto.adserver.adspace.PublisherDto;
import com.adfonic.domain.cache.dto.adserver.creative.CreativeDto;
import com.adfonic.domainserializer.loader.AdCacheBuildContext.PublisherAuditedCreativeValue;
import com.adfonic.domainserializer.xaudit.AuditCheckJmsSender;
import com.adfonic.domainserializer.xaudit.AuditCheckJmsSender.FakeAuditCheckSender;
import com.adfonic.domainserializer.xaudit.AuditEligibilityCheckImpl;
import com.google.common.collect.Sets;

public class AuditEligibilityCheckImplTest {

    AuditEligibilityCheckImpl filter = new AuditEligibilityCheckImpl(Sets.newHashSet(349l, 379l), Sets.newHashSet(379l), new AuditCheckJmsSender.FakeAuditCheckSender());

    @Test
    public void testExternalAuditingPublishersIncludesAdXAndAddsNewCreativeToPublisherAuditedCreative() {
        AdCacheBuildContext transientData = new AdCacheBuildContext();

        final Long publicationId = new Random().nextLong();
        final Long publisherId = 349l;
        transientData.publisherApprovedCreativeIds.put(publisherId, Collections.<Long> emptySet());
        transientData.publicationApprovedCreativeIds.put(publicationId, Collections.<Long> emptySet());

        final PublicationDto publication = new PublicationDto();
        publication.setId(publicationId);
        final PublisherDto publisher = new PublisherDto();
        publication.setPublisher(publisher);
        publisher.setId(publisherId);
        publisher.setOperatingPublisherId(publisherId);
        final CreativeDto creative = new CreativeDto();
        Long creativeId = new Random().nextLong();
        creative.setId(creativeId);
        creative.setExternalID("randomString");
        final AdSpaceDto adSpace = new AdSpaceDto();
        adSpace.setExternalID("anotherRandomString");
        adSpace.setPublication(publication);

        assertThat(transientData.creative2PublisherAudited.containsKey(creativeId), equalTo(false));
        assertThat(filter.isEligible(creative, adSpace, transientData), equalTo(false));
        assertThat(transientData.creative2PublisherAudited.containsKey(creativeId), equalTo(true));
    }

    @Test
    public void testCheck() {
        AdCacheBuildContext context = new AdCacheBuildContext();
        long auditingPublisherId = 12345l;
        FakeAuditCheckSender notifier = new AuditCheckJmsSender.FakeAuditCheckSender();
        AuditEligibilityCheckImpl check = new AuditEligibilityCheckImpl(Sets.newHashSet(auditingPublisherId), Sets.newHashSet(54321l), notifier);

        Random random = new Random();

        // Given
        AdSpaceDto adSpaceNoAudit = buildAdSpaceWithPublisher(auditingPublisherId + 10); // make non auditing publisher...
        CreativeDto creative = buildCreative(random.nextLong());
        // When
        boolean eligible1 = check.isEligible(creative, adSpaceNoAudit, context);
        // Then
        Assertions.assertThat(eligible1).isTrue();
        Assertions.assertThat(notifier.getCreativeId()).isNull();
        Assertions.assertThat(notifier.getPublisherId()).isNull();

        // Given
        AdSpaceDto adSpaceAudit = buildAdSpaceWithPublisher(auditingPublisherId); // Must audit
        // When
        boolean eligible2 = check.isEligible(creative, adSpaceAudit, context);
        // Then
        Assertions.assertThat(eligible2).isFalse();
        // sent for audit 
        Assertions.assertThat(notifier.getCreativeId()).isEqualTo(creative.getId());
        Assertions.assertThat(notifier.getPublisherId()).isEqualTo(adSpaceAudit.getPublication().getPublisher().getId());

        // Check same creative again
        notifier.clear();

        boolean eligible3 = check.isEligible(creative, adSpaceAudit, context);
        Assertions.assertThat(eligible3).isFalse();
        // Not sent again
        Assertions.assertThat(notifier.getCreativeId()).isNull();
        Assertions.assertThat(notifier.getPublisherId()).isNull();

    }

    private CreativeDto buildCreative(long creativeId) {
        CreativeDto creative = new CreativeDto();
        creative.setId(creativeId);
        return creative;
    }

    private AdSpaceDto buildAdSpaceWithPublisher(long publisherId) {
        PublisherDto publisher = new PublisherDto();
        publisher.setId(publisherId);
        PublicationDto publication = new PublicationDto();
        publication.setPublisher(publisher);
        AdSpaceDto adSpace = new AdSpaceDto();
        adSpace.setPublication(publication);
        return adSpace;
    }

    @Test
    @Ignore
    public void testAdXPendingAuditedCreativeIsEligible() {
        AdCacheBuildContext transientData = new AdCacheBuildContext();

        long creativeId = new Random().nextLong();
        long publisherId = 349l;
        PublisherAuditedCreativeValue auditedCreative = new PublisherAuditedCreativeValue(Status.PENDING, new DateTime().minusMinutes(2).toDate(), null);
        Map<Long, PublisherAuditedCreativeValue> publisherIdAndStatus = new HashMap<Long, PublisherAuditedCreativeValue>();
        publisherIdAndStatus.put(publisherId, auditedCreative);

        transientData.creative2PublisherAudited.put(creativeId, publisherIdAndStatus);

        final CreativeDto creative = new CreativeDto();
        creative.setId(creativeId);
        creative.setExternalID(RandomStringUtils.random(10));
        final PublicationDto publication = new PublicationDto();
        publication.setId(1111l);
        final PublisherDto publisher = new PublisherDto();
        publisher.setId(publisherId);
        publisher.setOperatingPublisherId(publisherId);
        publication.setPublisher(publisher);
        final AdSpaceDto adSpace = new AdSpaceDto();
        adSpace.setExternalID(RandomStringUtils.random(10));
        adSpace.setPublication(publication);

        // audited creative synced 2 minutes ago
        assertThat(filter.isEligible(creative, adSpace, transientData), equalTo(false));

        // audited creative synced 3 hours ago
        auditedCreative = new PublisherAuditedCreativeValue(auditedCreative.getStatus(), new DateTime().minusHours(3).toDate(), null);
        publisherIdAndStatus = new HashMap<Long, PublisherAuditedCreativeValue>();
        publisherIdAndStatus.put(publisherId, auditedCreative);
        transientData.creative2PublisherAudited.put(creativeId, publisherIdAndStatus);
        assertThat(filter.isEligible(creative, adSpace, transientData), equalTo(false));

        // audited creative then rejected
        PublisherAuditedCreativeValue auditedCreativeRejected = new PublisherAuditedCreativeValue(Status.REJECTED, new DateTime().minusHours(2).toDate(), null);

        publisherIdAndStatus.put(publisherId, auditedCreativeRejected);
        transientData.creative2PublisherAudited.put(creativeId, publisherIdAndStatus);
        assertThat(filter.isEligible(creative, adSpace, transientData), equalTo(false));
    }

    //    @Test
    //    @Ignore("not ready yet")
    //    public void testEligibleOnPublisherTermsForAppnexus() throws Exception {
    //        TransientData transientData = new TransientData();
    //
    //        final Long publicationId = new Random().nextLong();
    //        final Long publisherId = 379l;
    //        transientData.publisherApprovedCreativeIds.put(publisherId, Collections.<Long> emptySet());
    //        transientData.publicationApprovedCreativeIds.put(publicationId, Collections.<Long> emptySet());
    //
    //        final PublicationDto publication = new PublicationDto();
    //        publication.setId(publicationId);
    //        final PublisherDto publisher = new PublisherDto();
    //        publication.setPublisher(publisher);
    //        publisher.setId(publisherId);
    //        publisher.setOperatingPublisherId(publisherId);
    //        final CreativeDto creative = new CreativeDto();
    //        Long creativeId = new Random().nextLong();
    //        creative.setId(creativeId);
    //        creative.setExternalID("randomString");
    //        creative.setAllowExternalAudit(false);
    //        final AdSpaceDto adSpace = new AdSpaceDto();
    //        adSpace.setExternalID("anotherRandomString");
    //        adSpace.setPublication(publication);
    //        
    //        assertThat(transientData.auditedCreatByPubByCreatId.containsKey(creativeId), equalTo(false));
    //        assertThat(filter.isEligibleOnPublisherAuditTerms(creative, adSpace, transientData), equalTo(false));
    //        assertThat(transientData.auditedCreatByPubByCreatId.containsKey(creativeId), equalTo(false));
    //        
    //        creative.setAllowExternalAudit(true);
    //        assertThat(filter.isEligibleOnPublisherAuditTerms(creative, adSpace, transientData), equalTo(false));
    //        assertThat(transientData.auditedCreatByPubByCreatId.containsKey(creativeId), equalTo(true));
    //        
    //    }

}
