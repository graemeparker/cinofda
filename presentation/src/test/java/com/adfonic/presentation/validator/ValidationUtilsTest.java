package com.adfonic.presentation.validator;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.junit.BeforeClass;
import org.junit.Test;

public class ValidationUtilsTest {

    private static final String VALID_URL_FILE = "Destinations_Valid_Urls.txt";
    private static final String INVALID_URL_FILE = "Destinations_Invalid_Urls.txt";
    private static final String PROD_URL_FILE = "Destinations_Prod_Urls.txt";

    private static List<String> validUrls;
    private static List<String> invalidUrls;
    private static List<String> prodUrls;

    @BeforeClass
    public static void beforeClass() {
        validUrls = readUrls(VALID_URL_FILE);
        invalidUrls = readUrls(INVALID_URL_FILE);
        prodUrls = readUrls(PROD_URL_FILE);
    }

    // Basic URL validations

    @Test
    public void testNullOrEmptyUrl() {
        assertTrue((ValidationUtils.validateUrl(null)).isFailed());
        assertFalse((ValidationUtils.validateUrl(null, false)).isFailed());

        assertTrue((ValidationUtils.validateUrl("")).isFailed());
        assertFalse((ValidationUtils.validateUrl("", false)).isFailed());
    }

    // Max URL length test

    @Test
    public void testUrlMaxLength() {
        assertEquals(1024, ValidationUtils.getUrlMaxLength());

        testValidUrl("http://www." + StringUtils.repeat("b", 1008) + "co.uk"); // 1024 characters
        testInvalidUrl("http://www." + StringUtils.repeat("x", 1010) + ".com"); // 1025 characters
    }

    // Valid URL tests

    @Test
    public void testValidUrlsFromFile() {
        validateURLsFromFile(validUrls, VALID_URL_FILE, true);
    }

    // Invalid URL tests

    @Test
    public void testInvalidUrlsFromFile() {
        validateURLsFromFile(invalidUrls, INVALID_URL_FILE, false);
    }

    // Production url tests

    @Test
    public void testProdUrlsFromFile() throws IOException {
        validateURLsFromFile(prodUrls, PROD_URL_FILE, true);
    }

    // Private methods

    private void testValidUrl(String url) {
        testUrl(url, false);
    }

    private void testInvalidUrl(String url) {
        testUrl(url, true);
    }

    private void testUrl(String url, boolean shouldFail) {
        assertEquals("The '" + url + "' URL is " + ((shouldFail) ? "valid" : "invalid") + " (required case) based on validator.", shouldFail, ValidationUtils
                .validateUrl(url, true).isFailed());
        assertEquals("The '" + url + "' URL is " + ((shouldFail) ? "valid" : "invalid") + " (not required case) based on validator.", shouldFail,
                ValidationUtils.validateUrl(url, false).isFailed());
    }

    private void validateURLsFromFile(List<String> urls, String fileName, boolean shouldBeValid) {

        StringBuffer urlList = new StringBuffer();
        Integer cnt = 0;
        ValidationResult validationResult;
        for (String url : urls) {
            validationResult = ValidationUtils.validateUrl(url); // Validate URL

            // valid check
            if (shouldBeValid && validationResult.isFailed()) {
                urlList.append("\t").append("[").append(validationResult.getMessageKey()).append("]\t").append(url).append("\n");
                cnt++;
            }
            // invalid check
            else if (!shouldBeValid && !validationResult.isFailed()) {
                urlList.append("\t").append(url).append("\n");
                cnt++;
            }

        }

        assertEquals(new StringBuffer(cnt.toString()).append(" URLs are " + ((shouldBeValid) ? "not" : "") + " valid (" + fileName + "):\n").append(urlList).toString(),
                urlList.length(), 0);
    }

    private static List<String> readUrls(String fileName) {
        Charset charset = Charset.forName("UTF-8");
        Path path = Paths.get("src/test/resources", fileName);
        assertTrue("URLs test file ('" + path + "') does not found.", Files.exists(path));
        List<String> urls = new ArrayList<>();
        try (BufferedReader reader = Files.newBufferedReader(path, charset)) {
            String line = null;
            while ((line = reader.readLine()) != null) {
                if (!line.startsWith("#") && !line.isEmpty()) {
                    urls.add(line);
                }
            }
        } catch (IOException ioe) {
            System.err.format("IOException: %s%n", ioe);
        }
        return urls;
    }

}
