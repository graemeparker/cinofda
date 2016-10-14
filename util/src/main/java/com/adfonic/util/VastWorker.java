package com.adfonic.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Pattern;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.ValidationEvent;
import javax.xml.bind.ValidationEventHandler;
import javax.xml.namespace.NamespaceContext;
import javax.xml.stream.EventFilter;
import javax.xml.stream.StreamFilter;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.XMLEvent;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.xml.sax.SAXException;

import com.byyd.vast2.CompanionType;
import com.byyd.vast2.ImpressionType;
import com.byyd.vast2.NonLinearType;
import com.byyd.vast2.TrackingEventsType;
import com.byyd.vast2.VAST;
import com.byyd.vast2.VAST.Ad.InLine;
import com.byyd.vast2.VAST.Ad.InLine.Creatives.Creative.Linear;
import com.byyd.vast2.VAST.Ad.Wrapper;
import com.byyd.vast2.VideoClicksType;
import com.byyd.vast2.VideoClicksType.ClickThrough;

/**
 * 
 * @author mvanek
 *
 */
public class VastWorker {

    public static final String CDATA_OPEN = "<![CDATA[";
    public static final String CDATA_CLOSE = "]]>";

    private final XMLInputFactory staxInputFactory = XMLInputFactory.newInstance();
    private final XMLOutputFactory staxOutputFactory = XMLOutputFactory.newInstance();
    private final Schema schema;
    private final JAXBContext jaxbContext;

    public static VastWorker instance() {
        return instance;
    }

    private static final VastWorker instance = new VastWorker();

    private VastWorker() {
        schema = null;
        try {
            jaxbContext = JAXBContext.newInstance(VAST.class.getPackage().getName());
        } catch (JAXBException jaxbx) {
            throw new IllegalArgumentException("VAST jaxb initialization failed", jaxbx);
        }
    }

    /**
     * Most of VAST xml in the wild actually do not conform official xml schema 
     */
    public VastWorker(String vastXsdResource) {
        InputStream stream = getClass().getResourceAsStream(vastXsdResource);// "/vast_2.0.1.xsd"
        if (stream == null) {
            throw new IllegalArgumentException("VAST schema resource is not loadable: " + vastXsdResource);
        }
        SchemaFactory xsdFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        try {
            schema = xsdFactory.newSchema(new StreamSource(stream));
        } catch (SAXException sax) {
            throw new IllegalArgumentException("VAST schema loading failed: " + vastXsdResource, sax);
        }
        try {
            jaxbContext = JAXBContext.newInstance(VAST.class.getPackage().getName());
        } catch (JAXBException jaxbx) {
            throw new IllegalArgumentException("VAST jaxb initialization failed: " + vastXsdResource, jaxbx);
        }
    }

    /**
     * Traverse VAST elements and prefix click urls wherever possible
     * 
     * It is impossible change urls in remotely hosted VAST (obviously)
     * although it is possible change creatives inside Wrapper itself (except linear creative)
     */
    public void setClickThroughUrlPrefix(VAST vast, final Function<String, String> updater) throws VastParsingException {
        if (vast == null) {
            throw new IllegalArgumentException("Null VAST");
        }
        if (updater == null) {
            throw new IllegalArgumentException("Null update function");
        }

        VastElementAdapter adapter = new VastElementAdapter() {

            @Override
            public boolean onWrapper(String id, Wrapper wrapper) {
                throw new IllegalArgumentException("Cannot modify ClickThroughUrl in VAST Wrapper");
            }

            @Override
            public void onInLineLinear(String id, BigInteger sequence, String adID, Linear linear) {
                VideoClicksType videoClicks = linear.getVideoClicks();
                if (videoClicks != null) {
                    ClickThrough clickThrough = videoClicks.getClickThrough();
                    if (clickThrough != null) {
                        String originalUrl = clickThrough.getValue().trim();
                        clickThrough.setValue(CDATA_OPEN + updater.apply(originalUrl) + CDATA_CLOSE);
                    }
                }
            }

            @Override
            public void onInLineCompanion(String id, BigInteger sequence, String adID, CompanionType companion) {
                String originalUrl = companion.getCompanionClickThrough();
                if (originalUrl != null) {
                    companion.setCompanionClickThrough(CDATA_OPEN + updater.apply(originalUrl.trim()) + CDATA_CLOSE);
                }
            }

            @Override
            public void onInLineNonlinear(String id, BigInteger sequence, String adID, NonLinearType nonlinear, TrackingEventsType trackingEvents) {
                String originalUrl = nonlinear.getNonLinearClickThrough();
                if (originalUrl != null) {
                    nonlinear.setNonLinearClickThrough(CDATA_OPEN + updater.apply(originalUrl.trim()) + CDATA_CLOSE);
                }
            }

        };
        visit(vast, null, adapter);
    }

