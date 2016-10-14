package com.adfonic.adserver.deriver.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.math.BigDecimal;

import javax.xml.bind.JAXBElement;

import org.jmock.Expectations;
import org.junit.Before;
import org.junit.Test;

import com.adfonic.adserver.Parameters;
import com.adfonic.adserver.TargetingContext;
import com.adfonic.adserver.deriver.DeriverManager;
import com.adfonic.domain.cache.DomainCache;
import com.adfonic.domain.cache.dto.adserver.CountryDto;
import com.adfonic.geo.CanadianProvince;
import com.adfonic.geo.Coordinates;
import com.adfonic.geo.Dma;
import com.adfonic.geo.DmaManager;
import com.adfonic.geo.USState;
import com.adfonic.quova.QuovaClient;
import com.adfonic.test.AbstractAdfonicTest;
import com.adfonic.util.stats.CounterManager;
import com.quova.data._1.CityDataType;
import com.quova.data._1.CountryDataType;
import com.quova.data._1.Ipinfo;
import com.quova.data._1.LocationType;
import com.quova.data._1.StateDataType;

@SuppressWarnings("unchecked")
public class TestQuovaGeoDeriverImpl extends AbstractAdfonicTest {
	private DeriverManager deriverManager;
    private QuovaClient quovaClient;
    private DmaManager dmaManager;
	private QuovaGeoDeriverImpl quovaGeoDeriverImpl;
	private TargetingContext context;
    private DomainCache domainCache;
    private CounterManager counterManager;
	
	@Before
	public void initTests() throws InstantiationException, IllegalAccessException{
		deriverManager = new DeriverManager();
	    quovaClient = mock(QuovaClient.class);
	    counterManager = new CounterManager();
		
        quovaGeoDeriverImpl = new QuovaGeoDeriverImpl(deriverManager, quovaClient);
        
        inject(quovaGeoDeriverImpl, "counterManager", counterManager);
        
	    dmaManager = mock(DmaManager.class);
	    inject(quovaGeoDeriverImpl, "dmaManager", dmaManager);
        
		context = mock(TargetingContext.class);
        domainCache = mock(DomainCache.class);
	}

    @Test
    public void testGetAttribute01_quova_ip_info() {
		expect(new Expectations() {{
			oneOf (context).getAttribute(Parameters.IP); will(returnValue(null));
		}});
		assertNull(quovaGeoDeriverImpl.getAttribute(TargetingContext.QUOVA_IP_INFO, context));
    }

    @Test(expected=IllegalArgumentException.class)
    public void testGetAttribute02_anything_else() {
        quovaGeoDeriverImpl.getAttribute("something random", context);
    }

	@Test
	public void testDeriveQuovaIpinfo01_no_ip() {
		expect(new Expectations() {{
			oneOf (context).getAttribute(Parameters.IP); will(returnValue(null));
		}});
		assertNull(quovaGeoDeriverImpl.deriveQuovaIpinfo(context));
	}
	
	@Test
	public void testDeriveQuovaIpinfo02_normal() throws Exception {
        final String ip = "localhost";
        final Ipinfo ipinfo = mock(Ipinfo.class);
		expect(new Expectations() {{
			allowing (context).getAttribute(Parameters.IP); will(returnValue(ip));
            oneOf (quovaClient).getIpinfo(ip); will(returnValue(ipinfo));
		}});
        assertEquals(ipinfo, quovaGeoDeriverImpl.deriveQuovaIpinfo(context));
	}
	
	@Test
	public void testDeriveQuovaIpinfo03_exception() throws Exception {
        final String ip = "localhost";
		expect(new Expectations() {{
			allowing (context).getAttribute(Parameters.IP); will(returnValue(ip));
            oneOf (quovaClient).getIpinfo(ip); will(throwException(new IllegalStateException("bummer")));
		}});
        assertNull(quovaGeoDeriverImpl.deriveQuovaIpinfo(context));
	}
	
	@Test
	public void testDeriveCountryFromIp01_no_ip_info() {
        expect(new Expectations() {{
            oneOf (context).getAttribute(TargetingContext.QUOVA_IP_INFO); will(returnValue(null));
        }});
        assertNull(quovaGeoDeriverImpl.deriveCountryFromIp(context));
    }
    
	@Test
	public void testDeriveCountryFromIp02_no_location() {
        final Ipinfo ipinfo = mock(Ipinfo.class);
        expect(new Expectations() {{
            oneOf (context).getAttribute(TargetingContext.QUOVA_IP_INFO); will(returnValue(ipinfo));
            allowing (ipinfo).getLocation(); will(returnValue(null));
        }});
        assertNull(quovaGeoDeriverImpl.deriveCountryFromIp(context));
    }
    
