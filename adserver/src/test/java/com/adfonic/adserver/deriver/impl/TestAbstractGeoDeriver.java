package com.adfonic.adserver.deriver.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.TimeZone;

import org.jmock.Expectations;
import org.junit.Before;
import org.junit.Test;

import com.adfonic.adserver.LocationSource;
import com.adfonic.adserver.Parameters;
import com.adfonic.adserver.TargetingContext;
import com.adfonic.adserver.deriver.DeriverManager;
import com.adfonic.adserver.impl.TargetingContextImpl;
import com.adfonic.domain.cache.DomainCache;
import com.adfonic.domain.cache.dto.adserver.CountryDto;
import com.adfonic.domain.cache.dto.adserver.MobileIpAddressRangeDto;
import com.adfonic.geo.AustrianPostalCode;
import com.adfonic.geo.AustrianPostalCodeManager;
import com.adfonic.geo.AustrianProvince;
import com.adfonic.geo.CanadianPostalCode;
import com.adfonic.geo.CanadianPostalCodeManager;
import com.adfonic.geo.CanadianProvince;
import com.adfonic.geo.ChinesePostalCode;
import com.adfonic.geo.ChinesePostalCodeManager;
import com.adfonic.geo.ChineseProvince;
import com.adfonic.geo.Coordinates;
import com.adfonic.geo.Dma;
import com.adfonic.geo.DmaManager;
import com.adfonic.geo.GBPostalCodeManager;
import com.adfonic.geo.PostalCode;
import com.adfonic.geo.SpanishPostalCodeManager;
import com.adfonic.geo.USState;
import com.adfonic.geo.USZipCode;
import com.adfonic.geo.USZipCodeManager;
import com.adfonic.geo.postalcode.GeneralPostalCode;
import com.adfonic.geo.postalcode.PostalCodeImmutable;
import com.adfonic.test.AbstractAdfonicTest;

public class TestAbstractGeoDeriver extends AbstractAdfonicTest {

    private DeriverManager deriverManager;

    private static final class Impl extends AbstractGeoDeriver {
        CountryDto country;
        TimeZone timeZone;
        Coordinates coordinates;
        String postalCode;
        USState usState;
        CanadianProvince canadianProvince;
        ChineseProvince chineseProvince;
        AustrianProvince austrianProvince;
        Dma dma;
        String spanishProvince = "Aragon";

        Impl(DeriverManager deriverManager) {
            super(deriverManager);
        }

        @Override
        protected String deriveSpanishProvinceFromIp(TargetingContext context) {
            return spanishProvince;
        }

        @Override
        protected ChineseProvince deriveChineseProvinceFromIp(TargetingContext context) {
            return chineseProvince;
        }

        @Override
        protected AustrianProvince deriveAustrianProvinceFromIp(TargetingContext context) {
            return austrianProvince;
        }

        protected CountryDto deriveCountryFromIp(TargetingContext context) {
            return country;
        }

        protected TimeZone deriveTimeZoneFromIp(TargetingContext context) {
            return timeZone;
        }

        protected Coordinates deriveCoordinatesFromIp(TargetingContext context) {
            return coordinates;
        }

        protected String derivePostalCodeFromIp(TargetingContext context) {
            return postalCode;
        }

        protected USState deriveUSStateFromIp(TargetingContext context) {
            return usState;
        }

        protected CanadianProvince deriveCanadianProvinceFromIp(TargetingContext context) {
            return canadianProvince;
        }

        protected Dma deriveDmaFromIp(TargetingContext context) {
            return dma;
        }
    }


    private class AustrianPostalCodeStub implements AustrianPostalCode {
        private String postalCode;
        private AustrianProvince austrianProvince;

        private AustrianPostalCodeStub(String postalCode) {
            this.austrianProvince = AustrianProvince.OO;
            this.postalCode = postalCode;
        }


        private AustrianPostalCodeStub(AustrianProvince austrianProvince) {
            this.austrianProvince = austrianProvince;
            this.postalCode = "1234";
        }

        @Override
        public String getPostalCode() {
            return postalCode;
        }

        @Override
        public AustrianProvince getAustrianProvince() {
            return austrianProvince;
        }

        @Override
        public double getLatitude() {
            return 0;
        }

        @Override
        public double getLongitude() {
            return 0;
        }
    }


    private class ChinesePostalCodeStub implements ChinesePostalCode {
        private String postalCode;
        private ChineseProvince chineseProvince;

        private ChinesePostalCodeStub(String postalCode) {
            this.postalCode = postalCode;
            chineseProvince = ChineseProvince.HA;
        }

        private ChinesePostalCodeStub(ChineseProvince chineseProvince) {
            this.chineseProvince = chineseProvince;
            this.postalCode = "1234";

        }

        @Override
        public String getPostalCode() {
            return postalCode;
        }

        @Override
        public ChineseProvince getChineseProvince() {
            return chineseProvince;
        }

        @Override
        public double getLatitude() {
            return 0;
        }

        @Override
        public double getLongitude() {
            return 0;
        }
    }

    private GBPostalCodeManager gbPostalCodeManager;
    private USZipCodeManager usZipCodeManager;
    private CanadianPostalCodeManager canadianPostalCodeManager;
    private ChinesePostalCodeManager chinesePostalCodeManager;
    private AustrianPostalCodeManager austrianPostalCodeManager;
    private SpanishPostalCodeManager spanishPostalCodeManager;
    private DmaManager dmaManager;
    private Impl impl;
    private TargetingContext context;
    private DomainCache domainCache;

    @Before
    public void initTests() throws InstantiationException, IllegalAccessException {
        gbPostalCodeManager = mock(GBPostalCodeManager.class);
        usZipCodeManager = mock(USZipCodeManager.class);
        canadianPostalCodeManager = mock(CanadianPostalCodeManager.class);
        chinesePostalCodeManager = mock(ChinesePostalCodeManager.class);
        austrianPostalCodeManager = mock(AustrianPostalCodeManager.class);
        spanishPostalCodeManager = mock(SpanishPostalCodeManager.class);
        dmaManager = mock(DmaManager.class);

        deriverManager = new DeriverManager();
        impl = new Impl(deriverManager);
        inject(impl, "gbPostalCodeManager", gbPostalCodeManager);
        inject(impl, "usZipCodeManager", usZipCodeManager);
        inject(impl, "canadianPostalCodeManager", canadianPostalCodeManager);
        inject(impl, "chinesePostalCodeManager", chinesePostalCodeManager);
        inject(impl, "austrianPostalCodeManager", austrianPostalCodeManager);
        inject(impl, "spanishPostalCodeManager", spanishPostalCodeManager);
        inject(impl, "dmaManager", dmaManager);

        context = mock(TargetingContext.class);
        domainCache = mock(DomainCache.class);
    }

    @Test
    public void testGetDmaManager() {
        assertEquals(dmaManager, impl.getDmaManager());
    }

    @Test
    public void testDeriveCountry01_from_parameter() {
        final CountryDto country = mock(CountryDto.class);
        expect(new Expectations() {{
            oneOf(context).getAttribute(AbstractGeoDeriver.COUNTRY_FROM_PARAMETER); will(returnValue(country));
        }});
        assertEquals(country, impl.getAttribute(TargetingContext.COUNTRY, context));
    }

    @Test
    public void testDeriveCountry02_from_ip() {
        final CountryDto country = mock(CountryDto.class);
        impl.country = country;
        expect(new Expectations() {{
            oneOf(context).getAttribute(AbstractGeoDeriver.COUNTRY_FROM_PARAMETER); will(returnValue(null));
        }});
        assertEquals(country, impl.getAttribute(TargetingContext.COUNTRY, context));
    }

    @Test
    public void testDeriveIpBasedDataReliable01_all_null() {
        expect(new Expectations() {{
            oneOf(context).getAttribute(AbstractGeoDeriver.COORDINATES_FROM_PARAMETERS); will(returnValue(null));
            oneOf(context).getAttribute(Parameters.POSTAL_CODE); will(returnValue(null));
            oneOf(context).getAttribute(AbstractGeoDeriver.US_STATE_FROM_PARAMETER); will(returnValue(null));
            oneOf(context).getAttribute(AbstractGeoDeriver.COUNTRY_FROM_PARAMETER); will(returnValue(null));
            oneOf(context).getAttribute(AbstractGeoDeriver.DMA_FROM_PARAMETER); will(returnValue(null));
        }});
        assertEquals(Boolean.TRUE, impl.getAttribute(AbstractGeoDeriver.IP_BASED_DATA_RELIABLE, context));
    }

    @Test
    public void testDeriveIpBasedDataReliable02_dma_same() {
        final Dma dma1 = mock(Dma.class, "dma1");
        expect(new Expectations() {{
            oneOf(context).getAttribute(AbstractGeoDeriver.COORDINATES_FROM_PARAMETERS); will(returnValue(null));
            oneOf(context).getAttribute(Parameters.POSTAL_CODE); will(returnValue(null));
            oneOf(context).getAttribute(AbstractGeoDeriver.US_STATE_FROM_PARAMETER); will(returnValue(null));
            oneOf(context).getAttribute(AbstractGeoDeriver.COUNTRY_FROM_PARAMETER); will(returnValue(null));
            oneOf(context).getAttribute(AbstractGeoDeriver.DMA_FROM_PARAMETER); will(returnValue(dma1));
            oneOf(context).getAttribute(AbstractGeoDeriver.DMA_FROM_IP); will(returnValue(dma1));
        }});
        assertEquals(Boolean.TRUE, impl.getAttribute(AbstractGeoDeriver.IP_BASED_DATA_RELIABLE, context));
    }

    @Test
    public void testDeriveIpBasedDataReliable03_dma_mismatch() {
        final Dma dma1 = mock(Dma.class, "dma1");
        final Dma dma2 = mock(Dma.class, "dma2");
        expect(new Expectations() {{
            oneOf(context).getAttribute(AbstractGeoDeriver.COORDINATES_FROM_PARAMETERS); will(returnValue(null));
            oneOf(context).getAttribute(Parameters.POSTAL_CODE); will(returnValue(null));
            oneOf(context).getAttribute(AbstractGeoDeriver.US_STATE_FROM_PARAMETER); will(returnValue(null));
            oneOf(context).getAttribute(AbstractGeoDeriver.COUNTRY_FROM_PARAMETER); will(returnValue(null));
            oneOf(context).getAttribute(AbstractGeoDeriver.DMA_FROM_PARAMETER); will(returnValue(dma1));
            oneOf(context).getAttribute(AbstractGeoDeriver.DMA_FROM_IP); will(returnValue(dma2));
        }});
        assertEquals(Boolean.FALSE, impl.getAttribute(AbstractGeoDeriver.IP_BASED_DATA_RELIABLE, context));
    }