    /**
     * ClickTracking is supported only for InLine/Wrapper Linear Creatives
     * 
     * For Companion and NonLinear Creatives, to track clicks on them, replace 
     * their ClickThrough url with tracker and redirect to original destination
     */
    public void addClickTracker(VAST vast, final String clickTracker) throws VastParsingException {
        if (vast == null) {
            throw new IllegalArgumentException("Null VAST");
        }
        if (StringUtils.isBlank(clickTracker)) {
            throw new IllegalArgumentException("Blank clickTracker");
        }

        VastElementAdapter adapter = new VastElementAdapter() {

            @Override
            public void onInLineLinear(String id, BigInteger sequence, String adID, Linear linear) {
                VideoClicksType videoClicks = linear.getVideoClicks();
                if (videoClicks == null) {
                    videoClicks = new VideoClicksType();
                    linear.setVideoClicks(videoClicks);
                }
                VideoClicksType.ClickTracking clickTracking = new VideoClicksType.ClickTracking();
                clickTracking.setValue(clickTracker);
                videoClicks.getClickTracking().add(clickTracking);
            }

            @Override
            public void onWrapperLinear(String id, BigInteger sequence, String adID, com.byyd.vast2.VAST.Ad.Wrapper.Creatives.Creative.Linear linear) {
                VAST.Ad.Wrapper.Creatives.Creative.Linear.VideoClicks videoClicks = linear.getVideoClicks();
                if (videoClicks == null) {
                    videoClicks = new VAST.Ad.Wrapper.Creatives.Creative.Linear.VideoClicks();
                    linear.setVideoClicks(videoClicks);
                }
                VAST.Ad.Wrapper.Creatives.Creative.Linear.VideoClicks.ClickTracking clickTracking = new VAST.Ad.Wrapper.Creatives.Creative.Linear.VideoClicks.ClickTracking();
                clickTracking.setValue(clickTracker);
                videoClicks.getClickTracking().add(clickTracking);
            }
        };
        visit(vast, null, adapter);
    }

    /**
     * Add impression tracker into Ad element
     * 
     * Note: There are also TrackerEvents on Linear/NonLinear/Companion level not touched by this method
     */
    public void addImpressionTracker(VAST vast, String impressionTracker) {
        if (vast == null) {
            throw new IllegalArgumentException("Null VAST");
        }
        if (StringUtils.isBlank(impressionTracker)) {
            throw new IllegalArgumentException("Blank impressionTracker");
        }
        for (VAST.Ad ad : vast.getAd()) {
            if (ad.getInLine() != null) {
                ImpressionType impressionType = new ImpressionType();
                impressionType.setValue(impressionTracker);
                ad.getInLine().getImpression().add(impressionType);
            } else if (ad.getWrapper() != null) {
                ad.getWrapper().getImpression().add(impressionTracker);
            }
        }
    }

