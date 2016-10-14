package com.adfonic.adserver.view;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jmock.Expectations;
import org.junit.Before;
import org.junit.Test;

import com.adfonic.adserver.AdComponents;
import com.adfonic.adserver.BaseAdserverTest;
import com.adfonic.adserver.Impression;
import com.adfonic.adserver.MarkupGenerator;
import com.adfonic.adserver.TargetingContext;
import com.adfonic.adserver.view.AbstractAdView.BeaconsMode;
import com.adfonic.domain.DestinationType;
import com.adfonic.domain.cache.dto.adserver.creative.CreativeDto;

@SuppressWarnings("rawtypes")
public class TestXmlAdView extends BaseAdserverTest {

    private XmlAdView xmlAdView;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private TargetingContext targetingContext;
    private CreativeDto creative;
    private Impression impression;
    private AdComponents adComponents;
    private MarkupGenerator markupGenerator;
    private Map model;

    @Before
    public void initTests() {
        xmlAdView = new XmlAdView();
        model = mock(Map.class);
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        targetingContext = mock(TargetingContext.class);
        creative = mock(CreativeDto.class);
        impression = mock(Impression.class);
        adComponents = mock(AdComponents.class);
        markupGenerator = mock(MarkupGenerator.class);
        inject(xmlAdView, "markupGenerator", markupGenerator);
    }

    @Test
    public void testXmlAdView01_renderAd() throws Exception {
        final Map<String, String> hm = new LinkedHashMap<String, String>();
        hm.put("beacons", "hello");
        final Map<String, String> bidhm = new LinkedHashMap<String, String>();
        bidhm.put("bid", "hellobid");
        final Map<String, Map<String, String>> map = new LinkedHashMap<String, Map<String, String>>();
        map.put("beacons", hm);
        map.put("bid", bidhm);

        final StringBuilder actual = new StringBuilder();

        final ServletOutputStream outStream = new ServletOutputStream() {
            @Override
            public void write(int c) throws IOException {
                actual.append((char) c);
            }

            @Override
            public boolean isReady() {
                return false;
            }

            @Override
            public void setWriteListener(WriteListener writeListener) {

            }
        };

        expect(new Expectations() {
            {
                oneOf(markupGenerator).generateMarkup(adComponents, targetingContext, null, creative, impression, false);
                will(returnValue("HelloTest123Flase"));
                allowing(impression).getExternalID();
                will(returnValue("HelloTest123"));
                allowing(adComponents).getFormat();
                will(returnValue("Format123"));
                oneOf(response).getOutputStream();
                will(returnValue(outStream));
                allowing(adComponents).getComponents();
                will(returnValue(map));
                allowing(adComponents).getDestinationType();
                will(returnValue(DestinationType.IPHONE_APP_STORE));
                allowing(adComponents).getDestinationUrl();
                will(returnValue("AdfonicURL"));
                oneOf(targetingContext).getAdSpace();
                will(returnValue(null));
            }
        });

        xmlAdView.renderAd(model, request, response, targetingContext, creative, impression, adComponents, true, BeaconsMode.metadata);
        String actualSting = actual.toString();

        String[] actualArray = actualSting.split("\\n");
        StringBuilder expected = new StringBuilder();
        expected.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        expected.append("\n");
        expected.append("<ad>");
        expected.append("\n");
        expected.append("<status>success</status>");
        expected.append("\n");
        expected.append("<adId>HelloTest123</adId>");
        expected.append("\n");
        expected.append("<format>Format123</format>");
        expected.append("\n");
        expected.append("<destination type=\"IPHONE_APP_STORE\" url=\"AdfonicURL\"/>");
        expected.append("\n");
        expected.append("<adContent><![CDATA[HelloTest123Flase]]></adContent>");
        expected.append("\n");
        expected.append("<components>");
        expected.append("\n");
        expected.append("<component type=\"beacons\">");
        expected.append("\n");
        expected.append("<attribute name=\"beacons\">hello</attribute>");
        expected.append("\n");
        expected.append("</component>");
        expected.append("\n");
        expected.append("<component type=\"bid\">");
        expected.append("\n");
        expected.append("<attribute name=\"bid\">hellobid</attribute>");
        expected.append("\n");
        expected.append("</component>");
        expected.append("\n");
        expected.append("</components>");
        expected.append("\n");
        expected.append("</ad>");
        expected.append("\n");
        String expectedSting = expected.toString();
        String[] expectedArray = expectedSting.split("\\n");

        boolean result = true;

        for (int i = 0; i < expectedArray.length; i++) {
            if (!actualArray[i].trim().equals(expectedArray[i])) {
                result = false;
            }
        }
        assertTrue(result);
    }

