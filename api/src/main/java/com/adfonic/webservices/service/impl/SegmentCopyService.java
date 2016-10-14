package com.adfonic.webservices.service.impl;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.adfonic.domain.Campaign;
import com.adfonic.domain.Category;
import com.adfonic.domain.Channel;
import com.adfonic.domain.Country;
import com.adfonic.domain.Geotarget;
import com.adfonic.domain.Geotarget_;
import com.adfonic.domain.Model;
import com.adfonic.domain.Operator;
import com.adfonic.domain.Platform;
import com.adfonic.domain.Publisher;
import com.adfonic.domain.Segment;
import com.adfonic.domain.Vendor;
import com.adfonic.webservices.ErrorCode;
import com.adfonic.webservices.dto.GeoTargetDTO;
import com.adfonic.webservices.dto.GeoTargetDTO.Type;
import com.adfonic.webservices.dto.SegmentDTO;
import com.adfonic.webservices.dto.mapping.CategoryConverter;
import com.adfonic.webservices.dto.mapping.ChannelConverter;
import com.adfonic.webservices.dto.mapping.CountryConverter;
import com.adfonic.webservices.dto.mapping.InventoryTargetedCategoryConverter;
import com.adfonic.webservices.dto.mapping.ModelConverter;
import com.adfonic.webservices.dto.mapping.OperatorConverter;
import com.adfonic.webservices.dto.mapping.PlatformConverter;
import com.adfonic.webservices.dto.mapping.ReferenceSetCopier;
import com.adfonic.webservices.dto.mapping.TargetPublisherConverter;
import com.adfonic.webservices.dto.mapping.VendorConverter;
import com.adfonic.webservices.exception.ServiceException;
import com.adfonic.webservices.exception.ValidationException;
import com.adfonic.webservices.service.IRestrictingCopyService;
import com.adfonic.webservices.service.ISegmentCopyService;
import com.adfonic.webservices.service.IUtilService;
import com.adfonic.webservices.util.DspAccess;
import com.byyd.middleware.account.service.CompanyManager;
import com.byyd.middleware.campaign.filter.GeotargetFilter;
import com.byyd.middleware.campaign.service.TargetingManager;
import com.byyd.middleware.iface.dao.FetchStrategy;
import com.byyd.middleware.iface.dao.Pagination;
import com.byyd.middleware.iface.dao.SortOrder;
import com.byyd.middleware.iface.dao.Sorting;
import com.byyd.middleware.iface.dao.jpa.FetchStrategyBuilder;

/*
 * stopgap kind of class; formatting intended
 */
@Service
public class SegmentCopyService implements ISegmentCopyService{
    private static final FetchStrategy GEOTARGET_FETCH_STRATEGY = new FetchStrategyBuilder()
        .addInner(Geotarget_.country)
        .build();

    @Autowired
    private IRestrictingCopyService<SegmentDTO, Segment> copyService;

    
    @Autowired
    private TargetingManager targetingManager;

    @Autowired
    private IUtilService utilService;

    ReferenceSetCopier<Country> countryCopier = new ReferenceSetCopier<Country>(new CountryConverter());
    ReferenceSetCopier<Operator> operatorCopier = new ReferenceSetCopier<Operator>(new OperatorConverter());
    ReferenceSetCopier<Vendor> vendorCopier = new ReferenceSetCopier<Vendor>(new VendorConverter());
    ReferenceSetCopier<Model> modelCopier = new ReferenceSetCopier<Model>(new ModelConverter());
    ReferenceSetCopier<Platform> platformCopier = new ReferenceSetCopier<Platform>(new PlatformConverter());
    ReferenceSetCopier<Category> categoryCopier = new ReferenceSetCopier<Category>(new CategoryConverter());
    ReferenceSetCopier<Channel> channelCopier = new ReferenceSetCopier<Channel>(new ChannelConverter());
    ReferenceSetCopier<Category> includedCategoryCopier = new ReferenceSetCopier<>(new InventoryTargetedCategoryConverter());