    @Test
    public void testDeriveIpBasedDataReliable04_country_same() {
        final CountryDto country1 = mock(CountryDto.class, "country1");
        expect(new Expectations() {{
            oneOf(context).getAttribute(AbstractGeoDeriver.COORDINATES_FROM_PARAMETERS); will(returnValue(null));
            oneOf(context).getAttribute(Parameters.POSTAL_CODE); will(returnValue(null));
            oneOf(context).getAttribute(AbstractGeoDeriver.US_STATE_FROM_PARAMETER); will(returnValue(null));
            oneOf(context).getAttribute(AbstractGeoDeriver.COUNTRY_FROM_PARAMETER); will(returnValue(country1));
            oneOf(context).getAttribute(AbstractGeoDeriver.COUNTRY_FROM_IP); will(returnValue(country1));
            oneOf(context).getAttribute(AbstractGeoDeriver.DMA_FROM_PARAMETER); will(returnValue(null));
        }});
        assertEquals(Boolean.TRUE, impl.getAttribute(AbstractGeoDeriver.IP_BASED_DATA_RELIABLE, context));
    }

    @Test
    public void testDeriveIpBasedDataReliable05_country_mismatch() {
        final CountryDto country1 = mock(CountryDto.class, "country1");
        final CountryDto country2 = mock(CountryDto.class, "country2");
        expect(new Expectations() {{
            oneOf(context).getAttribute(AbstractGeoDeriver.COORDINATES_FROM_PARAMETERS); will(returnValue(null));
            oneOf(context).getAttribute(Parameters.POSTAL_CODE); will(returnValue(null));
            oneOf(context).getAttribute(AbstractGeoDeriver.US_STATE_FROM_PARAMETER); will(returnValue(null));
            oneOf(context).getAttribute(AbstractGeoDeriver.COUNTRY_FROM_PARAMETER); will(returnValue(country1));
            oneOf(context).getAttribute(AbstractGeoDeriver.COUNTRY_FROM_IP); will(returnValue(country2));
        }});
        assertEquals(Boolean.FALSE, impl.getAttribute(AbstractGeoDeriver.IP_BASED_DATA_RELIABLE, context));
    }

    @Test
    public void testDeriveIpBasedDataReliable06_state_same() {
        final USState usState1 = USState.KY;
        expect(new Expectations() {{
            oneOf(context).getAttribute(AbstractGeoDeriver.COORDINATES_FROM_PARAMETERS); will(returnValue(null));
            oneOf(context).getAttribute(Parameters.POSTAL_CODE); will(returnValue(null));
            oneOf(context).getAttribute(AbstractGeoDeriver.US_STATE_FROM_PARAMETER); will(returnValue(usState1));
            oneOf(context).getAttribute(AbstractGeoDeriver.US_STATE_FROM_IP); will(returnValue(usState1));
            oneOf(context).getAttribute(AbstractGeoDeriver.COUNTRY_FROM_PARAMETER); will(returnValue(null));
            oneOf(context).getAttribute(AbstractGeoDeriver.DMA_FROM_PARAMETER); will(returnValue(null));
        }});
        assertEquals(Boolean.TRUE, impl.getAttribute(AbstractGeoDeriver.IP_BASED_DATA_RELIABLE, context));
    }

    @Test
    public void testDeriveIpBasedDataReliable07_state_mismatch() {
        final USState usState1 = USState.KY;
        final USState usState2 = USState.TN;
        expect(new Expectations() {{
            oneOf(context).getAttribute(AbstractGeoDeriver.COORDINATES_FROM_PARAMETERS); will(returnValue(null));
            oneOf(context).getAttribute(Parameters.POSTAL_CODE); will(returnValue(null));
            oneOf(context).getAttribute(AbstractGeoDeriver.US_STATE_FROM_PARAMETER); will(returnValue(usState1));
            oneOf(context).getAttribute(AbstractGeoDeriver.US_STATE_FROM_IP); will(returnValue(usState2));
        }});
        assertEquals(Boolean.FALSE, impl.getAttribute(AbstractGeoDeriver.IP_BASED_DATA_RELIABLE, context));
    }

    @Test
    public void testDeriveIpBasedDataReliable08_postal_code_same() {
        final String postalCode1 = uniqueAlphaNumericString(10, "postalCode");
        expect(new Expectations() {{
            oneOf(context).getAttribute(AbstractGeoDeriver.COORDINATES_FROM_PARAMETERS); will(returnValue(null));
            oneOf(context).getAttribute(Parameters.POSTAL_CODE); will(returnValue(postalCode1));
            oneOf(context).getAttribute(AbstractGeoDeriver.POSTAL_CODE_FROM_IP); will(returnValue(postalCode1));
            oneOf(context).getAttribute(AbstractGeoDeriver.US_STATE_FROM_PARAMETER); will(returnValue(null));
            oneOf(context).getAttribute(AbstractGeoDeriver.COUNTRY_FROM_PARAMETER); will(returnValue(null));
            oneOf(context).getAttribute(AbstractGeoDeriver.DMA_FROM_PARAMETER); will(returnValue(null));
        }});
        assertEquals(Boolean.TRUE, impl.getAttribute(AbstractGeoDeriver.IP_BASED_DATA_RELIABLE, context));
    }

    @Test
    public void testDeriveIpBasedDataReliable09_postal_code_mismatch() {
        final String postalCode1 = uniqueAlphaNumericString(10, "postalCode");
        final String postalCode2 = uniqueAlphaNumericString(10, "postalCode");
        expect(new Expectations() {{
            oneOf(context).getAttribute(AbstractGeoDeriver.COORDINATES_FROM_PARAMETERS); will(returnValue(null));
            oneOf(context).getAttribute(Parameters.POSTAL_CODE); will(returnValue(postalCode1));
            oneOf(context).getAttribute(AbstractGeoDeriver.POSTAL_CODE_FROM_IP); will(returnValue(postalCode2));
        }});
        assertEquals(Boolean.FALSE, impl.getAttribute(AbstractGeoDeriver.IP_BASED_DATA_RELIABLE, context));
    }

    @Test
    public void testDeriveIpBasedDataReliable10_coordinates() {
        final Coordinates coordinates1 = mock(Coordinates.class);
        expect(new Expectations() {{
            oneOf(context).getAttribute(AbstractGeoDeriver.COORDINATES_FROM_PARAMETERS);
            will(returnValue(coordinates1));
        }});
        assertEquals(Boolean.FALSE, impl.getAttribute(AbstractGeoDeriver.IP_BASED_DATA_RELIABLE, context));
    }

    @Test
    public void testDeriveGeolocatable01_coordinates_specified() {
        final Coordinates coordinates = mock(Coordinates.class);
        expect(new Expectations() {{
            oneOf(context).getAttribute(TargetingContext.COORDINATES); will(returnValue(coordinates));
        }});
        assertEquals(Boolean.TRUE, impl.getAttribute(TargetingContext.HAS_COORDINATES, context));
    }

    @Test
    public void testDeriveGeolocatable02_coordinates_not_specified() {
        expect(new Expectations() {{
            oneOf(context).getAttribute(TargetingContext.COORDINATES); will(returnValue(null));
        }});
        assertEquals(Boolean.FALSE, impl.getAttribute(TargetingContext.HAS_COORDINATES, context));
    }

    @Test
    public void testDeriveCoordinates01_from_parameters() {
        final Coordinates coordinates = mock(Coordinates.class);
        expect(new Expectations() {{
            oneOf(context).getAttribute(AbstractGeoDeriver.COORDINATES_FROM_PARAMETERS); will(returnValue(coordinates));
            oneOf(context).setAttribute(TargetingContext.LOCATION_SOURCE, LocationSource.EXPLICIT);
        }});
        assertEquals(coordinates, impl.getAttribute(TargetingContext.COORDINATES, context));
    }

    @Test
    public void testDeriveCoordinates02_mobile_operator() {
        final MobileIpAddressRangeDto mobileIpAddressRange = mock(MobileIpAddressRangeDto.class);
        final long operatorId = randomLong();
        expect(new Expectations() {{
            oneOf(context).getAttribute(AbstractGeoDeriver.COORDINATES_FROM_PARAMETERS); will(returnValue(null));
            oneOf(context).getAttribute(TargetingContext.MOBILE_IP_ADDRESS_RANGE);
            will(returnValue(mobileIpAddressRange));
            oneOf(mobileIpAddressRange).getOperatorId(); will(returnValue(operatorId));
        }});
        assertNull(impl.getAttribute(TargetingContext.COORDINATES, context));
    }

    @Test
    public void testDeriveCoordinates03_mobile_no_operator() {
        final MobileIpAddressRangeDto mobileIpAddressRange = mock(MobileIpAddressRangeDto.class);
        final Coordinates coordinates = mock(Coordinates.class);
        impl.coordinates = coordinates;
        expect(new Expectations() {{
            oneOf(context).getAttribute(AbstractGeoDeriver.COORDINATES_FROM_PARAMETERS); will(returnValue(null));
            oneOf(context).getAttribute(TargetingContext.MOBILE_IP_ADDRESS_RANGE);
            will(returnValue(mobileIpAddressRange));
            oneOf(mobileIpAddressRange).getOperatorId(); will(returnValue(null));
            oneOf(context).setAttribute(TargetingContext.LOCATION_SOURCE, LocationSource.DERIVED);
        }});
        assertEquals(coordinates, impl.getAttribute(TargetingContext.COORDINATES, context));
    }

    @Test
    public void testDeriveCoordinates04_non_mobile() {
        final Coordinates coordinates = mock(Coordinates.class);
        impl.coordinates = coordinates;
        expect(new Expectations() {{
            oneOf(context).getAttribute(AbstractGeoDeriver.COORDINATES_FROM_PARAMETERS); will(returnValue(null));
            oneOf(context).getAttribute(TargetingContext.MOBILE_IP_ADDRESS_RANGE); will(returnValue(null));
            oneOf(context).setAttribute(TargetingContext.LOCATION_SOURCE, LocationSource.DERIVED);
        }});
        assertEquals(coordinates, impl.getAttribute(TargetingContext.COORDINATES, context));
    }

    @Test
    public void testDerivePostalCode01_parameter() {
        final String postalCode = randomAlphaNumericString(10);
        expect(new Expectations() {{
            oneOf(context).getAttribute(Parameters.POSTAL_CODE); will(returnValue(postalCode));
        }});
        assertEquals(postalCode, impl.getAttribute(TargetingContext.POSTAL_CODE, context));
    }

    @Test
    public void testDerivePostalCode02_ip_based_data_reliable() {
        final String postalCode = randomAlphaNumericString(10);
        expect(new Expectations() {{
            oneOf(context).getAttribute(Parameters.POSTAL_CODE); will(returnValue(null));
            oneOf(context).getAttribute(AbstractGeoDeriver.IP_BASED_DATA_RELIABLE, Boolean.class);
            will(returnValue(Boolean.TRUE));
            oneOf(context).getAttribute(AbstractGeoDeriver.POSTAL_CODE_FROM_IP); will(returnValue(postalCode));
        }});
        assertEquals(postalCode, impl.getAttribute(TargetingContext.POSTAL_CODE, context));
    }

    @Test
    public void testDerivePostalCode03_no_country() {
        expect(new Expectations() {{
            oneOf(context).getAttribute(Parameters.POSTAL_CODE); will(returnValue(null));
            oneOf(context).getAttribute(AbstractGeoDeriver.IP_BASED_DATA_RELIABLE, Boolean.class); will(returnValue(Boolean.FALSE));
            will(returnValue(Boolean.FALSE));
            oneOf(context).getAttribute(TargetingContext.COUNTRY); will(returnValue(null));
        }});
        assertNull(impl.getAttribute(TargetingContext.POSTAL_CODE, context));
    }

