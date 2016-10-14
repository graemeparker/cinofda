package com.adfonic.tasks;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.PrintStream;
import java.util.Map;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.com.bytecode.opencsv.CSVReader;

import com.adfonic.geo.USZipCode;
import com.adfonic.geo.USZipCodeManager;

public class GenerateZipToDma implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(GenerateZipToDma.class);

    private static File inputFile;
    private static File outputFile;

    @Override
    public void run() {
        try {
            doRun();
        } catch (Exception e) {
            throw new IllegalStateException("doRun failed", e);
        }
    }

    private static final class Dma {
        private String code;
        private String name;
    }

    private void doRun() throws java.io.IOException {
        // Sorry for the hack, but this is easier than adding this bean to
        // the tasks context -- it's not really needed by any other task.
        // And it's not worth adding a whole 'nuther context just for this.
        USZipCodeManager usZipCodeMgr = new USZipCodeManager(new File("/usr/local/adfonic/data/ziplist5-geo.csv"), 99999);
        usZipCodeMgr.initialize();

        // We'll use TreeMap so it's sorted by zip code
        Map<String, Dma> zipToDma = new TreeMap<String, Dma>();

        LOG.info("Reading DMA data from " + inputFile.getCanonicalPath());
        CSVReader csvReader = new CSVReader(new FileReader(inputFile));
        String[] line;
        int count = 0;
        while ((line = csvReader.readNext()) != null) {
            ++count;

            // ZIP, DMA, DMA, ADDL,ST,  CTY, ST, CTY, CTY, NMR, NOT, DMA, METRO
            // CODE,CODE,NAME,DMA, CODE,CODE,ABV,NAME,SIZE,TERR,USED,RANK,INDIC
            String zip = line[0];
            Dma dma = new Dma();
            dma.code = line[1];
            dma.name = GenerateDmaGeotargetData.capitalizeDmaName(line[2]);

            // See if we've seen this zip code yet...
            if (zipToDma.containsKey(zip)) {
                // We've seen this zip before.  Here's an example of what it
                // looks like when you have multiple lines for the same zip:
                //
                // 40330	541	LEXINGTON	+	16	021	KY	BOYLE	9	2		063	
                // 40330	541	LEXINGTON	+	16	167	KY	MERCER	9	2		063	
                // 40330	529	LOUISVILLE	+	16	229	KY	WASHINGTON	9	2		050
                //
                // In order to resolve duplicates, we'll consider the DMA line
                // to be authoritative if it has the actual county name for the
                // given zip code.
                String dmaCountyName = line[7];

                // Look up the zip code record
                USZipCode usZipCode = usZipCodeMgr.get(zip);
                if (usZipCode == null) {
                    throw new RuntimeException("Unrecognized zip: " + zip);
                }
                String officialCountyName = usZipCode.getCounty();

                if (!dmaCountyName.equalsIgnoreCase(officialCountyName)) {
                    // This line isn't for the official county name, so just
                    // ignore it.  Assume that either the previous DMA line
                    // for this zip was for the official county name, or that
                    // we'll encounter the official county name in a subsequent
                    // line.
                    LOG.debug("Ignoring duplicate DMA line \"{}\" for {}, county={}, officialCounty={}", dma.name, zip, dmaCountyName, officialCountyName);
                    continue;
                }

                LOG.debug("Duplicate DMA line \"{}\" for {} goes to official county: {}", dma.name, zip, dmaCountyName);
                // Fall through and override the setting...
            }

            zipToDma.put(zip, dma);
        }

        LOG.info("Read {} lines of DMA data", count);
        LOG.info("Processed {} unique zip codes", zipToDma.size());

        LOG.info("Writing CSV output to {}", outputFile.getCanonicalPath());
        FileOutputStream outputStream = new FileOutputStream(outputFile);
        PrintStream csvOut = new PrintStream(outputStream);
        for (Map.Entry<String, Dma> entry : zipToDma.entrySet()) {
            String zip = entry.getKey();
            Dma dma = entry.getValue();
            csvOut.println("\"" + zip + "\",\"" + dma.code + "\",\"" + dma.name + "\"");
        }
        csvOut.close();
    }

    public static void main(String[] args) {
        int exitCode = 0;
        try {
            if (args.length < 2) {
                throw new Exception("Usage: GenerateZipToDma <inputFile> <outputFile>");
            }
            inputFile = new File(args[0]);
            outputFile = new File(args[1]);
            SpringTaskBase.runBean(GenerateZipToDma.class, "adfonic-toolsdb-context.xml", "adfonic-tasks-context.xml");
        } catch (Exception e) {
            LOG.error("Exception caught {}", e);
            exitCode = 1;
        } finally {
            Runtime.getRuntime().exit(exitCode);
        }
    }
}
