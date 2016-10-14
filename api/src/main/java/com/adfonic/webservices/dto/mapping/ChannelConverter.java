package com.adfonic.webservices.dto.mapping;

import com.adfonic.domain.Channel;

public class ChannelConverter extends BaseReferenceEntityConverter<Channel> {

    public ChannelConverter() {
        super(Channel.class, "name");
    }

    @Override
    public Channel resolveEntity(String name) {
        return getCommonManager().getChannelByName(name);
    }
}
