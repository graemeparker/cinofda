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
import com.adfonic.adserver.MarkupGenerator;

@SuppressWarnings("rawtypes")
public class TestJavascriptAdView extends BaseAdserverTest {

    private JavascriptAdView javascriptAdView;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private MarkupGenerator markupGenerator;
    private Map model;

    @Before
    public void initTests() {
        javascriptAdView = new JavascriptAdView();
        model = mock(Map.class);
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        markupGenerator = mock(MarkupGenerator.class);
        inject(javascriptAdView, "markupGenerator", markupGenerator);
    }

    @Test
    public void testJavascriptAdView01_render() throws Exception {

        final PrintWriter writer = mock(PrintWriter.class);

        expect(new Expectations() {
            {
                oneOf(model).get("error");
                will(returnValue("ErrorExist"));
                oneOf(model).get("adComponents");
                will(returnValue(null));
                oneOf(response).getWriter();
                will(returnValue(writer));
                oneOf(response).setContentType(with("text/javascript"));
                allowing(writer).write(with(any(String.class)));
                allowing(writer).append(with(any(String.class)));
                will(returnValue(writer));
            }
        });

        javascriptAdView.render(model, request, response);
        assertNotNull(writer);
    }

    @Test
    public void testJavascriptAdView02_render() throws Exception {
        final Map<String, String> hm = new LinkedHashMap<String, String>();
        hm.put("beacons", "hello");
        final Map<String, String> bidhm = new LinkedHashMap<String, String>();
        bidhm.put("bid", "hellobid");
        final Map<String, Map<String, String>> map = new LinkedHashMap<String, Map<String, String>>();
        map.put("beacons", hm);
        map.put("bid", bidhm);

        final PrintWriter writer = mock(PrintWriter.class);
        final AdComponents adComponents = mock(AdComponents.class);

        expect(new Expectations() {
            {
                oneOf(model).get("error");
                will(returnValue(null));
                oneOf(model).get("adComponents");
                will(returnValue(adComponents));
                oneOf(model).get("targetingContext");
                will(returnValue(null));
                oneOf(model).get("impression");
                will(returnValue(null));
                oneOf(model).get("creative");
                will(returnValue(null));
                oneOf(response).getWriter();
                will(returnValue(writer));
                oneOf(response).setContentType(with("text/javascript"));
                allowing(writer).write(with(any(String.class)));
                allowing(adComponents).getDestinationUrl();
                will(returnValue("AdfonicURL"));
                oneOf(writer).append(with("document.write('"));
                will(returnValue(writer));
                oneOf(writer).append(with("HelloTest123True"));
                will(returnValue(writer));
                oneOf(writer).append(with("');"));
                will(returnValue(writer));
                oneOf(markupGenerator).generateMarkup(adComponents, null, null, null, null, true);
                will(returnValue("HelloTest123True"));

            }
        });

        javascriptAdView.render(model, request, response);
        assertNotNull(writer);
    }

    @Test
    public void testJavascriptAdView03_render() throws Exception {
        final PrintWriter writer = mock(PrintWriter.class);

        expect(new Expectations() {
            {
                oneOf(model).get("error");
                will(returnValue(null));
                oneOf(model).get("adComponents");
                will(returnValue(null));
                oneOf(response).getWriter();
                will(returnValue(writer));
                oneOf(response).setContentType(with("text/javascript"));
                allowing(request).getParameter("r.passback");
                will(returnValue(null));
                allowing(writer).write(with(any(String.class)));
                oneOf(writer).append(with("document.write('"));
                will(returnValue(writer));
                oneOf(writer).append(with("<!-- No ad available -->"));
                will(returnValue(writer));
                oneOf(writer).append(with("');"));
                will(returnValue(writer));
            }
        });

        javascriptAdView.render(model, request, response);
    }

    @Test
    public void testJavascriptAdView4_getContentType() throws Exception {
        assertTrue(javascriptAdView.getContentType().equals("text/javascript"));
    }
}