    @Test
    public void testDerivePostalCode04_US_no_zip() {
        final Coordinates coordinates = mock(Coordinates.class);
        final CountryDto country = mock(CountryDto.class);
        expect(new Expectations() {{
            oneOf(context).getAttribute(Parameters.POSTAL_CODE); will(returnValue(null));
            oneOf(context).getAttribute(AbstractGeoDeriver.IP_BASED_DATA_RELIABLE, Boolean.class);
            will(returnValue(Boolean.FALSE));
            oneOf(context).getAttribute(TargetingContext.COUNTRY); will(returnValue(country));
            oneOf(context).getAttribute(TargetingContext.COORDINATES); will(returnValue(coordinates));
            allowing(country).getIsoCode(); will(returnValue("US"));
            oneOf(usZipCodeManager).getNearest(coordinates); will(returnValue(null));
        }});
        assertNull(impl.getAttribute(TargetingContext.POSTAL_CODE, context));
    }

    @Test
    public void testDerivePostalCode05_US_zip() {
        final Coordinates coordinates = mock(Coordinates.class);
        final CountryDto country = mock(CountryDto.class);
        final USZipCode usZipCode = mock(USZipCode.class);
        final String postalCode = randomAlphaNumericString(10);
        expect(new Expectations() {{
            oneOf(context).getAttribute(Parameters.POSTAL_CODE); will(returnValue(null));
            oneOf(context).getAttribute(AbstractGeoDeriver.IP_BASED_DATA_RELIABLE, Boolean.class);
            will(returnValue(Boolean.FALSE));
            oneOf(context).getAttribute(TargetingContext.COUNTRY); will(returnValue(country));
            oneOf(context).getAttribute(TargetingContext.COORDINATES); will(returnValue(coordinates));
            allowing(country).getIsoCode(); will(returnValue("US"));
            oneOf(usZipCodeManager).getNearest(coordinates); will(returnValue(usZipCode));
            oneOf(context).setAttribute(TargetingContext.US_ZIP_CODE, usZipCode);
            oneOf(usZipCode).getZip(); will(returnValue(postalCode));
        }});
        assertEquals(postalCode, impl.getAttribute(TargetingContext.POSTAL_CODE, context));
    }

    @Test
    public void testDerivePostalCode06_GB_no_postal_code() {
        final Coordinates coordinates = mock(Coordinates.class);
        final CountryDto country = mock(CountryDto.class);
        expect(new Expectations() {{
            oneOf(context).getAttribute(Parameters.POSTAL_CODE); will(returnValue(null));
            oneOf(context).getAttribute(AbstractGeoDeriver.IP_BASED_DATA_RELIABLE, Boolean.class);
            will(returnValue(Boolean.FALSE));
            oneOf(context).getAttribute(TargetingContext.COUNTRY); will(returnValue(country));
            oneOf(context).getAttribute(TargetingContext.COORDINATES); will(returnValue(coordinates));
            allowing(country).getIsoCode(); will(returnValue("GB"));
            oneOf(gbPostalCodeManager).getNearest(coordinates); will(returnValue(null));
        }});
        assertNull(impl.getAttribute(TargetingContext.POSTAL_CODE, context));
    }

    @Test
    public void testDerivePostalCode07_GB_distance_too_great() {
        final Coordinates coordinates = mock(Coordinates.class);
        final CountryDto country = mock(CountryDto.class);
        final PostalCode ukPostalCode = mock(PostalCode.class);
        expect(new Expectations() {{
            oneOf(context).getAttribute(Parameters.POSTAL_CODE); will(returnValue(null));
            oneOf(context).getAttribute(AbstractGeoDeriver.IP_BASED_DATA_RELIABLE, Boolean.class);
            will(returnValue(Boolean.FALSE));
            oneOf(context).getAttribute(TargetingContext.COUNTRY); will(returnValue(country));
            oneOf(context).getAttribute(TargetingContext.COORDINATES); will(returnValue(coordinates));
            allowing(country).getIsoCode(); will(returnValue("GB"));
            oneOf(gbPostalCodeManager).getNearest(coordinates); will(returnValue(ukPostalCode));
            oneOf(coordinates).getLatitude(); will(returnValue(0.0));
            oneOf(coordinates).getLongitude(); will(returnValue(0.0));
            oneOf(ukPostalCode).getLatitude(); will(returnValue(30.0));
            oneOf(ukPostalCode).getLongitude(); will(returnValue(30.0));
        }});
        assertNull(impl.getAttribute(TargetingContext.POSTAL_CODE, context));
    }

    @Test
    public void testDerivePostalCode08_GB_postal_code() {
        final Coordinates coordinates = mock(Coordinates.class);
        final CountryDto country = mock(CountryDto.class);
        final PostalCode ukPostalCode = mock(PostalCode.class);
        final String postalCode = randomAlphaNumericString(10).toUpperCase();
        expect(new Expectations() {{
            oneOf(context).getAttribute(Parameters.POSTAL_CODE); will(returnValue(null));
            oneOf(context).getAttribute(AbstractGeoDeriver.IP_BASED_DATA_RELIABLE, Boolean.class);
            will(returnValue(Boolean.FALSE));
            oneOf(context).getAttribute(TargetingContext.COUNTRY); will(returnValue(country));
            oneOf(context).getAttribute(TargetingContext.COORDINATES); will(returnValue(coordinates));
            allowing(country).getIsoCode(); will(returnValue("GB"));
            oneOf(gbPostalCodeManager).getNearest(coordinates); will(returnValue(ukPostalCode));
            oneOf(coordinates).getLatitude(); will(returnValue(0.0));
            oneOf(coordinates).getLongitude(); will(returnValue(0.0));
            oneOf(ukPostalCode).getLatitude(); will(returnValue(0.0));
            oneOf(ukPostalCode).getLongitude(); will(returnValue(0.0));
            oneOf(context).setAttribute(TargetingContext.UK_POSTAL_CODE, ukPostalCode);
            oneOf(ukPostalCode).getPostalCode(); will(returnValue(postalCode));
        }});
        assertEquals(postalCode.toLowerCase(), impl.getAttribute(TargetingContext.POSTAL_CODE, context));
    }

    @Test
    public void testDerivePostalCode09_CA_no_postal_code() {
        final Coordinates coordinates = mock(Coordinates.class);
        final CountryDto country = mock(CountryDto.class);
        expect(new Expectations() {{
            oneOf(context).getAttribute(Parameters.POSTAL_CODE); will(returnValue(null));
            oneOf(context).getAttribute(AbstractGeoDeriver.IP_BASED_DATA_RELIABLE, Boolean.class);
            will(returnValue(Boolean.FALSE));
            oneOf(context).getAttribute(TargetingContext.COUNTRY); will(returnValue(country));
            oneOf(context).getAttribute(TargetingContext.COORDINATES); will(returnValue(coordinates));
            allowing(country).getIsoCode(); will(returnValue("CA"));
            oneOf(canadianPostalCodeManager).getNearest(coordinates); will(returnValue(null));
        }});
        assertNull(impl.getAttribute(TargetingContext.POSTAL_CODE, context));
    }

    @Test
    public void testDerivePostalCode10_CA_postal_code() {
        final Coordinates coordinates = mock(Coordinates.class);
        final CountryDto country = mock(CountryDto.class);
        final CanadianPostalCode canadianPostalCode = mock(CanadianPostalCode.class);
        final String postalCode = randomAlphaNumericString(10).toUpperCase();
        expect(new Expectations() {{
            oneOf(context).getAttribute(Parameters.POSTAL_CODE); will(returnValue(null));
            oneOf(context).getAttribute(AbstractGeoDeriver.IP_BASED_DATA_RELIABLE, Boolean.class);
            will(returnValue(Boolean.FALSE));
            oneOf(context).getAttribute(TargetingContext.COUNTRY); will(returnValue(country));
            oneOf(context).getAttribute(TargetingContext.COORDINATES); will(returnValue(coordinates));
            allowing(country).getIsoCode(); will(returnValue("CA"));
            oneOf(canadianPostalCodeManager).getNearest(coordinates); will(returnValue(canadianPostalCode));
            oneOf(context).setAttribute(TargetingContext.CANADIAN_POSTAL_CODE, canadianPostalCode);
            oneOf(canadianPostalCode).getPostalCode(); will(returnValue(postalCode));
        }});
        assertEquals(postalCode.toLowerCase(), impl.getAttribute(TargetingContext.POSTAL_CODE, context));
    }

    @Test
    public void derivePostalCodeForChinaEmpty() {
        final Coordinates coordinates = mock(Coordinates.class);
        final CountryDto country = mock(CountryDto.class);
        expect(new Expectations() {{
            oneOf(context).getAttribute(Parameters.POSTAL_CODE); will(returnValue(null));
            oneOf(context).getAttribute(AbstractGeoDeriver.IP_BASED_DATA_RELIABLE, Boolean.class);
            will(returnValue(Boolean.FALSE));
            oneOf(context).getAttribute(TargetingContext.COUNTRY); will(returnValue(country));
            oneOf(context).getAttribute(TargetingContext.COORDINATES); will(returnValue(coordinates));
            allowing(country).getIsoCode(); will(returnValue("CN"));
            oneOf(chinesePostalCodeManager).getNearest(coordinates); will(returnValue(null));
        }});
        assertNull(impl.getAttribute(TargetingContext.POSTAL_CODE, context));
    }

    @Test
    public void derivePostalCodeForChina() {
        final Coordinates coordinates = mock(Coordinates.class);
        final CountryDto country = mock(CountryDto.class);
        final String postalCode = randomAlphaNumericString(10).toUpperCase();
        final ChinesePostalCode chinesePostalCode = new ChinesePostalCodeStub(postalCode);
        expect(new Expectations() {{
            oneOf(context).getAttribute(Parameters.POSTAL_CODE); will(returnValue(null));
            oneOf(context).getAttribute(AbstractGeoDeriver.IP_BASED_DATA_RELIABLE, Boolean.class);
            will(returnValue(Boolean.FALSE));
            oneOf(context).getAttribute(TargetingContext.COUNTRY); will(returnValue(country));
            oneOf(context).getAttribute(TargetingContext.COORDINATES); will(returnValue(coordinates));
            allowing(country).getIsoCode(); will(returnValue("CN"));
            oneOf(chinesePostalCodeManager).getNearest(coordinates); will(returnValue(chinesePostalCode));
            oneOf(context).setAttribute(TargetingContext.CHINESE_POSTAL_CODE, chinesePostalCode);
        }});
        assertEquals(postalCode.toLowerCase(), impl.getAttribute(TargetingContext.POSTAL_CODE, context));
    }

