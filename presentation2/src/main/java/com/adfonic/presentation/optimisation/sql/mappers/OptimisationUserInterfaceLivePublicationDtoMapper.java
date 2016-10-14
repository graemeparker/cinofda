package com.adfonic.presentation.optimisation.sql.mappers;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.adfonic.dto.optimisation.OptimisationUserInterfaceLivePublicationDto;

/**
 * Mapper for live publication data. The breakdownByCreative boolean determines which fields are to be looked for.
 * 
 * @author pierre
 *
 */
public class OptimisationUserInterfaceLivePublicationDtoMapper implements RowMapper<OptimisationUserInterfaceLivePublicationDto> {
	
	private boolean breakdownByCreative = false;
	
	public OptimisationUserInterfaceLivePublicationDtoMapper(boolean breakdownByCreative) {
		super();
		this.breakdownByCreative = breakdownByCreative;
	}
	
	protected void mapRow(OptimisationUserInterfaceLivePublicationDto row, ResultSet rs) throws SQLException {
		row.setPublicationName(rs.getString("publication"));
		row.setPublicationId(rs.getLong("PUBLICATION_ID"));
		row.setPublicationType(rs.getString("publication_type"));
		row.setPublicationBundle(rs.getString("bundle"));
		row.setPublicationExternalId(rs.getString("publication_external_id"));
		if(this.breakdownByCreative) {
			row.setCreativeId(rs.getLong("CREATIVE_ID"));
			row.setCreativeName(rs.getString("creative"));
		} else {
			row.setPartiallyRemoved(rs.getBoolean("part_removed_flag"));
		}
		row.setIabCategory(rs.getString("iab_category"));
		row.setInventorySource(rs.getString("inventory_source"));
		row.setBids(rs.getInt("bids"));
		row.setImpressions(rs.getInt("impressions"));
		row.setWinRate(rs.getDouble("win_rate"));
		row.setClicks(rs.getInt("clicks"));
		row.setCtr(rs.getDouble("ctr"));
		row.setConversions(rs.getInt("conversions"));
		row.setCvr(rs.getDouble("cvr"));
		row.setSpend(rs.getDouble("spend"));
		row.setEcpm(rs.getDouble("ecpm"));
		row.setEcpc(rs.getDouble("ecpc"));
		row.setEcpa(rs.getDouble("ecpa"));
	}
	
	@Override
	public OptimisationUserInterfaceLivePublicationDto mapRow(ResultSet rs, int rowNum)
			throws SQLException {
		OptimisationUserInterfaceLivePublicationDto row = new OptimisationUserInterfaceLivePublicationDto();
		mapRow(row, rs);
		return row;
	}
	
	

}
