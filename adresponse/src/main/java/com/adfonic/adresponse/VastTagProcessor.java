package com.adfonic.adresponse;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.runtime.RuntimeSingleton;
import org.apache.velocity.runtime.parser.ParseException;
import org.apache.velocity.runtime.parser.node.SimpleNode;

import com.adfonic.adserver.Constant;
import com.adfonic.adserver.Impression;
import com.adfonic.adserver.MarkupGenerator;
import com.adfonic.adserver.TargetingContext;
import com.adfonic.domain.ContentForm;
import com.adfonic.domain.cache.dto.adserver.adspace.AdSpaceDto;
import com.adfonic.domain.cache.dto.adserver.creative.CreativeDto;
import com.adfonic.util.HttpUtils;
import com.adfonic.util.VastWorker;
import com.byyd.vast2.VAST;

/**
 * 
 * @author mvanek
 *
 */
public class VastTagProcessor {

    private static VastWorker worker = VastWorker.instance();

    public static String buildVastInLine(CreativeDto creative, AdSpaceDto adSpace, TargetingContext context, Impression impression, String byydClickRedirectUrl,
            List<String> imppressionTrackers) {
        try {
            String vastXml = creative.getExtendedCreativeTemplates().get(ContentForm.VAST_2_0);

            // VAST like all 3rd party tags may contain velocity macros. For example cachebuster ${macro.timestamp}
            Map<String, Object> properties = new HashMap<String, Object>();
            properties.put("macros", MarkupGenerator.getVelocityMacroProps(context, adSpace, creative, impression));
            vastXml = processVelocityMacros(vastXml, creative.getExternalID(), properties);

            VAST vast = worker.read(new StringReader(vastXml));

            // Impression trackers are directly supported in VAST
            if (imppressionTrackers != null) {
                for (String impTrackerUrl : imppressionTrackers) {
                    impTrackerUrl = MarkupGenerator.resolveMacros(impTrackerUrl, context, adSpace, creative, impression);
                    worker.addImpressionTracker(vast, impTrackerUrl);
                }
            }
            /*
             * Tracking clicks in VAST is hard and in some cases even impossible
             * Video creative clicks
             *  - can be tracked either using VAST ClickTracking support or using good old click redirect
             * Companion creative clicks
             *  - VAST 2.0 has no support for Companion ClickTracking, so click redirect is only way to do it
             *  - VAST 3.0 has Companion ClickTracking but only in Wrapper
             * 
             * But all is just theory as SDKs usually do not support companion tracking  
             * For exmaple Mopub says:
             * "We will not use the <CompanionClickThrough> element for HTML and iframe companion banners, and will expect to place the clickthrough destination inside of the iframe or HTML."
             * So actually only Static companion will use CompanionClickThrough
             * Theoretically we can do best effort to parse HTML of HTML companion and manipulate the links there...but that is dangerous hack to do
             * 
             * Anyway, we will prefix all click links in VAST with our click redirect url - old good click redirect way
             */
            if (byydClickRedirectUrl != null) {
                worker.setClickThroughUrlPrefix(vast, new Function<String, String>() {

                    @Override
                    public String apply(String vastClickUrl) {
                        return byydClickRedirectUrl + "?" + Constant.CLICK_REDIRECT_URL_PARAM + "="
                                + HttpUtils.urlEncode(MarkupGenerator.resolveMacros(vastClickUrl, context, adSpace, creative, impression));
                    }
                });
            }
            // Marshall it back to string
            StringWriter stringWriter = new StringWriter();
            worker.write(vast, stringWriter);
            return stringWriter.toString();
        } catch (Exception x) {
            throw new IllegalStateException("VAST markup processing failed for creative: " + creative.getId(), x);
        }
    }

    /**
     * Urls inside of VAST can contain macros/placeholders 
     * Common is ${macros.timestamp} macro preprocessed in Tools2 (ExtendedCreativeManagerJpaImpl.processExtendedCreativeTemplateContent)
     * from original %timestamp% placeholder in 3rd party tags
     */
    private static String processVelocityMacros(String vastTag, String creativeExternalId, Map<String, ?> macroProps) {
        RuntimeServices runtimeServices = RuntimeSingleton.getRuntimeServices();
        StringReader reader = new StringReader(vastTag);
        SimpleNode node = null;
        try {
            node = runtimeServices.parse(reader, creativeExternalId);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        // XXX - We can cache templates and purge cache on adserver cache reload
        Template template = new Template();
        template.setRuntimeServices(runtimeServices);
        template.setData(node);
        template.setName(creativeExternalId);
        template.initDocument();

        StringWriter swri = new StringWriter();
        template.merge(new VelocityContext(macroProps), swri);
        return swri.toString();
    }

    public static String buildVastWrapper(String vastAdTagUri, String impressionId, List<String> impressionTrackerUrls) {
        VAST vast = new VAST();
        vast.setVersion("2.0");

        VAST.Ad.Wrapper wrapper = new VAST.Ad.Wrapper();
        wrapper.setVASTAdTagURI(vastAdTagUri);

        if (impressionTrackerUrls != null) {
            wrapper.getImpression().addAll(impressionTrackerUrls);
        }

        VAST.Ad ad = new VAST.Ad();
        ad.setWrapper(wrapper);
        ad.setId(impressionId);

        List<VAST.Ad> adList = vast.getAd();
        adList.add(ad);
        StringWriter sw = new StringWriter();
        worker.write(vast, sw);
        return sw.toString();
    }
}
