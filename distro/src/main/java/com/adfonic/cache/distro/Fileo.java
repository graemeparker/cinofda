package com.adfonic.cache.distro;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;

public class Fileo {
    private static final transient Logger LOG = Logger.getLogger(Fileo.class.getName());

    private static final String CACHE_FILE_PATH = "/srv/cache/";
    private static final String HEALTH_CHECK_PATH = "healthcheck/";
    private static final String CACHE_FILE_NAME_REGEX = "^([^-]+)-(.+)-([^-]+)$";
    public static final String ADSERVER_DOMAIN_CACHE_FILENAME_REGEX = "^(AdserverDomainCache)-(.+)-([^-]+)$";
    public static final String DOMAIN_CACHE_FILENAME_REGEX = "^(DomainCache)-(.+)-([^-]+)$";
    
    public static final String ALGORITHM = "MD5";
    public static final String DAT = ".dat";
    public static final String GZ = ".gz";
    public static final String MD5 = "."+ ALGORITHM.toLowerCase();

    public static void main(String... args) throws IOException, NoSuchAlgorithmException {
        LOG.info("Fileo called directly from main. You shouldn't be doing this! Use Distro!");
    }

    // this will create the MD5 of the uncompressed file.
    // then write the compressed file
    public File compress(String sourceName) {
        String sourceFileName = CACHE_FILE_PATH + sourceName + DAT;
        File sourceCacheFile = new File(sourceFileName);
        md5(sourceCacheFile.getParent()+"/", sourceCacheFile.getName());
        LOG.info("Compressing file: " + sourceCacheFile.getName());
        File gzFile = new File(sourceFileName +GZ);
        GZIPOutputStream gzip = null;
        FileOutputStream fout = null;
        if(sourceCacheFile.exists()) {
            try {
                LOG.fine("Gzipping cache file to: " + gzFile.getAbsolutePath());
                fout = new FileOutputStream(gzFile);
                byte[] uncompressed = FileUtils.readFileToByteArray(sourceCacheFile);
                gzip = new GZIPOutputStream(fout);
                gzip.write(uncompressed);
                gzip.finish();
                LOG.fine("Gzipping finished");
            } catch (IOException ioe) {
                LOG.log(Level.SEVERE,"Problem compressing file: " + sourceFileName, ioe);
            } finally {
                try {
                    if (gzip != null){ 
                        gzip.close();
                    }
                    if (fout != null){
                        fout.close();
                    }
                } catch (IOException ioe){
                    LOG.log(Level.SEVERE,"Problem closing file: " + sourceFileName, ioe);
                }
            }
        } else {
            LOG.log(Level.SEVERE,"No such file exists at this path: " + sourceFileName);
        }
        LOG.info("Compressed file written to: " + gzFile.getName());
        return gzFile;
    }
    
    public File decompress(String sourceName) {
        LOG.info("Decompressing file: " + sourceName);
        File compresssedCacheFile = new File(sourceName);
        File uncompresssedCacheFile = null;
        ByteArrayOutputStream out = null;
        ByteArrayInputStream bin = null;
        GZIPInputStream gzipin = null;
        if(compresssedCacheFile.exists()) {
            try {
                    LOG.fine("unGzipping cache file to: " + sourceName);
                    byte[] uncompressed = FileUtils.readFileToByteArray(compresssedCacheFile);
                    bin = new ByteArrayInputStream(uncompressed);
                    out = new ByteArrayOutputStream();
                    gzipin = new GZIPInputStream(bin);
                    IOUtils.copy(gzipin, out);
                    uncompresssedCacheFile = write(compresssedCacheFile.getParent()+"/", FilenameUtils.getBaseName(compresssedCacheFile.getName()),out.toByteArray());
                    LOG.info("Decompressed file written to: " + uncompresssedCacheFile.getName());
            }catch (IOException ioe) {
                LOG.log(Level.SEVERE,"Problem compressing file: " + sourceName, ioe);
            } finally {
                try {
                    if (gzipin != null) {
                        gzipin.close();
                    }
                    if (out != null) {
                        out.close();
                    }
                    if (bin != null) {
                        bin.close();
                    }
                } catch (IOException ioe){
                    LOG.log(Level.SEVERE,"Problem closing file: " + sourceName, ioe);
                }
            }
        } else {
            LOG.log(Level.SEVERE,"No such file exists at this path: " + sourceName);
        }
        return uncompresssedCacheFile;
    }

