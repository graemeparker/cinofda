package com.adfonic.geo;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Manager of the mappings from country + postal code to POSTAL_CODE.ID.
 * This wraps around the postal-codes.csv data file, which is expected to
 * have a format like:
 *
 * 1,US,00501
 * 2,US,00544
 * ...
 * 42058,GB,ab10
 * 42059,GB,ab11
 * ...
 * 45603,CA,a1a 2l3
 * 45604,CA,a1a 2l4
 * ...
 */
public class PostalCodeIdManager extends AbstractCsvDataManager<Map<String, Long>> {
    private static final short INDEX_POSTALCODE_ID = 0;
    private static final short INDEX_COUNTRY_ISOCODE = 1;
    private static final short INDEX_POSTALCODE = 2;

    public PostalCodeIdManager(File dataFile, int checkForUpdatesPeriodSec) {
        super(dataFile, checkForUpdatesPeriodSec);
    }

    @Override
    protected void processCsvLine(String[] line, Map<String, Map<String, Long>> byCountry) {
        long postalCodeId = Long.parseLong(line[INDEX_POSTALCODE_ID]);
        String countryIsoCode = line[INDEX_COUNTRY_ISOCODE];
        String postalCode = line[INDEX_POSTALCODE];

        Map<String, Long> byPostalCode = byCountry.get(countryIsoCode);
        if (byPostalCode == null) {
            byPostalCode = new HashMap<String, Long>();
            byCountry.put(countryIsoCode, byPostalCode);
        }
        byPostalCode.put(postalCode, postalCodeId);
    }

    /**
     * Get a POSTAL_CODE.ID by country and postal code
     * @param countryIsoCode the COUNTRY.ISO_CODE
     * @param postalCode the POSTAL_CODE.POSTAL_CODE
     * @return the respective POSTAL_CODE.ID value, or null if not found
     */
    public Long getPostalCodeId(String countryIsoCode, String postalCode) {
        Map<String, Long> byPostalCode = get(countryIsoCode);
        // AF-1466 - force the input postalCode value to lowercase for the lookup
        return byPostalCode == null ? null : byPostalCode.get(postalCode.toLowerCase());
    }
}
