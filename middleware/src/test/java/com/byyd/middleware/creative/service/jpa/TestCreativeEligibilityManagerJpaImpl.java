package com.byyd.middleware.creative.service.jpa;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jmock.Expectations;
import org.junit.Before;
import org.junit.Test;

import com.adfonic.domain.Campaign;
import com.adfonic.domain.Category;
import com.adfonic.domain.Channel;
import com.adfonic.domain.Creative;
import com.adfonic.domain.Publication;
import com.adfonic.domain.Publisher;
import com.adfonic.domain.Segment;
import com.adfonic.test.AbstractAdfonicTest;

public class TestCreativeEligibilityManagerJpaImpl extends AbstractAdfonicTest {

    private Category adfonicNotCategorized;
    private Channel uncategorizedChannel;

    @Before
    public void setup() {
        uncategorizedChannel = new Channel("Uncategorized");
        
        adfonicNotCategorized = new Category(Category.NOT_CATEGORIZED_NAME);
        adfonicNotCategorized.setChannel(uncategorizedChannel);
    }
    
    private void checkForTargettingAsFalse(boolean returnValue,List<String> reasonsWhyNot){
        assertFalse("Test case failed, should have returned false.Campaign should not be targeted by this adspace/publication",returnValue);
        assertEquals("Test case failed, reasonsWhyNot list should have one Entry",1,reasonsWhyNot.size());
    }

    private void checkForTargettingAsTrue(boolean returnValue,List<String> reasonsWhyNot){
        assertTrue("Test case failed, should have returned true.Campaign should be targeted by this adspace/publication",returnValue);
        assertEquals("Test case failed, reasonsWhyNot list should have been empty",0,reasonsWhyNot.size());
    }

    /**
     * AF-319 As the Adserver I want to enforce the Channel targeting selection so that Advertiser can 
     * maximize their exposure to certain Publisher Channels
     * 
     * Acceptance 1 : If segment is not channel enabled then that campaign should be targeted for the adspace
     * 
     */
    @Test
    public void testCheckChannelTargetting_segmentNotChannelEnabled() {
        final boolean isSegmentChannelEnabled = false;
        final Segment segment = mock(Segment.class,"segment");
        final Publication pub = mock(Publication.class,"pub");
        final List<String> reasonsWhyNot = new ArrayList<String>();
        
        expect(new Expectations() {{
            oneOf (segment).isChannelEnabled();
                will(returnValue(isSegmentChannelEnabled));
        }});
        
        boolean returnValue = CreativeEligibilityManagerJpaImpl.checkChannelTargetting(segment, pub, reasonsWhyNot);
        checkForTargettingAsTrue(returnValue, reasonsWhyNot);
        
    }
    
    /**
     * AF-319 As the Adserver I want to enforce the Channel targeting selection so that Advertiser can 
     * maximize their exposure to certain Publisher Channels
     * 
     * Acceptance 2C : If segment is channel enabled and publication have not null set of categories
     * then this campaign should not be targeted for publication
     * 
     */
    @Test
    public void testCheckChannelTargetting_segmentChannelEnabledAndSegmentChannlesSelectedAndPubCategoriesNull() {
        final boolean isSegmentChannelEnabled = true;
        final Segment segment = mock(Segment.class,"segment");
        final Publication pub = mock(Publication.class,"pub");
        final List<String> reasonsWhyNot = new ArrayList<String>();
        final Set<Channel> segmentChannels = new HashSet<Channel>();
        final Channel oneSegmentChannel = mock(Channel.class,"oneSegmentChannel");
        segmentChannels.add(oneSegmentChannel);
        
        expect(new Expectations() {{
            oneOf (segment).isChannelEnabled();
                will(returnValue(isSegmentChannelEnabled));
            oneOf (pub).getCategory(); will(returnValue(adfonicNotCategorized));
            oneOf (segment).getChannels();
                will(returnValue(segmentChannels));
        }});
        
        boolean returnValue = CreativeEligibilityManagerJpaImpl.checkChannelTargetting(segment, pub, reasonsWhyNot);
        checkForTargettingAsFalse(returnValue, reasonsWhyNot);
    }
    
    
    /**
     * AF-319 As the Adserver I want to enforce the Channel targeting selection so that Advertiser can 
     * maximize their exposure to certain Publisher Channels
     * 
     * Acceptance 4A : If segment is channel enabled and Segment has no channels attached(=null)
     * then that campaign should not be targeted 
     * 
     */
    @Test
    public void testCheckChannelTargetting_segmentChannelEnabledAndSegmentHasNullChannels() {
        final boolean isSegmentChannelEnabled = true;
        final Segment segment = mock(Segment.class,"segment");
        final Publication pub = mock(Publication.class,"pub");
        final List<String> reasonsWhyNot = new ArrayList<String>();
        final Category pubCategory = mock(Category.class,"pubCategory");
        final Set<Channel> segmentChannels = Collections.emptySet();
        final Channel publicationChannel = mock(Channel.class,"publicationChannel"); 
        
        expect(new Expectations() {{
            oneOf (segment).isChannelEnabled();
                will(returnValue(isSegmentChannelEnabled));
                oneOf (pub).getCategory(); will(returnValue(pubCategory));
            oneOf (pubCategory).getChannel();
                will(returnValue(publicationChannel));
            oneOf (segment).getChannels();
                will(returnValue(segmentChannels));
        }});
        
        boolean returnValue = CreativeEligibilityManagerJpaImpl.checkChannelTargetting(segment, pub, reasonsWhyNot);
        assertFalse("Test case failed, should have returned false.Campaign should not be targeted by this adspace/publication",returnValue);
        assertEquals("Test case failed, reasonsWhyNot list should have one Entry",1,reasonsWhyNot.size());
    }
    
    /**
     * AF-319 As the Adserver I want to enforce the Channel targeting selection so that Advertiser can 
     * maximize their exposure to certain Publisher Channels
     * 
     * Acceptance 4B : If segment is channel enabled and Segment has empty set of channels
     * then that campaign should not be targeted 
     * 
     */
    @Test
    public void testCheckChannelTargetting_segmentChannelEnabledAndSegmentHasEmptyChannels() {
        final boolean isSegmentChannelEnabled = true;
        final Segment segment = mock(Segment.class,"segment");
        final Publication pub = mock(Publication.class,"pub");
        final List<String> reasonsWhyNot = new ArrayList<String>();
        final Category pubCategory = mock(Category.class,"pubCategory");
        final Set<Channel> segmentChannels = new HashSet<Channel>();
        final Channel publicationChannel = mock(Channel.class,"publicationChannel"); 
        
        expect(new Expectations() {{
            oneOf (segment).isChannelEnabled();
                will(returnValue(isSegmentChannelEnabled));
            oneOf (pub).getCategory(); will(returnValue(pubCategory));
            oneOf (pubCategory).getChannel();
                will(returnValue(publicationChannel));
            oneOf (segment).getChannels();
                will(returnValue(segmentChannels));
        }});
        
        boolean returnValue = CreativeEligibilityManagerJpaImpl.checkChannelTargetting(segment, pub, reasonsWhyNot);
        assertFalse("Test case failed, should have returned false.Campaign should not be targeted by this adspace/publication",returnValue);
        assertEquals("Test case failed, reasonsWhyNot list should have one Entry",1,reasonsWhyNot.size());
    }
    
