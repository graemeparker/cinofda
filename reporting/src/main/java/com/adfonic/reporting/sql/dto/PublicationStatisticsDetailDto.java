package com.adfonic.reporting.sql.dto;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import com.adfonic.reporting.sql.dto.gen.Tag;
import com.adfonic.reporting.sql.dto.gen.Tagged;
import com.adfonic.reporting.sql.dto.gen.TaggedTagGroup;
import com.adfonic.reporting.sql.dto.gen.Tag.TAG;

public class PublicationStatisticsDetailDto implements TaggedTagGroup, Serializable {

    private static final long serialVersionUID = 1L;

    private Tag tag;
    
    private Set<Tagged> adslot;


    public PublicationStatisticsDetailDto(String publicationId) {
        this.tag = new Tag(TAG.PUBLICATION, publicationId);
        adslot=new HashSet<Tagged>();
    }


    @Override
    public Tag getTag() {
        return tag;
    }


    @Override
    public Set<Tagged> getTaggedSet() {
        return adslot;
    }

}
