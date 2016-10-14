package com.adfonic.adserver.view;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.validator.routines.UrlValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.View;

import com.adfonic.adserver.AdComponents;
import com.adfonic.adserver.Impression;
import com.adfonic.adserver.MarkupGenerator;
import com.adfonic.adserver.Parameters;
import com.adfonic.adserver.TargetingContext;
import com.adfonic.adserver.impl.MarkupGeneratorImpl;
import com.adfonic.domain.cache.dto.adserver.adspace.AdSpaceDto;
import com.adfonic.domain.cache.dto.adserver.creative.CreativeDto;

@Component
public class JavascriptAdView implements View {
    private static final transient Logger LOG = Logger.getLogger(JavascriptAdView.class.getName());
    @Autowired
    @Qualifier(MarkupGeneratorImpl.BEAN_NAME)
    private MarkupGenerator markupGenerator;

    @Override
    public String getContentType() {
        return "text/javascript";
    }

    private static final UrlValidator HTTPURL_VALIDATOR = new UrlValidator(new String[] { "http" });

    @Override
    @SuppressWarnings("rawtypes")
    public void render(Map model, HttpServletRequest request, HttpServletResponse response) throws Exception {
        String error = (String) model.get("error");
        final AdComponents adComponents = (AdComponents) model.get("adComponents");
        final String html;
        if (error != null || adComponents == null) {
            if (error == null) {
                String passbackURL = request.getParameter(Parameters.STATIC_PASSBACK_URL);
                if (HTTPURL_VALIDATOR.isValid(passbackURL)) {// UrlValidator handles null
                    response.sendRedirect(passbackURL);
                    return;
                }
                error = "No ad available";
            }
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("Rendering error: " + error);
            }
            html = "<!-- " + error + " -->";
        } else {
            TargetingContext context = (TargetingContext) model.get("targetingContext");
            CreativeDto creative = (CreativeDto) model.get("creative");
            Impression impression = (Impression) model.get("impression");
            // Pass true as the last arg so beacons get rendered in the HTML markup
            AdSpaceDto adSpace = context != null ? context.getAdSpace() : null;
            html = markupGenerator.generateMarkup(adComponents, context, adSpace, creative, impression, true);
        }

        // Escape single quotes, newlines and returns
        final String escapedHtml = html.replaceAll("'", "\\\\'").replaceAll("\n", "").replaceAll("\r", "");

        response.setContentType(getContentType());
        response.getWriter().append("document.write('").append(escapedHtml).append("');");
    }
}