    @Test
    public void testXmlAdView02_renderAd() throws Exception {
        final Map<String, String> bidhm = new LinkedHashMap<String, String>();
        bidhm.put("bid", "hellobid");
        final Map<String, Map<String, String>> map = new LinkedHashMap<String, Map<String, String>>();
        map.put("bid", bidhm);

        final StringBuilder actual = new StringBuilder();

        final ServletOutputStream outStream = new ServletOutputStream() {
            @Override
            public void write(int c) throws IOException {
                actual.append((char) c);
            }

            @Override
            public boolean isReady() {
                return false;
            }

            @Override
            public void setWriteListener(WriteListener writeListener) {

            }
        };

        expect(new Expectations() {
            {
                oneOf(markupGenerator).generateMarkup(adComponents, targetingContext, null, creative, impression, true);
                will(returnValue("HelloTest123Flase"));
                allowing(impression).getExternalID();
                will(returnValue("HelloTest123"));
                allowing(adComponents).getFormat();
                will(returnValue("Format123"));
                oneOf(response).getOutputStream();
                will(returnValue(outStream));
                allowing(adComponents).getComponents();
                will(returnValue(map));
                allowing(adComponents).getDestinationType();
                will(returnValue(DestinationType.IPHONE_APP_STORE));
                allowing(adComponents).getDestinationUrl();
                will(returnValue("AdfonicURL"));
                oneOf(targetingContext).getAdSpace();
                will(returnValue(null));
            }
        });

        xmlAdView.renderAd(model, request, response, targetingContext, creative, impression, adComponents, true, BeaconsMode.markup);
        String actualSting = actual.toString();

        String[] actualArray = actualSting.split("\\n");
        StringBuilder expected = new StringBuilder();
        expected.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        expected.append("\n");
        expected.append("<ad>");
        expected.append("\n");
        expected.append("<status>success</status>");
        expected.append("\n");
        expected.append("<adId>HelloTest123</adId>");
        expected.append("\n");
        expected.append("<format>Format123</format>");
        expected.append("\n");
        expected.append("<destination type=\"IPHONE_APP_STORE\" url=\"AdfonicURL\"/>");
        expected.append("\n");
        expected.append("<adContent><![CDATA[HelloTest123Flase]]></adContent>");
        expected.append("\n");
        expected.append("<components>");
        expected.append("\n");
        expected.append("<component type=\"bid\">");
        expected.append("\n");
        expected.append("<attribute name=\"bid\">hellobid</attribute>");
        expected.append("\n");
        expected.append("</component>");
        expected.append("\n");
        expected.append("</components>");
        expected.append("\n");
        expected.append("</ad>");
        expected.append("\n");
        String expectedSting = expected.toString();
        String[] expectedArray = expectedSting.split("\\n");

        boolean result = true;

        for (int i = 0; i < expectedArray.length; i++) {
            if (!actualArray[i].trim().equals(expectedArray[i])) {
                result = false;
            }
        }
        assertTrue(result);

    }

    @Test
    public void testXmlAdView03_renderAd() throws Exception {
        final Map<String, Map<String, String>> map = new LinkedHashMap<String, Map<String, String>>();

        final StringBuilder actual = new StringBuilder();

        final ServletOutputStream outStream = new ServletOutputStream() {
            @Override
            public void write(int c) throws IOException {
                actual.append((char) c);
            }

            @Override
            public boolean isReady() {
                return false;
            }

            @Override
            public void setWriteListener(WriteListener writeListener) {

            }
        };

        expect(new Expectations() {
            {
                oneOf(markupGenerator).generateMarkup(adComponents, targetingContext, null, creative, impression, false);
                will(returnValue("HelloTest123Flase"));
                allowing(impression).getExternalID();
                will(returnValue("HelloTest123"));
                allowing(adComponents).getFormat();
                will(returnValue("Format123"));
                oneOf(response).getOutputStream();
                will(returnValue(outStream));
                allowing(adComponents).getComponents();
                will(returnValue(map));
                allowing(adComponents).getDestinationType();
                will(returnValue(DestinationType.IPHONE_APP_STORE));
                allowing(adComponents).getDestinationUrl();
                will(returnValue("AdfonicURL"));
                oneOf(targetingContext).getAdSpace();
                will(returnValue(null));
            }
        });

        xmlAdView.renderAd(model, request, response, targetingContext, creative, impression, adComponents, true, BeaconsMode.metadata);
        String actualSting = actual.toString();

        String[] actualArray = actualSting.split("\\n");
        StringBuilder expected = new StringBuilder();
        expected.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        expected.append("\n");
        expected.append("<ad>");
        expected.append("\n");
        expected.append("<status>success</status>");
        expected.append("\n");
        expected.append("<adId>HelloTest123</adId>");
        expected.append("\n");
        expected.append("<format>Format123</format>");
        expected.append("\n");
        expected.append("<destination type=\"IPHONE_APP_STORE\" url=\"AdfonicURL\"/>");
        expected.append("\n");
        expected.append("<adContent><![CDATA[HelloTest123Flase]]></adContent>");
        expected.append("\n");
        expected.append("</ad>");
        expected.append("\n");
        String expectedSting = expected.toString();
        String[] expectedArray = expectedSting.split("\\n");

        boolean result = true;

        for (int i = 0; i < expectedArray.length; i++) {
            if (!actualArray[i].trim().equals(expectedArray[i])) {
                result = false;
            }
        }
        assertTrue(result);
    }