    public void copyToSegment(SegmentDTO segmentDTO, Segment segment, Campaign.Status campaignStatus) {

        copyOperatorsOrIpAddressesToSegment(segmentDTO, segment);

        copyPlatformsOrModelsNvendorsToSegment(segmentDTO, segment);

        copyCountriesOrGeotargetsToSegment(segmentDTO, segment);
        
        // inventory Targeting Part of Segments
        copyTargetPublishersOrIncludedCategories(segmentDTO, segment);

        modelCopier.copy(segmentDTO.getExcludedModels(), segment.getExcludedModels());
        //channelCopier.copy(segmentDTO.getChannels(), segment.getChannels());

        segmentDTO.nullizeCollectionProperties();
        copyService.restrictOnCampaignStatus(campaignStatus).copyToDomain(segmentDTO, segment);

        Integer daysOfWeek, hoursOfDay, hoursOfDayWeekend;
        daysOfWeek = segmentDTO.getDaysOfWeek();
        hoursOfDay = segmentDTO.getHoursOfDay();
        hoursOfDayWeekend = segmentDTO.getHoursOfDayWeekend();

        if (daysOfWeek != null) {
            segment.setDaysOfWeekAsArray(getBooleanArrayFromBitmask(daysOfWeek, 7));
        }

        if (hoursOfDay != null) {
            segment.setHoursOfDayAsArray(getBooleanArrayFromBitmask(hoursOfDay, 24));
        }

        if (hoursOfDayWeekend != null) {
            segment.setHoursOfDayWeekendAsArray(getBooleanArrayFromBitmask(hoursOfDayWeekend, 24));
        }

    }


    private boolean[] getBooleanArrayFromBitmask(int bitMask, int noOfBits) {
        boolean[] target = new boolean[noOfBits];
        for (int i = 0; i < noOfBits; i++) {
            target[i] = 1 == (1 & bitMask);
            bitMask >>= 1;
        }
        return (target);
    }


    private void copyPlatformsOrModelsNvendorsToSegment(SegmentDTO segmentDTO, Segment segment) {
        Set<String> modelStrs = segmentDTO.getModels(), platformStrs = segmentDTO.getPlatforms(), vendorStrs=segmentDTO.getVendors();
        if (modelStrs != null || vendorStrs != null) {
            if (platformStrs != null) {
                throw new ValidationException("Will not simultaneously target Platforms along with Models or Vendors!");
            }

            if (modelStrs != null) {
                modelCopier.copy(modelStrs, segment.getModels());
            }

            if (vendorStrs != null) {
                vendorCopier.copy(segmentDTO.getVendors(), segment.getVendors());

                Set<Model> models = segment.getModels();

                for (Vendor vendor : segment.getVendors()) {// normalize just like UI so as to avoid problems for latter ..and consistency in serving
                    models.removeAll(vendor.getModels());
                }
            }

            clear(segment.getPlatforms());

        } else if (platformStrs != null) {
            platformCopier.copy(platformStrs, segment.getPlatforms());
            clear(segment.getModels());
            clear(segment.getVendors());
        }
    }


    private void copyCountriesOrGeotargetsToSegment(SegmentDTO segmentDTO, Segment segment) {
        Set<String> countryStrs = segmentDTO.getCountries();
        Set<GeoTargetDTO> geotargetDTOs = segmentDTO.getGeotargets();

        if (countryStrs != null) {
            if (geotargetDTOs != null) {
                throw new ValidationException("Cannot target based on country and geotarget simultaneously!");
            }

            countryCopier.copy(countryStrs, segment.getCountries());
            clear(segment.getGeotargets());
            segment.setGeotargetType(null);

        } else if (geotargetDTOs != null) {
            Set<Geotarget> segmentGeotargets = segment.getGeotargets();
            clear(segmentGeotargets);
            for (GeoTargetDTO geo : geotargetDTOs) {
                Set<Geotarget> geotargets = lookupGeotargets(geo.getCountry(), geo.getType(), geo.getName());
                if (geotargets == null || geotargets.size() != 1) {
                    throw new ServiceException(ErrorCode.GENERAL, "Geotarget invalid!");
                }
                segmentGeotargets.add(geotargets.toArray(new Geotarget[1])[0]);
            }
            if(!segmentGeotargets.isEmpty()){
                segment.setGeotargetType(segmentGeotargets.iterator().next().getGeotargetType());
            }
            clear(segment.getCountries());
        }

    }

