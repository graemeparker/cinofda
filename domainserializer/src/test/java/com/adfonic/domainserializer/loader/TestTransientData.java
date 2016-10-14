package com.adfonic.domainserializer.loader;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.Set;

import org.jmock.Expectations;
import org.junit.Test;

import com.adfonic.domain.cache.dto.adserver.adspace.PublicationDto;
import com.adfonic.domain.cache.dto.adserver.adspace.PublisherDto;
import com.adfonic.domain.cache.ext.util.AdfonicStopWatch;
import com.adfonic.test.AbstractAdfonicTest;

public class TestTransientData extends AbstractAdfonicTest {

    /*
     * Test when a category excluded by publication
     */
    @Test
    public void test01_isCampaignCategoryExcludedByPublicationOrPublisher() {
        AdCacheBuildContext td = new AdCacheBuildContext(new AdfonicStopWatch());

        Set<Long> excludedcategoryByPublication = new HashSet<Long>();
        Long excludedCategory1 = randomLong();
        Long excludedCategory2 = randomLong();
        Long excludedCategory3 = randomLong();
        excludedcategoryByPublication.add(excludedCategory1);
        excludedcategoryByPublication.add(excludedCategory2);
        excludedcategoryByPublication.add(excludedCategory3);

        final Long publicationId = randomLong();
        td.expandedPublicationExcludedCategoryIds.put(publicationId, excludedcategoryByPublication);

        final PublicationDto publication = new PublicationDto();
        publication.setId(publicationId);

        expect(new Expectations() {
            {

            }
        });
        assertTrue(td.isCampaignCategoryExcludedByPublicationOrPublisher(publication, excludedCategory1));
    }

    /*
     * Test whene category excluded by publisher and not by publication
     */
    @Test
    public void test02_isCampaignCategoryExcludedByPublicationOrPublisher() {
        AdCacheBuildContext td = new AdCacheBuildContext(new AdfonicStopWatch());

        Set<Long> excludedcategoryByPublisher = new HashSet<Long>();
        Set<Long> excludedcategoryByPublication = new HashSet<Long>();

        Long publisherExcludedCategory1 = randomLong();
        Long publisherExcludedCategory2 = randomLong();
        Long publicationExcludedCategory1 = randomLong();
        Long notExcludedCategory = randomLong();
        excludedcategoryByPublisher.add(publisherExcludedCategory1);
        excludedcategoryByPublisher.add(publisherExcludedCategory2);

        excludedcategoryByPublication.add(publicationExcludedCategory1);

        final Long publicationId = randomLong();
        final Long publisherId = randomLong();
        td.expandedPublisherExcludedCategoryIds.put(publisherId, excludedcategoryByPublisher);
        td.expandedPublicationExcludedCategoryIds.put(publicationId, excludedcategoryByPublication);

        final PublicationDto publication = new PublicationDto();
        publication.setId(publicationId);
        final PublisherDto publisher = new PublisherDto();
        publication.setPublisher(publisher);
        publisher.setId(publisherId);
        expect(new Expectations() {
            {

            }
        });
        assertTrue(td.isCampaignCategoryExcludedByPublicationOrPublisher(publication, publisherExcludedCategory1));
    }

    /*
     * Test whene category excluded by publisher and not by publication
     */
    @Test
    public void test03_isCampaignCategoryExcludedByPublicationOrPublisher() {
        AdCacheBuildContext td = new AdCacheBuildContext(new AdfonicStopWatch());

        Set<Long> excludedcategoryByPublisher = new HashSet<Long>();
        Set<Long> excludedcategoryByPublication = new HashSet<Long>();

        Long publisherExcludedCategory1 = randomLong();
        Long publisherExcludedCategory2 = randomLong();
        Long publicationExcludedCategory1 = randomLong();
        Long notExcludedCategory = randomLong();
        excludedcategoryByPublisher.add(publisherExcludedCategory1);
        excludedcategoryByPublisher.add(publisherExcludedCategory2);

        excludedcategoryByPublication.add(publicationExcludedCategory1);

        final Long publicationId = randomLong();
        final Long publisherId = randomLong();
        td.expandedPublisherExcludedCategoryIds.put(publisherId, excludedcategoryByPublisher);
        td.expandedPublicationExcludedCategoryIds.put(publicationId, excludedcategoryByPublication);

        final PublicationDto publication = new PublicationDto();
        publication.setId(publicationId);
        final PublisherDto publisher = new PublisherDto();
        publication.setPublisher(publisher);
        publisher.setId(publisherId);
        expect(new Expectations() {
            {

            }
        });
        assertFalse(td.isCampaignCategoryExcludedByPublicationOrPublisher(publication, notExcludedCategory));
    }

    /*
     * Test when no category excluded by publisher or publication
     */
    @Test
    public void test04_isCampaignCategoryExcludedByPublicationOrPublisher() {
        AdCacheBuildContext td = new AdCacheBuildContext(new AdfonicStopWatch());

        Long notExcludedCategory = randomLong();

        final Long publicationId = randomLong();
        final Long publisherId = randomLong();

        final PublicationDto publication = new PublicationDto();
        publication.setId(publicationId);
        final PublisherDto publisher = new PublisherDto();
        publication.setPublisher(publisher);
        publisher.setId(publisherId);

        expect(new Expectations() {
            {

            }
        });
        assertFalse(td.isCampaignCategoryExcludedByPublicationOrPublisher(publication, notExcludedCategory));
    }
}
