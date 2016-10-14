package com.adfonic.adserver.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.jmock.Expectations;
import org.junit.Before;
import org.junit.Test;

import com.adfonic.domain.AdSpace.ColorScheme;
import com.adfonic.domain.DestinationType;
import com.adfonic.test.AbstractAdfonicTest;
import com.adfonic.util.Base64;

public class TestIconManagerImpl extends AbstractAdfonicTest {
    private IconManagerImpl iconManagerImpl;

    @Before
    public void initTests() {
        iconManagerImpl = new IconManagerImpl();
    }

    @Test
    public void testGetFileBaseName() {
        DestinationType destinationType = DestinationType.URL;
        ColorScheme colorScheme = ColorScheme.blue;
        int width = randomInteger(3000);
        int height = randomInteger(3000);
        String expected = destinationType.name() + "-" + colorScheme.name() + "-" + width + "x" + height;
        assertEquals(expected, IconManagerImpl.getFileBaseName(destinationType, colorScheme, width, height));
    }

    @Test
    public void testGetResourcePath() {
        String fileBaseName = randomAlphaNumericString(10);
        String expected = IconManagerImpl.RESOURCE_PATH + fileBaseName + IconManagerImpl.EXTENSION;
        assertEquals(expected, IconManagerImpl.getResourcePath(fileBaseName));
    }

    @Test
    public void testGetBase64EncodedImageData01_already_cached() throws IOException {
        final DestinationType destinationType = DestinationType.URL;
        final ColorScheme colorScheme = ColorScheme.blue;
        final int width = randomInteger(3000);
        final int height = randomInteger(3000);
        final String fileBaseName = IconManagerImpl.getFileBaseName(destinationType, colorScheme, width, height);
        final String base64Encoded = randomAlphaNumericString(10);
        iconManagerImpl.cache.put(fileBaseName, base64Encoded);
        assertEquals(base64Encoded, iconManagerImpl.getBase64EncodedImageData(destinationType, colorScheme, width, height));
    }

    @Test(expected = IllegalStateException.class)
    public void testGetBase64EncodedImageData02_not_already_cached_IOException() throws IOException {
        final DestinationType destinationType = DestinationType.URL;
        final ColorScheme colorScheme = ColorScheme.blue;
        final int width = randomInteger(3000);
        final int height = randomInteger(3000);
        final String fileBaseName = IconManagerImpl.getFileBaseName(destinationType, colorScheme, width, height);
        final String path = IconManagerImpl.getResourcePath(fileBaseName);
        expect(new Expectations() {
            {
                //oneOf(servletContext).getResourceAsStream(path);
                //will(throwException(new java.io.IOException("bummer")));
            }
        });
        iconManagerImpl.getBase64EncodedImageData(destinationType, colorScheme, width, height);
    }

    @Test
    public void testGetBase64EncodedImageData03_not_already_cached_file_fallback() throws IOException {
        final DestinationType destinationType = DestinationType.URL;
        final ColorScheme colorScheme = ColorScheme.blue;
        final int width = 300;
        final int height = 50;
        final String fileBaseName = IconManagerImpl.getFileBaseName(destinationType, colorScheme, width, height);
        final String path = IconManagerImpl.getResourcePath(fileBaseName);
        expect(new Expectations() {
            {
                //oneOf(servletContext).getResourceAsStream(path);
                //will(throwException(new IllegalStateException("fall back on file")));
            }
        });
        assertNotNull(iconManagerImpl.getBase64EncodedImageData(destinationType, colorScheme, width, height));
    }

    @Test
    public void testGetBase64EncodedImageData04_not_already_cached() throws IOException {
        final DestinationType destinationType = DestinationType.URL;
        final ColorScheme colorScheme = ColorScheme.blue;
        final int width = 300;
        final int height = 50;
        final String fileBaseName = IconManagerImpl.getFileBaseName(destinationType, colorScheme, width, height);
        final String path = IconManagerImpl.getResourcePath(fileBaseName);
        final String base64Encoded = Base64.encode(IOUtils.toByteArray(getClass().getResourceAsStream(path)));
        expect(new Expectations() {
            {
                //oneOf(servletContext).getResourceAsStream(path);
                //will(returnValue(new java.io.ByteArrayInputStream(rawImageData)));
            }
        });
        assertEquals(base64Encoded, iconManagerImpl.getBase64EncodedImageData(destinationType, colorScheme, width, height));
        // Make sure it was cached
        assertEquals(base64Encoded, iconManagerImpl.cache.get(fileBaseName));
    }

    @Test
    public void testPreLoadIconImages01_good() {
        expect(new Expectations() {
            {
                //allowing(servletContext).getResourceAsStream(with(any(String.class)));
                //will(throwException(new IllegalStateException("fall back on file")));
            }
        });
        iconManagerImpl.preLoadIconImages();
    }

    @Test
    public void testPreLoadIconImages02_exception() {
        expect(new Expectations() {
            {
                //allowing(servletContext).getResourceAsStream(with(any(String.class)));
                //will(throwException(new java.io.IOException("bummer")));
            }
        });
        iconManagerImpl.preLoadIconImages();
    }
}