	@Test
	public void testDeriveCountryFromIp03_no_country_data() {
        final Ipinfo ipinfo = mock(Ipinfo.class);
        final LocationType location = mock(LocationType.class, "location");
        expect(new Expectations() {{
            oneOf (context).getAttribute(TargetingContext.QUOVA_IP_INFO); will(returnValue(ipinfo));
            allowing (ipinfo).getLocation(); will(returnValue(location));
            allowing (location).getCountryData(); will(returnValue(null));
        }});
        assertNull(quovaGeoDeriverImpl.deriveCountryFromIp(context));
    }
    
	@Test
	public void testDeriveCountryFromIp04_no_country_code() {
        final Ipinfo ipinfo = mock(Ipinfo.class);
        final LocationType location = mock(LocationType.class, "location");
        final CountryDataType countryData = mock(CountryDataType.class, "countryData");
        expect(new Expectations() {{
            oneOf (context).getAttribute(TargetingContext.QUOVA_IP_INFO); will(returnValue(ipinfo));
            allowing (ipinfo).getLocation(); will(returnValue(location));
            allowing (location).getCountryData(); will(returnValue(countryData));
            allowing (countryData).getCountryCode(); will(returnValue(null));
        }});
        assertNull(quovaGeoDeriverImpl.deriveCountryFromIp(context));
    }
    
	@Test
	public void testDeriveCountryFromIp05_no_country() {
        final Ipinfo ipinfo = mock(Ipinfo.class);
        final LocationType location = mock(LocationType.class, "location");
        final CountryDataType countryData = mock(CountryDataType.class, "countryData");
        final String countryCode = randomAlphaNumericString(10).toLowerCase();
        expect(new Expectations() {{
            oneOf (context).getAttribute(TargetingContext.QUOVA_IP_INFO); will(returnValue(ipinfo));
            allowing (ipinfo).getLocation(); will(returnValue(location));
            allowing (location).getCountryData(); will(returnValue(countryData));
            allowing (countryData).getCountryCode(); will(returnValue(countryCode));
            oneOf (context).getDomainCache(); will(returnValue(domainCache));
            oneOf (domainCache).getCountryByIsoCode(countryCode.toUpperCase()); will(returnValue(null));
        }});
        assertNull(quovaGeoDeriverImpl.deriveCountryFromIp(context));
    }
    
	@Test
	public void testDeriveCountryFromIp06_found_country() {
        final Ipinfo ipinfo = mock(Ipinfo.class);
        final LocationType location = mock(LocationType.class, "location");
        final CountryDataType countryData = mock(CountryDataType.class, "countryData");
        final String countryCode = randomAlphaNumericString(10).toLowerCase();
        final CountryDto country = mock(CountryDto.class);
        expect(new Expectations() {{
            oneOf (context).getAttribute(TargetingContext.QUOVA_IP_INFO); will(returnValue(ipinfo));
            allowing (ipinfo).getLocation(); will(returnValue(location));
            allowing (location).getCountryData(); will(returnValue(countryData));
            allowing (countryData).getCountryCode(); will(returnValue(countryCode));
            oneOf (context).getDomainCache(); will(returnValue(domainCache));
            oneOf (domainCache).getCountryByIsoCode(countryCode.toUpperCase()); will(returnValue(country));
        }});
        assertEquals(country, quovaGeoDeriverImpl.deriveCountryFromIp(context));
    }
	
	@Test
	public void testDeriveTimeZoneFromIp01_no_ip_info() {
        expect(new Expectations() {{
            oneOf (context).getAttribute(TargetingContext.QUOVA_IP_INFO); will(returnValue(null));
        }});
        assertNull(quovaGeoDeriverImpl.deriveTimeZoneFromIp(context));
    }
    
	@Test
	public void testDeriveTimeZoneFromIp02_no_location() {
        final Ipinfo ipinfo = mock(Ipinfo.class);
        expect(new Expectations() {{
            oneOf (context).getAttribute(TargetingContext.QUOVA_IP_INFO); will(returnValue(ipinfo));
            allowing (ipinfo).getLocation(); will(returnValue(null));
        }});
        assertNull(quovaGeoDeriverImpl.deriveTimeZoneFromIp(context));
    }
    
	@Test
	public void testDeriveTimeZoneFromIp03_no_city_data() {
        final Ipinfo ipinfo = mock(Ipinfo.class);
        final LocationType location = mock(LocationType.class, "location");
        expect(new Expectations() {{
            oneOf (context).getAttribute(TargetingContext.QUOVA_IP_INFO); will(returnValue(ipinfo));
            allowing (ipinfo).getLocation(); will(returnValue(location));
            allowing (location).getCityData(); will(returnValue(null));
        }});
        assertNull(quovaGeoDeriverImpl.deriveTimeZoneFromIp(context));
    }
    
