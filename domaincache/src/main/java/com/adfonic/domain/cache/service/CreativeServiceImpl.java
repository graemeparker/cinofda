package com.adfonic.domain.cache.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.adfonic.domain.cache.dto.adserver.CountryDto;
import com.adfonic.domain.cache.dto.adserver.LocationTargetDto;
import com.adfonic.domain.cache.dto.adserver.creative.AdspaceWeightedCreative;
import com.adfonic.domain.cache.dto.adserver.creative.CreativeDto;
import com.adfonic.domain.cache.dto.adserver.creative.PluginCreativeInfo;
import com.adfonic.domain.cache.dto.adserver.creative.SegmentDto;
import com.adfonic.domain.cache.ext.TransientDataExt;
import com.adfonic.geo.GeoUtils;
import com.adfonic.util.Subnet;

public class CreativeServiceImpl implements CreativeService {
    private static final long serialVersionUID = 2L;

    public CreativeServiceImpl() {

    }

    public CreativeServiceImpl(CreativeServiceImpl copy) {
        this.creativeCache.putAll(copy.creativeCache);
        this.creativeExternalIdToIdCache.putAll(copy.creativeExternalIdToIdCache);
        this.allCreative = Arrays.copyOf(copy.allCreative, copy.allCreative.length);
        this.pluginCreatives.addAll(copy.pluginCreatives);
        this.allPluginCreative = Arrays.copyOf(copy.allPluginCreative, copy.allPluginCreative.length);
        this.pluginCreativeInfoByCreativeId.putAll(copy.pluginCreativeInfoByCreativeId);
        this.adSpaceWeightedCreatives.putAll(copy.adSpaceWeightedCreatives);
        this.recentlyStoppedCreativesById.putAll(copy.recentlyStoppedCreativesById);
        this.subnetsBySegmentId.putAll(copy.subnetsBySegmentId);

    }

    final Map<Long, Set<Subnet>> subnetsBySegmentId = new HashMap<Long, Set<Subnet>>();

    //Holds creative id as key and creative as value
    private final Map<Long, CreativeDto> creativeCache = new HashMap<Long, CreativeDto>();

    //Holds creative externalId as key and creative Id as value
    private final Map<String, Long> creativeExternalIdToIdCache = new HashMap<String, Long>();

    private CreativeDto[] allCreative;

    final List<Long> pluginCreatives = new ArrayList<Long>();

    private CreativeDto[] allPluginCreative;

    final Map<Long, PluginCreativeInfo> pluginCreativeInfoByCreativeId = new HashMap<Long, PluginCreativeInfo>();

    final Map<Long, AdspaceWeightedCreative[]> adSpaceWeightedCreatives = new HashMap<Long, AdspaceWeightedCreative[]>();

    final Map<Long, CreativeDto> recentlyStoppedCreativesById = new ConcurrentHashMap<Long, CreativeDto>();

    final Map<Long, Set<Long>> countryEligibleCreativeIds = new HashMap<Long, Set<Long>>();

    private final AdspaceWeightedCreative[] emptyAdspaceWeightedCreative = new AdspaceWeightedCreative[0];

    private final Set<Long> emptyEligibleCreativeIdSet = Collections.EMPTY_SET;

    private final Long ANY_OR_UNKOWN_COUNTRY_ID = 0L;

    @Override
    public void afterDeserialize() {
        allCreative = creativeCache.values().toArray(new CreativeDto[creativeCache.size()]);
        for (CreativeDto creativeDto : allCreative) {
            SegmentDto segment = creativeDto.getSegment();
            if (segment != null) {
                for (LocationTargetDto locationTarget : segment.getLocationTargets()) {
                    double dLonM = GeoUtils.milesAtLatitudeToLongitudeDegrees(locationTarget.getRadius() * 1.1, locationTarget.getLatitude());
                    double dLatM = GeoUtils.milesToLatitudeDegrees(locationTarget.getRadius() * 1.1);
                    locationTarget.setFvENlatidute(locationTarget.getLatitude() - dLatM);
                    locationTarget.setFvENlongitude(locationTarget.getLongitude() - dLonM);
                    locationTarget.setFvWSlatidute(locationTarget.getLatitude() + dLatM);
                    locationTarget.setFvWSlongitude(locationTarget.getLongitude() + dLonM);
                }
            }
        }
        List<CreativeDto> pluginCreativesList = new ArrayList<CreativeDto>(pluginCreatives.size());
        CreativeDto creativeDto;
        for (Long oneCreativeId : pluginCreatives) {
            creativeDto = creativeCache.get(oneCreativeId);
            if (creativeDto != null) {
                pluginCreativesList.add(creativeDto);
            }
        }
        allPluginCreative = pluginCreativesList.toArray(new CreativeDto[pluginCreativesList.size()]);
    }

