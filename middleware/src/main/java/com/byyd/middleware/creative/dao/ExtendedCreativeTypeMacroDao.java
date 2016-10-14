package com.byyd.middleware.creative.dao;

import java.util.List;

import com.adfonic.domain.ExtendedCreativeType;
import com.adfonic.domain.ExtendedCreativeTypeMacro;
import com.byyd.middleware.iface.dao.BusinessKeyDao;
import com.byyd.middleware.iface.dao.FetchStrategy;
import com.byyd.middleware.iface.dao.Pagination;
import com.byyd.middleware.iface.dao.Sorting;

public interface ExtendedCreativeTypeMacroDao extends BusinessKeyDao<ExtendedCreativeTypeMacro> {

    Long countAllForExtendedCreativeType(ExtendedCreativeType extendedCreativeType);
    List<ExtendedCreativeTypeMacro> getAllForExtendedCreativeType(ExtendedCreativeType extendedCreativeType, FetchStrategy... fetchStrategy);
    List<ExtendedCreativeTypeMacro> getAllForExtendedCreativeType(ExtendedCreativeType extendedCreativeType, Sorting sort, FetchStrategy... fetchStrategy);
    List<ExtendedCreativeTypeMacro> getAllForExtendedCreativeType(ExtendedCreativeType extendedCreativeType, Pagination page, FetchStrategy... fetchStrategy);

}