	@Test
	public void testDeriveTimeZoneFromIp04_no_time_zone() {
        final Ipinfo ipinfo = mock(Ipinfo.class);
        final LocationType location = mock(LocationType.class, "location");
        final CityDataType cityData = mock(CityDataType.class, "cityData");
        expect(new Expectations() {{
            oneOf (context).getAttribute(TargetingContext.QUOVA_IP_INFO); will(returnValue(ipinfo));
            allowing (ipinfo).getLocation(); will(returnValue(location));
            allowing (location).getCityData(); will(returnValue(cityData));
            allowing (cityData).getTimeZone(); will(returnValue(null));
        }});
        assertNull(quovaGeoDeriverImpl.deriveTimeZoneFromIp(context));
    }
    
	@Test
	public void testDeriveTimeZoneFromIp05_no_tz_value() {
        final Ipinfo ipinfo = mock(Ipinfo.class);
        final LocationType location = mock(LocationType.class, "location");
        final CityDataType cityData = mock(CityDataType.class, "cityData");
		final JAXBElement<BigDecimal> timeZoneElement = mock(JAXBElement.class);
        expect(new Expectations() {{
            oneOf (context).getAttribute(TargetingContext.QUOVA_IP_INFO); will(returnValue(ipinfo));
            allowing (ipinfo).getLocation(); will(returnValue(location));
            allowing (location).getCityData(); will(returnValue(cityData));
            allowing (cityData).getTimeZone(); will(returnValue(timeZoneElement));
            allowing (timeZoneElement).getValue(); will(returnValue(null));
        }});
        assertNull(quovaGeoDeriverImpl.deriveTimeZoneFromIp(context));
    }
    
	@Test
	public void testDeriveTimeZoneFromIp06_normal() {
        final Ipinfo ipinfo = mock(Ipinfo.class);
        final LocationType location = mock(LocationType.class, "location");
        final CityDataType cityData = mock(CityDataType.class, "cityData");
		final JAXBElement<BigDecimal> timeZoneElement = mock(JAXBElement.class);
        final BigDecimal tz = new BigDecimal(5.0);
        expect(new Expectations() {{
            oneOf (context).getAttribute(TargetingContext.QUOVA_IP_INFO); will(returnValue(ipinfo));
            allowing (ipinfo).getLocation(); will(returnValue(location));
            allowing (location).getCityData(); will(returnValue(cityData));
            allowing (cityData).getTimeZone(); will(returnValue(timeZoneElement));
            allowing (timeZoneElement).getValue(); will(returnValue(tz));
        }});
        assertNotNull(quovaGeoDeriverImpl.deriveTimeZoneFromIp(context));
    }
	
	@Test
	public void testDeriveCoordinatesFromIp01_no_ip_info() {
        expect(new Expectations() {{
            oneOf (context).getAttribute(TargetingContext.QUOVA_IP_INFO); will(returnValue(null));
        }});
        assertNull(quovaGeoDeriverImpl.deriveCoordinatesFromIp(context));
    }
    
	@Test
	public void testDeriveCoordinatesFromIp02_no_location() {
        final Ipinfo ipinfo = mock(Ipinfo.class);
        expect(new Expectations() {{
            oneOf (context).getAttribute(TargetingContext.QUOVA_IP_INFO); will(returnValue(ipinfo));
            allowing (ipinfo).getLocation(); will(returnValue(null));
        }});
        assertNull(quovaGeoDeriverImpl.deriveCoordinatesFromIp(context));
    }
    
	@Test
	public void testDeriveCoordinatesFromIp03_no_latitude() {
        final Ipinfo ipinfo = mock(Ipinfo.class);
        final LocationType location = mock(LocationType.class, "location");
        expect(new Expectations() {{
            oneOf (context).getAttribute(TargetingContext.QUOVA_IP_INFO); will(returnValue(ipinfo));
            allowing (ipinfo).getLocation(); will(returnValue(location));
            allowing (location).getLatitude(); will(returnValue(null));
        }});
        assertNull(quovaGeoDeriverImpl.deriveCoordinatesFromIp(context));
    }
    
	@Test
	public void testDeriveCoordinatesFromIp04_no_longitude() {
        final Ipinfo ipinfo = mock(Ipinfo.class);
        final LocationType location = mock(LocationType.class, "location");
        final JAXBElement<BigDecimal> latitude = mock(JAXBElement.class, "latitude");
        expect(new Expectations() {{
            oneOf (context).getAttribute(TargetingContext.QUOVA_IP_INFO); will(returnValue(ipinfo));
            allowing (ipinfo).getLocation(); will(returnValue(location));
            allowing (location).getLatitude(); will(returnValue(latitude));
            allowing (location).getLongitude(); will(returnValue(null));
        }});
        assertNull(quovaGeoDeriverImpl.deriveCoordinatesFromIp(context));
    }
    
