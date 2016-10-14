package com.adfonic.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.junit.Test;

public class ImageUtilsIT {
    private static final transient Logger LOG = Logger.getLogger(ImageUtilsIT.class.getName());

    static final float ALPHA = 0.75f; // 75% opacity

    static final int BORDER_WIDTH = 5;

    @Test
    public void test() throws Exception {
        ImageUtils imageUtils = new ImageUtils();

        Map<String, String> colors = new LinkedHashMap<String, String>() {
            /**
         *
         */
            private static final long serialVersionUID = 1L;

            {
                put("white", "FFFFFF");
                put("black", "000000");
                put("red", "FF0000");
                put("green", "00FF00");
                put("blue", "0000FF");
                put("cyan", "00FFFF");
                put("magenta", "FF00FF");
                put("yellow", "FFFF00");
            }
        };

            File testOutputDir = new File("test-output");
            testOutputDir.mkdir();

            File outputDir;
            File inputFile, outputFile;
            FileOutputStream fileOutputStream;

            for (ImageUtils.BorderMode borderMode : ImageUtils.BorderMode.values()) {
                for (Map.Entry<String, String> entry : colors.entrySet()) {
                    LOG.info("Doing " + borderMode + " " + entry.getKey());

                outputDir = new File(testOutputDir, borderMode + "-jpeg");
                    outputDir.mkdir();
                    inputFile = new File("src/test/resources/border-test-input.jpg");
                    outputFile = new File(outputDir, "output-jpeg-" + entry.getKey() + ".jpg");
                    LOG.info("Input: " + inputFile.getCanonicalPath());
                    fileOutputStream = new FileOutputStream(outputFile);
                    imageUtils.addBorderToImage(new FileInputStream(inputFile), fileOutputStream, false, borderMode, entry.getValue(), ALPHA, BORDER_WIDTH);
                    fileOutputStream.close();
                    LOG.info("Output: " + outputFile.getCanonicalPath());

                    outputDir = new File(testOutputDir, borderMode + "-png");
                    outputDir.mkdir();
                    inputFile = new File("src/test/resources/border-test-input.png");
                    outputFile = new File(outputDir, "output-png-" + entry.getKey() + ".png");
                    LOG.info("Input: " + inputFile.getCanonicalPath());
                    fileOutputStream = new FileOutputStream(outputFile);
                    imageUtils.addBorderToImage(new FileInputStream(inputFile), fileOutputStream, false, borderMode, entry.getValue(), ALPHA, BORDER_WIDTH);
                    fileOutputStream.close();
                    LOG.info("Output: " + outputFile.getCanonicalPath());

                    outputDir = new File(testOutputDir, borderMode + "-animated");
                    outputDir.mkdir();
                    inputFile = new File("src/test/resources/border-test-animated.gif");
                    outputFile = new File(outputDir, "output-animated-" + entry.getKey() + ".gif");
                    LOG.info("Input: " + inputFile.getCanonicalPath());
                    fileOutputStream = new FileOutputStream(outputFile);
                    imageUtils.addBorderToImage(new FileInputStream(inputFile), fileOutputStream, true, borderMode, entry.getValue(), ALPHA, BORDER_WIDTH);
                    fileOutputStream.close();
                    LOG.info("Output: " + outputFile.getCanonicalPath());
                }
            }
    }
}
