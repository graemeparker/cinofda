package com.adfonic.data.cache.testUtils;

import com.adfonic.domain.BidType;
import com.adfonic.domain.CampaignBid.BidModelType;
import com.adfonic.domain.RtbConfig.RtbAuctionType;
import com.adfonic.domain.RtbConfig;
import com.adfonic.domain.cache.dto.SystemVariable;
import com.adfonic.domain.cache.dto.adserver.PlatformDto;
import com.adfonic.domain.cache.dto.adserver.adspace.AdSpaceDto;
import com.adfonic.domain.cache.dto.adserver.adspace.PublicationDto;
import com.adfonic.domain.cache.dto.adserver.adspace.PublisherDto;
import com.adfonic.domain.cache.dto.adserver.adspace.RtbConfigDto;
import com.adfonic.domain.cache.dto.adserver.creative.*;

import java.util.Random;

public class TestStubs {

    private static final Random RANDOM = new Random();

    public static int randomInteger() {
        return RANDOM.nextInt();
    }

    public static int randomInteger(int max) {
        return RANDOM.nextInt(max);
    }


    public static long randomLong() {
        return RANDOM.nextLong();
    }

    public static long randomLong(long max) {
        return Math.abs(RANDOM.nextLong() % max);
    }

    public static PlatformDto createPlatform() {
        PlatformDto platformDto = new PlatformDto();
        platformDto.setId(randomLong());
        platformDto.setName("name");
        platformDto.setSystemName("systemName");
        platformDto.setConstraints("constraints");

        return platformDto;
    }

    public static CreativeDto createCreative(CampaignDto campaign, long max_creatives) {

        return createCreativeWithId(randomLong(max_creatives), campaign);
    }


    public static CreativeDto createCreativeWithId(long id, CampaignDto campaign) {
        CreativeDto creative = new CreativeDto();
        creative.setId(id);
        creative.setCampaign(campaign);

        return creative;
    }


    public static AdvertiserDto createAdvertiser(CompanyDto company) {
        AdvertiserDto advertiser = new AdvertiserDto();
        advertiser.setId(randomLong());
        advertiser.setCompany(company);
        return advertiser;
    }

    public static PublicationDto createPublication(PublisherDto publisher) {
        PublicationDto publication = new PublicationDto();
        publication.setId(randomLong());
        publication.setPublisher(publisher);
        return publication;
    }

    public static AdSpaceDto createAdSpace(PublicationDto publication, long max_adspaces) {
        long id = randomLong(max_adspaces);
        return createAdSpaceWithId(id, publication);
    }

    public static AdSpaceDto createAdSpaceWithId(long id, PublicationDto publication) {
        AdSpaceDto adSpace = new AdSpaceDto();
        adSpace.setId(id);
        adSpace.setPublication(publication);

        return adSpace;
    }

    public static CampaignDto createCampaign(CampaignBidDto currentBid, AdvertiserDto advertiser) {
        CampaignDto campaign = new CampaignDto();
        campaign.setId(randomLong());
        campaign.setAdvertiser(advertiser);
        campaign.setCurrentBid(currentBid);
        campaign.setBoostFactor(1.0);
        return campaign;
    }

    public static PublisherDto createPublisher(double revShare, double buyerPremium, RtbConfigDto rtbConfig) {
        PublisherDto publisherDto = new PublisherDto();
        publisherDto.setId(randomLong());
        publisherDto.setCurrentRevShare(revShare);
        publisherDto.setRtbConfig(rtbConfig);
        publisherDto.setBuyerPremium(buyerPremium);

        return publisherDto;
    }

    public static RtbConfigDto createRtbConfig(RtbConfig.RtbAdMode adMode) {
        RtbConfigDto rtbConfig = new RtbConfigDto();
        rtbConfig.setId(randomLong());
        rtbConfig.setAdMode(adMode);
        rtbConfig.setAuctionType(RtbAuctionType.SECOND_PRICE);
        return rtbConfig;
    }

    public static boolean randomBoolean() {
        return randomInteger(2) > 0.5;
    }

    public static RtbConfig.RtbAdMode randomAdMode() {
        return (RtbConfig.RtbAdMode) randomFromEnum(RtbConfig.RtbAdMode.values());
    }

    public static Enum randomFromEnum(Enum[] values) {
        return values[randomInteger(values.length)];
    }

    public static BidType randomBidType() {
        return (BidType) randomFromEnum(BidType.values());
    }
    
    public static BidModelType randomBidModelType() {
        return (BidModelType) randomFromEnum(BidModelType.values());
    }

    public static CompanyDto createCompany(double discount) {
        CompanyDto companyDto = new CompanyDto();
        companyDto.setId(randomLong());
        companyDto.setMarginShareDSP(discount);
        return companyDto;
    }

    public static CampaignBidDto createBid(double amount, BidType bidType, BidModelType bidModelType, boolean maximum) {
        CampaignBidDto campaignBidDto = new CampaignBidDto();
        campaignBidDto.setId(randomLong());
        campaignBidDto.setBidType(bidType);
        campaignBidDto.setBidModelType(bidModelType);
        campaignBidDto.setAmount(amount);
        campaignBidDto.setMaximum(maximum);

        return campaignBidDto;
    }


    public static SystemVariable createSysVar(String varName, double doubleValue) {
        SystemVariable systemVariable = new SystemVariable();
        systemVariable.setId(randomLong());
        systemVariable.setName(varName);
        systemVariable.setDoubleValue(doubleValue);
        return systemVariable;
    }


}
