package com.adfonic.presentation.optimisation.sql.mappers;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.adfonic.dto.optimisation.OptimisationUserInterfaceRemovedPublicationDto;

/**
 * Mapper for removed publication data. The breakdownByCreative boolean determines which fields are to be looked for.
 * 
 * @author pierre
 *
 */
public class OptimisationUserInterfaceRemovedPublicationDtoMapper extends OptimisationUserInterfaceLivePublicationDtoMapper {
	
	public OptimisationUserInterfaceRemovedPublicationDtoMapper(boolean breakdownByCreative) {
		super(breakdownByCreative);
	}

	protected void mapRow(OptimisationUserInterfaceRemovedPublicationDto row, ResultSet rs) throws SQLException {
		super.mapRow(row, rs);
		
		row.setRemovedType(rs.getString("removal_type"));
        row.setDateRemoved(com.adfonic.presentation.util.Utils.dateFromTimestamp(rs.getString("removal_unix_timestamp")));
		
	}

	@Override
	public OptimisationUserInterfaceRemovedPublicationDto mapRow(ResultSet rs, int rowNum)
			throws SQLException {
		OptimisationUserInterfaceRemovedPublicationDto row = new OptimisationUserInterfaceRemovedPublicationDto();
		mapRow(row, rs);
		return row;
	}

}