	@Test
	public void testDeriveCoordinatesFromIp05_no_latitude_value() {
        final Ipinfo ipinfo = mock(Ipinfo.class);
        final LocationType location = mock(LocationType.class, "location");
        final JAXBElement<BigDecimal> latitude = mock(JAXBElement.class, "latitude");
        final JAXBElement<BigDecimal> longitude = mock(JAXBElement.class, "longitude");
        final BigDecimal longitudeValue = new BigDecimal(0.0);
        expect(new Expectations() {{
            oneOf (context).getAttribute(TargetingContext.QUOVA_IP_INFO); will(returnValue(ipinfo));
            allowing (ipinfo).getLocation(); will(returnValue(location));
            allowing (location).getLatitude(); will(returnValue(latitude));
            allowing (location).getLongitude(); will(returnValue(longitude));
            allowing (latitude).getValue(); will(returnValue(null));
            allowing (longitude).getValue(); will(returnValue(longitudeValue));
        }});
        assertNull(quovaGeoDeriverImpl.deriveCoordinatesFromIp(context));
    }
    
	@Test
	public void testDeriveCoordinatesFromIp06_no_longitude_value() {
        final Ipinfo ipinfo = mock(Ipinfo.class);
        final LocationType location = mock(LocationType.class, "location");
        final JAXBElement<BigDecimal> latitude = mock(JAXBElement.class, "latitude");
        final JAXBElement<BigDecimal> longitude = mock(JAXBElement.class, "longitude");
        final BigDecimal latitudeValue = new BigDecimal(0.0);
        expect(new Expectations() {{
            oneOf (context).getAttribute(TargetingContext.QUOVA_IP_INFO); will(returnValue(ipinfo));
            allowing (ipinfo).getLocation(); will(returnValue(location));
            allowing (location).getLatitude(); will(returnValue(latitude));
            allowing (location).getLongitude(); will(returnValue(longitude));
            allowing (latitude).getValue(); will(returnValue(latitudeValue));
            allowing (longitude).getValue(); will(returnValue(null));
        }});
        assertNull(quovaGeoDeriverImpl.deriveCoordinatesFromIp(context));
    }
    
	@Test
	public void testDeriveCoordinatesFromIp07_no_longitude_value() {
        final Ipinfo ipinfo = mock(Ipinfo.class);
        final LocationType location = mock(LocationType.class, "location");
        final JAXBElement<BigDecimal> latitude = mock(JAXBElement.class, "latitude");
        final JAXBElement<BigDecimal> longitude = mock(JAXBElement.class, "longitude");
        final BigDecimal latitudeValue = new BigDecimal(0.0);
        expect(new Expectations() {{
            oneOf (context).getAttribute(TargetingContext.QUOVA_IP_INFO); will(returnValue(ipinfo));
            allowing (ipinfo).getLocation(); will(returnValue(location));
            allowing (location).getLatitude(); will(returnValue(latitude));
            allowing (location).getLongitude(); will(returnValue(longitude));
            allowing (latitude).getValue(); will(returnValue(latitudeValue));
            allowing (longitude).getValue(); will(returnValue(null));
        }});
        assertNull(quovaGeoDeriverImpl.deriveCoordinatesFromIp(context));
    }
    
	@Test
	public void testDeriveCoordinatesFromIp08_invalid() {
        final Ipinfo ipinfo = mock(Ipinfo.class);
        final LocationType location = mock(LocationType.class, "location");
        final JAXBElement<BigDecimal> latitude = mock(JAXBElement.class, "latitude");
        final JAXBElement<BigDecimal> longitude = mock(JAXBElement.class, "longitude");
        final BigDecimal latitudeValue = new BigDecimal("90.1");
        final BigDecimal longitudeValue = new BigDecimal("180.1");
        expect(new Expectations() {{
            oneOf (context).getAttribute(TargetingContext.QUOVA_IP_INFO); will(returnValue(ipinfo));
            allowing (ipinfo).getLocation(); will(returnValue(location));
            allowing (location).getLatitude(); will(returnValue(latitude));
            allowing (location).getLongitude(); will(returnValue(longitude));
            allowing (latitude).getValue(); will(returnValue(latitudeValue));
            allowing (longitude).getValue(); will(returnValue(longitudeValue));
        }});
        assertNull(quovaGeoDeriverImpl.deriveCoordinatesFromIp(context));
    }
    
