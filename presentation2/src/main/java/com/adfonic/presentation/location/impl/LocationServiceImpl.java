package com.adfonic.presentation.location.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.adfonic.domain.Advertiser;
import com.adfonic.domain.Country;
import com.adfonic.domain.Country_;
import com.adfonic.domain.Geotarget;
import com.adfonic.domain.GeotargetType;
import com.adfonic.domain.Geotarget_;
import com.adfonic.domain.LocationTarget;
import com.adfonic.domain.LocationTarget_;
import com.adfonic.domain.Operator_;
import com.adfonic.domain.PostalAddress;
import com.adfonic.dto.address.PostalAddressDto;
import com.adfonic.dto.advertiser.AdvertiserDto;
import com.adfonic.dto.country.CountryDto;
import com.adfonic.dto.geotarget.GeotargetDto;
import com.adfonic.dto.geotarget.GeotargetTypeDto;
import com.adfonic.dto.geotarget.LocationTargetDto;
import com.adfonic.dto.geotarget.PostalCodeReferenceDto;
import com.adfonic.presentation.location.LocationService;
import com.adfonic.presentation.location.PostalCodeReferenceDao;
import com.adfonic.presentation.util.GenericServiceImpl;
import com.byyd.middleware.account.service.AccountManager;
import com.byyd.middleware.account.service.AdvertiserManager;
import com.byyd.middleware.campaign.filter.GeotargetFilter;
import com.byyd.middleware.campaign.service.TargetingManager;
import com.byyd.middleware.common.service.CommonManager;
import com.byyd.middleware.iface.dao.FetchStrategy;
import com.byyd.middleware.iface.dao.LikeSpec;
import com.byyd.middleware.iface.dao.SortOrder;
import com.byyd.middleware.iface.dao.Sorting;
import com.byyd.middleware.iface.dao.jpa.FetchStrategyBuilder;

