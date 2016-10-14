package com.byyd.middleware.device.service.jpa;

import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.adfonic.domain.Browser;
import com.adfonic.domain.Capability;
import com.adfonic.domain.Country;
import com.adfonic.domain.DeviceGroup;
import com.adfonic.domain.DeviceIdentifierType;
import com.adfonic.domain.DisplayType;
import com.adfonic.domain.Model;
import com.adfonic.domain.Operator;
import com.adfonic.domain.OperatorAlias;
import com.adfonic.domain.Platform;
import com.adfonic.domain.Segment;
import com.adfonic.domain.Vendor;
import com.byyd.middleware.campaign.service.TargetingManager;
import com.byyd.middleware.device.dao.BrowserDao;
import com.byyd.middleware.device.dao.CapabilityDao;
import com.byyd.middleware.device.dao.DeviceGroupDao;
import com.byyd.middleware.device.dao.DeviceIdentifierTypeDao;
import com.byyd.middleware.device.dao.DisplayTypeDao;
import com.byyd.middleware.device.dao.ModelDao;
import com.byyd.middleware.device.dao.OperatorDao;
import com.byyd.middleware.device.dao.PlatformDao;
import com.byyd.middleware.device.dao.VendorDao;
import com.byyd.middleware.device.filter.ModelFilter;
import com.byyd.middleware.device.filter.OperatorFilter;
import com.byyd.middleware.device.service.DeviceManager;
import com.byyd.middleware.iface.dao.FetchStrategy;
import com.byyd.middleware.iface.dao.LikeSpec;
import com.byyd.middleware.iface.dao.Pagination;
import com.byyd.middleware.iface.dao.SortOrder;
import com.byyd.middleware.iface.dao.Sorting;
import com.byyd.middleware.iface.service.jpa.BaseJpaManagerImpl;
import com.byyd.middleware.utils.AdfonicBeanDispatcher;

@Service("devicesManager")
public class DeviceManagerJpaImpl extends BaseJpaManagerImpl implements DeviceManager {
    
    @Autowired(required = false)
    private DeviceGroupDao deviceGroupDao;
    
    @Autowired(required = false)
    private VendorDao vendorDao;
    
    @Autowired(required = false)
    private ModelDao modelDao;
    
    @Autowired(required = false)
    private OperatorDao operatorDao;
    
    @Autowired(required = false)
    private PlatformDao platformDao;
    
    @Autowired(required = false)
    private DisplayTypeDao displayTypeDao;
    
    @Autowired(required = false)
    private DeviceIdentifierTypeDao deviceIdentifierTypeDao;
    
    @Autowired(required = false)
    private BrowserDao browserDao;
    
