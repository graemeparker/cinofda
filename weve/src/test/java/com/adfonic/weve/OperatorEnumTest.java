package com.adfonic.weve;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class OperatorEnumTest {

    @Test
    public void testReverseLookup() {
        assertThat("TELEFONICA", equalTo(OperatorEnum.getNameById(1)));
    }

}
