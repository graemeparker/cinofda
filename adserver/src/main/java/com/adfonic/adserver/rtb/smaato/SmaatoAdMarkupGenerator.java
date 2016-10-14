package com.adfonic.adserver.rtb.smaato;

import java.io.IOException;
import java.io.StringWriter;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.namespace.NamespaceContext;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.commons.lang.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.adfonic.adserver.AdComponents;
import com.adfonic.adserver.Impression;
import com.adfonic.adserver.MarkupGenerator;
import com.adfonic.adserver.SystemName;
import com.adfonic.adserver.TargetingContext;
import com.adfonic.adserver.impl.MarkupGeneratorImpl;
import com.adfonic.domain.cache.dto.adserver.FormatDto;
import com.adfonic.domain.cache.dto.adserver.adspace.AdSpaceDto;
import com.adfonic.domain.cache.dto.adserver.creative.CreativeDto;
import com.byyd.smaato.Ad;
import com.byyd.smaato.Beacons;
import com.byyd.smaato.ImageAd;
import com.byyd.smaato.RichmediaAd;
import com.byyd.smaato.TextAd;

/**
 * 
 * Feel free to examine exhaustive Smaato documentation on this topic - http://dspportal.smaato.com/documentation#adMarkup
 * 
 * @author mvanek
 *
 */
@Component(SmaatoAdMarkupGenerator.BEAN_NAME)
public class SmaatoAdMarkupGenerator implements MarkupGenerator {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    public static final String BEAN_NAME = "SMAATO_ADM";//AdmProfile.SMAATO_ADM.name();

    private final MarkupGenerator standardMarkupGenerator;
    private final XMLOutputFactory staxOutputFactory = XMLOutputFactory.newInstance();
    private final JAXBContext jaxbContext;

    @Autowired
    public SmaatoAdMarkupGenerator(@Qualifier(MarkupGeneratorImpl.BEAN_NAME) MarkupGenerator standardMarkupGenerator) {
        this.standardMarkupGenerator = standardMarkupGenerator;
        try {
            jaxbContext = JAXBContext.newInstance(Ad.class);
        } catch (JAXBException jaxbx) {
            throw new IllegalStateException("Failed to create JAXB context for " + Ad.class, jaxbx);
        }
    }

    @Override
    public String generateMarkup(AdComponents adComponents, TargetingContext context, AdSpaceDto adSpace, CreativeDto creative, Impression impression, boolean renderBeacons)
            throws IOException {

        List<String> beaconList = getBeaconList(adComponents.getComponents().get("beacons"));
        Beacons beacons = new Beacons();
        beacons.getBeacon().addAll(beaconList); // Smaato keeps beacons separate from markup itself
        Ad smaatoAd = new Ad();
        smaatoAd.setModelVersion("0.9");
        if (creative.getExtendedCreativeTypeId() != null) {
            // First build normal markup nut without beacons
            String htmlContent = standardMarkupGenerator.generateMarkup(adComponents, context, adSpace, creative, impression, false);
            RichmediaAd richAd = new RichmediaAd();
            // Put html in CDATA section in Smaato xml wrapper
            richAd.setContent(SmaatoXMLStreamWriter.CDATA_START + htmlContent + SmaatoXMLStreamWriter.CDATA_END);
            richAd.setBeacons(beacons);
            smaatoAd.setRichmediaAd(richAd);
        } else {
            FormatDto format = context.getDomainCache().getFormatById(creative.getFormatId());
            if (SystemName.FORMAT_TEXT.equals(format.getSystemName())) {
                TextAd textAd = new TextAd();
                Map<String, String> textComponent = adComponents.getComponents().get(SystemName.COMPONENT_TEXT);
                if (textComponent != null) {
                    textAd.setClickText(StringEscapeUtils.escapeXml(textComponent.get("content")));
                } else {
                    logger.warn("No text component for creative " + creative.getId());
                }
                textAd.setBeacons(beacons);
                textAd.setClickUrl(adComponents.getDestinationUrl());
                smaatoAd.setTextAd(textAd);
            } else {
                // Must be image ad then...
                ImageAd imageAd = new ImageAd();
                Map<String, String> imageComponent = adComponents.getComponents().get(SystemName.COMPONENT_IMAGE);
                if (imageComponent != null) {
                    imageAd.setImgUrl(imageComponent.get("url"));
                    imageAd.setWidth(new BigInteger(imageComponent.get("width")));
                    imageAd.setHeight(new BigInteger(imageComponent.get("height")));
                } else {
                    logger.warn("No image component for creative " + creative.getId());
                }
                imageAd.setBeacons(beacons);
                imageAd.setClickUrl(adComponents.getDestinationUrl());
                smaatoAd.setImageAd(imageAd);
            }
        }
        // JAXB marshalling. Automaticaly preserves CDATA and does XML escaping of '&<>'
        StringWriter writer = new StringWriter();
        try {
            Marshaller marshaller = jaxbContext.createMarshaller();
            SmaatoXMLStreamWriter smaatoStreamWriter = new SmaatoXMLStreamWriter(staxOutputFactory.createXMLStreamWriter(writer));
            marshaller.marshal(smaatoAd, smaatoStreamWriter);
            return writer.toString();
        } catch (JAXBException | XMLStreamException x) {
            throw new IllegalStateException("Failed to marshall Smaato markup for creative " + creative.getId(), x);
        }

    }