    @Test
    public void testXmlAdView04_renderAd() throws Exception {
        final Map<String, String> hm = new LinkedHashMap<String, String>();
        hm.put("beacons", "hello");
        final Map<String, String> bidhm = new LinkedHashMap<String, String>();
        bidhm.put("bid", "hellobid");
        final Map<String, Map<String, String>> map = new LinkedHashMap<String, Map<String, String>>();
        map.put("beacons", hm);
        map.put("bid", bidhm);
        final StringBuilder actual = new StringBuilder();

        final ServletOutputStream outStream = new ServletOutputStream() {
            @Override
            public void write(int c) throws IOException {
                actual.append((char) c);
            }

            @Override
            public boolean isReady() {
                return false;
            }

            @Override
            public void setWriteListener(WriteListener writeListener) {

            }
        };

        expect(new Expectations() {
            {
                allowing(impression).getExternalID();
                will(returnValue("HelloTest123"));
                allowing(adComponents).getFormat();
                will(returnValue("Format123"));
                oneOf(response).getOutputStream();
                will(returnValue(outStream));
                allowing(adComponents).getComponents();
                will(returnValue(map));
                allowing(adComponents).getDestinationType();
                will(returnValue(DestinationType.IPHONE_APP_STORE));
                allowing(adComponents).getDestinationUrl();
                will(returnValue("AdfonicURL"));
            }
        });

        xmlAdView.renderAd(model, request, response, targetingContext, creative, impression, adComponents, false, BeaconsMode.metadata);
        String actualSting = actual.toString();

        String[] actualArray = actualSting.split("\\n");
        StringBuilder expected = new StringBuilder();
        expected.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        expected.append("\n");
        expected.append("<ad>");
        expected.append("\n");
        expected.append("<status>success</status>");
        expected.append("\n");
        expected.append("<adId>HelloTest123</adId>");
        expected.append("\n");
        expected.append("<format>Format123</format>");
        expected.append("\n");
        expected.append("<destination type=\"IPHONE_APP_STORE\" url=\"AdfonicURL\"/>");
        expected.append("\n");
        expected.append("<components>");
        expected.append("\n");
        expected.append("<component type=\"beacons\">");
        expected.append("\n");
        expected.append("<attribute name=\"beacons\">hello</attribute>");
        expected.append("\n");
        expected.append("</component>");
        expected.append("\n");
        expected.append("<component type=\"bid\">");
        expected.append("\n");
        expected.append("<attribute name=\"bid\">hellobid</attribute>");
        expected.append("\n");
        expected.append("</component>");
        expected.append("\n");
        expected.append("</components>");
        expected.append("\n");
        expected.append("</ad>");
        expected.append("\n");
        String expectedSting = expected.toString();
        String[] expectedArray = expectedSting.split("\\n");

        boolean result = true;

        for (int i = 0; i < expectedArray.length; i++) {
            if (!actualArray[i].trim().equals(expectedArray[i])) {
                result = false;
            }
        }
        assertTrue(result);
    }

