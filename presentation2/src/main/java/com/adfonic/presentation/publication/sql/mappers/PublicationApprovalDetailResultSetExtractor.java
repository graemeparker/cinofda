package com.adfonic.presentation.publication.sql.mappers;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.lang.BooleanUtils;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;

import com.adfonic.dto.publication.enums.PublicationSafetyLevel;
import com.adfonic.presentation.publication.model.PublicationApprovalDetailModel;

public class PublicationApprovalDetailResultSetExtractor implements ResultSetExtractor<PublicationApprovalDetailModel> {

    @Override
    public PublicationApprovalDetailModel extractData(ResultSet rs) throws SQLException, DataAccessException {
        PublicationApprovalDetailModel tableRow = null;
        if (rs.next()) {
            tableRow = new PublicationApprovalDetailModel();
            tableRow.setStatus(rs.getString("publication_status"));
            tableRow.setInternalId(rs.getString("publication_id"));
            tableRow.setExternalId(rs.getString("publication_external_id"));
            tableRow.setRtbId(rs.getString("publication_rtb_id"));
            tableRow.setAccountType(rs.getString("account_type"));
            tableRow.setCompany(rs.getString("company_name"));
            tableRow.setSupplierName(rs.getString("publisher_name"));
            tableRow.setName(rs.getString("publication_name"));
            tableRow.setUrl(rs.getString("publication_url"));
            
            // Revenue Share
            tableRow.setRevenueShare(new BigDecimal(rs.getString("revenue_share")));
            
            // Disclose Identity
            tableRow.setDiscloseIdentity(BooleanUtils.toBoolean(Integer.valueOf(rs.getString("disclosed"))));
            
            tableRow.setFriendlyName(rs.getString("publication_friendly_name"));
            
            // Safety Level mapping
            PublicationSafetyLevel safetyLevel = PublicationSafetyLevel.valueOf(rs.getString("publication_safety_level"));
            tableRow.setSafetyLevel((safetyLevel == null) ? PublicationSafetyLevel.UN_CATEGORISED : safetyLevel );
            
            tableRow.setSellerNetworkId(rs.getString("supplier_network_id"));
            tableRow.setBundle(rs.getString("bundle_external_ids"));
            tableRow.setSupplierUserName(rs.getString("supplier_user_name"));
            tableRow.setType(rs.getString("publication_type"));
            tableRow.setStatedCategory(rs.getString("category_name"));
            
            // Soft Floor
            tableRow.setSoftFloor(BooleanUtils.toBoolean(rs.getString("soft_floor")));
            
            tableRow.setAlgorithmStatus(rs.getString("algo_status"));
            tableRow.setDeadZoneStatus(rs.getString("dead_zone_status"));
            
            // Sampling
            tableRow.setSamplingRate(rs.getString("sampling_rate"));
            tableRow.setSamplingActive(BooleanUtils.toBoolean(Integer.valueOf(rs.getString("publication_sampling_active"))));
        }
        return tableRow;
    }

}
