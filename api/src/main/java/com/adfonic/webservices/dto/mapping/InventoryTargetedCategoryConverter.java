package com.adfonic.webservices.dto.mapping;

import com.adfonic.domain.Category;
import com.adfonic.webservices.exception.ValidationException;

public class InventoryTargetedCategoryConverter extends BaseReferenceEntityConverter<Category> {

    public InventoryTargetedCategoryConverter() {
        super(Category.class, "iabId");
    }


    @Override
    protected Category resolveEntity(String iabId) {// resolve IAB category (both) top level
        Category category = getCommonManager().getCategoryByIabId(iabId);
        if (category != null && category.getParent() == null && iabId.startsWith("IAB")) {
            return category;
        }

        throw new ValidationException("Invalid targeted category!");
    }

}