	@Test
	public void testDeriveCoordinatesFromIp09_valid() {
        final Ipinfo ipinfo = mock(Ipinfo.class);
        final LocationType location = mock(LocationType.class, "location");
        final JAXBElement<BigDecimal> latitude = mock(JAXBElement.class, "latitude");
        final JAXBElement<BigDecimal> longitude = mock(JAXBElement.class, "longitude");
        final BigDecimal latitudeValue = new BigDecimal("12.345");
        final BigDecimal longitudeValue = new BigDecimal("-123.456");
        expect(new Expectations() {{
            oneOf (context).getAttribute(TargetingContext.QUOVA_IP_INFO); will(returnValue(ipinfo));
            allowing (ipinfo).getLocation(); will(returnValue(location));
            allowing (location).getLatitude(); will(returnValue(latitude));
            allowing (location).getLongitude(); will(returnValue(longitude));
            allowing (latitude).getValue(); will(returnValue(latitudeValue));
            allowing (longitude).getValue(); will(returnValue(longitudeValue));
        }});
        Coordinates coords = quovaGeoDeriverImpl.deriveCoordinatesFromIp(context);
        assertNotNull(coords);
        assertEquals(latitudeValue.doubleValue(), coords.getLatitude(), 0.0);
        assertEquals(longitudeValue.doubleValue(), coords.getLongitude(), 0.0);
    }

    @Test
    public void testDerivePostalCodeFromIp01_no_ip_info() {
        expect(new Expectations() {{
            oneOf (context).getAttribute(TargetingContext.QUOVA_IP_INFO); will(returnValue(null));
        }});
        assertNull(quovaGeoDeriverImpl.derivePostalCodeFromIp(context));
    }

    @Test
    public void testDerivePostalCodeFromIp02_no_location() {
        final Ipinfo ipinfo = mock(Ipinfo.class);
        expect(new Expectations() {{
            oneOf (context).getAttribute(TargetingContext.QUOVA_IP_INFO); will(returnValue(ipinfo));
            allowing (ipinfo).getLocation(); will(returnValue(null));
        }});
        assertNull(quovaGeoDeriverImpl.derivePostalCodeFromIp(context));
    }

    @Test
    public void testDerivePostalCodeFromIp03_no_city_data() {
        final Ipinfo ipinfo = mock(Ipinfo.class);
        final LocationType location = mock(LocationType.class, "location");
        expect(new Expectations() {{
            oneOf (context).getAttribute(TargetingContext.QUOVA_IP_INFO); will(returnValue(ipinfo));
            allowing (ipinfo).getLocation(); will(returnValue(location));
            allowing (location).getCityData(); will(returnValue(null));
        }});
        assertNull(quovaGeoDeriverImpl.derivePostalCodeFromIp(context));
    }

    @Test
    public void testDerivePostalCodeFromIp04_normal() {
        final Ipinfo ipinfo = mock(Ipinfo.class);
        final LocationType location = mock(LocationType.class, "location");
        final CityDataType cityData = mock(CityDataType.class, "cityData");
        final String postalCode = randomAlphaNumericString(10);
        expect(new Expectations() {{
            oneOf (context).getAttribute(TargetingContext.QUOVA_IP_INFO); will(returnValue(ipinfo));
            allowing (ipinfo).getLocation(); will(returnValue(location));
            allowing (location).getCityData(); will(returnValue(cityData));
            allowing (cityData).getPostalCode(); will(returnValue(postalCode));
        }});
        assertEquals(postalCode, quovaGeoDeriverImpl.derivePostalCodeFromIp(context));
    }

    @Test
    public void testDeriveUSStateFromIp01_no_ip_info() {
        expect(new Expectations() {{
            oneOf (context).getAttribute(TargetingContext.QUOVA_IP_INFO); will(returnValue(null));
        }});
        assertNull(quovaGeoDeriverImpl.deriveUSStateFromIp(context));
    }

    @Test
    public void testDeriveUSStateFromIp02_no_location() {
        final Ipinfo ipinfo = mock(Ipinfo.class);
        expect(new Expectations() {{
            oneOf (context).getAttribute(TargetingContext.QUOVA_IP_INFO); will(returnValue(ipinfo));
            allowing (ipinfo).getLocation(); will(returnValue(null));
        }});
        assertNull(quovaGeoDeriverImpl.deriveUSStateFromIp(context));
    }

    @Test
    public void testDeriveUSStateFromIp03_no_state_data() {
        final Ipinfo ipinfo = mock(Ipinfo.class);
        final LocationType location = mock(LocationType.class, "location");
        expect(new Expectations() {{
            oneOf (context).getAttribute(TargetingContext.QUOVA_IP_INFO); will(returnValue(ipinfo));
            allowing (ipinfo).getLocation(); will(returnValue(location));
            allowing (location).getStateData(); will(returnValue(null));
        }});
        assertNull(quovaGeoDeriverImpl.deriveUSStateFromIp(context));
    }

    @Test
    public void testDeriveUSStateFromIp04_no_country_data() {
        final Ipinfo ipinfo = mock(Ipinfo.class);
        final LocationType location = mock(LocationType.class, "location");
        final StateDataType stateData = mock(StateDataType.class, "stateData");
        expect(new Expectations() {{
            oneOf (context).getAttribute(TargetingContext.QUOVA_IP_INFO); will(returnValue(ipinfo));
            allowing (ipinfo).getLocation(); will(returnValue(location));
            allowing (location).getStateData(); will(returnValue(stateData));
            allowing (location).getCountryData(); will(returnValue(null));
        }});
        assertNull(quovaGeoDeriverImpl.deriveUSStateFromIp(context));
    }