    @Test
    public void derivePostalCodeForSpainEmpty() {
        final Coordinates coordinates = mock(Coordinates.class);
        final CountryDto country = mock(CountryDto.class);
        expect(new Expectations() {{
            oneOf(context).getAttribute(Parameters.POSTAL_CODE); will(returnValue(null));
            oneOf(context).getAttribute(AbstractGeoDeriver.IP_BASED_DATA_RELIABLE, Boolean.class);
            will(returnValue(Boolean.FALSE));
            oneOf(context).getAttribute(TargetingContext.COUNTRY); will(returnValue(country));
            oneOf(context).getAttribute(TargetingContext.COORDINATES); will(returnValue(coordinates));
            allowing(country).getIsoCode(); will(returnValue("ES"));
            oneOf(spanishPostalCodeManager).getNearest(coordinates); will(returnValue(null));
        }});
        assertNull(impl.getAttribute(TargetingContext.POSTAL_CODE, context));
    }

    @Test
    public void derivePostalCodeForSpain() {
        final Coordinates coordinates = mock(Coordinates.class);
        final CountryDto country = mock(CountryDto.class);
        final String postalCode = randomAlphaNumericString(10).toUpperCase();
        final GeneralPostalCode gPostalCode = new PostalCodeImmutable("ES", postalCode, "PR", 10, 20);
        expect(new Expectations() {{
            oneOf(context).getAttribute(Parameters.POSTAL_CODE); will(returnValue(null));
            oneOf(context).getAttribute(AbstractGeoDeriver.IP_BASED_DATA_RELIABLE, Boolean.class);
            will(returnValue(Boolean.FALSE));
            oneOf(context).getAttribute(TargetingContext.COUNTRY); will(returnValue(country));
            oneOf(context).getAttribute(TargetingContext.COORDINATES); will(returnValue(coordinates));
            allowing(country).getIsoCode(); will(returnValue("ES"));
            oneOf(spanishPostalCodeManager).getNearest(coordinates); will(returnValue(gPostalCode));
            oneOf(context).setAttribute(TargetingContext.SPANISH_POSTAL_CODE, gPostalCode);
        }});
        assertEquals(postalCode, impl.getAttribute(TargetingContext.POSTAL_CODE, context));
    }

    @Test
    public void derivePostalCodeForAustriaEmpty() {
        final Coordinates coordinates = mock(Coordinates.class);
        final CountryDto country = mock(CountryDto.class);
        expect(new Expectations() {{
            oneOf(context).getAttribute(Parameters.POSTAL_CODE); will(returnValue(null));
            oneOf(context).getAttribute(AbstractGeoDeriver.IP_BASED_DATA_RELIABLE, Boolean.class);
            will(returnValue(Boolean.FALSE));
            oneOf(context).getAttribute(TargetingContext.COUNTRY); will(returnValue(country));
            oneOf(context).getAttribute(TargetingContext.COORDINATES); will(returnValue(coordinates));
            allowing(country).getIsoCode(); will(returnValue("AT"));
            oneOf(austrianPostalCodeManager).getNearest(coordinates); will(returnValue(null));
        }});
        assertNull(impl.getAttribute(TargetingContext.POSTAL_CODE, context));
    }

    @Test
    public void derivePostalCodeForAustria() {
        final Coordinates coordinates = mock(Coordinates.class);
        final CountryDto country = mock(CountryDto.class);
        final String postalCode = randomAlphaNumericString(10).toUpperCase();
        final AustrianPostalCode austrianPostalCode = new AustrianPostalCodeStub(postalCode);
        expect(new Expectations() {{
            oneOf(context).getAttribute(Parameters.POSTAL_CODE); will(returnValue(null));
            oneOf(context).getAttribute(AbstractGeoDeriver.IP_BASED_DATA_RELIABLE, Boolean.class);
            will(returnValue(Boolean.FALSE));
            oneOf(context).getAttribute(TargetingContext.COUNTRY); will(returnValue(country));
            oneOf(context).getAttribute(TargetingContext.COORDINATES); will(returnValue(coordinates));
            allowing(country).getIsoCode(); will(returnValue("AT"));
            oneOf(austrianPostalCodeManager).getNearest(coordinates); will(returnValue(austrianPostalCode));
            oneOf(context).setAttribute(TargetingContext.AUSTRIAN_POSTAL_CODE, austrianPostalCode);
        }});
        assertEquals(postalCode.toLowerCase(), impl.getAttribute(TargetingContext.POSTAL_CODE, context));
    }




    @Test
    public void testDerivePostalCode11_some_other_country() {
        final Coordinates coordinates = mock(Coordinates.class);
        final CountryDto country = mock(CountryDto.class);
        expect(new Expectations() {{
            oneOf(context).getAttribute(Parameters.POSTAL_CODE); will(returnValue(null));
            oneOf(context).getAttribute(AbstractGeoDeriver.IP_BASED_DATA_RELIABLE, Boolean.class);
            will(returnValue(Boolean.FALSE));
            oneOf(context).getAttribute(TargetingContext.COUNTRY); will(returnValue(country));
            oneOf(context).getAttribute(TargetingContext.COORDINATES); will(returnValue(coordinates));
            allowing(country).getIsoCode(); will(returnValue("XX"));
        }});
        assertNull(impl.getAttribute(TargetingContext.POSTAL_CODE, context));
    }

    @Test
    public void testDeriveUKPostalCode01_no_postal_code() {
        final CountryDto country = mock(CountryDto.class, "country");
        final String countryIsoCode = "GB";
        expect(new Expectations() {{
            oneOf(context).getAttribute(TargetingContext.COUNTRY); will(returnValue(country));
            allowing(country).getIsoCode(); will(returnValue(countryIsoCode));
            oneOf(context).getAttribute(TargetingContext.POSTAL_CODE); will(returnValue(null));
        }});
        assertNull(impl.getAttribute(TargetingContext.UK_POSTAL_CODE, context));
    }

    @Test
    public void testDeriveUKPostalCode02_not_UK() {
        final CountryDto country = mock(CountryDto.class, "country");
        final String countryIsoCode = "US";
        expect(new Expectations() {{
            oneOf(context).getAttribute(TargetingContext.COUNTRY); will(returnValue(country));
            allowing(country).getIsoCode(); will(returnValue(countryIsoCode));
        }});
        assertNull(impl.getAttribute(TargetingContext.UK_POSTAL_CODE, context));
    }

    @Test
    public void testDeriveUKPostalCode03_postal_code() {
        final CountryDto country = mock(CountryDto.class, "country");
        final String countryIsoCode = "GB";
        final String postalCode = "abc";
        final String postalCodeUpper = "ABC";
        expect(new Expectations() {{
            oneOf(context).getAttribute(TargetingContext.COUNTRY); will(returnValue(country));
            allowing(country).getIsoCode(); will(returnValue(countryIsoCode));
            oneOf(context).getAttribute(TargetingContext.POSTAL_CODE); will(returnValue(postalCode));
            oneOf(gbPostalCodeManager).get(postalCodeUpper); will(returnValue(null));
        }});
        assertNull(impl.getAttribute(TargetingContext.UK_POSTAL_CODE, context));
    }

    @Test
    public void testDeriveUSZipCode01_no_postal_code() {
        final CountryDto country = mock(CountryDto.class, "country");
        final String countryIsoCode = "US";
        expect(new Expectations() {{
            oneOf(context).getAttribute(TargetingContext.COUNTRY); will(returnValue(country));
            allowing(country).getIsoCode(); will(returnValue(countryIsoCode));
            oneOf(context).getAttribute(TargetingContext.POSTAL_CODE); will(returnValue(null));
        }});
        assertNull(impl.getAttribute(TargetingContext.US_ZIP_CODE, context));
    }

    @Test
    public void testDeriveUSZipCode02_not_US() {
        final CountryDto country = mock(CountryDto.class, "country");
        final String countryIsoCode = "DE";
        expect(new Expectations() {{
            oneOf(context).getAttribute(TargetingContext.COUNTRY); will(returnValue(country));
            allowing(country).getIsoCode(); will(returnValue(countryIsoCode));
        }});
        assertNull(impl.getAttribute(TargetingContext.US_ZIP_CODE, context));
    }

    @Test
    public void testDeriveUSZipCode03_postal_code() {
        final CountryDto country = mock(CountryDto.class, "country");
        final String countryIsoCode = "US";
        final String postalCode = randomAlphaNumericString(10);
        expect(new Expectations() {{
            oneOf(context).getAttribute(TargetingContext.COUNTRY); will(returnValue(country));
            allowing(country).getIsoCode(); will(returnValue(countryIsoCode));
            oneOf(context).getAttribute(TargetingContext.POSTAL_CODE); will(returnValue(postalCode));
            oneOf(usZipCodeManager).get(postalCode); will(returnValue(null));
        }});
        assertNull(impl.getAttribute(TargetingContext.US_ZIP_CODE, context));
    }

    @Test
    public void testDeriveCanadianPostalCode01_no_postal_code() {
        final CountryDto country = mock(CountryDto.class, "country");
        final String countryIsoCode = "CA";
        expect(new Expectations() {{
            oneOf(context).getAttribute(TargetingContext.COUNTRY); will(returnValue(country));
            allowing(country).getIsoCode(); will(returnValue(countryIsoCode));
            oneOf(context).getAttribute(TargetingContext.POSTAL_CODE); will(returnValue(null));
        }});
        assertNull(impl.getAttribute(TargetingContext.CANADIAN_POSTAL_CODE, context));
    }

    @Test
    public void testDeriveCanadianPostalCode02_not_CA() {
        final CountryDto country = mock(CountryDto.class, "country");
        final String countryIsoCode = "DE";
        expect(new Expectations() {{
            oneOf(context).getAttribute(TargetingContext.COUNTRY); will(returnValue(country));
            allowing(country).getIsoCode(); will(returnValue(countryIsoCode));
        }});
        assertNull(impl.getAttribute(TargetingContext.CANADIAN_POSTAL_CODE, context));
    }

    @Test
    public void testDeriveCanadianPostalCode03_postal_code() {
        final CountryDto country = mock(CountryDto.class, "country");
        final String countryIsoCode = "CA";
        final String postalCode = randomAlphaNumericString(10);
        expect(new Expectations() {{
            oneOf(context).getAttribute(TargetingContext.COUNTRY); will(returnValue(country));
            allowing(country).getIsoCode(); will(returnValue(countryIsoCode));
            oneOf(context).getAttribute(TargetingContext.POSTAL_CODE); will(returnValue(postalCode));
            oneOf(canadianPostalCodeManager).get(postalCode); will(returnValue(null));
        }});
        assertNull(impl.getAttribute(TargetingContext.CANADIAN_POSTAL_CODE, context));
    }


    @Test
    public void deriveChinesePostalCodeEmptyIfOtherCountry() {
        final CountryDto country = mock(CountryDto.class, "country");
        final String countryIsoCode = "DE";
        expect(new Expectations() {{
            oneOf(context).getAttribute(TargetingContext.COUNTRY); will(returnValue(country));
            allowing(country).getIsoCode(); will(returnValue(countryIsoCode));
        }});
        assertNull(impl.getAttribute(TargetingContext.CHINESE_POSTAL_CODE, context));
    }

