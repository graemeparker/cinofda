package com.adfonic.presentation.location;

import java.math.BigDecimal;
import java.util.Collection;

import com.adfonic.domain.GeotargetType;
import com.adfonic.dto.address.PostalAddressDto;
import com.adfonic.dto.advertiser.AdvertiserDto;
import com.adfonic.dto.country.CountryDto;
import com.adfonic.dto.geotarget.GeotargetDto;
import com.adfonic.dto.geotarget.GeotargetTypeDto;
import com.adfonic.dto.geotarget.LocationTargetDto;
import com.adfonic.dto.geotarget.PostalCodeReferenceDto;

public interface LocationService {
	public Collection<CountryDto> searchCountryByName(String search);

	public CountryDto getCountryByName(String name);
	
	public CountryDto getCountryById(Long id);
	
	public Collection<GeotargetDto> getGeotargetsByNameAndTypeAndIsoCode(
            String search,
            String countryIsoCode, 
            GeotargetTypeDto type);
	
	public Collection<GeotargetDto> getGeotargetsByNameAndTypeAndIsoCode(
			String search,
			String countryIsoCode, 
			/*String type*/ GeotargetType type);
	
    public Collection<GeotargetDto> getGeotargetsByTypeAndIsoCode(String countryIsoCode, /*String type*/ GeotargetType type);
    
    public Collection<GeotargetDto> getGeotargetsByTypeAndIsoCode(String countryIsoCode, GeotargetTypeDto type);
    
	public GeotargetDto getGeotargetByNameAndTypeAndIsoCode(String search,String countryIsoCode,/*String type*/ GeotargetType type);
	
	public GeotargetDto getGeotargetByNameAndTypeAndIsoCode(String search,String countryIsoCode, GeotargetTypeDto type);
	
	public GeotargetDto getGeotargetById(Long id);
	
	public GeotargetDto getGeotargetWithCountryById(Long id);
	
	public CountryDto getCountryByIsoCode(String isoCode);
	
	public Collection<CountryDto> getAllCountries();

    public Collection<CountryDto> getAllGeoTargetingCountries();
    
    public Collection<CountryDto> getAllGeoTargetingCountries(boolean includeRadiusTypes);
    
    public Collection<GeotargetTypeDto> getGeotargetingTypesForCountry(CountryDto countryDto);

    public Collection<GeotargetTypeDto> getGeotargetingTypesForCountry(CountryDto countryDto, Boolean isRadiusType);
    
    public GeotargetTypeDto getGeotargetTypeById(Long id);
    
    public GeotargetTypeDto getGeotargetTypeByNameAndType(String name,String type);
    
    public PostalCodeReferenceDto getLatLonFromPostalCode(CountryDto countryDto, String postalCode);
    
    public LocationTargetDto createLocationTarget(AdvertiserDto advertiser,String name, BigDecimal latitude, BigDecimal longitude, BigDecimal radius);
    
    public LocationTargetDto searchLocationFromCode(CountryDto country, String postCode);
    
    //---------------------------------------------------------------------------------------------------------
    
	PostalAddressDto getPostalAddressById(Long id);
	PostalAddressDto createPostalAddress(PostalAddressDto postalAddressDto);
	PostalAddressDto updatePostalAddress(PostalAddressDto postalAddressDto);
	void deletePostalAddress(PostalAddressDto postalAddressDto);


}
 