package com.adfonic.util;

import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.stream.ImageInputStream;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.w3c.dom.Node;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Metadata;
import com.drew.metadata.MetadataException;
import com.drew.metadata.jpeg.JpegDirectory;

/**
 * Some utilities for dealing with "media"
 * 
 * @author jdewald
 * @author wbiggs
 *
 */
public class MediaUtils {
    private static final transient Logger LOG = Logger.getLogger(MediaUtils.class.getName());
    
    private MediaUtils(){
    }

    public static class ImageInfo {
        private String formatName = null;
        private int numFrames = 0;
        private int width = 0;
        private int height = 0;

        public String getFormatName() {
            return formatName;
        }

        public int getNumFrames() {
            return numFrames;
        }

        public int getWidth() {
            return width;
        }

        public int getHeight() {
            return height;
        }

        public boolean isAnimated() {
            return numFrames > 1;
        }

        @Override
        public String toString() {
            return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).append("formatName", formatName).append("numFrames", numFrames).append("width", width)
                    .append("height", height).toString();
        }
    }

    public static ImageInfo getImageInfo(byte[] imageData) throws IOException {
        try {
            return getImageInfoUsingJAI(new ByteArrayInputStream(imageData));
        } catch (Exception e) {
            // AO-177 - Try falling back on reading metadata...which works in
            // the
            // case of some funky JPEG files
            LOG.warning("Failed to get ImageInfo using JAI (message=" + e.getMessage() + "), falling back on metadata");
            try {
                return getImageInfoUsingMetadata(new ByteArrayInputStream(imageData));
            } catch (Exception e2) {
                throw new IllegalStateException("Can't get metadata from image input stream", e2);
            }
        }
    }

    public static ImageInfo getImageInfo(InputStream inputStream) throws IOException {
        // Since we may need to read from the stream twice, we need to copy the
        // contents of the image into memory. We're making the assumption that
        // the front end has limited the upload max file size, so we don't get
        // screwed and run out of memory, but I think we can live with that in
        // order to keep life simple here...
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        IOUtils.copy(inputStream, baos);
        return getImageInfo(baos.toByteArray());
    }

    static ImageInfo getImageInfoUsingJAI(InputStream inputStream) throws IOException {
        ImageInputStream imageInputStream = ImageIO.createImageInputStream(inputStream);
        try {
            Iterator<ImageReader> iter = ImageIO.getImageReaders(imageInputStream);
            if (!iter.hasNext()) {
                throw new IllegalArgumentException("No image readers available to read the given input");
            }

            ImageReader imageReader = iter.next(); // take the first available
            try {
                imageReader.setInput(imageInputStream);

                ImageInfo imageInfo = new ImageInfo();
                imageInfo.formatName = imageReader.getFormatName().toUpperCase();

                // If image stream metadata is available, use it to get the
                // overall width and height as the per-frame values for
                // transparent GIFs can be incorrect.
                getWidhtHeightPerFrame(imageReader, imageInfo);

                // Loop through images (i.e. animated gif)
                processImages(imageReader, imageInfo);
                
                return imageInfo;
            } finally {
                imageReader.dispose();
            }
        } finally {
            imageInputStream.close();
        }
    }

    private static void processImages(ImageReader imageReader, ImageInfo imageInfo) throws IOException {
        
        boolean allImagesProcessed = false;
        int k = 0;
        while (!allImagesProcessed) {
            BufferedImage image;
            try {
                image = imageReader.read(k);
                ++imageInfo.numFrames;
                if (image.getWidth() > imageInfo.width) {
                    imageInfo.width = image.getWidth();
                }
                if (image.getHeight() > imageInfo.height) {
                    imageInfo.height = image.getHeight();
                }
                ++k;
            } catch (IndexOutOfBoundsException e) {
                allImagesProcessed = true;
            }
        }
    }

    private static void getWidhtHeightPerFrame(ImageReader imageReader, ImageInfo imageInfo) throws IOException {
        IIOMetadata meta = imageReader.getStreamMetadata();
        if (meta != null) {
            Node node = meta.getAsTree("javax_imageio_1.0");
            if (node != null) {
                XPathFactory factory = XPathFactory.newInstance();
                XPath xpath = factory.newXPath();
                try {
                    XPathExpression expr = xpath.compile("//HorizontalScreenSize");
                    Node out = (Node) expr.evaluate(node, XPathConstants.NODE);
                    String val = out.getAttributes().getNamedItem("value").getNodeValue();
                    imageInfo.width = Integer.parseInt(val);
                    expr = xpath.compile("//VerticalScreenSize");
                    out = (Node) expr.evaluate(node, XPathConstants.NODE);
                    val = out.getAttributes().getNamedItem("value").getNodeValue();
                    imageInfo.height = Integer.parseInt(val);
                } catch (Exception e) {
                    LOG.log(Level.WARNING, "Failed to read screen size from image metadata", e);
                }
            }
        }
    }

    /**
     * AO-177 - fallback method of reading a JPEG's ImageInfo when the image is
     * "corrupt" in some way that causes JAI to trip over it.
     */
    static ImageInfo getImageInfoUsingMetadata(InputStream inputStream) throws IOException, ImageProcessingException, MetadataException {
        Metadata metadata = ImageMetadataReader.readMetadata(new BufferedInputStream(inputStream), true);
        JpegDirectory jpegDirectory = metadata.getDirectory(JpegDirectory.class);
        if (jpegDirectory == null) {
            throw new IllegalStateException("Can't read metadata from non-JPEG");
        }

        ImageInfo imageInfo = new ImageInfo();
        imageInfo.formatName = "JPEG";
        imageInfo.numFrames = 1;
        imageInfo.width = jpegDirectory.getImageWidth();
        imageInfo.height = jpegDirectory.getImageHeight();
        return imageInfo;
    }
}