    /**
     * AF-319 As the Adserver I want to enforce the Channel targeting selection so that Advertiser can 
     * maximize their exposure to certain Publisher Channels
     * 
     * Acceptance 5 : If segment is channel enabled and Segment has channels but none of the channels matches to publication category channel
     * then that campaign should not be targeted 
     * 
     */
    @Test
    public void testCheckChannelTargetting_segmentChannelEnabledAndSegmentHasChannelsAndNotContainPublicationCategoryChannel() {
        final boolean isSegmentChannelEnabled = true;
        final Segment segment = mock(Segment.class,"segment");
        final Publication pub = mock(Publication.class,"pub");
        final List<String> reasonsWhyNot = new ArrayList<String>();
        final Category pubCategory = mock(Category.class,"pubCategory");
        final Channel publicationChannel = mock(Channel.class,"publicationChannel"); 
        final Set<Channel> segmentChannels = new HashSet<Channel>();
        Channel oneSegmentChannel =  mock(Channel.class,"oneSegmentChannel");
        Channel twoSegmentChannel =  mock(Channel.class,"twoSegmentChannel");
        segmentChannels.add(oneSegmentChannel);
        segmentChannels.add(twoSegmentChannel);
        
        expect(new Expectations() {{
            oneOf (segment).isChannelEnabled();
                will(returnValue(isSegmentChannelEnabled));
            oneOf (pub).getCategory(); will(returnValue(pubCategory));
            oneOf (pubCategory).getChannel();
                will(returnValue(publicationChannel));
            oneOf (segment).getChannels();
                will(returnValue(segmentChannels));
        }});
        
        boolean returnValue = CreativeEligibilityManagerJpaImpl.checkChannelTargetting(segment, pub, reasonsWhyNot);
        assertFalse("Test case failed, should have returned false.Campaign should not be targeted by this adspace/publication",returnValue);
        assertEquals("Test case failed, reasonsWhyNot list should have one Entry",1,reasonsWhyNot.size());
    }
    
    /**
     * AF-319 As the Adserver I want to enforce the Channel targeting selection so that Advertiser can 
     * maximize their exposure to certain Publisher Channels
     * 
     * Acceptance 5 : If segment is channel enabled and Segment has channels and one of the channels is publication category channel
     * then that campaign should be targeted 
     * 
     */
    @Test
    public void testCheckChannelTargetting_segmentChannelEnabledAndSegmentHasChannelsAndContainPublicationCategoryChannel() {
        final boolean isSegmentChannelEnabled = true;
        final Segment segment = mock(Segment.class,"segment");
        final Publication pub = mock(Publication.class,"pub");
        final List<String> reasonsWhyNot = new ArrayList<String>();
        final Category pubCategory = mock(Category.class,"pubCategory");
        final Channel publicationChannel = mock(Channel.class,"publicationChannel"); 
        final Set<Channel> segmentChannels = new HashSet<Channel>();
        Channel oneSegmentChannel =  mock(Channel.class,"oneSegmentChannel");
        Channel twoSegmentChannel =  mock(Channel.class,"twoSegmentChannel");
        segmentChannels.add(oneSegmentChannel);
        segmentChannels.add(twoSegmentChannel);
        segmentChannels.add(publicationChannel);
        
        expect(new Expectations() {{
            oneOf (segment).isChannelEnabled();
                will(returnValue(isSegmentChannelEnabled));
            oneOf (pub).getCategory(); will(returnValue(pubCategory));
            oneOf (pubCategory).getChannel();
                will(returnValue(publicationChannel));
            oneOf (segment).getChannels();
                will(returnValue(segmentChannels));
        }});
        
        boolean returnValue = CreativeEligibilityManagerJpaImpl.checkChannelTargetting(segment, pub, reasonsWhyNot);
        assertTrue("Test case failed, should have returned true.Campaign should be targeted by this adspace/publication",returnValue);
        assertEquals("Test case failed, reasonsWhyNot list should have zero Entry",0,reasonsWhyNot.size());
    }
    
    /**
     * AF-319 As the Adserver I want to enforce the Channel targeting selection so that Advertiser can 
     * maximize their exposure to certain Publisher Channels
     * 
     * Acceptance 6 : If segment is channel enabled/disabled/whatever and 
     * Campaign category is null(not provided) and publication excluded categories are not empty
     * 
     *  then this campaign should not be targeted for this adspace/publication
     * 
     */
    @Test
    public void testcheckCategoryExclusionForTargeting_campaignCategoryNullAndNonEmptyPublicationExcludedCategoryExists() {
        final Segment segment = mock(Segment.class,"segment");
        final Publication pub = mock(Publication.class,"pub");
        final Creative creative = mock(Creative.class,"creative");
        final List<String> reasonsWhyNot = new ArrayList<String>();
        final Category campaignCategory = null;
        final Set<Category> publicationExcludedCategories = new HashSet<Category>();
        final Category publicationExcludedCategory1 = mock(Category.class,"publicationExcludedCategory1");
        publicationExcludedCategories.add(publicationExcludedCategory1);
        final Campaign campaign = mock(Campaign.class,"campaign");
        
        final Set<Category> segmentExcludedCategories = Collections.emptySet();
        final Publisher publisher = mock(Publisher.class);
        
        expect(new Expectations() {{
            oneOf (creative).getCampaign();
                will(returnValue(campaign));
            oneOf (campaign).getCategory();
                will(returnValue(campaignCategory));
            allowing (pub).getExcludedCategories();
                will(returnValue(publicationExcludedCategories));
            allowing (pub).getPublisher(); will(returnValue(publisher));
            allowing (publisher).getExcludedCategories(); will(returnValue(Collections.emptySet()));
            oneOf (pub).getCategory(); will(returnValue(adfonicNotCategorized));
            oneOf (segment).getExcludedCategories();
                will(returnValue(segmentExcludedCategories));
        }});
        
        boolean returnValue = CreativeEligibilityManagerJpaImpl.checkCategoryExclusionForTargeting(pub, creative, segment, reasonsWhyNot);
        assertFalse("Test case failed, should have returned false.Campaign should not be targeted for this adspace/publication",returnValue);
        assertEquals("Test case failed, reasonsWhyNot list should have one Entry",1,reasonsWhyNot.size());
    }
    
