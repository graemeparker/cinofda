package com.adfonic.util;

import static org.junit.Assert.assertTrue;

import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

public class TestMediaUtils {
    @Test
    public void testGetImageInfo() throws Exception {
        final int numLoops = 10;
        for (int k = 0; k < numLoops; ++k) {
            doTest("/120x20_B_HI.png", 120, 20);
            doTest("/banners/test_images/image_9_xl.jpg", 300, 50);
            doTest("/banners/test_images/image_9_l.jpg", 216, 36);
            doTest("/banners/test_images/image_9_m.gif", 168, 28);
            doTest("/banners/test_images/image_9_s.png", 120, 20);
            doTest("/320x480-for-AO-177.jpg", 320, 480);
            doTest("/anim-test.gif", 176, 81);
        }
    }

    private void doTest(String resource, int width, int height) throws java.io.IOException {
        InputStream in = this.getClass().getResourceAsStream(resource);
        MediaUtils.ImageInfo ii = MediaUtils.getImageInfo(in);
        System.out.println(resource + " => " + ii);
        assertTrue(ii.getWidth() == width);
        assertTrue(ii.getHeight() == height);
        IOUtils.closeQuietly(in);
    }
}
