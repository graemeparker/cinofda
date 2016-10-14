package com.adfonic.webservices.dto.mapping;

import com.adfonic.domain.Category;

public class CategoryConverter extends BaseReferenceEntityConverter<Category> {

    public CategoryConverter() {
        super(Category.class, "name");
    }

    @Override
    public Category resolveEntity(String name) {
        return getCommonManager().getCategoryByName(name);
    }
}
