package com.adfonic.domain.cache;

import static com.adfonic.domain.cache.ext.util.DbUtil.nullableLong;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import javax.sql.DataSource;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.StopWatch;

import com.adfonic.domain.BeaconMode;
import com.adfonic.domain.ContentForm;
import com.adfonic.domain.ContentSpec;
import com.adfonic.domain.Feature;
import com.adfonic.domain.MediaType;
import com.adfonic.domain.Medium;
import com.adfonic.domain.TrackingIdentifierType;
import com.adfonic.domain.cache.dto.adserver.AdserverPluginDto;
import com.adfonic.domain.cache.dto.adserver.BrowserDto;
import com.adfonic.domain.cache.dto.adserver.CapabilityDto;
import com.adfonic.domain.cache.dto.adserver.ComponentDto;
import com.adfonic.domain.cache.dto.adserver.ContentSpecDto;
import com.adfonic.domain.cache.dto.adserver.ContentTypeDto;
import com.adfonic.domain.cache.dto.adserver.CountryDto;
import com.adfonic.domain.cache.dto.adserver.DeviceIdentifierTypeDto;
import com.adfonic.domain.cache.dto.adserver.DisplayTypeDto;
import com.adfonic.domain.cache.dto.adserver.ExtendedCreativeTypeDto;
import com.adfonic.domain.cache.dto.adserver.FormatDto;
import com.adfonic.domain.cache.dto.adserver.GeotargetDto;
import com.adfonic.domain.cache.dto.adserver.GeotargetDto.Type;
import com.adfonic.domain.cache.dto.adserver.IntegrationTypeDto;
import com.adfonic.domain.cache.dto.adserver.LanguageDto;
import com.adfonic.domain.cache.dto.adserver.MobileIpAddressRangeDto;
import com.adfonic.domain.cache.dto.adserver.ModelDto;
import com.adfonic.domain.cache.dto.adserver.OperatorDto;
import com.adfonic.domain.cache.dto.adserver.PlatformDto;
import com.adfonic.domain.cache.dto.adserver.PublicationTypeDto;
import com.adfonic.domain.cache.dto.adserver.VendorDto;
import com.adfonic.util.Range;

/**
 * Domain Cache Loader: loads semi-static core domain objects
 */
public class DomainCacheLoader {

    private static final transient Logger LOG = Logger.getLogger(DomainCacheLoader.class.getName());

    private final DataSource dataSource;

    public DomainCacheLoader(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public synchronized DomainCacheImpl loadDomainCache() throws java.sql.SQLException {
        if (LOG.isLoggable(Level.INFO)) {
            LOG.info("Loading the DomainCache");
        }

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        DomainCacheImpl domainCache = new DomainCacheImpl(new Date());
        loadAdserverPlugins(domainCache);
        loadBlacklistedDeviceIdentifiers(domainCache);
        loadBrowsers(domainCache);
        loadCapabilities(domainCache);
        loadCategories(domainCache);
        loadCountries(domainCache);
        loadDeviceIdentifierTypes(domainCache);
        loadDisplayTypesAndFormats(domainCache);
        loadExtendedCreativeTypes(domainCache);
        loadGeotargets(domainCache);
        loadIntegrationTypes(domainCache);
        loadLanguages(domainCache);
        loadMccMncOperatorMappings(domainCache);
        loadMobileIpAddressRanges(domainCache);
        loadOperators(domainCache);
        loadPlatformsAndModels(domainCache);
        loadPublicationTypes(domainCache);
        loadQuovaAliasOperatorMappings(domainCache);

        stopWatch.stop();
        if (LOG.isLoggable(Level.INFO)) {
            LOG.info("Loading DomainCache took " + stopWatch);
        }

        return domainCache;
    }

    private void loadCountries(DomainCacheImpl cache) throws java.sql.SQLException {
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("Loading Countries");
        }

        Connection conn = null;
        PreparedStatement pst = null;
        ResultSet rs = null;
        try {
            conn = dataSource.getConnection();
            String sql = "SELECT ID, NAME, ISO_CODE, ISO_ALPHA3 FROM COUNTRY";
            if (LOG.isLoggable(Level.FINER)) {
                LOG.finer(sql);
            }
            pst = conn.prepareStatement(sql);
            rs = pst.executeQuery();
            while (rs.next()) {
                int idx = 1;
                CountryDto country = new CountryDto();
                country.setId(rs.getLong(idx++));
                country.setName(rs.getString(idx++));
                country.setIsoCode(rs.getString(idx++));

                cache.countriesById.put(country.getId(), country);
                cache.countriesByIsoCode.put(country.getIsoCode(), country);
                cache.countriesByIsoAlpha3.put(rs.getString(idx++), country);
                cache.countriesByLowerCaseName.put(country.getName().toLowerCase(), country);
            }
        } finally {
            DbUtils.closeQuietly(conn, pst, rs);
        }

        if (LOG.isLoggable(Level.FINER)) {
            LOG.finer("Loaded " + cache.countriesById.size() + " Countries");
        }
    }

    private void loadMobileIpAddressRanges(DomainCacheImpl cache) throws java.sql.SQLException {
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("Loading MobileIpAddressRanges");
        }

        Connection conn = null;
        PreparedStatement pst = null;
        ResultSet rs = null;
        try {
            conn = dataSource.getConnection();
            String sql = "SELECT " + "m0.ID, m0.START_POINT, m0.END_POINT, m0.OPERATOR_ID, m0.PRIORITY" + ", COUNTRY.ISO_CODE" + " FROM MOBILE_IP_ADDRESS_RANGE m0"
                    + " JOIN COUNTRY ON COUNTRY.ID=m0.COUNTRY_ID";
            if (LOG.isLoggable(Level.FINER)) {
                LOG.finer(sql);
            }
            pst = conn.prepareStatement(sql);
            rs = pst.executeQuery();
            while (rs.next()) {
                int idx = 1;
                MobileIpAddressRangeDto m = new MobileIpAddressRangeDto();
                m.setId(rs.getLong(idx++));
                m.setStartPoint(rs.getLong(idx++));
                m.setEndPoint(rs.getLong(idx++));
                m.setOperatorId(nullableLong(rs, idx++));
                m.setPriority(rs.getInt(idx++));

                String countryIsoCode = rs.getString(idx++);
                SortedSet<MobileIpAddressRangeDto> sortedSet = cache.mobileIpAddressRangesByCountryIsoCode.get(countryIsoCode);
                if (sortedSet == null) {
                    sortedSet = new TreeSet<MobileIpAddressRangeDto>();
                    cache.mobileIpAddressRangesByCountryIsoCode.put(countryIsoCode, sortedSet);
                }
                sortedSet.add(m);
            }
        } finally {
            DbUtils.closeQuietly(conn, pst, rs);
        }

