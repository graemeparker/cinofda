package com.adfonic.webservices.view;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Component;

import com.adfonic.reporting.Metric;
import com.adfonic.reporting.sql.dto.gen.Tagged;
import com.adfonic.util.XmlWriter;

@Component
public class XmlCampaignStatisticsView extends AbstractXmlView {
    @Override
    protected void renderXml(Map model, HttpServletRequest request, XmlWriter xml) {
        Object result = model.get("result");
        List<Metric> metrics = (List<Metric>)model.get("metrics");
        
        xml.startTag("masg-response").newLine();
        if (result instanceof Collection<?>) {
            writeStatisticsForCampaign((Collection<Tagged>) result, xml, metrics);
        } else {
            writeStatisticsForCampaign(result, xml, metrics);
        }

        xml.endTag(true); // masg-response
    }
}
