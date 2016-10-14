package com.adfonic.tools.beans.campaign.targeting;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.adfonic.domain.Channel;
import com.adfonic.dto.campaign.CampaignDto;
import com.adfonic.dto.channel.ChannelDto;
import com.adfonic.presentation.FacesUtils;
import com.adfonic.presentation.campaign.channel.ChannelService;
import com.adfonic.tools.beans.util.GenericAbstractBean;

@Component
@Scope("view")
public class CampaignTargetingChannelMBean extends GenericAbstractBean implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private static final Logger LOGGER = LoggerFactory.getLogger(CampaignTargetingChannelMBean.class);

    @Autowired
    private ChannelService channelService;

    private CampaignDto campaignDto;

    private Collection<ChannelDto> channels = null;

    private List<ChannelDto> targetedChannels = new ArrayList<ChannelDto>(0);

    private boolean channelEnabled = false;

    @Override
    public void init() {
    };

    public void loadCampaignDto(CampaignDto dto) {
        LOGGER.debug("loadCampaignDto-->");
        this.campaignDto = dto;
        if (campaignDto != null && !CollectionUtils.isEmpty(campaignDto.getCurrentSegment().getChannels())) {
            // compare if all the channels added
            // if all the channels selected, then no display checked channels!
            // greater or equal, in case user has also the uncategorized
            // channel!
            if (campaignDto.getCurrentSegment().getChannels().size() < getChannels().size()) {
                CollectionUtils.addAll(targetedChannels, campaignDto.getCurrentSegment().getChannels().iterator());
                setChannelEnabled(true);
            }
        }
        LOGGER.debug("loadCampaignDto<--");
    }

    public CampaignDto prepareDto(CampaignDto dto, boolean channelEnabled) {
        LOGGER.debug("prepareDto-->");
        campaignDto = dto;

        if (channelEnabled) {
            LOGGER.debug("Channel enabled");
            // user has channels in it either if has chosen some channels or not
            // (not selecting one means he wants all!)
            dto.getCurrentSegment().setChannelEnabled(true);
            // check if user has the uncategorized channel
            Set<ChannelDto> setUncate = getCheckUncategorizedChannel();

            if (CollectionUtils.isEmpty(targetedChannels)) {
                // need to add ALL the channels since none clicked means target
                // all!
                setUncate.addAll(getChannels());
            } else {
                // user will have targeted only a few, which will be already in
                // the getTargetedChannels
                setUncate.addAll(targetedChannels);
            }
            dto.getCurrentSegment().getChannels().clear();
            dto.getCurrentSegment().getChannels().addAll(setUncate);
        } else {
            LOGGER.debug("Channel not enabled");
            // user is not a channel enabled or dsp, so
            dto.getCurrentSegment().setChannelEnabled(false);
            dto.getCurrentSegment().getChannels().clear();
        }

        LOGGER.debug("prepareDto<--");
        return dto;
    }

    public Set<ChannelDto> getCheckUncategorizedChannel() {
        LOGGER.debug("getCheckUncategorizedChannel-->");
        Set<ChannelDto> returnChannels = new HashSet<ChannelDto>();
        // Only return the special "Uncategorized" channel if it's among the set
        if (!CollectionUtils.isEmpty(campaignDto.getCurrentSegment().getChannels())) {
            for (ChannelDto chan : campaignDto.getCurrentSegment().getChannels()) {
                if (Channel.NOT_CATEGORIZED_NAME.equals(chan.getName())) {
                    LOGGER.debug(chan.getName() + " channel not catecorized");
                    returnChannels.add(chan);
                    break; // don't need to go any further
                }
            }

        }
        LOGGER.debug("getCheckUncategorizedChannel<--");
        return returnChannels;
    }

    public boolean allChannels() {
        if (campaignDto.getCurrentSegment().getChannels().size() == getChannels().size()
                || CollectionUtils.isEmpty(campaignDto.getCurrentSegment().getChannels())) {
            return true;
        }
        return false;
    }

    public boolean isChannelEnabledUser(Map<String, Object> map) {
        String a = (String) map.get("channelEnabledUser");
        if (a != null) {
            return true;
        } else {
            return false;
        }
    }

    public String getChannelsSummary(boolean spaces) {
        if (campaignDto != null && campaignDto.getCurrentSegment() != null
                && !CollectionUtils.isEmpty(campaignDto.getCurrentSegment().getChannels())) {
            String space = "";
            if (spaces) {
                space = " ";
            }
            if (allChannels()) {
                return FacesUtils.getBundleMessage("page.campaign.menu.all.label");
            }
            String message = "";

            for (ChannelDto c : campaignDto.getCurrentSegment().getChannels()) {
                message += c.getName() + "," + space;
            }
            message = message.substring(0, message.length() - (1 + space.length()));
            return message;
        }
        return FacesUtils.getBundleMessage("page.campaign.menu.all.label");
    }

    public CampaignDto getCampaignDto() {
        return campaignDto;
    }

    public void setCampaignDto(CampaignDto campaignDto) {
        this.campaignDto = campaignDto;
    }

    public boolean getChannelEnabled() {
        return channelEnabled;
    }

    public void setChannelEnabled(boolean channelEnabled) {
        this.channelEnabled = channelEnabled;
    }

    /***
     * Get all channel Dto
     * **/
    public Collection<ChannelDto> getChannels() {
        // This needs to exclude the NOT_CATEGORIED_CHANNEL
        if (channels == null) {
            channels = channelService.getAllChannels();
        }
        return channels;
    }

    public List<ChannelDto> getTargetedChannels() {
        if (targetedChannels == null) {
            targetedChannels = new ArrayList<ChannelDto>();
        }
        return targetedChannels;
    }

    public void setTargetedChannels(List<ChannelDto> targetedChannels) {
        this.targetedChannels = targetedChannels;
    }
}