    @Test
    public void deriveChinesePostalCodeEmptyIfNotPresentAndNoCoordinates() {
        final CountryDto country = mock(CountryDto.class, "country");
        final String countryIsoCode = "CN";
        final String postalCode = randomAlphaNumericString(10);
        expect(new Expectations() {{
            oneOf(context).getAttribute(TargetingContext.COUNTRY); will(returnValue(country));
            allowing(country).getIsoCode(); will(returnValue(countryIsoCode));
            oneOf(context).getAttribute(TargetingContext.POSTAL_CODE); will(returnValue(postalCode));
            oneOf(context).getAttribute(TargetingContext.COORDINATES); will(returnValue(null));
            oneOf(chinesePostalCodeManager).get(postalCode); will(returnValue(null));
        }});
        assertNull(impl.getAttribute(TargetingContext.CHINESE_POSTAL_CODE, context));
    }

    @Test
    public void deriveChinesePostalCodeUseNearestIfNotPresent() {
        final CountryDto country = mock(CountryDto.class, "country");
        final String countryIsoCode = "CN";
        final String postalCode = randomAlphaNumericString(10);
        final Coordinates coords = mock(Coordinates.class);
        final ChinesePostalCode chinesePostalCode = new ChinesePostalCodeStub(postalCode);
        expect(new Expectations() {{
            oneOf(context).getAttribute(TargetingContext.COUNTRY); will(returnValue(country));
            allowing(country).getIsoCode(); will(returnValue(countryIsoCode));
            oneOf(context).getAttribute(TargetingContext.POSTAL_CODE); will(returnValue(postalCode));
            oneOf(context).getAttribute(TargetingContext.COORDINATES); will(returnValue(coords));
            oneOf(chinesePostalCodeManager).get(postalCode); will(returnValue(null));
            oneOf(chinesePostalCodeManager).getNearest(coords); will(returnValue(chinesePostalCode));
        }});
        assertEquals(postalCode, ((ChinesePostalCode) impl.getAttribute(TargetingContext.CHINESE_POSTAL_CODE, context)).getPostalCode());
    }

    @Test
    public void deriveChinesePostalCodeCorrect() {
        final CountryDto country = mock(CountryDto.class, "country");
        final String countryIsoCode = "CN";
        final String postalCode = randomAlphaNumericString(10);
        final ChinesePostalCode chinesePostalCode = new ChinesePostalCodeStub(postalCode);
        expect(new Expectations() {{
            oneOf(context).getAttribute(TargetingContext.COUNTRY); will(returnValue(country));
            allowing(country).getIsoCode(); will(returnValue(countryIsoCode));
            oneOf(context).getAttribute(TargetingContext.POSTAL_CODE); will(returnValue(postalCode));
            oneOf(chinesePostalCodeManager).get(postalCode); will(returnValue(chinesePostalCode));
        }});
        assertEquals(postalCode, ((ChinesePostalCode) impl.getAttribute(TargetingContext.CHINESE_POSTAL_CODE, context)).getPostalCode());
    }


    @Test
    public void deriveSpanishPostalCodeCorrect() {
        final CountryDto country = mock(CountryDto.class, "country");
        final String countryIsoCode = "ES";
        final String postalCode = randomAlphaNumericString(10);
        String province = "Estremadura";
        final GeneralPostalCode spanishPostalCode = new PostalCodeImmutable("ES", postalCode, province, 0, 0);
        expect(new Expectations() {{
            oneOf(context).getAttribute(TargetingContext.COUNTRY); will(returnValue(country));
            allowing(country).getIsoCode(); will(returnValue(countryIsoCode));
            oneOf(context).getAttribute(TargetingContext.POSTAL_CODE); will(returnValue(postalCode));
            oneOf(spanishPostalCodeManager).get(postalCode); will(returnValue(spanishPostalCode));
        }});
        assertEquals(postalCode, ((GeneralPostalCode) impl.getAttribute(TargetingContext.SPANISH_POSTAL_CODE, context)).getPostalCode());
    }

    @Test
    public void deriveSpanishProvinceByIp() {
        final CountryDto country = mock(CountryDto.class, "country");
        final String countryIsoCode = "ES";
        expect(new Expectations() {{
            oneOf(context).getAttribute(Parameters.STATE); will(returnValue(null));
            oneOf(context).getAttribute(AbstractGeoDeriver.IP_BASED_DATA_RELIABLE, Boolean.class);
            will(returnValue(Boolean.TRUE));
            oneOf(context).getAttribute(TargetingContext.COUNTRY); will(returnValue(country));
            allowing(country).getIsoCode(); will(returnValue(countryIsoCode));
        }});
        assertEquals("Aragon", impl.getAttribute(TargetingContext.SPANISH_PROVINCE, context));
    }

    @Test
    public void deriveSpanishProvinceByParam() {
        final String province = "Estremadura";
        expect(new Expectations() {{
            oneOf(context).getAttribute(Parameters.STATE); will(returnValue(province));
        }});
        assertEquals(province, impl.getAttribute(TargetingContext.SPANISH_PROVINCE, context));
    }

    @Test
    public void deriveSpanishPostalCodeConcrete() {
        final String countryIsoCode = "ES";
        final String postalCode = randomAlphaNumericString(10);
        String province = "Estremadura";
        final GeneralPostalCode spanishPostalCode = new PostalCodeImmutable("ES", postalCode, province, 0,0);
        expect(new Expectations() {{
            oneOf (spanishPostalCodeManager).get(postalCode); will(returnValue(spanishPostalCode));
        }});

        impl.country = new CountryDto();
        impl.country.setIsoCode(countryIsoCode);
        impl.postalCode = postalCode;

        TargetingContext tc = new TargetingContextImpl(domainCache, null, deriverManager, null);

        assertEquals(postalCode, ((GeneralPostalCode) impl.getAttribute(TargetingContext.SPANISH_POSTAL_CODE, tc)).getPostalCode());
    }

    @Test
    public void deriveSpanishProvinceConcrete() {
        final String countryIsoCode = "ES";
        final String postalCode = randomAlphaNumericString(10);
        String province = "Estremadura";
        final GeneralPostalCode spanishPostalCode = new PostalCodeImmutable("ES", postalCode, province, 0,0);
        expect(new Expectations() {{
            oneOf (spanishPostalCodeManager).get(postalCode); will(returnValue(spanishPostalCode));
        }});

        impl.country = new CountryDto();
        impl.country.setIsoCode(countryIsoCode);
        impl.postalCode = postalCode;
        impl.spanishProvince = null;  //to force the geotargeting manager

        TargetingContext tc = new TargetingContextImpl(domainCache, null, deriverManager, null);

        assertEquals(province, impl.getAttribute(TargetingContext.SPANISH_PROVINCE, tc));
    }

    @Test
    public void deriveAustrianPostalCodeEmptyIfOtherCountry() {
        final CountryDto country = mock(CountryDto.class, "country");
        final String countryIsoCode = "DE";
        expect(new Expectations() {{
            oneOf(context).getAttribute(TargetingContext.COUNTRY); will(returnValue(country));
            allowing(country).getIsoCode(); will(returnValue(countryIsoCode));
        }});
        assertNull(impl.getAttribute(TargetingContext.AUSTRIAN_POSTAL_CODE, context));
    }

    @Test
    public void deriveAustrianPostalCodeEmptyIfNotPresentAndNoCoordinates() {
        final CountryDto country = mock(CountryDto.class, "country");
        final String countryIsoCode = "AT";
        final String postalCode = randomAlphaNumericString(10);
        expect(new Expectations() {{
            oneOf(context).getAttribute(TargetingContext.COUNTRY); will(returnValue(country));
            allowing(country).getIsoCode(); will(returnValue(countryIsoCode));
            oneOf(context).getAttribute(TargetingContext.POSTAL_CODE); will(returnValue(postalCode));
            oneOf(context).getAttribute(TargetingContext.COORDINATES); will(returnValue(null));
            oneOf(austrianPostalCodeManager).get(postalCode); will(returnValue(null));
        }});
        assertNull(impl.getAttribute(TargetingContext.AUSTRIAN_POSTAL_CODE, context));
    }


    @Test
    public void deriveAustrianPostalCodeUseNearestIfNotPresent() {
        final CountryDto country = mock(CountryDto.class, "country");
        final String countryIsoCode = "AT";
        final String postalCode = randomAlphaNumericString(10);
        final Coordinates coords = mock(Coordinates.class);
        final AustrianPostalCode austrianPostalCode = new AustrianPostalCodeStub(postalCode);
        expect(new Expectations() {{
            oneOf(context).getAttribute(TargetingContext.COUNTRY); will(returnValue(country));
            allowing(country).getIsoCode(); will(returnValue(countryIsoCode));
            oneOf(context).getAttribute(TargetingContext.POSTAL_CODE); will(returnValue(postalCode));
            oneOf(context).getAttribute(TargetingContext.COORDINATES); will(returnValue(coords));
            oneOf(austrianPostalCodeManager).get(postalCode); will(returnValue(null));
            oneOf(austrianPostalCodeManager).getNearest(coords); will(returnValue(austrianPostalCode));
        }});
        assertEquals(postalCode, ((AustrianPostalCode) impl.getAttribute(TargetingContext.AUSTRIAN_POSTAL_CODE, context)).getPostalCode());
    }


    @Test
    public void deriveAustrianPostalCodeCorrect() {
        final CountryDto country = mock(CountryDto.class, "country");
        final String countryIsoCode = "AT";
        final String postalCode = randomAlphaNumericString(10);
        final AustrianPostalCode austrianPostalCode = new AustrianPostalCodeStub(postalCode);
        expect(new Expectations() {{
            oneOf(context).getAttribute(TargetingContext.COUNTRY); will(returnValue(country));
            allowing(country).getIsoCode(); will(returnValue(countryIsoCode));
            oneOf(context).getAttribute(TargetingContext.POSTAL_CODE); will(returnValue(postalCode));
            oneOf(austrianPostalCodeManager).get(postalCode); will(returnValue(austrianPostalCode));
        }});
        assertEquals(postalCode, ((AustrianPostalCode) impl.getAttribute(TargetingContext.AUSTRIAN_POSTAL_CODE, context)).getPostalCode());
    }


    @Test
    public void testDeriveUSState01_parameter() {
        final USState usState = USState.KY;
        expect(new Expectations() {{
            oneOf(context).getAttribute(AbstractGeoDeriver.US_STATE_FROM_PARAMETER); will(returnValue(usState));
        }});
        assertEquals(usState, impl.getAttribute(TargetingContext.US_STATE, context));
    }

    @Test
    public void testDeriveUSState02_ip_based_not_US() {
        final CountryDto country = mock(CountryDto.class, "country");
        final String countryIsoCode = "DE";
        final USState usState = USState.KY;
        impl.usState = usState;
        expect(new Expectations() {{
            oneOf(context).getAttribute(AbstractGeoDeriver.US_STATE_FROM_PARAMETER); will(returnValue(null));
            oneOf(context).getAttribute(TargetingContext.COUNTRY); will(returnValue(country));
            allowing(country).getIsoCode(); will(returnValue(countryIsoCode));
        }});
        assertNull(impl.getAttribute(TargetingContext.US_STATE, context));
    }

