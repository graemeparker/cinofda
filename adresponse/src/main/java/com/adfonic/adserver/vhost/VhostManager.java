package com.adfonic.adserver.vhost;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.servlet.http.HttpServletRequest;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.context.support.WebApplicationObjectSupport;

import com.adfonic.adserver.Constant;
import com.adfonic.util.FileUpdateMonitor;

/**
 * Virtual host manager. This component uses an XML config file that defines any
 * number of virtual hosts, including a catch-all. It uses JAXB to process the
 * XML. The respective binding classes were generated from the XML via:
 *
 * 1) Download and unzip "trang" from: http://code.google.com/p/jing-trang/,
 * i.e.: wget http://jing-trang.googlecode.com/files/trang-20091111.zip 2)
 * Generate the XSD from XML, i.e.: java -jar trang.jar adserver-vhosts.xml
 * adserver-vhosts.xsd 3) Generate java source code from the XSD, i.e.: xjc -p
 * com.adfonic.adserver.vhost adserver-vhosts.xsd
 */
@Component
public class VhostManager extends WebApplicationObjectSupport {

    private static final transient Logger LOG = Logger.getLogger(VhostManager.class.getName());

    private static final int HTTP_PORT = 80;

    @Value("${VhostManager.configFile}")
    private File configFile;
    @Value("${PreProcessor.checkForUpdatesPeriodSec}")
    private int checkForUpdatesPeriodSec;
    private FileUpdateMonitor configFileUpdateMonitor;
    final AtomicReference<Config> configRef = new AtomicReference<Config>();

    static class Config {
        private final Map<String, Vhost> vhostsByName;
        private final Vhost catchAllVhost;

        public Config(Map<String, Vhost> vhostsByName, Vhost catchAllVhost) {
            if (catchAllVhost == null) {
                throw new IllegalArgumentException("catchAllVhost must not be null");
            }
            this.vhostsByName = vhostsByName;
            this.catchAllVhost = catchAllVhost;
        }

        public Vhost getVhost(HttpServletRequest request) {
            Vhost vhost = vhostsByName.get(request.getServerName());
            return vhost != null ? vhost : catchAllVhost;
        }
    }

    @PostConstruct
    public void initialize() throws javax.xml.bind.JAXBException {

        reloadConfig();

        configFileUpdateMonitor = new FileUpdateMonitor(configFile, checkForUpdatesPeriodSec, new Runnable() {
            @Override
            public void run() {
                LOG.info("Reloading Config");
                try {
                    reloadConfig();
                } catch (javax.xml.bind.JAXBException e) {
                    LOG.log(Level.SEVERE, "Failed to reload config", e);
                }
            }
        });
        configFileUpdateMonitor.start();
    }

    @PreDestroy
    public void destroy() {
        if (configFileUpdateMonitor != null) {
            configFileUpdateMonitor.stop();
        }
    }

    public StringBuilder getBeaconBaseUrl(HttpServletRequest request) {
        return getBeaconBaseUrl(request, false);
    }

    /**
     * Get the beacon base URL that should be used with the given request host
     * 
     * @param request
     *            the current request
     * @return the base base beacon URL for the given request
     */
    public StringBuilder getBeaconBaseUrl(HttpServletRequest request, boolean httpsRequired) {
        Vhost vhost = configRef.get().getVhost(request);
        if (StringUtils.isNotEmpty(vhost.getBeaconBaseUrl())) {
            return new StringBuilder(vhost.getBeaconBaseUrl());
        } else {
            // We can serve our own beacons from this server using the Host header
            return makeBaseUrl(request, Constant.BEACON_URI_PATH, httpsRequired);
        }
    }

    /**
     * Get the click redirect base URL that should be used with the given
     * request host
     * 
     * @param request
     *            the current request
     * @return the base click redirect URL for the given request
     */
    public StringBuilder getClickRedirectBaseUrl(HttpServletRequest request) {
        Vhost vhost = configRef.get().getVhost(request);
        if (StringUtils.isNotEmpty(vhost.getClickRedirectBaseUrl())) {
            return new StringBuilder(vhost.getClickRedirectBaseUrl());
        } else {
            // We can serve our own clickRedirects from this server using the Host header
            return makeBaseUrl(request, Constant.CLICK_REDIRECT_PATH, false);
        }
    }

