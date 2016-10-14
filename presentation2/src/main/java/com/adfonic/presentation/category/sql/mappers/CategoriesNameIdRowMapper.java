package com.adfonic.presentation.category.sql.mappers;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

import com.adfonic.presentation.sql.mappers.AbstractNameIdDtoRowMapper;

public class CategoriesNameIdRowMapper extends AbstractNameIdDtoRowMapper {

    public static final String CATEGORY_SEPARATOR = " > ";
    
    @Override
    protected String getNameColumnName() {
        return "hierarchy";
    }
    
    @Override
    /** Reverse the order of the categories, replace the ~ separator */
    protected String transformNameColumnValue(String nameColumnValue) {
        if (!StringUtils.isEmpty(nameColumnValue)) {
            StringBuilder sb = new StringBuilder();
            String[] categoryHierarchy = nameColumnValue.split(TILDE);
            ArrayUtils.reverse(categoryHierarchy);
            String sep = StringUtils.EMPTY;
            for (String category : categoryHierarchy) {
                sb.append(sep).append(category);
                sep = CATEGORY_SEPARATOR;
            }
            
            return sb.toString();
        }
        return StringUtils.EMPTY;
    }

}