    private Set<Geotarget> lookupGeotargets(String isoCode, Type type, String... names) {
        GeotargetFilter filter = new GeotargetFilter();
        filter.setCountryIsoCode(isoCode);
        String gtType=type.name();
        //TODO GT filter.setType(type);
        if (names != null) {
            Set<String> nameSet = new HashSet<String>();
            for (String name : names) {
                nameSet.add(name);
            }
            filter.setNames(nameSet, false); // case-insensitive
        }
        List<Geotarget> geotargetList = targetingManager.getAllGeotargets(filter, new Pagination(0, 50, new Sorting(SortOrder.asc("name"))), GEOTARGET_FETCH_STRATEGY);
        Set<Geotarget> geotargets=new HashSet<>();
        for(Geotarget geotarget: geotargetList){
            if(!geotargets.contains(geotarget) && geotarget.getGeotargetType().getType().equals(gtType)){
                geotargets.add(geotarget);
            }
        }
        return geotargets;
    }

    private void copyOperatorsOrIpAddressesToSegment(SegmentDTO segmentDTO, Segment segment) {
        Set<String> ipAddrStrs = segmentDTO.getIpAddresses(), ipAddrs, operatorStrs = segmentDTO.getOperators();

        if (operatorStrs != null) {
            if (ipAddrStrs != null) {
                throw new ValidationException("Cannot target operators and ip addresses simultaneously!");
            }

            operatorCopier.copy(operatorStrs, segment.getOperators());
            clear(segment.getIpAddresses());

        } else if (ipAddrStrs != null) {
            segment.setIpAddressesListWhitelist(segmentDTO.isIpAddressesWhitelist());
            ipAddrs = segment.getIpAddresses();
            clear(ipAddrs);
            ipAddrs.addAll(ipAddrStrs);

            clear(segment.getOperators());
        }

    }

    @Autowired
    private CompanyManager companyManager;

    private void copyTargetPublishersOrIncludedCategories(SegmentDTO segmentDTO, Segment segment) {
        Set<String> targetPublisherStrs = segmentDTO.getTargetedPublishers(), includedCategoryStrs = segmentDTO.getIncludedCategories();

        if (targetPublisherStrs != null) {
            DspAccess dspAccess = utilService.getEffectiveDspAccess(segment.getAdvertiser().getCompany());
            // will never be null under changed circumstances - remove in next round
            if (dspAccess == null) {
                throw new ServiceException(ErrorCode.AUTH_NO_AUTHORIZATION, "cannot authorize non DSP!");
            }

            if (includedCategoryStrs != null) {
                throw new ValidationException("Inventory targeting: cannot target categories and publishers simultaneously!");
            }

            ReferenceSetCopier<Publisher> targetPublisherCopier = new ReferenceSetCopier<>(new TargetPublisherConverter(true));
            targetPublisherCopier.copy(targetPublisherStrs, segment.getTargettedPublishers());
            clear(segment.getIncludedCategories());

        } else if (includedCategoryStrs != null) {
            includedCategoryCopier.copy(includedCategoryStrs, segment.getIncludedCategories());
            clear(segment.getTargettedPublishers());
        }
    }


    // Needed because JDO proxy Set's clear() implementation seems to have a bug
    // TODO: do we still need this?
    private void clear(Set<?> targets) {
        targets.removeAll(targets);
    }

}