    /**
     * Traverse VAST elements and report them into provided VastVisitor
     */
    public void visit(VAST vast, String resourceUrl, VastElementVisitor visitor) throws VastParsingException {
        if (vast == null) {
            throw new IllegalArgumentException("Null VAST");
        }
        if (visitor == null) {
            throw new IllegalArgumentException("Null visitor");
        }
        visitor.onVAST(vast, resourceUrl);
        for (VAST.Ad ad : vast.getAd()) {
            VAST.Ad.InLine inLine;
            if ((inLine = ad.getInLine()) != null) {
                visitor.onInLine(ad.getId(), inLine);
                VAST.Ad.InLine.Creatives creatives = inLine.getCreatives();
                if (creatives != null && creatives.getCreative().size() != 0) {
                    for (VAST.Ad.InLine.Creatives.Creative creative : creatives.getCreative()) {
                        if (creative.getLinear() != null) {
                            visitor.onInLineLinear(creative.getId(), creative.getSequence(), creative.getAdID(), creative.getLinear());
                        } else if (creative.getNonLinearAds() != null) {
                            for (NonLinearType nonLinear : creative.getNonLinearAds().getNonLinear()) {
                                visitor.onInLineNonlinear(creative.getId(), creative.getSequence(), creative.getAdID(), nonLinear, creative.getNonLinearAds().getTrackingEvents());
                            }
                        } else if (creative.getCompanionAds() != null) {
                            for (CompanionType companion : creative.getCompanionAds().getCompanion()) {
                                visitor.onInLineCompanion(creative.getId(), creative.getSequence(), creative.getAdID(), companion);
                            }
                        }

                    }
                }
            } else if (ad.getWrapper() != null) {
                VAST.Ad.Wrapper wrapper = ad.getWrapper();
                boolean follow = visitor.onWrapper(ad.getId(), wrapper);
                if (wrapper.getCreatives() != null) {
                    for (VAST.Ad.Wrapper.Creatives.Creative creative : wrapper.getCreatives().getCreative()) {
                        if (creative.getLinear() != null) {
                            visitor.onWrapperLinear(creative.getId(), creative.getSequence(), creative.getAdID(), creative.getLinear());
                        } else if (creative.getNonLinearAds() != null) {
                            for (NonLinearType nonLinear : creative.getNonLinearAds().getNonLinear()) {
                                visitor.onWrapperNonlinear(creative.getId(), creative.getSequence(), creative.getAdID(), nonLinear, creative.getNonLinearAds().getTrackingEvents());
                            }
                        } else if (creative.getCompanionAds() != null) {
                            for (CompanionType companion : creative.getCompanionAds().getCompanion()) {
                                visitor.onWrapperCompanion(creative.getId(), creative.getSequence(), creative.getAdID(), companion);
                            }
                        }
                    }
                }
                if (follow) {
                    URL url;
                    try {
                        url = new URL(wrapper.getVASTAdTagURI());
                    } catch (MalformedURLException mux) {
                        throw new VastParsingException("Invalid VASTAdTagURI " + wrapper.getVASTAdTagURI(), mux);
                    }
                    VAST vast2 = read(url);
                    visit(vast2, wrapper.getVASTAdTagURI(), visitor);
                }
            }
        }
    }

    /**
     * Unmarshall VAST xml from reader.
     */
    public VAST read(Reader reader) throws VastParsingException {
        VAST vast;
        ValidationHandlerImpl errorHandler = new ValidationHandlerImpl(false);
        try {
            vast = read(reader, errorHandler);
        } catch (JAXBException | XMLStreamException x) {
            throw new VastParsingException("Failed to parse VAST xml", x);
        } finally {
            IOUtils.closeQuietly(reader);
        }
        if (errorHandler.hasErrors()) {
            throw new VastParsingException(errorHandler.getErrors());
        }
        return vast;
    }

    /**
     * Unmarshall VAST xml from stream. Report errors into provided handler
     */
    public VAST read(Reader inputStream, ValidationEventHandler handler) throws JAXBException, XMLStreamException {

        VAST vast;
        try {
            XMLStreamReader staxStreamReader = staxInputFactory.createXMLStreamReader(inputStream);

            XMLStreamReader filteredReader = staxInputFactory.createFilteredReader(staxStreamReader, new StreamWhitespaceFilter());

            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            if (schema != null) {
                unmarshaller.setSchema(schema);
            }
            unmarshaller.setEventHandler(handler);
            vast = (VAST) unmarshaller.unmarshal(filteredReader);
        } finally {
            IOUtils.closeQuietly(inputStream);
        }
        return vast;
    }

    /**
     * Unmarshall VAST xml from url.
     */
    public VAST read(URL url) throws VastParsingException {
        ValidationHandlerImpl errorHandler = new ValidationHandlerImpl(false);
        VAST vast;
        InputStream inputStream = null;
        try {
            URLConnection connection = url.openConnection();
            inputStream = connection.getInputStream();
            Reader reader = new BufferedReader(new InputStreamReader(inputStream, Charset.forName("utf-8")));
            vast = read(reader, errorHandler);
        } catch (IOException | JAXBException | XMLStreamException x) {
            throw new VastParsingException("Failed to parse " + url, x);
        } finally {
            IOUtils.closeQuietly(inputStream);
        }
        if (errorHandler.hasErrors()) {
            throw new VastParsingException(errorHandler.getErrors());
        }
        return vast;
    }