    @Autowired(required = false)
    private CapabilityDao capabilityDao;
    
    
    // ------------------------------------------------------------------------------------------
    // DeviceGroup
    // ------------------------------------------------------------------------------------------
    @Override
    @Transactional(readOnly = false)
    public DeviceGroup newDeviceGroup(String systemName, String constraints, FetchStrategy... fetchStrategy) {
        DeviceGroup deviceGroup = new DeviceGroup(systemName, constraints);
        if (fetchStrategy == null || fetchStrategy.length == 0) {
            return create(deviceGroup);
        } else {
            deviceGroup = create(deviceGroup);
            return getDeviceGroupById(deviceGroup.getId(), fetchStrategy);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public DeviceGroup getDeviceGroupById(String id, FetchStrategy... fetchStrategy) {
        return getDeviceGroupById(makeLong(id), fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public DeviceGroup getDeviceGroupById(Long id, FetchStrategy... fetchStrategy) {
        return deviceGroupDao.getById(id, fetchStrategy);
    }

    @Transactional(readOnly = false)
    public DeviceGroup create(DeviceGroup deviceGroup) {
        return deviceGroupDao.create(deviceGroup);
    }

    @Override
    @Transactional(readOnly = false)
    public DeviceGroup update(DeviceGroup deviceGroup) {
        return deviceGroupDao.update(deviceGroup);
    }

    @Override
    @Transactional(readOnly = false)
    public void delete(DeviceGroup deviceGroup) {
        deviceGroupDao.delete(deviceGroup);
    }

    @Override
    @Transactional(readOnly = false)
    public void deleteDeviceGroups(List<DeviceGroup> list) {
        if (list == null || list.isEmpty()) {
            return;
        }
        for (DeviceGroup entry : list) {
            delete(entry);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public DeviceGroup getDeviceGroupBySystemName(String systemName) {
        return deviceGroupDao.getBySystemName(systemName);
    }

    @Override
    @Transactional(readOnly = true)
    public Long countAllDeviceGroups() {
        return deviceGroupDao.countAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<DeviceGroup> getAllDeviceGroups(FetchStrategy... fetchStrategy) {
        return deviceGroupDao.getAll(fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DeviceGroup> getAllDeviceGroups(Sorting sort, FetchStrategy... fetchStrategy) {
        return deviceGroupDao.getAll(sort, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DeviceGroup> getAllDeviceGroups(Pagination page, Sorting sort, FetchStrategy... fetchStrategy) {
        return deviceGroupDao.getAll(page, fetchStrategy);
    }
    
    // ------------------------------------------------------------------------------------------
    // Vendor
    // ------------------------------------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public Vendor getVendorById(String id, FetchStrategy... fetchStrategy) {
        return getVendorById(makeLong(id), fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public Vendor getVendorById(Long id, FetchStrategy... fetchStrategy) {
        return vendorDao.getById(id, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = false)
    public Vendor create(Vendor vendor) {
        return vendorDao.create(vendor);
    }

    @Override
    @Transactional(readOnly = false)
    public Vendor update(Vendor vendor) {
        return vendorDao.update(vendor);
    }

    @Override
    @Transactional(readOnly = false)
    public void delete(Vendor vendor) {
        vendorDao.delete(vendor);
    }

    @Override
    @Transactional(readOnly = false)
    public void deleteVendors(List<Vendor> list) {
        if (list == null || list.isEmpty()) {
            return;
        }
        for (Vendor vendor : list) {
            delete(vendor);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Vendor getVendorByName(String name, FetchStrategy... fetchStrategy) {
        return vendorDao.getByName(name, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public Vendor getVendorByName(String name, boolean caseSensitive, FetchStrategy... fetchStrategy) {
        return vendorDao.getByName(name, caseSensitive, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public Long countAllVendors() {
        return vendorDao.countAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Vendor> getAllVendors(FetchStrategy... fetchStrategy) {
        return vendorDao.getAll(fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Vendor> getAllVendors(Sorting sort, FetchStrategy... fetchStrategy) {
        return vendorDao.getAll(sort, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Vendor> getAllVendors(Pagination page, FetchStrategy... fetchStrategy) {
        return vendorDao.getAll(page, fetchStrategy);
    }

    // ------------------------------------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public Long countVendorsByName(String name, LikeSpec like, boolean caseSensitive) {
        return vendorDao.countAllForName(name, like, caseSensitive);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Vendor> getVendorsByName(String name, LikeSpec like, boolean caseSensitive, FetchStrategy... fetchStrategy) {
        return vendorDao.getAllForName(name, like, caseSensitive, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Vendor> getVendorsByName(String name, LikeSpec like, boolean caseSensitive, Sorting sort, FetchStrategy... fetchStrategy) {
        return vendorDao.getAllForName(name, like, caseSensitive, sort, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Vendor> getVendorsByName(String name, LikeSpec like, boolean caseSensitive, Pagination page, FetchStrategy... fetchStrategy) {
        return vendorDao.getAllForName(name, like, caseSensitive, page, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Vendor> getVendorsByPlatformAndDeviceGroup(String vendorName, List<Platform> platforms, DeviceGroup deviceGroup, FetchStrategy... fetchStrategy) {
        return vendorDao.getVendorsByPlatformAndDeviceGroup(vendorName, platforms, deviceGroup, fetchStrategy);
    }
    
    // ------------------------------------------------------------------------------------------
    // Model
    // ------------------------------------------------------------------------------------------

	@Override
	@Transactional(readOnly = false)
	public Model newModel(Vendor vendor, String modelName, String externalID, DeviceGroup deviceGroup) {
		Model model = vendor.newModel(modelName, deviceGroup);
		if (externalID != null) {
			model.setExternalID(externalID);
		}
		return modelDao.create(model);
	}

    @Override
    @Transactional(readOnly = true)
    public Model getModelById(String id, FetchStrategy... fetchStrategy) {
        return getModelById(makeLong(id), fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public Model getModelById(Long id, FetchStrategy... fetchStrategy) {
        return modelDao.getById(id, fetchStrategy);
    }

    @Transactional(readOnly = false)
    public Model create(Model model) {
        return modelDao.create(model);
    }

    @Override
    @Transactional(readOnly = false)
    public Model update(Model model) {
    	return modelDao.update(model);
    }

    @Override
    @Transactional(readOnly = false)
    public void delete(Model model) {
        modelDao.delete(model);
    }

    @Override
    @Transactional(readOnly = false)
    public void deleteModels(List<Model> list) {
        if (list == null || list.isEmpty()) {
            return;
        }
        for (Model model : list) {
            delete(model);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Model getModelByExternalId(String externalId, FetchStrategy... fetchStrategy) {
        return modelDao.getByExternalId(externalId, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public Model getModelByName(String name, FetchStrategy... fetchStrategy) {
        return getModelByName(name, false, null, null, fetchStrategy);
    }

    @Transactional(readOnly = true)
    public Model getModelByName(String name, boolean caseSensitive, FetchStrategy... fetchStrategy) {
        return getModelByName(name, caseSensitive, null, null, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public Model getModelByName(String name, boolean caseSensitive, Boolean deleted, Boolean hidden, FetchStrategy... fetchStrategy) {
        return modelDao.getModelByName(name, caseSensitive, deleted, hidden, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public Long countAllModels() {
        return modelDao.countAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Model> getAllModels(FetchStrategy... fetchStrategy) {
        return modelDao.getAll(fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Model> getAllModels(Sorting sort, FetchStrategy... fetchStrategy) {
        return modelDao.getAll(sort, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Model> getAllModels(Pagination page, FetchStrategy... fetchStrategy) {
        return modelDao.getAll(page, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public Long countAllModels(ModelFilter filter) {
        return modelDao.countAll(filter);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Model> getAllModels(ModelFilter filter, FetchStrategy... fetchStrategy) {
        return modelDao.findAll(filter, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Model> getAllModels(ModelFilter filter, Sorting sort, FetchStrategy... fetchStrategy) {
        return modelDao.findAll(filter, sort, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Model> getAllModels(ModelFilter filter, Pagination page, FetchStrategy... fetchStrategy) {
        return modelDao.findAll(filter, page, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public Long countAllModels(Boolean deleted, Boolean hidden) {
        return modelDao.countAllModels(deleted, hidden);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Model> getAllModels(Boolean deleted, Boolean hidden, FetchStrategy... fetchStrategy) {
        return modelDao.getAllModels(deleted, hidden, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Model> getAllModels(Boolean deleted, Boolean hidden, Sorting sort, FetchStrategy... fetchStrategy) {
        return modelDao.getAllModels(deleted, hidden, sort, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Model> getAllModels(Boolean deleted, Boolean hidden, Pagination page, FetchStrategy... fetchStrategy) {
        return modelDao.getAllModels(deleted, hidden, page, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public Long countModelsByName(String name, LikeSpec like, boolean caseSensitive) {
        return modelDao.countAllForName(name, like, caseSensitive);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Model> getModelsByName(String name, LikeSpec like, boolean caseSensitive, FetchStrategy... fetchStrategy) {
        return modelDao.getAllForName(name, like, caseSensitive, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Model> getModelsByName(String name, LikeSpec like, boolean caseSensitive, Sorting sort, FetchStrategy... fetchStrategy) {
        return modelDao.getAllForName(name, like, caseSensitive, sort, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Model> getModelsByName(String name, LikeSpec like, boolean caseSensitive, Pagination page, FetchStrategy... fetchStrategy) {
        return modelDao.getAllForName(name, like, caseSensitive, page, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public Long countModelsByName(String name, LikeSpec like, boolean caseSensitive, Boolean deleted, Boolean hidden, boolean prependVendorName) {
        return modelDao.countModelsByName(name, like, caseSensitive, deleted, hidden, prependVendorName);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Model> getModelsByName(String name, LikeSpec like, boolean caseSensitive, Boolean deleted, Boolean hidden, boolean prependVendorName,
            FetchStrategy... fetchStrategy) {
        return modelDao.getModelsByName(name, like, caseSensitive, deleted, hidden, prependVendorName, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Model> getModelsByName(String name, LikeSpec like, boolean caseSensitive, Boolean deleted, Boolean hidden, boolean prependVendorName, Sorting sort,
            FetchStrategy... fetchStrategy) {
        return modelDao.getModelsByName(name, like, caseSensitive, deleted, hidden, prependVendorName, sort, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Model> getModelsByName(String name, LikeSpec like, boolean caseSensitive, Boolean deleted, Boolean hidden, boolean prependVendorName, Pagination page,
            FetchStrategy... fetchStrategy) {
        return modelDao.getModelsByName(name, like, caseSensitive, deleted, hidden, prependVendorName, page, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public Long countModelsByNameAndPlatform(String name, LikeSpec like, boolean caseSensitive, Boolean deleted, Boolean hidden, boolean prependVendorName, List<Platform> platforms) {
        return modelDao.countModelsByNameAndPlatform(name, like, caseSensitive, deleted, hidden, prependVendorName, platforms);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Model> getModelsByNameAndPlatform(String name, LikeSpec like, boolean caseSensitive, Boolean deleted, Boolean hidden, boolean prependVendorName,
            List<Platform> platforms, FetchStrategy... fetchStrategy) {
        return modelDao.getModelsByNameAndPlatform(name, like, caseSensitive, deleted, hidden, prependVendorName, platforms, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Model> getModelsByNameAndPlatform(String name, LikeSpec like, boolean caseSensitive, Boolean deleted, Boolean hidden, boolean prependVendorName,
            List<Platform> platforms, Sorting sort, FetchStrategy... fetchStrategy) {
        return modelDao.getModelsByNameAndPlatform(name, like, caseSensitive, deleted, hidden, prependVendorName, platforms, sort, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Model> getModelsByNameAndPlatform(String name, LikeSpec like, boolean caseSensitive, Boolean deleted, Boolean hidden, boolean prependVendorName,
            List<Platform> platforms, Pagination page, FetchStrategy... fetchStrategy) {
        return modelDao.getModelsByNameAndPlatform(name, like, caseSensitive, deleted, hidden, prependVendorName, platforms, page, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public Long countModelsByNameAndPlatformAndDeviceGroup(String name, LikeSpec like, boolean caseSensitive, Boolean deleted, Boolean hidden, boolean prependVendorName,
            List<Platform> platforms, DeviceGroup deviceGroup) {
        return modelDao.countModelsByNameAndPlatformAndDeviceGroup(name, like, caseSensitive, deleted, hidden, prependVendorName, platforms, deviceGroup);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Model> getModelsByNameAndPlatformAndDeviceGroup(String name, LikeSpec like, boolean caseSensitive, Boolean deleted, Boolean hidden, boolean prependVendorName,
            List<Platform> platforms, DeviceGroup deviceGroup, FetchStrategy... fetchStrategy) {
        return modelDao.getModelsByNameAndPlatformAndDeviceGroup(name, like, caseSensitive, deleted, hidden, prependVendorName, platforms, deviceGroup, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Model> getModelsByNameAndPlatformAndDeviceGroup(String name, LikeSpec like, boolean caseSensitive, Boolean deleted, Boolean hidden, boolean prependVendorName,
            List<Platform> platforms, DeviceGroup deviceGroup, Sorting sort, FetchStrategy... fetchStrategy) {
        return modelDao.getModelsByNameAndPlatformAndDeviceGroup(name, like, caseSensitive, deleted, hidden, prependVendorName, platforms, deviceGroup, sort, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Model> getModelsByNameAndPlatformAndDeviceGroup(String name, LikeSpec like, boolean caseSensitive, Boolean deleted, Boolean hidden, boolean prependVendorName,
            List<Platform> platforms, DeviceGroup deviceGroup, Pagination page, FetchStrategy... fetchStrategy) {
        return modelDao.getModelsByNameAndPlatformAndDeviceGroup(name, like, caseSensitive, deleted, hidden, prependVendorName, platforms, deviceGroup, page, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Model> getModelsByVendorNameAndPlatformAndDeviceGroup(String vendorName, List<Platform> platforms, DeviceGroup deviceGroup, FetchStrategy... fetchStrategy) {
        return modelDao.getModelsByVendorNameAndPlatformAndDeviceGroup(vendorName, platforms, deviceGroup, fetchStrategy);
    }

    // ------------------------------------------------------------------------------------------
    // Operator
    // ------------------------------------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public Operator getOperatorById(String id, FetchStrategy... fetchStrategy) {
        return getOperatorById(makeLong(id), fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public Operator getOperatorById(Long id, FetchStrategy... fetchStrategy) {
        return operatorDao.getById(id, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = false)
    public Operator create(Operator operator) {
        return operatorDao.create(operator);
    }

    @Override
    @Transactional(readOnly = false)
    public Operator update(Operator operator) {
        return operatorDao.update(operator);
    }

    @Transactional(readOnly = false)
    public void delete(Operator operator) {
        operatorDao.delete(operator);
    }

    @Transactional(readOnly = false)
    public void deleteOperators(List<Operator> list) {
        if (list == null || list.isEmpty()) {
            return;
        }
        for (Operator operator : list) {
            delete(operator);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Operator getOperatorByName(String name, FetchStrategy... fetchStrategy) {
        return operatorDao.getByName(name, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public Operator getOperatorByName(String name, boolean caseSensitive, FetchStrategy... fetchStrategy) {
        return operatorDao.getByName(name, caseSensitive, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public Long countAllOperators() {
        return operatorDao.countAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Operator> getAllOperators(FetchStrategy... fetchStrategy) {
        return operatorDao.getAll(fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Operator> getAllOperators(Sorting sort, FetchStrategy... fetchStrategy) {
        return operatorDao.getAll(sort, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Operator> getAllOperators(Pagination page, FetchStrategy... fetchStrategy) {
        return operatorDao.getAll(page, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public Long countOperatorsByName(String name, LikeSpec like, boolean caseSensitive) {
        return operatorDao.countAllForName(name, like, caseSensitive);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Operator> getOperatorsByName(String name, LikeSpec like, boolean caseSensitive, FetchStrategy... fetchStrategy) {
        return operatorDao.getAllForName(name, like, caseSensitive, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Operator> getOperatorsByName(String name, LikeSpec like, boolean caseSensitive, Sorting sort, FetchStrategy... fetchStrategy) {
        return operatorDao.getAllForName(name, like, caseSensitive, sort, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Operator> getOperatorsByName(String name, LikeSpec like, boolean caseSensitive, Pagination page, FetchStrategy... fetchStrategy) {
        return operatorDao.getAllForName(name, like, caseSensitive, page, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public Long countOperatorsForName(String name, LikeSpec like, boolean caseSensitive, boolean mandateQuova) {
        return this.countOperatorsForNameAndCountries(name, like, caseSensitive, mandateQuova, null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Operator> getOperatorsForName(String name, LikeSpec like, boolean caseSensitive, boolean mandateQuova, FetchStrategy... fetchStrategy) {
        return this.getOperatorsForNameAndCountries(name, like, caseSensitive, mandateQuova, null, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Operator> getOperatorsForName(String name, LikeSpec like, boolean caseSensitive, boolean mandateQuova, Sorting sort, FetchStrategy... fetchStrategy) {
        return this.getOperatorsForNameAndCountries(name, like, caseSensitive, mandateQuova, null, sort, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Operator> getOperatorsForName(String name, LikeSpec like, boolean caseSensitive, boolean mandateQuova, Pagination page, FetchStrategy... fetchStrategy) {
        return this.getOperatorsForNameAndCountries(name, like, caseSensitive, mandateQuova, null, page, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public Long countOperatorsForNameAndCountries(String name, LikeSpec like, boolean caseSensitive, boolean mandateQuova, List<Country> countries) {
        OperatorFilter filter = new OperatorFilter(name, like, caseSensitive, mandateQuova, new Boolean(true), countries);
        return countOperators(filter);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Operator> getOperatorsForNameAndCountries(String name, LikeSpec like, boolean caseSensitive, boolean mandateQuova, List<Country> countries,
            FetchStrategy... fetchStrategy) {
        OperatorFilter filter = new OperatorFilter(name, like, caseSensitive, mandateQuova, new Boolean(true), countries);
        return getOperators(filter, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Operator> getOperatorsForNameAndCountries(String name, LikeSpec like, boolean caseSensitive, boolean mandateQuova, List<Country> countries, Sorting sort,
            FetchStrategy... fetchStrategy) {
        OperatorFilter filter = new OperatorFilter(name, like, caseSensitive, mandateQuova, new Boolean(true), countries);
        return getOperators(filter, sort, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Operator> getOperatorsForNameAndCountries(String name, LikeSpec like, boolean caseSensitive, boolean mandateQuova, List<Country> countries, Pagination page,
            FetchStrategy... fetchStrategy) {
        OperatorFilter filter = new OperatorFilter(name, like, caseSensitive, mandateQuova, new Boolean(true), countries);
        return getOperators(filter, page, fetchStrategy);
    }

    
    @Override
    @Transactional(readOnly = true)
    public Long countOperators(OperatorFilter filter) {
        return operatorDao.countOperators(filter);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Operator> getOperators(OperatorFilter filter,
            FetchStrategy... fetchStrategy) {
        return operatorDao.getOperators(filter, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Operator> getOperators(OperatorFilter filter, Sorting sort,
            FetchStrategy... fetchStrategy) {
        return operatorDao.getOperators(filter, sort, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Operator> getOperators(OperatorFilter filter, Pagination page,
            FetchStrategy... fetchStrategy) {
        return operatorDao.getOperators(filter, page, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public Operator getOperatorForOperatorAliasAndCountry(OperatorAlias.Type operatorAliasType, Country country, String alias, FetchStrategy... fetchStrategy) {
        return operatorDao.getOperatorForOperatorAliasAndCountry(operatorAliasType, country, alias, fetchStrategy);
    }

    // ------------------------------------------------------------------------------------------
    // Platform
    // ------------------------------------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public Platform getPlatformById(String id, FetchStrategy... fetchStrategy) {
        return getPlatformById(makeLong(id), fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public Platform getPlatformById(Long id, FetchStrategy... fetchStrategy) {
        return platformDao.getById(id, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = false)
    public Platform create(Platform platform) {
        return platformDao.create(platform);
    }

    @Override
    @Transactional(readOnly = false)
    public Platform update(Platform platform) {
        return platformDao.update(platform);
    }

    @Transactional(readOnly = false)
    public void delete(Platform platform) {
        platformDao.delete(platform);
    }

    @Transactional(readOnly = false)
    public void deletePlatforms(List<Platform> list) {
        if (list == null || list.isEmpty()) {
            return;
        }
        for (Platform platform : list) {
            delete(platform);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Platform getPlatformByName(String name, FetchStrategy... fetchStrategy) {
        return platformDao.getByName(name, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public Platform getPlatformByName(String name, boolean caseSensitive, FetchStrategy... fetchStrategy) {
        return platformDao.getByName(name, caseSensitive, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public Platform getPlatformBySystemName(String name, FetchStrategy... fetchStrategy) {
        return platformDao.getBySystemName(name, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public Long countAllPlatforms() {
        return platformDao.countAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Platform> getAllPlatforms(FetchStrategy... fetchStrategy) {
        return platformDao.getAll(fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Platform> getAllPlatforms(Sorting sort, FetchStrategy... fetchStrategy) {
        return platformDao.getAll(sort, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Platform> getAllPlatforms(Pagination page, FetchStrategy... fetchStrategy) {
        return platformDao.getAll(page, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public Long countPlatformsByName(String name, LikeSpec like, boolean caseSensitive) {
        return platformDao.countAllForName(name, like, caseSensitive);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Platform> getPlatformsByName(String name, LikeSpec like, boolean caseSensitive, FetchStrategy... fetchStrategy) {
        return platformDao.getAllForName(name, like, caseSensitive, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Platform> getPlatformsByName(String name, LikeSpec like, boolean caseSensitive, Sorting sort, FetchStrategy... fetchStrategy) {
        return platformDao.getAllForName(name, like, caseSensitive, sort, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Platform> getPlatformsByName(String name, LikeSpec like, boolean caseSensitive, Pagination page, FetchStrategy... fetchStrategy) {
        return platformDao.getAllForName(name, like, caseSensitive, page, fetchStrategy);
    }

    // ------------------------------------------------------------------------------------------
    // DisplayType
    // ------------------------------------------------------------------------------------------
    @Override
    @Transactional(readOnly = false)
    public DisplayType newDisplayType(String systemName, String name, String constraints) {
        DisplayType displayType = new DisplayType(systemName, name, constraints);
        return create(displayType);
    }

    @Override
    @Transactional(readOnly = true)
    public DisplayType getDisplayTypeById(String id) {
        return getDisplayTypeById(makeLong(id));
    }

    @Override
    @Transactional(readOnly = true)
    public DisplayType getDisplayTypeById(Long id) {
        return displayTypeDao.getById(id, (FetchStrategy[]) null);
    }

    @Transactional(readOnly = false)
    public DisplayType create(DisplayType displayType) {
        return displayTypeDao.create(displayType);
    }

    @Override
    @Transactional(readOnly = false)
    public DisplayType update(DisplayType displayType) {
        return displayTypeDao.update(displayType);
    }

    @Override
    @Transactional(readOnly = false)
    public void delete(DisplayType displayType) {
        displayTypeDao.delete(displayType);
    }

    @Override
    @Transactional(readOnly = false)
    public void deleteDisplayTypes(List<DisplayType> list) {
        if (list == null || list.isEmpty()) {
            return;
        }
        for (DisplayType entry : list) {
            delete(entry);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public DisplayType getDisplayTypeBySystemName(String systemName) {
        return displayTypeDao.getBySystemName(systemName);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DisplayType> getAllDisplayTypes(FetchStrategy... fetchStrategy) {
        return displayTypeDao.getAll(fetchStrategy);
    }
    
    // ------------------------------------------------------------------------------
    // DeviceIdentifierType
    // ------------------------------------------------------------------------------

    @Override
    @Transactional(readOnly = false)
    public DeviceIdentifierType create(DeviceIdentifierType deviceIdentifierType) {
        return deviceIdentifierTypeDao.create(deviceIdentifierType);
    }

    @Override
    @Transactional(readOnly = false)
    public DeviceIdentifierType update(DeviceIdentifierType deviceIdentifierType) {
        return deviceIdentifierTypeDao.update(deviceIdentifierType);
    }

    @Override
    @Transactional(readOnly = false)
    public void delete(DeviceIdentifierType deviceIdentifierType) {
        deviceIdentifierTypeDao.delete(deviceIdentifierType);
    }

    @Override
    @Transactional(readOnly = true)
    public DeviceIdentifierType getDeviceIdentifierTypeBySystemName(String systemName) {
        return deviceIdentifierTypeDao.getBySystemName(systemName);
    }

    @Transactional(readOnly = true)
    public DeviceIdentifierType getDeviceIdentifierTypeByName(String name) {
        return getDeviceIdentifierTypeByName(name, true);
    }

    @Transactional(readOnly = true)
    public DeviceIdentifierType getDeviceIdentifierTypeByName(String name, boolean caseSensitive) {
        return deviceIdentifierTypeDao.getByName(name, caseSensitive);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DeviceIdentifierType> getAllDeviceIdentifierTypes() {
        return deviceIdentifierTypeDao.getAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<DeviceIdentifierType> getAllNonHiddenDeviceIdentifierTypes() {
        return deviceIdentifierTypeDao.getAllNonHidden();
    }

    @Override
    @Transactional(readOnly = true)
    public List<DeviceIdentifierType> getUIDeviceIdentifierTypes() {
        return deviceIdentifierTypeDao.getAllNonHidden(new Sorting(SortOrder.asc("name")));
    }

    @Transactional(readOnly = true)
    public List<DeviceIdentifierType> getAllDeviceIdentifierTypes(FetchStrategy... fetchStrategy) {
        return deviceIdentifierTypeDao.getAll(fetchStrategy);
    }

    @Transactional(readOnly = true)
    public List<DeviceIdentifierType> getAllDeviceIdentifierTypes(Sorting sort, FetchStrategy... fetchStrategy) {
        return deviceIdentifierTypeDao.getAll(sort, fetchStrategy);
    }

    @Transactional(readOnly = true)
    public List<DeviceIdentifierType> getAllDeviceIdentifierTypes(Pagination page, FetchStrategy... fetchStrategy) {
        return deviceIdentifierTypeDao.getAll(page, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public DeviceIdentifierType getDeviceIdentifierTypeForPromotion(DeviceIdentifierType type) {
        if (type.getSystemName().equals(DeviceIdentifierType.SYSTEM_NAME_IFA)) {
            return this.getDeviceIdentifierTypeBySystemName(DeviceIdentifierType.SYSTEM_NAME_HIFA);
        }
        if (type.getSystemName().equals(DeviceIdentifierType.SYSTEM_NAME_UDID)) {
            return this.getDeviceIdentifierTypeBySystemName(DeviceIdentifierType.SYSTEM_NAME_DPID);
        }
        if (type.getSystemName().equals(DeviceIdentifierType.SYSTEM_NAME_ANDROID)) {
            return this.getDeviceIdentifierTypeBySystemName(DeviceIdentifierType.SYSTEM_NAME_DPID);
        }
        return type;
    }
    
    // ------------------------------------------------------------------------------------------
    // Browser
    // ------------------------------------------------------------------------------------------
    
    @Override
    @Transactional(readOnly = false)
    public Browser newBrowser(String name, FetchStrategy... fetchStrategy) {
        Browser browser = new Browser(name);
        if (fetchStrategy == null || fetchStrategy.length == 0) {
            return create(browser);
        } else {
            browser = create(browser);
            return getBrowserById(browser.getId(), fetchStrategy);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Browser getBrowserById(String id, FetchStrategy... fetchStrategy) {
        return getBrowserById(makeLong(id), fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public Browser getBrowserById(Long id, FetchStrategy... fetchStrategy) {
        return browserDao.getById(id, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = false)
    public Browser create(Browser browser) {
        return browserDao.create(browser);
    }

    @Override
    @Transactional(readOnly = false)
    public Browser update(Browser browser) {
        return browserDao.update(browser);
    }

    @Transactional(readOnly = false)
    public void delete(Browser browser) {
        browserDao.delete(browser);
    }

    @Transactional(readOnly = false)
    public void deleteBrowsers(List<Browser> list) {
        if (list == null || list.isEmpty()) {
            return;
        }
        for (Browser entry : list) {
            delete(entry);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Browser getBrowserByName(String name, FetchStrategy... fetchStrategy) {
        return browserDao.getByName(name, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public Browser getBrowserByName(String name, boolean caseSensitive, FetchStrategy... fetchStrategy) {
        return browserDao.getByName(name, caseSensitive, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public Browser getOperaBrowser(FetchStrategy... fetchStrategy) {
        return getBrowserByName(OPERA_BROWSER_NAME, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public Long countAllBrowsers() {
        return browserDao.countAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Browser> getAllBrowsers(FetchStrategy... fetchStrategy) {
        return browserDao.getAll(fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Browser> getAllBrowsers(Sorting sort, FetchStrategy... fetchStrategy) {
        return browserDao.getAll(sort, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Browser> getAllBrowsers(Pagination page, FetchStrategy... fetchStrategy) {
        return browserDao.getAll(page, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public Long countBrowsersByName(String name, LikeSpec like, boolean caseSensitive) {
        return browserDao.countAllForName(name, like, caseSensitive);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Browser> getBrowsersByName(String name, LikeSpec like, boolean caseSensitive, FetchStrategy... fetchStrategy) {
        return browserDao.getAllForName(name, like, caseSensitive, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Browser> getBrowsersByName(String name, LikeSpec like, boolean caseSensitive, Sorting sort, FetchStrategy... fetchStrategy) {
        return browserDao.getAllForName(name, like, caseSensitive, sort, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Browser> getBrowsersByName(String name, LikeSpec like, boolean caseSensitive, Pagination page, FetchStrategy... fetchStrategy) {
        return browserDao.getAllForName(name, like, caseSensitive, page, fetchStrategy);
    }

    // ------------------------------------------------------------------------------------------
    // Capability
    // ------------------------------------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public Capability getCapabilityById(String id, FetchStrategy... fetchStrategy) {
        return getCapabilityById(makeLong(id), fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public Capability getCapabilityById(Long id, FetchStrategy... fetchStrategy) {
        return capabilityDao.getById(id, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = false)
    public Capability create(Capability capability) {
        return capabilityDao.create(capability);
    }

    @Override
    @Transactional(readOnly = false)
    public Capability update(Capability capability) {
        return capabilityDao.update(capability);
    }

    @Transactional(readOnly = false)
    public void delete(Capability capability) {
        capabilityDao.delete(capability);
    }

    @Transactional(readOnly = false)
    public void deleteCapabilities(List<Capability> list) {
        if (list == null || list.isEmpty()) {
            return;
        }
        for (Capability entry : list) {
            delete(entry);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Capability getCapabilityByName(String name, FetchStrategy... fetchStrategy) {
        return capabilityDao.getByName(name, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public Capability getCapabilityByName(String name, boolean caseSensitive, FetchStrategy... fetchStrategy) {
        return capabilityDao.getByName(name, caseSensitive, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public Long countAllCapabilities() {
        return capabilityDao.countAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Capability> getAllCapabilities(FetchStrategy... fetchStrategy) {
        return capabilityDao.getAll(fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Capability> getAllCapabilities(Sorting sort, FetchStrategy... fetchStrategy) {
        return capabilityDao.getAll(sort, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Capability> getAllCapabilities(Pagination page, FetchStrategy... fetchStrategy) {
        return capabilityDao.getAll(page, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public Long countCapabilitiesByName(String name, LikeSpec like, boolean caseSensitive) {
        return capabilityDao.countAllForName(name, like, caseSensitive);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Capability> getCapabilitiesByName(String name, LikeSpec like, boolean caseSensitive, FetchStrategy... fetchStrategy) {
        return capabilityDao.getAllForName(name, like, caseSensitive, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Capability> getCapabilitiesByName(String name, LikeSpec like, boolean caseSensitive, Sorting sort, FetchStrategy... fetchStrategy) {
        return capabilityDao.getAllForName(name, like, caseSensitive, sort, fetchStrategy);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Capability> getCapabilitiesByName(String name, LikeSpec like, boolean caseSensitive, Pagination page, FetchStrategy... fetchStrategy) {
        return capabilityDao.getAllForName(name, like, caseSensitive, page, fetchStrategy);
    }
    
    // ------------------------------------------------------------------------------------------
    // DeviceTargetType High level stuff
    // ------------------------------------------------------------------------------------------
    /*
     * No more campaign type.
     * 
     * The spec calls for certain format/destination options to be presented if:
     * 
     * ALL - no platforms, vendors, or models targeted MULTIPLE - at least one
     * from apple+android and one non apple + android is targeted. This means
     * text link will appear instead of text banner.
     * 
     * IOS_ONLY targeted ANDROID_ONLY targeted IOS_ANDROID_ONLY targeted
     * 
     * OTHER_ONLY - targeted, but NO apple or android targeting thus no iab
     * 
     * We check platforms targeted -or- models + vendors targeted
     */
    @Override
    @Transactional(readOnly = true)
    public DeviceTargetType getPlatformDeviceTarget(Segment s) {

        DeviceTargetType dt = DeviceTargetType.ALL; // our default

        Set<Platform> platforms = null;
        Set<Vendor> vendors = null;
        Set<Model> models = null;

        TargetingManager targetingManager = AdfonicBeanDispatcher.getBean(TargetingManager.class);
        s = targetingManager.getSegmentById(s.getId());

        boolean appleTargeted = false;
        boolean androidTargeted = false;
        boolean otherNrimTargeted = false;
        boolean otherNwindowsTargeted = false;
        boolean otherTargeted = false;

        platforms = s.getPlatforms();
        vendors = s.getVendors();
        models = s.getModels();

        if (platforms.isEmpty() && vendors.isEmpty() && models.isEmpty()) {
            dt = DeviceTargetType.ALL;
        } else if (!platforms.isEmpty()) {
            for (Platform p : platforms) {
                if (p.getSystemName().equals(APPLE_PLATFORM_SYSTEM_NAME)) {
                    appleTargeted = true;
                } else if (p.getSystemName().equals(ANDROID_PLATFORM_SYSTEM_NAME)) {
                    androidTargeted = true;
                } else if (p.getSystemName().equals(RIM_PLATFORM_SYSTEM_NAME)) {
                    otherNrimTargeted = true;
                } else if (p.getSystemName().equals(WINDOWS_PLATFORM_SYSTEM_NAME)) {
                    otherNwindowsTargeted = true;
                } else {
                    otherTargeted = true;
                }
            }
        } else {
            // vendor targeted vs apple
            if (!vendors.isEmpty()) {
                for (Vendor vendor : vendors) {
                    if (vendor.getName().equals(APPLE_VENDOR_NAME)) {
                        appleTargeted = true;
                    } else {
                        otherTargeted = true;
                    }
                }
            }

            // model targeted vs apple, android
            if (!models.isEmpty()) {
                for (Model model : models) {
                    for (Platform p : model.getPlatforms()) {
                        if (p.getSystemName().equals(APPLE_PLATFORM_SYSTEM_NAME)) {
                            appleTargeted = true;
                        } else if (p.getSystemName().equals(ANDROID_PLATFORM_SYSTEM_NAME)) {
                            androidTargeted = true;
                        } else if (p.getSystemName().equals(RIM_PLATFORM_SYSTEM_NAME)) {
                            otherNrimTargeted = true;
                        } else if (p.getSystemName().equals(WINDOWS_PLATFORM_SYSTEM_NAME)) {
                            otherNwindowsTargeted = true;
                        } else {
                            otherTargeted = true;
                        }
                    }
                }
            }
        }

        if (otherTargeted || otherNrimTargeted || otherNwindowsTargeted) {
            if (appleTargeted || androidTargeted) {
                dt = DeviceTargetType.MULTIPLE;
            } else {
                dt = otherNrimTargeted ? DeviceTargetType.OTHER_N_RIM_ONLY : otherNwindowsTargeted ? DeviceTargetType.OTHER_N_WINDOWS_ONLY : DeviceTargetType.OTHER_ONLY;
            }
        } else if (appleTargeted && androidTargeted) {
            dt = DeviceTargetType.IOS_ANDROID_ONLY;
        } else if (appleTargeted) {
            dt = DeviceTargetType.IOS_ONLY;
        } else if (androidTargeted) {
            dt = DeviceTargetType.ANDROID_ONLY;
        }

        return dt;
    }

    /**
     * Decide whether the segment contains devices with specific platform system
     * name and/or vendor name At least platform system name or vendor parameter
     * should be set else always return false
     */
    private boolean isSegmentContainsSpecificTarget(Segment s, String platformSystemName, String vendorName) {

        Set<Platform> platforms = null;
        Set<Vendor> vendors = null;
        Set<Model> models = null;

        TargetingManager targetingManager = AdfonicBeanDispatcher.getBean(TargetingManager.class);
        s = targetingManager.getSegmentById(s.getId());

        // platform target check
        platforms = s.getPlatforms();
        if (!StringUtils.isEmpty(platformSystemName) && !platforms.isEmpty()) {
            for (Platform p : platforms) {
                if (p.getSystemName().equals(platformSystemName)) {
                    return true;
                }
            }
        } else {
            // vendor target check
            vendors = s.getVendors();
            if (!StringUtils.isEmpty(vendorName) && !vendors.isEmpty()) {
                for (Vendor vendor : vendors) {
                    if (vendor.getName().equals(vendorName)) {
                        return true;
                    }
                }
            }

            // model target check
            models = s.getModels();
            if (!StringUtils.isEmpty(platformSystemName) && !models.isEmpty()) {
                for (Model model : models) {
                    for (Platform p : model.getPlatforms()) {
                        if (p.getSystemName().equals(platformSystemName)) {
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isAppleTarget(Segment s) {
        return isSegmentContainsSpecificTarget(s, APPLE_PLATFORM_SYSTEM_NAME, APPLE_VENDOR_NAME);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isAndroidTarget(Segment s) {
        return isSegmentContainsSpecificTarget(s, ANDROID_PLATFORM_SYSTEM_NAME, null);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isAppleOnly(Segment segment) {
        return getPlatformDeviceTarget(segment) == DeviceTargetType.IOS_ONLY;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isAndroidOnly(Segment segment) {
        return getPlatformDeviceTarget(segment) == DeviceTargetType.ANDROID_ONLY;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isIOSAndroidOnly(Segment segment) {
        return getPlatformDeviceTarget(segment) == DeviceTargetType.IOS_ANDROID_ONLY;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isMultiple(Segment segment) {
        return getPlatformDeviceTarget(segment) == DeviceTargetType.MULTIPLE;
    }

    @Override
    @Transactional(readOnly = true)
    public String getPlatformDeviceTargetDescription(Segment s) {
        DeviceTargetType t = getPlatformDeviceTarget(s);
        switch (t) {
        case ANDROID_ONLY:
        case IOS_ONLY:
        case IOS_ANDROID_ONLY:
            return t.getDescription();
        default:
            // other, multiple, all map to the old General until a spec is provided
            return "General";
        }
    }


}
