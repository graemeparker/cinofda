package com.adfonic.util;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Node;

public class ImageUtils {
    private static final transient Logger LOG = Logger.getLogger(ImageUtils.class.getName());

    public enum BorderMode {
        SOLID, AVERAGE
    }

    /**
     * Add a border to a given image
     * 
     * @param inputStream
     *            the input image data stream
     * @param outputStream
     *            the stream to which the image output will be written
     * @param animated
     *            whether or not the image is animated (has multiple frames)
     * @param borderMode
     *            the desired border mode
     * @param rgbHex
     *            string identifying the border color in RGB hex format
     * @param alpha
     *            the alpha or opacity value between 0.0 (transparent) and 1.0
     *            (opaque)
     * @param borderWidth
     *            the border width in pixels
     */
    public void addBorderToImage(InputStream inputStream, OutputStream outputStream, boolean animated, BorderMode borderMode, String rgbHex, float alpha, int borderWidth)
            throws java.io.IOException {
        addBorderToImage(inputStream, outputStream, animated, borderMode, getColor(rgbHex, alpha), borderWidth);
    }

    /**
     * Add a border to a given image
     * 
     * @param inputStream
     *            the input image data stream
     * @param outputStream
     *            the stream to which the image output will be written
     * @param animated
     *            whether or not the image is animated (has multiple frames)
     * @param borderMode
     *            the desired border mode
     * @param color
     *            the color to use when drawing the border
     * @param borderWidth
     *            the border width in pixels
     */
    public void addBorderToImage(InputStream inputStream, OutputStream outputStream, boolean animated, BorderMode borderMode, Color color, int borderWidth)
            throws java.io.IOException {
        if (animated) {
            addBorderToAnimatedImage(inputStream, outputStream, borderMode, color, borderWidth);
        } else {
            addBorderToStaticImage(inputStream, outputStream, borderMode, color, borderWidth);
        }
    }

    /**
     * Add a border to a given static (non-animated) image
     * 
     * @param inputStream
     *            the input image data stream
     * @param outputStream
     *            the stream to which the image output will be written
     * @param borderMode
     *            the desired border mode
     * @param color
     *            the color to use when drawing the border
     * @param borderWidth
     *            the border width in pixels
     */
    void addBorderToStaticImage(InputStream inputStream, OutputStream outputStream, BorderMode borderMode, Color color, int borderWidth) throws java.io.IOException {
        ImageInputStream imageInputStream = ImageIO.createImageInputStream(inputStream);
        try {
            Iterator<ImageReader> iter = ImageIO.getImageReaders(imageInputStream);
            if (!iter.hasNext()) {
                throw new IllegalArgumentException("No image readers available to read the given input");
            }
            ImageReader imageReader = iter.next(); // take the first available
            try {
                imageReader.setInput(imageInputStream);
                BufferedImage bi = imageReader.read(0, null);
                drawBorder(bi, bi.getWidth(), bi.getHeight(), borderMode, color, borderWidth);
                ImageIO.write(bi, imageReader.getFormatName(), outputStream);
            } finally {
                imageReader.dispose();
            }
        } finally {
            imageInputStream.close();
        }
    }

