package com.adfonic.tasks.combined;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPOutputStream;

import javax.sql.DataSource;

import org.apache.commons.dbutils.DbUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.adfonic.tasks.combined.mmx.LookupFileDefinition;
import com.adfonic.util.ConfUtils;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.util.Base16;
import com.amazonaws.util.Md5Utils;

//@Component
public class MetamarketsLookupTableTask {

    private static final Logger LOG = LoggerFactory.getLogger(MetamarketsLookupTableTask.class.getName());

    @Autowired
    @Qualifier(ConfUtils.TOOLS_DS)
    public DataSource toolsDataSource;
    @Autowired
    @Qualifier(ConfUtils.ADM_REPORTING_DS)
    public DataSource admReportingDataSource;

    @Value("${MetamarketsLookupTableTask.enabled:true}")
    private boolean enabled = true;
    @Value("${MetamarketsLookupTableTask.s3.path:current/}")
    private String s3Path = "current/";
    @Value("${MetamarketsLookupTableTask.s3.accessKey:AKIAJRQDOV4DMY47DKPQ}")
    private String s3AccessKey = "AKIAJRQDOV4DMY47DKPQ";
    @Value("${MetamarketsLookupTableTask.s3.secretKey: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}")
    private String s3SecretKey = "kldAIrcq+lUVbWqOMO6FDOoIbhTC3syuOAliqBq/";
    @Value("${MetamarketsLookupTableTask.s3.endpoint:https://s3.amazonaws.com}")
    private String s3EndPoint = "https://s3.amazonaws.com";
    @Value("${MetamarketsLookupTableTask.s3.bucket:byydmmxlookup}")
    private String s3Bucket = "byydmmxlookup";

    private AmazonS3Client client;

    private List<LookupFileDefinition> lookups = new ArrayList<>();

    // Run every hour
    //@Scheduled(fixedRate = 3600000)
    public void runPeriodically() {
        if (!enabled) {
            LOG.info("Disabled, skipping");
            return;
        }

        LOG.info("Producing lookup files for MMX with " + lookups.size());

        for (LookupFileDefinition lfd : lookups) {
            try {
                LOG.debug("Producing " + lfd.getName());

                File tmp = File.createTempFile("mmx-" + lfd.getName(), ".json");
                try (GZIPOutputStream gzo = new GZIPOutputStream(new FileOutputStream(tmp)); PrintWriter out = new PrintWriter(gzo);) {

                    if (lfd.getStaticContent() == null) {
                        queryData(lfd, out);
                    } else {
                        out.print(lfd.getStaticContent());
                    }
                }

                AmazonS3Client s3Client = retrieveClient();
                String hash = Base16.encodeAsString(Md5Utils.computeMD5Hash(tmp));

                String file = s3Path + lfd.getName() + ".json.gz";
                ObjectListing ls = s3Client.listObjects(s3Bucket, file);
                if (ls.getObjectSummaries().size() == 0 || !hash.equals(ls.getObjectSummaries().get(0).getETag())) {
                    ObjectMetadata meta = new ObjectMetadata();
                    meta.setContentLength(tmp.length());
                    s3Client.putObject(s3Bucket, file, new FileInputStream(tmp), meta);
                } else {
                    LOG.info("Unchanged file: " + lfd.getName() + " / " + hash);
                }

                tmp.delete();
            } catch (IOException ioe) {
                LOG.error("Unable to write file: {} {}", lfd.getName(), ioe);
            }
        }

        LOG.info("Finished cycle");
    }

    private AmazonS3Client retrieveClient() {
        if (client == null) {
            client = new AmazonS3Client(new BasicAWSCredentials(s3AccessKey, s3SecretKey));
        }

        try {
            client.listObjects(s3Bucket);
        } catch (Exception e) {
            client = new AmazonS3Client(new BasicAWSCredentials(s3AccessKey, s3SecretKey));
        }

        return client;
    }

    private void queryData(LookupFileDefinition lfd, PrintWriter out) {
        Connection admReportingConn = null;
        PreparedStatement pst = null;
        ResultSet rs = null;
        try {
            admReportingConn = admReportingDataSource.getConnection();
            pst = admReportingConn.prepareStatement(lfd.getQuery());
            rs = pst.executeQuery();
            String[] cols = lfd.getColumns().split(",");
            while (rs.next()) {
                StringBuilder sb = new StringBuilder("{");
                boolean first = true;
                for (String c : cols) {
                    String value = rs.getString(c);
                    if (!first) {
                        sb.append(", ");
                    } else {
                        first = false;
                    }
                    sb.append("\"").append(c.toLowerCase()).append("\":\"").append(escapeJson(value)).append("\"");
                }
                sb.append("}");
                out.println(sb.toString());
            }
        } catch (java.sql.SQLException e) {
            LOG.error("SQL select failed: {} {}", lfd.getQuery(), e);
            return;
        } finally {
            DbUtils.closeQuietly(admReportingConn, pst, rs);
        }
    }

    private String escapeJson(String value) {
        StringBuilder builder = new StringBuilder();

        if (value != null) {
            for (char c : value.toCharArray()) {
                switch (c) {
                case '"':
                case '\\':
                case '/':
                case '\f':
                    builder.append("\\");
                default:
                    if (c >= 32 && c < 256) {
                        builder.append(c);
                    } else {
                        if (c > 0) {
                            builder.append("\\u").append(String.format("%04d", (int) c));
                        }
                    }
                }
            }
        }

        return builder.toString();
    }

    public static void main(String[] args) {
        int exitCode = 0;
        try {
            ClassPathXmlApplicationContext cp1 = new ClassPathXmlApplicationContext("adfonic-toolsdb-context.xml");
            ClassPathXmlApplicationContext cp2 = new ClassPathXmlApplicationContext("adfonic-admreportingdb-context.xml");
            ClassPathXmlApplicationContext cp3 = new ClassPathXmlApplicationContext("adfonic-mmx-context.xml");

            MetamarketsLookupTableTask t = cp3.getBean(MetamarketsLookupTableTask.class);
            t.admReportingDataSource = (DataSource) cp2.getBean(ConfUtils.ADM_REPORTING_DS);
            t.toolsDataSource = (DataSource) cp1.getBean(ConfUtils.TOOLS_DS);

            LOG.info("Single run");
            t.runPeriodically();
            LOG.info("Finished");
        } catch (Exception e) {
            LOG.error("Exception caught {}", e);
            exitCode = 1;
        } finally {
            Runtime.getRuntime().exit(exitCode);
        }
    }

    public List<LookupFileDefinition> getLookups() {
        return lookups;
    }

    public void setLookups(List<LookupFileDefinition> lookups) {
        this.lookups = lookups;
    }
}
