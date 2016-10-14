package com.adfonic.dto.campaign.segment;

import java.util.HashSet;
import java.util.Set;

import org.springframework.util.CollectionUtils;

import com.adfonic.dto.model.ModelDto;

public class SegmentDto extends AbstractSegmentDto {

    private static final long serialVersionUID = 1L;

    private Set<ModelDto> models = new HashSet<ModelDto>(0); // empty = don't  care
    private Set<ModelDto> excludedModels = new HashSet<ModelDto>(0); // empty = don't care. Further restricts a targeted Platform.

    public Set<ModelDto> getModels() {
        return models;
    }

    public void setModels(Set<ModelDto> models) {
        this.models = models;
    }

    public Set<ModelDto> getExcludedModels() {
        return excludedModels;
    }

    public void setExcludedModels(Set<ModelDto> excludedModels) {
        this.excludedModels = excludedModels;
    }
    
    @Override
    public boolean isModelsTargeted() {
        return !CollectionUtils.isEmpty(getModels());
    }
}
