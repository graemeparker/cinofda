package com.adfonic.presentation.publication.sql.mappers;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.lang.BooleanUtils;
import org.springframework.jdbc.core.RowMapper;

import com.adfonic.presentation.publication.model.PublicationApprovalModel;

public class PublicationApprovalDtoRowMapper implements RowMapper<PublicationApprovalModel> {

    @Override
    public PublicationApprovalModel mapRow(ResultSet rs, int rowNum) throws SQLException {
        PublicationApprovalModel tableRow = new PublicationApprovalModel();

        tableRow.setInternalId(rs.getString("publication_id"));
        tableRow.setName(rs.getString("publication_name"));
        tableRow.setFriendlyName(rs.getString("publication_friendly_name"));
        tableRow.setSupplierName(rs.getString("publisher_name"));
        tableRow.setSupplierUserName(rs.getString("supplier_user_name"));
        tableRow.setExternalId(rs.getString("publication_external_id"));
        tableRow.setBundle(rs.getString("bundle_external_ids"));
        tableRow.setType(rs.getString("publication_type"));
        tableRow.setStatus(rs.getString("publication_status"));
        tableRow.setAssignedTo(rs.getString("assigned_to"));
        tableRow.setAccountType(rs.getString("account_type"));
        tableRow.setRtbId(rs.getString("publication_rtb_id"));
        tableRow.setSellerNetworkId(rs.getString("supplier_network_id"));
        tableRow.setAlgorithmStatus(rs.getString("algorithm_status"));
        tableRow.setDeadZoneStatus(rs.getString("dead_zone_status"));
        tableRow.setSamplingActive(BooleanUtils.toBoolean(Integer.valueOf(rs.getString("publication_sampling_active"))));

        return tableRow;
    }

}