        if (LOG.isLoggable(Level.FINER)) {
            LOG.finer("Loaded MobileIpAddressRanges");
        }
    }

    private void loadOperators(DomainCacheImpl cache) throws java.sql.SQLException {
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("Loading Operators");
        }

        Connection conn = null;
        PreparedStatement pst = null;
        ResultSet rs = null;
        try {
            conn = dataSource.getConnection();
            String sql = "SELECT OPERATOR.ID, OPERATOR.NAME, COUNTRY.ISO_CODE, OPERATOR.WEVE_ENABLED, OPERATOR.MOBILE_OPERATOR" + " FROM OPERATOR" + " JOIN COUNTRY ON COUNTRY.ID=OPERATOR.COUNTRY_ID";
            if (LOG.isLoggable(Level.FINER)) {
                LOG.finer(sql);
            }
            pst = conn.prepareStatement(sql);
            rs = pst.executeQuery();
            while (rs.next()) {
                int idx = 1;
                OperatorDto operator = new OperatorDto();
                operator.setId(rs.getLong(idx++));
                operator.setName(rs.getString(idx++));
                operator.setCountryIsoCode(rs.getString(idx++));
                operator.setWeveEnabled(rs.getBoolean(idx++));
                operator.setMobileOperator(rs.getBoolean(idx++));

                cache.operatorsById.put(operator.getId(), operator);
            }
        } finally {
            DbUtils.closeQuietly(conn, pst, rs);
        }

        if (LOG.isLoggable(Level.FINER)) {
            LOG.finer("Loaded " + cache.operatorsById.size() + " Operators");
        }
    }

    private void loadPlatformsAndModels(DomainCacheImpl cache) throws java.sql.SQLException {
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("Loading Platforms and Models");
        }

        Map<Long, VendorDto> vendorsById = new HashMap<Long, VendorDto>();

        Connection conn = null;
        PreparedStatement pst = null;
        ResultSet rs = null;
        try {
            conn = dataSource.getConnection();

            String sql = "SELECT ID, NAME, SYSTEM_NAME, CONSTRAINTS FROM PLATFORM";
            if (LOG.isLoggable(Level.FINER)) {
                LOG.finer(sql);
            }
            pst = conn.prepareStatement(sql);
            rs = pst.executeQuery(sql);
            while (rs.next()) {
                int idx = 1;
                PlatformDto platform = new PlatformDto();
                platform.setId(rs.getLong(idx++));
                platform.setName(rs.getString(idx++));
                platform.setSystemName(rs.getString(idx++));
                platform.setConstraints(rs.getString(idx++));

                cache.platforms.add(platform);
                cache.platformsById.put(platform.getId(), platform);
                cache.platformsBySystemName.put(platform.getSystemName(), platform);
            }
            DbUtils.closeQuietly(null, pst, rs);

            sql = "SELECT "
                    + "m0.ID AS MODEL_ID, m0.EXTERNAL_ID AS MODEL_EXTRNAL_ID, m0.NAME AS MODEL_NAME, m0.HIDDEN AS MODEL_HIDDEN, m0.DEVICE_GROUP_ID AS MODEL_DEVICE_GROUP_ID"
                    + ", v0.ID AS VENDOR_ID, v0.NAME AS VENDOR_NAME" + " FROM MODEL m0" + " JOIN VENDOR v0 ON v0.ID=m0.VENDOR_ID";
            if (LOG.isLoggable(Level.FINER)) {
                LOG.finer(sql);
            }
            pst = conn.prepareStatement(sql);
            rs = pst.executeQuery(sql);
            while (rs.next()) {
                ModelDto model = new ModelDto();
                model.setId(rs.getLong("MODEL_ID"));
                model.setExternalID(rs.getString("MODEL_EXTRNAL_ID"));
                model.setName(rs.getString("MODEL_NAME"));
                model.setHidden(rs.getBoolean("MODEL_HIDDEN"));
                model.setDeviceGroupId(rs.getLong("MODEL_DEVICE_GROUP_ID"));

                long vendorId = rs.getLong("VENDOR_ID");
                VendorDto vendor = vendorsById.get(vendorId);
                if (vendor == null) {
                    vendor = new VendorDto();
                    vendor.setId(vendorId);
                    vendor.setName(rs.getString("VENDOR_NAME"));
                    vendorsById.put(vendor.getId(), vendor);
                }
                model.setVendor(vendor);

                cache.modelsByExternalID.put(model.getExternalID(), model);
                cache.modelsById.put(model.getId(), model);
            }
            DbUtils.closeQuietly(null, pst, rs);

            sql = "SELECT MODEL_ID, PLATFORM_ID FROM MODEL_PLATFORM";
            if (LOG.isLoggable(Level.FINER)) {
                LOG.finer(sql);
            }
            pst = conn.prepareStatement(sql);
            rs = pst.executeQuery(sql);
            while (rs.next()) {
                cache.modelsById.get(rs.getLong(1)).getPlatforms().add(cache.platformsById.get(rs.getLong(2)));
            }
        } finally {
            DbUtils.closeQuietly(conn, pst, rs);
        }

        if (LOG.isLoggable(Level.FINER)) {
            LOG.finer("Loaded " + cache.platforms.size() + " Platforms and " + cache.modelsById.size() + " Models");
        }
    }

    private void loadCapabilities(DomainCacheImpl cache) throws java.sql.SQLException {
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("Loading Capabilities");
        }

        Connection conn = null;
        PreparedStatement pst = null;
        ResultSet rs = null;
        try {
            conn = dataSource.getConnection();
            String sql = "SELECT ID, NAME, CONSTRAINTS FROM CAPABILITY";
            if (LOG.isLoggable(Level.FINER)) {
                LOG.finer(sql);
            }
            pst = conn.prepareStatement(sql);
            rs = pst.executeQuery();
            while (rs.next()) {
                int idx = 1;
                CapabilityDto capability = new CapabilityDto();
                capability.setId(rs.getLong(idx++));
                capability.setName(rs.getString(idx++));
                capability.setConstraints(rs.getString(idx++));

                cache.capabilities.add(capability);
            }
        } finally {
            DbUtils.closeQuietly(conn, pst, rs);
        }

        if (LOG.isLoggable(Level.FINER)) {
            LOG.finer("Loaded " + cache.capabilities.size() + " Capabilities");
        }
    }

    private void loadCategories(DomainCacheImpl cache) throws java.sql.SQLException {
        // Query all Categories
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("Loading Categories");
        }

        Set<Long> allCategoryIds = new HashSet<Long>();
        Map<Long, Set<Long>> childIdsByParentId = new HashMap<Long, Set<Long>>();

        Connection conn = null;
        PreparedStatement pst = null;
        ResultSet rs = null;
        try {
            conn = dataSource.getConnection();

            String sql = "SELECT ID, PARENT_ID, IAB_ID FROM CATEGORY";
            if (LOG.isLoggable(Level.FINER)) {
                LOG.finer(sql);
            }
            pst = conn.prepareStatement(sql);
            rs = pst.executeQuery(sql);
            while (rs.next()) {
                long categoryId = rs.getLong(1);
                long parentId = rs.getLong(2);
                String iabId = rs.getString(3);
                allCategoryIds.add(categoryId);
                if (parentId > 0) {
                    Set<Long> siblings = childIdsByParentId.get(parentId);
                    if (siblings == null) {
                        siblings = new HashSet<Long>();
                        childIdsByParentId.put(parentId, siblings);
                    }
                    siblings.add(categoryId);
                }
                // Store the IAB mapping if an IAB category id is associated
                if (StringUtils.isNotEmpty(iabId)) {
                    cache.categoryIdsByIabId.put(iabId, categoryId);
                }
            }
        } finally {
            DbUtils.closeQuietly(conn, pst, rs);
        }

        // Build the expanded category id cache for every category
        if (LOG.isLoggable(Level.FINER)) {
            LOG.finer("Expanding Categories");
        }
        for (long categoryId : allCategoryIds) {
            Set<Long> expanded = new HashSet<Long>();
            expandCategory(categoryId, expanded, childIdsByParentId);
            if (LOG.isLoggable(Level.FINEST)) {
                LOG.finest("Category id=" + categoryId + " expanded to " + expanded.size() + " categor" + (expanded.size() == 1 ? "y" : "ies"));
            }
            cache.expandedCategoryIdsByCategoryId.put(categoryId, expanded);
        }

        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("Done Loading and Expanding categories");
        }
    }

    // Recursively walk the tree to produce a full inclusive set from
    // the given node onward.  This method is only used when first loading
    // and expanding all the categories.  After that, just use the cached map.
    private void expandCategory(long categoryId, Set<Long> expanded, Map<Long, Set<Long>> childIdsByParentId) {
        expanded.add(categoryId);
        Set<Long> childIds = childIdsByParentId.get(categoryId);
        if (childIds != null) {
            for (long childId : childIds) {
                expandCategory(childId, expanded, childIdsByParentId);
            }
        }
    }

    private void loadLanguages(DomainCacheImpl cache) throws java.sql.SQLException {
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("Loading Languages");
        }

        Connection conn = null;
        PreparedStatement pst = null;
        ResultSet rs = null;
        try {
            conn = dataSource.getConnection();
            String sql = "SELECT ID, NAME, ISO_CODE FROM LANGUAGE";
            if (LOG.isLoggable(Level.FINER)) {
                LOG.finer(sql);
            }
            pst = conn.prepareStatement(sql);
            rs = pst.executeQuery();
            while (rs.next()) {
                int idx = 1;
                LanguageDto language = new LanguageDto();
                language.setId(rs.getLong(idx++));
                language.setName(rs.getString(idx++));
                language.setISOCode(rs.getString(idx++));

                cache.languagesById.put(language.getId(), language);
                cache.languagesByIsoCode.put(language.getISOCode(), language);
            }
        } finally {
            DbUtils.closeQuietly(conn, pst, rs);
        }

        if (LOG.isLoggable(Level.FINER)) {
            LOG.finer("Loaded " + cache.languagesByIsoCode.size() + " Languages");
        }
    }

    private void loadDeviceIdentifierTypes(DomainCacheImpl cache) throws java.sql.SQLException {
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("Loading DeviceIdentifierTypes");
        }

        Connection conn = null;
        PreparedStatement pst = null;
        ResultSet rs = null;
        try {
            conn = dataSource.getConnection();

            String sql = "SELECT ID, SYSTEM_NAME, PRECEDENCE_ORDER, VALIDATION_REGEX, SECURE FROM DEVICE_IDENTIFIER_TYPE";
            if (LOG.isLoggable(Level.FINER)) {
                LOG.finer(sql);
            }
            pst = conn.prepareStatement(sql);
            rs = pst.executeQuery(sql);
            while (rs.next()) {
                int idx = 1;
                DeviceIdentifierTypeDto deviceIdentifierType = new DeviceIdentifierTypeDto();
                deviceIdentifierType.setId(rs.getLong(idx++));
                deviceIdentifierType.setSystemName(rs.getString(idx++));
                deviceIdentifierType.setPrecedenceOrder(rs.getInt(idx++));
                String validationRegex = rs.getString(idx++);
                if (StringUtils.isNotEmpty(validationRegex)) {
                    deviceIdentifierType.setValidationPattern(Pattern.compile(validationRegex));
                }
                deviceIdentifierType.setSecure(rs.getBoolean(idx++));

                cache.deviceIdentifierTypes.add(deviceIdentifierType);
                cache.deviceIdentifierTypesById.put(deviceIdentifierType.getId(), deviceIdentifierType);
                cache.deviceIdentifierTypesBySystemName.put(deviceIdentifierType.getSystemName(), deviceIdentifierType);
                cache.deviceIdentifierTypeIdsBySystemName.put(deviceIdentifierType.getSystemName(), deviceIdentifierType.getId());
            }
        } finally {
            DbUtils.closeQuietly(conn, pst, rs);
        }

        if (LOG.isLoggable(Level.FINER)) {
            LOG.finer("Loaded " + cache.deviceIdentifierTypesById.size() + " DeviceIdentifierTypes");
        }
    }

    private void loadDisplayTypesAndFormats(DomainCacheImpl cache) throws java.sql.SQLException {
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("Loading DisplayTypes");
        }

        Map<Long, DisplayTypeDto> displayTypesById = new HashMap<Long, DisplayTypeDto>();
        Map<Long, ContentTypeDto> contentTypesById = new HashMap<Long, ContentTypeDto>();
        Map<Long, ContentSpecDto> contentSpecsById = new HashMap<Long, ContentSpecDto>();
        Map<Long, ComponentDto> componentsById = new HashMap<Long, ComponentDto>();
        Map<Long, String> manifestsByContentSpecId = new HashMap<Long, String>();

        Connection conn = null;
        PreparedStatement pst = null;
        ResultSet rs = null;
        try {
            conn = dataSource.getConnection();
            String sql = "SELECT ID, NAME, SYSTEM_NAME, CONSTRAINTS FROM DISPLAY_TYPE";
            if (LOG.isLoggable(Level.FINER)) {
                LOG.finer(sql);
            }
            pst = conn.prepareStatement(sql);
            rs = pst.executeQuery();
            while (rs.next()) {
                int idx = 1;
                DisplayTypeDto displayType = new DisplayTypeDto();
                displayType.setId(rs.getLong(idx++));
                displayType.setName(rs.getString(idx++));
                displayType.setSystemName(rs.getString(idx++));
                displayType.setConstraints(rs.getString(idx++));

                displayTypesById.put(displayType.getId(), displayType);
                cache.displayTypesBySystemName.put(displayType.getSystemName(), displayType);
            }
            DbUtils.closeQuietly(null, pst, rs);

            // Load ContentTypes
            sql = "SELECT ID, NAME, MIME_TYPE, ANIMATED FROM CONTENT_TYPE";
            if (LOG.isLoggable(Level.FINER)) {
                LOG.finer(sql);
            }
            pst = conn.prepareStatement(sql);
            rs = pst.executeQuery();
            while (rs.next()) {
                ContentTypeDto contentType = ContentTypeDto.getContentType(rs.getLong(1), rs.getString(2), rs.getString(3), rs.getBoolean(4));
                contentTypesById.put(contentType.getId(), contentType);
            }
            cache.animatedContentTypes.putAll(ContentTypeDto.getContentTypes(true));
            cache.normalContentTypes.putAll(ContentTypeDto.getContentTypes(false));
            DbUtils.closeQuietly(null, pst, rs);

            // Load ContentSpecs
            sql = "SELECT ID, NAME, MANIFEST FROM CONTENT_SPEC";
            if (LOG.isLoggable(Level.FINER)) {
                LOG.finer(sql);
            }
            pst = conn.prepareStatement(sql);
            rs = pst.executeQuery();
            while (rs.next()) {
                int idx = 1;
                ContentSpecDto contentSpec = new ContentSpecDto();
                contentSpec.setId(rs.getLong(idx++));
                contentSpec.setName(rs.getString(idx++));
                // Strip off any ";maxBytes=..." since that won't match
                // what clients are passing us in "t.constraints".
                String manifest = rs.getString(idx++).replaceAll(";maxBytes=\\d+", "");
                contentSpec.getManifestProperties().putAll(ContentSpec.parseManifestProperties(manifest));
                contentSpecsById.put(contentSpec.getId(), contentSpec);
                manifestsByContentSpecId.put(contentSpec.getId(), manifest); // we'll need this below
            }
            DbUtils.closeQuietly(null, pst, rs);

            // ContentSpec.contentTypes
            sql = "SELECT CONTENT_SPEC_ID, CONTENT_TYPE_ID FROM CONTENT_SPEC_CONTENT_TYPE";
            if (LOG.isLoggable(Level.FINER)) {
                LOG.finer(sql);
            }
            pst = conn.prepareStatement(sql);
            rs = pst.executeQuery();
            while (rs.next()) {
                contentSpecsById.get(rs.getLong(1)).getContentTypes().add(contentTypesById.get(rs.getLong(2)));
            }
            DbUtils.closeQuietly(null, pst, rs);

            // Load Formats
            sql = "SELECT ID, NAME, SYSTEM_NAME FROM FORMAT";
            if (LOG.isLoggable(Level.FINER)) {
                LOG.finer(sql);
            }
            pst = conn.prepareStatement(sql);
            rs = pst.executeQuery();
            while (rs.next()) {
                int idx = 1;
                FormatDto format = new FormatDto();
                format.setId(rs.getLong(idx++));
                format.setName(rs.getString(idx++));
                format.setSystemName(rs.getString(idx++));
                cache.formatsById.put(format.getId(), format);
                cache.formatsBySystemName.put(format.getSystemName(), format);
            }
            DbUtils.closeQuietly(null, pst, rs);

            // Format.displayTypes
            sql = "SELECT FORMAT_ID, DISPLAY_TYPE_ID FROM FORMAT_DISPLAY_TYPE_LIST ORDER BY FORMAT_ID, FORMAT_ORDER DESC";
            if (LOG.isLoggable(Level.FINER)) {
                LOG.finer(sql);
            }
            pst = conn.prepareStatement(sql);
            rs = pst.executeQuery();
            while (rs.next()) {
                cache.formatsById.get(rs.getLong(1)).getDisplayTypes().add(displayTypesById.get(rs.getLong(2)));
            }
            DbUtils.closeQuietly(null, pst, rs);

            // Load Components
            sql = "SELECT ID, SYSTEM_NAME, FORMAT_ID FROM COMPONENT ORDER BY FORMAT_ID, FORMAT_ORDER";
            if (LOG.isLoggable(Level.FINER)) {
                LOG.finer(sql);
            }
            pst = conn.prepareStatement(sql);
            rs = pst.executeQuery();
            while (rs.next()) {
                int idx = 1;
                ComponentDto component = new ComponentDto();
                component.setId(rs.getLong(idx++));
                component.setSystemName(rs.getString(idx++));
                cache.formatsById.get(rs.getLong(idx++)).getComponents().add(component);
                componentsById.put(component.getId(), component);
            }
            DbUtils.closeQuietly(null, pst, rs);

            // Component.contentSpecMap
            sql = "SELECT COMPONENT_ID, DISPLAY_TYPE_ID, CONTENT_SPEC_ID FROM COMPONENT_CONTENT_SPEC_MAP";
            if (LOG.isLoggable(Level.FINER)) {
                LOG.finer(sql);
            }
            pst = conn.prepareStatement(sql);
            rs = pst.executeQuery();
            while (rs.next()) {
                ComponentDto component = componentsById.get(rs.getLong(1));
                DisplayTypeDto displayType = displayTypesById.get(rs.getLong(2));
                ContentSpecDto contentSpec = contentSpecsById.get(rs.getLong(3));
                component.getContentSpecMap().put(displayType, contentSpec);
            }
            DbUtils.closeQuietly(null, pst, rs);

            // Image format size map
            sql = "SELECT DISTINCT map.CONTENT_SPEC_ID, COMPONENT.FORMAT_ID from COMPONENT_CONTENT_SPEC_MAP map JOIN COMPONENT ON map.COMPONENT_ID=COMPONENT.ID WHERE COMPONENT.SYSTEM_NAME='image' OR COMPONENT.SYSTEM_NAME='adm' OR COMPONENT.SYSTEM_NAME='video'";
            if (LOG.isLoggable(Level.FINER)) {
                LOG.finer(sql);
            }
            pst = conn.prepareStatement(sql);
            rs = pst.executeQuery();
            while (rs.next()) {
                ContentSpecDto contentSpec = contentSpecsById.get(rs.getLong(1));
                FormatDto format = cache.formatsById.get(rs.getLong(2));
                String width = contentSpec.getManifestProperties().get("width");
                String height = contentSpec.getManifestProperties().get("height");
                if (width == null || height == null) {
                    // Not likely, but log a warning if it does happen
                    LOG.warning("Format \"" + format.getSystemName() + "\" maps to ContentSpec id=" + contentSpec.getId() + " whose manifest doesn't have both width & height: "
                            + contentSpec.getManifestProperties());
                    continue;
                }
                // Map <width>x<height> to the respective Format
                if (LOG.isLoggable(Level.FINER)) {
                    LOG.finer("Mapping image size " + width + "x" + height + " to Format: " + format.getSystemName());
                }

                Integer iWidth = Integer.parseInt(width), iHeight = Integer.parseInt(height);
                @SuppressWarnings("unchecked")
                Set<FormatDto> formats = (Set<FormatDto>) cache.boxableFormatSizeMap.get(iWidth, iHeight);
                if (formats == null) {
                    cache.boxableFormatSizeMap.put(iWidth, iHeight, formats = new HashSet<FormatDto>());
                }
                formats.add(format);
            }
        } finally {
            DbUtils.closeQuietly(conn, pst, rs);
        }

        // For every format, map each ContentSpec.manifest -> DisplayType,
        // and map all Components for each Format
        for (FormatDto format : cache.formatsById.values()) {
            Map<String, DisplayTypeDto> displayTypesByManifest = new HashMap<String, DisplayTypeDto>();
            for (ComponentDto component : format.getComponents()) {
                cache.componentsByFormatAndSystemName.put(format.getSystemName(), component.getSystemName(), component);

                for (Map.Entry<DisplayTypeDto, ContentSpecDto> entry : component.getContentSpecMap().entrySet()) {
                    DisplayTypeDto displayType = entry.getKey();
                    ContentSpecDto contentSpec = entry.getValue();
                    // NOTE: ";maxBytes=..." has already been stripped from the manifest (see above)
                    String manifest = manifestsByContentSpecId.get(contentSpec.getId());
                    displayTypesByManifest.put(manifest, displayType);
                }
            }
            cache.displayTypeMap.put(format.getSystemName(), displayTypesByManifest);
        }

        if (LOG.isLoggable(Level.FINER)) {
            LOG.finer("Loaded " + cache.displayTypesBySystemName.size() + " DisplayTypes");
        }
    }

    private void loadGeotargets(DomainCacheImpl cache) throws java.sql.SQLException {
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("Loading Geotargets");
        }

        // AF-767 - adserver doesn't need Geotarget.geotargetPoints at all,
        // since we provide adserver pre-compiled polygons.  So here we're
        // fetching all points for all polygon-based geotargets separately
        // from the primary Geotarget query.
        // Map<Long, Set<Point2D.Double>> pointsByGeotargetId = new HashMap<Long, Set<Point2D.Double>>();
        Connection conn = null;
        PreparedStatement pst = null;
        ResultSet rs = null;
        try {
            conn = dataSource.getConnection();

            String sql = "SELECT" + " g0.ID, g0.NAME, c0.ISO_CODE, gt.TYPE, g0.DISPLAY_LATITUDE, g0.DISPLAY_LONGITUDE" + " FROM GEOTARGET g0"
                    + " JOIN COUNTRY c0 ON c0.ID=g0.COUNTRY_ID" + " JOIN GEOTARGET_TYPE gt ON g0.GEOTARGET_TYPE_ID=gt.ID";
            if (LOG.isLoggable(Level.FINER)) {
                LOG.finer(sql);
            }
            pst = conn.prepareStatement(sql);
            rs = pst.executeQuery();
            while (rs.next()) {
                int idx = 1;
                GeotargetDto geotarget = new GeotargetDto();
                geotarget.setId(rs.getLong(idx++));
                geotarget.setName(rs.getString(idx++));
                geotarget.setCountryIsoCode(rs.getString(idx++));
                geotarget.setType(Type.valueOf(rs.getString(idx++)));
                geotarget.setDisplayLatitude(rs.getDouble(idx++));
                geotarget.setDisplayLongitude(rs.getDouble(idx++));

                cache.geotargets.add(geotarget);
                cache.geotargetsById.put(geotarget.getId(), geotarget);
            }
            DbUtils.closeQuietly(null, pst, rs);

            // AF-767 - adserver doesn't need Geotarget.geotargetPoints at all,
            // since we provide adserver pre-compiled polygons.  So here we're
            // fetching all points for all polygon-based geotargets separately
            // from the primary Geotarget query.
            /*
            sql = "SELECT GEOTARGET_ID, LATITUDE, LONGITUDE FROM GEOTARGET_POINT ORDER BY GEOTARGET_ID, IDX";
            if (LOG.isLoggable(Level.FINER)) {
                LOG.finer(sql);
            }
            pst = conn.prepareStatement(sql);
            rs = pst.executeQuery();
            while (rs.next()) {
                long geotargetId = rs.getLong(1);
                Set<Point2D.Double> points = pointsByGeotargetId.get(geotargetId);
                if (points == null) {
                    points = new HashSet<Point2D.Double>();
                    pointsByGeotargetId.put(geotargetId, points);
                }
                points.add(new Point2D.Double(rs.getDouble(2), rs.getDouble(3)));
            }
            */
        } finally {
            DbUtils.closeQuietly(conn, pst, rs);
        }

        // Pre-compile geotarget polygons
        /*
        for (Map.Entry<Long, Set<Point2D.Double>> entry : pointsByGeotargetId.entrySet()) {
            cache.polygonsByGeotargetId.put(entry.getKey(), compilePolygon(entry.getValue()));
        }
        */
        if (LOG.isLoggable(Level.FINER)) {
            LOG.finer("Loaded " + cache.geotargets.size() + " Geotargets");
        }
    }

    private void loadIntegrationTypes(DomainCacheImpl cache) throws java.sql.SQLException {
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("Loading IntegrationTypes");
        }

        Connection conn = null;
        PreparedStatement pst = null;
        ResultSet rs = null;
        try {
            conn = dataSource.getConnection();

            String sql = "SELECT ID, NAME, SYSTEM_NAME, PREFIX, VERSION_RANGE_START, VERSION_RANGE_END FROM INTEGRATION_TYPE";
            if (LOG.isLoggable(Level.FINER)) {
                LOG.finer(sql);
            }
            pst = conn.prepareStatement(sql);
            rs = pst.executeQuery(sql);
            while (rs.next()) {
                int idx = 1;
                IntegrationTypeDto integrationType = new IntegrationTypeDto();
                integrationType.setId(rs.getLong(idx++));
                integrationType.setName(rs.getString(idx++));
                integrationType.setSystemName(rs.getString(idx++));

                String prefix = rs.getString(idx++);
                if (prefix != null) {
                    int versionRangeStart = rs.getInt(idx++);
                    int versionRangeEnd = rs.getInt(idx++);
                    Map<Range<Integer>, IntegrationTypeDto> mapByRange = cache.integrationTypeVersionRangeMapsByPrefix.get(prefix);
                    if (mapByRange == null) {
                        mapByRange = new HashMap<Range<Integer>, IntegrationTypeDto>();
                        cache.integrationTypeVersionRangeMapsByPrefix.put(prefix, mapByRange);
                    }
                    Range<Integer> versionRange = new Range<Integer>(versionRangeStart, versionRangeEnd);
                    if (LOG.isLoggable(Level.FINER)) {
                        LOG.finer("Mapping prefix=" + prefix + ", versionRange=" + versionRange + ", IntegrationType " + integrationType.getSystemName());
                    }
                    mapByRange.put(versionRange, integrationType);
                }

                cache.integrationTypesById.put(integrationType.getId(), integrationType);
                cache.integrationTypesBySystemName.put(integrationType.getSystemName(), integrationType);
            }
            DbUtils.closeQuietly(null, pst, rs);

            String commaSeparatedIntegrationTypeIds = StringUtils.join(cache.integrationTypesById.keySet(), ',');

            // supportedFeatures
            sql = "SELECT INTEGRATION_TYPE_ID, FEATURE FROM INTEGRATION_TYPE_FEATURE WHERE INTEGRATION_TYPE_ID IN (" + commaSeparatedIntegrationTypeIds + ")";
            if (LOG.isLoggable(Level.FINER)) {
                LOG.finer(sql);
            }
            pst = conn.prepareStatement(sql);
            rs = pst.executeQuery(sql);
            while (rs.next()) {
                cache.integrationTypesById.get(rs.getLong(1)).getSupportedFeatures().add(Feature.valueOf(rs.getString(2)));
            }
            DbUtils.closeQuietly(null, pst, rs);

            // beaconModes
            sql = "SELECT INTEGRATION_TYPE_ID, BEACON_MODE FROM INTEGRATION_TYPE_BEACON_MODE WHERE INTEGRATION_TYPE_ID IN (" + commaSeparatedIntegrationTypeIds + ")";
            if (LOG.isLoggable(Level.FINER)) {
                LOG.finer(sql);
            }
            pst = conn.prepareStatement(sql);
            rs = pst.executeQuery(sql);
            while (rs.next()) {
                cache.integrationTypesById.get(rs.getLong(1)).getSupportedBeaconModes().add(BeaconMode.valueOf(rs.getString(2)));
            }
            DbUtils.closeQuietly(null, pst, rs);

            // rtb blocked vendor types
            sql = "SELECT INTEGRATION_TYPE_ID, EXTENDED_CREATIVE_TYPE_ID FROM DYNAMIC_INTEGRATION_TYPE_VENDOR_BLOCKING WHERE INTEGRATION_TYPE_ID IN ("
                    + commaSeparatedIntegrationTypeIds + ")";
            if (LOG.isLoggable(Level.FINER)) {
                LOG.finer(sql);
            }
            pst = conn.prepareStatement(sql);
            rs = pst.executeQuery(sql);
            while (rs.next()) {
                cache.integrationTypesById.get(rs.getLong(1)).getBlockedExtendedCreativeTypes().add(cache.extendedCreativeTypesById.get(rs.getLong(2)).getName());
            }
            DbUtils.closeQuietly(null, pst, rs);

            // contentFormsByMediaType
            sql = "SELECT i0.INTEGRATION_TYPE_ID, i0.MEDIA_TYPE, i1.CONTENT_FORM" + " FROM INTEGRATION_TYPE_MEDIA_TYPE i0"
                    + " JOIN INTEGRATION_TYPE_MEDIA_FORM_MAP i1 ON i1.INTEGRATION_TYPE_MEDIA_TYPE_ID=i0.ID" + " WHERE i0.INTEGRATION_TYPE_ID IN ("
                    + commaSeparatedIntegrationTypeIds + ")";
            if (LOG.isLoggable(Level.FINER)) {
                LOG.finer(sql);
            }
            pst = conn.prepareStatement(sql);
            rs = pst.executeQuery(sql);
            while (rs.next()) {
                cache.integrationTypesById.get(rs.getLong(1)).addSupportedContentForm(MediaType.valueOf(rs.getString(2)), ContentForm.valueOf(rs.getString(3)));
            }
        } finally {
            DbUtils.closeQuietly(conn, pst, rs);
        }

        if (LOG.isLoggable(Level.FINER)) {
            LOG.finer("Loaded " + cache.integrationTypesBySystemName.size() + " IntegrationTypes");
        }
    }

    private void loadMccMncOperatorMappings(DomainCacheImpl cache) throws java.sql.SQLException {
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("Loading MCC+MNC to Operator mappings");
        }
        Connection conn = null;
        PreparedStatement pst = null;
        ResultSet rs = null;
        try {
            conn = dataSource.getConnection();
            String sql = "SELECT MCC, MNC, OPERATOR_ID FROM MOBILE_NETWORK";
            if (LOG.isLoggable(Level.FINER)) {
                LOG.finer(sql);
            }
            pst = conn.prepareStatement(sql);
            rs = pst.executeQuery();
            while (rs.next()) {
                cache.operatorIdsByMccMnc.put(rs.getString(1) + rs.getString(2), rs.getLong(3));
            }
        } finally {
            DbUtils.closeQuietly(conn, pst, rs);
        }
        if (LOG.isLoggable(Level.FINER)) {
            LOG.finer("Loaded " + cache.operatorIdsByMccMnc.size() + " MCC+MNC -> Operator mappings");
        }
    }

    private void loadQuovaAliasOperatorMappings(DomainCacheImpl cache) throws java.sql.SQLException {
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("Loading Country + Quova alias to Operator mappings");
        }
        Connection conn = null;
        PreparedStatement pst = null;
        ResultSet rs = null;
        try {
            conn = dataSource.getConnection();
            String sql = "SELECT o.COUNTRY_ID, a.ALIAS, o.ID FROM OPERATOR o INNER JOIN OPERATOR_ALIAS a ON a.OPERATOR_ID=o.ID WHERE a.TYPE='QUOVA'";
            if (LOG.isLoggable(Level.FINER)) {
                LOG.finer(sql);
            }
            pst = conn.prepareStatement(sql);
            rs = pst.executeQuery();
            while (rs.next()) {
                cache.operatorIdsByCountryIdAndQuovaAlias.put(rs.getLong(1), rs.getString(2), rs.getLong(3));
            }
        } finally {
            DbUtils.closeQuietly(conn, pst, rs);
        }
        if (LOG.isLoggable(Level.FINER)) {
            LOG.finer("Loaded " + cache.operatorIdsByCountryIdAndQuovaAlias.size() + " Country + Quova alias -> Operator mappings");
        }
    }

    private void loadAdserverPlugins(DomainCacheImpl cache) throws java.sql.SQLException {
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("Loading AdserverPlugins");
        }

        Map<Long, AdserverPluginDto> adserverPluginsById = new HashMap<Long, AdserverPluginDto>();

        Connection conn = null;
        PreparedStatement pst = null;
        ResultSet rs = null;
        try {
            conn = dataSource.getConnection();
            String sql = "SELECT ID, SYSTEM_NAME, ENABLED, EXPECTED_RESPONSE_TIME_MILLIS FROM ADSERVER_PLUGIN";
            if (LOG.isLoggable(Level.FINER)) {
                LOG.finer(sql);
            }
            pst = conn.prepareStatement(sql);
            rs = pst.executeQuery();
            while (rs.next()) {
                int idx = 1;
                AdserverPluginDto adserverPlugin = new AdserverPluginDto();
                adserverPlugin.setId(rs.getLong(idx++));
                adserverPlugin.setSystemName(rs.getString(idx++));
                adserverPlugin.setEnabled(rs.getBoolean(idx++));
                adserverPlugin.setExpectedResponseTimeMillis(rs.getLong(idx++));

                adserverPluginsById.put(adserverPlugin.getId(), adserverPlugin);
                cache.adserverPluginsBySystemName.put(adserverPlugin.getSystemName(), adserverPlugin);
            }
            DbUtils.closeQuietly(null, pst, rs);

            sql = "SELECT ADSERVER_PLUGIN_ID, NAME, VALUE FROM ADSERVER_PLUGIN_PROPERTY";
            if (LOG.isLoggable(Level.FINER)) {
                LOG.finer(sql);
            }
            pst = conn.prepareStatement(sql);
            rs = pst.executeQuery();
            while (rs.next()) {
                adserverPluginsById.get(rs.getLong(1)).setProperty(rs.getString(2), rs.getString(3));
            }
        } finally {
            DbUtils.closeQuietly(conn, pst, rs);
        }

        if (LOG.isLoggable(Level.FINER)) {
            LOG.finer("Loaded " + cache.adserverPluginsBySystemName.size() + " AdserverPlugins");
        }
    }

    private void loadBlacklistedDeviceIdentifiers(DomainCacheImpl cache) throws java.sql.SQLException {
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("Loading Blacklisted Device Identifiers");
        }

        Connection conn = null;
        PreparedStatement pst = null;
        ResultSet rs = null;
        int count = 0;
        try {
            conn = dataSource.getConnection();
            String sql = "SELECT DEVICE_IDENTIFIER_TYPE_ID, DEVICE_IDENTIFIER FROM BLACKLISTED_DEVICE_IDENTIFIER";
            if (LOG.isLoggable(Level.FINER)) {
                LOG.finer(sql);
            }
            pst = conn.prepareStatement(sql);
            rs = pst.executeQuery();
            while (rs.next()) {
                long deviceIdentifierTypeId = rs.getLong(1);
                Set<String> blacklistedDeviceIdentifiers = cache.blacklistedDeviceIdentifiersByType.get(deviceIdentifierTypeId);
                if (blacklistedDeviceIdentifiers == null) {
                    blacklistedDeviceIdentifiers = new HashSet<String>();
                    cache.blacklistedDeviceIdentifiersByType.put(deviceIdentifierTypeId, blacklistedDeviceIdentifiers);
                }
                blacklistedDeviceIdentifiers.add(rs.getString(2));
                ++count;
            }
        } finally {
            DbUtils.closeQuietly(conn, pst, rs);
        }

        if (LOG.isLoggable(Level.FINER)) {
            LOG.finer("Loaded " + count + " Blacklisted Device Identifier(s)");
        }
    }

    private void loadExtendedCreativeTypes(DomainCacheImpl cache) throws java.sql.SQLException {
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("Loading ExtendedCreativeTypes");
        }

        Connection conn = null;
        PreparedStatement pst = null;
        ResultSet rs = null;
        try {
            conn = dataSource.getConnection();

            String sql = "SELECT ID, NAME, MEDIA_TYPE, CLICK_REDIRECT_REQUIRED, USE_DYNAMIC_TEMPLATES FROM EXTENDED_CREATIVE_TYPE";
            if (LOG.isLoggable(Level.FINER)) {
                LOG.finer(sql);
            }
            pst = conn.prepareStatement(sql);
            rs = pst.executeQuery(sql);
            while (rs.next()) {
                int idx = 1;
                ExtendedCreativeTypeDto e = new ExtendedCreativeTypeDto();
                e.setId(rs.getLong(idx++));
                e.setName(rs.getString(idx++));
                e.setMediaType(MediaType.valueOf(rs.getString(idx++)));
                e.setClickRedirectRequired(rs.getBoolean(idx++));
                e.setUseDynamicTemplates(rs.getBoolean(idx++));
                cache.extendedCreativeTypesById.put(e.getId(), e);
            }
            DbUtils.closeQuietly(null, pst, rs);

            // features
            sql = "SELECT EXTENDED_CREATIVE_TYPE_ID, FEATURE FROM EXTENDED_CREATIVE_TYPE_FEATURE";
            if (LOG.isLoggable(Level.FINER)) {
                LOG.finer(sql);
            }
            pst = conn.prepareStatement(sql);
            rs = pst.executeQuery(sql);
            while (rs.next()) {
                cache.extendedCreativeTypesById.get(rs.getLong(1)).getFeatures().add(Feature.valueOf(rs.getString(2)));
            }
            DbUtils.closeQuietly(null, pst, rs);

            // templateMap
            sql = "SELECT EXTENDED_CREATIVE_TYPE_ID, CONTENT_FORM, TEMPLATE FROM EXTENDED_CREATIVE_TYPE_TEMPLATE_MAP";
            if (LOG.isLoggable(Level.FINER)) {
                LOG.finer(sql);
            }
            pst = conn.prepareStatement(sql);
            rs = pst.executeQuery(sql);
            while (rs.next()) {
                cache.extendedCreativeTypesById.get(rs.getLong(1)).getTemplateMap().put(ContentForm.valueOf(rs.getString(2)), rs.getString(3));
            }
        } finally {
            DbUtils.closeQuietly(conn, pst, rs);
        }

        if (LOG.isLoggable(Level.FINER)) {
            LOG.finer("Loaded " + cache.extendedCreativeTypesById.size() + " ExtendedCreativeTypes");
        }
    }

    private void loadPublicationTypes(DomainCacheImpl cache) throws java.sql.SQLException {
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("Loading PublicationTypes");
        }

        Connection conn = null;
        PreparedStatement pst = null;
        ResultSet rs = null;
        try {
            conn = dataSource.getConnection();

            String sql = "SELECT ID, SYSTEM_NAME, MEDIUM, DEFAULT_TRACKING_IDENTIFIER_TYPE, DEFAULT_INTEGRATION_TYPE_ID FROM PUBLICATION_TYPE";
            if (LOG.isLoggable(Level.FINER)) {
                LOG.finer(sql);
            }
            pst = conn.prepareStatement(sql);
            rs = pst.executeQuery(sql);
            while (rs.next()) {
                int idx = 1;
                PublicationTypeDto publicationType = new PublicationTypeDto();
                publicationType.setId(rs.getLong(idx++));
                publicationType.setSystemName(rs.getString(idx++));
                publicationType.setMedium(Medium.valueOf(rs.getString(idx++)));
                publicationType.setDefaultTrackingIdentifierType(TrackingIdentifierType.valueOf(rs.getString(idx++)));
                publicationType.setDefaultIntegrationTypeId(nullableLong(rs, idx++));

                cache.publicationTypesById.put(publicationType.getId(), publicationType);
            }
        } finally {
            DbUtils.closeQuietly(conn, pst, rs);
        }

        if (LOG.isLoggable(Level.FINER)) {
            LOG.finer("Loaded " + cache.publicationTypesById.size() + " PublicationTypes");
        }
    }

    private void loadBrowsers(DomainCacheImpl cache) throws java.sql.SQLException {
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("Loading Browsers");
        }

        Connection conn = null;
        PreparedStatement pst = null;
        ResultSet rs = null;
        try {
            conn = dataSource.getConnection();
            // This query grabs both BROWSER and BROWSER_HEADER_MAP in one fell swoop.
            // There will be one row per browser per header.  Browsers with no headers
            // will return just the one row.  Browsers with multiple headers will
            // return multiple rows.  This is deliberate to consolidate queries.
            String sql = "SELECT b0.ID, b0.NAME, h0.HEADER, h0.VALUE" + " FROM BROWSER b0" + " LEFT OUTER JOIN BROWSER_HEADER_MAP h0 ON h0.BROWSER_ID=b0.ID";
            if (LOG.isLoggable(Level.FINER)) {
                LOG.finer(sql);
            }
            pst = conn.prepareStatement(sql);
            rs = pst.executeQuery();
            while (rs.next()) {
                long browserId = rs.getLong(1);
                BrowserDto browser = cache.browsersById.get(browserId);
                if (browser == null) {
                    browser = new BrowserDto();
                    browser.setId(browserId);
                    browser.setName(rs.getString(2));
                    cache.browsersById.put(browser.getId(), browser);
                }

                String header = rs.getString(3);
                String value = rs.getString(4);
                if (header != null) {
                    browser.getHeaderPatternMap().put(header, Pattern.compile(value, Pattern.CASE_INSENSITIVE));
                }
            }
        } finally {
            DbUtils.closeQuietly(conn, pst, rs);
        }

        if (LOG.isLoggable(Level.FINER)) {
            LOG.finer("Loaded " + cache.browsersById.size() + " Browsers");
        }
    }
}
