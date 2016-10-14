package com.adfonic.presentation.dashboard.statistics.mock;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.adfonic.dto.campaign.enums.BidType;
import com.adfonic.dto.dashboard.DashboardDto;
import com.adfonic.dto.dashboard.PublisherDashboardDto;
import com.adfonic.dto.dashboard.statistic.AdvertiserHeadlineStatsDto;
import com.adfonic.dto.dashboard.statistic.PublisherHeadlineStatsDto;
import com.adfonic.dto.dashboard.statistic.PublisherStatisticsDto;
import com.adfonic.dto.dashboard.statistic.StatisticsDto;
import com.adfonic.dto.publication.enums.Approval;
import com.adfonic.dto.publication.enums.Backfill;
import com.adfonic.presentation.dashboard.statistics.StatisticsService;
import com.adfonic.presentation.publication.service.PublicationService;

@Service("statisticsServiceMock")
public class StatisticsServiceMock implements StatisticsService {
    
    @Autowired
    private PublicationService pService;

	public AdvertiserHeadlineStatsDto getDashboardStatistics(DashboardDto searchDto) {
		StatisticsDto dto = new StatisticsDto();
		dto.setClicks(1000);
		dto.setConversions(2334);
		dto.setCostPerConversion(2.34);
		dto.setCtr(0.43);
		dto.setImpressions(2993);
		dto.setSpend(2.76);
		return dto;
	}

	@Override
	public List<StatisticsDto> getDashboardReportingTable(
			DashboardDto searchDto) {
		List<StatisticsDto> result = new ArrayList<StatisticsDto>(0);
		for (int k = 0; k < 1000; k++) {
			StatisticsDto dto = new StatisticsDto();
			dto.setCampaignId(1+k);
			dto.setBidPrice(123.3 + k);
			dto.setBudgetSpent(234.1 + k);
			dto.setCampaignName("Campaign name generated " + k);
			dto.setClicks(123 + k);
			dto.setConversions(2 + k);
			dto.setCostPerConversion(23.2 + k);
			dto.setCpa(23 + k);
			dto.setCtr(12 + k);
			dto.setImpressions(1 + k);
			dto.setSpend(23.1 + k);
			if( (k+"").indexOf("4")!=-1 ){
				dto.setStatus("Active");
				dto.setBidType(BidType.CPA);
			}else if( (k+"").indexOf("4")!=-1 ){
				dto.setStatus("Completed");
				dto.setBidType(BidType.CPC);
			}
			else if( (k+"").indexOf("2")!=-1 ){
				dto.setStatus("Pending");
				dto.setBidType(BidType.CPM);
			}
			else if( (k+"").indexOf("3")!=-1 ){
				dto.setStatus("Stopped");
				dto.setBidType(BidType.CPA);
			}else if( (k+"").indexOf("1")!=-1 ){
				dto.setStatus("Paused");
				dto.setBidType(BidType.CPC);
			}else{
				dto.setStatus("Active");
				dto.setBidType(BidType.CPM);
			}
			
			dto.setTotalBudgetToDate(100 + k);
			if(searchDto!=null && searchDto.getCampaignStatusFilter()!=null && !StringUtils.isEmpty(searchDto.getCampaignStatusFilter().getCampaignStatusStr()) ){
				if(dto.getStatus().toLowerCase().equals(searchDto.getCampaignStatusFilter().getCampaignStatusStr().toLowerCase())){
					result.add(dto);
				}else{}
			if(searchDto!=null && searchDto.getBidTypeFilter()!=null &&!StringUtils.isEmpty(searchDto.getBidTypeFilter().getBidType()) && !searchDto.getBidTypeFilter().getId().equals("all")){
				if(dto.getBidType().name().toLowerCase().equals(searchDto.getBidTypeFilter().getId().toLowerCase())){
					result.add(dto);
				}else{}
			}
			}else{
				result.add(dto);
			}
			
		}
		return result;
	}
	
	
	public PublisherHeadlineStatsDto getPublisherDashboardStatistics() {
        PublisherStatisticsDto dto = new PublisherStatisticsDto();
        dto.setRequests(82300000);
        dto.setImpressions(412000);
        dto.setFillRate(53);
        dto.setRevenue(97000);
        dto.setEcpm(0.18);
        return dto;
    }

    @Override
    public List<PublisherStatisticsDto> getPublisherDashboardReportingTable(
            PublisherDashboardDto searchDto) {
        List<PublisherStatisticsDto> result = new ArrayList<PublisherStatisticsDto>(0);
        for (int i = 0; i < NUM_RECORDS; i++) {
            PublisherStatisticsDto dto = new PublisherStatisticsDto();
            dto.setPublicationId(1+i);
            dto.setRequests(randomNumber(2000000000));
            dto.setImpressions(randomNumber(10000));
            dto.setFillRate(randomNumber(100));
            dto.setRequests(randomNumber(100000));
            dto.setPublicationName("Publication name generated " + i);
            dto.setApproval(getApproval((int)randomNumber(2)));
            dto.setBackfill(getBackfill((int)randomNumber(2)));
            dto.setStatus("ACTIVE");
            dto.setPlatform(pService.getPublicationTypeById(randomNumber(7)+1));
            dto.setEcpm(Math.random());
            
            boolean statusFilter=true;
            boolean approvalFilter=true;
            boolean backfillFilter=true;
            boolean platformFilter=true;
            
            if(searchDto!=null && searchDto.getPublicationStatusFilter()!=null && !StringUtils.isEmpty(searchDto.getPublicationStatusFilter().getPublicationStatusStr()) && !searchDto.getPublicationStatusFilter().getPublicationStatusStr().equals("page.dashboard.labels.table.filter.status.options.all") ){
                if(dto.getStatus().equals(searchDto.getPublicationStatusFilter().getPublicationStatusStr())){
                }else{
                    statusFilter=false;
                }
            }
            if(searchDto!=null && searchDto.getApprovalFilter()!=null &&!StringUtils.isEmpty(searchDto.getApprovalFilter().getapproval()) && !searchDto.getApprovalFilter().getId().equals("all")){
                if(dto.getApproval().getId().equals("1")){
                }else{
                    approvalFilter=false;
                }
            }
            if(searchDto!=null && searchDto.getBackfillFilter()!=null &&!StringUtils.isEmpty(searchDto.getBackfillFilter().getbackfill()) && !searchDto.getBackfillFilter().getId().equals("all")){
                if(dto.getBackfill().getId().equals("1")
                        ){
                }else{
                    backfillFilter=false;
                }
            } 
            if(searchDto!=null && searchDto.getPlatformFilter()!=null &&!StringUtils.isEmpty(searchDto.getPlatformFilter().getName()) && searchDto.getPlatformFilter().getId()!=-1){
                if(dto.getPlatform().getName().toLowerCase().equals(searchDto.getPlatformFilter().getName().toLowerCase())){
                }else{
                    platformFilter=false;
                }
            }
            
            if(statusFilter && approvalFilter && backfillFilter && platformFilter)
                result.add(dto);
        }
        return result;
    }
    
    private Approval getApproval(int i){
        Approval approval;
        
        switch (i){
            case 0:
                approval=Approval.MANUAL;
                break;
            
            default:
                approval=Approval.AUTO;
                break;
            
        }
        return approval;
    }
    
    private Backfill getBackfill(int i){        
        switch (i){
            case 0:
                return Backfill.NO;
            
            default:
                return Backfill.YES;                
                
        }
    }


      
    private long randomNumber(int i){
        Random randomGenerator = new Random();
        return Long.valueOf(randomGenerator.nextInt(i));
    }
      	
}
