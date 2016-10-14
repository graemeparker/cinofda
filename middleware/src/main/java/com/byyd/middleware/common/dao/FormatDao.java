package com.byyd.middleware.common.dao;

import java.util.Collection;
import java.util.List;

import com.adfonic.domain.AdSpace;
import com.adfonic.domain.Campaign;
import com.adfonic.domain.Format;
import com.byyd.middleware.iface.dao.BusinessKeyDao;
import com.byyd.middleware.iface.dao.FetchStrategy;

public interface FormatDao extends BusinessKeyDao<Format> {

    @Override
    Format getBySystemName(String systemName, FetchStrategy... fetchStrategy);
    List<Format> getSupportedFormats(Collection<AdSpace> adSpaces, FetchStrategy... fetchStrategy);
    List<Format> getSupportedFormats(Campaign campaign, FetchStrategy... fetchStrategy);
}