    @Test
    public void testXmlAdView05_renderAd() throws Exception {
        final StringBuilder actual = new StringBuilder();

        final ServletOutputStream outStream = new ServletOutputStream() {
            @Override
            public void write(int c) throws IOException {
                actual.append((char) c);
            }

            @Override
            public boolean isReady() {
                return false;
            }

            @Override
            public void setWriteListener(WriteListener writeListener) {

            }
        };

        expect(new Expectations() {
            {
                allowing(impression).getExternalID();
                will(returnValue(null));
                allowing(adComponents).getFormat();
                will(returnValue("Format123"));
                oneOf(response).getOutputStream();
                will(returnValue(outStream));
                allowing(adComponents).getComponents();
                will(returnValue(null));
                allowing(adComponents).getDestinationType();
                will(returnValue(DestinationType.IPHONE_APP_STORE));
                allowing(adComponents).getDestinationUrl();
                will(returnValue("AdfonicURL"));
            }
        });

        xmlAdView.renderAd(model, request, response, targetingContext, creative, impression, adComponents, false, BeaconsMode.metadata);
        String actualSting = actual.toString();

        String[] actualArray = actualSting.split("\\n");
        StringBuilder expected = new StringBuilder();
        expected.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        expected.append("\n");
        expected.append("<ad>");
        expected.append("\n");
        expected.append("<status>success</status>");
        expected.append("\n");
        expected.append("<format>Format123</format>");
        expected.append("\n");
        expected.append("<destination type=\"IPHONE_APP_STORE\" url=\"AdfonicURL\"/>");
        expected.append("\n");
        expected.append("</ad>");
        expected.append("\n");
        String expectedSting = expected.toString();
        String[] expectedArray = expectedSting.split("\\n");

        boolean result = true;

        for (int i = 0; i < expectedArray.length; i++) {
            if (!actualArray[i].trim().equals(expectedArray[i])) {
                result = false;
            }
        }
        assertTrue(result);

    }

    @Test
    public void testXmlAdView06_renderError() throws Exception {
        final StringBuilder actual = new StringBuilder();
        final ServletOutputStream outStream = new ServletOutputStream() {
            @Override
            public void write(int c) throws IOException {
                actual.append((char) c);
            }

            @Override
            public boolean isReady() {
                return false;
            }

            @Override
            public void setWriteListener(WriteListener writeListener) {

            }
        };

        expect(new Expectations() {
            {
                oneOf(response).getOutputStream();
                will(returnValue(outStream));
            }
        });

        xmlAdView.renderError(model, request, response, targetingContext, "testerror");
        String actualSting = actual.toString();
        String[] actualArray = actualSting.split("\\n");

        StringBuilder expected = new StringBuilder();
        expected.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        expected.append("\n");
        expected.append("<ad>");
        expected.append("\n");
        expected.append("<status>error</status>");
        expected.append("\n");
        expected.append("<error>testerror</error>");
        expected.append("\n");
        expected.append("</ad>");
        expected.append("\n");

        boolean result = true;
        String expectedSting = expected.toString();
        String[] expectedArray = expectedSting.split("\\n");

        for (int i = 0; i < expectedArray.length; i++) {
            if (!actualArray[i].trim().equals(expectedArray[i])) {
                result = false;
            }
        }

        assertTrue(result);
    }

    @Test
    public void testXmlAdView07_renderError() throws Exception {
        final StringBuilder actual = new StringBuilder();
        final ServletOutputStream outStream = new ServletOutputStream() {
            @Override
            public void write(int c) throws IOException {
                actual.append((char) c);
            }

            @Override
            public boolean isReady() {
                return false;
            }

            @Override
            public void setWriteListener(WriteListener writeListener) {

            }
        };

        expect(new Expectations() {
            {
                oneOf(response).getOutputStream();
                will(returnValue(outStream));
            }
        });
        xmlAdView.renderError(model, request, response, targetingContext, null);
        String actualSting = actual.toString();
        String[] actualArray = actualSting.split("\\n");

        StringBuilder expected = new StringBuilder();
        expected.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        expected.append("\n");
        expected.append("<ad>");
        expected.append("\n");
        expected.append("<status>error</status>");
        expected.append("\n");
        expected.append("<error></error>");
        expected.append("\n");
        expected.append("</ad>");
        expected.append("\n");

        boolean result = true;
        String expectedSting = expected.toString();
        String[] expectedArray = expectedSting.split("\\n");

        for (int i = 0; i < expectedArray.length; i++) {
            if (!actualArray[i].trim().equals(expectedArray[i])) {
                result = false;
            }
        }

        assertTrue(result);

    }

    @Test
    public void testXmlAdView8_getContentType() throws Exception {
        assertTrue(xmlAdView.getContentType().equals("text/xml"));
    }

}
