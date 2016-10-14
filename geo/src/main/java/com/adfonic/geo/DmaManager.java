package com.adfonic.geo;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/** Manager for accessing Nielsen DMA data by id or by zip code. This class
    loads mappings from zip codes to DMAs from a configured CSV file, expected
    to have the following format:
    
    zip code, DMA code, DMA name
    ...
    "40771","541","Lexington"
    "40801","557","Knoxville"
    "40803","531","Tri-Cities, TN-VA"
    "40806","557","Knoxville"
    ...
*/
public class DmaManager extends AbstractCsvDataManager<Dma> {

    private static final short INDEX_CODE = 1;
    private static final short INDEX_NAME = 2;

    private final Map<String, Dma> dmaById = new HashMap<String, Dma>();
    private final Map<String, Dma> dmaByName = new HashMap<String, Dma>();

    public DmaManager(File dataFile, int checkForUpdatesPeriodSec) {
        super(dataFile, checkForUpdatesPeriodSec);
    }

    @Override
    protected void processCsvLine(String[] line, Map<String, Dma> map) {
        if (line.length <= 1) {
            return;
        }
        String zip = line[0];
        // This is totally not the most efficient way to do this, since it
        // doesn't "reuse" Dma objects for multiple zip associations, but
        // the extra cost involved with wrapping the Dma around 2 strings
        // as opposed to just mapping to the name is negligible, given the
        // relatively small number of zip codes.  If we were doing this
        // right, we'd have 2 data files...one would contain all DMA data,
        // and the other would just have zip-to-DMA mappings by code.  We
        // can revisit this if we ever need more than this simple mapping.
        // As of right now (Nov 12, 2010), that's all we need.
        Dma dma = new DmaImmutable(line[INDEX_CODE], line[INDEX_NAME]);
        map.put(zip, dma);

        if (!dmaById.containsKey(dma.getCode())) {
            // Store our own mapping of DMAs by id
            dmaById.put(dma.getCode(), dma);
        }

        if (!dmaByName.containsKey(dma.getName())) {
            // Store our own mapping of DMAs by name
            dmaByName.put(dma.getName().toLowerCase(), dma);
        }
    }

    public Dma getDmaByZipCode(String zipCode) {
        return get(zipCode); // zipCode is the primary lookup key
    }

    public Dma getDmaById(String id) {
        return dmaById.get(id);
    }

    public Dma getDmaByName(String name) {
        return dmaByName.get(name.toLowerCase());
    }

    private static final class DmaImmutable implements Dma {
        private final String code;
        private final String name;

        private DmaImmutable(String code, String name) {
            this.code = code;
            this.name = name;
        }

        @Override
        public String getCode() {
            return code;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            } else if (obj == this) {
                return true;
            } else if (!(obj instanceof Dma)) {
                return false;
            }
            Dma dma = (Dma) obj;
            return dma.getCode().equals(this.code) && dma.getName().equals(this.name);
        }

        @Override
        public int hashCode() {
            return code.hashCode();
        }

        @Override
        public String toString() {
            return "Dma[code=" + code + ",name=" + name + "]";
        }
    }
}
