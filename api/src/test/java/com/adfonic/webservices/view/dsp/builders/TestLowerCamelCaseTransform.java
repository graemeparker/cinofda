package com.adfonic.webservices.view.dsp.builders;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class TestLowerCamelCaseTransform {

    private ValueNameTransformer transformer = LowerCamelCaseTransform.INSTANCE;


    @Test
    public void testMult() {
        assertEquals("abCoDo", transformer.convert("ab_co_do"));
        assertEquals("sdA", transformer.convert("sdA"));
        assertEquals("raod_", transformer.convert("raod_"));
        assertEquals("Raod_", transformer.convert("_raod_"));
    }
}
