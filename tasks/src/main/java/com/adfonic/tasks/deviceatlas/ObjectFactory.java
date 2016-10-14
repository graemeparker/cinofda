//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vJAXB 2.1.10 in JDK 6 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2011.07.08 at 08:33:06 AM EDT 
//


package com.adfonic.tasks.deviceatlas;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the com.adfonic.tasks.deviceatlas package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _Model_QNAME = new QName("", "model");
    private final static QName _Title_QNAME = new QName("", "title");
    private final static QName _Vendor_QNAME = new QName("", "vendor");
    private final static QName _Nid_QNAME = new QName("", "nid");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: com.adfonic.tasks.deviceatlas
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link Device }
     * 
     */
    public Device createDevice() {
        return new Device();
    }

    /**
     * Create an instance of {@link Deviceatlas }
     * 
     */
    public Deviceatlas createDeviceatlas() {
        return new Deviceatlas();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "model")
    public JAXBElement<String> createModel(String value) {
        return new JAXBElement<String>(_Model_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "title")
    public JAXBElement<String> createTitle(String value) {
        return new JAXBElement<String>(_Title_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "vendor")
    public JAXBElement<String> createVendor(String value) {
        return new JAXBElement<String>(_Vendor_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "nid")
    public JAXBElement<String> createNid(String value) {
        return new JAXBElement<String>(_Nid_QNAME, String.class, null, value);
    }

}
