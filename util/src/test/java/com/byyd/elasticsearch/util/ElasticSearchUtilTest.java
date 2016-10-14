package com.byyd.elasticsearch.util;

import static com.byyd.elasticsearch.util.ElasticSearchUtil.*;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

public class ElasticSearchUtilTest {

    private static final String HIT_SOURCE_JSON = "elastic-search-source.json";

    private static ObjectMapper mapper;

    @BeforeClass
    public static void loadElasticSource() {
        mapper = new ObjectMapper();
    }

    @Test
    public void hitSourceShouldNotBeNull() {
        assertNotNull("Hit source file should follow JSON structure", getHitSource());
    }

    // lookupValueFromHitSource API test cases

    @Test
    public void missingPropertyLookupTest() {
        assertTrue(PROP_NOT_FOUND.equals(lookupValueFromHitSource(getHitSource(), "missing-property")));
    }

    @Test
    public void simplePropertyLookupTest() {
        assertTrue("5ad4fa0a-a171-4a69-a79f-c099c7d5e434".equals(lookupValueFromHitSource(getHitSource(), "companyId")));
    }

    @Test
    public void nestedPropertyLookupTest() {
        assertTrue("value".equals(lookupValueFromHitSource(getHitSource(), "nested.property")));
    }

    @Test
    public void nestedPropertyMatchLookupTest() {
        assertTrue("2562-1440149155324".equals(lookupValueFromHitSource(getHitSource(), "notifications[audienceId=2562].sessionId")));
        Long val = 2562L;
        assertTrue(("notifications[audienceId=2562].sessionId".equals(MessageFormat.format("notifications[audienceId={0}].sessionId", String.format("%d", val)))));
    }

    @Test
    public void missingPropertyNotMatchLookupTest() {
        assertTrue(PROP_NOT_FOUND.equals(lookupValueFromHitSource(getHitSource(), "notifications[audienceId=227].missing-property")));
    }

    @Test
    public void nestedPropertyNoMatchLookupTest() {
        assertTrue(PROP_NOT_FOUND.equals(lookupValueFromHitSource(getHitSource(), "notifications[audienceId=99999].sessionId")));
    }

    @Test
    public void nestedPropertyEmptyMatchLookupTest() {
        assertTrue(PROP_NOT_FOUND.equals(lookupValueFromHitSource(getHitSource(), "empty-array[matcher=value].sub-property")));
    }

    @Test
    public void deeplyNestedPropertyMatchLookupTest() {
        assertTrue("val2".equals(lookupValueFromHitSource(getHitSource(), "nested.nested.deep-array[prop22=val22].prop2")));
    }

    // buildNestedPropertyMatcher API test cases

    @Test
    public void buildNestedPropertyMatcherTest() {
        assertTrue("notifications[audienceId=123].sessionId".equals(buildNestedPropertyMatcher("notifications", "audienceId", "123", "sessionId")));
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> getHitSource() {
        try {
            URL resourceURL = getClass().getClassLoader().getResource(HIT_SOURCE_JSON);
            assertNotNull("Resource not exist with name: " + HIT_SOURCE_JSON, resourceURL);
            return mapper.readValue(new File(resourceURL.getFile()), Map.class);
        } catch (IOException e) {
            return null;
        }
    }

}