    public File write(String dirName, String fileName, Object content) {
        File dir = new File(dirName);
        File fileToWRite = null;
        if(!dir.exists() && !dir.mkdirs()) { // only ever runs once
            LOG.log(Level.SEVERE, "Can not create folder...");
        }

        try {
            fileToWRite = new File(dirName + fileName);
            LOG.info("Writing to: " + fileToWRite.getName());
            if (content instanceof String) {
                LOG.info("Writing " + ((String) content).length() + " strings");
                FileUtils.writeStringToFile(fileToWRite, (String) content);
            } else if (content instanceof byte[]) {
                LOG.info("Writing " + ((byte[]) content).length + " bytes");
                FileUtils.writeByteArrayToFile(fileToWRite, (byte[]) content);
            } else {
                LOG.log(Level.SEVERE,"Invalid format for content. Can't proceed");
                //kill here
            }
        } catch (IOException ioe) {
            LOG.log(Level.SEVERE,"Problem writing to: " + fileToWRite.getName(), ioe);
        }
        return fileToWRite;
    }
    
    public boolean verify(File uncommpressedFile, String md5FileName) {
        File md5File = new File(md5FileName);
        LOG.info("Verifying file: " + uncommpressedFile.getName() + " using MD5 file: " + md5File.getName());
        if(md5File.exists()) {
            try {
                String[] split = FileUtils.readFileToString(md5File).split("  ");
                return generageMd5(uncommpressedFile.getParent(), uncommpressedFile.getName()).equals(split[0]);
            } catch (IOException e) {
                LOG.log(Level.SEVERE,"Problem verifying file: " + uncommpressedFile.getName(), e);
            }
        } else {
            LOG.log(Level.WARNING, "Cannot find MD5 file for verification: " + md5FileName);
        }
        return false;
    }
    
    private String generageMd5(String dirName, String fileName) {
        StringBuilder sb = new StringBuilder();
        try {
            LOG.info("Generating MD5 for file: " + fileName);
            MessageDigest md = MessageDigest.getInstance(ALGORITHM);
            String localDirName = "";
            if (dirName != null) {
                localDirName = dirName + "/";
            } 
            try (InputStream is = Files.newInputStream(Paths.get(localDirName + fileName))) {
                //read until EOF
                new DigestInputStream(is, md);
            }
            byte[] digest = md.digest();
            for (int i = 0; i < digest.length; ++i) {
                sb.append(Integer.toHexString((digest[i] & 0xFF) | 0x100).substring(1,3));
            }
        } catch (NoSuchAlgorithmException nsae) {
            LOG.log(Level.SEVERE,"No such algorithm for " + ALGORITHM, nsae);
        } catch (IOException ioe) {
            LOG.log(Level.SEVERE,"Problem reading file: " + fileName, ioe);
        } 
        return sb.toString();
    }
    
    private void md5(String dirName, String fileName) {
        LOG.info("Generating MD5 for file: " + fileName);
        write(dirName,fileName + MD5,generageMd5(dirName, fileName));
    }

    public String getVersion(String batch) {
        Matcher m1 = Pattern.compile(CACHE_FILE_NAME_REGEX, Pattern.CASE_INSENSITIVE).matcher(batch);
        StringBuilder version = new StringBuilder();
        if(m1.matches()) {
            version.append(m1.group(1)).append("-").append(m1.group(2));
            return version.toString();
        } else {
            LOG.log(Level.SEVERE,"Unrecognized batchId format: "+batch+" (can't track version for healthcheck)");
        }
        //returns empty string
        return version.toString();
    }

    public void deleteFile(String fileNameWithPath) {
        File fileToDelete = new File(fileNameWithPath);
        if(fileToDelete.exists()) {
            LOG.info("Removing file: " + fileNameWithPath);
            FileUtils.deleteQuietly(fileToDelete);
        } else {
            LOG.log(Level.SEVERE,"Cannot remove file: " + fileNameWithPath);
        }
    }

    public String setHealth(String version, String batch) {
        String healthCheckDir = CACHE_FILE_PATH + HEALTH_CHECK_PATH;
        String healthCheckFile = String.format("%s.txt", version);
        write(healthCheckDir, healthCheckFile, batch);
        return healthCheckDir + healthCheckFile;
    }
}
