package com.adfonic.tracker.jdbc;

import java.sql.ResultSet;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.transaction.annotation.Transactional;

import com.adfonic.adserver.Click;
import com.adfonic.adserver.Impression;
import com.adfonic.domain.Gender;
import com.adfonic.tracker.ClickService;
import com.adfonic.tracker.ConversionService;
import com.adfonic.tracker.InstallService;
import com.adfonic.tracker.PendingAuthenticatedInstall;
import com.adfonic.tracker.PendingConversion;
import com.adfonic.tracker.PendingInstall;
import com.adfonic.tracker.PendingVideoView;
import com.adfonic.tracker.VideoViewService;
import com.adfonic.util.Range;

/**
 * Beware! This beauty is used in DataCollector, CombinedTasks and Tracker through ClickService interface
 *
 */
public class TrackerMultiServiceJdbcImpl implements ClickService, InstallService, ConversionService, VideoViewService {
    private static final transient Logger LOG = LoggerFactory.getLogger(TrackerMultiServiceJdbcImpl.class.getName());

    private static final short TOKEN_LEN_4 = 4;
    private static final short TOKEN_LEN_8 = 8;
    private static final short TOKEN_LEN_12 = 12;
    private static final short TOTAL_TOKEN_LEN = 32;
    private static final short TOTAL_TOKEN_LEN_WITH_DASH = 36;

    private static final String CLICK_EXTERNAL_ID = "click_external_id";
    private static final String CREATION_TIME = "creation_time";
    private static final String CLAIM = "claim";
    private static final String DEVICE_IDENTIFIER = "device_identifier";
    private static final String DEVICE_IDENTIFIER_TYPE_ID = "device_identifier_type_id";
    private static final String APP_ID = "app_id";
    private static final String CLIP_MS = "clip_ms";
    private static final String VIEW_MS = "view_ms";

    private static final int RESOLVE_UA_HEADER_MAX_TRIES = 10;
    private static final int UA_HEADER_CACHE_TTL_SEC = 3600;

    private final DataSource dataSource;
    private final JdbcTemplate jdbcTemplate;
    private Ehcache uaHeaderIdCache;

