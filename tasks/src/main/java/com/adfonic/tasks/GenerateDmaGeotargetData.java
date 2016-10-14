package com.adfonic.tasks;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.WordUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.simple.parser.JSONParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.com.bytecode.opencsv.CSVReader;

import com.adfonic.geo.Coordinates;
import com.adfonic.geo.USZipCodeManager;
import com.adfonic.util.HttpUtils;

public class GenerateDmaGeotargetData implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(GenerateDmaGeotargetData.class);

    private static File inputFile;
    private static File sqlOutputFile;
    private static File kmlOutputFile;

    @Override
    public void run() {
        try {
            doRun();
        } catch (Exception e) {
            throw new IllegalStateException("doRun failed", e);
        }
    }

    private static final class Dma implements Comparable<Dma> {
        private String code;
        private String name;
        // This is actually a map from state abbreviation to the lowest
        // county size number encountered (low number = high population)
        private final Map<String, Integer> stateAbbrevs = new LinkedHashMap<String, Integer>();

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof Dma)) {
                return false;
            }
            Dma other = (Dma) o;
            return other.code.equals(this.code) && other.name.equals(this.name);
        }

        @Override
        public int hashCode() {
            return (code + name).hashCode();
        }

        @Override
        public int compareTo(Dma other) {
            return name.compareTo(other.name);
        }

        @Override
        public String toString() {
            return "Dma[code=" + code + ",name=" + name + ",states=" + stateAbbrevs + "]";
        }

        public List<String> getStateAbbrevsInOrderOfSize() {
            Map<Integer, List<String>> byCountySize = new TreeMap<Integer, List<String>>();
            for (Map.Entry<String, Integer> entry : stateAbbrevs.entrySet()) {
                int countySize = entry.getValue();
                List<String> list = byCountySize.get(countySize);
                if (list == null) {
                    list = new ArrayList<String>();
                    byCountySize.put(countySize, list);
                }
                list.add(entry.getKey());
            }
            List<String> stateAbbrevsInOrderOfSize = new ArrayList<String>();
            for (Map.Entry<Integer, List<String>> entry : byCountySize.entrySet()) {
                stateAbbrevsInOrderOfSize.addAll(entry.getValue());
            }
            return stateAbbrevsInOrderOfSize;
        }
    }

    private void doRun() throws java.io.IOException {
        // Sorry for the hack, but this is easier than adding this bean to
        // the tasks context -- it's not really needed by any other task.
        // And it's not worth adding a whole 'nuther context just for this.
        USZipCodeManager usZipCodeMgr = new USZipCodeManager(new File("/usr/local/adfonic/data/ziplist5-geo.csv"), 99999);
        usZipCodeMgr.initialize();

        Map<String, Dma> dmasByName = new TreeMap<String, Dma>();

        // Read distinct DMAs, tracking the set of states associated with each
        LOG.info("Reading DMA data from {}", inputFile.getCanonicalPath());
        CSVReader csvReader = new CSVReader(new FileReader(inputFile));
        String[] line;
        int count = 0;
        while ((line = csvReader.readNext()) != null) {
            ++count;

            // ZIP, DMA, DMA, ADDL,ST,  CTY, ST, CTY, CTY, NMR, NOT, DMA, METRO
            // CODE,CODE,NAME,DMA, CODE,CODE,ABV,NAME,SIZE,TERR,USED,RANK,INDIC
            String code = line[1];
            String name = capitalizeDmaName(line[2]);
            String stateAbbrev = line[6];
            int countySize = Integer.parseInt(line[8]);

            Dma dma = dmasByName.get(name);
            if (dma == null) {
                dma = new Dma();
                dma.code = code;
                dma.name = name;
                dmasByName.put(name, dma);
            }
            Integer priorCountySize = dma.stateAbbrevs.get(stateAbbrev);
            if (priorCountySize == null ||
            // County size is in reverse order...lower number is bigger
                    countySize < priorCountySize.intValue()) {
                dma.stateAbbrevs.put(stateAbbrev, countySize);
            }
        }

        LOG.info("Read {} lines of DMA data, found {} unique DMAs", count, dmasByName.size());

        Map<Dma, Coordinates> coordinatesByDma = new HashMap<Dma, Coordinates>();

        Set<Dma> dmasToResolve = new LinkedHashSet<Dma>(dmasByName.values());
        int iteration = 0;
        // Iterate until we've resolve them all.  Google Maps API really
        // sucks sometimes...it's pretty inconsistent, at least for non
        // premium api keys.
        while (!dmasToResolve.isEmpty()) {
            LOG.info("Iteration #{}, {} left to do", (++iteration), dmasToResolve.size());

            for (Iterator<Dma> iter = dmasToResolve.iterator(); iter.hasNext();) {
                Dma dma = iter.next();
                LOG.info(dma.toString());

                List<String> stuffToTry = new ArrayList<String>();
                // -&() city names, try separating them and adding each state
                for (String city : dma.name.split("[-&()]")) {
                    city = city.trim();
                    if (city.indexOf(", ") != -1) {
                        stuffToTry.add(city); // already has the , st
                    } else {
                        // We'll iterate through states in order of size.
                        for (String stateAbbrev : dma.getStateAbbrevsInOrderOfSize()) {
                            stuffToTry.add(city + ", " + stateAbbrev);
                        }
                    }
                }
                stuffToTry.add(dma.name);

                Coordinates coords = null;
                for (String tryThis : stuffToTry) {
                    try {
                        coords = geocode(tryThis);
                        break;
                    } catch (Exception e) {
                        continue;
                    }
                }

                if (coords == null) {
                    LOG.error("Failed to geocode: {}, tried: {}", dma, stuffToTry);
                    continue;
                }

                // Got it
                coordinatesByDma.put(dma, coords);
                iter.remove();
            }
        }

        LOG.info("Writing SQL output to {}", sqlOutputFile.getCanonicalPath());
        PrintStream sqlOut = new PrintStream(new FileOutputStream(sqlOutputFile));

        LOG.info("Writing KML output to {}", kmlOutputFile.getCanonicalPath());
        PrintStream kmlOut = new PrintStream(new FileOutputStream(kmlOutputFile));

        kmlOut.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        kmlOut.println("<kml xmlns=\"http://www.opengis.net/kml/2.2\">");
        kmlOut.println("<Folder>");
        kmlOut.println("<name>DMA</name>");

        sqlOut.println("SELECT ID INTO @cId FROM COUNTRY WHERE ISO_CODE='US';");
        sqlOut.println("DELETE FROM GEOTARGET WHERE TYPE='DMA';");

        for (Dma dma : dmasByName.values()) {
            Coordinates coords = coordinatesByDma.get(dma);

            sqlOut.println("INSERT INTO GEOTARGET (NAME,COUNTRY_ID,TYPE,DISPLAY_LATITUDE,DISPLAY_LONGITUDE) VALUES ('" + dma.name + "',@cId,'DMA'," + coords.getLatitude() + ","
                    + coords.getLongitude() + ");");

            kmlOut.println("<Placemark><name>" + dma.name.replaceAll("&", "&amp;") + "</name><Point><coordinates>" + coords.getLongitude() + "," + coords.getLatitude()
                    + "</coordinates></Point></Placemark>");
        }

        kmlOut.println("</Folder>");
        kmlOut.println("</kml>");

        sqlOut.close();
        kmlOut.close();
    }

    private Coordinates geocode(String location) throws Exception {
        Map<String, String> params = new LinkedHashMap<String, String>();
        params.put("key", "ABQIAAAAgAoXjBu9Lgw5qGN-i2gsGRQHkPubxCKGbTfDBhilQBEy1HfyeBQPcd8HQ8edCAlA1Uth6qd6nzOQ2Q");
        params.put("output", "json");
        params.put("sensor", "false");
        params.put("oe", "utf8");
        params.put("q", location);

        Map<String, String> headers = new LinkedHashMap<String, String>();
        headers.put("User-Agent", "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.6; en-US; rv:1.9.2.12) Gecko/20101026 Firefox/3.6.12");

        InputStream inputStream = null;

        String url = "http://maps.googleapis.com/maps/geo?" + HttpUtils.encodeParams(params);
        HttpGet httpGet = new HttpGet(url);
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            httpGet.setHeader(entry.getKey(), entry.getValue());
        }
        DefaultHttpClient httpClient = new DefaultHttpClient();
        HttpResponse httpResponse = httpClient.execute(httpGet);
        HttpEntity httpEntity = httpResponse.getEntity();
        try {
            if (httpResponse.getStatusLine().getStatusCode() >= 300) {
                throw new HttpResponseException(httpResponse.getStatusLine().getStatusCode(), httpResponse.getStatusLine().getReasonPhrase());
            }
            inputStream = httpEntity.getContent();
            Map json = (Map) new JSONParser().parse(new InputStreamReader(inputStream));
            List placemarks = (List) json.get("Placemark");
            if (placemarks == null || placemarks.isEmpty()) {
                LOG.warn("No placemarks for: {}", location);
                throw new IllegalArgumentException(location);
            }
            Map placemark = (Map) placemarks.get(0);
            Map point = (Map) placemark.get("Point");
            List coordList = (List) point.get("coordinates");
            final double longitude = ((Number) coordList.get(0)).doubleValue();
            final double latitude = ((Number) coordList.get(1)).doubleValue();
            return new Coordinates() {
                @Override
                public double getLatitude() {
                    return latitude;
                }

                @Override
                public double getLongitude() {
                    return longitude;
                }
            };
        } finally {
            IOUtils.closeQuietly(inputStream);
            EntityUtils.consumeQuietly(httpEntity);
        }
    }

    // Find improperly capitalized state abbreviations following either
    // comma-space, or anything other than whitespace and letters, i.e.
    // ", Az" or "-Az"
    private static final Pattern STATE_ABBREV_PATTERN = Pattern.compile("(,\\s+|[^\\sA-Za-z])([A-Z][a-z])\\b");

    /*package*/static String capitalizeDmaName(String text) {
        // First Capitalize All-Words (Like This)
        text = WordUtils.capitalizeFully(text, new char[] { ' ', '.', '-', '(', ')', '&' });
        // Now capitalize state abbreviations like: Boston, Ma -> Boston, MA
        StringBuffer sb = new StringBuffer();
        Matcher matcher = STATE_ABBREV_PATTERN.matcher(text);
        while (matcher.find()) {
            matcher.appendReplacement(sb, matcher.group(1) + matcher.group(2).toUpperCase());
        }
        matcher.appendTail(sb);
        return sb.toString()
        // Watch out for FT and ST and EL being improperly capitalized
                .replaceAll("\\bFT\\b", "Ft").replaceAll("\\bST\\b", "St").replaceAll("\\bEL\\b", "El");
    }

    public static void main(String[] args) {
        int exitCode = 0;
        try {
            if (args.length < 3) {
                throw new Exception("Usage: GenerateDmaGeotargetData <inputFile> <sqlOutputFile> <kmlOutputFile>");
            }
            inputFile = new File(args[0]);
            sqlOutputFile = new File(args[1]);
            kmlOutputFile = new File(args[2]);
            SpringTaskBase.runBean(GenerateDmaGeotargetData.class, "adfonic-toolsdb-context.xml", "adfonic-tasks-context.xml");
        } catch (Exception e) {
            LOG.error("Exception caught {}", e);
            exitCode = 1;
        } finally {
            Runtime.getRuntime().exit(exitCode);
        }
    }
}