@Service("locationService")
public class LocationServiceImpl extends GenericServiceImpl implements
		LocationService {
	
	@Autowired
	private CommonManager commonManager;
	@Autowired
    private AdvertiserManager advertiserManager;
	@Autowired
	private AccountManager accountManager;
	@Autowired
	private TargetingManager targetingManager;
    @Autowired
	private PostalCodeReferenceDao postalCodeReferenceDao;
    
    private static final FetchStrategy locationTargetFs = new FetchStrategyBuilder()
        .addLeft(LocationTarget_.advertiser)
        .build();
    
	public Collection<CountryDto> searchCountryByName(String search) {
		List<Country> countries = commonManager.getCountriesByName(search,
				LikeSpec.STARTS_WITH, false, // case sensitive
				false); // include hidden like "unknown"
		Collection<CountryDto> countriesDto = getDtoList(CountryDto.class, countries);
		return countriesDto;
	}
	
	// the list of countries supporting geotargetingby
    @Transactional(readOnly=true)
    public Collection<CountryDto> getAllCountries() {
        Sorting sort = new Sorting(SortOrder.asc(Country.class,"name"));
        List<Country> countries = commonManager.getAllCountries(sort);
        Collection<CountryDto> countriesDto = getDtoList(CountryDto.class,
                countries);
        return countriesDto;
    }

	// the list of countries supporting geotargetingby
	@Transactional(readOnly=true)
	public Collection<CountryDto> getAllGeoTargetingCountries() {
		return getAllGeoTargetingCountries(false);
	}
	
	@Transactional(readOnly=true)
	public Collection<CountryDto> getAllGeoTargetingCountries(boolean includeRadiusTypes) {
        List<Country> targetCountries = new ArrayList<Country>(0);
        List<GeotargetType> geotargetTypes =targetingManager.getAllGeotargetTypes();
        for(GeotargetType type : geotargetTypes) {
        	if(!includeRadiusTypes && type.getType().equals(GeotargetType.RADIUS_TYPE)) {
        		continue;
        	}
        	if(type.getCountries() != null) {
        		for(Country country : type.getCountries()) {
        			if(!targetCountries.contains(country)) {
        				targetCountries.add(country);
        			}
        		}
        	}
        }
        Collection<CountryDto> countriesDto = getDtoList(CountryDto.class, targetCountries);
        return countriesDto;
    }
	
	@Transactional(readOnly=true)
	public Collection<GeotargetTypeDto> getGeotargetingTypesForCountry(CountryDto countryDto) {
		return getGeotargetingTypesForCountry(countryDto, false);
	}
	
	@Transactional(readOnly=true)
    public Collection<GeotargetTypeDto> getGeotargetingTypesForCountry(CountryDto countryDto, Boolean isRadiusType) {
        String isocode  = countryDto.getIsoCode();
        List<GeotargetType> types = targetingManager.getAllGeotargetTypesForCountryIsoCode(isocode, isRadiusType);
        Collection<GeotargetTypeDto> typesDto = getDtoList(GeotargetTypeDto.class, types);
        return typesDto;
    }
    
    @Transactional(readOnly=true)
    public GeotargetTypeDto getGeotargetTypeById(Long id) {
        GeotargetType type = targetingManager.getGeotargetTypeById(id);
        if (type == null) {
            return null;
        }
        return getDtoObject(GeotargetTypeDto.class, type);
    }
    
	public CountryDto getCountryByName(String name) {
		Country c = commonManager.getCountryByName(name);
		if(c==null){
		    return null;
		}
		CountryDto dto = getDtoObject(CountryDto.class, c);
		return dto;
	}
	
	@Transactional(readOnly=true)
	public GeotargetTypeDto getGeotargetTypeByNameAndType(String name,String type) {
        GeotargetType t = targetingManager.getGeotargetTypeByNameAndType(name, type);
        if (t == null) {
            return null;
        }
        return getDtoObject(GeotargetTypeDto.class, t);
    }
	
	public CountryDto getCountryById(Long id) {
		Country c = commonManager.getCountryById(id);
		CountryDto dto = getDtoObject(CountryDto.class, c);
		return dto;
	}
	
	public CountryDto getCountryByIsoCode(String isoCode) {
		Country c = commonManager.getCountryByIsoCode(isoCode);
		CountryDto dto = getDtoObject(CountryDto.class, c);
		return dto;
	}
	
	@Override
	public GeotargetDto getGeotargetById(Long id) {
		  FetchStrategy targetFs = new FetchStrategyBuilder()
				.addLeft(Geotarget_.country)
				.addLeft(Country_.operators)
				.addLeft(Country_.region)
				.addLeft(Operator_.aliases)
				.addLeft(Operator_.operatorAliases)
				.addLeft(Operator_.group)
				.build();		
		return getGeotargetById(id, targetFs);
	}
	
	@Override
	public GeotargetDto getGeotargetWithCountryById(Long id) {
		  FetchStrategy targetFs = new FetchStrategyBuilder()
				.addLeft(Geotarget_.country)
				.build();		
		return getGeotargetById(id, targetFs);
	}
	
	private GeotargetDto getGeotargetById(Long id, FetchStrategy fetchStrategy) {
		return getDtoObject(GeotargetDto.class, targetingManager.getGeotargetById(id, fetchStrategy));
	}
	
	public Collection<GeotargetDto> getGeotargetsByNameAndTypeAndIsoCode(
			String search,
			String countryIsoCode,
			GeotargetType type
			/*String type*/) {
		  FetchStrategy targetFs = new FetchStrategyBuilder()
				.addLeft(Geotarget_.country)
				.build();
		  
		List<Geotarget> a =  targetingManager.getGeotargetsByNameAndTypeAndIsoCode(
                countryIsoCode,													//isoCode,
                type,
                search,															//name,
                true,															//caseSensitive,
                LikeSpec.STARTS_WITH,targetFs);									//like);
		
		Collection<GeotargetDto> countriesDto = getDtoList(GeotargetDto.class,
				a);
		
		return countriesDto;
	}
	
	public Collection<GeotargetDto> getGeotargetsByNameAndTypeAndIsoCode(
            String search,
            String countryIsoCode, 
            GeotargetTypeDto type){
	    
	    GeotargetType geotargetType = targetingManager.getGeotargetTypeById(type.getId()); 
	    
	    return getGeotargetsByNameAndTypeAndIsoCode(search, countryIsoCode, geotargetType);
	}
	
	public GeotargetDto getGeotargetByNameAndTypeAndIsoCode(
			String search,
			String countryIsoCode,
			GeotargetType type
			/*String type*/) {
        FetchStrategy targetFs = new FetchStrategyBuilder()
              .addLeft(Geotarget_.country)
              .build();
        
      List<Geotarget> a =  targetingManager.getGeotargetsByNameAndTypeAndIsoCode(
              countryIsoCode,                                                 //isoCode,
              type,
              search,                                                         //name,
              true,                                                           //caseSensitive,
              LikeSpec.STARTS_WITH,targetFs);                                 //like);
          
      // Check if the geotargets match exactly the name
      Geotarget checkedTarget = null;
      if(a!=null){
          for(Geotarget geo : a){
              if(geo.getName().toLowerCase().equals(search.toLowerCase())){
                  checkedTarget = geo;
              }
          }
      }
      if(checkedTarget!=null){
          return getDtoObject(GeotargetDto.class, checkedTarget);
      }
      return null;
	}
	
	public GeotargetDto getGeotargetByNameAndTypeAndIsoCode(String search, 
            String countryIsoCode, 
            GeotargetTypeDto type){
        
        GeotargetType geotargetType = targetingManager.getGeotargetTypeById(type.getId()); 
        
        return getGeotargetByNameAndTypeAndIsoCode(search, countryIsoCode, geotargetType);
        
    }

    public Collection<GeotargetDto> getGeotargetsByTypeAndIsoCode(
    		String countryIsoCode, 
    		GeotargetType type
			/*String type*/) {
        FetchStrategy targetFs = new FetchStrategyBuilder()
              .addLeft(Geotarget_.country)
              .build();
        GeotargetFilter filter = new GeotargetFilter();
        //filter.setType(getEnumType(type));
        filter.setType(type);
        filter.setCountryIsoCode(countryIsoCode);
        List<Geotarget> geos = targetingManager.getAllGeotargets(filter, new Sorting(SortOrder.asc("name")), targetFs);
        return getDtoList(GeotargetDto.class, geos);
    }
    
    public Collection<GeotargetDto> getGeotargetsByTypeAndIsoCode(
            String countryIsoCode, 
            GeotargetTypeDto type){
        
        GeotargetType geotargetType = targetingManager.getGeotargetTypeById(type.getId()); 
        
        return getGeotargetsByTypeAndIsoCode(countryIsoCode, geotargetType);
    }
	    
	@Override
    @Transactional(readOnly = false)
	public LocationTargetDto createLocationTarget(AdvertiserDto advertiserDto,String name, BigDecimal latitude, BigDecimal longitude, BigDecimal radius){
	    Advertiser advertiser = advertiserManager.getAdvertiserById(advertiserDto.getId());
	    if(advertiser!=null){
	        LocationTarget locationTarget = targetingManager.newLocationTarget(advertiser, name, latitude, longitude, radius,locationTargetFs);
	        if(locationTarget!=null){
	            return getDtoObject(LocationTargetDto.class, locationTarget);
	        }
	    }
	    return null;
	}
	    
	public PostalCodeReferenceDto getLatLonFromPostalCode(CountryDto countryDto, String postalCode) {
		return postalCodeReferenceDao.getLatLonFromPostalCode(countryDto.getId(), postalCode);
	}
	
	public LocationTargetDto searchLocationFromCode(CountryDto country, String postCode){
	    PostalCodeReferenceDto reference = getLatLonFromPostalCode(country, postCode);
	    if(reference!=null){
	        LocationTargetDto location = new LocationTargetDto();
	        location.setName(postCode);
	        location.setLatitude(reference.getLatitude());
	        location.setLongitude(reference.getLongitude());
	        return location;
	    }
	    return null;
	}
	
	//------------------------------------------------------------------------------------------------------------------
	
	public void initPostalAddress(PostalAddress postalAddress, PostalAddressDto postalAddressDto) {
		postalAddress.setFirstName(postalAddressDto.getFirstName());
		postalAddress.setLastName(postalAddressDto.getLastName());
		postalAddress.setAddress1(postalAddressDto.getAddress1());
		postalAddress.setAddress2(postalAddressDto.getAddress2());
		postalAddress.setCity(postalAddressDto.getCity());
		postalAddress.setState(postalAddressDto.getState());
		postalAddress.setPostcode(postalAddressDto.getPostcode());
		if(postalAddressDto.getCountry() != null) {
			Country country = commonManager.getCountryById(postalAddressDto.getCountry().getId());
			postalAddress.setCountry(country);
		}
	}
	
	@Transactional(readOnly=true)
	public PostalAddressDto getPostalAddressById(Long id) {
		PostalAddress postalAddress = accountManager.getPostalAddressById(id);
		if(postalAddress == null) {
			return null;
		}
		return super.getDtoObject(PostalAddressDto.class, postalAddress);
	}
	
	@Transactional(readOnly=false)
	public PostalAddressDto createPostalAddress(PostalAddressDto postalAddressDto) {
		if(postalAddressDto.persisted()) {
			return updatePostalAddress(postalAddressDto);
		}
		PostalAddress postalAddress = new PostalAddress();
		this.initPostalAddress(postalAddress, postalAddressDto);
		postalAddress = accountManager.create(postalAddress);
		// Wanna make sure we use the JDTO stuff
		return super.getDtoObject(PostalAddressDto.class, postalAddress);
	}
	
	@Transactional(readOnly=false)
	public PostalAddressDto updatePostalAddress(PostalAddressDto postalAddressDto) {
		if(!postalAddressDto.persisted()) {
			return this.createPostalAddress(postalAddressDto);
		}
		PostalAddress postalAddress = accountManager.getPostalAddressById(postalAddressDto.getId());
		if(postalAddress == null) {
			return null;
		}
		this.initPostalAddress(postalAddress, postalAddressDto);
		postalAddress = accountManager.update(postalAddress);
		// Wanna make sure we use the JDTO stuff
		return super.getDtoObject(PostalAddressDto.class, postalAddress);
	}
	
	@Transactional(readOnly=false)
	public void deletePostalAddress(PostalAddressDto postalAddressDto) {
		if(!postalAddressDto.persisted()) {
			return;
		}
		PostalAddress postalAddress = accountManager.getPostalAddressById(postalAddressDto.getId());
		if(postalAddress == null) {
			return;
		}
		accountManager.delete(postalAddress);
	}


}
