package com.adfonic.retargeting;

import org.junit.Test;

import com.adfonic.test.AbstractAdfonicTest;

public class TestRetargetingException extends AbstractAdfonicTest {
    @Test
    public void test01() {
        new RetargetingException(randomAlphaNumericString(10));
    }

    @Test
    public void test02() {
        new RetargetingException(randomAlphaNumericString(10), new IllegalStateException("bummer dude"));
    }
}