    @Test
    public void testDeriveUSStateFromIp05_non_us_country() {
        final Ipinfo ipinfo = mock(Ipinfo.class);
        final LocationType location = mock(LocationType.class, "location");
        final StateDataType stateData = mock(StateDataType.class, "stateData");
        final CountryDataType countryData = mock(CountryDataType.class, "countryData");
        expect(new Expectations() {{
            oneOf (context).getAttribute(TargetingContext.QUOVA_IP_INFO); will(returnValue(ipinfo));
            allowing (ipinfo).getLocation(); will(returnValue(location));
            allowing (location).getStateData(); will(returnValue(stateData));
            allowing (location).getCountryData(); will(returnValue(countryData));
            allowing (countryData).getCountryCode(); will(returnValue("ca"));
        }});
        assertNull(quovaGeoDeriverImpl.deriveUSStateFromIp(context));
    }

    @Test
    public void testDeriveUSStateFromIp06_no_state_code() {
        final Ipinfo ipinfo = mock(Ipinfo.class);
        final LocationType location = mock(LocationType.class, "location");
        final StateDataType stateData = mock(StateDataType.class, "stateData");
        final CountryDataType countryData = mock(CountryDataType.class, "countryData");
        expect(new Expectations() {{
            oneOf (context).getAttribute(TargetingContext.QUOVA_IP_INFO); will(returnValue(ipinfo));
            allowing (ipinfo).getLocation(); will(returnValue(location));
            allowing (location).getStateData(); will(returnValue(stateData));
            allowing (location).getCountryData(); will(returnValue(countryData));
            allowing (countryData).getCountryCode(); will(returnValue("us"));
            allowing (stateData).getStateCode(); will(returnValue(null));
        }});
        assertNull(quovaGeoDeriverImpl.deriveUSStateFromIp(context));
    }

    @Test
    public void testDeriveUSStateFromIp07_unrecognized_state() {
        final Ipinfo ipinfo = mock(Ipinfo.class);
        final LocationType location = mock(LocationType.class, "location");
        final StateDataType stateData = mock(StateDataType.class, "stateData");
        final CountryDataType countryData = mock(CountryDataType.class, "countryData");
        expect(new Expectations() {{
            oneOf (context).getAttribute(TargetingContext.QUOVA_IP_INFO); will(returnValue(ipinfo));
            allowing (ipinfo).getLocation(); will(returnValue(location));
            allowing (location).getStateData(); will(returnValue(stateData));
            allowing (location).getCountryData(); will(returnValue(countryData));
            allowing (countryData).getCountryCode(); will(returnValue("us"));
            allowing (stateData).getStateCode(); will(returnValue("not a state"));
        }});
        assertNull(quovaGeoDeriverImpl.deriveUSStateFromIp(context));
    }

    @Test
    public void testDeriveUSStateFromIp08_normal() {
        final Ipinfo ipinfo = mock(Ipinfo.class);
        final LocationType location = mock(LocationType.class, "location");
        final StateDataType stateData = mock(StateDataType.class, "stateData");
        final CountryDataType countryData = mock(CountryDataType.class, "countryData");
        final USState usState = USState.KY;
        expect(new Expectations() {{
            oneOf (context).getAttribute(TargetingContext.QUOVA_IP_INFO); will(returnValue(ipinfo));
            allowing (ipinfo).getLocation(); will(returnValue(location));
            allowing (location).getStateData(); will(returnValue(stateData));
            allowing (location).getCountryData(); will(returnValue(countryData));
            allowing (countryData).getCountryCode(); will(returnValue("us"));
            allowing (stateData).getStateCode(); will(returnValue(usState.name().toLowerCase()));
        }});
        assertEquals(usState, quovaGeoDeriverImpl.deriveUSStateFromIp(context));
    }

    @Test
    public void testDeriveCanadianProvinceFromIp01_no_ip_info() {
        expect(new Expectations() {{
            oneOf (context).getAttribute(TargetingContext.QUOVA_IP_INFO); will(returnValue(null));
        }});
        assertNull(quovaGeoDeriverImpl.deriveCanadianProvinceFromIp(context));
    }

    @Test
    public void testDeriveCanadianProvinceFromIp02_no_location() {
        final Ipinfo ipinfo = mock(Ipinfo.class);
        expect(new Expectations() {{
            oneOf (context).getAttribute(TargetingContext.QUOVA_IP_INFO); will(returnValue(ipinfo));
            allowing (ipinfo).getLocation(); will(returnValue(null));
        }});
        assertNull(quovaGeoDeriverImpl.deriveCanadianProvinceFromIp(context));
    }

