package com.byyd.middleware.campaign.service.jpa;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.adfonic.domain.AdSpace;
import com.adfonic.domain.Advertiser;
import com.adfonic.domain.Advertiser_;
import com.adfonic.domain.Campaign;
import com.adfonic.domain.CampaignTimePeriod;
import com.adfonic.domain.Country;
import com.adfonic.domain.Geotarget;
import com.adfonic.domain.GeotargetList;
import com.adfonic.domain.GeotargetType;
import com.adfonic.domain.LocationTarget;
import com.adfonic.domain.Segment;
import com.byyd.middleware.account.service.AdvertiserManager;
import com.byyd.middleware.campaign.dao.CampaignTimePeriodDao;
import com.byyd.middleware.campaign.dao.GeotargetDao;
import com.byyd.middleware.campaign.dao.GeotargetListDao;
import com.byyd.middleware.campaign.dao.GeotargetTypeDao;
import com.byyd.middleware.campaign.dao.LocationTargetDao;
import com.byyd.middleware.campaign.dao.SegmentDao;
import com.byyd.middleware.campaign.filter.GeotargetFilter;
import com.byyd.middleware.campaign.filter.LocationTargetFilter;
import com.byyd.middleware.campaign.filter.SegmentStateSyncingFilter;
import com.byyd.middleware.campaign.service.CampaignManager;
import com.byyd.middleware.campaign.service.TargetingManager;
import com.byyd.middleware.iface.dao.FetchStrategy;
import com.byyd.middleware.iface.dao.LikeSpec;
import com.byyd.middleware.iface.dao.Pagination;
import com.byyd.middleware.iface.dao.Sorting;
import com.byyd.middleware.iface.dao.jpa.FetchStrategyBuilder;
import com.byyd.middleware.iface.service.jpa.BaseJpaManagerImpl;
import com.byyd.middleware.utils.AdfonicBeanDispatcher;

@Service("targetingManager")
public class TargetingManagerJpaImpl extends BaseJpaManagerImpl implements TargetingManager {
    
    @Autowired(required = false)
    private SegmentDao segmentDao;
    
    @Autowired(required = false)
    private LocationTargetDao locationTargetDao;
    
    @Autowired(required=false)
    private CampaignTimePeriodDao campaignTimePeriodDao;
    
    @Autowired(required = false)
    private GeotargetDao geotargetDao;
    
    @Autowired(required = false)
    private GeotargetTypeDao geotargetTypeDao;
    
    @Autowired(required = false)
    private GeotargetListDao geotargetListDao;
    
    @Value("${CopyCampaign.PositiveRetargeting.NoDaysForActivityTest:30}")
    int copyCampaignPositiveRetargetingNoDaysForActivityTest;
    
    // ------------------------------------------------------------------------------------------
    // Segment
    // ------------------------------------------------------------------------------------------
    @Override
    @Transactional(readOnly = false)
    public Segment newSegment(Advertiser advertiser, String name, FetchStrategy... fetchStrategy) {
        Advertiser localAdvertiser = advertiser;
        boolean segmentsSet = false;
        try {
            localAdvertiser.getSegments().size();
            segmentsSet = true;
        } catch(Exception e) {
            //do nothing
        }
        if(!segmentsSet) {
            FetchStrategy fs = new FetchStrategyBuilder()
                               .addLeft(Advertiser_.segments)
                               .build();
            AdvertiserManager advertiserManager = AdfonicBeanDispatcher.getBean(AdvertiserManager.class);
            localAdvertiser = advertiserManager.getAdvertiserById(localAdvertiser.getId(), fs);
        }
        Segment segment = localAdvertiser.newSegment();
        segment.setName(name);
        if(fetchStrategy == null || fetchStrategy.length == 0) {
            return create(segment);
        } else {
            segment = create(segment);
            return getSegmentById(segment.getId(), fetchStrategy);
        }
    }

    @Override
    @Transactional(readOnly = false)
    public Segment newSegment(Segment segment, FetchStrategy... fetchStrategy) {
        Segment persistedSegment = create(segment);
        if(fetchStrategy != null && fetchStrategy.length != 0) {
            persistedSegment = getSegmentById(persistedSegment.getId(), fetchStrategy);
        }
        return persistedSegment;
    }