    /**
     * Publisher level category exclusion test, campaign category is null
     */
    @Test
    @SuppressWarnings("serial")
    public void testcheckCategoryExclusionForTargeting_campaignCategoryNullAndNonEmptyPublisherExcludedCategories() {
        final Publication pub = mock(Publication.class, "pub");
        final Creative creative = mock(Creative.class);
        final Segment segment = mock(Segment.class);
        final List<String> reasonsWhyNot = new ArrayList<String>();
        
        final Campaign campaign = mock(Campaign.class,"campaign");
        final Publisher publisher = mock(Publisher.class);
        final Category excludedCategory1 = mock(Category.class, "excludedCategory1");
        final Set<Category> excludedCategories = new HashSet<Category>() {{
                add(excludedCategory1);
            }};
        
        expect(new Expectations() {{
            oneOf (creative).getCampaign(); will(returnValue(campaign));
            oneOf (campaign).getCategory(); will(returnValue(null));
            allowing (pub).getExcludedCategories(); will(returnValue(Collections.emptySet()));
            allowing (pub).getPublisher(); will(returnValue(publisher));
            allowing (publisher).getExcludedCategories(); will(returnValue(excludedCategories));
            oneOf (pub).getCategory(); will(returnValue(null));
            oneOf (segment).getExcludedCategories(); will(returnValue(Collections.emptySet()));
        }});
        
        boolean returnValue = CreativeEligibilityManagerJpaImpl.checkCategoryExclusionForTargeting(pub, creative, segment, reasonsWhyNot);
        assertFalse("Test case failed, should have returned false.Campaign should not be targeted for this adspace/publication",returnValue);
        assertEquals("Test case failed, reasonsWhyNot list should have one Entry",1,reasonsWhyNot.size());
    }
    
    /**
     * Publisher level category exclusion test, campaign category is not null,
     * and publisher excluded category doesn't match the campaign category
     */
    @Test
    @SuppressWarnings("serial")
    public void testcheckCategoryExclusionForTargeting_campaignCategoryNotNullAndNonEmptyPublisherExcludedCategories_no_match() {
        final Publication pub = mock(Publication.class, "pub");
        final Creative creative = mock(Creative.class);
        final Segment segment = mock(Segment.class);
        final List<String> reasonsWhyNot = new ArrayList<String>();
        
        final Campaign campaign = mock(Campaign.class);
        final Category campaignCategory = mock(Category.class, "campaignCategory");
        final Publisher publisher = mock(Publisher.class);
        final Category excludedCategory1 = mock(Category.class, "excludedCategory1");
        final Set<Category> excludedCategories = new HashSet<Category>() {{
                add(excludedCategory1);
            }};
        
        expect(new Expectations() {{
            oneOf (creative).getCampaign(); will(returnValue(campaign));
            oneOf (campaign).getCategory(); will(returnValue(campaignCategory));
            allowing (pub).getExcludedCategories(); will(returnValue(Collections.emptySet()));
            allowing (pub).getPublisher(); will(returnValue(publisher));
            allowing (publisher).getExcludedCategories(); will(returnValue(excludedCategories));
            oneOf (excludedCategory1).getChildren(); will(returnValue(Collections.emptyList()));
            oneOf (pub).getCategory(); will(returnValue(null));
            oneOf (segment).getExcludedCategories(); will(returnValue(Collections.emptySet()));
        }});
        
        boolean returnValue = CreativeEligibilityManagerJpaImpl.checkCategoryExclusionForTargeting(pub, creative, segment, reasonsWhyNot);
        assertTrue("Test case failed, should have returned false.Campaign should be targeted for this adspace/publication", returnValue);
    }
    
    /**
     * Publisher level category exclusion test, campaign category is not null,
     * and publisher excluded category matches the campaign category
     */
    @Test
    @SuppressWarnings("serial")
    public void testcheckCategoryExclusionForTargeting_campaignCategoryNotNullAndNonEmptyPublisherExcludedCategories_matches() {
        final Publication pub = mock(Publication.class, "pub");
        final Creative creative = mock(Creative.class);
        final Segment segment = mock(Segment.class);
        final List<String> reasonsWhyNot = new ArrayList<String>();
        
        final Campaign campaign = mock(Campaign.class);
        final Publisher publisher = mock(Publisher.class);
        final Category excludedCategory1 = mock(Category.class, "excludedCategory1");
        final Set<Category> excludedCategories = new HashSet<Category>() {{
                add(excludedCategory1);
            }};
        
        expect(new Expectations() {{
            oneOf (creative).getCampaign(); will(returnValue(campaign));
            oneOf (campaign).getCategory(); will(returnValue(excludedCategory1));
            allowing (pub).getExcludedCategories(); will(returnValue(Collections.emptySet()));
            allowing (pub).getPublisher(); will(returnValue(publisher));
            allowing (publisher).getExcludedCategories(); will(returnValue(excludedCategories));
            oneOf (excludedCategory1).getChildren(); will(returnValue(Collections.emptyList()));
            allowing (excludedCategory1).getId(); will(returnValue(randomLong()));
            allowing (excludedCategory1).getName(); will(returnValue(randomAlphaNumericString(10)));
            oneOf (pub).getCategory(); will(returnValue(null));
            oneOf (segment).getExcludedCategories(); will(returnValue(Collections.emptySet()));
        }});
        
        boolean returnValue = CreativeEligibilityManagerJpaImpl.checkCategoryExclusionForTargeting(pub, creative, segment, reasonsWhyNot);
        assertFalse("Test case failed, should have returned false.Campaign should not be targeted for this adspace/publication",returnValue);
        assertEquals("Test case failed, reasonsWhyNot list should have one Entry",1,reasonsWhyNot.size());
    }
    
    /**
     * Publisher level category exclusion test, campaign category is not null,
     * and publisher excluded category (expanded) matches the campaign category
     */
    @Test
    @SuppressWarnings("serial")
    public void testcheckCategoryExclusionForTargeting_campaignCategoryNotNullAndNonEmptyPublisherExcludedCategories_matches_expanded() {
        final Publication pub = mock(Publication.class, "pub");
        final Creative creative = mock(Creative.class);
        final Segment segment = mock(Segment.class);
        final List<String> reasonsWhyNot = new ArrayList<String>();
        
        final Campaign campaign = mock(Campaign.class);
        final Publisher publisher = mock(Publisher.class);
        final Category excludedCategory1 = mock(Category.class, "excludedCategory1");
        final Set<Category> excludedCategories = new HashSet<Category>() {{
                add(excludedCategory1);
            }};
        final Category excludedChildCategory = mock(Category.class, "excludedChildCategory");
        
        expect(new Expectations() {{
            oneOf (creative).getCampaign(); will(returnValue(campaign));
            oneOf (campaign).getCategory(); will(returnValue(excludedChildCategory));
            allowing (pub).getExcludedCategories(); will(returnValue(Collections.emptySet()));
            allowing (pub).getPublisher(); will(returnValue(publisher));
            allowing (publisher).getExcludedCategories(); will(returnValue(excludedCategories));
            oneOf (excludedCategory1).getChildren(); will(returnValue(Collections.singletonList(excludedChildCategory)));
            oneOf (excludedChildCategory).getChildren(); will(returnValue(Collections.emptyList()));
            allowing (excludedCategory1).getId(); will(returnValue(randomLong()));
            allowing (excludedCategory1).getName(); will(returnValue(randomAlphaNumericString(10)));
            allowing (excludedChildCategory).getId(); will(returnValue(randomLong()));
            allowing (excludedChildCategory).getName(); will(returnValue(randomAlphaNumericString(10)));
            oneOf (pub).getCategory(); will(returnValue(null));
            oneOf (segment).getExcludedCategories(); will(returnValue(Collections.emptySet()));
        }});
        
        boolean returnValue = CreativeEligibilityManagerJpaImpl.checkCategoryExclusionForTargeting(pub, creative, segment, reasonsWhyNot);
        assertFalse("Test case failed, should have returned false.Campaign should not be targeted for this adspace/publication",returnValue);
        assertEquals("Test case failed, reasonsWhyNot list should have one Entry",1,reasonsWhyNot.size());
    }
    