    @Test
    public void testDeriveCanadianProvinceFromIp03_no_state_data() {
        final Ipinfo ipinfo = mock(Ipinfo.class);
        final LocationType location = mock(LocationType.class, "location");
        expect(new Expectations() {{
            oneOf (context).getAttribute(TargetingContext.QUOVA_IP_INFO); will(returnValue(ipinfo));
            allowing (ipinfo).getLocation(); will(returnValue(location));
            allowing (location).getStateData(); will(returnValue(null));
        }});
        assertNull(quovaGeoDeriverImpl.deriveCanadianProvinceFromIp(context));
    }

    @Test
    public void testDeriveCanadianProvinceFromIp04_no_country_data() {
        final Ipinfo ipinfo = mock(Ipinfo.class);
        final LocationType location = mock(LocationType.class, "location");
        final StateDataType stateData = mock(StateDataType.class, "stateData");
        expect(new Expectations() {{
            oneOf (context).getAttribute(TargetingContext.QUOVA_IP_INFO); will(returnValue(ipinfo));
            allowing (ipinfo).getLocation(); will(returnValue(location));
            allowing (location).getStateData(); will(returnValue(stateData));
            allowing (location).getCountryData(); will(returnValue(null));
        }});
        assertNull(quovaGeoDeriverImpl.deriveCanadianProvinceFromIp(context));
    }

    @Test
    public void testDeriveCanadianProvinceFromIp05_non_ca_country() {
        final Ipinfo ipinfo = mock(Ipinfo.class);
        final LocationType location = mock(LocationType.class, "location");
        final StateDataType stateData = mock(StateDataType.class, "stateData");
        final CountryDataType countryData = mock(CountryDataType.class, "countryData");
        expect(new Expectations() {{
            oneOf (context).getAttribute(TargetingContext.QUOVA_IP_INFO); will(returnValue(ipinfo));
            allowing (ipinfo).getLocation(); will(returnValue(location));
            allowing (location).getStateData(); will(returnValue(stateData));
            allowing (location).getCountryData(); will(returnValue(countryData));
            allowing (countryData).getCountryCode(); will(returnValue("us"));
        }});
        assertNull(quovaGeoDeriverImpl.deriveCanadianProvinceFromIp(context));
    }

    @Test
    public void testDeriveCanadianProvinceFromIp06_no_state_code() {
        final Ipinfo ipinfo = mock(Ipinfo.class);
        final LocationType location = mock(LocationType.class, "location");
        final StateDataType stateData = mock(StateDataType.class, "stateData");
        final CountryDataType countryData = mock(CountryDataType.class, "countryData");
        expect(new Expectations() {{
            oneOf (context).getAttribute(TargetingContext.QUOVA_IP_INFO); will(returnValue(ipinfo));
            allowing (ipinfo).getLocation(); will(returnValue(location));
            allowing (location).getStateData(); will(returnValue(stateData));
            allowing (location).getCountryData(); will(returnValue(countryData));
            allowing (countryData).getCountryCode(); will(returnValue("ca"));
            allowing (stateData).getStateCode(); will(returnValue(null));
        }});
        assertNull(quovaGeoDeriverImpl.deriveCanadianProvinceFromIp(context));
    }

    @Test
    public void testDeriveCanadianProvinceFromIp07_unrecognized_province() {
        final Ipinfo ipinfo = mock(Ipinfo.class);
        final LocationType location = mock(LocationType.class, "location");
        final StateDataType stateData = mock(StateDataType.class, "stateData");
        final CountryDataType countryData = mock(CountryDataType.class, "countryData");
        expect(new Expectations() {{
            oneOf (context).getAttribute(TargetingContext.QUOVA_IP_INFO); will(returnValue(ipinfo));
            allowing (ipinfo).getLocation(); will(returnValue(location));
            allowing (location).getStateData(); will(returnValue(stateData));
            allowing (location).getCountryData(); will(returnValue(countryData));
            allowing (countryData).getCountryCode(); will(returnValue("ca"));
            allowing (stateData).getStateCode(); will(returnValue("not a province"));
        }});
        assertNull(quovaGeoDeriverImpl.deriveCanadianProvinceFromIp(context));
    }

    @Test
    public void testDeriveCanadianProvinceFromIp08_normal() {
        final Ipinfo ipinfo = mock(Ipinfo.class);
        final LocationType location = mock(LocationType.class, "location");
        final StateDataType stateData = mock(StateDataType.class, "stateData");
        final CountryDataType countryData = mock(CountryDataType.class, "countryData");
        final CanadianProvince canadianProvince = CanadianProvince.BC;
        expect(new Expectations() {{
            oneOf (context).getAttribute(TargetingContext.QUOVA_IP_INFO); will(returnValue(ipinfo));
            allowing (ipinfo).getLocation(); will(returnValue(location));
            allowing (location).getStateData(); will(returnValue(stateData));
            allowing (location).getCountryData(); will(returnValue(countryData));
            allowing (countryData).getCountryCode(); will(returnValue("ca"));
            allowing (stateData).getStateCode(); will(returnValue(canadianProvince.name().toLowerCase()));
        }});
        assertEquals(canadianProvince, quovaGeoDeriverImpl.deriveCanadianProvinceFromIp(context));
    }

