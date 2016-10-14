package com.adfonic.adserver;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.HashMap;

import org.junit.Before;
import org.junit.Test;

import com.adfonic.domain.DestinationType;

@SuppressWarnings("serial")
public class TestAbstractAdComponents extends BaseAdserverTest {

    private AbstractAdComponents abstractAdComponents;

    @Before
    public void intTest() {
        abstractAdComponents = new AbstractAdComponents() {
        };
    }

    @Test
    public void testAbstractAdComponents1() {
        DestinationType destinationType = DestinationType.ANDROID;
        String destinationUrl = "http://adfonic.com";
        String format = "banner";

        abstractAdComponents.setDestinationType(destinationType);
        abstractAdComponents.setDestinationUrl(destinationUrl);
        abstractAdComponents.setFormat(format);
        abstractAdComponents.getComponents().put("SomeKey", new HashMap<String, String>());

        assertEquals(destinationType, abstractAdComponents.getDestinationType());
        assertEquals(destinationUrl, abstractAdComponents.getDestinationUrl());
        assertEquals(format, abstractAdComponents.getFormat());

        //System.out.println("abstractAdComponents="+abstractAdComponents);
    }

    @Test
    public void testAbstractAdComponents2() {
        DestinationType destinationType = DestinationType.ANDROID;
        String destinationUrl = "http://adfonic.com";
        String format = "banner";

        abstractAdComponents.setDestinationType(destinationType);
        abstractAdComponents.setDestinationUrl(destinationUrl);
        abstractAdComponents.setFormat(format);
        abstractAdComponents.getComponents().put("SomeKey", new HashMap<String, String>());

        AbstractAdComponents anotherAbstractAdComponents = new AbstractAdComponents(abstractAdComponents) {
        };

        assertEquals(destinationType, anotherAbstractAdComponents.getDestinationType());
        assertEquals(destinationUrl, anotherAbstractAdComponents.getDestinationUrl());
        assertEquals(format, anotherAbstractAdComponents.getFormat());

        //Make Sure components were added to new HashMap
        assertFalse(anotherAbstractAdComponents.getComponents() == abstractAdComponents.getComponents());

        //System.out.println("abstractAdComponents="+abstractAdComponents);
    }
}