    @Override
    public void syncStates(Segment source, Segment destination, SegmentStateSyncingFilter parameters) {
        destination.setDaysOfWeek(source.getDaysOfWeek());
        destination.setHoursOfDay(source.getHoursOfDay());
        destination.setHoursOfDayWeekend(source.getHoursOfDayWeekend());

        destination.getCountries().clear();
        destination.getCountries().addAll(source.getCountries());

        destination.getGeotargets().clear();
        destination.getGeotargets().addAll(source.getGeotargets());

        destination.getLocationTargets().clear();
        if(parameters.getCreateNewLocationTargets()) {
            for(LocationTarget locationtarget : source.getLocationTargets()) {
                destination.getLocationTargets().add(this.copyLocationTarget(locationtarget));
            }
        } else {
            for(LocationTarget locationtarget : source.getLocationTargets()) {
                destination.getLocationTargets().add(locationtarget);
            }
        }

        destination.setGeotargetType(source.getGeotargetType());

        destination.getOperators().clear();
        destination.getOperators().addAll(source.getOperators());

        destination.setGenderMix(source.getGenderMix());

        destination.setMinAge(source.getMinAge());
        destination.setMaxAge(source.getMaxAge());

        destination.getVendors().clear();
        destination.getVendors().addAll(source.getVendors());

        destination.getBrowsers().clear();
        destination.getBrowsers().addAll(source.getBrowsers());

        destination.getChannels().clear();
        destination.getChannels().addAll(source.getChannels());

        destination.getModels().clear();
        destination.getModels().addAll(source.getModels());

        destination.getPlatforms().clear();
        destination.getPlatforms().addAll(source.getPlatforms());

        //destination.getDayParting().clear();
        //destination.getDayParting().putAll(source.getDayParting());

        destination.getCapabilityMap().clear();
        destination.getCapabilityMap().putAll(source.getCapabilityMap());

        destination.getIpAddresses().clear();
        destination.getIpAddresses().addAll(source.getIpAddresses());
        destination.setIpAddressesListWhitelist(source.isIpAddressesListWhitelist());

        destination.getExcludedCategories().clear();
        destination.getExcludedCategories().addAll(source.getExcludedCategories());

        destination.getIncludedCategories().clear();
        destination.getIncludedCategories().addAll(source.getIncludedCategories());

        destination.setConnectionType(source.getConnectionType());
        destination.setMobileOperatorListIsWhitelist(source.getMobileOperatorListIsWhitelist());
        destination.setIspOperatorListIsWhitelist(source.getIspOperatorListIsWhitelist());
        destination.setCountryListIsWhitelist(source.getCountryListIsWhitelist());

        destination.getExcludedModels().clear();
        destination.getExcludedModels().addAll(source.getExcludedModels());

        destination.setSafetyLevel(source.getSafetyLevel());
        destination.setIncentivizedAllowed(source.isIncentivizedAllowed());

        destination.getAdSpaces().clear();
        destination.getAdSpaces().addAll(source.getAdSpaces());

        destination.getChannels().clear();
        destination.getChannels().addAll(source.getChannels());

        destination.setChannelEnabled(source.isChannelEnabled());

        destination.getTargettedPublishers().clear();
        destination.getTargettedPublishers().addAll(source.getTargettedPublishers());
        destination.setIncludeAdfonicNetwork(source.isIncludeAdfonicNetwork());

        // AT-1011Campaign copy missing 'Apps vs Websites' targeting
        destination.setMedium(source.getMedium());

        destination.getDeviceGroups().clear();
        destination.getDeviceGroups().addAll(source.getDeviceGroups());
    }