    @Test
    public void testDeriveUSState03_ip_based() {
        final CountryDto country = mock(CountryDto.class, "country");
        final String countryIsoCode = "US";
        final USState usState = USState.KY;
        impl.usState = usState;
        expect(new Expectations() {{
            oneOf(context).getAttribute(AbstractGeoDeriver.US_STATE_FROM_PARAMETER); will(returnValue(null));
            oneOf(context).getAttribute(TargetingContext.COUNTRY); will(returnValue(country));
            allowing(country).getIsoCode(); will(returnValue(countryIsoCode));
            oneOf(context).getAttribute(AbstractGeoDeriver.IP_BASED_DATA_RELIABLE, Boolean.class);
            will(returnValue(Boolean.TRUE));
        }});
        assertEquals(usState, impl.getAttribute(TargetingContext.US_STATE, context));
    }

    @Test
    public void testDeriveUSState03_derived_null() {
        final CountryDto country = mock(CountryDto.class, "country");
        final String countryIsoCode = "US";
        expect(new Expectations() {{
            oneOf(context).getAttribute(AbstractGeoDeriver.US_STATE_FROM_PARAMETER); will(returnValue(null));
            oneOf(context).getAttribute(TargetingContext.COUNTRY); will(returnValue(country));
            allowing(country).getIsoCode(); will(returnValue(countryIsoCode));
            oneOf(context).getAttribute(AbstractGeoDeriver.IP_BASED_DATA_RELIABLE, Boolean.class);
            will(returnValue(Boolean.FALSE));
            oneOf(context).getAttribute(TargetingContext.US_ZIP_CODE); will(returnValue(null));
        }});
        assertNull(impl.getAttribute(TargetingContext.US_STATE, context));
    }

    @Test
    public void testDeriveUSState04_derived_unrecognized() {
        final CountryDto country = mock(CountryDto.class, "country");
        final String countryIsoCode = "US";
        final USZipCode usZipCode = mock(USZipCode.class);
        expect(new Expectations() {{
            oneOf(context).getAttribute(AbstractGeoDeriver.US_STATE_FROM_PARAMETER); will(returnValue(null));
            oneOf(context).getAttribute(TargetingContext.COUNTRY); will(returnValue(country));
            allowing(country).getIsoCode(); will(returnValue(countryIsoCode));
            oneOf(context).getAttribute(AbstractGeoDeriver.IP_BASED_DATA_RELIABLE, Boolean.class);
            will(returnValue(Boolean.FALSE));
            oneOf(context).getAttribute(TargetingContext.US_ZIP_CODE); will(returnValue(usZipCode));
            allowing(usZipCode).getState(); will(returnValue("not a valid state"));
        }});
        assertNull(impl.getAttribute(TargetingContext.US_STATE, context));
    }

    @Test
    public void testDeriveUSState05_derived() {
        final CountryDto country = mock(CountryDto.class, "country");
        final String countryIsoCode = "US";
        final USZipCode usZipCode = mock(USZipCode.class);
        final USState usState = USState.KY;
        expect(new Expectations() {{
            oneOf(context).getAttribute(AbstractGeoDeriver.US_STATE_FROM_PARAMETER); will(returnValue(null));
            oneOf(context).getAttribute(TargetingContext.COUNTRY); will(returnValue(country));
            allowing(country).getIsoCode(); will(returnValue(countryIsoCode));
            oneOf(context).getAttribute(AbstractGeoDeriver.IP_BASED_DATA_RELIABLE, Boolean.class);
            will(returnValue(Boolean.FALSE));
            oneOf(context).getAttribute(TargetingContext.US_ZIP_CODE); will(returnValue(usZipCode));
            allowing(usZipCode).getState(); will(returnValue(usState.name()));
        }});
        assertEquals(usState, impl.getAttribute(TargetingContext.US_STATE, context));
    }

    @Test
    public void testDeriveCanadianProvince01_parameter() {
        final CanadianProvince canadianProvince = CanadianProvince.BC;
        expect(new Expectations() {{
            oneOf(context).getAttribute(Parameters.STATE); will(returnValue(canadianProvince.name()));
        }});
        assertEquals(canadianProvince, impl.getAttribute(TargetingContext.CANADIAN_PROVINCE, context));
    }

    @Test
    public void testDeriveCanadianProvince02_ip_based_not_CA() {
        final CountryDto country = mock(CountryDto.class, "country");
        final String countryIsoCode = "DE";
        final CanadianProvince canadianProvince = CanadianProvince.BC;
        impl.canadianProvince = canadianProvince;
        expect(new Expectations() {{
            oneOf(context).getAttribute(Parameters.STATE); will(returnValue(null));
            oneOf(context).getAttribute(AbstractGeoDeriver.IP_BASED_DATA_RELIABLE, Boolean.class);
            will(returnValue(Boolean.TRUE));
            oneOf(context).getAttribute(TargetingContext.COUNTRY); will(returnValue(country));
            allowing(country).getIsoCode(); will(returnValue(countryIsoCode));
        }});
        assertNull(impl.getAttribute(TargetingContext.CANADIAN_PROVINCE, context));
    }

    @Test
    public void testDeriveCanadianProvince03_ip_based() {
        final CountryDto country = mock(CountryDto.class, "country");
        final String countryIsoCode = "CA";
        final CanadianProvince canadianProvince = CanadianProvince.BC;
        impl.canadianProvince = canadianProvince;
        expect(new Expectations() {{
            oneOf(context).getAttribute(Parameters.STATE); will(returnValue(null));
            oneOf(context).getAttribute(AbstractGeoDeriver.IP_BASED_DATA_RELIABLE, Boolean.class);
            will(returnValue(Boolean.TRUE));
            oneOf(context).getAttribute(TargetingContext.COUNTRY); will(returnValue(country));
            allowing(country).getIsoCode(); will(returnValue(countryIsoCode));
        }});
        assertEquals(canadianProvince, impl.getAttribute(TargetingContext.CANADIAN_PROVINCE, context));
    }

    @Test
    public void testDeriveCanadianProvince04_derived_null() {
        expect(new Expectations() {{
            oneOf(context).getAttribute(Parameters.STATE); will(returnValue(null));
            oneOf(context).getAttribute(AbstractGeoDeriver.IP_BASED_DATA_RELIABLE, Boolean.class);
            will(returnValue(Boolean.FALSE));
            oneOf(context).getAttribute(TargetingContext.CANADIAN_POSTAL_CODE); will(returnValue(null));
        }});
        assertNull(impl.getAttribute(TargetingContext.CANADIAN_PROVINCE, context));
    }

    @Test
    public void testDeriveCanadianProvince05_derived() {
        final CanadianProvince canadianProvince = CanadianProvince.BC;
        final CanadianPostalCode canadianPostalCode = mock(CanadianPostalCode.class);
        expect(new Expectations() {{
            oneOf(context).getAttribute(Parameters.STATE); will(returnValue(null));
            oneOf(context).getAttribute(AbstractGeoDeriver.IP_BASED_DATA_RELIABLE, Boolean.class);
            will(returnValue(Boolean.FALSE));
            oneOf(context).getAttribute(TargetingContext.CANADIAN_POSTAL_CODE); will(returnValue(canadianPostalCode));
            oneOf(canadianPostalCode).getCanadianProvince(); will(returnValue(canadianProvince));
        }});
        assertEquals(canadianProvince, impl.getAttribute(TargetingContext.CANADIAN_PROVINCE, context));
    }


    @Test
    public void deriveChineseProvinceByParam() {
        final ChineseProvince province = ChineseProvince.BJ;
        expect(new Expectations() {{
            oneOf(context).getAttribute(Parameters.STATE); will(returnValue(province.name()));
        }});
        assertEquals(province, impl.getAttribute(TargetingContext.CHINESE_PROVINCE, context));
    }

    @Test
    public void deriveChineseProvinceEmptyIfIpDataIfOutsideChina() {
        final CountryDto country = mock(CountryDto.class, "country");
        final String countryIsoCode = "DE";
        impl.austrianProvince = AustrianProvince.OO;
        expect(new Expectations() {{
            oneOf(context).getAttribute(Parameters.STATE); will(returnValue(null));
            oneOf(context).getAttribute(AbstractGeoDeriver.IP_BASED_DATA_RELIABLE, Boolean.class);
            will(returnValue(Boolean.TRUE));
            oneOf(context).getAttribute(TargetingContext.COUNTRY); will(returnValue(country));
            allowing(country).getIsoCode(); will(returnValue(countryIsoCode));
        }});
        assertNull(impl.getAttribute(TargetingContext.AUSTRIAN_PROVINCE, context));
    }

    @Test
    public void deriveChineseProvinceByIp() {
        final CountryDto country = mock(CountryDto.class, "country");
        final String countryIsoCode = "CN";
        impl.chineseProvince = ChineseProvince.LN;
        expect(new Expectations() {{
            oneOf(context).getAttribute(Parameters.STATE); will(returnValue(null));
            oneOf(context).getAttribute(AbstractGeoDeriver.IP_BASED_DATA_RELIABLE, Boolean.class);
            will(returnValue(Boolean.TRUE));
            oneOf(context).getAttribute(TargetingContext.COUNTRY); will(returnValue(country));
            allowing(country).getIsoCode(); will(returnValue(countryIsoCode));
        }});
        assertEquals(ChineseProvince.LN, impl.getAttribute(TargetingContext.CHINESE_PROVINCE, context));
    }

    @Test
    public void deriveChineseProvinceEmptyIfNoDataAvailable() {
        impl.chineseProvince = ChineseProvince.JL;
        expect(new Expectations() {{
            oneOf(context).getAttribute(Parameters.STATE); will(returnValue(null));
            oneOf(context).getAttribute(AbstractGeoDeriver.IP_BASED_DATA_RELIABLE, Boolean.class);
            will(returnValue(Boolean.FALSE));
            oneOf(context).getAttribute(TargetingContext.CHINESE_POSTAL_CODE); will(returnValue(null));
        }});
        assertNull(impl.getAttribute(TargetingContext.CHINESE_PROVINCE, context));
    }

    @Test
    public void deriveChineseProvinceFromPostalCodeIfNoReliableIp() {
        final ChineseProvince chineseProvince = ChineseProvince.NM;
        final ChinesePostalCode postalCode = new ChinesePostalCodeStub(chineseProvince);
        expect(new Expectations() {{
            oneOf(context).getAttribute(Parameters.STATE); will(returnValue(null));
            oneOf(context).getAttribute(AbstractGeoDeriver.IP_BASED_DATA_RELIABLE, Boolean.class);
            will(returnValue(Boolean.FALSE));
            oneOf(context).getAttribute(TargetingContext.CHINESE_POSTAL_CODE); will(returnValue(postalCode));
        }});
        assertEquals(chineseProvince, impl.getAttribute(TargetingContext.CHINESE_PROVINCE, context));
    }

    @Test
    public void deriveSpagnishProvinceByParam() {
        final String province = "Andorian";
        expect(new Expectations() {{
            oneOf (context).getAttribute(Parameters.STATE); will(returnValue(province));
        }});
        assertEquals(province, impl.getAttribute(TargetingContext.SPANISH_PROVINCE, context));
    }

