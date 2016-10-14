package com.adfonic.tools.converter.category;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.FacesConverter;

import com.adfonic.dto.category.CategoryHierarchyDto;
import com.adfonic.presentation.category.service.CategoryHierarchyCategoryService;
import com.adfonic.tools.converter.GenericConverter;

@FacesConverter(value = "categoryHierarchyConverter", forClass = com.adfonic.dto.category.CategoryHierarchyDto.class)
public class CategoryHierarchyConverter extends GenericConverter {

    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        if (value != null) {
            CategoryHierarchyCategoryService service = getSpringService(context,
                    com.adfonic.presentation.category.service.CategoryHierarchyCategoryService.class);
            CategoryHierarchyDto dto = service.getCategoryHierarchyById(Long.valueOf(value));
            return dto;
        } else {
            return null;
        }
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Object value) {
        if (value != null) {
            CategoryHierarchyDto dto = (CategoryHierarchyDto) value;
            return Long.toString(dto.getId());
        } else {
            return null;
        }
    }

}