    /**
     * Add a border to a given animated image
     * 
     * @param inputStream
     *            the input image data stream
     * @param outputStream
     *            the stream to which the image output will be written
     * @param borderMode
     *            the desired border mode
     * @param color
     *            the color to use when drawing the border
     * @param borderWidth
     *            the border width in pixels
     */
    void addBorderToAnimatedImage(InputStream inputStream, OutputStream outputStream, BorderMode borderMode, Color color, int borderWidth) throws java.io.IOException {
        ImageInputStream imageInputStream = ImageIO.createImageInputStream(inputStream);
        ImageReader imageReader = null;
        ImageOutputStream imageOutputStream = null;
        ImageWriter imageWriter = null;
        
        try {
            Iterator<ImageReader> iter = ImageIO.getImageReaders(imageInputStream);
            if (!iter.hasNext()) {
                throw new IllegalArgumentException("No image readers available to read the given input");
            }
            imageReader = iter.next(); // take the first available
            imageReader.setInput(imageInputStream);

            String format = imageReader.getFormatName();
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("Reading image data of format=" + format);
            }

            // If image stream metadata is available, use it to get the
            // overall width and height as the per-frame values for
            // transparent GIFs can be incorrect.
            int overallWidth = 0;
            int overallHeight = 0;
            IIOMetadata meta = imageReader.getStreamMetadata();
            if (meta != null) {
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.fine("Reading meta data");
                }
                Node node = meta.getAsTree("javax_imageio_1.0");
                if (node != null) {
                    XPathFactory factory = XPathFactory.newInstance();
                    XPath xpath = factory.newXPath();
                    try {
                        Node out = (Node) xpath.compile("//HorizontalScreenSize").evaluate(node, XPathConstants.NODE);
                        overallWidth = Integer.parseInt(out.getAttributes().getNamedItem("value").getNodeValue());
                        out = (Node) xpath.compile("//VerticalScreenSize").evaluate(node, XPathConstants.NODE);
                        overallHeight = Integer.parseInt(out.getAttributes().getNamedItem("value").getNodeValue());
                        if (LOG.isLoggable(Level.FINE)) {
                            LOG.fine("Overall size: " + overallWidth + "x" + overallHeight);
                        }
                    } catch (Exception e) {
                        LOG.log(Level.WARNING, "Failed to read screen size from image metadata", e);
                    }
                }
            }

            imageOutputStream = ImageIO.createImageOutputStream(outputStream);
            imageWriter = ImageIO.getImageWriter(imageReader);
            imageWriter.setOutput(imageOutputStream);
            imageWriter.prepareWriteSequence(meta);

            // Loop through images (i.e. animated gif)
            int numFrames = processImages(borderMode, color, borderWidth, imageReader, imageWriter, format, overallWidth, overallHeight);

