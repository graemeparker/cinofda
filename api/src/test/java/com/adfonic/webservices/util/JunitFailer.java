package com.adfonic.webservices.util;

import org.junit.Assert;

public class JunitFailer implements GeneralAbort {

    public void abort(String message) {
        Assert.fail(message);
    }

}
