package com.byyd.middleware.integrations.dao;

import com.adfonic.domain.PluginVendor;
import com.byyd.middleware.iface.dao.BusinessKeyDao;
import com.byyd.middleware.iface.dao.FetchStrategy;

public interface PluginVendorDao extends BusinessKeyDao<PluginVendor>  {

    public PluginVendor getByEmail(String emailAddress, FetchStrategy... fetchStrategy);
    
}
