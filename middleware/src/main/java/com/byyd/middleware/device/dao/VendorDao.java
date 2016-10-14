package com.byyd.middleware.device.dao;

import java.util.List;

import com.adfonic.domain.DeviceGroup;
import com.adfonic.domain.Platform;
import com.adfonic.domain.Vendor;
import com.byyd.middleware.iface.dao.BusinessKeyDao;
import com.byyd.middleware.iface.dao.FetchStrategy;

public interface VendorDao extends BusinessKeyDao<Vendor> {

    List<Vendor> getVendorsByPlatformAndDeviceGroup(String vendorName, List<Platform> platforms, DeviceGroup deviceGroup, FetchStrategy... fetchStrategy);
}