    @Test
    public void deriveSpagnishProvinceByIp() {
        final CountryDto country = mock(CountryDto.class, "country");
        final String countryIsoCode = "ES";
        expect(new Expectations() {{
            oneOf (context).getAttribute(Parameters.STATE); will(returnValue(null));
            oneOf (context).getAttribute(AbstractGeoDeriver.IP_BASED_DATA_RELIABLE, Boolean.class); will(returnValue(Boolean.TRUE));
            oneOf (context).getAttribute(TargetingContext.COUNTRY); will(returnValue(country));
            allowing (country).getIsoCode(); will(returnValue(countryIsoCode));
        }});
        assertEquals("Aragon", impl.getAttribute(TargetingContext.SPANISH_PROVINCE, context));
    }

    @Test
    public void deriveAustrianProvinceByParam() {
        final AustrianProvince province = AustrianProvince.ST;
        expect(new Expectations() {{
            oneOf(context).getAttribute(Parameters.STATE); will(returnValue(province.name()));
        }});
        assertEquals(province, impl.getAttribute(TargetingContext.AUSTRIAN_PROVINCE, context));
    }

    @Test
    public void deriveAustrianProvinceEmptyIfIpDataIfOutsideAustria() {
        final CountryDto country = mock(CountryDto.class, "country");
        final String countryIsoCode = "DE";
        impl.austrianProvince = AustrianProvince.SZ;
        expect(new Expectations() {{
            oneOf(context).getAttribute(Parameters.STATE); will(returnValue(null));
            oneOf(context).getAttribute(AbstractGeoDeriver.IP_BASED_DATA_RELIABLE, Boolean.class);
            will(returnValue(Boolean.TRUE));
            oneOf(context).getAttribute(TargetingContext.COUNTRY); will(returnValue(country));
            allowing(country).getIsoCode(); will(returnValue(countryIsoCode));
        }});
        assertNull(impl.getAttribute(TargetingContext.AUSTRIAN_PROVINCE, context));
    }

    @Test
    public void deriveAustrianProvinceByIp() {
        final CountryDto country = mock(CountryDto.class, "country");
        final String countryIsoCode = "AT";
        impl.austrianProvince = AustrianProvince.OO;
        expect(new Expectations() {{
            oneOf(context).getAttribute(Parameters.STATE); will(returnValue(null));
            oneOf(context).getAttribute(AbstractGeoDeriver.IP_BASED_DATA_RELIABLE, Boolean.class);
            will(returnValue(Boolean.TRUE));
            oneOf(context).getAttribute(TargetingContext.COUNTRY); will(returnValue(country));
            allowing(country).getIsoCode(); will(returnValue(countryIsoCode));
        }});
        assertEquals(AustrianProvince.OO, impl.getAttribute(TargetingContext.AUSTRIAN_PROVINCE, context));
    }

    @Test
    public void deriveAustrianProvinceEmptyIfNoDataAvailable() {
        impl.austrianProvince = AustrianProvince.KA;
        expect(new Expectations() {{
            oneOf(context).getAttribute(Parameters.STATE); will(returnValue(null));
            oneOf(context).getAttribute(AbstractGeoDeriver.IP_BASED_DATA_RELIABLE, Boolean.class);
            will(returnValue(Boolean.FALSE));
            oneOf(context).getAttribute(TargetingContext.AUSTRIAN_POSTAL_CODE); will(returnValue(null));
        }});
        assertNull(impl.getAttribute(TargetingContext.AUSTRIAN_PROVINCE, context));
    }

    @Test
    public void deriveAustrianProvinceFromPostalCodeIfNoReliableIp() {
        final AustrianProvince austrianProvince = AustrianProvince.WI;
        final AustrianPostalCode postalCode = new AustrianPostalCodeStub(austrianProvince);
        expect(new Expectations() {{
            oneOf(context).getAttribute(Parameters.STATE); will(returnValue(null));
            oneOf(context).getAttribute(AbstractGeoDeriver.IP_BASED_DATA_RELIABLE, Boolean.class);
            will(returnValue(Boolean.FALSE));
            oneOf(context).getAttribute(TargetingContext.AUSTRIAN_POSTAL_CODE); will(returnValue(postalCode));
        }});
        assertEquals(austrianProvince, impl.getAttribute(TargetingContext.AUSTRIAN_PROVINCE, context));
    }

    @Test
    public void testDeriveDma01_parameter() {
        final Dma dma = mock(Dma.class);
        expect(new Expectations() {{
            oneOf(context).getAttribute(AbstractGeoDeriver.DMA_FROM_PARAMETER); will(returnValue(dma));
        }});
        assertEquals(dma, impl.getAttribute(TargetingContext.DMA, context));
    }

    @Test
    public void testDeriveDma02_ip_based_not_US() {
        final CountryDto country = mock(CountryDto.class, "country");
        final String countryIsoCode = "DE";
        final Dma dma = mock(Dma.class);
        impl.dma = dma;
        expect(new Expectations() {{
            oneOf(context).getAttribute(AbstractGeoDeriver.DMA_FROM_PARAMETER); will(returnValue(null));
            oneOf(context).getAttribute(AbstractGeoDeriver.IP_BASED_DATA_RELIABLE, Boolean.class);
            will(returnValue(Boolean.TRUE));
            oneOf(context).getAttribute(TargetingContext.COUNTRY); will(returnValue(country));
            allowing(country).getIsoCode(); will(returnValue(countryIsoCode));
        }});
        assertNull(impl.getAttribute(TargetingContext.DMA, context));
    }

    @Test
    public void testDeriveDma02_ip_based() {
        final CountryDto country = mock(CountryDto.class, "country");
        final String countryIsoCode = "US";
        final Dma dma = mock(Dma.class);
        impl.dma = dma;
        expect(new Expectations() {{
            oneOf(context).getAttribute(AbstractGeoDeriver.DMA_FROM_PARAMETER); will(returnValue(null));
            oneOf(context).getAttribute(AbstractGeoDeriver.IP_BASED_DATA_RELIABLE, Boolean.class);
            will(returnValue(Boolean.TRUE));
            oneOf(context).getAttribute(TargetingContext.COUNTRY); will(returnValue(country));
            allowing(country).getIsoCode(); will(returnValue(countryIsoCode));
        }});
        assertEquals(dma, impl.getAttribute(TargetingContext.DMA, context));
    }

    @Test
    public void testDeriveDma03_derived_null() {
        expect(new Expectations() {{
            oneOf(context).getAttribute(AbstractGeoDeriver.DMA_FROM_PARAMETER); will(returnValue(null));
            oneOf(context).getAttribute(AbstractGeoDeriver.IP_BASED_DATA_RELIABLE, Boolean.class);
            will(returnValue(Boolean.FALSE));
            oneOf(context).getAttribute(TargetingContext.US_ZIP_CODE); will(returnValue(null));
        }});
        assertNull(impl.getAttribute(TargetingContext.DMA, context));
    }

    @Test
    public void testDeriveDma04_derived() {
        final USZipCode usZipCode = mock(USZipCode.class);
        final String zip = randomAlphaNumericString(10);
        final Dma dma = mock(Dma.class);
        expect(new Expectations() {{
            oneOf(context).getAttribute(AbstractGeoDeriver.DMA_FROM_PARAMETER); will(returnValue(null));
            oneOf(context).getAttribute(AbstractGeoDeriver.IP_BASED_DATA_RELIABLE, Boolean.class);
            will(returnValue(Boolean.FALSE));
            oneOf(context).getAttribute(TargetingContext.US_ZIP_CODE); will(returnValue(usZipCode));
            oneOf(usZipCode).getZip(); will(returnValue(zip));
            oneOf(dmaManager).getDmaByZipCode(zip); will(returnValue(dma));
        }});
        assertEquals(dma, impl.getAttribute(TargetingContext.DMA, context));
    }

    @Test
    public void testDeriveTimeZone01_parameter_found() {
        expect(new Expectations() {{
            oneOf(context).getAttribute(Parameters.TIME_ZONE); will(returnValue("GMT"));
        }});
        assertNotNull(impl.getAttribute(TargetingContext.TIME_ZONE, context));
    }

    @Test
    public void testDeriveTimeZone02_parameter_not_found() {
        expect(new Expectations() {{
            oneOf(context).getAttribute(Parameters.TIME_ZONE); will(returnValue("not a valid time zone"));
        }});
        assertNull(impl.getAttribute(TargetingContext.TIME_ZONE, context));
    }

    @Test
    public void testDeriveTimeZone03_ip_based() {
        final TimeZone timeZone = mock(TimeZone.class);
        impl.timeZone = timeZone;
        expect(new Expectations() {{
            oneOf(context).getAttribute(Parameters.TIME_ZONE); will(returnValue(null));
        }});
        assertEquals(timeZone, impl.getAttribute(TargetingContext.TIME_ZONE, context));
    }

    @Test
    public void testDeriveCountryFromParameter01_no_country_code() {
        expect(new Expectations() {{
            oneOf(context).getAttribute(Parameters.COUNTRY_CODE); will(returnValue(null));
        }});
        assertNull(impl.getAttribute(AbstractGeoDeriver.COUNTRY_FROM_PARAMETER, context));
    }

    @Test
    public void testDeriveCountryFromParameter02_country_code_alpha2() {
        final String countryCode = randomAlphaString(2);
        final CountryDto country = mock(CountryDto.class);
        expect(new Expectations() {{
            oneOf(context).getAttribute(Parameters.COUNTRY_CODE); will(returnValue(countryCode));
            oneOf(context).getDomainCache(); will(returnValue(domainCache));
            oneOf(domainCache).getCountryByIsoCode(countryCode); will(returnValue(country));
        }});
        assertEquals(country, impl.getAttribute(AbstractGeoDeriver.COUNTRY_FROM_PARAMETER, context));
    }

    @Test
    public void testDeriveCountryFromParameter03_country_code_alpha3() {
        final String countryCode = randomAlphaString(3);
        final CountryDto country = mock(CountryDto.class);
        expect(new Expectations() {{
            oneOf(context).getAttribute(Parameters.COUNTRY_CODE); will(returnValue(countryCode));
            oneOf(context).getDomainCache(); will(returnValue(domainCache));
            oneOf(domainCache).getCountryByIsoAlpha3(countryCode); will(returnValue(country));
        }});
        assertEquals(country, impl.getAttribute(AbstractGeoDeriver.COUNTRY_FROM_PARAMETER, context));
    }

    @Test
    public void testDeriveCountryFromParameter04_country_name() {
        final String countryCode = randomAlphaString(10);
        final CountryDto country = mock(CountryDto.class);
        expect(new Expectations() {{
            oneOf(context).getAttribute(Parameters.COUNTRY_CODE); will(returnValue(countryCode));
            oneOf(context).getDomainCache(); will(returnValue(domainCache));
            oneOf(domainCache).getCountryByName(countryCode); will(returnValue(country));
        }});
        assertEquals(country, impl.getAttribute(AbstractGeoDeriver.COUNTRY_FROM_PARAMETER, context));
    }