    @Override
    public void beforeSerialization() {
        allCreative = null;
        allPluginCreative = null;
        //Do more memory saving things here
        //Remove all creatives which are not eligible at all
        Set<Long> allCreativeIds = new HashSet<Long>(this.creativeCache.size());
        allCreativeIds.addAll(this.creativeCache.keySet());

        //Now remove all creative ids from this set which are eligible for atleast one adspace
        for (Entry<Long, AdspaceWeightedCreative[]> oneEntry : this.adSpaceWeightedCreatives.entrySet()) {
            for (AdspaceWeightedCreative oneAdspaceWeightedCreative : oneEntry.getValue())
                for (Long oneCreativeId : oneAdspaceWeightedCreative.getCreativeIds()) {
                    allCreativeIds.remove(oneCreativeId);
                }
        }
        //Now whatever left in allCreativeIds remove them from creativeCache and creativeExternalIdToIdCache
        CreativeDto creativeRemoved;
        for (Long oneCreativeId : allCreativeIds) {
            creativeRemoved = this.creativeCache.remove(oneCreativeId);
            this.creativeExternalIdToIdCache.remove(creativeRemoved.getExternalID());
        }
    }

    @Override
    public void addCreativeToCache(CreativeDto creative) {
        if (creative.isPluginBased()) {
            try {
                // Pre-parse the PluginCreativeInfo so we don't have to do
                // it on demand later...save one of N threads from having
                // to do it in parallel later
                pluginCreativeInfoByCreativeId.put(creative.getId(), new PluginCreativeInfo(creative));
            } catch (Exception e) {
                // If we let this creative in, then stuff will fail
                // later when somebody tries to grab the
                // PluginCreativeInfo on demand anyway, so exclude it...
                return;
            }
            pluginCreatives.add(creative.getId());
        }
        creativeCache.put(creative.getId(), creative);
        creativeExternalIdToIdCache.put(creative.getExternalID(), creative.getId());
    }

    @Override
    public CreativeDto getCreativeByExternalID(String externalID) {
        Long creativeId = creativeExternalIdToIdCache.get(externalID);
        return creativeCache.get(creativeId);
    }

    @Override
    public CreativeDto getCreativeById(Long creativeId) {
        return creativeCache.get(creativeId);
    }

    @Override
    public CreativeDto[] getAllCreatives() {
        if (allCreative == null) {
            //Not thread safe and we don't need it to be.
            //instead u call after deserialize before u start using this cache/function
            allCreative = creativeCache.values().toArray(new CreativeDto[creativeCache.size()]);
        }
        return allCreative;
    }

    @Override
    public void addAdSpaceEligibleCreative(Long adSpaceId, Set<AdspaceWeightedCreative> creativeList, List<CountryDto> allCountries) {
        //Replace actual Long objects in list with singleton(kind of) object of Long from transient cache
        //This make sure that each number represents by single object(of type Long)
        //e.g. earlier if one creative with 101 was eligible for 6000 adspaces then there were 10 Long objects
        //in the memory so total memory taken = 16(Size of Long) X 6000 + 8(Reference) X 6000
        //By doing this enahancement total memory taken will be 16(Size of Long) X 1 + 8(Reference) X 6000
        //So we save 5999 X 6000 bytes and thats only for one creative Ids
        Long adspaceKey = TransientDataExt.getSingltonId(adSpaceId);
        Long creativeKey;
        for (AdspaceWeightedCreative oneAdspaceWeightedCreative : creativeList) {
            for (int i = 0; i < oneAdspaceWeightedCreative.getCreativeIds().length; i++) {
                creativeKey = TransientDataExt.getSingltonId(oneAdspaceWeightedCreative.getCreativeIds()[i]);
                oneAdspaceWeightedCreative.getCreativeIds()[i] = creativeKey;
            }
        }
        AdspaceWeightedCreative[] creativeArray = creativeList.toArray(new AdspaceWeightedCreative[creativeList.size()]);
        synchronized (adSpaceWeightedCreatives) {
            adSpaceWeightedCreatives.put(adspaceKey, creativeArray);
        }
        createCountrySpecificMaps(allCountries, creativeArray);
    }

