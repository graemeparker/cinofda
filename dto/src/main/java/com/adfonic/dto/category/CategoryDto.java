package com.adfonic.dto.category;

import java.util.List;

import com.adfonic.dto.NameIdBusinessDto;

public class CategoryDto extends NameIdBusinessDto {

    private static final long serialVersionUID = 1L;
    
    private CategoryDto parent;
    private List<CategoryDto> children;
    private String iabId;

    public CategoryDto getParent() {
        return parent;
    }

    public void setParent(CategoryDto parent) {
        this.parent = parent;
    }

    public List<CategoryDto> getChildren() {
        return children;
    }

    public void setChildren(List<CategoryDto> children) {
        this.children = children;
    }

    public String getIabId() {
        return iabId;
    }

    public void setIabId(String iabId) {
        this.iabId = iabId;
    }

}