    @Override
    @Transactional(readOnly = false)
    public Segment copySegment(Segment segment, String name, FetchStrategy... fetchStrategy) {

        Segment dbSegment = this.getSegmentById(segment.getId());

        Segment newSegment = newSegment(dbSegment.getAdvertiser(), name);
        syncStates(dbSegment, newSegment, SegmentStateSyncingFilter.FOR_NEW_INSTANCE);
        newSegment = update(newSegment);

        if(fetchStrategy == null || fetchStrategy.length == 0) {
            return newSegment;
        } else {
            return getSegmentById(dbSegment.getId(), fetchStrategy);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Segment getSegmentById(String id, FetchStrategy... fetchStrategy) {
        return getSegmentById(makeLong(id), fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public Segment getSegmentById(Long id, FetchStrategy... fetchStrategy) {
        return segmentDao.getById(id, fetchStrategy);
    }

    @Transactional(readOnly = false)
    public Segment create(Segment segment) {
        return segmentDao.create(segment);
    }

    @Override
    @Transactional(readOnly = false)
    public Segment update(Segment segment) {
        return segmentDao.update(segment);
    }

    @Override
    @Transactional(readOnly = false)
    public void delete(Segment segment) {
        segmentDao.delete(segment);
    }

    @Override
    @Transactional(readOnly = false)
    public void deleteSegments(List<Segment> list) {
        if (list == null || list.isEmpty()) {
            return;
        }
        for (Segment entry : list) {
            delete(entry);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isAdSpaceAlreadyAllocated(Segment segment, AdSpace adSpace) {
        return segmentDao.isAdSpaceAlreadyAllocated(segment, adSpace);
    }

    @Override
    @Transactional(readOnly = true)
    public Long countBrowsersForSegment(Segment segment) {
        return segmentDao.countBrowsersForSegment(segment);
    }

    @Override
    @Transactional(readOnly = true)
    public Long countCategoriesForSegment(Segment segment) {
        return segmentDao.countCategoriesForSegment(segment);
    }

    @Override
    @Transactional(readOnly = true)
    public Long countExcludedCategoriesForSegment(Segment segment) {
        return segmentDao.countExcludedCategoriesForSegment(segment);
    }

    @Override
    @Transactional(readOnly = true)
    public Long countChannelsForSegment(Segment segment) {
        return segmentDao.countChannelsForSegment(segment);
    }

    @Override
    @Transactional(readOnly = true)
    public Long countCountriesForSegment(Segment segment) {
        return segmentDao.countCountriesForSegment(segment);
    }

    @Override
    @Transactional(readOnly = true)
    public Long countGeotargetsForSegment(Segment segment) {
        return segmentDao.countGeotargetsForSegment(segment);
    }

    @Override
    @Transactional(readOnly = true)
    public Long countIpAddressesForSegment(Segment segment) {
        return segmentDao.countIpAddressesForSegment(segment);
    }

    @Override
    @Transactional(readOnly = true)
    public Long countModelsForSegment(Segment segment) {
        return segmentDao.countModelsForSegment(segment);
    }

    @Override
    @Transactional(readOnly = true)
    public Long countExcludedModelsForSegment(Segment segment) {
        return segmentDao.countExcludedModelsForSegment(segment);
    }

    @Override
    @Transactional(readOnly = true)
    public Long countOperatorsForSegment(Segment segment) {
        return segmentDao.countOperatorsForSegment(segment);
    }

    @Override
    @Transactional(readOnly = true)
    public Long countPlatformsForSegment(Segment segment) {
        return segmentDao.countPlatformsForSegment(segment);
    }

    @Override
    @Transactional(readOnly = true)
    public Long countPublishersForSegment(Segment segment) {
        return segmentDao.countPublishersForSegment(segment);
    }

    @Override
    @Transactional(readOnly = true)
    public Long countVendorsForSegment(Segment segment) {
        return segmentDao.countVendorsForSegment(segment);
    }
    
    //------------------------------------------------------------------------------------------
    // LocationTarget
    //------------------------------------------------------------------------------------------
    @Override
    @Transactional(readOnly=false)
    public LocationTarget newLocationTarget(Advertiser advertiser, String name, BigDecimal latitude, BigDecimal longitude, BigDecimal radiusMiles, FetchStrategy... fetchStrategy) {
        LocationTarget locationTarget = new LocationTarget(advertiser, name, latitude, longitude, radiusMiles);
        if(fetchStrategy == null || fetchStrategy.length == 0) {
            return create(locationTarget);
        } else {
            locationTarget = create(locationTarget);
            return getLocationTargetById(locationTarget.getId(), fetchStrategy);
        }
    }

    @Override
    @Transactional(readOnly=false)
    public LocationTarget copyLocationTarget(LocationTarget source) {
        return this.newLocationTarget(
                source.getAdvertiser(),
                source.getName(),
                source.getLatitude(),
                source.getLongitude(),
                source.getRadiusMiles());
    }

    @Override
    @Transactional(readOnly=true)
    public LocationTarget getLocationTargetById(String id, FetchStrategy... fetchStrategy) {
        return getLocationTargetById(makeLong(id), fetchStrategy);
    }

    @Override
    @Transactional(readOnly=true)
    public LocationTarget getLocationTargetById(Long id, FetchStrategy... fetchStrategy) {
        return locationTargetDao.getById(id, fetchStrategy);
    }
    
    @Override
    @Transactional(readOnly=true)
    public List<LocationTarget> getLocationTargetByIds(List<Long> ids, FetchStrategy... fetchStrategy) {
        return locationTargetDao.getObjectsByIds(ids, fetchStrategy);
    }

    @Transactional(readOnly=false)
    public LocationTarget create(LocationTarget publicationList) {
        return locationTargetDao.create(publicationList);
    }

    @Override
    @Transactional(readOnly=false)
    public LocationTarget update(LocationTarget publicationList) {
        return locationTargetDao.update(publicationList);
    }

    @Override
    @Transactional(readOnly=false)
    public void delete(LocationTarget publicationList) {
        locationTargetDao.delete(publicationList);
    }

    @Override
    @Transactional(readOnly=false)
    public void deleteLocationTargets(List<LocationTarget> list) {
        if(list == null || list.isEmpty()) {
            return;
        }
        for(LocationTarget entry : list) {
            delete(entry);
        }
    }

    //------------------------------------------------------------------------------------------

    @Override
    @Transactional(readOnly=true)
    public Long countAllLocationTargets(LocationTargetFilter filter) {
        return locationTargetDao.countAll(filter);
    }

    @Override
    @Transactional(readOnly=true)
    public List<LocationTarget> getAllLocationTargets(LocationTargetFilter filter, FetchStrategy ... fetchStrategy) {
        return locationTargetDao.getAll(filter, fetchStrategy);
    }

    @Override
    @Transactional(readOnly=true)
    public List<LocationTarget> getAllLocationTargets(LocationTargetFilter filter, Sorting sort, FetchStrategy ... fetchStrategy) {
        return locationTargetDao.getAll(filter, sort, fetchStrategy);
    }

    @Override
    @Transactional(readOnly=true)
    public List<LocationTarget> getAllLocationTargets(LocationTargetFilter filter, Pagination page, FetchStrategy ... fetchStrategy) {
        return locationTargetDao.getAll(filter, page, fetchStrategy);
    }

    //------------------------------------------------------------------------------------------

    @Override
    public Long countAllLocationTargetsForAdvertiser(Advertiser advertiser) {
        return countAllLocationTargets(new LocationTargetFilter().setAdvertiser(advertiser));
    }

    @Override
    @Transactional(readOnly=true)
    public List<LocationTarget> getAllLocationTargetsForAdvertiser(Advertiser advertiser, FetchStrategy ... fetchStrategy) {
        return getAllLocationTargets(new LocationTargetFilter().setAdvertiser(advertiser), fetchStrategy);
    }

    @Override
    @Transactional(readOnly=true)
    public List<LocationTarget> getAllLocationTargetsForAdvertiser(Advertiser advertiser, Sorting sort, FetchStrategy ... fetchStrategy) {
        return locationTargetDao.getAll(new LocationTargetFilter().setAdvertiser(advertiser), sort, fetchStrategy);
    }

    @Override
    @Transactional(readOnly=true)
    public List<LocationTarget> getAllLocationTargetsForAdvertiser(Advertiser advertiser, Pagination page, FetchStrategy ... fetchStrategy) {
        return locationTargetDao.getAll(new LocationTargetFilter().setAdvertiser(advertiser), page, fetchStrategy);
    }

    //------------------------------------------------------------------------------------------

    @Override
    public Long countAllLocationTargetsForAdvertiserAndPartialName(Advertiser advertiser, String name, LikeSpec likeSpec, boolean caseSensitive) {
        return countAllLocationTargets(
                new LocationTargetFilter()
                .setAdvertiser(advertiser)
                .setName(name, likeSpec, caseSensitive)
            );
    }

    @Override
    @Transactional(readOnly=true)
    public List<LocationTarget> getAllLocationTargetsForAdvertiserAndPartialName(Advertiser advertiser, String name, LikeSpec likeSpec, boolean caseSensitive, FetchStrategy ... fetchStrategy) {
        return getAllLocationTargets(
                new LocationTargetFilter()
                .setAdvertiser(advertiser)
                .setName(name, likeSpec, caseSensitive),
                fetchStrategy);
    }

    @Override
    @Transactional(readOnly=true)
    public List<LocationTarget> getAllLocationTargetsForAdvertiserAndPartialName(Advertiser advertiser, String name, LikeSpec likeSpec, boolean caseSensitive, Sorting sort, FetchStrategy ... fetchStrategy) {
        return locationTargetDao.getAll(
                new LocationTargetFilter()
                .setAdvertiser(advertiser)
                .setName(name, likeSpec, caseSensitive),
                sort,
                fetchStrategy);
    }

    @Override
    @Transactional(readOnly=true)
    public List<LocationTarget> getAllLocationTargetsForAdvertiserAndPartialName(Advertiser advertiser, String name, LikeSpec likeSpec, boolean caseSensitive, Pagination page, FetchStrategy ... fetchStrategy) {
        return locationTargetDao.getAll(
                new LocationTargetFilter()
                .setAdvertiser(advertiser)
                .setName(name, likeSpec, caseSensitive),
                page,
                fetchStrategy);
    }


    //------------------------------------------------------------------------------------------
    // CampaignTimePeriod
    //------------------------------------------------------------------------------------------
    @Override
    @Transactional(readOnly=false)
    public CampaignTimePeriod newCampaignTimePeriod(Campaign campaign, FetchStrategy... fetchStrategy) {
        return newCampaignTimePeriod(campaign, null, null, fetchStrategy);
    }

    @Override
    @Transactional(readOnly=false)
    public CampaignTimePeriod newCampaignTimePeriod(Campaign campaign, Date startDate, Date endDate, FetchStrategy... fetchStrategy) {
        CampaignTimePeriod ctp = new CampaignTimePeriod(campaign, startDate, endDate);
        if(fetchStrategy == null || fetchStrategy.length == 0) {
            return create(ctp);
        } else {
            ctp = create(ctp);
            return getCampaignTimePeriodById(ctp.getId(), fetchStrategy);
        }
    }

    @Override
    @Transactional(readOnly=true)
    public boolean isTimePeriodOverlapping(Campaign campaign, CampaignTimePeriod timePeriod) {
        // Load the current time periods through a query, so we dont need them set in the campaign
        List<CampaignTimePeriod> currentTimePeriods = this.getAllCampaignTimePeriodsForCampaign(campaign);
        for (CampaignTimePeriod existing : currentTimePeriods) {
            if (timePeriod.overlaps(existing)) {
                return true;
            }
        }
        return false;
    }

    @Override
    @Transactional(readOnly=false)
    public Campaign addTimePeriodToCampaign(Campaign campaign, CampaignTimePeriod timePeriod) {
        Set<CampaignTimePeriod> timePeriods = new HashSet<CampaignTimePeriod>();
        timePeriods.add(timePeriod);
        return this.addTimePeriodsToCampaign(campaign, timePeriods);
     }

    @Override
    @SuppressWarnings("unchecked")
    @Transactional(readOnly=false)
    public Campaign addTimePeriodsToCampaign(Campaign campaign, Set<CampaignTimePeriod> timePeriods) {
        // Load the current time periods through a query, so we dont need them set in the campaign
        List<CampaignTimePeriod> currentTimePeriods = this.getAllCampaignTimePeriodsForCampaign(campaign);

        for(CampaignTimePeriod timePeriod : timePeriods) {
            if (campaign != timePeriod.getCampaign()) {
                throw new IllegalArgumentException("Can't add a time period belonging to another campaign");
            } else if (timePeriod.getStartDate() != null &&
                     timePeriod.getEndDate() != null &&
                     !timePeriod.getStartDate().before(timePeriod.getEndDate())) {
                throw new IllegalArgumentException("Invalid date range...start date must be prior to end date");
            }

            // Make sure it's not a duplicate
            if (currentTimePeriods.contains(timePeriod)) {
                throw new IllegalArgumentException("Can't add duplicate time period");
            }

            // Make sure it doesn't overlap any one of the existing time periods
            for (CampaignTimePeriod existing : currentTimePeriods) {
                if (timePeriod.overlaps(existing)) {
                    throw new IllegalArgumentException("Can't add time period, overlaps: " + existing);
                }
            }

            // Sane...ok to add it - but dont set it in the campaign object, that was JDO's way. Here, just persist
            // and add to the local list
            if(!isPersisted(timePeriod)) {
                // Persist it first
                timePeriod = newCampaignTimePeriod(campaign, timePeriod.getStartDate(), timePeriod.getEndDate());
            }
            currentTimePeriods.add(timePeriod);
        }


        // Update the "boundary" startDate/endDate by sorting all of the
        // time periods and taking the earliest startDate and latest endDate
        Collections.sort(currentTimePeriods);

        if (currentTimePeriods.isEmpty()) {
            campaign.setStartDate(null);
            campaign.setEndDate(null);
        }else {
            CampaignTimePeriod first = currentTimePeriods.get(0);
            if (first.getStartDate() == null) {
                // The earliest one is unbounded...
                campaign.setStartDate(null);
            }else {
                campaign.setStartDate((Date)first.getStartDate().clone());
            }

            CampaignTimePeriod last = currentTimePeriods.get(currentTimePeriods.size() - 1);
            if (last.getEndDate() == null) {
                campaign.setEndDate(null);
            }else {
                campaign.setEndDate((Date)last.getEndDate().clone());
            }
        }

        // Invalidate the current time period so it will need to be
        // re-determined if somebody calls getCurrentTimePeriod.
        // Technically we should probably be using AtomicReference for
        // these changes, since threads could step on each other by calling
        // newTimePeriod and getCurrentTimePeriod concurrently...but since
        // we *know* that right now (at least when this was written), adserver
        // is read-only, and nothing else is calling getCurrentTimePeriod,
        // we can be lazy for the time being.
        campaign.setCurrentTimePeriod(null);
        campaign.setNextTimePeriod(null);
        CampaignManager campaignManager = AdfonicBeanDispatcher.getBean(CampaignManager.class);
        return campaignManager.update(campaign);
     }

    @Override
    @Transactional(readOnly=true)
    public CampaignTimePeriod getCampaignTimePeriodById(String id, FetchStrategy... fetchStrategy) {
        return getCampaignTimePeriodById(makeLong(id), fetchStrategy);
    }

    @Override
    @Transactional(readOnly=true)
    public CampaignTimePeriod getCampaignTimePeriodById(Long id, FetchStrategy... fetchStrategy) {
        return campaignTimePeriodDao.getById(id, fetchStrategy);
    }

    @Override
    @Transactional(readOnly=false)
    public CampaignTimePeriod create(CampaignTimePeriod campaignTimePeriod) {
        return campaignTimePeriodDao.create(campaignTimePeriod);
    }

    @Override
    @Transactional(readOnly=false)
    public CampaignTimePeriod update(CampaignTimePeriod campaignTimePeriod) {
        return campaignTimePeriodDao.update(campaignTimePeriod);
    }

    @Override
    @Transactional(readOnly=false)
    public void delete(CampaignTimePeriod campaignTimePeriod) {
        campaignTimePeriodDao.delete(campaignTimePeriod);
    }

    @Override
    @Transactional(readOnly=false)
    public void deleteCampaignTimePeriods(List<CampaignTimePeriod> list) {
        if(list == null || list.isEmpty()) {
            return;
        }
        for(CampaignTimePeriod entry : list) {
            delete(entry);
        }
    }

    @Override
    @Transactional(readOnly=true)
    public Long countAllCampaignTimePeriodsForCampaign(Campaign campaign) {
        return campaignTimePeriodDao.countAllForCampaign(campaign);
    }

    @Override
    @Transactional(readOnly=true)
    public List<CampaignTimePeriod> getAllCampaignTimePeriodsForCampaign(Campaign campaign, FetchStrategy... fetchStrategy) {
        return campaignTimePeriodDao.getAllForCampaign(campaign, fetchStrategy);
    }

    @Override
    @Transactional(readOnly=true)
    public List<CampaignTimePeriod> getAllCampaignTimePeriodsForCampaign(Campaign campaign, Sorting sort, FetchStrategy... fetchStrategy) {
        return campaignTimePeriodDao.getAllForCampaign(campaign, sort, fetchStrategy);
    }

    @Override
    @Transactional(readOnly=true)
    public List<CampaignTimePeriod> getAllCampaignTimePeriodsForCampaign(Campaign campaign, Pagination page, FetchStrategy... fetchStrategy) {
        return campaignTimePeriodDao.getAllForCampaign(campaign, page, fetchStrategy);
    }
    
    // ------------------------------------------------------------------------------------------
    // Geotarget
    // ------------------------------------------------------------------------------------------
    @Override
    @Transactional(readOnly = false)
    public Geotarget newGeotarget(String name, Country country, GeotargetType geotargetType, double displayLatitude, double displayLongitude, FetchStrategy... fetchStrategy) {
        Geotarget geotarget = new Geotarget(name, country, geotargetType, displayLatitude, displayLongitude);
        if (fetchStrategy == null || fetchStrategy.length == 0) {
            return create(geotarget);
        } else {
            geotarget = create(geotarget);
            return getGeotargetById(geotarget.getId(), fetchStrategy);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<GeotargetList> getGeotargetListByGeotargetBy(long id, FetchStrategy fetchStrategy) {
        return geotargetListDao.getGeotargetListByGeotargetBy(id, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public Geotarget getGeotargetById(String id, FetchStrategy... fetchStrategy) {
        return getGeotargetById(makeLong(id), fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public Geotarget getGeotargetById(Long id, FetchStrategy... fetchStrategy) {
        return geotargetDao.getById(id, fetchStrategy);
    }

    @Transactional(readOnly = false)
    public Geotarget create(Geotarget geotarget) {
        return geotargetDao.create(geotarget);
    }

    @Override
    @Transactional(readOnly = false)
    public Geotarget update(Geotarget geotarget) {
        return geotargetDao.update(geotarget);
    }

    @Override
    @Transactional(readOnly = false)
    public void delete(Geotarget geotarget) {
        geotargetDao.delete(geotarget);
    }

    @Override
    @Transactional(readOnly = false)
    public void deleteGeotargets(List<Geotarget> list) {
        if (list == null || list.isEmpty()) {
            return;
        }
        for (Geotarget entry : list) {
            delete(entry);
        }
    }

    // ------------------------------------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public Geotarget getGeotargetByName(String name, FetchStrategy... fetchStrategy) {
        return geotargetDao.getByName(name, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Geotarget> getAllGeotargets(FetchStrategy... fetchStrategy) {
        return geotargetDao.getAll(fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public Long countAllGeotargets(GeotargetFilter filter) {
        return geotargetDao.countAll(filter);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Geotarget> getAllGeotargets(GeotargetFilter filter, Sorting sort, FetchStrategy... fetchStrategy) {
        return geotargetDao.getAll(filter, sort, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Geotarget> getAllGeotargets(GeotargetFilter filter, Pagination page, FetchStrategy... fetchStrategy) {
        return geotargetDao.getAll(filter, page, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public Long countGeotargetsByNameAndTypeAndIsoCode(String isoCode, GeotargetType geotargetType, String name, boolean caseSensitive, LikeSpec like) {
        return geotargetDao.countGeotargetsByNameAndTypeAndIsoCode(isoCode, geotargetType, name, caseSensitive, like);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Geotarget> getGeotargetsByNameAndTypeAndIsoCode(String isoCode, GeotargetType geotargetType, String name, boolean caseSensitive, LikeSpec like,
            FetchStrategy... fetchStrategy) {
        return geotargetDao.getGeotargetsByNameAndTypeAndIsoCode(isoCode, geotargetType, name, caseSensitive, like, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Geotarget> getGeotargetsByNameAndTypeAndIsoCode(String isoCode, GeotargetType geotargetType, String name, boolean caseSensitive, LikeSpec like, Sorting sort,
            FetchStrategy... fetchStrategy) {
        return geotargetDao.getGeotargetsByNameAndTypeAndIsoCode(isoCode, geotargetType, name, caseSensitive, like, sort, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Geotarget> getGeotargetsByNameAndTypeAndIsoCode(String isoCode, GeotargetType geotargetType, String name, boolean caseSensitive, LikeSpec like, Pagination page,
            FetchStrategy... fetchStrategy) {
        return geotargetDao.getGeotargetsByNameAndTypeAndIsoCode(isoCode, geotargetType, name, caseSensitive, like, page, fetchStrategy);
    }

    // ------------------------------------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public Long countGeotargetTypesForCountryIsoCode(String isoCode) {
        return geotargetDao.countGeotargetTypesForCountryIsoCode(isoCode);
    }

    @Override
    @Transactional(readOnly = true)
    public List<GeotargetType> getGeotargetTypesForCountryIsoCode(String isoCode) {
        return geotargetDao.getGeotargetTypesForCountryIsoCode(isoCode);
    }

    @Override
    @Transactional(readOnly = true)
    public List<GeotargetType> getGeotargetTypesForCountryIsoCode(String isoCode, Sorting sort) {
        return geotargetDao.getGeotargetTypesForCountryIsoCode(isoCode, sort);
    }

    @Override
    @Transactional(readOnly = true)
    public List<GeotargetType> getGeotargetTypesForCountryIsoCode(String isoCode, Pagination page) {
        return geotargetDao.getGeotargetTypesForCountryIsoCode(isoCode, page);
    }

    // ------------------------------------------------------------------------------------------
    // GeotargetType
    // ------------------------------------------------------------------------------------------
    @Override
    @Transactional(readOnly = true)
    public GeotargetType getGeotargetTypeById(Long id, FetchStrategy... fetchStrategy) {
        return geotargetTypeDao.getById(id, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public GeotargetType getGeotargetTypeById(String id, FetchStrategy... fetchStrategy) {
        return geotargetTypeDao.getById(makeLong(id), fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public Long countAllGeotargetTypes() {
        return geotargetTypeDao.countAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<GeotargetType> getAllGeotargetTypes(FetchStrategy... fetchStrategy) {
        return geotargetTypeDao.getAll(fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public List<GeotargetType> getAllGeotargetTypes(Sorting sort, FetchStrategy... fetchStrategy) {
        return geotargetTypeDao.getAll(sort, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public List<GeotargetType> getAllGeotargetTypes(Pagination page, FetchStrategy... fetchStrategy) {
        return geotargetTypeDao.getAll(page, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public GeotargetType getGeotargetTypeByNameAndType(String name, String type) {
        return geotargetTypeDao.getByNameAndType(name, type);
    }

    @Override
    @Transactional(readOnly = true)
    public Long countAllGeotargetTypesForCountryIsoCode(String isoCode) {
        return geotargetTypeDao.countAllForCountryIsoCode(isoCode, null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<GeotargetType> getAllGeotargetTypesForCountryIsoCode(String isoCode, FetchStrategy... fetchStrategy) {
        return geotargetTypeDao.getAllForCountryIsoCode(isoCode, null, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public List<GeotargetType> getAllGeotargetTypesForCountryIsoCode(String isoCode, Sorting sort, FetchStrategy... fetchStrategy) {
        return geotargetTypeDao.getAllForCountryIsoCode(isoCode, null, sort, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public List<GeotargetType> getAllGeotargetTypesForCountryIsoCode(String isoCode, Pagination page, FetchStrategy... fetchStrategy) {
        return geotargetTypeDao.getAllForCountryIsoCode(isoCode, null, page, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public Long countAllGeotargetTypesForCountryIsoCode(String isoCode, Boolean isRadiusType) {
        return geotargetTypeDao.countAllForCountryIsoCode(isoCode, isRadiusType);
    }

    @Override
    @Transactional(readOnly = true)
    public List<GeotargetType> getAllGeotargetTypesForCountryIsoCode(String isoCode, Boolean isRadiusType, FetchStrategy... fetchStrategy) {
        return geotargetTypeDao.getAllForCountryIsoCode(isoCode, isRadiusType, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public List<GeotargetType> getAllGeotargetTypesForCountryIsoCode(String isoCode, Boolean isRadiusType, Sorting sort, FetchStrategy... fetchStrategy) {
        return geotargetTypeDao.getAllForCountryIsoCode(isoCode, isRadiusType, sort, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public List<GeotargetType> getAllGeotargetTypesForCountryIsoCode(String isoCode, Boolean isRadiusType, Pagination page, FetchStrategy... fetchStrategy) {
        return geotargetTypeDao.getAllForCountryIsoCode(isoCode, isRadiusType, page, fetchStrategy);
    }

}
