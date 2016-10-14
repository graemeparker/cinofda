package com.adfonic.presentation.category.dao;

import java.util.List;

import com.adfonic.presentation.NameIdModel;

public interface CategorySearchDao {
    
    List<NameIdModel> searchForCategories(String categoryNamePrefix);
    
}
