package com.byyd.middleware.common.service;

import static com.byyd.middleware.iface.dao.SortOrder.asc;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.adfonic.domain.AdSpace;
import com.adfonic.domain.Campaign;
import com.adfonic.domain.Category;
import com.adfonic.domain.Channel;
import com.adfonic.domain.Component_;
import com.adfonic.domain.ContentSpec;
import com.adfonic.domain.ContentType;
import com.adfonic.domain.Country;
import com.adfonic.domain.Format;
import com.adfonic.domain.Format_;
import com.adfonic.domain.Language;
import com.adfonic.domain.MobileIpAddressRange;
import com.adfonic.domain.Operator;
import com.adfonic.domain.Region;
import com.adfonic.domain.Segment;
import com.adfonic.domain.TransparentNetwork;
import com.adfonic.domain.UploadedContent;
import com.byyd.middleware.campaign.filter.ChannelFilter;
import com.byyd.middleware.campaign.service.CampaignManager;
import com.byyd.middleware.campaign.service.TargetingManager;
import com.byyd.middleware.creative.service.CreativeManager;
import com.byyd.middleware.device.service.DeviceManager;
import com.byyd.middleware.iface.dao.FetchStrategy;
import com.byyd.middleware.iface.dao.FetchStrategyImpl;
import com.byyd.middleware.iface.dao.FetchStrategyImpl.JoinType;
import com.byyd.middleware.iface.dao.LikeSpec;
import com.byyd.middleware.iface.dao.Sorting;
import com.byyd.middleware.iface.dao.jpa.FetchStrategyBuilder;
import com.byyd.middleware.publication.service.PublicationManager;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/adfonic-springdata-hibernate-context.xml"})
@DirtiesContext
public class CommonManagerIT {
    
    @Autowired
    private CommonManager commonManager;
    
    @Autowired
    private DeviceManager devicesManager;
    
    @Autowired
    private TargetingManager targetingManager;
    
    @Autowired
    private CreativeManager creativeManager;
    
    @Autowired
    private CampaignManager campaignManager;
    
    @Autowired
    private PublicationManager publicationManager;
    
    //----------------------------------------------------------------------------------------------------------------

    @Test
    public void testGetUploadedContentByIdWithInvalidId() {
        assertNull(commonManager.getUploadedContentById(0L));
    }

    @Test
    public void testUploadedContent() {
       ContentType contentType = commonManager.getContentTypeByName("PNG");
       UploadedContent content = null;
       byte[] data = "Testing".getBytes();
       try {
           content = commonManager.newUploadedContent(contentType, data);
           assertNotNull(content);
           long id = content.getId();
           assertTrue(id > 0);

           content = commonManager.getUploadedContentById(id);
           assertNotNull(content);
           assertEquals(content.getId(), id);

           content = commonManager.getUploadedContentById(Long.toString(id));
           assertNotNull(content);
           assertEquals(content.getId(), id);

           byte[] newData = "Testing Changed".getBytes();
           content.setData(newData);
           content = commonManager.update(content);
           assertEquals(new String(content.getData()), new String(newData));

           String externalId = content.getExternalID();
           UploadedContent uc = commonManager.getUploadedContentByExternalId(externalId);
           assertEquals(uc.getId(), content.getId());

       } catch(Exception e) {
           String stackTrace = ExceptionUtils.getStackTrace(e);
           System.out.println(stackTrace);
           fail(stackTrace);
       } finally {
           commonManager.delete(content);
           assertNull(commonManager.getUploadedContentById(content.getId()));
       }
    }
    
    //----------------------------------------------------------------------------------------------------------------

