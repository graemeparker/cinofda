package com.adfonic.dto.campaign.segment;

import java.util.HashSet;
import java.util.Set;

import org.springframework.util.CollectionUtils;

import com.adfonic.dto.model.ModelPartialDto;

public class SegmentPartialDto extends AbstractSegmentDto {
    
    private static final long serialVersionUID = 1L;

    private Set<ModelPartialDto> models = new HashSet<ModelPartialDto>(0); // empty  = don't care
    private Set<ModelPartialDto> excludedModels = new HashSet<ModelPartialDto>(0); // empty = don't care. Further restricts a targeted Platform.

    public Set<ModelPartialDto> getModels() {
        return models;
    }

    public void setModels(Set<ModelPartialDto> models) {
        this.models = models;
    }

    public Set<ModelPartialDto> getExcludedModels() {
        return excludedModels;
    }

    public void setExcludedModels(Set<ModelPartialDto> excludedModels) {
        this.excludedModels = excludedModels;
    }
    
    @Override
    public boolean isModelsTargeted() {
        return !CollectionUtils.isEmpty(getModels());
    }
}