    /**
     * Marshall VAST to xml
     */
    public void write(VAST vast, Writer writer) {
        try {
            Marshaller marshaller = jaxbContext.createMarshaller();
            CDataXMLStreamWriter cdataStreamWriter = new CDataXMLStreamWriter(staxOutputFactory.createXMLStreamWriter(writer), true);
            marshaller.marshal(vast, cdataStreamWriter);
        } catch (JAXBException | XMLStreamException jaxbx) {
            throw new IllegalStateException("Failed to marshall VAST object", jaxbx);
        }
    }

    /**
     * Marshall VAST to xml
     */
    public void write(VAST vast, OutputStream stream) {
        try {
            Marshaller marshaller = jaxbContext.createMarshaller();
            CDataXMLStreamWriter cdataStreamWriter = new CDataXMLStreamWriter(staxOutputFactory.createXMLStreamWriter(stream), true);
            marshaller.marshal(vast, cdataStreamWriter);
        } catch (JAXBException | XMLStreamException jaxbx) {
            throw new IllegalStateException("Failed to marshall VAST object", jaxbx);
        }
    }

    public static class VastParsingException extends Exception {

        private static final long serialVersionUID = 1L;

        private List<ValidationEvent> formatErrors;

        public VastParsingException(List<ValidationEvent> errors) {
            this.formatErrors = errors;
        }

        public VastParsingException(String message, Exception cause) {
            super(message, cause);
        }

        public List<ValidationEvent> getFormatErrors() {
            return formatErrors;
        }

        public void setFormatErrors(List<ValidationEvent> errors) {
            this.formatErrors = errors;
        }

    }

    public static class ValidationHandlerImpl implements ValidationEventHandler {

        private boolean failFast;

        private List<ValidationEvent> warnings = new ArrayList<ValidationEvent>();
        private List<ValidationEvent> errors = new ArrayList<ValidationEvent>();

        public ValidationHandlerImpl() {
            this(true);
        }

        public List<ValidationEvent> getErrors() {
            return errors;
        }

        public boolean hasErrors() {
            return !errors.isEmpty();
        }

        public ValidationHandlerImpl(boolean failFast) {
            this.failFast = failFast;
        }

        @Override
        public boolean handleEvent(ValidationEvent event) {
            if (event.getSeverity() == ValidationEvent.WARNING) {
                warnings.add(event);
                return true; //continue on warning
            } else {
                errors.add(event);
                return !failFast;
            }
        }

    }

    public static interface VastElementVisitor {

        public void onVAST(VAST vast, String url);

        public void onInLine(String id, VAST.Ad.InLine inline);

        /**
         * @return true if Wrapper's VASTAdTagURI shall be downloaded, parsed and traversed too
         */
        public boolean onWrapper(String id, VAST.Ad.Wrapper wrapper);

        public void onInLineLinear(String id, BigInteger sequence, String adID, VAST.Ad.InLine.Creatives.Creative.Linear linear);

        public void onInLineNonlinear(String id, BigInteger sequence, String adID, NonLinearType nonlinear, TrackingEventsType trackingEvents);

        public void onWrapperLinear(String id, BigInteger sequence, String adID, com.byyd.vast2.VAST.Ad.Wrapper.Creatives.Creative.Linear linear);

        public void onWrapperNonlinear(String id, BigInteger sequence, String adID, NonLinearType nonlinear, TrackingEventsType trackingEvents);

        public void onInLineCompanion(String id, BigInteger sequence, String adID, CompanionType companion);

        public void onWrapperCompanion(String id, BigInteger sequence, String adID, CompanionType companion);
    }

    public static class VastElementAdapter implements VastElementVisitor {

        @Override
        public void onVAST(VAST vast, String url) {
            // To be overriden
        }

        @Override
        public void onInLine(String id, InLine inline) {
            // To be overriden
        }

        @Override
        public boolean onWrapper(String id, Wrapper wrapper) {
            return true;
        }

        @Override
        public void onInLineLinear(String id, BigInteger sequence, String adID, Linear linear) {
            // To be overriden
        }