    @Test
    public void testMobileIpAddressRange() {
        long startPoint = 1000L;
        long endPoint = 2000L;
        String carrier = "Testing" + System.currentTimeMillis();
        Country country = commonManager.getCountryByIsoCode("GB");
        Operator operator = devicesManager.getOperatorByName("Mobiland");
        MobileIpAddressRange.Source source = MobileIpAddressRange.Source.MASSIVE;
        int priority = 0;
        MobileIpAddressRange range = null;
        try {
            range = new MobileIpAddressRange();
            range.setStartPoint(startPoint);
            range.setEndPoint(endPoint);
            range.setCarrier(carrier);
            range.setCountry(country);
            range.setOperator(operator);
            range.setSource(source);
            range.setPriority(priority);

            range = commonManager.create(range);
            assertNotNull(range);
            assertTrue(range.getId() > 0);

            assertEquals(range, commonManager.getMobileIpAddressRangeById(range.getId()));
            assertEquals(range, commonManager.getMobileIpAddressRangeById(Long.toString(range.getId())));

            carrier = carrier + "Changed";
            range.setCarrier(carrier);
            range = commonManager.update(range);

            range = commonManager.getMobileIpAddressRangeById(range.getId());
            assertEquals(range.getCarrier(), carrier);

            long count = commonManager.countAllMobileIpAddressRanges();
            assertTrue(count > 0);

            List<MobileIpAddressRange> list = commonManager.getAllMobileIpAddressRanges(new Sorting(asc("carrier")));
            assertTrue(list != null);
            assertTrue(list.size() > 0);
            assertTrue(list.contains(range));

        } catch(Exception e) {
               String stackTrace = ExceptionUtils.getStackTrace(e);
            System.out.println(stackTrace);
            fail(stackTrace);
        } finally {
            commonManager.delete(range);
            assertNull(commonManager.getMobileIpAddressRangeById(range.getId()));
        }

    }
    
    //----------------------------------------------------------------------------------------

    @Test
    public void testCategories() {
        try {
           Category category = commonManager.getCategoryByName("Automotive");
           assertNotNull(category);
           assertEquals(category, commonManager.getCategoryByName("Automotive".toUpperCase(), true));
        
           assertEquals(category, commonManager.getCategoryById(category.getId()));
           assertEquals(category, commonManager.getCategoryById(Long.toString(category.getId())));
        
           long count = commonManager.countAllCategories();
           assertTrue(count > 0);
        
           List<Category> categories = commonManager.getAllCategories();
           assertNotNull(categories);
           assertTrue(categories.size() > 0);
           System.out.println("All Categories:");
           for(Category p : categories) {
               System.out.println(p.getName());
           }
        
           categories = commonManager.getAllCategories(new Sorting(asc("name")));
           assertNotNull(categories);
           assertTrue(categories.size() > 0);
           System.out.println("All Categories, sorted by name:");
           for(Category p : categories) {
               System.out.println(p.getName());
           }
        
           categories = commonManager.getCategoriesByName("school", LikeSpec.CONTAINS, false);
           assertNotNull(categories);
           assertTrue(categories.size() > 0);
           System.out.println("All Categories containing \"school\":");
           for(Category p : categories) {
               System.out.println(p.getName());
           }
        
           categories = commonManager.getCategoriesByName("school", LikeSpec.CONTAINS, false, new Sorting(asc("name")));
           assertNotNull(categories);
           assertTrue(categories.size() > 0);
           System.out.println("All Categories containing \"school\", sorted by name:");
           for(Category p : categories) {
               System.out.println(p.getName());
           }
        
           FetchStrategyImpl fs = new FetchStrategyImpl();
           fs.addEagerlyLoadedFieldForClass(Segment.class, "excludedCategories", JoinType.LEFT);
           String excludedCategoryNamesStr = commonManager.getExcludedCategoryNamesBySegment(targetingManager.getSegmentById(110L, fs));
           assertNotNull(excludedCategoryNamesStr);
           System.out.println("All excluded categories for a segment in a string: " + excludedCategoryNamesStr);
        
       } catch(Exception e) {
           String stackTrace = ExceptionUtils.getStackTrace(e);
           System.out.println(stackTrace);
           fail(stackTrace);
       }

   }

    @Test
    public void testGetParentCategory() {
        try {
           Category category = commonManager.getCategoryByName("Dating");
           assertNotNull(category);

           Category parentCategory = commonManager.getParentCategoryForCategory(category);
           assertNotNull(parentCategory);
           assertEquals(parentCategory.getName(), "Society");
        } catch(Exception e) {
               String stackTrace = ExceptionUtils.getStackTrace(e);
               System.out.println(stackTrace);
               fail(stackTrace);
        }
   }

