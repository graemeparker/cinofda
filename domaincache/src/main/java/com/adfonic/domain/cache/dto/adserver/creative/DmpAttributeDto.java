package com.adfonic.domain.cache.dto.adserver.creative;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class DmpAttributeDto implements Serializable {

    private static final long serialVersionUID = 2L;

    private Long id;

    private List<DmpSelectorDto> selectors;

    public DmpAttributeDto(Long id) {
        this(id, new ArrayList<DmpSelectorDto>());
    }

    public DmpAttributeDto(Long id, List<DmpSelectorDto> selectors) {
        this.id = id;
        this.selectors = selectors;
    }

    public DmpAttributeDto() {
        // default
    }

    public Long getId() {
        return id;
    }

    public List<DmpSelectorDto> getSelectors() {
        return selectors;
    }

    public void addSelector(DmpSelectorDto selector) {
        this.selectors.add(selector);
    }

    @Override
    public String toString() {
        return "DmpAttributeDto {id=" + id + ", selectors=" + selectors + "}";
    }

}