    private List<String> getBeaconList(Map<String, String> beaconsComponent) {
        if (beaconsComponent != null) {
            String numBeacons = beaconsComponent.get("numBeacons");
            int cntBeacon = numBeacons == null ? 0 : Integer.parseInt(numBeacons);
            List<String> beacons = new ArrayList<String>(cntBeacon);
            while (cntBeacon > 0) {
                String beaconKey = "beacon" + cntBeacon--;
                beacons.add(beaconsComponent.get(beaconKey));
            }
            return beacons;
        } else {
            return Collections.EMPTY_LIST;
        }
    }

    /*
        @Override
        protected Template getTemplate(AdComponents adComponents, TargetingContext context, CreativeDto creative, Impression impression) {
            escapeBeaconsForXmlGen(adComponents);

            if (creative != null && creative.getExtendedCreativeTypeId() != null) {
                String content;
                try {
                    content = standardMarkupGenerator.generateMarkup(adComponents, context, creative, impression, false);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                adComponents.setFormat(content);// TODO - rm this bad hack - to minimise impact for this release
                return getHtmlTemplate(null, null, null);
            }

            return super.getTemplate(adComponents, context, creative, impression);// a waste to go up - just to get some negative checks up there
        }

        @Override
        protected Template getHtmlTemplate(String formatSystemName, String displayTypeSystemName, Dimension templateSize) {
            if (formatSystemName == null) {
                formatSystemName = "richmedia";
            } else if (!formatSystemName.equals("text")) {
                formatSystemName = "image";
            }

            StringBuilder bld = new StringBuilder();
            bld.append("adm-profiles/smaato-rtb/").append(formatSystemName);

            bld.append(".vtl");
            String templateName = bld.toString();

            return getAndCacheTemplate(templateName);
        }

        private void escapeBeaconsForXmlGen(AdComponents adComponents) {
            Map<String, String> beaconsComponent = adComponents.getComponents().get("beacons");

            if (beaconsComponent != null) { // null checks not necessary, but with the kind of usage seen, let it be
                String numBeacons = beaconsComponent.get("numBeacons");
                int beaconNo = numBeacons == null ? 0 : Integer.parseInt(numBeacons);
                while (beaconNo > 0) {
                    String beaconKey = "beacon" + beaconNo--;
                    beaconsComponent.put(beaconKey, StringEscapeUtils.escapeXml(beaconsComponent.get(beaconKey)));
                }
            }

        }
    */

    /**
     * Marshall element content as CDATA (writeCData) if input string uses it, but xml escape everything else (default behaviour of writeCharacters)
     *
     */
    static class SmaatoXMLStreamWriter implements XMLStreamWriter {

        public static final String CDATA_START = "<![CDATA[";
        public static final String CDATA_END = "]]>";

        private final XMLStreamWriter delegate;

        public SmaatoXMLStreamWriter(XMLStreamWriter delegate) {
            this.delegate = delegate;
        }

        @Override
        public void writeCharacters(String text) throws XMLStreamException {
            // trim usual leading newline
            String trtext = text.trim();
            if (trtext.startsWith(CDATA_START)) {
                if (!text.endsWith(CDATA_END)) {
                    throw new IllegalArgumentException("'<![CDATA[' started but not terminated by ']]>' : " + text);
                }
                // If CDATA wrapper is passed, we cannot write it using writeCharacters because it would be escaped by internal xml writer
                // Original wrapped text must be cut out and written using writeCData instead
                String original = trtext.substring(9, trtext.length() - 3);
                delegate.writeCData(original);
                return;
            } else {
                delegate.writeCharacters(text);
            }
        }