   @Test
   public void testGetDefaultCategory() {
       try {
           Category category = commonManager.getDefaultCategory();
           assertNotNull(category);
           assertEquals(category.getName(), Category.NOT_CATEGORIZED_NAME);
        } catch(Exception e) {
               String stackTrace = ExceptionUtils.getStackTrace(e);
               System.out.println(stackTrace);
               fail(stackTrace);
        }
   }

   //----------------------------------------------------------------------------------------

   @Test
   public void testCountries() {
      try {
          Country country = commonManager.getCountryByName("Belgium");
          assertNotNull(country);
          assertEquals(country, commonManager.getCountryByName("Belgium".toUpperCase(), true));

          assertEquals(country, commonManager.getCountryById(country.getId()));
          assertEquals(country, commonManager.getCountryById(Long.toString(country.getId())));

          long count = commonManager.countAllCountries();
          assertTrue(count > 0);

          List<Country> countries = commonManager.getAllCountries();
          assertNotNull(countries);
          assertTrue(countries.size() > 0);
          System.out.println("All Countries:");
          for(Country p : countries) {
              System.out.println(p.getName());
              if(p.getName().equals("Unknown")) {
                  fail("Unknown country was found");
              }
          }
           
          countries = commonManager.getAllCountries(true);
          boolean unknownFound = true;
          for(Country p : countries) {
              System.out.println(p.getName());
              if(p.getName().equals("Unknown")) {
                  unknownFound = true;
              }
          }
          if(!unknownFound) {
              fail("Unknown country was not found");
          }

          countries = commonManager.getAllCountries(new Sorting(asc("name")));
          assertNotNull(countries);
          assertTrue(countries.size() > 0);
          System.out.println("All Countries, sorted by name:");
          for(Country p : countries) {
              System.out.println(p.getName());
          }

          countries = commonManager.getCountriesByName("united", LikeSpec.CONTAINS, false);
          assertNotNull(countries);
          assertTrue(countries.size() > 0);
          System.out.println("All Countries containing \"united\":");
          for(Country p : countries) {
              System.out.println(p.getName());
          }

          countries = commonManager.getCountriesByName("united", LikeSpec.CONTAINS, false, new Sorting(asc("name")));
          assertNotNull(countries);
          assertTrue(countries.size() > 0);
          System.out.println("All Countries containing \"united\", sorted by name:");
          for(Country p : countries) {
              System.out.println(p.getName());
          }

          countries = commonManager.getCountriesByName("un", LikeSpec.CONTAINS, false, false, new Sorting(asc("name")));
          assertNotNull(countries);
          assertTrue(countries.size() > 0);
          System.out.println("All non-hidden Countries containing \"un\", sorted by name:");
          for(Country p : countries) {
              System.out.println(p.getName());
          }

          countries = commonManager.getCountriesByName("un", LikeSpec.CONTAINS, false, true, new Sorting(asc("name")));
          assertNotNull(countries);
          assertTrue(countries.size() > 0);
          System.out.println("All hidden Countries containing \"un\", sorted by name:");
          for(Country p : countries) {
              System.out.println(p.getName());
          }

          String countryNamesStr = commonManager.getCountryNamesBySegment(targetingManager.getSegmentById(110L));
          assertNotNull(countryNamesStr);
          System.out.println("All Countries for segment in a string: " + countryNamesStr);

      } catch(Exception e) {
             String stackTrace = ExceptionUtils.getStackTrace(e);
            System.out.println(stackTrace);
            fail(stackTrace);
      }

   }

    @Test
    public void testGetCountryByIsoCode() {
       Country country = commonManager.getCountryByIsoCode("ZA");
       assertNotNull(country);
       assertEquals(country.getId(), (long)1);
    }

    @Test
    public void testGetCountryByRegexName() {
        System.out.println("Ends with es:");
        List<Country> countries = commonManager.getCountriesByName("es", LikeSpec.WORD_ENDS_WITH, false);
        for(Country country : countries) {
            System.out.println(country.getName());
        }
        System.out.println("Starts with es:");
        countries = commonManager.getCountriesByName("es", LikeSpec.WORD_STARTS_WITH, false);
        for(Country country : countries) {
            System.out.println(country.getName());
        }
    }

    //----------------------------------------------------------------------------------------------------------------