    /**
     * AF-319 As the Adserver I want to enforce the Channel targeting selection so that Advertiser can 
     * maximize their exposure to certain Publisher Channels
     * 
     * Acceptance 7A : If segment is channel enabled/disabled/whatever and 
     * Campaign category is null(not provided) and publication excluded categories are null
     * 
     *  then this campaign should be targeted for this adspace/publication
     * 
     */
    @Test
    public void testcheckCategoryExclusionForTargeting_campaignCategoryNullAndNullPublicationExcludedCategoryExists() {
        final Segment segment = mock(Segment.class,"segment");
        final Publication pub = mock(Publication.class,"pub");
        final Creative creative = mock(Creative.class,"creative");
        final List<String> reasonsWhyNot = new ArrayList<String>();
        final Category campaignCategory = null;
        final Set<Category> publicationExcludedCategories = Collections.emptySet();
        final Campaign campaign = mock(Campaign.class,"campaign");
        final Publisher publisher = mock(Publisher.class);
        final Set<Category> segmentExcludedCategories = Collections.emptySet();
        
        
        expect(new Expectations() {{
            oneOf (creative).getCampaign();
                will(returnValue(campaign));
            oneOf (campaign).getCategory();
                will(returnValue(campaignCategory));
            allowing (pub).getExcludedCategories();
                will(returnValue(publicationExcludedCategories));
            allowing (pub).getPublisher(); will(returnValue(publisher));
            allowing (publisher).getExcludedCategories(); will(returnValue(Collections.emptySet()));
            oneOf (pub).getCategory(); will(returnValue(adfonicNotCategorized));
            oneOf (segment).getExcludedCategories();
                will(returnValue(segmentExcludedCategories));
        }});
        
        boolean returnValue = CreativeEligibilityManagerJpaImpl.checkCategoryExclusionForTargeting(pub, creative, segment, reasonsWhyNot);
        assertTrue("Test case failed, should have returned true.Campaign should be targeted for this adspace/publication",returnValue);
        assertEquals("Test case failed, reasonsWhyNot list should have zero Entry",0,reasonsWhyNot.size());
    }
    
    /**
     * AF-319 As the Adserver I want to enforce the Channel targeting selection so that Advertiser can 
     * maximize their exposure to certain Publisher Channels
     * 
     * Acceptance 7B : If segment is channel enabled/disabled/whatever and 
     * Campaign category is null(not provided) and publication excluded categories is empty
     * 
     *  then this campaign should be targeted for this adspace/publication
     * 
     */
    @Test
    public void testcheckCategoryExclusionForTargeting_campaignCategoryNullAndEmptyPublicationExcludedCategoryExists() {
        final Segment segment = mock(Segment.class,"segment");
        final Publication pub = mock(Publication.class,"pub");
        final Creative creative = mock(Creative.class,"creative");
        final List<String> reasonsWhyNot = new ArrayList<String>();
        final Category campaignCategory = null;
        final Set<Category> publicationExcludedCategories = new HashSet<Category>();
        final Campaign campaign = mock(Campaign.class,"campaign");
        final Publisher publisher = mock(Publisher.class);        
        final Set<Category> segmentExcludedCategories = Collections.emptySet();
        
        
        expect(new Expectations() {{
            oneOf (creative).getCampaign();
                will(returnValue(campaign));
            oneOf (campaign).getCategory();
                will(returnValue(campaignCategory));
            allowing (pub).getExcludedCategories();
                will(returnValue(publicationExcludedCategories));
            allowing (pub).getPublisher(); will(returnValue(publisher));
            allowing (publisher).getExcludedCategories(); will(returnValue(Collections.emptySet()));
            oneOf (pub).getCategory(); will(returnValue(adfonicNotCategorized));
            oneOf (segment).getExcludedCategories();
                will(returnValue(segmentExcludedCategories));
        }});
        
        boolean returnValue = CreativeEligibilityManagerJpaImpl.checkCategoryExclusionForTargeting(pub, creative, segment, reasonsWhyNot);
        assertTrue("Test case failed, should have returned true.Campaign should be targeted for this adspace/publication",returnValue);
        assertEquals("Test case failed, reasonsWhyNot list should have zero Entry",0,reasonsWhyNot.size());
    }
    
    
    
    /**
     * AF-319 As the Adserver I want to enforce the Channel targeting selection so that Advertiser can 
     * maximize their exposure to certain Publisher Channels
     * 
     * Acceptance 8A : If segment is channel enabled/disabled/whatever and 
     * Campaign category is not null(provided) and publication excluded categories is null
     * 
     *  then this campaign should be targeted for this adspace/publication
     * 
     */
    @Test
    public void testcheckCategoryExclusionForTargeting_campaignCategoryNotNullAndNullPublicationExcludedCategoryExists() {
        final Segment segment = mock(Segment.class,"segment");
        final Publication pub = mock(Publication.class,"pub");
        final Creative creative = mock(Creative.class,"creative");
        final List<String> reasonsWhyNot = new ArrayList<String>();
        final Category campaignCategory = mock(Category.class,"campaignCategory");
        final Set<Category> publicationExcludedCategories = Collections.emptySet();
        final Campaign campaign = mock(Campaign.class,"campaign");
        final Publisher publisher = mock(Publisher.class);        
        final Set<Category> segmentExcludedCategories = Collections.emptySet();
        
        
        expect(new Expectations() {{
            oneOf (creative).getCampaign();
                will(returnValue(campaign));
            oneOf (campaign).getCategory();
                will(returnValue(campaignCategory));
            allowing (pub).getExcludedCategories();
                will(returnValue(publicationExcludedCategories));
            allowing (pub).getPublisher(); will(returnValue(publisher));
            allowing (publisher).getExcludedCategories(); will(returnValue(Collections.emptySet()));
            oneOf (pub).getCategory(); will(returnValue(adfonicNotCategorized));
            oneOf (segment).getExcludedCategories();
                will(returnValue(segmentExcludedCategories));
        }});
        
        boolean returnValue = CreativeEligibilityManagerJpaImpl.checkCategoryExclusionForTargeting(pub, creative, segment, reasonsWhyNot);
        assertTrue("Test case failed, should have returned true.Campaign should be targeted for this adspace/publication",returnValue);
        assertEquals("Test case failed, reasonsWhyNot list should have zero Entry",0,reasonsWhyNot.size());
    }
    
