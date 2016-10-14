package com.adfonic.presentation.targetpublisher.impl;

import static com.byyd.middleware.iface.dao.SortOrder.asc;

import java.util.Collection;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.adfonic.domain.Publisher;
import com.adfonic.domain.TargetPublisher;
import com.adfonic.domain.TargetPublisher_;
import com.adfonic.dto.targetpublisher.TargetPublisherDto;
import com.adfonic.presentation.targetpublisher.TargetPublisherService;
import com.adfonic.presentation.util.GenericServiceImpl;
import com.byyd.middleware.account.filter.TargetPublisherFilter;
import com.byyd.middleware.account.service.CompanyManager;
import com.byyd.middleware.account.service.PublisherManager;
import com.byyd.middleware.iface.dao.FetchStrategy;
import com.byyd.middleware.iface.dao.Sorting;
import com.byyd.middleware.iface.dao.jpa.FetchStrategyBuilder;

@Service("targetPublisherService")
public class TargetPublisherServiceImpl extends GenericServiceImpl implements TargetPublisherService {
	
	private static Logger LOGGER = LoggerFactory.getLogger(TargetPublisherServiceImpl.class);
	
    @Autowired
    private org.dozer.Mapper mapper;
	@Autowired
	private PublisherManager publisherManager;
	@Autowired
	private CompanyManager companyManager;	
	
	public Collection<TargetPublisherDto> getAllTargetPublishers(boolean isRtb, boolean hidden) {
	    FetchStrategy fetchStrategy = new FetchStrategyBuilder()
	    .addLeft(TargetPublisher_.publisher)
	    .build();
		// TargetPublisherFilter filter, Sorting sort,FetchStrategy...
		// fetchStrategy
		TargetPublisherFilter filter = new TargetPublisherFilter(isRtb, hidden);
	    
		// Sorting sort = new Sorting();
		Sorting sort = new Sorting(
				asc(TargetPublisher.class, "displayPriority"),
				asc(TargetPublisher.class, "name"));
		List<TargetPublisher> publishers = publisherManager.getAllTargetPublishers(filter, sort,fetchStrategy);
		return getDtoList(TargetPublisherDto.class, publishers);
	}
	
	public Collection<TargetPublisherDto> getAllTargetPublishersForPmp(boolean isRtb, boolean pmpAvailable, boolean hidden) {
	    FetchStrategy fetchStrategy = new FetchStrategyBuilder()
	    .addLeft(TargetPublisher_.publisher)
	    .build();
	    // TargetPublisherFilter filter, Sorting sort,FetchStrategy...
	    // fetchStrategy
	    TargetPublisherFilter filter = new TargetPublisherFilter(isRtb, pmpAvailable, hidden);
	    
	    // Sorting sort = new Sorting();
	    Sorting sort = new Sorting(
	            asc(TargetPublisher.class, "displayPriority"),
	            asc(TargetPublisher.class, "name"));
	    List<TargetPublisher> publishers = publisherManager.getAllTargetPublishers(filter, sort,fetchStrategy);//
	    return getDtoList(TargetPublisherDto.class, publishers);
	}
	
	public TargetPublisherDto getTargetPublisherByName(String name){
	    FetchStrategy fetchStrategy = new FetchStrategyBuilder()
	    .addLeft(TargetPublisher_.publisher)
	    .build();
		TargetPublisher targetPublisher = publisherManager.getTargetPublisherByName(name,fetchStrategy);
		TargetPublisherDto targetPublisherDto = getDtoObject(TargetPublisherDto.class, targetPublisher);
		return targetPublisherDto;
	}
	
	public TargetPublisherDto getTargetPublisherByPublisherId(Long publisherId){
	    FetchStrategy fetchStrategy = new FetchStrategyBuilder()
									    .addLeft(TargetPublisher_.publisher)
									    .build();
	    TargetPublisherFilter filter = new TargetPublisherFilter();
	    Publisher pub = publisherManager.getPublisherById(publisherId);
	    filter.setPublisher(pub);
	    try{
			TargetPublisher targetPublisher = publisherManager.getTargetPublisherByPublisherId(filter,fetchStrategy);
			if(targetPublisher != null) {
				TargetPublisherDto targetPublisherDto = getDtoObject(TargetPublisherDto.class, targetPublisher);
				return targetPublisherDto;
			} else {
				return null;
			}
	    }catch(Exception e ) {
	    	LOGGER.warn("getTargetPublisherByPublisherId error loading targetPublisher for pub=["+publisherId+"]",e);
	    	return null;
	    }
	}
	
	public TargetPublisherDto getTargetPublisherById(Long targetPublisherId){
	    FetchStrategy fetchStrategy = new FetchStrategyBuilder()
	    .addLeft(TargetPublisher_.publisher)
	    .build();
	    TargetPublisher targetPublisher = publisherManager.getObjectById(TargetPublisher.class, targetPublisherId,fetchStrategy);
		TargetPublisherDto targetPublisherDto = getDtoObject(TargetPublisherDto.class, targetPublisher);
		return targetPublisherDto;
	}
}