package com.adfonic.tasks;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Map;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

public class GeneratePostalCodesDataFile implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(GeneratePostalCodesDataFile.class);

    private static File dataFile;

    @Autowired
    private DataSource dataSource;

    @Override
    public void run() {
        try {
            doRun();
        } catch (Exception e) {
            LOG.error("The sky is falling {}", e);
        }
    }

    private void doRun() throws java.io.IOException, org.springframework.dao.DataAccessException {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

        LOG.info("Writing data file: {}", dataFile);
        FileOutputStream fos = new FileOutputStream(dataFile);
        PrintStream out = new PrintStream(fos);
        try {
            for (Map<String, Object> row : jdbcTemplate
                    .queryForList("SELECT POSTAL_CODE.ID, COUNTRY.ISO_CODE, POSTAL_CODE.POSTAL_CODE FROM POSTAL_CODE JOIN COUNTRY ON COUNTRY.ID=POSTAL_CODE.COUNTRY_ID ORDER BY POSTAL_CODE.ID ASC")) {
                // Make sure to lowercase the postal code
                out.println(row.get("ID") + "," + row.get("ISO_CODE") + "," + ((String) row.get("POSTAL_CODE")).toLowerCase());
            }
        } finally {
            out.close();
            fos.close();
        }
    }

    public static void main(String[] args) {
        int exitCode = 0;
        try {
            if (args.length != 1) {
                throw new Exception("Usage: GeneratePostalCodesDataFile <dataFile>");
            }
            dataFile = new File(args[0]);
            SpringTaskBase.runBean(GeneratePostalCodesDataFile.class, "adfonic-toolsdb-context.xml");
        } catch (Exception e) {
            LOG.error("Exception caught {}", e);
            exitCode = 1;
        } finally {
            Runtime.getRuntime().exit(exitCode);
        }
    }
}