    /**
     * AF-319 As the Adserver I want to enforce the Channel targeting selection so that Advertiser can 
     * maximize their exposure to certain Publisher Channels
     * 
     * Acceptance 8B : If segment is channel enabled/disabled/whatever and 
     * Campaign category is not null(provided) and publication excluded categories is Empty
     * 
     *  then this campaign should be targeted for this adspace/publication
     * 
     */
    @Test
    public void testcheckCategoryExclusionForTargeting_campaignCategoryNotNullAndEmptyPublicationExcludedCategoryExists() {
        final Segment segment = mock(Segment.class,"segment");
        final Publication pub = mock(Publication.class,"pub");
        final Creative creative = mock(Creative.class,"creative");
        final List<String> reasonsWhyNot = new ArrayList<String>();
        final Category campaignCategory = mock(Category.class,"campaignCategory");
        final Set<Category> publicationExcludedCategories = new HashSet<Category>();
        final Campaign campaign = mock(Campaign.class,"campaign");
        final Publisher publisher = mock(Publisher.class);        
        final Set<Category> segmentExcludedCategories = Collections.emptySet();
        
        
        expect(new Expectations() {{
            oneOf (creative).getCampaign();
                will(returnValue(campaign));
            oneOf (campaign).getCategory();
                will(returnValue(campaignCategory));
            allowing (pub).getExcludedCategories();
                will(returnValue(publicationExcludedCategories));
            allowing (pub).getPublisher(); will(returnValue(publisher));
            allowing (publisher).getExcludedCategories(); will(returnValue(Collections.emptySet()));
            oneOf (pub).getCategory(); will(returnValue(adfonicNotCategorized));
            oneOf (segment).getExcludedCategories();
                will(returnValue(segmentExcludedCategories));
        }});
        
        boolean returnValue = CreativeEligibilityManagerJpaImpl.checkCategoryExclusionForTargeting(pub, creative, segment, reasonsWhyNot);
        assertTrue("Test case failed, should have returned true.Campaign should be targeted for this adspace/publication",returnValue);
        assertEquals("Test case failed, reasonsWhyNot list should have zero Entry",0,reasonsWhyNot.size());
    }
    
    /**
     * AF-319 As the Adserver I want to enforce the Channel targeting selection so that Advertiser can 
     * maximize their exposure to certain Publisher Channels
     * 
     * Acceptance 9 : If segment is channel enabled/disabled/whatever and 
     * Campaign category is not null(provided) and publication excluded categories is Non Empty but do not have any category
     * which matches with campaign Category
     * 
     *  then this campaign should be targeted for this adspace/publication
     * 
     */
    @Test
    public void testcheckCategoryExclusionForTargeting_campaignCategoryNotNullAndNonEmptyPublicationExcludedCategoryExistsAndNoMatch() {
        final Segment segment = mock(Segment.class,"segment");
        final Publication pub = mock(Publication.class,"pub");
        final Creative creative = mock(Creative.class,"creative");
        final List<String> reasonsWhyNot = new ArrayList<String>();
        final Category campaignCategory = mock(Category.class,"campaignCategory");
        final Set<Category> publicationExcludedCategories = new HashSet<Category>();
        final Category publicationExcludedCategory1 = mock(Category.class,"publicationExcludedCategory1");
        publicationExcludedCategories.add(publicationExcludedCategory1);
        final List<Category> publicationExcludedCategory1Children = new ArrayList<Category>(); 
        final Campaign campaign = mock(Campaign.class,"campaign");
        final Publisher publisher = mock(Publisher.class);        
        final Set<Category> segmentExcludedCategories = Collections.emptySet();
        
        
        expect(new Expectations() {{
            oneOf (creative).getCampaign();
                will(returnValue(campaign));
            oneOf (campaign).getCategory();
                will(returnValue(campaignCategory));
            allowing (pub).getExcludedCategories();
                will(returnValue(publicationExcludedCategories));
            allowing (pub).getPublisher(); will(returnValue(publisher));
            allowing (publisher).getExcludedCategories(); will(returnValue(Collections.emptySet()));
            oneOf (pub).getCategory(); will(returnValue(adfonicNotCategorized));
            oneOf (segment).getExcludedCategories();
                will(returnValue(segmentExcludedCategories));
            oneOf (publicationExcludedCategory1).getChildren();
                will(returnValue(publicationExcludedCategory1Children));
                
        }});
        
        boolean returnValue = CreativeEligibilityManagerJpaImpl.checkCategoryExclusionForTargeting(pub, creative, segment, reasonsWhyNot);
        assertTrue("Test case failed, should have returned true.Campaign should be targeted for this adspace/publication",returnValue);
        assertEquals("Test case failed, reasonsWhyNot list should have zero Entry",0,reasonsWhyNot.size());
    }
    
    /**
     * AF-319 As the Adserver I want to enforce the Channel targeting selection so that Advertiser can 
     * maximize their exposure to certain Publisher Channels
     * 
     * Acceptance 10 : If segment is channel enabled/disabled/whatever and 
     * Campaign category is not null(provided) and publication excluded categories is Non Empty and have one category
     * which matches with campaign Category
     * 
     *  then this campaign should be targeted for this adspace/publication
     * 
     */
    @Test
    public void testcheckCategoryExclusionForTargeting_campaignCategoryNotNullAndNonEmptyPublicationExcludedCategoryExistsAndMatchExists() {
        final Segment segment = mock(Segment.class,"segment");
        final Publication pub = mock(Publication.class,"pub");
        final Creative creative = mock(Creative.class,"creative");
        final List<String> reasonsWhyNot = new ArrayList<String>();
        final Category campaignCategory = mock(Category.class,"campaignCategory");
        final Set<Category> publicationExcludedCategories = new HashSet<Category>();
        final Category publicationExcludedCategory1 = mock(Category.class,"publicationExcludedCategory1");
        publicationExcludedCategories.add(publicationExcludedCategory1);
        publicationExcludedCategories.add(campaignCategory);
        
        final List<Category> publicationExcludedCategory1Children = new ArrayList<Category>();
        final List<Category> campaignCategoryChildren = new ArrayList<Category>();
        
        final Campaign campaign = mock(Campaign.class,"campaign");
        
        final Set<Category> segmentExcludedCategories = Collections.emptySet();
        
        final long campaignCategoryId = 1;
        final String campaignCategoryName = randomAlphaNumericString(30);
        final Publisher publisher = mock(Publisher.class);
        
        
        expect(new Expectations() {{
            oneOf (creative).getCampaign();
                will(returnValue(campaign));
            oneOf (campaign).getCategory();
                will(returnValue(campaignCategory));
            allowing (pub).getExcludedCategories();
                will(returnValue(publicationExcludedCategories));
            allowing (pub).getPublisher(); will(returnValue(publisher));
            allowing (publisher).getExcludedCategories(); will(returnValue(Collections.emptySet()));
            oneOf (pub).getCategory(); will(returnValue(adfonicNotCategorized));
            oneOf (segment).getExcludedCategories();
                will(returnValue(segmentExcludedCategories));
            oneOf (publicationExcludedCategory1).getChildren();
                will(returnValue(publicationExcludedCategory1Children));
            oneOf (campaignCategory).getChildren();
                will(returnValue(campaignCategoryChildren));
            oneOf (campaignCategory).getId();
                will(returnValue(campaignCategoryId));
            oneOf (campaignCategory).getName();
                will(returnValue(campaignCategoryName));
                
        }});
        
        boolean returnValue = CreativeEligibilityManagerJpaImpl.checkCategoryExclusionForTargeting(pub, creative, segment, reasonsWhyNot);
        assertFalse("Test case failed, should have returned false.Campaign should not be targeted for this adspace/publication",returnValue);
        assertEquals("Test case failed, reasonsWhyNot list should have 1 Entry",1,reasonsWhyNot.size());
    }
    
