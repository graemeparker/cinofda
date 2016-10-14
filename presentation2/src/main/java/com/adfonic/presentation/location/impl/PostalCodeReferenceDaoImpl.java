package com.adfonic.presentation.location.impl;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.commons.lang.StringUtils;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.SqlReturnResultSet;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.jdbc.object.StoredProcedure;

import com.adfonic.dto.geotarget.PostalCodeReferenceDto;
import com.adfonic.presentation.location.PostalCodeReferenceDao;

public class PostalCodeReferenceDaoImpl extends JdbcDaoSupport implements PostalCodeReferenceDao {
    
    public PostalCodeReferenceDto getLatLonFromPostalCode(Long countryId, String postalCode) {
    	GetLatLonFromPostalCodeProcedure proc = new GetLatLonFromPostalCodeProcedure(getDataSource());
    	Map<String, Object> data = proc.execute(countryId, postalCode);
    	PostalCodeReferenceDto dto = (PostalCodeReferenceDto) data.get("result");
    	return dto;
    }
	
	class GetLatLonFromPostalCodeProcedure extends StoredProcedure {
		
		public GetLatLonFromPostalCodeProcedure(DataSource dataSource) {
			this(dataSource, "proc_return_lat_long_from_pc");
		}

        public GetLatLonFromPostalCodeProcedure(DataSource dataSource, String procedureCall) {
            super(dataSource, procedureCall);
            declareParameter(new SqlParameter("in_country_id", Types.NUMERIC));
            declareParameter(new SqlParameter("in_postcode", Types.VARCHAR));
            declareParameter(new SqlReturnResultSet("result", new PostalCodeReferenceDtoRowMapper()));
            compile();
        }
    }
	
	class PostalCodeReferenceDtoRowMapper implements ResultSetExtractor<PostalCodeReferenceDto> {
        public PostalCodeReferenceDto extractData(ResultSet rs) throws SQLException,DataAccessException {
        	if(rs.next()) {
	        	BigDecimal latitude = rs.getBigDecimal("LATITUDE");
	        	BigDecimal longitude = rs.getBigDecimal("LONGITUDE");
	        	String postalCode = rs.getString("postcode");
	        	
	        	if(StringUtils.isEmpty(postalCode) || latitude == null || longitude == null) {
	        		return null;
	        	}
	        	PostalCodeReferenceDto dto = new PostalCodeReferenceDto();
	        	dto.setPostalCode(postalCode);
	        	dto.setLatitude(latitude);
	        	dto.setLongitude(longitude);
	        	return dto;
        	}
        	return null;
        }
	}
}
