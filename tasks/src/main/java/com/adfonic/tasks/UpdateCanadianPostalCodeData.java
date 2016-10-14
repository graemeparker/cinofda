package com.adfonic.tasks;

import java.io.File;
import java.io.FileReader;
import java.util.HashSet;
import java.util.Set;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import au.com.bytecode.opencsv.CSVReader;

public class UpdateCanadianPostalCodeData implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(UpdateCanadianPostalCodeData.class);

    private static final int NUM_HEADER_LINES_TO_SKIP = 2;

    private static File ziplistDataFile;

    @Autowired
    private DataSource dataSource;

    @Override
    public void run() {
        try {
            doRun();
        } catch (Exception e) {
            throw new IllegalStateException("doRun failed", e);
        }
    }

    private void doRun() throws java.io.IOException, org.springframework.dao.DataAccessException {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

        long canadaCountryId = jdbcTemplate.queryForObject("SELECT ID FROM COUNTRY WHERE ISO_CODE='CA'", Long.class);
        if (canadaCountryId <= 0) {
            throw new IllegalStateException("Canada not found in the database?!");
        }
        LOG.info("Canada's COUNTRY.ID is {}", canadaCountryId);

        LOG.info("Loading existing Canadian postal code data from the db...");
        // Since List lookups are slow, just build a HashSet
        Set<String> postalCodesAlreadyInTheDb = new HashSet<String>();
        for (String postalCode : jdbcTemplate.queryForList("SELECT POSTAL_CODE FROM POSTAL_CODE WHERE COUNTRY_ID=" + canadaCountryId, String.class)) {
            postalCodesAlreadyInTheDb.add(postalCode);
        }
        LOG.info("Found {} Canadian postal codes in the db", postalCodesAlreadyInTheDb.size());

        LOG.info("Reading Ziplist data from {}", ziplistDataFile.getCanonicalPath());
        if (!ziplistDataFile.exists()) {
            throw new java.io.FileNotFoundException(ziplistDataFile.getCanonicalPath());
        }
        CSVReader csvReader = new CSVReader(new FileReader(ziplistDataFile));
        String[] line;
        int count = 0;
        int addedCount = 0;
        while ((line = csvReader.readNext()) != null) {
            if (count++ < NUM_HEADER_LINES_TO_SKIP) {
                continue;
            }
            String postalCode = line[2];
            if (!postalCodesAlreadyInTheDb.contains(postalCode)) {
                LOG.info("Adding new postal code: {}", postalCode);
                // We use IGNORE here because the ziplist data has multiple rows for some postal codes
                addedCount += jdbcTemplate.update("INSERT IGNORE INTO POSTAL_CODE (COUNTRY_ID, POSTAL_CODE) VALUES (?,?)", canadaCountryId, postalCode);
            }
        }
        LOG.info("Processed " + count + " postal codes, added " + addedCount);
    }

    public static void main(String[] args) {
        int exitCode = 0;
        try {
            if (args.length != 1) {
                throw new Exception("Usage: UpdateCanadianPostalCodeData <ziplistDataFile>");
            }
            ziplistDataFile = new File(args[0]);
            SpringTaskBase.runBean(UpdateCanadianPostalCodeData.class, "adfonic-toolsdb-context.xml");
        } catch (Exception e) {
            LOG.error("Exception caught {}", e);
            exitCode = 1;
        } finally {
            Runtime.getRuntime().exit(exitCode);
        }
    }
}
