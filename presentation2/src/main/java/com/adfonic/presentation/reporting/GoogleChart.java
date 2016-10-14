package com.adfonic.presentation.reporting;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;


public abstract class GoogleChart {
    public static final String SERVICE_URL_BASE = "http://chart.apis.google.com/chart?";

    private List<BasicNameValuePair> params;
    private int width;
    private int height;
    private Object url;

    protected GoogleChart(int width, int height) {
        this.width = width;
        this.height = height;
        this.params = new ArrayList<BasicNameValuePair>();

        // Specify chart size
        addParam("chs", width + "x" + height);
    }

    protected final void addParam(String name, String value) {
        params.add(new BasicNameValuePair(name, value));
    }

    protected void generateURL() {
        this.url = SERVICE_URL_BASE +
            URLEncodedUtils.format(params, StandardCharsets.ISO_8859_1);
    }

    public List<BasicNameValuePair> getParams() {
        return params;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public Object getUrl() {
        return url;
    }
}
