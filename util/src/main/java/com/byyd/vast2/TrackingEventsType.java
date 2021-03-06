//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.11 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2015.02.06 at 10:17:13 AM GMT 
//


package com.byyd.vast2;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * <p>Java class for TrackingEvents_type complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="TrackingEvents_type"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="Tracking" maxOccurs="unbounded" minOccurs="0"&gt;
 *           &lt;complexType&gt;
 *             &lt;simpleContent&gt;
 *               &lt;extension base="&lt;http://www.w3.org/2001/XMLSchema&gt;anyURI"&gt;
 *                 &lt;attribute name="event" use="required"&gt;
 *                   &lt;simpleType&gt;
 *                     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}NMTOKEN"&gt;
 *                       &lt;enumeration value="creativeView"/&gt;
 *                       &lt;enumeration value="start"/&gt;
 *                       &lt;enumeration value="midpoint"/&gt;
 *                       &lt;enumeration value="firstQuartile"/&gt;
 *                       &lt;enumeration value="thirdQuartile"/&gt;
 *                       &lt;enumeration value="complete"/&gt;
 *                       &lt;enumeration value="mute"/&gt;
 *                       &lt;enumeration value="unmute"/&gt;
 *                       &lt;enumeration value="pause"/&gt;
 *                       &lt;enumeration value="rewind"/&gt;
 *                       &lt;enumeration value="resume"/&gt;
 *                       &lt;enumeration value="fullscreen"/&gt;
 *                       &lt;enumeration value="expand"/&gt;
 *                       &lt;enumeration value="collapse"/&gt;
 *                       &lt;enumeration value="acceptInvitation"/&gt;
 *                       &lt;enumeration value="close"/&gt;
 *                     &lt;/restriction&gt;
 *                   &lt;/simpleType&gt;
 *                 &lt;/attribute&gt;
 *               &lt;/extension&gt;
 *             &lt;/simpleContent&gt;
 *           &lt;/complexType&gt;
 *         &lt;/element&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "TrackingEvents_type", propOrder = {
    "tracking"
})
public class TrackingEventsType {

    @XmlElement(name = "Tracking")
    protected List<TrackingEventsType.Tracking> tracking;

    /**
     * Gets the value of the tracking property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the tracking property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getTracking().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link TrackingEventsType.Tracking }
     * 
     * 
     */
    public List<TrackingEventsType.Tracking> getTracking() {
        if (tracking == null) {
            tracking = new ArrayList<TrackingEventsType.Tracking>();
        }
        return this.tracking;
    }


    /**
     * <p>Java class for anonymous complex type.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.
     * 
     * <pre>
     * &lt;complexType&gt;
     *   &lt;simpleContent&gt;
     *     &lt;extension base="&lt;http://www.w3.org/2001/XMLSchema&gt;anyURI"&gt;
     *       &lt;attribute name="event" use="required"&gt;
     *         &lt;simpleType&gt;
     *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}NMTOKEN"&gt;
     *             &lt;enumeration value="creativeView"/&gt;
     *             &lt;enumeration value="start"/&gt;
     *             &lt;enumeration value="midpoint"/&gt;
     *             &lt;enumeration value="firstQuartile"/&gt;
     *             &lt;enumeration value="thirdQuartile"/&gt;
     *             &lt;enumeration value="complete"/&gt;
     *             &lt;enumeration value="mute"/&gt;
     *             &lt;enumeration value="unmute"/&gt;
     *             &lt;enumeration value="pause"/&gt;
     *             &lt;enumeration value="rewind"/&gt;
     *             &lt;enumeration value="resume"/&gt;
     *             &lt;enumeration value="fullscreen"/&gt;
     *             &lt;enumeration value="expand"/&gt;
     *             &lt;enumeration value="collapse"/&gt;
     *             &lt;enumeration value="acceptInvitation"/&gt;
     *             &lt;enumeration value="close"/&gt;
     *           &lt;/restriction&gt;
     *         &lt;/simpleType&gt;
     *       &lt;/attribute&gt;
     *     &lt;/extension&gt;
     *   &lt;/simpleContent&gt;
     * &lt;/complexType&gt;
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "value"
    })
    public static class Tracking {

        @XmlValue
        @XmlSchemaType(name = "anyURI")
        protected String value;
        @XmlAttribute(name = "event", required = true)
        @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
        protected String event;

        /**
         * Gets the value of the value property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getValue() {
            return value;
        }

        /**
         * Sets the value of the value property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setValue(String value) {
            this.value = value;
        }

        /**
         * Gets the value of the event property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getEvent() {
            return event;
        }

        /**
         * Sets the value of the event property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setEvent(String value) {
            this.event = value;
        }

    }

}