    @Test
    public void testRegions() {
        try {
            Region region = commonManager.getRegionByName("Africa");
            assertNotNull(region);
            assertEquals(region, commonManager.getRegionByName("Africa".toUpperCase(), true));

            assertEquals(region, commonManager.getRegionById(region.getId()));
            assertEquals(region, commonManager.getRegionById(Long.toString(region.getId())));

            long count = commonManager.countAllRegions();
            assertTrue(count > 0);

            List<Region> regions = commonManager.getAllRegions();
            assertNotNull(regions);
            assertTrue(regions.size() > 0);
            System.out.println("All Regions:");
            for(Region p : regions) {
                System.out.println(p.getName());
            }

            regions = commonManager.getAllRegions(new Sorting(asc("name")));
            assertNotNull(regions);
            assertTrue(regions.size() > 0);
            System.out.println("All Regions, sorted by name:");
            for(Region p : regions) {
                System.out.println(p.getName());
            }

            regions = commonManager.getRegionsByName("america", LikeSpec.CONTAINS, false);
            assertNotNull(regions);
            assertTrue(regions.size() > 0);
            System.out.println("All Regions containing \"america\":");
            for(Region p : regions) {
                System.out.println(p.getName());
            }

            regions = commonManager.getRegionsByName("america", LikeSpec.CONTAINS, false, new Sorting(asc("name")));
            assertNotNull(regions);
            assertTrue(regions.size() > 0);
            System.out.println("All Regions containing \"america\", sorted by name:");
            for(Region p : regions) {
                System.out.println(p.getName());
            }

        } catch(Exception e) {
               String stackTrace = ExceptionUtils.getStackTrace(e);
               System.out.println(stackTrace);
               fail(stackTrace);
        }
    }
    
    //----------------------------------------------------------------------------------------

    @Test
    public void testLanguages() {
        try {
            Language language = commonManager.getLanguageByName("French");
            assertNotNull(language);
            assertEquals(language, commonManager.getLanguageByName("FRENCH", true));

            assertEquals(language, commonManager.getLanguageById(language.getId()));
            assertEquals(language, commonManager.getLanguageById(Long.toString(language.getId())));

            long count = commonManager.countAllLanguages();
            assertTrue(count > 0);

            List<Language> languages = commonManager.getAllLanguages();
            assertNotNull(languages);
            assertTrue(languages.size() > 0);
            System.out.println("All Languages:");
            for(Language p : languages) {
                System.out.println(p.getName());
            }

            languages = commonManager.getAllLanguages(new Sorting(asc("name")));
            assertNotNull(languages);
            assertTrue(languages.size() > 0);
            System.out.println("All Languages, sorted by name:");
            for(Language p : languages) {
                System.out.println(p.getName());
            }

            languages = commonManager.getLanguagesByName("en", LikeSpec.CONTAINS, false);
            assertNotNull(languages);
            assertTrue(languages.size() > 0);
            System.out.println("All Languages containing \"en\":");
            for(Language p : languages) {
                System.out.println(p.getName());
            }

            languages = commonManager.getLanguagesByName("en", LikeSpec.CONTAINS, false, new Sorting(asc("name")));
            assertNotNull(languages);
            assertTrue(languages.size() > 0);
            System.out.println("All Languages containing \"en\", sorted by name:");
            for(Language p : languages) {
                System.out.println(p.getName());
            }

        } catch(Exception e) {
               String stackTrace = ExceptionUtils.getStackTrace(e);
               System.out.println(stackTrace);
               fail(stackTrace);
        }

    }

    @Test
    public void testGetLanguageByIsoCode() {
       Language language = commonManager.getLanguageByIsoCode("fr");
       assertNotNull(language);
       assertEquals(language.getName(), "French");
    }
    
  //----------------------------------------------------------------------------------------------------------------