    /**
     * AF-319 As the Adserver I want to enforce the Channel targeting selection so that Advertiser can 
     * maximize their exposure to certain Publisher Channels
     * 
     * Acceptance 10 : If segment is channel enabled/disabled/whatever and 
     * Campaign category is not null(provided) and publication excluded categories is Non Empty and have one children category
     * which matches with campaign Category
     * 
     *  then this campaign should be targeted for this adspace/publication
     * 
     */
    @Test
    public void testcheckCategoryExclusionForTargeting_campaignCategoryNotNullAndNonEmptyPublicationExcludedCategoryExistsAndMatchExistsForChilderCategory() {
        final Segment segment = mock(Segment.class,"segment");
        final Publication pub = mock(Publication.class,"pub");
        final Creative creative = mock(Creative.class,"creative");
        final List<String> reasonsWhyNot = new ArrayList<String>();
        final Category campaignCategory = mock(Category.class,"campaignCategory");
        final Set<Category> publicationExcludedCategories = new HashSet<Category>();
        final Category publicationExcludedCategory1 = mock(Category.class,"publicationExcludedCategory1");
        publicationExcludedCategories.add(publicationExcludedCategory1);
        
        final List<Category> publicationExcludedCategory1Children = new ArrayList<Category>();
        publicationExcludedCategory1Children.add(campaignCategory);
        final List<Category> campaignCategoryChildren = new ArrayList<Category>();
        
        final Campaign campaign = mock(Campaign.class,"campaign");
        
        final Set<Category> segmentExcludedCategories = Collections.emptySet();
        
        final long campaignCategoryId = 1;
        final String campaignCategoryName = randomAlphaNumericString(30);

        final long publicationExcludedCategory1Id = 1;
        final String publicationExcludedCategory1Name = randomAlphaNumericString(30);
        final Publisher publisher = mock(Publisher.class);
        
        
        expect(new Expectations() {{
            oneOf (creative).getCampaign();
                will(returnValue(campaign));
            oneOf (campaign).getCategory();
                will(returnValue(campaignCategory));
            allowing (pub).getExcludedCategories();
                will(returnValue(publicationExcludedCategories));
            allowing (pub).getPublisher(); will(returnValue(publisher));
            allowing (publisher).getExcludedCategories(); will(returnValue(Collections.emptySet()));
            oneOf (pub).getCategory(); will(returnValue(adfonicNotCategorized));
            oneOf (segment).getExcludedCategories();
                will(returnValue(segmentExcludedCategories));
            oneOf (publicationExcludedCategory1).getChildren();
                will(returnValue(publicationExcludedCategory1Children));
            oneOf (campaignCategory).getChildren();
                will(returnValue(campaignCategoryChildren));
            oneOf (campaignCategory).getId();
                will(returnValue(campaignCategoryId));
            oneOf (campaignCategory).getName();
                will(returnValue(campaignCategoryName));
            oneOf (publicationExcludedCategory1).getId();
                will(returnValue(publicationExcludedCategory1Id));
            oneOf (publicationExcludedCategory1).getName();
                will(returnValue(publicationExcludedCategory1Name));
                
        }});
        
        boolean returnValue = CreativeEligibilityManagerJpaImpl.checkCategoryExclusionForTargeting(pub, creative, segment, reasonsWhyNot);
        assertFalse("Test case failed, should have returned false.Campaign should not be targeted for this adspace/publication",returnValue);
        assertEquals("Test case failed, reasonsWhyNot list should have 1 Entry",1,reasonsWhyNot.size());
    }
    
    /**
     * AF-319 As the Adserver I want to enforce the Channel targeting selection so that Advertiser can 
     * maximize their exposure to certain Publisher Channels
     * 
     * Acceptance 11A : If segment is channel enabled/disabled/whatever and 
     * Publication category is null(not provided) and segment excluded categories are null
     * 
     *  then this campaign should be targeted for this adspace/publication
     * 
     */
    @Test
    public void testcheckCategoryExclusionForTargeting_publicationCategoryNullAndNullSegmentExcludedCategoryExists() {
        final Segment segment = mock(Segment.class,"segment");
        final Publication pub = mock(Publication.class,"pub");
        final Creative creative = mock(Creative.class,"creative");
        final List<String> reasonsWhyNot = new ArrayList<String>();
        final Category campaignCategory = null;
        final Set<Category> publicationExcludedCategories = Collections.emptySet();
        final Campaign campaign = mock(Campaign.class,"campaign");
        final Publisher publisher = mock(Publisher.class);        
        final Set<Category> segmentExcludedCategories = Collections.emptySet();
        
        
        expect(new Expectations() {{
            oneOf (creative).getCampaign();
                will(returnValue(campaign));
            oneOf (campaign).getCategory();
                will(returnValue(campaignCategory));
            allowing (pub).getExcludedCategories();
                will(returnValue(publicationExcludedCategories));

            allowing (pub).getPublisher(); will(returnValue(publisher));
            allowing (publisher).getExcludedCategories(); will(returnValue(Collections.emptySet()));
            oneOf (pub).getCategory(); will(returnValue(adfonicNotCategorized));
            oneOf (segment).getExcludedCategories();
                will(returnValue(segmentExcludedCategories));

        }});
        
        boolean returnValue = CreativeEligibilityManagerJpaImpl.checkCategoryExclusionForTargeting(pub, creative, segment, reasonsWhyNot);
        assertTrue("Test case failed, should have returned true.Campaign should be targeted for this adspace/publication",returnValue);
        assertEquals("Test case failed, reasonsWhyNot list should have zero Entry",0,reasonsWhyNot.size());
    }
    
    /**
     * AF-319 As the Adserver I want to enforce the Channel targeting selection so that Advertiser can 
     * maximize their exposure to certain Publisher Channels
     * 
     * Acceptance 11B : If segment is channel enabled/disabled/whatever and 
     * Publication category is null(not provided) and segment excluded categories are Empty
     * 
     *  then this campaign should be targeted for this adspace/publication
     * 
     */
    @Test
    public void testcheckCategoryExclusionForTargeting_publicationCategoryNullAndEmptySegmentExcludedCategoryExists() {
        final Segment segment = mock(Segment.class,"segment");
        final Publication pub = mock(Publication.class,"pub");
        final Creative creative = mock(Creative.class,"creative");
        final List<String> reasonsWhyNot = new ArrayList<String>();
        final Category campaignCategory = null;
        final Set<Category> publicationExcludedCategories = Collections.emptySet();
        final Campaign campaign = mock(Campaign.class,"campaign");
        final Publisher publisher = mock(Publisher.class);        
        final Set<Category> segmentExcludedCategories = new HashSet<Category>();
        
        
        expect(new Expectations() {{
            oneOf (creative).getCampaign();
                will(returnValue(campaign));
            oneOf (campaign).getCategory();
                will(returnValue(campaignCategory));
            allowing (pub).getExcludedCategories();
                will(returnValue(publicationExcludedCategories));

            allowing (pub).getPublisher(); will(returnValue(publisher));
            allowing (publisher).getExcludedCategories(); will(returnValue(Collections.emptySet()));
            oneOf (pub).getCategory(); will(returnValue(adfonicNotCategorized));
            allowing (segment).getExcludedCategories();
                will(returnValue(segmentExcludedCategories));

        }});
        
        boolean returnValue = CreativeEligibilityManagerJpaImpl.checkCategoryExclusionForTargeting(pub, creative, segment, reasonsWhyNot);
        assertTrue("Test case failed, should have returned true.Campaign should be targeted for this adspace/publication",returnValue);
        assertEquals("Test case failed, reasonsWhyNot list should have zero Entry",0,reasonsWhyNot.size());
    }
    