    public TrackerMultiServiceJdbcImpl(DataSource dataSource) {
        this.dataSource = dataSource;
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public Ehcache getUaHeaderIdCache() {
        return uaHeaderIdCache;
    }

    public void setUaHeaderIdCache(Ehcache uaHeaderIdCache) {
        this.uaHeaderIdCache = uaHeaderIdCache;
    }

    /** {@inheritDoc} */
    @Override
    public boolean trackClick(Impression impression, String applicationIdForInstallTracking, Date creationTime, Date expireTime, String ipAddress, String userAgentHeader) {
        // Create the click object that we'll be saving in the db
        Click click = new Click(impression, creationTime, expireTime, ipAddress, userAgentHeader);

        try {
            // Resolve the ua_header_id from the User-Agent header
            long uaHeaderId = resolveUaHeaderId(click.getUserAgentHeader());
            LOG.debug("Resolved uaHeaderId={}", uaHeaderId);

            LOG.debug("Saving click record for {}, expireTime={}", click.getExternalID(), click.getExpireTime());
            jdbcTemplate
                    .update("INSERT INTO click (external_id, creation_time, expire_time, test_mode, tracking_identifier, ad_space_id, creative_id, model_id, country_id, operator_id, age_range_min, age_range_max, gender, geotarget_id, integration_type_id, ip_address, ua_header_id, rtb_settlement_price, postal_code_id, rtb_bid_price, hostname, user_time_zone_id, strategy, date_of_birth, latitude, longitude, location_source) VALUES (UNHEX(REPLACE(?,'-','')),?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
                            click.getExternalID(), click.getCreationTime(), click.getExpireTime(), click.isTestMode(), click.getTrackingIdentifier(), click.getAdSpaceId(), click
                                    .getCreativeId(), click.getModelId(), click.getCountryId(), click.getOperatorId(), click.getAgeRange() == null ? null : click.getAgeRange()
                                    .getStart(), click.getAgeRange() == null ? null : click.getAgeRange().getEnd(), nullableName(click.getGender()), click.getGeotargetId(), click
                                    .getIntegrationTypeId(), click.getIpAddress(), uaHeaderId, click.getRtbSettlementPrice(), click.getPostalCodeId(), click.getRtbBidPrice(),
                            click.getHost(), click.getUserTimeZoneId(), click.getStrategy(), click.getDateOfBirth(), click.getLatitude(), click.getLongitude(), click
                                    .getLocationSource());
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            LOG.info("Duplicate click detected, returning false for {}", click.getExternalID());
            return false;
        } catch (Exception e) {
            LOG.error("Failed to insert: {} {}", click, e);
            return false; // don't log the event
        }

        // When supplied, save device identifiers with every click
        if (MapUtils.isNotEmpty(click.getDeviceIdentifiers())) {
            for (Map.Entry<Long, String> entry : click.getDeviceIdentifiers().entrySet()) {
                long deviceIdentifierTypeId = entry.getKey();
                String deviceIdentifier = entry.getValue();
                saveClickDeviceIdentifier(click, deviceIdentifierTypeId, deviceIdentifier);
            }
        } else if (StringUtils.isNotBlank(applicationIdForInstallTracking) && StringUtils.isNotBlank(click.getTrackingIdentifier())) {
            // remove this eventually, this is just a fallback to ensure that we're storing
            // click.trackingIdentifier as the "dpid" device identifier like we have been in the
            // past...until publishers have been fully migrated over to the new d.* methodology.
            saveClickDeviceIdentifier(click, 1, click.getTrackingIdentifier()); // id=1 for "dpid"
        }

        // See if we need to track installs for this creative...if both the applicationId and
        // at least one device identifier have been supplied, then we'll provision for install tracking
        if (StringUtils.isNotBlank(applicationIdForInstallTracking) && MapUtils.isNotEmpty(click.getDeviceIdentifiers())) {
            // Create a click lookup for each device identifier
            for (Map.Entry<Long, String> entry : click.getDeviceIdentifiers().entrySet()) {
                long deviceIdentifierTypeId = entry.getKey();
                String deviceIdentifier = entry.getValue();
                saveAppClickLookup(click, applicationIdForInstallTracking, deviceIdentifierTypeId, deviceIdentifier);
            }
        } else if (StringUtils.isNotBlank(applicationIdForInstallTracking) && StringUtils.isNotBlank(click.getTrackingIdentifier())) {
            // remove this eventually, this is just a fallback to ensure that we're storing
            // click.trackingIdentifier as the "dpid" device identifier like we have been in the
            // past...until publishers have been fully migrated over to the new d.* methodology.
            saveAppClickLookup(click, applicationIdForInstallTracking, 1, click.getTrackingIdentifier()); // id=1 for "dpid"
        }

        return true;
    }

    private void saveClickDeviceIdentifier(Click click, long deviceIdentifierTypeId, String deviceIdentifier) {
        try {
            jdbcTemplate.update(
                    "INSERT INTO click_device_identifier (click_external_id, device_identifier_type_id, device_identifier, expire_time) VALUES (UNHEX(REPLACE(?,'-','')),?,?,?)",
                    click.getExternalID(), deviceIdentifierTypeId, deviceIdentifier.toLowerCase(), // store lowercase since we'll query it that way later
                    click.getExpireTime());
        } catch (Exception e) {
            LOG.error("Failed to create click_device_identifier entry for deviceIdentifierTypeId={}, deviceIdentifier={}, click={}", deviceIdentifierTypeId, deviceIdentifier,
                    click, e);
        }
    }

    private void saveAppClickLookup(Click click, String applicationIdForInstallTracking, long deviceIdentifierTypeId, String deviceIdentifier) {
        try {
            jdbcTemplate
                    .update("REPLACE INTO app_click_lookup (app_id, device_identifier_type_id, device_identifier, click_external_id, expire_time) VALUES (?,?,?,UNHEX(REPLACE(?,'-','')),?)",
                            applicationIdForInstallTracking, deviceIdentifierTypeId, deviceIdentifier.toLowerCase(), // store lowercase since we'll query it that way later
                            click.getExternalID(), click.getExpireTime());
        } catch (Exception e) {
            // This should theoretically never happen, since we're doing
            // a REPLACE and not an INSERT, but some other database issue
            // might pop up...and if it does, we don't want to bail on this
            // method completely.  Just log severe and proceed.
            LOG.error("Failed to create app_click_lookup entry for deviceIdentifierTypeId={}, deviceIdentifier={}, click={}", deviceIdentifierTypeId, deviceIdentifier, click, e);
        }
    }

    /** {@inheritDoc} */
    @Override
    public Click getClickByExternalID(String externalID) {
        try {
            return jdbcTemplate.queryForObject(ClickRowMapper.getSelectBase("c") + " FROM " + ClickRowMapper.getFrom("c") + " WHERE c.external_id=UNHEX(REPLACE(?,'-',''))",
                    ClickRowMapper.INSTANCE, externalID);
        } catch (org.springframework.dao.EmptyResultDataAccessException e) {
            return null;
        }
    }

    /** {@inheritDoc} */
    @Override
    public Click getClickByAppIdAndDeviceIdentifier(String appId, long deviceIdentifierTypeId, String deviceIdentifier) {
        LOG.debug("Querying for click by appId={}, deviceIdentifierTypeId={}, deviceIdentifier={}", appId, deviceIdentifierTypeId, deviceIdentifier);
        try {
            return jdbcTemplate.queryForObject(ClickRowMapper.getSelectBase("c") + " FROM " + ClickRowMapper.getFrom("c")
                    + " INNER JOIN app_click_lookup ON app_click_lookup.click_external_id = c.external_id" + " WHERE app_click_lookup.app_id=?"
                    + " AND app_click_lookup.device_identifier_type_id=?" + " AND app_click_lookup.device_identifier=?", ClickRowMapper.INSTANCE, appId, deviceIdentifierTypeId,
                    deviceIdentifier.toLowerCase());
        } catch (org.springframework.dao.EmptyResultDataAccessException e) {
            return null;
        }
    }

    /** {@inheritDoc} */
    @Override
    public Click getClick(PendingAuthenticatedInstall pendingAuthenticatedInstall) {
        return getClickByExternalID(pendingAuthenticatedInstall.getClickExternalID());
    }

    /** {@inheritDoc} */
    @Override
    public Click getClick(PendingConversion pendingConversion) {
        return getClickByExternalID(pendingConversion.getClickExternalID());
    }

    /** {@inheritDoc} */
    @Override
    public Click getClick(PendingInstall pendingInstall) {
        return getClickByAppIdAndDeviceIdentifier(pendingInstall.getApplicationId(), pendingInstall.getDeviceIdentifierTypeId(), pendingInstall.getDeviceIdentifier());
    }

    /** {@inheritDoc} */
    @Override
    public Click getClick(PendingVideoView pendingVideoView) {
        return getClickByExternalID(pendingVideoView.getClickExternalID());
    }

    /** {@inheritDoc} */
    @Override
    public void loadDeviceIdentifiers(Click click) {
        LOG.debug("Loading device identifiers for click externalID={}", click.getExternalID());
        final Map<Long, String> deviceIdentifiers = new LinkedHashMap<Long, String>();
        jdbcTemplate.query("SELECT device_identifier_type_id, device_identifier FROM click_device_identifier WHERE click_external_id=UNHEX(REPLACE(?,'-',''))",
                new RowCallbackHandler() {
                    @Override
                    public void processRow(ResultSet rs) throws java.sql.SQLException {
                        deviceIdentifiers.put(rs.getLong("device_identifier_type_id"), rs.getString("device_identifier"));
                    }
                }, click.getExternalID());
        click.setDeviceIdentifiers(deviceIdentifiers);
    }

    /** {@inheritDoc} */
    @Override
    @Transactional
    public boolean trackInstall(Click click) {
        try {
            if (!click.isTracked()) {
                jdbcTemplate.update("INSERT INTO install (click_external_id, expire_time) VALUES (UNHEX(REPLACE(?,'-','')),?)", click.getExternalID(), click.getExpireTime());
                //update the click table
                updateClick(click);
                return true; // this was the first install, should log it
            } else {
                LOG.info("Click is already tracked. {}", click.getExternalID());
                return false; // this click has already been tracked before. Don't track the install for this click.
            }
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            LOG.info("Duplicate install detected for click {}", click.getExternalID());
            return false; // this was a duplicate, don't log it
        }
    }

    private void updateClick(Click click) {
        //update the click table
        jdbcTemplate.update("UPDATE click set tracked = ?, tracked_at = CURRENT_TIMESTAMP()" + " WHERE external_id = UNHEX(REPLACE(?,'-',''))", true, // click is now tracked
                click.getExternalID());
    }

    /** {@inheritDoc} */
    @Override
    public void scheduleInstallRetry(String applicationId, long deviceIdentifierTypeId, String deviceIdentifier, boolean claim) {
        try {
            jdbcTemplate.update("{CALL schedule_install_retry_with_claim(?,?,?,?)}", applicationId, deviceIdentifierTypeId, deviceIdentifier, claim);
        } catch (org.springframework.dao.DataAccessException e) {
            LOG.error("Failed to schedule retry of install for applicationId={}, deviceIdentifierTypeId={}, deviceIdentifier={}, claim={}", applicationId, deviceIdentifierTypeId,
                    deviceIdentifier, claim, e);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void scheduleRetry(PendingInstall pendingInstall) {
        scheduleInstallRetry(pendingInstall.getApplicationId(), pendingInstall.getDeviceIdentifierTypeId(), pendingInstall.getDeviceIdentifier(), pendingInstall.isClaim());
    }

    /** {@inheritDoc} */
    @Override
    public void deleteScheduledInstallRetry(PendingInstall pendingInstall) {
        try {
            jdbcTemplate.update("DELETE FROM pending_install WHERE app_id=? AND device_identifier_type_id=? AND device_identifier=?", pendingInstall.getApplicationId(),
                    pendingInstall.getDeviceIdentifierTypeId(), pendingInstall.getDeviceIdentifier());
        } catch (org.springframework.dao.DataAccessException e) {
            LOG.error("Failed to delete scheduled retry of install for applicationId={}, deviceIdentifierTypeId={}, deviceIdentifier={}", pendingInstall.getApplicationId(),
                    pendingInstall.getDeviceIdentifierTypeId(), pendingInstall.getDeviceIdentifier(), e);
        }
    }

    /** {@inheritDoc} */
    @Override
    public List<PendingInstall> getPendingInstallsToRetry(int maxRows) {
        JdbcTemplate tmpTemplate = new JdbcTemplate(dataSource);
        if (maxRows > 0) {
            tmpTemplate.setMaxRows(maxRows);
        }
        return tmpTemplate
                .query("SELECT creation_time, app_id, device_identifier_type_id, device_identifier, claim FROM pending_install WHERE next_retry_time <= CURRENT_TIMESTAMP ORDER BY next_retry_time ASC",
                        PendingInstallImpl.ROW_MAPPER);
    }

    /** {@inheritDoc} */
    @Override
    public void scheduleAuthenticatedInstallRetry(String clickExternalID) {
        try {
            jdbcTemplate.update("{CALL schedule_authenticated_install_retry(?)}", clickExternalID);
        } catch (org.springframework.dao.DataAccessException e) {
            LOG.error("Failed to schedule retry of authenticated install for clickExternalID={}", clickExternalID, e);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void scheduleRetry(PendingAuthenticatedInstall pendingAuthenticatedInstall) {
        scheduleAuthenticatedInstallRetry(pendingAuthenticatedInstall.getClickExternalID());
    }

    /** {@inheritDoc} */
    @Override
    public void deleteScheduledAuthenticatedInstallRetry(PendingAuthenticatedInstall pendingAuthenticatedInstall) {
        try {
            jdbcTemplate.update("DELETE FROM pending_authenticated_install WHERE click_external_id=?", pendingAuthenticatedInstall.getClickExternalID());
        } catch (org.springframework.dao.DataAccessException e) {
            LOG.error("Failed to delete scheduled retry of authenticated install for clickExternalID={}", pendingAuthenticatedInstall.getClickExternalID(), e);
        }
    }

    /** {@inheritDoc} */
    @Override
    public List<PendingAuthenticatedInstall> getPendingAuthenticatedInstallsToRetry(int maxRows) {
        JdbcTemplate tmpTemplate = new JdbcTemplate(dataSource);
        if (maxRows > 0) {
            tmpTemplate.setMaxRows(maxRows);
        }
        return tmpTemplate.query(
                "SELECT creation_time, click_external_id FROM pending_authenticated_install WHERE next_retry_time <= CURRENT_TIMESTAMP ORDER BY next_retry_time ASC",
                PendingAuthenticatedInstallImpl.ROW_MAPPER);
    }

    /** {@inheritDoc} */
    @Override
    @Transactional
    public boolean trackConversion(Click click) {
        try {
            if (!click.isTracked()) {
                jdbcTemplate.update("INSERT INTO conversion (click_external_id, expire_time)" + " VALUES (UNHEX(REPLACE(?,'-','')),?)", click.getExternalID(),
                        click.getExpireTime());
                updateClick(click);
                return true; // this was the first conversion, should log it
            } else {
                LOG.info("Click is already tracked. {}", click.getExternalID());
                return false; // this click has already been tracked before. Don't track the conversion for this click.
            }
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            LOG.info("Duplicate conversion detected for click {}", click.getExternalID());
            return false; // this was a duplicate, don't log it
        }
    }

    /** {@inheritDoc} */
    @Override
    public void scheduleConversionRetry(String clickExternalID) {
        try {
            jdbcTemplate.update("{CALL schedule_conversion_retry(?)}", clickExternalID);
        } catch (org.springframework.dao.DataAccessException e) {
            LOG.error("Failed to schedule retry of conversion for clickExternalID={}", clickExternalID, e);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void scheduleRetry(PendingConversion pendingConversion) {
        scheduleConversionRetry(pendingConversion.getClickExternalID());
    }

    /** {@inheritDoc} */
    @Override
    public void deleteScheduledConversionRetry(PendingConversion pendingConversion) {
        try {
            jdbcTemplate.update("DELETE FROM pending_conversion WHERE click_external_id=?", pendingConversion.getClickExternalID());
        } catch (org.springframework.dao.DataAccessException e) {
            LOG.error("Failed to delete scheduled retry of conversion for clickExternalID={}", pendingConversion.getClickExternalID(), e);
        }
    }

    /** {@inheritDoc} */
    @Override
    public List<PendingConversion> getPendingConversionsToRetry(int maxRows) {
        JdbcTemplate tmpTemplate = new JdbcTemplate(dataSource);
        if (maxRows > 0) {
            tmpTemplate.setMaxRows(maxRows);
        }
        return tmpTemplate.query("SELECT creation_time, click_external_id FROM pending_conversion WHERE next_retry_time <= CURRENT_TIMESTAMP ORDER BY next_retry_time ASC",
                PendingConversionImpl.ROW_MAPPER);
    }

    /** {@inheritDoc} */
    @Override
    public boolean trackVideoView(Click click, int viewMs, int clipMs) {
        // For now we don't do anything with viewMs or clipMs, just de-dup the view record
        try {
            jdbcTemplate.update("INSERT INTO video_view (click_external_id, expire_time)" + " VALUES (UNHEX(REPLACE(?,'-','')),?)", click.getExternalID(), click.getExpireTime());
            return true; // this was the first conversion
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            LOG.info("Duplicate video view detected for click {}", click.getExternalID());
            return false; // this was a duplicate
        }
    }

    /** {@inheritDoc} */
    @Override
    public void scheduleVideoViewRetry(String clickExternalID, int viewMs, int clipMs) {
        try {
            jdbcTemplate.update("{CALL schedule_video_view_retry(?,?,?)}", clickExternalID, viewMs, clipMs);
        } catch (org.springframework.dao.DataAccessException e) {
            LOG.error("Failed to schedule retry of conversion for clickExternalID={}, viewMs={}, clipMs={}", clickExternalID, viewMs, clipMs, e);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void scheduleRetry(PendingVideoView pendingVideoView) {
        scheduleVideoViewRetry(pendingVideoView.getClickExternalID(), pendingVideoView.getViewMs(), pendingVideoView.getClipMs());
    }

    /** {@inheritDoc} */
    @Override
    public void deleteScheduledVideoViewRetry(PendingVideoView pendingVideoView) {
        try {
            jdbcTemplate.update("DELETE FROM pending_video_view WHERE click_external_id=? AND view_ms=? AND clip_ms=?", pendingVideoView.getClickExternalID(),
                    pendingVideoView.getViewMs(), pendingVideoView.getClipMs());
        } catch (org.springframework.dao.DataAccessException e) {
            LOG.error("Failed to delete scheduled retry of video view for clickExternalID={}, viewMs={}, clipMs={}", pendingVideoView.getClickExternalID(),
                    pendingVideoView.getViewMs(), pendingVideoView.getClipMs(), e);
        }
    }

    /** {@inheritDoc} */
    @Override
    public List<PendingVideoView> getPendingVideoViewsToRetry(int maxRows) {
        JdbcTemplate tmpTemplate = new JdbcTemplate(dataSource);
        if (maxRows > 0) {
            tmpTemplate.setMaxRows(maxRows);
        }
        return tmpTemplate.query(
                "SELECT creation_time, click_external_id, view_ms, clip_ms FROM pending_video_view WHERE next_retry_time <= CURRENT_TIMESTAMP ORDER BY next_retry_time ASC",
                PendingVideoViewImpl.ROW_MAPPER);
    }

    public long resolveUaHeaderId(String userAgentHeader) {
        Long uaHeaderId = null;

        // Try the cache first
        Element element = uaHeaderIdCache.get(userAgentHeader);
        if (element != null) {
            uaHeaderId = (Long) element.getValue();
            LOG.debug("Found uaHeaderId={} in cache", uaHeaderId);
            return uaHeaderId;
        }

        for (int k = 0; k < RESOLVE_UA_HEADER_MAX_TRIES; ++k) {
            // Query for it...
            try {
                uaHeaderId = jdbcTemplate.queryForObject("SELECT id FROM ua_header WHERE header=?", new Object[] { userAgentHeader }, Long.class);
                break;
            } catch (org.springframework.dao.EmptyResultDataAccessException e) {
                // It doesn't exist yet...fall through to inserting it...
            }

            // Insert it...
            LOG.debug("Inserting ua_header.header={}", userAgentHeader);
            try {
                jdbcTemplate.update("INSERT INTO ua_header (header) VALUES (?)", userAgentHeader);
            } catch (org.springframework.dao.DataIntegrityViolationException e) {
                // Somebody must have just inserted it...fall back on the select
                LOG.debug("Looks like another process just inserted the same User-Agent header...falling back on select");
            }
        }
        if (uaHeaderId == null) {
            throw new IllegalStateException("Failed to resolve ua_header id for User-Agent: " + userAgentHeader);
        }

        // Cache it for subsequent use
        uaHeaderIdCache.put(new Element(userAgentHeader, uaHeaderId, false, null, UA_HEADER_CACHE_TTL_SEC));

        return uaHeaderId;
    }

    @SuppressWarnings("rawtypes")
    private static <E extends Enum> String nullableName(E e) {
        return e == null ? null : e.name();
    }

    public static final class PendingInstallImpl implements PendingInstall {
        private static final RowMapper<PendingInstall> ROW_MAPPER = new RowMapper<PendingInstall>() {
            @Override
            public PendingInstall mapRow(ResultSet rs, int rowNum) throws java.sql.SQLException {
                return new PendingInstallImpl(rs.getTimestamp(CREATION_TIME), rs.getString(APP_ID), rs.getLong(DEVICE_IDENTIFIER_TYPE_ID), rs.getString(DEVICE_IDENTIFIER),
                        rs.getBoolean(CLAIM));
            }
        };

        private final Date creationTime;
        private final String applicationId;
        private final long deviceIdentifierTypeId;
        private final String deviceIdentifier;
        private final boolean claim;

        private PendingInstallImpl(Date creationTime, String applicationId, long deviceIdentifierTypeId, String deviceIdentifier, boolean claim) {
            this.creationTime = creationTime;
            this.applicationId = applicationId;
            this.deviceIdentifierTypeId = deviceIdentifierTypeId;
            this.deviceIdentifier = deviceIdentifier;
            this.claim = claim;
        }

        @Override
        public Date getCreationTime() {
            return creationTime;
        }

        @Override
        public String getApplicationId() {
            return applicationId;
        }

        @Override
        public long getDeviceIdentifierTypeId() {
            return deviceIdentifierTypeId;
        }

        @Override
        public String getDeviceIdentifier() {
            return deviceIdentifier;
        }

        @Override
        public boolean isClaim() {
            return claim;
        }
    }

    public static final class PendingAuthenticatedInstallImpl implements PendingAuthenticatedInstall {

        private static final RowMapper<PendingAuthenticatedInstall> ROW_MAPPER = new RowMapper<PendingAuthenticatedInstall>() {
            @Override
            public PendingAuthenticatedInstall mapRow(ResultSet rs, int rowNum) throws java.sql.SQLException {
                return new PendingAuthenticatedInstallImpl(rs.getTimestamp(CREATION_TIME), rs.getString(CLICK_EXTERNAL_ID));
            }
        };

        private final Date creationTime;
        private final String clickExternalID;

        private PendingAuthenticatedInstallImpl(Date creationTime, String clickExternalID) {
            this.creationTime = creationTime;
            this.clickExternalID = clickExternalID;
        }

        @Override
        public Date getCreationTime() {
            return creationTime;
        }

        @Override
        public String getClickExternalID() {
            return clickExternalID;
        }
    }

    public static final class PendingConversionImpl implements PendingConversion {
        private static final RowMapper<PendingConversion> ROW_MAPPER = new RowMapper<PendingConversion>() {
            @Override
            public PendingConversion mapRow(ResultSet rs, int rowNum) throws java.sql.SQLException {
                return new PendingConversionImpl(rs.getTimestamp(CREATION_TIME), rs.getString(CLICK_EXTERNAL_ID));
            }
        };

        private final Date creationTime;
        private final String clickExternalID;

        private PendingConversionImpl(Date creationTime, String clickExternalID) {
            this.creationTime = creationTime;
            this.clickExternalID = clickExternalID;
        }

        @Override
        public Date getCreationTime() {
            return creationTime;
        }

        @Override
        public String getClickExternalID() {
            return clickExternalID;
        }
    }

    public static final class PendingVideoViewImpl implements PendingVideoView {
        private static final RowMapper<PendingVideoView> ROW_MAPPER = new RowMapper<PendingVideoView>() {
            @Override
            public PendingVideoView mapRow(ResultSet rs, int rowNum) throws java.sql.SQLException {
                return new PendingVideoViewImpl(rs.getTimestamp(CREATION_TIME), rs.getString(CLICK_EXTERNAL_ID), rs.getInt(VIEW_MS), rs.getInt(CLIP_MS));
            }
        };

        private final Date creationTime;
        private final String clickExternalID;
        private final int viewMs;
        private final int clipMs;

        private PendingVideoViewImpl(Date creationTime, String clickExternalID, int viewMs, int clipMs) {
            this.creationTime = creationTime;
            this.clickExternalID = clickExternalID;
            this.viewMs = viewMs;
            this.clipMs = clipMs;
        }

        @Override
        public Date getCreationTime() {
            return creationTime;
        }

        @Override
        public String getClickExternalID() {
            return clickExternalID;
        }

        @Override
        public int getViewMs() {
            return viewMs;
        }

        @Override
        public int getClipMs() {
            return clipMs;
        }
    }

    private static final class ClickRowMapper implements RowMapper<Click> {
        private static final ClickRowMapper INSTANCE = new ClickRowMapper();

        private static final String SELECT_BASE = "SELECT lower(hex($.external_id)) as external_id, $.creation_time, $.expire_time, $.test_mode, $.tracking_identifier, $.ad_space_id, $.creative_id, $.model_id, $.country_id, $.operator_id, $.age_range_min, $.age_range_max, $.gender, $.geotarget_id, $.integration_type_id, $.ip_address, ua_header.header, $.rtb_settlement_price, $.postal_code_id, $.rtb_bid_price, $.hostname, $.user_time_zone_id, $.strategy, $.date_of_birth, $.latitude, $.longitude, $.location_source, $.tracked";

        public static String getSelectBase(String alias) {
            return SELECT_BASE.replaceAll("\\$", alias);
        }

        public static String getFrom(String alias) {
            return "click " + alias + " INNER JOIN ua_header ON ua_header.id=" + alias + ".ua_header_id";
        }

        @Override
        public Click mapRow(ResultSet rs, int rowNum) throws java.sql.SQLException {
            Click click = new Click();
            click.setExternalID(addDashes(rs.getString(1)));
            click.setCreationTime(rs.getTimestamp(2));
            click.setExpireTime(rs.getTimestamp(3));
            click.setTestMode(rs.getBoolean(4));
            click.setTrackingIdentifier(rs.getString(5));
            click.setAdSpaceId(rs.getLong(6));
            click.setCreativeId(rs.getLong(7));
            Number modelId = (Number) rs.getObject(8);
            if (modelId != null) {
                click.setModelId(modelId.longValue());
            }
            Number countryId = (Number) rs.getObject(9);
            if (countryId != null) {
                click.setCountryId(countryId.longValue());
            }
            Number operatorId = (Number) rs.getObject(10);
            if (operatorId != null) {
                click.setOperatorId(operatorId.longValue());
            }
            Number ageRangeMin = (Number) rs.getObject(11);
            Number ageRangeMax = (Number) rs.getObject(12);
            if (ageRangeMin != null && ageRangeMax != null) {
                click.setAgeRange(new Range<Integer>(ageRangeMin.intValue(), ageRangeMax.intValue()));
            }
            String genderStr = rs.getString(13);
            if (genderStr != null) {
                try {
                    click.setGender(Gender.valueOf(genderStr));
                } catch (Exception e) {
                    LOG.warn("Invalid Gender value: {}", genderStr);
                }
            }
            Number geotargetId = (Number) rs.getObject(14);
            if (geotargetId != null) {
                click.setGeotargetId(geotargetId.longValue());
            }
            Number integrationTypeId = (Number) rs.getObject(15);
            if (integrationTypeId != null) {
                click.setIntegrationTypeId(integrationTypeId.longValue());
            }
            click.setIpAddress(rs.getString(16));
            click.setUserAgentHeader(rs.getString(17));
            click.setRtbSettlementPrice(rs.getBigDecimal(18));
            Number postalCodeId = (Number) rs.getObject(19);
            if (postalCodeId != null) {
                click.setPostalCodeId(postalCodeId.longValue());
            }
            click.setRtbBidPrice(rs.getBigDecimal(20));
            click.setHost(rs.getString(21));
            click.setUserTimeZoneId(rs.getString(22));
            click.setStrategy(rs.getString(23));
            click.setDateOfBirth(rs.getDate(24));
            Number latitude = (Number) rs.getObject(25);
            if (latitude != null) {
                click.setLatitude(latitude.doubleValue());
            }
            Number longitude = (Number) rs.getObject(26);
            if (longitude != null) {
                click.setLongitude(longitude.doubleValue());
            }
            click.setLocationSource(rs.getString(27));
            click.setTracked(rs.getBoolean(28));
            return click;
        }
    }

    /** Add dashes back to a UUID that had been converted to plain hex.
        For example, turn this...
        98ee47e5c4244d0681c55abe0c72dae3
        ...into this:
        98ee47e5-c424-4d06-81c5-5abe0c72dae3
        Or, if we need to add leading zeros, turn this...
        47e5c4244d0681c55abe0c72dae3
        ...into this:
        000047e5-c424-4d06-81c5-5abe0c72dae3
    */
    public static String addDashes(String hex) {
        if (hex.length() > TOTAL_TOKEN_LEN) {
            throw new IllegalArgumentException("Expected length <=32, got " + hex.length() + ": " + hex);
        }
        StringBuilder bld = new StringBuilder(TOTAL_TOKEN_LEN_WITH_DASH);
        // It's conceivable that the value starts with zeros, in which case
        // the zeros may actually not be there.  Pad with leading zeros as
        // needed.
        int firstTokLen = TOKEN_LEN_8;
        for (int k = hex.length(); k < TOTAL_TOKEN_LEN; ++k) {
            bld.append('0');
            --firstTokLen;
        }

        int idx = 0;
        bld.append(hex.substring(idx, firstTokLen)).append('-');
        idx += firstTokLen;
        return bld.append(hex.substring(idx, idx + TOKEN_LEN_4)).append('-').append(hex.substring(idx + TOKEN_LEN_4, idx + TOKEN_LEN_8)).append('-')
                .append(hex.substring(idx + TOKEN_LEN_8, idx + TOKEN_LEN_12)).append('-').append(hex.substring(idx + TOKEN_LEN_12)).toString().toLowerCase();
    }
}
