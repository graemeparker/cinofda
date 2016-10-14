package com.adfonic.adserver.view;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.PrintWriter;
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
import com.adfonic.domain.cache.dto.adserver.creative.CreativeDto;

@SuppressWarnings("rawtypes")
public class TestHtmlAdView extends BaseAdserverTest {

    private HttpServletRequest request;
    private HttpServletResponse response;
    private HtmlAdView htmlAdView;

    private TargetingContext targetingContext;
    private CreativeDto creative;
    private Impression impression;
    private AdComponents adComponents;
    private MarkupGenerator markupGenerator;
    private Map model;

    @Before
    public void initTests() {
        htmlAdView = new HtmlAdView();
        model = mock(Map.class);
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        targetingContext = mock(TargetingContext.class);
        creative = mock(CreativeDto.class);
        impression = mock(Impression.class);
        adComponents = mock(AdComponents.class);
        markupGenerator = mock(MarkupGenerator.class);
        inject(htmlAdView, "markupGenerator", markupGenerator);
    }

    @Test
    public void testHtmlAdView01_renderAd() throws Exception {

        final PrintWriter writer = mock(PrintWriter.class);

        expect(new Expectations() {
            {
                oneOf(markupGenerator).generateMarkup(adComponents, targetingContext, null, creative, impression, true);
                will(returnValue("HelloTest123Flase"));
                oneOf(response).getWriter();
                will(returnValue(writer));
                oneOf(writer).append(with(any(String.class)));
                will(returnValue(writer));
                oneOf(targetingContext).getAdSpace();
                will(returnValue(null));
            }
        });

        htmlAdView.renderAd(model, request, response, targetingContext, creative, impression, adComponents, true, null);
        assertNotNull(writer);
    }

    @Test
    public void testHtmlAdView02_renderError() throws Exception {
        final PrintWriter writer = mock(PrintWriter.class);

        expect(new Expectations() {
            {
                oneOf(response).getWriter();
                will(returnValue(writer));
                oneOf(writer).append(with("<!-- No ad available -->"));
                will(returnValue(writer));
            }
        });

        htmlAdView.renderError(model, request, response, targetingContext, "testerror");
        assertNotNull(writer);
    }

    @Test
    public void testJavascriptAdView3_getContentType() throws Exception {
        assertTrue(htmlAdView.getContentType().equals("text/html"));
    }
}
