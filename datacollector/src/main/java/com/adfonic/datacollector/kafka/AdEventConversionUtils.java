package com.adfonic.datacollector.kafka;

import java.util.LinkedHashMap;

import net.byyd.archive.model.v1.AdAction;
import net.byyd.archive.model.v1.AdEvent;
import net.byyd.archive.model.v1.Gender;
import net.byyd.archive.model.v1.UnfilledReason;

import org.apache.commons.collections.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.adfonic.adserver.AdEventFactory;
import com.adfonic.util.Range;

@Component
public class AdEventConversionUtils {
    
    @Autowired
    private AdEventFactory adEventFactory;
    
    public com.adfonic.adserver.AdEvent convertJsonAdEvent(AdEvent source, com.adfonic.domain.AdAction adAction){
        com.adfonic.adserver.AdEvent result = adEventFactory.newInstance(adAction, source.getHost(), source.getEventTime(), source.getUserTimeId());
        
        result.setCreativeId(source.getCreativeId());
        result.setCampaignId(source.getCampaignId());
        result.setAdSpaceId(source.getAdSpaceId());
        result.setPublicationId(source.getPublicationId());
        result.setModelId(source.getModelId());
        result.setCountryId(source.getCountryId());
        result.setOperatorId(source.getOperatorId());
        if (source.getAgeFrom() != null && source.getAgeTo() != null) {
            result.setAgeRange(new Range<Integer>(source.getAgeFrom(),source.getAgeTo()));
        }
        if(source.getGender()!=null){
            if(source.getGender().equals(Gender.FEMALE)){
                result.setGender(com.adfonic.domain.Gender.FEMALE);
            }
            else{
                result.setGender(com.adfonic.domain.Gender.MALE);
            }
        }
        result.setGeotargetId(source.getGeotargetId());
        result.setIntegrationTypeId(source.getIntegrationTypeId());
        result.setTestMode(source.isTestMode());
        result.setIpAddress(source.getIpAddress());
        if(source.getUnfilledReason()!=null){
            result.setUnfilledReason(convertUnfilledReason(source.getUnfilledReason()));
        }
        result.setUserAgentHeader(source.getUserAgentHeader());
        result.setTrackingIdentifier(source.getTrackingIdentifier());
        result.setRtbSettlementPrice(source.getRtbSettlementPrice());
        result.setPostalCodeId(source.getPostalCodeId());
        result.setActionValue(source.getActionValue());
        if (MapUtils.isNotEmpty(source.getDeviceIdentifiers())) {
            result.setDeviceIdentifiers(new LinkedHashMap<Long,String>(source.getDeviceIdentifiers()));
        }
        result.setImpressionExternalID(source.getImpressionExternalID());
        result.setRtbBidPrice(source.getRtbBidPrice());
        result.setStrategy(source.getStrategy());
        result.setDateOfBirth(source.getDateOfBirth());
        result.setLatitude(source.getLatitude());
        result.setLongitude(source.getLongitude());
        result.setLocationSource(source.getLocationSource());
        result.setCampaignHistoryDataFeeId(source.getCampaignHistoryDataFeeId());
        
        return result;
    }
    
    public com.adfonic.domain.AdAction convertAdAction(AdAction source){
        if(source.equals(AdAction.AD_SERVED)){
            return com.adfonic.domain.AdAction.AD_SERVED;
        } else if(source.equals(AdAction.IMPRESSION)){
            return com.adfonic.domain.AdAction.IMPRESSION;
        } else if(source.equals(AdAction.CLICK)){
            return com.adfonic.domain.AdAction.CLICK;
        } else if(source.equals(AdAction.UNFILLED_REQUEST)){
            return com.adfonic.domain.AdAction.UNFILLED_REQUEST;
        }  else if(source.equals(AdAction.INSTALL)){
            return com.adfonic.domain.AdAction.INSTALL;
        } else if(source.equals(AdAction.CONVERSION)){
            return com.adfonic.domain.AdAction.CONVERSION;
        } else if(source.equals(AdAction.COMPLETED_VIEW)){
            return com.adfonic.domain.AdAction.COMPLETED_VIEW;
        } else if(source.equals(AdAction.VIEW_Q1)){
            return com.adfonic.domain.AdAction.VIEW_Q1;
        } else if(source.equals(AdAction.VIEW_Q2)){
            return com.adfonic.domain.AdAction.VIEW_Q2;
        } else if(source.equals(AdAction.VIEW_Q3)){
            return com.adfonic.domain.AdAction.VIEW_Q3;
        } else if(source.equals(AdAction.VIEW_Q4)){
            return com.adfonic.domain.AdAction.VIEW_Q4;
        } else if(source.equals(AdAction.RTB_LOST)){
            return com.adfonic.domain.AdAction.BID_FAILED;
        }
     
        return null;
    }
    
    public com.adfonic.domain.UnfilledReason convertUnfilledReason(UnfilledReason source){
        if(source.equals(UnfilledReason.EXCEPTION)){
            return com.adfonic.domain.UnfilledReason.EXCEPTION;
        } else if(source.equals(UnfilledReason.FREQUENCY_CAP)){
            return com.adfonic.domain.UnfilledReason.FREQUENCY_CAP;
        } else if(source.equals(UnfilledReason.NO_CREATIVES)){
            return com.adfonic.domain.UnfilledReason.NO_CREATIVES;
        } else if(source.equals(UnfilledReason.NO_DEVICE_PROPS)){
            return com.adfonic.domain.UnfilledReason.NO_DEVICE_PROPS;
        } else if(source.equals(UnfilledReason.NO_MODEL)){
            return com.adfonic.domain.UnfilledReason.NO_MODEL;
        } else if(source.equals(UnfilledReason.NO_USER_AGENT)){
            return com.adfonic.domain.UnfilledReason.NO_USER_AGENT;
        }  else if(source.equals(UnfilledReason.NOT_MOBILE_DEVICE)){
            return com.adfonic.domain.UnfilledReason.NOT_MOBILE_DEVICE;
        } else if(source.equals(UnfilledReason.PRIVATE_NETWORK)){
            return com.adfonic.domain.UnfilledReason.PRIVATE_NETWORK;
        } else if(source.equals(UnfilledReason.PUB_TYPE_MODEL_MISMATCH)){
            return com.adfonic.domain.UnfilledReason.PUB_TYPE_MODEL_MISMATCH;
        } else if(source.equals(UnfilledReason.TIMEOUT)){
            return com.adfonic.domain.UnfilledReason.TIMEOUT;
        } else if(source.equals(UnfilledReason.UNKNOWN)){
            return com.adfonic.domain.UnfilledReason.UNKNOWN;
        }
        
        return com.adfonic.domain.UnfilledReason.UNKNOWN;
    }
}