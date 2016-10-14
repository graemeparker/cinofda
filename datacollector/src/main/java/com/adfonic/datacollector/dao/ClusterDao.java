package com.adfonic.datacollector.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sql.DataSource;

import org.apache.commons.dbutils.DbUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.adfonic.adserver.AdEvent;
import com.adfonic.datacollector.AdEventAccounting;
import com.adfonic.datacollector.UserAgent;
import com.adfonic.datacollector.kafka.AdEventData;

@Component
public class ClusterDao extends AbstractDao {

    private static final transient Logger LOG = Logger.getLogger(ClusterDao.class.getName());

    private final String userAgentSelectStatement;
    private final String userAgentInsertStatement;
    private final String userAgentUpdateStatement;
    private final String adEventLogInsertStatement;
    private final JdbcTemplate jdbcTemplate;
    //private final long dpidTypeId;
    //private final long odin1TypeId;
    //private final long openudidTypeId;
    private final long hifaTypeId;
    private final long atidTypeId;
    private final long gouidTypeId;
    private final long adidTypeId;
    //private final long adid_md5TypeId;
    private final long idfaTypeId;
    //private final long idfa_md5TypeId;

    private DataSource toolsDataSource;

    @Autowired
    public ClusterDao(@Qualifier("clusterDataSource") DataSource dataSource, @Qualifier("toolsDataSource") DataSource toolsDataSource,
            @Value("${cluster.schema.adfonic}") String adfonicSchema, @Value("${cluster.schema.event}") String eventSchema) {
        //cluster data source
        super(dataSource);
        this.toolsDataSource = toolsDataSource;

        //AD-140 tools data source
        this.jdbcTemplate = new JdbcTemplate(toolsDataSource);

//        // Initialize the device identifier type ids we'll need later
//        dpidTypeId = jdbcTemplate.queryForLong("SELECT ID FROM " + adfonicSchema + ".DEVICE_IDENTIFIER_TYPE WHERE SYSTEM_NAME='dpid'");
//        if (dpidTypeId == 0) {
//            throw new IllegalStateException("Couldn't determine DEVICE_IDENTIFIER_TYPE.ID for dpid");
//        }

//        odin1TypeId = jdbcTemplate.queryForLong("SELECT ID FROM " + adfonicSchema + ".DEVICE_IDENTIFIER_TYPE WHERE SYSTEM_NAME='odin-1'");
//        if (odin1TypeId == 0) {
//            throw new IllegalStateException("Couldn't determine DEVICE_IDENTIFIER_TYPE.ID for odin-1");
//        }
//
//        openudidTypeId = jdbcTemplate.queryForLong("SELECT ID FROM " + adfonicSchema + ".DEVICE_IDENTIFIER_TYPE WHERE SYSTEM_NAME='openudid'");
//        if (openudidTypeId == 0) {
//            throw new IllegalStateException("Couldn't determine DEVICE_IDENTIFIER_TYPE.ID for openudid");
//        }

        hifaTypeId = jdbcTemplate.queryForObject("SELECT ID FROM " + adfonicSchema + ".DEVICE_IDENTIFIER_TYPE WHERE SYSTEM_NAME='hifa'", Long.class);
        if (hifaTypeId == 0) {
            throw new IllegalStateException("Couldn't determine DEVICE_IDENTIFIER_TYPE.ID for hifa");
        }

        atidTypeId = jdbcTemplate.queryForObject("SELECT ID FROM " + adfonicSchema + ".DEVICE_IDENTIFIER_TYPE WHERE SYSTEM_NAME='atid'", Long.class);
        if (atidTypeId == 0) {
            throw new IllegalStateException("Couldn't determine DEVICE_IDENTIFIER_TYPE.ID for atid");
        }

        gouidTypeId = jdbcTemplate.queryForObject("SELECT ID FROM " + adfonicSchema + ".DEVICE_IDENTIFIER_TYPE WHERE SYSTEM_NAME='gouid'", Long.class);
        if (gouidTypeId == 0) {
            throw new IllegalStateException("Couldn't determine DEVICE_IDENTIFIER_TYPE.ID for gouid");
        }

        adidTypeId = jdbcTemplate.queryForObject("SELECT ID FROM " + adfonicSchema + ".DEVICE_IDENTIFIER_TYPE WHERE SYSTEM_NAME='adid'", Long.class);
        if (adidTypeId == 0) {
            throw new IllegalStateException("Couldn't determine DEVICE_IDENTIFIER_TYPE.ID for adid");
        }

//        adid_md5TypeId = jdbcTemplate.queryForLong("SELECT ID FROM " + adfonicSchema + ".DEVICE_IDENTIFIER_TYPE WHERE SYSTEM_NAME='adid_md5'");
//        if (adid_md5TypeId == 0) {
//            throw new IllegalStateException("Couldn't determine DEVICE_IDENTIFIER_TYPE.ID for adid_md5");
//        }

        idfaTypeId = jdbcTemplate.queryForObject("SELECT ID FROM " + adfonicSchema + ".DEVICE_IDENTIFIER_TYPE WHERE SYSTEM_NAME='idfa'", Long.class);
        if (idfaTypeId == 0) {
            throw new IllegalStateException("Couldn't determine DEVICE_IDENTIFIER_TYPE.ID for idfa");
        }

//        idfa_md5TypeId = jdbcTemplate.queryForLong("SELECT ID FROM " + adfonicSchema + ".DEVICE_IDENTIFIER_TYPE WHERE SYSTEM_NAME='idfa_md5'");
//        if (idfa_md5TypeId == 0) {
//            throw new IllegalStateException("Couldn't determine DEVICE_IDENTIFIER_TYPE.ID for idfa_md5");
//        }

        userAgentSelectStatement = "SELECT ID, DATE_LAST_SEEN FROM " + adfonicSchema + ".USER_AGENT WHERE UA_HEADER=? AND DELETED = false FOR UPDATE";
        userAgentInsertStatement = "INSERT INTO " + adfonicSchema + ".USER_AGENT(UA_HEADER,MODEL_ID,DATE_LAST_SEEN) VALUES(?,?,?)";
        userAgentUpdateStatement = "UPDATE " + adfonicSchema + ".USER_AGENT SET DATE_LAST_SEEN = ?, DELETED = false WHERE UA_HEADER = ?";

        adEventLogInsertStatement = "INSERT INTO " + eventSchema + ".AD_EVENT_LOG(" + "EVENT_TIME" // 1
                + ",PUBLISHER_TIME_ID" // 2
                + ",ADVERTISER_TIME_ID" // 3
                + ",COST" // 4
                + ",ADVERTISER_VAT" // 5
                + ",PAYOUT" //6
                + ",PUBLISHER_VAT" // 7
                + ",AD_ACTION" //8
                + ",CREATIVE_ID" // 9
                + ",CAMPAIGN_ID" // 10
                + ",AD_SPACE_ID" //11
                + ",PUBLICATION_ID" //12
                + ",MODEL_ID" // 13
                + ",COUNTRY_ID" // 14
                + ",OPERATOR_ID" // 15
                + ",AGE_LOW" // 16
                + ",AGE_HIGH" // 17
                + ",GENDER" // 18
                + ",USER_AGENT_ID" // 19
                + ",BACKFILLED" // 20
                + ",GEOTARGET_ID" // 21
                + ",IP_ADDRESS" // 22
                + ",INTEGRATION_TYPE_ID" // 23
                + ",POSTAL_CODE_ID" // 24
                + ",ACTION_VALUE" // 25
                + ",RTB_BID_PRICE" // 26
                + ",RTB_SETTLEMENT_PRICE" // 27
                + ",DPID" // 28
                + ",ODIN1" // 29
                + ",OPENUDID" // 30
                + ",HIFA" // 31
                + ",ADTRUTHID" // 32
                + ",HOSTNAME" // 33
                + ",IMPRESSION_ID" // 34
                + ",USER_TIME_ID" // 35
                + ",STRATEGY" // 36
                + ",DATE_OF_BIRTH" // 37
                + ",LATITUDE" // 38
                + ",LONGITUDE" // 39
                + ",LOCATION_SOURCE" // 40
                + ",GOUID" // 41
                + ",ADID_RAW" // 42
                + ",ADID_MD5" // 43
                + ",IDFA_RAW" // 44
                + ",IDFA_MD5" // 45
                + ") VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

    }

