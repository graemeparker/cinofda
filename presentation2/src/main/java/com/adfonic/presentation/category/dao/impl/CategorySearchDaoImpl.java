package com.adfonic.presentation.category.dao.impl;

import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import com.adfonic.presentation.NameIdModel;
import com.adfonic.presentation.category.dao.CategorySearchDao;
import com.adfonic.presentation.category.sql.procedure.CategoriesStoredProcedure;
import com.adfonic.presentation.util.GenericDaoImpl;
import com.adfonic.presentation.util.Utils;

/**
 * DAO class for category search.
 */
@Repository
public class CategorySearchDaoImpl extends GenericDaoImpl implements CategorySearchDao {

    private static Logger LOG = Logger.getLogger(CategorySearchDaoImpl.class.getName());

    @Autowired(required = false)
    @Qualifier("readOnlyDataSourceForPublicationApproval")
    private DataSource dataSource;

    @Override
    public List<NameIdModel> searchForCategories(String categoryNamePrefix) {

        CategoriesStoredProcedure procedure = new CategoriesStoredProcedure(dataSource, "proc_return_categories");
        Utils.logWithTitle(LOG, Level.FINE, "Categories proc call", procCallWithOneParam(procedure.getSql(), categoryNamePrefix));
        Map<String, Object> data = procedure.execute(categoryNamePrefix);

        @SuppressWarnings("unchecked")
        List<NameIdModel> result = (List<NameIdModel>) data.get("result");
        Utils.logWithTitle(LOG, Level.FINE, "Record count", Integer.valueOf(result.size()));
        return result;
    }

}
