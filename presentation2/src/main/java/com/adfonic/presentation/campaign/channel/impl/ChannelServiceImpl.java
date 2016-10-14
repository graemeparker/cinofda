package com.adfonic.presentation.campaign.channel.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.adfonic.domain.Category;
import com.adfonic.domain.Channel;
import com.adfonic.dto.category.CategoryDto;
import com.adfonic.dto.channel.ChannelDto;
import com.adfonic.presentation.campaign.channel.ChannelService;
import com.adfonic.presentation.util.GenericServiceImpl;
import com.byyd.middleware.campaign.filter.ChannelFilter;
import com.byyd.middleware.common.service.CommonManager;

@Service("channelService")
public class ChannelServiceImpl extends GenericServiceImpl implements ChannelService {
	
	@Autowired
	private CommonManager commonManager;

	public Collection<ChannelDto> getAllChannels() {
		// This needs to exclude the NOT_CATEGORIED_CHANNEL
		List<Channel> channels = commonManager
				.getAllChannels(new ChannelFilter()
						.setExcludeUncategorized(true));
		Collection<ChannelDto> channelsDto = getList(ChannelDto.class, channels);
		return channelsDto;
	}

	public ChannelDto getChannelById(Long id) {
		// This needs to exclude the NOT_CATEGORIED_CHANNEL
		Channel channel = commonManager.getChannelById(id);
		ChannelDto channelDto = getObjectDto(ChannelDto.class, channel);
		return channelDto;
	}

	public ChannelDto getChannelByName(String name) {
		// This needs to exclude the NOT_CATEGORIED_CHANNEL
		Channel channel = commonManager.getChannelByName(name);
		ChannelDto channelDto = getObjectDto(ChannelDto.class, channel);
		return channelDto;
	}
	
	public List<CategoryDto> getCategories(){
	    List<CategoryDto> categories = new ArrayList<CategoryDto>();
	    
	    for(int i=1;i<24;i++){
	        Category c = commonManager.getCategoryByIabId("IAB" + i);
	        categories.add(getObjectDto(CategoryDto.class, c));
	    }
	    
	    return categories;
	}
	
	public CategoryDto getCategoryById(Long id){
        Category category = commonManager.getCategoryById(id);
        
        return getObjectDto(CategoryDto.class, category);
    }
}
