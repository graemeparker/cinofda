package com.adfonic.asset.controller;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;

import javax.servlet.ServletContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jmock.Expectations;
import org.junit.Before;
import org.junit.Test;

import com.adfonic.domain.Asset;
import com.adfonic.domain.ContentType;
import com.adfonic.test.AbstractAdfonicTest;
import com.adfonic.util.ImageUtils;
import com.byyd.middleware.creative.service.AssetManager;

public class TestAssetController extends AbstractAdfonicTest {
    private AssetManager assetManager;
    private ImageUtils imageUtils;
    private String autoBorderAverageColor;
    private AssetController assetController;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private ServletContext servletContext;

    @Before
    public void runBeforeEachTest() {
        assetManager = mock(AssetManager.class);
        imageUtils = mock(ImageUtils.class);
        autoBorderAverageColor = randomHexString(6);
        servletContext = mock(ServletContext.class);
        expect(new Expectations() {
            {
                allowing(servletContext).getResourceAsStream(with(any(String.class)));
                will(returnValue(new ByteArrayInputStream(new byte[0])));
            }
        });
        assetController = new AssetController(assetManager, imageUtils, autoBorderAverageColor, servletContext);
        request = mock(HttpServletRequest.class, "request");
        response = mock(HttpServletResponse.class, "response");
    }

