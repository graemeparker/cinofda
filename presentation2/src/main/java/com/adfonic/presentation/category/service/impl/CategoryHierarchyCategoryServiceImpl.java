package com.adfonic.presentation.category.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.adfonic.domain.Category;
import com.adfonic.dto.category.CategoryHierarchyDto;
import com.adfonic.presentation.category.service.CategoryHierarchyCategoryService;
import com.adfonic.presentation.util.GenericServiceImpl;
import com.byyd.middleware.common.service.CommonManager;

@Service("categoryHierarchyCategoryService")
public class CategoryHierarchyCategoryServiceImpl extends GenericServiceImpl implements CategoryHierarchyCategoryService {

	@Autowired
	private CommonManager commonManager;

	@Transactional(readOnly=true)
	public CategoryHierarchyDto getCategoryHierarchyById(Long id) {
        Category category = commonManager.getCategoryById(id);
        CategoryHierarchyDto dto = getObjectDto(CategoryHierarchyDto.class, category);
        return dto;
    }

}