    @Test
    public void testDeriveCountryFromParameter05_country_not_found() {
        final String countryCode = randomAlphaString(10);
        expect(new Expectations() {{
            oneOf(context).getAttribute(Parameters.COUNTRY_CODE); will(returnValue(countryCode));
            oneOf(context).getDomainCache(); will(returnValue(domainCache));
            oneOf(domainCache).getCountryByName(countryCode); will(returnValue(null));
        }});
        assertNull(impl.getAttribute(AbstractGeoDeriver.COUNTRY_FROM_PARAMETER, context));
    }

    @Test
    public void testDeriveCoordinatesFromParameters01_nothing() {
        expect(new Expectations() {{
            oneOf(context).getAttribute(Parameters.DEVICE_LATITUDE); will(returnValue(null));
            oneOf(context).getAttribute(Parameters.DEVICE_LONGITUDE); will(returnValue(null));
            oneOf(context).getAttribute(Parameters.USER_LATITUDE); will(returnValue(null));
            oneOf(context).getAttribute(Parameters.USER_LONGITUDE); will(returnValue(null));
        }});
        assertNull(impl.getAttribute(AbstractGeoDeriver.COORDINATES_FROM_PARAMETERS, context));
    }

    @Test
    public void testDeriveCoordinatesFromParameters02_user_coords() {
        final double latitude = 12.345;
        final double longitude = -123.456;
        expect(new Expectations() {{
            oneOf(context).getAttribute(Parameters.DEVICE_LATITUDE); will(returnValue(null));
            oneOf(context).getAttribute(Parameters.DEVICE_LONGITUDE); will(returnValue(null));
            oneOf(context).getAttribute(Parameters.USER_LATITUDE); will(returnValue(String.valueOf(latitude)));
            oneOf(context).getAttribute(Parameters.USER_LONGITUDE); will(returnValue(String.valueOf(longitude)));
        }});
        Coordinates coords = (Coordinates) impl.getAttribute(AbstractGeoDeriver.COORDINATES_FROM_PARAMETERS, context);
        assertNotNull(coords);
        assertEquals(latitude, coords.getLatitude(), 0.0);
        assertEquals(longitude, coords.getLongitude(), 0.0);
    }

    @Test
    public void testDeriveCoordinatesFromParameters03_device_coords() {
        final double latitude = 12.345;
        final double longitude = -123.456;
        expect(new Expectations() {{
            oneOf(context).getAttribute(Parameters.DEVICE_LATITUDE); will(returnValue(String.valueOf(latitude)));
            oneOf(context).getAttribute(Parameters.DEVICE_LONGITUDE); will(returnValue(String.valueOf(longitude)));
        }});
        Coordinates coords = (Coordinates) impl.getAttribute(AbstractGeoDeriver.COORDINATES_FROM_PARAMETERS, context);
        assertNotNull(coords);
        assertEquals(latitude, coords.getLatitude(), 0.0);
        assertEquals(longitude, coords.getLongitude(), 0.0);
    }

    @Test
    public void testDeriveCoordinatesFromParameters04_invalid_coords() {
        final double latitude = 90.1;
        final double longitude = 180.1;
        expect(new Expectations() {{
            oneOf(context).getAttribute(Parameters.DEVICE_LATITUDE); will(returnValue(String.valueOf(latitude)));
            oneOf(context).getAttribute(Parameters.DEVICE_LONGITUDE); will(returnValue(String.valueOf(longitude)));
        }});
        assertNull(impl.getAttribute(AbstractGeoDeriver.COORDINATES_FROM_PARAMETERS, context));
    }

    @Test
    public void testDeriveCoordinatesFromParameters05_number_format_exception() {
        expect(new Expectations() {{
            oneOf(context).getAttribute(Parameters.DEVICE_LATITUDE); will(returnValue(String.valueOf("x")));
            oneOf(context).getAttribute(Parameters.DEVICE_LONGITUDE); will(returnValue(String.valueOf("x")));
        }});
        assertNull(impl.getAttribute(AbstractGeoDeriver.COORDINATES_FROM_PARAMETERS, context));
    }

    @Test
    public void testDeriveUSStateFromParameter01_nothing() {
        expect(new Expectations() {{
            oneOf(context).getAttribute(Parameters.STATE); will(returnValue(null));
        }});
        assertNull(impl.getAttribute(AbstractGeoDeriver.US_STATE_FROM_PARAMETER, context));
    }

    @Test
    public void testDeriveUSStateFromParameter02_valid() {
        final USState usState = USState.KY;
        final String lowerState = usState.name().toLowerCase(); // should get upper'd
        expect(new Expectations() {{
            oneOf(context).getAttribute(Parameters.STATE); will(returnValue(lowerState));
        }});
        assertEquals(usState, impl.getAttribute(AbstractGeoDeriver.US_STATE_FROM_PARAMETER, context));
    }

    @Test
    public void testDeriveUSStateFromParameter03_invalid() {
        expect(new Expectations() {{
            oneOf(context).getAttribute(Parameters.STATE); will(returnValue("not a valid state"));
        }});
        assertNull(impl.getAttribute(AbstractGeoDeriver.US_STATE_FROM_PARAMETER, context));
    }

    @Test
    public void testDeriveCanadianProvinceFromParameter01_nothing() {
        expect(new Expectations() {{
            oneOf(context).getAttribute(Parameters.STATE); will(returnValue(null));
        }});
        assertNull(Impl.deriveCanadianProvinceFromParameter(context));
    }

    @Test
    public void testDeriveCanadianProvinceFromParameter02_valid() {
        final CanadianProvince canadianProvince = CanadianProvince.BC;
        final String lowerName = canadianProvince.name().toLowerCase();
        expect(new Expectations() {{
            oneOf(context).getAttribute(Parameters.STATE); will(returnValue(lowerName));
        }});
        assertEquals(canadianProvince, Impl.deriveCanadianProvinceFromParameter(context));
    }

    @Test
    public void testDeriveCanadianProvinceFromParameter03_invalid() {
        expect(new Expectations() {{
            oneOf(context).getAttribute(Parameters.STATE); will(returnValue("not a valid province"));
        }});
        assertNull(Impl.deriveCanadianProvinceFromParameter(context));
    }


    @Test
    public void deriveChineseProvinceFromParameterEmptyIfNotStateAvailable() {
        expect(new Expectations() {{
            oneOf(context).getAttribute(Parameters.STATE); will(returnValue(null));
        }});
        assertNull(Impl.deriveChineseProvinceFromParameter(context));
    }

    @Test
    public void deriveChineseProvinceFromParameterFromState() {
        final ChineseProvince province = ChineseProvince.HE;
        final String lowerName = province.name().toLowerCase();
        expect(new Expectations() {{
            oneOf(context).getAttribute(Parameters.STATE); will(returnValue(lowerName));
        }});
        assertEquals(province, Impl.deriveChineseProvinceFromParameter(context));
    }

    @Test
    public void deriveChineseProvinceFromParameterEmptyIfInvalidState() {
        expect(new Expectations() {{
            oneOf(context).getAttribute(Parameters.STATE); will(returnValue("not a valid province"));
        }});
        assertNull(Impl.deriveChineseProvinceFromParameter(context));
    }


    @Test
    public void deriveAustrianProvinceFromParameterEmptyIfNotStateAvailable() {
        expect(new Expectations() {{
            oneOf(context).getAttribute(Parameters.STATE); will(returnValue(null));
        }});
        assertNull(Impl.deriveAustrianProvinceFromParameter(context));
    }

    @Test
    public void deriveAustrianProvinceFromParameterFromState() {
        final AustrianProvince province = AustrianProvince.WI;
        final String lowerName = province.name().toLowerCase();
        expect(new Expectations() {{
            oneOf(context).getAttribute(Parameters.STATE); will(returnValue(lowerName));
        }});
        assertEquals(province, Impl.deriveAustrianProvinceFromParameter(context));
    }

    @Test
    public void deriveAustrianProvinceFromParameterEmptyIfInvalidState() {
        expect(new Expectations() {{
            oneOf(context).getAttribute(Parameters.STATE); will(returnValue("not a valid province"));
        }});
        assertNull(Impl.deriveAustrianProvinceFromParameter(context));
    }


    @Test
    public void testDeriveDmaFromParameter01_nothing() {
        expect(new Expectations() {{
            oneOf(context).getAttribute(Parameters.DMA); will(returnValue(null));
        }});
        assertNull(impl.getAttribute(AbstractGeoDeriver.DMA_FROM_PARAMETER, context));
    }

    @Test
    public void testDeriveDmaFromParameter02_id() {
        final String dmaParam = randomAlphaNumericString(10);
        final Dma dma = mock(Dma.class);
        expect(new Expectations() {{
            oneOf(context).getAttribute(Parameters.DMA); will(returnValue(dmaParam));
            oneOf(dmaManager).getDmaById(dmaParam); will(returnValue(dma));
        }});
        assertEquals(dma, impl.getAttribute(AbstractGeoDeriver.DMA_FROM_PARAMETER, context));
    }

    @Test
    public void testDeriveDmaFromParameter03_name() {
        final String dmaParam = randomAlphaNumericString(10);
        final Dma dma = mock(Dma.class);
        expect(new Expectations() {{
            oneOf(context).getAttribute(Parameters.DMA); will(returnValue(dmaParam));
            oneOf(dmaManager).getDmaById(dmaParam); will(returnValue(null));
            oneOf(dmaManager).getDmaByName(dmaParam); will(returnValue(dma));
        }});
        assertEquals(dma, impl.getAttribute(AbstractGeoDeriver.DMA_FROM_PARAMETER, context));
    }

    @Test
    public void testDeriveDmaFromParameter04_invalid() {
        final String dmaParam = randomAlphaNumericString(10);
        expect(new Expectations() {{
            oneOf(context).getAttribute(Parameters.DMA); will(returnValue(dmaParam));
            oneOf(dmaManager).getDmaById(dmaParam); will(returnValue(null));
            oneOf(dmaManager).getDmaByName(dmaParam); will(returnValue(null));
        }});
        assertNull(impl.getAttribute(AbstractGeoDeriver.DMA_FROM_PARAMETER, context));
    }

    @Test
    public void testDerivePostalCodeFromIp01_() {
        final String postalCode = randomAlphaNumericString(10);
        impl.postalCode = postalCode;
        assertEquals(postalCode, impl.getAttribute(AbstractGeoDeriver.POSTAL_CODE_FROM_IP, context));
    }

    @Test
    public void testDeriveUSStateFromIp01_() {
        final USState usState = USState.KY;
        impl.usState = usState;
        assertEquals(usState, impl.getAttribute(AbstractGeoDeriver.US_STATE_FROM_IP, context));
    }

    @Test
    public void testDeriveCountryFromIp01_() {
        final CountryDto country = mock(CountryDto.class);
        impl.country = country;
        assertEquals(country, impl.getAttribute(AbstractGeoDeriver.COUNTRY_FROM_IP, context));
    }

    @Test
    public void testDeriveDmaFromIp01_() {
        final Dma dma = mock(Dma.class);
        impl.dma = dma;
        assertEquals(dma, impl.getAttribute(AbstractGeoDeriver.DMA_FROM_IP, context));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDeriveUnknownAttribute() {
        impl.getAttribute("foobarbaz", context);
    }

}