    /**
     * AF-319 As the Adserver I want to enforce the Channel targeting selection so that Advertiser can 
     * maximize their exposure to certain Publisher Channels
     * 
     * Acceptance 12A : If segment is channel enabled/disabled/whatever and 
     * Publication category is not null(provided) and segment excluded categories is null
     * 
     *  then this campaign should be targeted for this adspace/publication
     * 
     */
    @Test
    public void testcheckCategoryExclusionForTargeting_publicationCategoryNotNullAndNullSegmentExcludedCategoryExists() {
        final Segment segment = mock(Segment.class,"segment");
        final Publication pub = mock(Publication.class,"pub");
        final Creative creative = mock(Creative.class,"creative");
        final List<String> reasonsWhyNot = new ArrayList<String>();
        final Category campaignCategory = null;
        final Set<Category> publicationExcludedCategories = Collections.emptySet();
        final Campaign campaign = mock(Campaign.class,"campaign");
        final Publisher publisher = mock(Publisher.class);        
        final Set<Category> segmentExcludedCategories = Collections.emptySet();
        final Category publicationCategory = mock(Category.class,"publicationCategory");
        
        expect(new Expectations() {{
            oneOf (creative).getCampaign();
                will(returnValue(campaign));
            oneOf (campaign).getCategory();
                will(returnValue(campaignCategory));
            allowing (pub).getExcludedCategories();
                will(returnValue(publicationExcludedCategories));

            allowing (pub).getPublisher(); will(returnValue(publisher));
            allowing (publisher).getExcludedCategories(); will(returnValue(Collections.emptySet()));
            oneOf (pub).getCategory(); will(returnValue(publicationCategory));
            allowing (segment).getExcludedCategories();
                will(returnValue(segmentExcludedCategories));

        }});
        
        boolean returnValue = CreativeEligibilityManagerJpaImpl.checkCategoryExclusionForTargeting(pub, creative, segment, reasonsWhyNot);
        assertTrue("Test case failed, should have returned true.Campaign should be targeted for this adspace/publication",returnValue);
        assertEquals("Test case failed, reasonsWhyNot list should have zero Entry",0,reasonsWhyNot.size());
    }
    
    /**
     * AF-319 As the Adserver I want to enforce the Channel targeting selection so that Advertiser can 
     * maximize their exposure to certain Publisher Channels
     * 
     * Acceptance 12B : If segment is channel enabled/disabled/whatever and 
     * Publication category is not null(provided) and segment excluded categories is empty
     * 
     *  then this campaign should be targeted for this adspace/publication
     * 
     */
    @Test
    public void testcheckCategoryExclusionForTargeting_publicationCategoryNotNullAndEmptySegmentExcludedCategoryExists() {
        final Segment segment = mock(Segment.class,"segment");
        final Publication pub = mock(Publication.class,"pub");
        final Creative creative = mock(Creative.class,"creative");
        final List<String> reasonsWhyNot = new ArrayList<String>();
        final Category campaignCategory = null;
        final Set<Category> publicationExcludedCategories = Collections.emptySet();
        final Campaign campaign = mock(Campaign.class,"campaign");
        final Publisher publisher = mock(Publisher.class);        
        final Set<Category> segmentExcludedCategories = new HashSet<Category>();
        final Category publicationCategory = mock(Category.class,"publicationCategory");
        
        expect(new Expectations() {{
            oneOf (creative).getCampaign();
                will(returnValue(campaign));
            oneOf (campaign).getCategory();
                will(returnValue(campaignCategory));
            allowing (pub).getExcludedCategories();
                will(returnValue(publicationExcludedCategories));

            allowing (pub).getPublisher(); will(returnValue(publisher));
            allowing (publisher).getExcludedCategories(); will(returnValue(Collections.emptySet()));
            oneOf (pub).getCategory(); will(returnValue(publicationCategory));
            allowing (segment).getExcludedCategories();
                will(returnValue(segmentExcludedCategories));

        }});
        
        boolean returnValue = CreativeEligibilityManagerJpaImpl.checkCategoryExclusionForTargeting(pub, creative, segment, reasonsWhyNot);
        assertTrue("Test case failed, should have returned true.Campaign should be targeted for this adspace/publication",returnValue);
        assertEquals("Test case failed, reasonsWhyNot list should have zero Entry",0,reasonsWhyNot.size());
    }
    
    /**
     * AF-319 As the Adserver I want to enforce the Channel targeting selection so that Advertiser can 
     * maximize their exposure to certain Publisher Channels
     * 
     * Acceptance 12C : If segment is channel enabled/disabled/whatever and 
     * Publication category is not null(provided) and segment excluded categories is Non Empty set,
     * but none of the list item matches with publicatioCategory
     * 
     *  then this campaign should be targeted for this adspace/publication
     * 
     */
    @Test
    public void testcheckCategoryExclusionForTargeting_publicationCategoryNotNullAndNonEmptySegmentExcludedCategoryExistsButNoMatch() {
        final Segment segment = mock(Segment.class,"segment");
        final Publication pub = mock(Publication.class,"pub");
        final Creative creative = mock(Creative.class,"creative");
        final List<String> reasonsWhyNot = new ArrayList<String>();
        final Category campaignCategory = null;
        final Set<Category> publicationExcludedCategories = Collections.emptySet();
        final Campaign campaign = mock(Campaign.class,"campaign");
        
        final Set<Category> segmentExcludedCategories = new HashSet<Category>();
        final Category segmentExcludedCategory = mock(Category.class,"segmentExcludedCategory");
        segmentExcludedCategories.add(segmentExcludedCategory);
        final List<Category> segmentExcludedCategoryChildrens = new ArrayList<Category>();
        final Category publicationCategory = mock(Category.class,"publicationCategory");
        final Publisher publisher = mock(Publisher.class);
        
        expect(new Expectations() {{
            oneOf (creative).getCampaign();
                will(returnValue(campaign));
            oneOf (campaign).getCategory();
                will(returnValue(campaignCategory));
            allowing (pub).getExcludedCategories();
                will(returnValue(publicationExcludedCategories));

            allowing (pub).getPublisher(); will(returnValue(publisher));
            allowing (publisher).getExcludedCategories(); will(returnValue(Collections.emptySet()));
            oneOf (pub).getCategory(); will(returnValue(publicationCategory));
            allowing (segment).getExcludedCategories();
                will(returnValue(segmentExcludedCategories));
            oneOf (segmentExcludedCategory).getChildren();
                will(returnValue(segmentExcludedCategoryChildrens));
            allowing (pub).getStatedCategories(); will(returnValue(new HashSet<Category>()));
        }});
        
        boolean returnValue = CreativeEligibilityManagerJpaImpl.checkCategoryExclusionForTargeting(pub, creative, segment, reasonsWhyNot);
        assertTrue("Test case failed, should have returned true.Campaign should be targeted for this adspace/publication",returnValue);
        assertEquals("Test case failed, reasonsWhyNot list should have zero Entry",0,reasonsWhyNot.size());
    }
    