    /**
     * Get the click base URL that should be used with the given request host
     * 
     * @param request
     *            the current request
     * @return the hostname to be used on click URLs
     */
    public StringBuilder getClickBaseUrl(HttpServletRequest request) {
        Vhost vhost = configRef.get().getVhost(request);
        if (StringUtils.isNotEmpty(vhost.getClickBaseUrl())) {
            return new StringBuilder(vhost.getClickBaseUrl());
        } else {
            // We can serve our own clicks from this server using the Host header
            return makeBaseUrl(request, Constant.CLICK_THROUGH_PATH, false);
        }
    }

    /**
     * Get the asset base URL that should be used with the given request host
     * 
     * @param request
     *            the current request
     * @return the hostname to be used on asset URLs
     */
    public String getAssetBaseUrl(HttpServletRequest request) {
        // assetBaseUrl is always defined on every vhost, so just return that
        return configRef.get().getVhost(request).getAssetBaseUrl();
    }

    /**
     * Reload the XML config file and cache the configuration
     */
    void reloadConfig() throws javax.xml.bind.JAXBException {
        LOG.info("Reading " + configFile.getAbsolutePath());

        // Create the JAXB Unmarshaller for this vhosts package
        Unmarshaller unmarshaller = JAXBContext.newInstance(this.getClass().getPackage().getName()).createUnmarshaller();

        // Process each Vhost in the config
        Map<String, Vhost> vhostsByName = new HashMap<String, Vhost>();
        Vhost catchAllVhost = null;
        for (Vhost vhost : ((Vhosts) unmarshaller.unmarshal(configFile)).getVhost()) {
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("Processing Vhost name=" + StringUtils.defaultIfEmpty(vhost.getName(), "<catchAll>") + ", beaconBaseUrl="
                        + StringUtils.defaultIfEmpty(vhost.getBeaconBaseUrl(), "<request>") + ", clickBaseUrl=" + StringUtils.defaultIfEmpty(vhost.getClickBaseUrl(), "<request>")
                        + ", clickRedirectBaseUrl=" + StringUtils.defaultIfEmpty(vhost.getClickRedirectBaseUrl(), "<request>") + ", assetBaseUrl=" + vhost.getAssetBaseUrl());
            }

            // assetBaseUrl must be set
            if (StringUtils.isBlank(vhost.getAssetBaseUrl())) {
                throw new IllegalArgumentException("assetBaseUrl not defined on vhost (name=" + vhost.getName() + ")");
            }

            if (StringUtils.isBlank(vhost.getName())) {
                // This is a catch-all...make sure one and only one is defined
                if (catchAllVhost != null) {
                    throw new IllegalStateException("Multiple catch-all vhosts defined in " + configFile.getAbsolutePath());
                }
                catchAllVhost = vhost;
            } else {
                if (vhostsByName.containsKey(vhost.getName())) {
                    LOG.warning("Multiple vhosts defined for name=" + vhost.getName());
                    // Allow it...last defined wins
                }
                vhostsByName.put(vhost.getName(), vhost);
            }
        }

        if (catchAllVhost == null) {
            throw new IllegalStateException("A catch-all vhost is required but was not defined");
        }

        // Replace the atomic reference config
        configRef.set(new Config(vhostsByName, catchAllVhost));
    }

    public static StringBuilder makeBaseUrl(HttpServletRequest request) {
        return makeBaseUrl(request, null, false);
    }

    public static StringBuilder makeBaseUrl(HttpServletRequest request, boolean httpsRequired) {
        return makeBaseUrl(request, null, httpsRequired);
    }

    public static StringBuilder makeBaseUrl(HttpServletRequest request, String baseUri) {
        return makeBaseUrl(request, baseUri, false);
    }

    public static StringBuilder makeBaseUrl(HttpServletRequest request, String baseUri, boolean httpsRequired) {
        StringBuilder bld = new StringBuilder();
        if (httpsRequired) {
            bld.append("https");
        } else {
            bld.append(request.getScheme());
        }
        bld.append("://").append(request.getServerName());

        if ("https".equals(request.getScheme()) && request.getServerPort() != 443) {
            //copy custom https port allways
            bld.append(':').append(String.valueOf(request.getServerPort()));
        } else if (request.getServerPort() != HTTP_PORT && !httpsRequired) {
            //copy custom http port only when we do not enforse https
            bld.append(':').append(String.valueOf(request.getServerPort()));
        }
        bld.append(request.getContextPath());
        if (baseUri != null) {
            bld.append(baseUri);
        }
        return bld;
    }
}
