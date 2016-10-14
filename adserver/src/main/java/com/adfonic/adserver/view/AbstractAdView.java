package com.adfonic.adserver.view;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.servlet.View;

import com.adfonic.adserver.AdComponents;
import com.adfonic.adserver.Impression;
import com.adfonic.adserver.MarkupGenerator;
import com.adfonic.adserver.Parameters;
import com.adfonic.adserver.TargetingContext;
import com.adfonic.adserver.impl.MarkupGeneratorImpl;
import com.adfonic.domain.cache.dto.adserver.creative.CreativeDto;

public abstract class AbstractAdView implements View {
    private static final transient Logger LOG = Logger.getLogger(AbstractAdView.class.getName());
    @Autowired
    @Qualifier(MarkupGeneratorImpl.BEAN_NAME)
    private MarkupGenerator markupGenerator;

    protected enum BeaconsMode { markup, metadata }
    
    public abstract String getContentType();

    protected final MarkupGenerator getMarkupGenerator() {
        return markupGenerator;
    }
    
    @SuppressWarnings("rawtypes")
	public void render(Map model, HttpServletRequest request, HttpServletResponse response) throws Exception {
        String error = (String)model.get("error");
        final AdComponents adComponents = (AdComponents)model.get("adComponents");
        final Impression impression = (Impression)model.get("impression");
        final TargetingContext context = (TargetingContext)model.get("targetingContext");
        
        if (error != null || impression == null || adComponents == null) {
            if (error == null) {
                error = "No ad available";
            }
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("Rendering error: " + error);
            }
            renderError(model, request, response, context, error);
            return;
        }

        // See if we need to provide marked-up content...
        boolean doMarkup = context.getAttribute(TargetingContext.MARKUP_AVAILABLE, Boolean.class);

        // Determine the BeaconsMode
        final String beaconsModeParam = context.getAttribute(Parameters.BEACONS_MODE);
        BeaconsMode beaconsMode;
        if (beaconsModeParam != null) {
            try {
                beaconsMode = BeaconsMode.valueOf(beaconsModeParam);
            } catch (Exception e) {
                renderError(model, request, response, context, "Invalid value for " + Parameters.BEACONS_MODE + ": " + beaconsModeParam);
                return;
            }
            // Make sure they didn't specify t.markup=0&t.beacons=markup
            if (beaconsMode == BeaconsMode.markup && !doMarkup) {
                // Originally I was going to have this error out, but the problem is that
                // by this point, we've already logged the impression and what not, and
                // in the case of a plugin-based creative, we've already incurred the
                // impression on the partner's side.  Let this slide, I guess, defaulting
                // to using markup mode...but at least log a warning.
                LOG.warning(Parameters.BEACONS_MODE + "=markup conflicts with " + Parameters.MARKUP + "=0, using BeaconsMode.metadata"); 
                beaconsMode = BeaconsMode.markup; 
            }
        } else {
            // Default the BeaconsMode to markup if not specified.
            beaconsMode = BeaconsMode.markup;
        }
        
        response.setContentType(getContentType());
        
        renderAd(model, request, response, context, (CreativeDto)model.get("creative"), impression, adComponents, doMarkup, beaconsMode);
    }
    
    @SuppressWarnings("rawtypes")
    protected abstract void renderAd(Map model, HttpServletRequest request, HttpServletResponse response, TargetingContext context, CreativeDto creative, Impression impression, AdComponents adComponents, boolean doMarkup, BeaconsMode beaconsMode) throws Exception;

    @SuppressWarnings("rawtypes")
    protected abstract void renderError(Map model, HttpServletRequest request, HttpServletResponse response, TargetingContext context, String error) throws Exception;
}