    @Test
    public void testChannel() {
        Channel channel = null;
        String name = "Testing";
        try {
            channel = commonManager.newChannel(name);
            assertNotNull(channel);
            long id = channel.getId();
            assertEquals(channel, commonManager.getChannelById(id));
            assertEquals(channel, commonManager.getChannelById(Long.toString(id)));

            String newName = name + " Changed";
            channel.setName(newName);
            channel = commonManager.update(channel);
            channel = commonManager.getChannelById(channel.getId());
            assertEquals(newName, channel.getName());

            boolean channelFound = false;
            boolean uncategorizedChannelFound = false;
            List<Channel> channels = commonManager.getAllChannels(new ChannelFilter().setExcludeUncategorized(true));
            for(Channel c : channels) {
                if(c.getName().equals(newName)) {
                    channelFound = true;
                }
                if(c.getName().equals(Channel.NOT_CATEGORIZED_NAME)) {
                    uncategorizedChannelFound = true;
                }
            }
            if(!channelFound || uncategorizedChannelFound) {
                fail();
            }

            channelFound = false;
            uncategorizedChannelFound = false;
            ChannelFilter filter = new ChannelFilter().setExcludeUncategorized(false);
            channels = commonManager.getAllChannels(filter);
            for(Channel c : channels) {
                if(c.getName().equals(newName)) {
                    channelFound = true;
                }
                if(c.getName().equals(Channel.NOT_CATEGORIZED_NAME)) {
                    uncategorizedChannelFound = true;
                }
            }
            if(!channelFound || !uncategorizedChannelFound) {
                fail();
            }

        } catch(Exception e) {
            String stackTrace = ExceptionUtils.getStackTrace(e);
            System.out.println(stackTrace);
            fail(stackTrace);
        } finally {
            commonManager.delete(channel);
        }
    }
    
    //----------------------------------------------------------------------------------------

    @Test
    public void testFormats() {
        try {
            Format format = commonManager.getFormatByName("Banner");
            assertNotNull(format);
            assertEquals(format, commonManager.getFormatByName("BANNER", true));

            assertEquals(format, commonManager.getFormatById(format.getId()));
            assertEquals(format, commonManager.getFormatById(Long.toString(format.getId())));

            long count = commonManager.countAllFormats();
            assertTrue(count > 0);

            List<Format> formats = commonManager.getAllFormats();
            assertNotNull(formats);
            assertTrue(formats.size() > 0);
            System.out.println("All Formats:");
            for(Format p : formats) {
                System.out.println(p.getName());
            }

            formats = commonManager.getAllFormats(new Sorting(asc("name")));
            assertNotNull(formats);
            assertTrue(formats.size() > 0);
            System.out.println("All Formats, sorted by name:");
            for(Format p : formats) {
                System.out.println(p.getName());
            }

            formats = commonManager.getFormatsByName("banner", LikeSpec.CONTAINS, false);
            assertNotNull(formats);
            assertTrue(formats.size() > 0);
            System.out.println("All Formats containing \"banner\":");
            for(Format p : formats) {
                System.out.println(p.getName());
            }

            formats = commonManager.getFormatsByName("banner", LikeSpec.CONTAINS, false, new Sorting(asc("name")));
            assertNotNull(formats);
            assertTrue(formats.size() > 0);
            System.out.println("All Formats containing \"banner\", sorted by name:");
            for(Format p : formats) {
                System.out.println(p.getName());
            }

            FetchStrategyImpl fs = new FetchStrategyImpl();
            fs.addEagerlyLoadedFieldForClass(Campaign.class, "transparentNetworks", JoinType.LEFT);
            Campaign campaign = campaignManager.getCampaignById(1L, fs);
            formats = commonManager.getSupportedFormats(campaign);
            for(Format p : formats) {
                System.out.println(p.getName());
            }

            TransparentNetwork tn1 = publicationManager.getTransparentNetworkById(1L);
            TransparentNetwork tn3 = publicationManager.getTransparentNetworkById(3L);

            campaign.getTransparentNetworks().add(tn1);
            campaign.getTransparentNetworks().add(tn3);

            formats = commonManager.getSupportedFormats(campaign);
            for(Format p : formats) {
                System.out.println(p.getName());
            }


        } catch(Exception e) {
               String stackTrace = ExceptionUtils.getStackTrace(e);
               System.out.println(stackTrace);
               fail(stackTrace);
        }

    }

    @Test
    public void testFormatDeux() {
        try {
             FetchStrategy formatFs = new FetchStrategyBuilder()
                                         .addLeft(Format_.displayTypes)
                                         //.addLeft(Format_.components)
                                         .addLeft(Component_.contentSpecMap)
                                         .build();
            Format format = commonManager.getFormatById(1L, formatFs);
            System.out.println(format.toString());
        } catch(Exception e) {
            String stackTrace = ExceptionUtils.getStackTrace(e);
            System.out.println(stackTrace);
            fail(stackTrace);
     }
    }