    static boolean checkSegmentCountries(CreativeDto creative, SegmentDto segment, Long countryId) {
        if (segment == null) {
            return true;
        }
        if (segment.getCountryIds().isEmpty()) {
            return true; // No country targeting
        } else if (segment.getCountryListIsWhitelist()) {
            // Whitelist: if countries is non-empty the country must be in it
            if (!segment.getCountryIds().contains(countryId)) {
                // CountryDto isn't supported
                return false;
            }
        }
        // Otherwise it's a blacklist
        else if (segment.getCountryIds().contains(countryId)) {
            // CountryDto isn't supported
            return false;
        }
        return true;
    }

    @Override
    public AdspaceWeightedCreative[] getEligibleCreatives(Long adSpaceId) {
        AdspaceWeightedCreative[] returnValue = adSpaceWeightedCreatives.get(adSpaceId);
        if (returnValue == null) {
            returnValue = emptyAdspaceWeightedCreative;
        }
        return returnValue;
    }

    @Override
    public Map<Long, AdspaceWeightedCreative[]> getAllEligibleCreatives() {
        return adSpaceWeightedCreatives;
    }

    @Override
    public CreativeDto[] getPluginCreatives() {
        return allPluginCreative;
    }

    @Override
    public PluginCreativeInfo getPluginCreativeInfo(CreativeDto creative) {
        return getPluginCreativeInfo(creative.getId());
    }

    @Override
    public PluginCreativeInfo getPluginCreativeInfo(Long creativeId) {
        return pluginCreativeInfoByCreativeId.get(creativeId);
    }

    @Override
    public void addRecentlyStoppedCreative(CreativeDto creative) {
        recentlyStoppedCreativesById.put(creative.getId(), creative);
    }

    @Override
    public CreativeDto getRecentlyStoppedCreativeById(Long id) {
        return recentlyStoppedCreativesById.get(id);
    }

    @Override
    public void stopCampaign(Long campaignID) {
        Long[] creativeIds;
        List<Long> creativeIdList;
        CreativeDto oneCreative;
        CreativeDto removedCreative;
        //go through all adspace weighted creatives
        //No Locking and synch required
        for (AdspaceWeightedCreative[] wcsByPriority : adSpaceWeightedCreatives.values()) {
            //for each adspace there will be list of(array of) AdspaceWeightedCreative
            //go through all of them
            for (AdspaceWeightedCreative oneAdspaceWeightedCreative : wcsByPriority) {
                //Each AdspaceWeightedCreative has list of creative Ids, go through them
                //and remove which are under the given campaign id
                creativeIds = oneAdspaceWeightedCreative.getCreativeIds();
                //Following list(creativeIdList) will have creativeIds which creative WILL NOT be stopped
                creativeIdList = new ArrayList<Long>();
                for (Long oneId : creativeIds) {
                    oneCreative = getCreativeById(oneId);
                    if (oneCreative != null) {
                        if (!creativeCache.get(oneId).getCampaign().getId().equals(campaignID)) {
                            creativeIdList.add(oneId);
                        } else {

                        }
                    }
                }
                //Update creativeIds for adSpace's particular weighted(as per priority) object
                oneAdspaceWeightedCreative.setCreativeIds(creativeIdList.toArray(new Long[creativeIdList.size()]));
            }
        }

        List<Long> creativesToRemoveFromCache = new ArrayList<Long>();
        for (Entry<Long, CreativeDto> oneEntry : creativeCache.entrySet()) {
            if (oneEntry.getValue().getCampaign().getId().equals(campaignID)) {
                //Add creative Id to another List to remove it from creativeCache later
                creativesToRemoveFromCache.add(oneEntry.getKey());
                removedCreative = oneEntry.getValue();

                //No lock and sychronization required as no one is iterating over this map
                //i.e. API is not available to iterate over it.
                creativeExternalIdToIdCache.remove(removedCreative.getExternalID());

                //No lock and sychronization required as no one is iterating over this map
                //i.e. API is not available to iterate over it.
                recentlyStoppedCreativesById.put(oneEntry.getKey(), removedCreative);

                //TODO : Do we need to remove these entries from pluginCreativeInfoByCreativeId
            }
        }
        for (Long oneCreativeId : creativesToRemoveFromCache) {
            //No lock and sychronization required as no one is iterating over this map
            //i.e. API is not available to iterate over it.
            creativeCache.remove(oneCreativeId);
        }
        //creative cache map has been updated make sure we update the array too.
        allCreative = creativeCache.values().toArray(new CreativeDto[creativeCache.size()]);
    }