        @Override
        public void onInLineNonlinear(String id, BigInteger sequence, String adID, NonLinearType nonlinear, TrackingEventsType trackingEvents) {
            // To be overriden
        }

        @Override
        public void onWrapperLinear(String id, BigInteger sequence, String adID, com.byyd.vast2.VAST.Ad.Wrapper.Creatives.Creative.Linear linear) {
            // To be overriden
        }

        @Override
        public void onWrapperNonlinear(String id, BigInteger sequence, String adID, NonLinearType nonlinear, TrackingEventsType trackingEvents) {
            // To be overriden
        }

        @Override
        public void onInLineCompanion(String id, BigInteger sequence, String adID, CompanionType companion) {
            // To be overriden
        }

        @Override
        public void onWrapperCompanion(String id, BigInteger sequence, String adID, CompanionType companion) {
            // To be overriden
        }
    }

    static class StreamWhitespaceFilter implements StreamFilter {

        @Override
        public boolean accept(XMLStreamReader reader) {
            return !reader.isWhiteSpace();
        }

    }

    static class WhitespaceFilter implements EventFilter {
        @Override
        public boolean accept(XMLEvent event) {
            return !(event.isCharacters() && ((Characters) event).isWhiteSpace());
        }
    }

    /**
     * This needs explanation. Mind that VAST is interpreted in limited and hacky runtimes of various mobile ad SDKs 
     * so we have to be nice and compliant with their quirks as we possibly can.
     * VAST industry unwritten standard is to wrap ALL urls in CDATA section (even when url does not contain restricted characters and it is usable as is in element value)
     * 
     * Also be aware that escaping URL in any way is also mistake, just stuff CDATA section there...
     * Also be aware that this XMLStreamWriter is used for writing EVERY element value in VAST document not just urls....
     */
    static class CDataXMLStreamWriter implements XMLStreamWriter {

        private static final Pattern XML_CHARS = Pattern.compile("[&<>]");

        private final XMLStreamWriter delegate;

        private final boolean urlHack;

        public CDataXMLStreamWriter(XMLStreamWriter delegate, boolean urlHack) {
            this.delegate = delegate;
            this.urlHack = urlHack;
        }

        @Override
        public void writeCharacters(String text) throws XMLStreamException {
            // trim usual leading newline
            String trtext = text.trim();
            if (trtext.startsWith(CDATA_OPEN)) {
                if (!text.endsWith(CDATA_CLOSE)) {
                    throw new IllegalArgumentException("'<![CDATA[' started but not terminated by ']]>' : " + text);
                }
                // If CDATA wrapper is passed, we cannot write it using writeCharacters because it would be escaped by internal xml writer
                // Original wrapped text must be cut out and written using writeCData instead
                String original = trtext.substring(9, trtext.length() - 3);
                delegate.writeCData(original);
                return;
            }

            boolean needCData = false;
            if (urlHack) {
                // Hack that URL crap into CDATA and read class comment if you want to know why
                needCData = trtext.startsWith("http");
            }
            // Finally check if it is really necessary because of restricted characters
            needCData = needCData || XML_CHARS.matcher(trtext).find();
            if (needCData) {
                delegate.writeCData(text);
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

    public static void main(String[] args) {
        if (args.length != 1) {
            throw new IllegalArgumentException("One parameter expected: file with tag to be verified");
        }
        File file = new File(args[0]);
        if (!file.exists() || !file.isFile() || !file.canRead()) {
            throw new IllegalArgumentException("Cannot read: " + file);
        }
        try {
            VastWorker worker = VastWorker.instance();
            VAST vast = worker.read(new FileReader(file));
            // Tracker urls in CDATA wrappers
            worker.addImpressionTracker(vast, "<![CDATA[#if($renderBeacons && $adComponents.components.beacons) ${adComponents.components.beacons.beacon1} #end]]>");
            worker.addClickTracker(vast, "<![CDATA[${adComponents.destinationUrl}]]>");
            worker.addImpressionTracker(vast, "http://www.xxx.yyy");
            StringWriter writer = new StringWriter();
            worker.write(vast, writer);
            System.out.println(writer.toString());
            // try to parse result to verify ...
            worker.read(new StringReader(writer.toString()));
        } catch (Exception x) {
            x.printStackTrace();
        }
    }

}