    public UserAgent getOrCreateUserAgent(String userAgentHeader, long modelId, int date) throws java.sql.SQLException {
        if (userAgentHeader == null) {
            return null;
        }

        // https://tickets.adfonic.com/browse/BZ-919
        if (userAgentHeader.length() >= 512) {
            if (LOG.isLoggable(Level.INFO)) {
                LOG.info("Truncating User-Agent header from " + userAgentHeader.length() + " to 512: " + userAgentHeader);
            }
            userAgentHeader = userAgentHeader.substring(0, 512);
        }

        PreparedStatement pstSelect = null;
        PreparedStatement pstInsert = null;
        PreparedStatement pstUpdate = null;
        ResultSet rs = null;
        UserAgent userAgent;
        Connection conn = toolsDataSource.getConnection();
        try {
            conn.setAutoCommit(true); // use implicit transactions
            // First try querying for the USER_AGENT by UA_HEADER
            userAgent = selectUserAgent(conn, userAgentHeader);
            if (userAgent != null) {
                if (userAgent.getLastSeen() == 0 || userAgent.getLastSeen() < date) {
                    // if there is no date against this USER_AGENT record OR the date is less than 
                    // current date then just update it
                    updateUserAgent(conn, userAgentHeader, modelId, date);
                    // get the latest UserAgent if there was an update.
                    userAgent = selectUserAgent(conn, userAgentHeader);
                }
                return userAgent;
            }

            // Doesn't exist yet...insert it
            pstInsert = conn.prepareStatement(userAgentInsertStatement);
            pstInsert.setString(1, userAgentHeader);
            pstInsert.setLong(2, modelId);
            pstInsert.setInt(3, date);
            try {
                pstInsert.executeUpdate();
                // We inserted the new value successfully...return its id
                return selectUserAgent(conn, userAgentHeader);
            } catch (java.sql.SQLException e) {
                // Probably a unique constraint violation...fall back on
                // retrying the select query
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.fine("Caught " + e.getClass().getName() + ", retrying the select");
                }
                //DbUtils.closeQuietly(rs);
                return selectUserAgent(conn, userAgentHeader);
            }
        } catch (Exception e) {
            LOG.severe("Exception occured. " + e.getMessage());
            throw e;
        } finally {
            try {
                DbUtils.closeQuietly(pstInsert);
                DbUtils.closeQuietly(conn);
                //DbUtils.closeQuietly(pstUpdate);
                //DbUtils.closeQuietly(conn, pstSelect, rs);
            } catch (Exception e) {
                LOG.severe("Couldn't clean up " + e.getMessage());
            } finally {
                DbUtils.closeQuietly(conn);
            }
        }
    }

    public void updateUserAgent(Connection conn, String userAgentHeader, long modelId, int date) throws java.sql.SQLException {
        PreparedStatement pstUpdate = null;
        Connection con = null;
        try {
            if (conn == null) {
                con = toolsDataSource.getConnection();
                con.setAutoCommit(true); // use implicit transactions
            } else {
                con = conn;
                con.setAutoCommit(true);
            }
            pstUpdate = con.prepareStatement(userAgentUpdateStatement);
            pstUpdate.setInt(1, date);
            pstUpdate.setString(2, userAgentHeader);
            pstUpdate.executeUpdate();
        } catch (SQLException se) {
            LOG.severe("Couldn't updateUserAgent because of " + se.getClass().getName() + "causing " + se.getMessage());
            DbUtils.closeQuietly(pstUpdate);
        } finally {
            try {
                DbUtils.closeQuietly(pstUpdate);
            } catch (Exception e) {
                LOG.severe("Couldn't close connection " + e.getMessage());
            } finally {
                if (conn == null) {
                    DbUtils.closeQuietly(con);
                }
            }
        }
    }

    public UserAgent selectUserAgent(Connection conn, String userAgentHeader) throws SQLException {
        PreparedStatement pstSelect = null;
        ResultSet rs = null;
        try {
            pstSelect = conn.prepareStatement(userAgentSelectStatement);
            pstSelect.setString(1, userAgentHeader);
            rs = pstSelect.executeQuery();
            if (rs.next()) {
                return new UserAgent(rs.getLong(1), rs.getInt(2));
            }
        } catch (java.sql.SQLException e) {
            // Probably a unique constraint violation...fall back on
            // retrying the select query
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("Caught " + e.getClass().getName() + ", retrying the select");
            }
            DbUtils.closeQuietly(rs);
            rs = pstSelect.executeQuery();
            if (rs.next()) {
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.fine("Select retry worked");
                }
                return new UserAgent(rs.getLong(1), rs.getInt(2));
            } else {
                // Something must have gone legitimately wrong
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.fine("Select retry failed");
                }
                throw e;
            }
        } finally {
            DbUtils.closeQuietly(pstSelect);
            DbUtils.closeQuietly(rs);
        }
        return null;
    }

    @Deprecated
    public Long getOrCreateUserAgentId(String userAgentHeader, long modelId) throws java.sql.SQLException {
        if (userAgentHeader == null) {
            return null;
        }

        // https://tickets.adfonic.com/browse/BZ-919
        if (userAgentHeader.length() >= 512) {
            if (LOG.isLoggable(Level.INFO)) {
                LOG.info("Truncating User-Agent header from " + userAgentHeader.length() + " to 512: " + userAgentHeader);
            }
            userAgentHeader = userAgentHeader.substring(0, 512);
        }

        PreparedStatement pstSelect = null;
        PreparedStatement pstInsert = null;
        PreparedStatement pstUpdate = null;
        ResultSet rs = null;
        Connection conn = toolsDataSource.getConnection();
        try {
            conn.setAutoCommit(true); // use implicit transactions

            // First try querying for the USER_AGENT by UA_HEADER
            pstSelect = conn.prepareStatement(userAgentSelectStatement);
            pstSelect.setString(1, userAgentHeader);
            rs = pstSelect.executeQuery();
            if (rs.next()) {
                return rs.getLong(1);
            }

            // Doesn't exist yet...insert it
            pstInsert = conn.prepareStatement(userAgentInsertStatement);
            pstInsert.setString(1, userAgentHeader);
            pstInsert.setLong(2, modelId);
            try {
                pstInsert.executeUpdate();
                // We inserted the new value successfully...return its id
                return getLastInsertId(conn);
            } catch (java.sql.SQLException e) {
                // Probably a unique constraint violation...fall back on
                // retrying the select query
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.fine("Caught " + e.getClass().getName() + ", retrying the select");
                }
                DbUtils.closeQuietly(rs);
                rs = pstSelect.executeQuery();
                if (rs.next()) {
                    if (LOG.isLoggable(Level.FINE)) {
                        LOG.fine("Select retry worked");
                    }
                    return rs.getLong(1);
                } else {
                    // Something must have gone legitimately wrong
                    if (LOG.isLoggable(Level.FINE)) {
                        LOG.fine("Select retry failed");
                    }
                    throw e;
                }
            }
        } finally {
            DbUtils.closeQuietly(pstInsert);
            DbUtils.closeQuietly(pstUpdate);
            DbUtils.closeQuietly(conn, pstSelect, rs);
        }
    }

    public void createAdEventLog(AdEvent event, Long userAgentId, int publisherTimeId) throws java.sql.SQLException {
        createAdEventLog(event, null, userAgentId, null, publisherTimeId);
    }

    public void createAdEventLog(AdEventAccounting accounting, Long userAgentId, Integer advertiserTimeId, int publisherTimeId) throws java.sql.SQLException {
        createAdEventLog(accounting.getAdEvent(), accounting, userAgentId, advertiserTimeId, publisherTimeId);
    }

    public void createAdEventLog(AdEvent event, AdEventAccounting accounting, Long userAgentId, Integer advertiserTimeId, int publisherTimeId) throws java.sql.SQLException {
        // Use JDBC to insert the values into AD_EVENT_LOG directly
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("Inserting into AD_EVENT_LOG");
        }
        PreparedStatement ps = null;
        Connection conn = getDataSource().getConnection();
        try {
            conn.setAutoCommit(true); // use implicit transactions

            ps = prepareEventStatement(event, accounting, userAgentId, advertiserTimeId, publisherTimeId, conn);

            ps.executeUpdate();
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("Inserted AD_EVENT_LOG");
            }
        } finally {
            DbUtils.closeQuietly(conn, ps, null);
        }
    }
    
    public PreparedStatement prepareEventStatement(AdEvent event, AdEventAccounting accounting, Long userAgentId,
            Integer advertiserTimeId, int publisherTimeId, Connection conn) throws SQLException {
        PreparedStatement ps;
        ps = conn.prepareStatement(adEventLogInsertStatement);
        ps = fillEventPreparedStatement(ps,event,accounting,userAgentId,advertiserTimeId,publisherTimeId,conn);
        return ps;
    }
    
	public PreparedStatement fillEventPreparedStatement(PreparedStatement ps, AdEvent event, AdEventAccounting accounting, Long userAgentId,
			Integer advertiserTimeId, int publisherTimeId, Connection conn) throws SQLException {
		ps.setTimestamp(1, new java.sql.Timestamp(event.getEventTime().getTime()));
		ps.setLong(2, publisherTimeId);
		if (advertiserTimeId == null) {
		    ps.setNull(3, java.sql.Types.INTEGER);
		} else {
		    ps.setLong(3, advertiserTimeId);
		}
		if (accounting == null || accounting.getCost() == null) {
		    ps.setNull(4, java.sql.Types.DECIMAL);
		} else {
		    ps.setBigDecimal(4, accounting.getCost());
		}
		if (accounting == null || accounting.getAdvertiserVat() == null) {
		    ps.setNull(5, java.sql.Types.DECIMAL);
		} else {
		    ps.setBigDecimal(5, accounting.getAdvertiserVat());
		}
		if (accounting == null || accounting.getPayout() == null) {
		    ps.setNull(6, java.sql.Types.DECIMAL);
		} else {
		    ps.setBigDecimal(6, accounting.getPayout());
		}
		if (accounting == null || accounting.getPublisherVat() == null) {
		    ps.setNull(7, java.sql.Types.DECIMAL);
		} else {
		    ps.setBigDecimal(7, accounting.getPublisherVat());
		}
		ps.setString(8, event.getAdAction().name());
		if (event.getCreativeId() == null) {
		    ps.setNull(9, java.sql.Types.INTEGER);
		} else {
		    ps.setLong(9, event.getCreativeId());
		}
		if (event.getCampaignId() == null) {
		    ps.setNull(10, java.sql.Types.INTEGER);
		} else {
		    ps.setLong(10, event.getCampaignId());
		}
		ps.setLong(11, event.getAdSpaceId());
		ps.setLong(12, event.getPublicationId());
		if (event.getModelId() == null) {
		    ps.setNull(13, java.sql.Types.INTEGER);
		} else {
		    ps.setLong(13, event.getModelId());
		}
		if (event.getCountryId() == null) {
		    ps.setNull(14, java.sql.Types.INTEGER);
		} else {
		    ps.setLong(14, event.getCountryId());
		}
		if (event.getOperatorId() == null) {
		    ps.setNull(15, java.sql.Types.INTEGER);
		} else {
		    ps.setLong(15, event.getOperatorId());
		}
		if (event.getAgeRange() == null) {
		    ps.setNull(16, java.sql.Types.INTEGER);
		    ps.setNull(17, java.sql.Types.INTEGER);
		} else {
		    ps.setInt(16, event.getAgeRange().getStart());
		    ps.setInt(17, event.getAgeRange().getEnd());
		}
		if (event.getGender() == null) {
		    ps.setNull(18, java.sql.Types.VARCHAR);
		} else {
		    ps.setString(18, event.getGender().name());
		}
		if (userAgentId == null) {
		    ps.setNull(19, java.sql.Types.INTEGER);
		} else {
		    ps.setLong(19, userAgentId);
		}
		if (accounting == null) {
		    ps.setBoolean(20, false);
		} else {
		    ps.setBoolean(20, accounting.getAdvertiser().getCompany().isBackfill());
		}
		if (event.getGeotargetId() == null) {
		    ps.setNull(21, java.sql.Types.INTEGER);
		} else {
		    ps.setLong(21, event.getGeotargetId());
		}
		ps.setString(22, event.getIpAddress());
		if (event.getIntegrationTypeId() == null) {
		    ps.setNull(23, java.sql.Types.INTEGER);
		} else {
		    ps.setLong(23, event.getIntegrationTypeId());
		}
		if (event.getPostalCodeId() == null) {
		    ps.setNull(24, java.sql.Types.INTEGER);
		} else {
		    ps.setLong(24, event.getPostalCodeId());
		}
		if (event.getActionValue() == null) {
		    ps.setNull(25, java.sql.Types.INTEGER);
		} else {
		    ps.setLong(25, event.getActionValue());
		}
		if (event.getRtbBidPrice() == null) {
		    ps.setNull(26, java.sql.Types.DECIMAL);
		} else {
		    ps.setBigDecimal(26, event.getRtbBidPrice());
		}
		if (event.getRtbSettlementPrice() == null) {
		    ps.setNull(27, java.sql.Types.DECIMAL);
		} else {
		    ps.setBigDecimal(27, event.getRtbSettlementPrice());
		}

//            String dpid = event.getDeviceIdentifiers().get(dpidTypeId);
//            if (dpid == null) {
		    ps.setNull(28, java.sql.Types.VARCHAR);
//            } else {
//                ps.setString(28, dpid);
//            }
//            String odin1 = event.getDeviceIdentifiers().get(odin1TypeId);
//            if (odin1 == null) {
		    ps.setNull(29, java.sql.Types.VARCHAR);
//            } else {
//                ps.setString(29, odin1);
//            }

//            String openudid = event.getDeviceIdentifiers().get(openudidTypeId);
//            if (openudid == null) {
		    ps.setNull(30, java.sql.Types.VARCHAR);
//            } else {
//                ps.setString(30, openudid);
//            }
		String hifa = event.getDeviceIdentifiers().get(hifaTypeId);
		if (hifa == null) {
		    ps.setNull(31, java.sql.Types.VARCHAR);
		} else {
		    ps.setString(31, hifa);
		}
		String atid = event.getDeviceIdentifiers().get(atidTypeId);
		if (atid == null) {
		    ps.setNull(32, java.sql.Types.VARCHAR);
		} else {
		    ps.setString(32, atid);
		}
		if (event.getHost() == null) {
		    ps.setNull(33, java.sql.Types.VARCHAR);
		} else {
		    ps.setString(33, event.getHost());
		}
		if (event.getImpressionExternalID() == null) {
		    ps.setNull(34, java.sql.Types.VARCHAR);
		} else {
		    ps.setString(34, event.getImpressionExternalID());
		}
		if (event.getUserTimeId() == null) {
		    ps.setNull(35, java.sql.Types.INTEGER);
		} else {
		    ps.setLong(35, event.getUserTimeId());
		}
		if (event.getStrategy() == null) {
		    ps.setNull(36, java.sql.Types.VARCHAR);
		} else {
		    ps.setString(36, event.getStrategy());
		}
		if (event.getDateOfBirth() == null) {
		    ps.setNull(37, java.sql.Types.DATE);
		} else {
		    ps.setDate(37, new java.sql.Date(event.getDateOfBirth().getTime()));
		}
		if (event.getLatitude() == null) {
		    ps.setNull(38, java.sql.Types.DECIMAL);
		} else {
		    ps.setDouble(38, event.getLatitude());
		}
		if (event.getLongitude() == null) {
		    ps.setNull(39, java.sql.Types.DECIMAL);
		} else {
		    ps.setDouble(39, event.getLongitude());
		}
		if (event.getLocationSource() == null) {
		    ps.setNull(40, java.sql.Types.VARCHAR);
		} else {
		    ps.setString(40, event.getLocationSource());
		}
//            String gouid = event.getDeviceIdentifiers().get(gouidTypeId);
//            if (gouid == null) {
		    ps.setNull(41, java.sql.Types.VARCHAR);
//            } else {
//                ps.setString(41, gouid);
//            }
		String adid = event.getDeviceIdentifiers().get(adidTypeId);
		// MAX-76 setting adid to null as a hack i.e. we are not going to write it to the adeventlog
		// Length of the column is 40 varchar
		if (adid != null && adid.length() > 40) {
		    adid = null;
		}
		if (adid == null) {
		    ps.setNull(42, java.sql.Types.VARCHAR);
		} else {
		    ps.setString(42, adid);
		}

//            String adid_md5 = event.getDeviceIdentifiers().get(adid_md5TypeId);
//            if (adid_md5 == null) {
		    ps.setNull(43, java.sql.Types.VARCHAR);
//            } else {
//                ps.setString(43, adid_md5);
//            }
//            String idfa = event.getDeviceIdentifiers().get(idfaTypeId);
//            if (idfa == null) {
		    ps.setNull(44, java.sql.Types.VARCHAR);
//            } else {
//                ps.setString(44, dpid);
//            }
//            String idfa_md5 = event.getDeviceIdentifiers().get(idfa_md5TypeId);
//            if (idfa_md5 == null) {
		    ps.setNull(45, java.sql.Types.VARCHAR);
//            } else {
//                ps.setString(45, idfa_md5);
//            }
		return ps;
	}
	
	public void insertAdeventsBatch(List<AdEventData> adEvents) throws SQLException{
	    PreparedStatement ps = null;
        Connection conn = getDataSource().getConnection();
        try{
            conn.setAutoCommit(false);
            ps = conn.prepareStatement(adEventLogInsertStatement);
            for(AdEventData aed : adEvents){
                ps = fillEventPreparedStatement(ps, aed.getAdEvent(), aed.getAccounting(), aed.getUserAgentId(), aed.getAdvertiserTimeId(), aed.getPublisherTimeId(), conn);
                ps.addBatch();
            }
       
            ps.executeBatch();
            conn.commit();
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("Inserted a batch of AD_EVENT_LOG");
            }
            conn.setAutoCommit(false);
        }
        catch (SQLException e){
            LOG.warning("Failed inserting batched adevents: " + e.getMessage());
            throw e;
        } finally {
            DbUtils.closeQuietly(conn, ps, null);
        }
	}
}
