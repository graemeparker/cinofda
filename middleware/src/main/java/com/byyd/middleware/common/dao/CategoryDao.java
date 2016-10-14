package com.byyd.middleware.common.dao;

import java.util.List;

import com.adfonic.domain.Category;
import com.byyd.middleware.iface.dao.BusinessKeyDao;
import com.byyd.middleware.iface.dao.FetchStrategy;
import com.byyd.middleware.iface.dao.Pagination;
import com.byyd.middleware.iface.dao.Sorting;

public interface CategoryDao extends BusinessKeyDao<Category> {

    Category getByIabId(String iabId, FetchStrategy ... fetchStrategy);
    
    Long countAllForParent(Category parent);
    List<Category> getAllForParent(Category parent, FetchStrategy... fetchStrategy);
    List<Category> getAllForParent(Category parent, Sorting sort, FetchStrategy... fetchStrategy);
    List<Category> getAllForParent(Category parent, Pagination page, FetchStrategy... fetchStrategy);

    Long countAllForParentIsNull();
    List<Category> getAllForParentIsNull(FetchStrategy... fetchStrategy);
    List<Category> getAllForParentIsNull(Sorting sort, FetchStrategy... fetchStrategy);
    List<Category> getAllForParentIsNull(Pagination page, FetchStrategy... fetchStrategy);

    Category getParentCategoryForCategory(Category category, FetchStrategy... fetchStrategy);

}