    @Test
    public void testGetSupportedFormats() {
        try {
            List<AdSpace> adSpaces = new ArrayList<AdSpace>();
            adSpaces.add(publicationManager.getAdSpaceById(1L));
            adSpaces.add(publicationManager.getAdSpaceById(2L));
            adSpaces.add(publicationManager.getAdSpaceById(5L));

            List<Format> formats = commonManager.getSupportedFormats(adSpaces);
            if(formats != null) {
                for(Format format : formats) {
                    System.out.println(format.getName());
                }
            }

        } catch(Exception e) {
            String stackTrace = ExceptionUtils.getStackTrace(e);
            System.out.println(stackTrace);
            fail(stackTrace);
        } finally {
        }
    }

  //----------------------------------------------------------------------------------------------------------------

    @Test
    public void testContentSpec() {
        String name = "Testing" + System.currentTimeMillis();
        String manifest = null;
        ContentSpec contentSpec = null;
        try {
            contentSpec = commonManager.newContentSpec(name, manifest);
            assertNotNull(contentSpec);
            assertTrue(contentSpec.getId() > 0);

            assertEquals(contentSpec, commonManager.getContentSpecById(contentSpec.getId()));
            assertEquals(contentSpec, commonManager.getContentSpecById(Long.toString(contentSpec.getId())));

            String newName = name + "Changed";
            contentSpec.setName(newName);
            contentSpec = commonManager.update(contentSpec);
            contentSpec = commonManager.getContentSpecById(contentSpec.getId());
            assertEquals(contentSpec.getName(), newName);

            List<ContentSpec> list = commonManager.getAllContentSpecs();
            assertNotNull(list);
            assertTrue(list.size() > 0);
            assertTrue(list.contains(contentSpec));

        } catch(Exception e) {
            String stackTrace = ExceptionUtils.getStackTrace(e);
            System.out.println(stackTrace);
            fail(stackTrace);
        } finally {
            commonManager.delete(contentSpec);
            assertNull(commonManager.getContentSpecById(contentSpec.getId()));
        }
    }
    
  //----------------------------------------------------------------------------------------------------------------

    @Test
    public void testGetContentTypeWithInvalidId() {
        assertNull(commonManager.getContentTypeById(0L));
    }

    @Test
    public void testContentType() {
        ContentType contentType = null;
        String name = "Testing";
        String mimeType = "text/testing";
        boolean animated = false;
        try {
            contentType = commonManager.newContentType(name, mimeType, animated);
            assertNotNull(contentType);
            long id = contentType.getId();
            assertTrue(id > 1L);

            assertEquals(contentType, commonManager.getContentTypeById(id));
            assertEquals(contentType, commonManager.getContentTypeById(Long.toString(id)));

            String newName = name + " Changed";
            contentType.setName(newName);
            contentType = commonManager.update(contentType);
            contentType = commonManager.getContentTypeById(contentType.getId());
            assertEquals(contentType.getName(), newName);

            ContentType c = commonManager.getContentTypeByName(newName);
            assertEquals(c.getId(), contentType.getId());

            c = commonManager.getContentTypeForMimeType(mimeType, animated);
            assertEquals(c.getId(), contentType.getId());

            List<ContentType> contentTypes = commonManager.getAllContentTypesForMimeType(mimeType);
            assertTrue(contentTypes.contains(contentType));

            contentTypes = commonManager.getAllContentTypesForMimeTypeLike(mimeType);
            assertTrue(contentTypes.contains(contentType));

            String testCt = "image/gif";
            assertNotNull(commonManager.getContentTypeForMimeType(testCt, true));
            assertNotNull(commonManager.getContentTypeForMimeType(testCt, false));

            // we know text/plain has no animated version
            testCt = "text/plain";
            assertNull(commonManager.getContentTypeForMimeType(testCt, true));
            assertNotNull(commonManager.getContentTypeForMimeType(testCt, false));

        } catch(Exception e) {
            String stackTrace = ExceptionUtils.getStackTrace(e);
            System.out.println(stackTrace);
            fail(stackTrace);
        } finally {
            commonManager.delete(contentType);
            assertNull(commonManager.getContentTypeById(contentType.getId()));
        }
    }

}
