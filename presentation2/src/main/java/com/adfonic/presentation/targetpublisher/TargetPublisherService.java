package com.adfonic.presentation.targetpublisher;

import java.util.Collection;

import com.adfonic.dto.targetpublisher.TargetPublisherDto;

public interface TargetPublisherService {
    
	public Collection<TargetPublisherDto> getAllTargetPublishers(boolean rtb, boolean hidden);
	
	public Collection<TargetPublisherDto> getAllTargetPublishersForPmp(boolean rtb, boolean pmpAvailable, boolean hidden);

	public TargetPublisherDto getTargetPublisherByName(String name);
	
	public TargetPublisherDto getTargetPublisherByPublisherId(Long publisherId);
	
	public TargetPublisherDto getTargetPublisherById(Long targetPublisherId);
}