            imageWriter.endWriteSequence();

            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("Wrote " + numFrames + " frame(s)");
            }
        } finally {
            imageInputStream.close();
            if (imageReader!=null){
                imageReader.dispose();
            }
            if (imageOutputStream!=null){
                imageOutputStream.flush();
                imageOutputStream.close();
            }
            if (imageWriter!=null){
                imageWriter.dispose();
            }
        }
    }

    private int processImages(BorderMode borderMode, Color color, int borderWidth, ImageReader imageReader, ImageWriter imageWriter, 
                              String format, int overallWidth, int overallHeight) throws IOException {
        int numFrames = 0;
        int width = 0;
        int height = 0;
        boolean allFramesProcessed = false; 
        while(!allFramesProcessed) {
            if (LOG.isLoggable(Level.FINER)) {
                LOG.finer("Reading frame " + (numFrames + 1));
            }
            BufferedImage bi = null;
            IIOMetadata imageMetadata = null;
            try {
                if ("GIF".equalsIgnoreCase(format)) {
                    IIOImage image = imageReader.readAll(numFrames, null);
                    bi = (BufferedImage) image.getRenderedImage();
                    imageMetadata = image.getMetadata();
                } else {
                    bi = imageReader.read(numFrames);
                }
            } catch (IndexOutOfBoundsException e) {
                allFramesProcessed = true;
            }

            if (!allFramesProcessed){
                // MAD-1071: Basing border on first frame due to GIF
                // issue.
                if (numFrames <= 0) {
                    width = Math.max(overallWidth, bi.getWidth());
                    height = Math.max(overallHeight, bi.getHeight());
                }
   
                if (LOG.isLoggable(Level.FINER)) {
                    LOG.finer("Adding " + width + "x" + height + " border to frame " + (numFrames + 1));
                }
   
                drawBorder(bi, width, height, borderMode, color, borderWidth);
   
                imageWriter.writeToSequence(new IIOImage(bi, null, imageMetadata), null);
                ++numFrames;
            }
        }
        return numFrames;
    }

    /**
     * Convert an RGB hex string and an alpha value into a Color for use with
     * java.awt.Graphics operations
     */
    static Color getColor(String rgbHex, float alpha) {
        int rgb = Integer.parseInt(rgbHex, 16);
        int rgba = rgb | ((int) (alpha * 255) << 24);
        return new Color(rgba, true);
    }

    /**
     * Draw a border on a BufferedImage
     */
    static void drawBorder(BufferedImage bi, int width, int height, BorderMode borderMode, Color color, int borderWidth) {
        Graphics2D g = bi.createGraphics();
        int thickness;
        try {
            if (borderMode == BorderMode.AVERAGE){
                int avgWithRGB = color.getRGB();
                int avgWithR = (avgWithRGB & 0xFF0000) >> 16;
                int avgWithG = (avgWithRGB & 0xFF00) >> 8;
                int avgWithB = avgWithRGB & 0xFF;
                int alpha = color.getAlpha();
                // Top & bottom rows
                drawTopButtonRows(bi, width, height, borderWidth, g, avgWithR, avgWithG, avgWithB, alpha);
                // Left & right sides
                drawLeftRightSides(bi, width, height, borderWidth, g, avgWithR, avgWithG, avgWithB, alpha);
            }else{
                g.setColor(color);
                thickness = Math.min(Math.min(borderWidth, width), height);
                for (int k = 0; k < thickness; ++k) {
                    g.drawRect(k, k, width - 1 - (2 * k), height - 1 - (2 * k));
                }
            }
        } finally {
            g.dispose();
        }
    }

    private static void drawTopButtonRows(BufferedImage bi, int width, int height, int borderWidth, Graphics2D g, int avgWithR, int avgWithG, int avgWithB, int alpha) {
        int thickness;
        thickness = Math.min(borderWidth, height);
        for (int x = 0; x < width; ++x) {
            for (int y = 0; y < thickness; ++y) {
                average(bi, g, x, y, avgWithR, avgWithG, avgWithB, alpha);
            }
            for (int y = height - thickness; y < height; ++y) {
                average(bi, g, x, y, avgWithR, avgWithG, avgWithB, alpha);
            }
        }
    }
    
    private static void drawLeftRightSides(BufferedImage bi, int width, int height, int borderWidth, Graphics2D g, int avgWithR, int avgWithG, int avgWithB, int alpha) {
        int thickness;
        thickness = Math.min(borderWidth, width);
        for (int y = borderWidth; y < (height - borderWidth); ++y) {
            for (int x = 0; x < thickness; ++x) {
                average(bi, g, x, y, avgWithR, avgWithG, avgWithB, alpha);
            }
            for (int x = width - thickness; x < width; ++x) {
                average(bi, g, x, y, avgWithR, avgWithG, avgWithB, alpha);
            }
        }
    }

    static void average(BufferedImage bi, Graphics2D g, int x, int y, int avgWithR, int avgWithG, int avgWithB, int alpha) {
        int pixelRGB = bi.getRGB(x, y);
        int pixelR = (pixelRGB & 0xFF0000) >> 16;
                                int pixelG = (pixelRGB & 0xFF00) >> 8;
                                int pixelB = pixelRGB & 0xFF;
                                int avgR = (pixelR + avgWithR) / 2;
                                int avgG = (pixelG + avgWithG) / 2;
                                int avgB = (pixelB + avgWithB) / 2;
                                /*
         * if (LOG.isLoggable(Level.FINER)) { LOG.finer("Average of " + pixelR +
         * "," + pixelG + "," + pixelB + " and " + avgWithR + "," + avgWithG +
         * "," + avgWithB + " == " + avgR + "," + avgG + "," + avgB); }
         */
                                g.setColor(new Color(avgR, avgG, avgB, alpha));
                                g.drawLine(x, y, x, y);
    }

    public ByteArrayOutputStream overlayTextOnBGImage(String text, Font font, InputStream background, String format) throws java.io.IOException {
        BufferedImage image = ImageIO.read(background);
        Graphics2D g2D = image.createGraphics();
        RenderingHints rh = new RenderingHints(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_GASP);
        g2D.setRenderingHints(rh);
        g2D.setFont(font);
        g2D.setColor(Color.BLACK);
        // FontMetrics metrics=g2D.getFontMetrics();
        g2D.drawString(text, 5, image.getHeight() / 2 + 4);
        g2D.dispose();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(image, format, out);
        return out;
    }
}
