package com.adfonic.presentation.campaign.channel;

import java.util.Collection;
import java.util.List;

import com.adfonic.dto.category.CategoryDto;
import com.adfonic.dto.channel.ChannelDto;

public interface ChannelService {

	public Collection<ChannelDto> getAllChannels();

	public ChannelDto getChannelById(Long id);

	public ChannelDto getChannelByName(String name);
	
	//TODO: Remove methods after demo
	public List<CategoryDto> getCategories();
	public CategoryDto getCategoryById(Long id);
}
