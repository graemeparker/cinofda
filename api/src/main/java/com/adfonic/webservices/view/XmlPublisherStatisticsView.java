package com.adfonic.webservices.view;

import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Component;

import com.adfonic.reporting.sql.dto.gen.Tagged;
import com.adfonic.util.XmlWriter;

@Component
public class XmlPublisherStatisticsView extends AbstractXmlView {
    @Override
    protected void renderXml(Map model, HttpServletRequest request, XmlWriter xml) {
        Object result = model.get("result");

        xml.startTag("masg-response").newLine();

        if (result instanceof Set<?>) { // overloading being static, need this anyway
            writeStatisticsForPublisher((Set<Tagged>) result, xml);
        } else {
            writeStatisticsForPublisher(result, xml);
        }

        xml.endTag(true); // masg-response
    }
}
