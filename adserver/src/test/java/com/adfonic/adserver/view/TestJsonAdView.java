package com.adfonic.adserver.view;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.PrintWriter;
import java.util.LinkedHashMap;
import java.util.Map;

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
public class TestJsonAdView extends BaseAdserverTest {

    private JsonAdView jsonAdView;
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
        jsonAdView = new JsonAdView();
        model = mock(Map.class);
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        targetingContext = mock(TargetingContext.class);
        creative = mock(CreativeDto.class);
        impression = mock(Impression.class);
        adComponents = mock(AdComponents.class);
        markupGenerator = mock(MarkupGenerator.class);
        inject(jsonAdView, "markupGenerator", markupGenerator);
    }

    @Test
    public void testJsonAdView01_renderAd() throws Exception {
        //TODO: Implementation of JSON objects comparison from the response stream..
        final Map<String, String> hm = new LinkedHashMap<String, String>();
        hm.put("beacons", "hello");
        final Map<String, String> bidhm = new LinkedHashMap<String, String>();
        bidhm.put("bid", "hellobid");
        final Map<String, Map<String, String>> map = new LinkedHashMap<String, Map<String, String>>();
        map.put("beacons", hm);
        map.put("bid", bidhm);

        final PrintWriter writer = mock(PrintWriter.class);

        expect(new Expectations() {
            {
                oneOf(markupGenerator).generateMarkup(adComponents, targetingContext, null, creative, impression, false);
                will(returnValue("HelloTest123Flase"));
                allowing(impression).getExternalID();
                will(returnValue("HelloTest123"));
                allowing(adComponents).getFormat();
                will(returnValue("Format123"));
                oneOf(response).getWriter();
                will(returnValue(writer));
                allowing(writer).write(with(any(Integer.class)));
                allowing(writer).write(with(any(String.class)));
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

        jsonAdView.renderAd(model, request, response, targetingContext, creative, impression, adComponents, true, BeaconsMode.metadata);
        assertNotNull(writer);
    }

    @Test
    public void testJsonAdView02_renderAd() throws Exception {
        final Map<String, String> bidhm = new LinkedHashMap<String, String>();
        bidhm.put("bid", "hellobid");
        final Map<String, Map<String, String>> map = new LinkedHashMap<String, Map<String, String>>();
        map.put("bid", bidhm);

        final PrintWriter writer = mock(PrintWriter.class);

        expect(new Expectations() {
            {
                oneOf(markupGenerator).generateMarkup(adComponents, targetingContext, null, creative, impression, true);
                will(returnValue("HelloTest123Flase"));
                allowing(impression).getExternalID();
                will(returnValue("HelloTest123"));
                allowing(adComponents).getFormat();
                will(returnValue("Format123"));
                oneOf(response).getWriter();
                will(returnValue(writer));
                allowing(writer).write(with(any(Integer.class)));
                allowing(writer).write(with(any(String.class)));
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

        jsonAdView.renderAd(model, request, response, targetingContext, creative, impression, adComponents, true, BeaconsMode.markup);
        assertNotNull(writer);
    }

    @Test
    public void testJsonAdView03_renderAd() throws Exception {
        final Map<String, Map<String, String>> map = new LinkedHashMap<String, Map<String, String>>();

        final PrintWriter writer = mock(PrintWriter.class);

        expect(new Expectations() {
            {
                oneOf(markupGenerator).generateMarkup(adComponents, targetingContext, null, creative, impression, false);
                will(returnValue("HelloTest123Flase"));
                allowing(impression).getExternalID();
                will(returnValue("HelloTest123"));
                allowing(adComponents).getFormat();
                will(returnValue("Format123"));
                oneOf(response).getWriter();
                will(returnValue(writer));
                allowing(writer).write(with(any(Integer.class)));
                allowing(writer).write(with(any(String.class)));
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

        jsonAdView.renderAd(model, request, response, targetingContext, creative, impression, adComponents, true, BeaconsMode.metadata);
        assertNotNull(writer);
    }

    @Test
    public void testJsonAdView04_renderAd() throws Exception {
        final Map<String, String> hm = new LinkedHashMap<String, String>();
        hm.put("beacons", "hello");
        final Map<String, String> bidhm = new LinkedHashMap<String, String>();
        bidhm.put("bid", "hellobid");
        final Map<String, Map<String, String>> map = new LinkedHashMap<String, Map<String, String>>();
        map.put("beacons", hm);
        map.put("bid", bidhm);
        final PrintWriter writer = mock(PrintWriter.class);

        expect(new Expectations() {
            {
                allowing(impression).getExternalID();
                will(returnValue("HelloTest123"));
                allowing(adComponents).getFormat();
                will(returnValue("Format123"));
                allowing(writer).write(with(any(Integer.class)));
                allowing(writer).write(with(any(String.class)));
                oneOf(response).getWriter();
                will(returnValue(writer));
                allowing(adComponents).getComponents();
                will(returnValue(map));
                allowing(adComponents).getDestinationType();
                will(returnValue(DestinationType.IPHONE_APP_STORE));
                allowing(adComponents).getDestinationUrl();
                will(returnValue("AdfonicURL"));
            }
        });

        jsonAdView.renderAd(model, request, response, targetingContext, creative, impression, adComponents, false, BeaconsMode.metadata);
        assertNotNull(writer);
    }

    @Test
    public void testJsonAdView05_renderAd() throws Exception {
        final PrintWriter writer = mock(PrintWriter.class);

        expect(new Expectations() {
            {
                allowing(impression).getExternalID();
                will(returnValue(null));
                allowing(adComponents).getFormat();
                will(returnValue("Format123"));
                allowing(writer).write(with(any(Integer.class)));
                allowing(writer).write(with(any(String.class)));
                oneOf(response).getWriter();
                will(returnValue(writer));
                allowing(adComponents).getComponents();
                will(returnValue(null));
                allowing(adComponents).getDestinationType();
                will(returnValue(DestinationType.IPHONE_APP_STORE));
                allowing(adComponents).getDestinationUrl();
                will(returnValue("AdfonicURL"));
            }
        });

        jsonAdView.renderAd(model, request, response, targetingContext, creative, impression, adComponents, false, BeaconsMode.metadata);
        assertNotNull(writer);
    }

    @Test
    public void testJsonAdView06_renderError() throws Exception {
        final PrintWriter writer = mock(PrintWriter.class);

        expect(new Expectations() {
            {
                oneOf(response).getWriter();
                will(returnValue(writer));
                allowing(writer).write(with(any(Integer.class)));
                allowing(writer).write(with(any(String.class)));
            }
        });

        jsonAdView.renderError(model, request, response, targetingContext, "testerror");
        assertNotNull(writer);
    }

    @Test
    public void testJsonAdView07_renderError() throws Exception {
        final PrintWriter writer = mock(PrintWriter.class);

        expect(new Expectations() {
            {
                oneOf(response).getWriter();
                will(returnValue(writer));
                allowing(writer).write(with(any(Integer.class)));
                allowing(writer).write(with(any(String.class)));
            }
        });

        jsonAdView.renderError(model, request, response, targetingContext, null);
        assertNotNull(writer);
    }

    @Test
    public void testJsonAdView8_getContentType() throws Exception {
        assertTrue(jsonAdView.getContentType().equals("application/json"));
    }

}
