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
import com.adfonic.adserver.Parameters;
import com.adfonic.adserver.TargetingContext;
import com.adfonic.adserver.view.AbstractAdView.BeaconsMode;
import com.adfonic.domain.DestinationType;
import com.adfonic.domain.cache.dto.adserver.creative.CreativeDto;

@SuppressWarnings("rawtypes")
public class TestUrlencodeAdView extends BaseAdserverTest {

    private HttpServletRequest request;
    private HttpServletResponse response;
    private UrlencodeAdView urlencodeAdView;

    private TargetingContext targetingContext;
    private CreativeDto creative;
    private Impression impression;
    private AdComponents adComponents;
    private MarkupGenerator markupGenerator;
    private Map model;

    @Before
    public void initTests() {
        urlencodeAdView = new UrlencodeAdView();
        model = mock(Map.class);
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        targetingContext = mock(TargetingContext.class);
        creative = mock(CreativeDto.class);
        impression = mock(Impression.class);
        adComponents = mock(AdComponents.class);
        markupGenerator = mock(MarkupGenerator.class);
        inject(urlencodeAdView, "markupGenerator", markupGenerator);
    }

    @Test
    public void testUrlencodeAdView01_renderAd() throws Exception {
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
                allowing(adComponents).getComponents();
                will(returnValue(map));
                oneOf(response).getWriter();
                will(returnValue(writer));
                oneOf(writer)
                        .append(with("status=success&adId=HelloTest123&format=Format123&adContent=HelloTest123Flase&components=beacons%2Cbid&component.beacons.beacons=hello&component.bid.bid=hellobid"));
                will(returnValue(writer));
                oneOf(targetingContext).getAdSpace();
                will(returnValue(null));
            }
        });

        urlencodeAdView.renderAd(model, request, response, targetingContext, creative, impression, adComponents, true, BeaconsMode.metadata);
        assertNotNull(writer);
    }

    @Test
    public void testUrlencodeAdView02_renderAd() throws Exception {
        final PrintWriter writer = mock(PrintWriter.class);
        final Map<String, String> bidhm = new LinkedHashMap<String, String>();
        bidhm.put("bid", "hellobid");
        final Map<String, Map<String, String>> map = new LinkedHashMap<String, Map<String, String>>();
        map.put("bid", bidhm);

        expect(new Expectations() {
            {
                oneOf(markupGenerator).generateMarkup(adComponents, targetingContext, null, creative, impression, true);
                will(returnValue("HelloTest123True"));
                allowing(impression).getExternalID();
                will(returnValue(null));
                allowing(adComponents).getFormat();
                will(returnValue("Format123"));
                allowing(adComponents).getComponents();
                will(returnValue(map));
                oneOf(response).getWriter();
                will(returnValue(writer));
                oneOf(writer).append(with("status=success&format=Format123&adContent=HelloTest123True&components=bid&component.bid.bid=hellobid"));
                will(returnValue(writer));
                oneOf(targetingContext).getAdSpace();
                will(returnValue(null));
            }
        });

        urlencodeAdView.renderAd(model, request, response, targetingContext, creative, impression, adComponents, true, BeaconsMode.markup);
        assertNotNull(writer);
    }

    @Test
    public void testUrlencodeAdView03_renderAd() throws Exception {
        final PrintWriter writer = mock(PrintWriter.class);
        final Map<String, Map<String, String>> map = new LinkedHashMap<String, Map<String, String>>();
        expect(new Expectations() {
            {
                oneOf(markupGenerator).generateMarkup(adComponents, targetingContext, null, creative, impression, true);
                will(returnValue("HelloTest123True"));
                allowing(impression).getExternalID();
                will(returnValue("HelloTest123"));
                allowing(adComponents).getFormat();
                will(returnValue("Format123"));
                allowing(adComponents).getComponents();
                will(returnValue(map));
                oneOf(response).getWriter();
                will(returnValue(writer));
                oneOf(writer).append(with("status=success&adId=HelloTest123&format=Format123&adContent=HelloTest123True"));
                will(returnValue(writer));
                oneOf(targetingContext).getAdSpace();
                will(returnValue(null));
            }
        });

        urlencodeAdView.renderAd(model, request, response, targetingContext, creative, impression, adComponents, true, BeaconsMode.markup);
        assertNotNull(writer);
    }

    @Test
    public void testUrlencodeAdView04_renderAd() throws Exception {
        final PrintWriter writer = mock(PrintWriter.class);
        final Map<String, String> bidhm = new LinkedHashMap<String, String>();
        bidhm.put("bid", "hellobid");
        final Map<String, Map<String, String>> map = new LinkedHashMap<String, Map<String, String>>();
        map.put("bid", bidhm);

        expect(new Expectations() {
            {
                allowing(impression).getExternalID();
                will(returnValue("HelloTest123"));
                oneOf(response).getWriter();
                will(returnValue(writer));
                oneOf(writer).append(with("status=success&adId=HelloTest123"));
                will(returnValue(writer));
            }
        });

        urlencodeAdView.renderAd(model, request, response, targetingContext, creative, impression, null, true, BeaconsMode.markup);
        assertNotNull(writer);
    }

    @Test
    public void testUrlencodeAdView05_renderAd() throws Exception {
        final PrintWriter writer = mock(PrintWriter.class);
        final Map<String, Map<String, String>> map = new LinkedHashMap<String, Map<String, String>>();

        expect(new Expectations() {
            {
                allowing(impression).getExternalID();
                will(returnValue("HelloTest123"));
                allowing(adComponents).getFormat();
                will(returnValue("Format123"));
                allowing(adComponents).getDestinationType();
                will(returnValue(DestinationType.IPHONE_APP_STORE));
                allowing(adComponents).getDestinationUrl();
                will(returnValue("AdfonicURL"));
                allowing(adComponents).getComponents();
                will(returnValue(map));
                oneOf(response).getWriter();
                will(returnValue(writer));
                oneOf(writer).append(with("status=success&adId=HelloTest123&format=Format123&destination.type=IPHONE_APP_STORE&destination.url=AdfonicURL&components="));
                will(returnValue(writer));
            }
        });

        urlencodeAdView.renderAd(model, request, response, targetingContext, creative, impression, adComponents, false, BeaconsMode.metadata);
        assertNotNull(writer);
    }

    @Test
    public void testUrlencodeAdView06_renderAd() throws Exception {
        final PrintWriter writer = mock(PrintWriter.class);
        final Map<String, Map<String, String>> map = new LinkedHashMap<String, Map<String, String>>();

        expect(new Expectations() {
            {
                allowing(impression).getExternalID();
                will(returnValue("HelloTest123"));
                allowing(adComponents).getFormat();
                will(returnValue("Format123"));
                allowing(adComponents).getDestinationType();
                will(returnValue(null));
                allowing(adComponents).getDestinationUrl();
                will(returnValue(null));
                allowing(adComponents).getComponents();
                will(returnValue(map));
                oneOf(response).getWriter();
                will(returnValue(writer));
                oneOf(writer).append(with("status=success&adId=HelloTest123&format=Format123&components="));
                will(returnValue(writer));
            }
        });

        urlencodeAdView.renderAd(model, request, response, targetingContext, creative, impression, adComponents, false, BeaconsMode.metadata);
        assertNotNull(writer);
    }

    @Test
    public void testUrlencodeAdView07_renderAd() throws Exception {
        final Map<String, Map<String, String>> map = new LinkedHashMap<String, Map<String, String>>();
        impression = null;

        final PrintWriter writer = mock(PrintWriter.class);

        expect(new Expectations() {
            {
                oneOf(markupGenerator).generateMarkup(adComponents, targetingContext, null, creative, impression, false);
                will(returnValue("HelloTest123Flase"));
                allowing(adComponents).getFormat();
                will(returnValue("Format123"));
                allowing(adComponents).getComponents();
                will(returnValue(map));
                oneOf(response).getWriter();
                will(returnValue(writer));
                oneOf(writer).append(with("status=success&format=Format123&adContent=HelloTest123Flase"));
                will(returnValue(writer));
                oneOf(targetingContext).getAdSpace();
                will(returnValue(null));
            }
        });

        urlencodeAdView.renderAd(model, request, response, targetingContext, creative, impression, adComponents, true, BeaconsMode.metadata);
        assertNotNull(writer);
    }

    @Test
    public void testUrlencodeAdView08_renderError() throws Exception {
        final PrintWriter writer = mock(PrintWriter.class);

        expect(new Expectations() {
            {
                oneOf(response).getWriter();
                will(returnValue(writer));
                oneOf(writer).append(with("status=error&error=testerror"));
                will(returnValue(writer));
            }
        });

        urlencodeAdView.renderError(model, request, response, targetingContext, "testerror");
        assertNotNull(writer);
    }

    @Test
    public void testUrlencodeAdView09_renderError() throws Exception {
        final PrintWriter writer = mock(PrintWriter.class);

        expect(new Expectations() {
            {
                oneOf(response).getWriter();
                will(returnValue(writer));
                oneOf(writer).append(with("status=error&error=No+ad+available"));
                will(returnValue(writer));
            }
        });

        urlencodeAdView.renderError(model, request, response, targetingContext, null);
        assertNotNull(writer);
    }

    @Test
    public void testUrlencodeAdView10_getContentType() throws Exception {
        assertTrue(urlencodeAdView.getContentType().equals("application/x-www-form-urlencoded"));
    }

    @Test
    public void testAbstractAdView11_render() throws Exception {

        final PrintWriter writer = mock(PrintWriter.class);

        expect(new Expectations() {
            {
                oneOf(model).get("error");
                will(returnValue("ErrorExist"));
                oneOf(model).get("adComponents");
                will(returnValue(null));
                oneOf(model).get("impression");
                will(returnValue(null));
                oneOf(model).get("targetingContext");
                will(returnValue(targetingContext));
                oneOf(response).getWriter();
                will(returnValue(writer));
                allowing(writer).append(with(any(String.class)));
                will(returnValue(writer));
            }
        });

        urlencodeAdView.render(model, request, response);
        assertNotNull(writer);
    }

    @Test
    public void testAbstractAdView12_render() throws Exception {
        final PrintWriter writer = mock(PrintWriter.class);

        expect(new Expectations() {
            {
                oneOf(model).get("error");
                will(returnValue(null));
                oneOf(model).get("adComponents");
                will(returnValue(adComponents));
                oneOf(model).get("impression");
                will(returnValue(null));
                oneOf(model).get("targetingContext");
                will(returnValue(targetingContext));
                oneOf(response).getWriter();
                will(returnValue(writer));
                allowing(writer).append(with(any(String.class)));
                will(returnValue(writer));
            }
        });

        urlencodeAdView.render(model, request, response);
        assertNotNull(writer);
    }

    @Test
    public void testAbstractAdView13_render() throws Exception {
        final PrintWriter writer = mock(PrintWriter.class);

        expect(new Expectations() {
            {
                oneOf(model).get("error");
                will(returnValue(null));
                oneOf(model).get("adComponents");
                will(returnValue(null));
                oneOf(model).get("impression");
                will(returnValue(impression));
                oneOf(model).get("targetingContext");
                will(returnValue(targetingContext));
                oneOf(response).getWriter();
                will(returnValue(writer));
                allowing(writer).append(with(any(String.class)));
                will(returnValue(writer));
            }
        });

        urlencodeAdView.render(model, request, response);
        assertNotNull(writer);
    }

    @Test
    public void testAbstractAdView14_render() throws Exception {
        final PrintWriter writer = mock(PrintWriter.class);
        final Map<String, Map<String, String>> map = new LinkedHashMap<String, Map<String, String>>();

        expect(new Expectations() {
            {
                oneOf(model).get("error");
                will(returnValue(null));
                oneOf(model).get("adComponents");
                will(returnValue(adComponents));
                oneOf(model).get("targetingContext");
                will(returnValue(targetingContext));
                oneOf(model).get("impression");
                will(returnValue(impression));
                oneOf(targetingContext).getAttribute(TargetingContext.MARKUP_AVAILABLE, Boolean.class);
                will(returnValue(false));
                oneOf(targetingContext).getAttribute(Parameters.BEACONS_MODE);
                will(returnValue("markup"));
                oneOf(model).get("creative");
                will(returnValue(null));
                oneOf(response).getWriter();
                will(returnValue(writer));
                allowing(impression).getExternalID();
                will(returnValue(null));
                oneOf(response).setContentType(with("application/x-www-form-urlencoded"));
                allowing(writer).append(with(any(String.class)));
                will(returnValue(writer));
                allowing(adComponents).getFormat();
                will(returnValue(null));
                allowing(adComponents).getComponents();
                will(returnValue(map));
                allowing(adComponents).getDestinationType();
                will(returnValue(DestinationType.IPHONE_APP_STORE));
                allowing(adComponents).getDestinationUrl();
                will(returnValue("AdfonicURL"));
            }
        });

        urlencodeAdView.render(model, request, response);
        assertNotNull(writer);
    }

    @Test
    public void testAbstractAdView15_render() throws Exception {
        final Map<String, Map<String, String>> map = new LinkedHashMap<String, Map<String, String>>();
        final PrintWriter writer = mock(PrintWriter.class);

        expect(new Expectations() {
            {
                oneOf(model).get("error");
                will(returnValue(null));
                oneOf(model).get("adComponents");
                will(returnValue(adComponents));
                oneOf(model).get("targetingContext");
                will(returnValue(targetingContext));
                oneOf(model).get("impression");
                will(returnValue(impression));
                oneOf(targetingContext).getAttribute(TargetingContext.MARKUP_AVAILABLE, Boolean.class);
                will(returnValue(true));
                oneOf(targetingContext).getAttribute(Parameters.BEACONS_MODE);
                will(returnValue("markup"));
                oneOf(model).get("creative");
                will(returnValue(null));
                oneOf(response).getWriter();
                will(returnValue(writer));
                allowing(impression).getExternalID();
                will(returnValue(null));
                oneOf(response).setContentType(with("application/x-www-form-urlencoded"));
                allowing(writer).append(with(any(String.class)));
                will(returnValue(writer));
                allowing(adComponents).getFormat();
                will(returnValue(null));
                allowing(adComponents).getComponents();
                will(returnValue(map));
                oneOf(markupGenerator).generateMarkup(adComponents, targetingContext, null, null, impression, true);
                will(returnValue("HelloTest123False"));
                oneOf(targetingContext).getAdSpace();
                will(returnValue(null));
            }
        });

        urlencodeAdView.render(model, request, response);
        assertNotNull(writer);
    }

    @Test
    public void testAbstractAdView16_render() throws Exception {
        final Map<String, Map<String, String>> map = new LinkedHashMap<String, Map<String, String>>();
        final PrintWriter writer = mock(PrintWriter.class);

        expect(new Expectations() {
            {
                oneOf(model).get("error");
                will(returnValue(null));
                oneOf(model).get("adComponents");
                will(returnValue(adComponents));
                oneOf(model).get("targetingContext");
                will(returnValue(targetingContext));
                oneOf(model).get("impression");
                will(returnValue(impression));
                oneOf(targetingContext).getAttribute(TargetingContext.MARKUP_AVAILABLE, Boolean.class);
                will(returnValue(true));
                oneOf(targetingContext).getAttribute(Parameters.BEACONS_MODE);
                will(returnValue(null));
                oneOf(model).get("creative");
                will(returnValue(null));
                oneOf(response).getWriter();
                will(returnValue(writer));
                allowing(impression).getExternalID();
                will(returnValue(null));
                oneOf(response).setContentType(with("application/x-www-form-urlencoded"));
                allowing(writer).append(with(any(String.class)));
                will(returnValue(writer));
                allowing(adComponents).getFormat();
                will(returnValue(null));
                allowing(adComponents).getComponents();
                will(returnValue(map));
                oneOf(markupGenerator).generateMarkup(adComponents, targetingContext, null, null, impression, true);
                will(returnValue("HelloTest123False"));
                oneOf(targetingContext).getAdSpace();
                will(returnValue(null));
            }
        });

        urlencodeAdView.render(model, request, response);
        assertNotNull(writer);
    }

    @Test
    public void testAbstractAdView17_render() throws Exception {
        final PrintWriter writer = mock(PrintWriter.class);

        expect(new Expectations() {
            {
                oneOf(model).get("error");
                will(returnValue(null));
                oneOf(model).get("adComponents");
                will(returnValue(adComponents));
                oneOf(model).get("targetingContext");
                will(returnValue(targetingContext));
                oneOf(model).get("impression");
                will(returnValue(impression));
                oneOf(targetingContext).getAttribute(TargetingContext.MARKUP_AVAILABLE, Boolean.class);
                will(returnValue(false));
                oneOf(targetingContext).getAttribute(Parameters.BEACONS_MODE);
                will(returnValue(null));
                oneOf(model).get("creative");
                will(returnValue(null));
                oneOf(response).getWriter();
                will(returnValue(writer));
                allowing(impression).getExternalID();
                will(returnValue(null));
                oneOf(response).setContentType(with("application/x-www-form-urlencoded"));
                allowing(writer).append(with(any(String.class)));
                will(returnValue(writer));
                allowing(adComponents).getFormat();
                will(returnValue(null));
                allowing(adComponents).getComponents();
                will(returnValue(null));
                allowing(adComponents).getDestinationType();
                will(returnValue(DestinationType.IPHONE_APP_STORE));
                allowing(adComponents).getDestinationUrl();
                will(returnValue("AdfonicURL"));
            }
        });

        urlencodeAdView.render(model, request, response);
        assertNotNull(writer);
    }

    @Test
    public void testAbstractAdView18_render() throws Exception {
        final PrintWriter writer = mock(PrintWriter.class);

        expect(new Expectations() {
            {
                oneOf(model).get("error");
                will(returnValue(null));
                oneOf(model).get("adComponents");
                will(returnValue(adComponents));
                oneOf(model).get("targetingContext");
                will(returnValue(targetingContext));
                oneOf(model).get("impression");
                will(returnValue(impression));
                oneOf(targetingContext).getAttribute(TargetingContext.MARKUP_AVAILABLE, Boolean.class);
                will(returnValue(true));
                oneOf(targetingContext).getAttribute(Parameters.BEACONS_MODE);
                will(returnValue("something"));
                oneOf(response).getWriter();
                will(returnValue(writer));
                allowing(writer).append(with(any(String.class)));
                will(returnValue(writer));
            }
        });

        urlencodeAdView.render(model, request, response);
        assertNotNull(writer);
    }

    @Test
    public void testAbstractAdView19_render() throws Exception {
        final PrintWriter writer = mock(PrintWriter.class);

        expect(new Expectations() {
            {
                oneOf(model).get("error");
                will(returnValue(null));
                oneOf(model).get("adComponents");
                will(returnValue(null));
                oneOf(model).get("impression");
                will(returnValue(null));
                oneOf(model).get("targetingContext");
                will(returnValue(targetingContext));
                oneOf(response).getWriter();
                will(returnValue(writer));
                allowing(writer).append(with(any(String.class)));
            }
        });

        urlencodeAdView.render(model, request, response);
    }

    @Test
    public void testAbstractAdView20_render() throws Exception {
        final Map<String, Map<String, String>> map = new LinkedHashMap<String, Map<String, String>>();
        final PrintWriter writer = mock(PrintWriter.class);

        expect(new Expectations() {
            {
                oneOf(model).get("error");
                will(returnValue(null));
                oneOf(model).get("adComponents");
                will(returnValue(adComponents));
                oneOf(model).get("targetingContext");
                will(returnValue(targetingContext));
                oneOf(model).get("impression");
                will(returnValue(impression));
                oneOf(targetingContext).getAttribute(TargetingContext.MARKUP_AVAILABLE, Boolean.class);
                will(returnValue(false));
                oneOf(targetingContext).getAttribute(Parameters.BEACONS_MODE);
                will(returnValue("metadata"));
                oneOf(model).get("creative");
                will(returnValue(null));
                oneOf(response).getWriter();
                will(returnValue(writer));
                oneOf(impression).getExternalID();
                will(returnValue(null));
                oneOf(response).setContentType(with("application/x-www-form-urlencoded"));
                oneOf(writer).append(with(any(String.class)));
                will(returnValue(writer));
                oneOf(adComponents).getFormat();
                will(returnValue(null));
                oneOf(adComponents).getComponents();
                will(returnValue(map));
                allowing(adComponents).getDestinationType();
                will(returnValue(DestinationType.IPHONE_APP_STORE));
                allowing(adComponents).getDestinationUrl();
                will(returnValue("AdfonicURL"));
            }
        });

        urlencodeAdView.render(model, request, response);
        assertNotNull(writer);
    }

}