    @Override
    public void stopAdvertiser(Long advertiserId) {
        Long[] creativeIds;
        List<Long> creativeIdList;
        CreativeDto oneCreative;
        CreativeDto removedCreative;
        //go through all adspace weighted creatives
        //No Locking and synch required
        for (AdspaceWeightedCreative[] wcsByPriority : adSpaceWeightedCreatives.values()) {
            //for each adspace there will be list of(array of) AdspaceWeightedCreative
            //go through all of them
            for (AdspaceWeightedCreative oneAdspaceWeightedCreative : wcsByPriority) {
                //Each AdspaceWeightedCreative has list of creative Ids, go through them
                //and remove which are under the given campaign id
                creativeIds = oneAdspaceWeightedCreative.getCreativeIds();
                //Following list(creativeIdList) will have creativeIds which creative WILL NOT be stopped
                creativeIdList = new ArrayList<Long>();
                for (Long oneId : creativeIds) {
                    oneCreative = creativeCache.get(oneId);
                    if (oneCreative != null) {
                        if (!creativeCache.get(oneId).getCampaign().getAdvertiser().getId().equals(advertiserId)) {
                            creativeIdList.add(oneId);
                        } else {
                            //No lock and sychronization required as no one is iterating over this map
                            //i.e. API is not available to iterate over it.
                            removedCreative = creativeCache.remove(oneId);

                            //No lock and sychronization required as no one is iterating over this map
                            //i.e. API is not available to iterate over it.
                            creativeExternalIdToIdCache.remove(removedCreative.getExternalID());

                            //No lock and sychronization required as no one is iterating over this map
                            //i.e. API is not available to iterate over it.
                            recentlyStoppedCreativesById.put(oneId, removedCreative);
                        }
                    }
                }
                //Update creativeIds for adSpace's particular weighted(as per priority) object
                oneAdspaceWeightedCreative.setCreativeIds(creativeIdList.toArray(new Long[creativeIdList.size()]));
            }
        }
        //creative cahce map has been updated make sure we update the array too.
        allCreative = creativeCache.values().toArray(new CreativeDto[creativeCache.size()]);
    }

    @Override
    public void addSegmentSubnets(Long segmentId, Set<Subnet> subnets) {
        synchronized (subnetsBySegmentId) {
            subnetsBySegmentId.put(segmentId, subnets);
        }
    }

    @Override
    public Set<Subnet> getSubnetsBySegmentId(Long segmentId) {
        return subnetsBySegmentId.get(segmentId);
    }

    @Override
    public void logCounts(String description, Logger logger, Level level) {
        if (logger.isLoggable(level)) {
            logger.log(level, "Total Creatives = " + this.creativeCache.size());
            logger.log(level, "Total Creatives(ExternalId) = " + this.creativeExternalIdToIdCache.size());
            if (this.allCreative != null) {
                logger.log(level, "Total Creatives(Array) = " + this.allCreative.length);
            } else {
                logger.log(level, "Total Creatives(Array) = 0");
            }
            logger.log(level, "Total Plugin Creatives = " + this.pluginCreatives.size());
            if (this.allPluginCreative != null) {
                logger.log(level, "Total Plugin Creatives(Array) = " + this.allPluginCreative.length);
            } else {
                logger.log(level, "Total Plugin Creatives(Array) = 0");
            }
            logger.log(level, "Total pluginCreativeInfoByCreativeId = " + this.pluginCreativeInfoByCreativeId.size());
            logger.log(level, "Total adSpaceWeightedCreatives = " + this.adSpaceWeightedCreatives.size());
            int totalEligibleCteaiveCount = 0;
            for (Entry<Long, AdspaceWeightedCreative[]> oneAdSpaceEntry : this.adSpaceWeightedCreatives.entrySet()) {
                for (AdspaceWeightedCreative oneAdspaceWeightedCreative : oneAdSpaceEntry.getValue()) {
                    totalEligibleCteaiveCount = totalEligibleCteaiveCount + oneAdspaceWeightedCreative.getCreativeIds().length;
                }

            }
            int totalCountryEligibleCteaiveCount = 0;
            for (Entry<Long, Set<Long>> oneCountryEntry : this.countryEligibleCreativeIds.entrySet()) {
                //logger.log(level,oneCountryEntry.getKey()+ " = "+ oneCountryEntry.getValue().size());
                totalCountryEligibleCteaiveCount = totalCountryEligibleCteaiveCount + oneCountryEntry.getValue().size();
            }
            logger.log(level, "         Total Eligible WeightedCreatives = " + totalEligibleCteaiveCount);
            logger.log(level, "         Total Eligible WeightedCreatives(Country) = " + totalCountryEligibleCteaiveCount);
            logger.log(level, "Total recentlyStoppedCreativesById = " + this.recentlyStoppedCreativesById.size());
            logger.log(level, "Total subnetsBySegmentId = " + this.subnetsBySegmentId.size());

        }
    }