    @Test
    public void testDeriveDmaFromIp01_no_ip_info() {
        expect(new Expectations() {{
            oneOf (context).getAttribute(TargetingContext.QUOVA_IP_INFO); will(returnValue(null));
        }});
        assertNull(quovaGeoDeriverImpl.deriveDmaFromIp(context));
    }

    @Test
    public void testDeriveDmaFromIp02_no_location() {
        final Ipinfo ipinfo = mock(Ipinfo.class);
        expect(new Expectations() {{
            oneOf (context).getAttribute(TargetingContext.QUOVA_IP_INFO); will(returnValue(ipinfo));
            allowing (ipinfo).getLocation(); will(returnValue(null));
        }});
        assertNull(quovaGeoDeriverImpl.deriveDmaFromIp(context));
    }

    @Test
    public void testDeriveDmaFromIp03_no_dma() {
        final Ipinfo ipinfo = mock(Ipinfo.class);
        final LocationType location = mock(LocationType.class, "location");
        expect(new Expectations() {{
            oneOf (context).getAttribute(TargetingContext.QUOVA_IP_INFO); will(returnValue(ipinfo));
            allowing (ipinfo).getLocation(); will(returnValue(location));
            allowing (location).getDma(); will(returnValue(null));
        }});
        assertNull(quovaGeoDeriverImpl.deriveDmaFromIp(context));
    }

    @Test
    public void testDeriveDmaFromIp04_no_dma_value() {
        final Ipinfo ipinfo = mock(Ipinfo.class);
        final LocationType location = mock(LocationType.class, "location");
        final JAXBElement<Integer> dmaElement = mock(JAXBElement.class, "dmaElement");
        expect(new Expectations() {{
            oneOf (context).getAttribute(TargetingContext.QUOVA_IP_INFO); will(returnValue(ipinfo));
            allowing (ipinfo).getLocation(); will(returnValue(location));
            allowing (location).getDma(); will(returnValue(dmaElement));
            allowing (location).getCountryData(); will(returnValue(null));
            allowing (dmaElement).getValue(); will(returnValue(null));
        }});
        assertNull(quovaGeoDeriverImpl.deriveDmaFromIp(context));
    }

    @Test
    public void testDeriveDmaFromIp05_country_not_us() {
        final Ipinfo ipinfo = mock(Ipinfo.class);
        final LocationType location = mock(LocationType.class, "location");
        final CountryDataType countryData = mock(CountryDataType.class, "countryData");
        final JAXBElement<Integer> dmaElement = mock(JAXBElement.class, "dmaElement");
        expect(new Expectations() {{
            oneOf (context).getAttribute(TargetingContext.QUOVA_IP_INFO); will(returnValue(ipinfo));
            allowing (ipinfo).getLocation(); will(returnValue(location));
            allowing (location).getDma(); will(returnValue(dmaElement));
            allowing (location).getCountryData(); will(returnValue(countryData));
            allowing (countryData).getCountryCode(); will(returnValue("de"));
            allowing (dmaElement).getValue(); will(returnValue(null));
        }});
        assertNull(quovaGeoDeriverImpl.deriveDmaFromIp(context));
    }

    @Test
    public void testDeriveDmaFromIp06_normal() {
        final Ipinfo ipinfo = mock(Ipinfo.class);
        final LocationType location = mock(LocationType.class, "location");
        final CountryDataType countryData = mock(CountryDataType.class, "countryData");
        final JAXBElement<Integer> dmaElement = mock(JAXBElement.class, "dmaElement");
        final Integer dmaId = randomInteger();
        final Dma dma = mock(Dma.class);
        expect(new Expectations() {{
            oneOf (context).getAttribute(TargetingContext.QUOVA_IP_INFO); will(returnValue(ipinfo));
            allowing (ipinfo).getLocation(); will(returnValue(location));
            allowing (location).getDma(); will(returnValue(dmaElement));
            allowing (location).getCountryData(); will(returnValue(countryData));
            allowing (countryData).getCountryCode(); will(returnValue("us"));
            allowing (dmaElement).getValue(); will(returnValue(dmaId));
            oneOf (dmaManager).getDmaById(dmaId.toString()); will(returnValue(dma));
        }});
        assertEquals(dma, quovaGeoDeriverImpl.deriveDmaFromIp(context));
    }
}
