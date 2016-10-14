package com.adfonic.dto.category;

import java.util.List;

import org.jdto.annotation.DTOCascade;
import org.jdto.annotation.Source;

import com.adfonic.dto.NameIdBusinessDto;

public class CategoryHierarchyDto extends NameIdBusinessDto {
    
    private static final long serialVersionUID = 1L;
    
    @DTOCascade
    @Source(value = "parent")
    private CategoryHierarchyDto parent;
    private List<CategoryHierarchyDto> children;
    private String iabId;

    public CategoryHierarchyDto getParent() {
        return parent;
    }

    public void setParent(CategoryHierarchyDto parent) {
        this.parent = parent;
    }

    public List<CategoryHierarchyDto> getChildren() {
        return children;
    }

    public void setChildren(List<CategoryHierarchyDto> children) {
        this.children = children;
    }

    public String getIabId() {
        return iabId;
    }

    public void setIabId(String iabId) {
        this.iabId = iabId;
    }

}