    private void createCountrySpecificMaps(List<CountryDto> allCountries, AdspaceWeightedCreative[] allEligibleCreatives) {
        CreativeDto oneCreative;
        Set<Long> countryCreativeIds;
        Set<Long> existingCountryCreativeIds;
        Set<Long> anyCountryCreativeIds = new HashSet<Long>();
        //Go through all countries for which we want to do better performance
        for (CountryDto oneCountry : allCountries) {
            if (oneCountry.getId() == 0) {
                //For country = 0 we dont need to do the normal logic as we create this list manually using anyCountryCreativeIds and ANY_OR_UNKOWN_COUNTRY_ID
                continue;
            }
            countryCreativeIds = new HashSet<Long>(1000);

            for (AdspaceWeightedCreative oneAdspaceWeightedCreative : allEligibleCreatives) {
                for (Long oneCreativeId : oneAdspaceWeightedCreative.getCreativeIds()) {
                    oneCreative = getCreativeById(oneCreativeId);
                    //Logger.getLogger(this.getClass().getName()).warning("oneCreative="+oneCreative+", "+oneCreativeId+","+oneCreative.getSegment()+","+ oneCountry.getId());
                    //System.out.println("oneCreative="+oneCreative+", "+oneCreativeId);
                    if (checkSegmentCountries(oneCreative, oneCreative.getSegment(), oneCountry.getId())) {
                        countryCreativeIds.add(oneCreativeId);
                    }
                    if (oneCreative.getSegment() == null || oneCreative.getSegment().getCountryIds().isEmpty()) {
                        anyCountryCreativeIds.add(oneCreativeId);
                    }
                }
            }
            //If needed to do this in multithread env then sync following code
            if (!countryCreativeIds.isEmpty()) {
                synchronized (countryEligibleCreativeIds) {
                    existingCountryCreativeIds = countryEligibleCreativeIds.get(oneCountry.getId());
                    if (existingCountryCreativeIds == null) {
                        existingCountryCreativeIds = new HashSet<Long>(1000);
                        countryEligibleCreativeIds.put(oneCountry.getId(), existingCountryCreativeIds);
                    }
                }
                synchronized (existingCountryCreativeIds) {
                    existingCountryCreativeIds.addAll(countryCreativeIds);
                }
            }

        }
        //Now add those creative which can be served any where in the world
        existingCountryCreativeIds = countryEligibleCreativeIds.get(ANY_OR_UNKOWN_COUNTRY_ID);
        if (existingCountryCreativeIds == null) {
            countryEligibleCreativeIds.put(ANY_OR_UNKOWN_COUNTRY_ID, anyCountryCreativeIds);
        } else {
            synchronized (existingCountryCreativeIds) {
                existingCountryCreativeIds.addAll(anyCountryCreativeIds);
            }
        }
    }

    @Override
    public Set<Long> getEligibleCreativeIdsForCountry(Long countryId) {
        Set<Long> eligibleCreativeIds = countryEligibleCreativeIds.get(countryId);
        if (eligibleCreativeIds == null) {
            eligibleCreativeIds = emptyEligibleCreativeIdSet;
        }
        return eligibleCreativeIds;
    }
}