    @Test
    public void test01_handleRequest_asset_not_found() throws Exception {
        final String assetExternalID = randomAlphaNumericString(10);
        final String borderColorRgbHex = null;
        expect(new Expectations() {
            {
                oneOf(assetManager).getAssetByExternalId(assetExternalID);
                will(returnValue(null));
                oneOf(response).sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        });
        assetController.handleRequest(request, response, assetExternalID, borderColorRgbHex, null, 1, null);
    }

    @Test
    public void test02_handleRequest_asset_found() throws Exception {
        final String assetExternalID = randomAlphaNumericString(10);
        final String borderColorRgbHex = null;
        final Asset asset = mock(Asset.class);
        final ContentType contentType = mock(ContentType.class);
        final String mimeType = randomAlphaNumericString(10);
        final byte[] data = randomAlphaNumericString(300).getBytes();
        final ServletOutputStream outputStream = mock(ServletOutputStream.class);
        expect(new Expectations() {
            {
                oneOf(assetManager).getAssetByExternalId(assetExternalID);
                will(returnValue(asset));
                oneOf(response).setHeader("Cache-Control", "max-age=" + Integer.MAX_VALUE);
                oneOf(response).setHeader(with("Expires"), with(any(String.class)));
                oneOf(asset).getData();
                will(returnValue(data));
                atLeast(1).of(asset).getContentType();
                will(returnValue(contentType));
                atLeast(1).of(contentType).getMIMEType();
                will(returnValue(mimeType));
                oneOf(response).setContentType(mimeType);
                oneOf(response).setContentLength(data.length);
                oneOf(response).getOutputStream();
                will(returnValue(outputStream));
                oneOf(outputStream).write(data);
            }
        });
        assetController.handleRequest(request, response, assetExternalID, borderColorRgbHex, null, 1, null);
    }

    @Test
    public void test03_handleRequest_border() throws Exception {
        final String assetExternalID = randomAlphaNumericString(10);
        final String borderColorRgbHex = "FFFF00";
        final Asset asset = mock(Asset.class);
        final long assetId = randomLong();
        final ContentType contentType = mock(ContentType.class);
        final String mimeType = randomAlphaNumericString(10);
        final byte[] data = randomAlphaNumericString(300).getBytes();
        final ServletOutputStream outputStream = mock(ServletOutputStream.class);
        final boolean animated = true;
        final int borderWidth = randomInteger(10);
        expect(new Expectations() {
            {
                allowing(asset).getId();
                will(returnValue(assetId));
                oneOf(assetManager).getAssetByExternalId(assetExternalID);
                will(returnValue(asset));
                oneOf(response).setHeader("Cache-Control", "max-age=" + Integer.MAX_VALUE);
                oneOf(response).setHeader(with("Expires"), with(any(String.class)));
                oneOf(asset).getData();
                will(returnValue(data));
                allowing(asset).getExternalID();
                will(returnValue(assetExternalID));
                oneOf(contentType).isAnimated();
                will(returnValue(animated));
                oneOf(imageUtils).addBorderToImage(with(any(InputStream.class)), with(any(OutputStream.class)), with(animated), with(ImageUtils.BorderMode.SOLID),
                        with(borderColorRgbHex), with(AssetController.DEFAULT_ALPHA_SOLID), with(borderWidth));
                allowing(asset).getContentType();
                will(returnValue(contentType));
                atLeast(1).of(contentType).getMIMEType();
                will(returnValue(mimeType));
                oneOf(response).setContentType(mimeType);
                oneOf(response).setContentLength(with(any(Integer.class)));
                oneOf(response).getOutputStream();
                will(returnValue(outputStream));
                oneOf(outputStream).write(with(any(byte[].class)));
            }
        });
        assetController.handleRequest(request, response, assetExternalID, borderColorRgbHex, null, borderWidth, null);
    }

    @Test
    public void test04_handleRequest_border_exception() throws Exception {
        final String assetExternalID = randomAlphaNumericString(10);
        final String borderColorRgbHex = "FFFF00";
        final Asset asset = mock(Asset.class);
        final long assetId = randomLong();
        final ContentType contentType = mock(ContentType.class);
        final String mimeType = randomAlphaNumericString(10);
        final byte[] data = randomAlphaNumericString(300).getBytes();
        final ServletOutputStream outputStream = mock(ServletOutputStream.class);
        final boolean animated = true;
        final int borderWidth = randomInteger(10);
        expect(new Expectations() {
            {
                allowing(asset).getId();
                will(returnValue(assetId));
                oneOf(assetManager).getAssetByExternalId(assetExternalID);
                will(returnValue(asset));
                oneOf(response).setHeader("Cache-Control", "max-age=" + Integer.MAX_VALUE);
                oneOf(response).setHeader(with("Expires"), with(any(String.class)));
                oneOf(asset).getData();
                will(returnValue(data));
                allowing(asset).getExternalID();
                will(returnValue(assetExternalID));
                oneOf(contentType).isAnimated();
                will(returnValue(animated));
                oneOf(imageUtils).addBorderToImage(with(any(InputStream.class)), with(any(OutputStream.class)), with(animated), with(ImageUtils.BorderMode.SOLID),
                        with(borderColorRgbHex), with(AssetController.DEFAULT_ALPHA_SOLID), with(borderWidth));
                will(throwException(new IllegalStateException("This is intentional test exception")));
                atLeast(1).of(asset).getContentType();
                will(returnValue(contentType));
                atLeast(1).of(contentType).getMIMEType();
                will(returnValue(mimeType));
                oneOf(response).setContentType(mimeType);
                oneOf(response).setContentLength(with(any(Integer.class)));
                oneOf(response).getOutputStream();
                will(returnValue(outputStream));
                oneOf(outputStream).write(with(any(byte[].class)));
            }
        });
        assetController.handleRequest(request, response, assetExternalID, borderColorRgbHex, null, borderWidth, null);
    }

    @Test
    public void test05_handleRequest_border_auto() throws Exception {
        final String assetExternalID = randomAlphaNumericString(10);
        final String borderColorRgbHex = AssetController.AUTO;
        final Asset asset = mock(Asset.class);
        final long assetId = randomLong();
        final ContentType contentType = mock(ContentType.class);
        final String mimeType = randomAlphaNumericString(10);
        final byte[] data = randomAlphaNumericString(300).getBytes();
        final ServletOutputStream outputStream = mock(ServletOutputStream.class);
        final boolean animated = true;
        final int borderWidth = randomInteger(10);
        expect(new Expectations() {
            {
                allowing(asset).getId();
                will(returnValue(assetId));
                oneOf(assetManager).getAssetByExternalId(assetExternalID);
                will(returnValue(asset));
                oneOf(response).setHeader("Cache-Control", "max-age=" + Integer.MAX_VALUE);
                oneOf(response).setHeader(with("Expires"), with(any(String.class)));
                oneOf(asset).getData();
                will(returnValue(data));
                allowing(asset).getExternalID();
                will(returnValue(assetExternalID));
                oneOf(contentType).isAnimated();
                will(returnValue(animated));
                oneOf(imageUtils).addBorderToImage(with(any(InputStream.class)), with(any(OutputStream.class)), with(animated), with(ImageUtils.BorderMode.AVERAGE),
                        with(autoBorderAverageColor), with(AssetController.DEFAULT_ALPHA_AVERAGE), with(borderWidth));
                atLeast(1).of(asset).getContentType();
                will(returnValue(contentType));
                atLeast(1).of(contentType).getMIMEType();
                will(returnValue(mimeType));
                oneOf(response).setContentType(mimeType);
                oneOf(response).setContentLength(with(any(Integer.class)));
                oneOf(response).getOutputStream();
                will(returnValue(outputStream));
                oneOf(outputStream).write(with(any(byte[].class)));
            }
        });
        assetController.handleRequest(request, response, assetExternalID, borderColorRgbHex, null, borderWidth, null);
    }

    @Test
    public void test06_handleRequest_border_auto_with_alpha() throws Exception {
        final String assetExternalID = randomAlphaNumericString(10);
        final String borderColorRgbHex = AssetController.AUTO;
        final Asset asset = mock(Asset.class);
        final long assetId = randomLong();
        final ContentType contentType = mock(ContentType.class);
        final String mimeType = randomAlphaNumericString(10);
        final byte[] data = randomAlphaNumericString(300).getBytes();
        final ServletOutputStream outputStream = mock(ServletOutputStream.class);
        final boolean animated = true;
        final int borderWidth = randomInteger(10);
        final float alpha = 0.25f;
        expect(new Expectations() {
            {
                allowing(asset).getId();
                will(returnValue(assetId));
                oneOf(assetManager).getAssetByExternalId(assetExternalID);
                will(returnValue(asset));
                oneOf(response).setHeader("Cache-Control", "max-age=" + Integer.MAX_VALUE);
                oneOf(response).setHeader(with("Expires"), with(any(String.class)));
                oneOf(asset).getData();
                will(returnValue(data));
                allowing(asset).getExternalID();
                will(returnValue(assetExternalID));
                oneOf(contentType).isAnimated();
                will(returnValue(animated));
                oneOf(imageUtils).addBorderToImage(with(any(InputStream.class)), with(any(OutputStream.class)), with(animated), with(ImageUtils.BorderMode.AVERAGE),
                        with(autoBorderAverageColor), with(alpha), with(borderWidth));
                atLeast(1).of(asset).getContentType();
                will(returnValue(contentType));
                atLeast(1).of(contentType).getMIMEType();
                will(returnValue(mimeType));
                oneOf(response).setContentType(mimeType);
                oneOf(response).setContentLength(with(any(Integer.class)));
                oneOf(response).getOutputStream();
                will(returnValue(outputStream));
                oneOf(outputStream).write(with(any(byte[].class)));
            }
        });
        assetController.handleRequest(request, response, assetExternalID, borderColorRgbHex, alpha, borderWidth, null);
    }
}