    /**
     * AF-319 As the Adserver I want to enforce the Channel targeting selection so that Advertiser can 
     * maximize their exposure to certain Publisher Channels
     * 
     * Acceptance 13A : If segment is channel enabled/disabled/whatever and 
     * Publication category is not null(provided) and segment excluded categories is Non Empty set,
     * but it one of the list item matches with publicatioCategory
     * 
     *  then this campaign should not be targeted for this adspace/publication
     * 
     */
    @Test
    public void testcheckCategoryExclusionForTargeting_publicationCategoryNotNullAndNonEmptySegmentExcludedCategoryExistsAndMatch() {
        final Segment segment = mock(Segment.class,"segment");
        final Publication pub = mock(Publication.class,"pub");
        final Creative creative = mock(Creative.class,"creative");
        final List<String> reasonsWhyNot = new ArrayList<String>();
        final Category campaignCategory = null;
        final Set<Category> publicationExcludedCategories = Collections.emptySet();
        final Campaign campaign = mock(Campaign.class,"campaign");
        
        final Category publicationCategory = mock(Category.class,"publicationCategory");

        final Set<Category> segmentExcludedCategories = new HashSet<Category>();
        final Category segmentExcludedCategory = mock(Category.class,"segmentExcludedCategory");
        segmentExcludedCategories.add(segmentExcludedCategory);
        segmentExcludedCategories.add(publicationCategory);
        final List<Category> segmentExcludedCategoryChildrens = new ArrayList<Category>();
        final List<Category> publicationCategoryChildrens = new ArrayList<Category>();
        final long publicationCategoryId = 2;
        final String publicationCategoryName = randomAlphaNumericString(30);
        final Publisher publisher = mock(Publisher.class);
        
        expect(new Expectations() {{
            oneOf (creative).getCampaign();
                will(returnValue(campaign));
            oneOf (campaign).getCategory();
                will(returnValue(campaignCategory));
            allowing (pub).getExcludedCategories();
                will(returnValue(publicationExcludedCategories));

            allowing (pub).getPublisher(); will(returnValue(publisher));
            allowing (publisher).getExcludedCategories(); will(returnValue(Collections.emptySet()));
            oneOf (pub).getCategory(); will(returnValue(publicationCategory));
            allowing (segment).getExcludedCategories();
                will(returnValue(segmentExcludedCategories));
            oneOf (segmentExcludedCategory).getChildren();
                will(returnValue(segmentExcludedCategoryChildrens));
            oneOf (publicationCategory).getChildren();
                will(returnValue(publicationCategoryChildrens));
            oneOf (publicationCategory).getId();
                will(returnValue(publicationCategoryId));
            oneOf (publicationCategory).getName();
                will(returnValue(publicationCategoryName));

            allowing (pub).getStatedCategories(); will(returnValue(new HashSet<Category>()));
        }});
        
        boolean returnValue = CreativeEligibilityManagerJpaImpl.checkCategoryExclusionForTargeting(pub, creative, segment, reasonsWhyNot);
        assertFalse("Test case failed, should have returned false.Campaign should not be targeted for this adspace/publication",returnValue);
        assertEquals("Test case failed, reasonsWhyNot list should have one Entry",1,reasonsWhyNot.size());
    }
    
    /**
     * AF-319 As the Adserver I want to enforce the Channel targeting selection so that Advertiser can 
     * maximize their exposure to certain Publisher Channels
     * 
     * Acceptance 13B : If segment is channel enabled/disabled/whatever and 
     * Publication category is not null(provided) and segment excluded categories is Non Empty set,
     * but it one of the list item matches with publicatioCategory's children category
     * 
     *  then this campaign should be targeted for this adspace/publication
     * 
     */
    @Test
    public void testcheckCategoryExclusionForTargeting_publicationCategoryNotNullAndNonEmptySegmentExcludedCategoryExistsAndMatchChildren() {
        final Segment segment = mock(Segment.class,"segment");
        final Publication pub = mock(Publication.class,"pub");
        final Creative creative = mock(Creative.class,"creative");
        final List<String> reasonsWhyNot = new ArrayList<String>();
        final Category campaignCategory = null;
        final Set<Category> publicationExcludedCategories = Collections.emptySet();
        final Campaign campaign = mock(Campaign.class,"campaign");
        
        final Category publicationCategory = mock(Category.class,"publicationCategory");

        final Set<Category> segmentExcludedCategories = new HashSet<Category>();
        final Category segmentExcludedCategory = mock(Category.class,"segmentExcludedCategory");
        segmentExcludedCategories.add(segmentExcludedCategory);
        final List<Category> segmentExcludedCategoryChildrens = new ArrayList<Category>();
        segmentExcludedCategoryChildrens.add(publicationCategory);
        final List<Category> publicationCategoryChildrens = new ArrayList<Category>();
        final long publicationCategoryId = 2;
        final String publicationCategoryName = randomAlphaNumericString(30);
        final long segmentExcludedCategoryId = 2;
        final String segmentExcludedCategoryName = randomAlphaNumericString(30);
        final Publisher publisher = mock(Publisher.class);
        
        expect(new Expectations() {{
            oneOf (creative).getCampaign();
                will(returnValue(campaign));
            oneOf (campaign).getCategory();
                will(returnValue(campaignCategory));
            allowing (pub).getExcludedCategories();
                will(returnValue(publicationExcludedCategories));

            allowing (pub).getPublisher(); will(returnValue(publisher));
            allowing (publisher).getExcludedCategories(); will(returnValue(Collections.emptySet()));
            oneOf (pub).getCategory(); will(returnValue(publicationCategory));
            allowing (segment).getExcludedCategories();
                will(returnValue(segmentExcludedCategories));
            oneOf (segmentExcludedCategory).getChildren();
                will(returnValue(segmentExcludedCategoryChildrens));
            oneOf (publicationCategory).getChildren();
                will(returnValue(publicationCategoryChildrens));
            oneOf (publicationCategory).getId();
                will(returnValue(publicationCategoryId));
            oneOf (publicationCategory).getName();
                will(returnValue(publicationCategoryName));
            oneOf (segmentExcludedCategory).getId();
                will(returnValue(segmentExcludedCategoryId));
            oneOf (segmentExcludedCategory).getName();
                will(returnValue(segmentExcludedCategoryName));

            allowing (pub).getStatedCategories(); will(returnValue(new HashSet<Category>()));
        }});
        
        boolean returnValue = CreativeEligibilityManagerJpaImpl.checkCategoryExclusionForTargeting(pub, creative, segment, reasonsWhyNot);
        assertFalse("Test case failed, should have returned false.Campaign should not be targeted for this adspace/publication",returnValue);
        assertEquals("Test case failed, reasonsWhyNot list should have one Entry",1,reasonsWhyNot.size());
    }
}
