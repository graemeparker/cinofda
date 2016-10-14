package com.adfonic.presentation.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.faces.context.FacesContext;

import org.apache.commons.io.IOUtils;
import org.springframework.core.io.Resource;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.jsf.FacesContextUtils;

public final class FileUtils {
    
    private static final transient Logger LOG = Logger.getLogger(FileUtils.class.getName());
    
    private FileUtils() {
    }
    
    public static byte[] getResourceBytes(String relativePath) {
        byte[] bytes = null;

        InputStream in = null;
        try {
            in = getResource(relativePath).getInputStream();
            bytes = IOUtils.toByteArray(in);
        } catch (IOException e) {
            LOG.log(Level.SEVERE, "Can not load bytes from resource " + relativePath, e);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    LOG.log(Level.WARNING, "Error closing inputstream", e);
                }
            }
        }

        return bytes;
    }
    
    public static Resource getResource(String relativePath) {
        WebApplicationContext appContext = FacesContextUtils.getWebApplicationContext(FacesContext.getCurrentInstance());
        String path = "file:" + appContext.getServletContext().getRealPath("/") + relativePath;
        return appContext.getResource(path);
    }
}