        @Override
        public void writeStartElement(String localName) throws XMLStreamException {
            delegate.writeStartElement(localName);
        }

        @Override
        public void writeStartElement(String namespaceURI, String localName) throws XMLStreamException {
            delegate.writeStartElement(namespaceURI, localName);
        }

        @Override
        public void writeStartElement(String prefix, String localName, String namespaceURI) throws XMLStreamException {
            delegate.writeStartElement(prefix, localName, namespaceURI);
        }

        @Override
        public void writeEmptyElement(String namespaceURI, String localName) throws XMLStreamException {
            delegate.writeEmptyElement(namespaceURI, localName);
        }

        @Override
        public void writeEmptyElement(String prefix, String localName, String namespaceURI) throws XMLStreamException {
            delegate.writeEmptyElement(prefix, localName, namespaceURI);
        }

        @Override
        public void writeEmptyElement(String localName) throws XMLStreamException {
            delegate.writeEmptyElement(localName);
        }

        @Override
        public void writeEndElement() throws XMLStreamException {
            delegate.writeEndElement();
        }

        @Override
        public void writeEndDocument() throws XMLStreamException {
            delegate.writeEndDocument();
        }

        @Override
        public void close() throws XMLStreamException {
            delegate.close();
        }

        @Override
        public void flush() throws XMLStreamException {
            delegate.flush();
        }

        @Override
        public void writeAttribute(String localName, String value) throws XMLStreamException {
            delegate.writeAttribute(localName, value);
        }

        @Override
        public void writeAttribute(String prefix, String namespaceURI, String localName, String value) throws XMLStreamException {
            delegate.writeAttribute(prefix, namespaceURI, localName, value);
        }

        @Override
        public void writeAttribute(String namespaceURI, String localName, String value) throws XMLStreamException {
            delegate.writeAttribute(namespaceURI, localName, value);
        }

        @Override
        public void writeNamespace(String prefix, String namespaceURI) throws XMLStreamException {
            delegate.writeNamespace(prefix, namespaceURI);
        }

        @Override
        public void writeDefaultNamespace(String namespaceURI) throws XMLStreamException {
            delegate.writeDefaultNamespace(namespaceURI);
        }

        @Override
        public void writeComment(String data) throws XMLStreamException {
            delegate.writeComment(data);
        }

        @Override
        public void writeProcessingInstruction(String target) throws XMLStreamException {
            delegate.writeProcessingInstruction(target);
        }

        @Override
        public void writeProcessingInstruction(String target, String data) throws XMLStreamException {
            delegate.writeProcessingInstruction(target, data);
        }

        @Override
        public void writeCData(String data) throws XMLStreamException {
            delegate.writeCData(data);
        }

        @Override
        public void writeDTD(String dtd) throws XMLStreamException {
            delegate.writeDTD(dtd);
        }

        @Override
        public void writeEntityRef(String name) throws XMLStreamException {
            delegate.writeEntityRef(name);
        }

        @Override
        public void writeStartDocument() throws XMLStreamException {
            delegate.writeStartDocument();
        }

        @Override
        public void writeStartDocument(String version) throws XMLStreamException {
            delegate.writeStartDocument(version);
        }

        @Override
        public void writeStartDocument(String encoding, String version) throws XMLStreamException {
            delegate.writeStartDocument(encoding, version);
        }

        @Override
        public void writeCharacters(char[] text, int start, int len) throws XMLStreamException {
            delegate.writeCharacters(text, start, len);
        }

        @Override
        public String getPrefix(String uri) throws XMLStreamException {
            return delegate.getPrefix(uri);
        }

        @Override
        public void setPrefix(String prefix, String uri) throws XMLStreamException {
            delegate.setPrefix(prefix, uri);
        }

        @Override
        public void setDefaultNamespace(String uri) throws XMLStreamException {
            delegate.setDefaultNamespace(uri);
        }

        @Override
        public void setNamespaceContext(NamespaceContext context) throws XMLStreamException {
            delegate.setNamespaceContext(context);
        }

        @Override
        public NamespaceContext getNamespaceContext() {
            return delegate.getNamespaceContext();
        }

        @Override
        public Object getProperty(String name) throws IllegalArgumentException {
            return delegate.getProperty(name);
        }
    }
}
