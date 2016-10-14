package com.adfonic.presentation.location;

import com.adfonic.dto.geotarget.PostalCodeReferenceDto;

public interface PostalCodeReferenceDao {

	PostalCodeReferenceDto getLatLonFromPostalCode(Long countryId, String postalCode);
}
