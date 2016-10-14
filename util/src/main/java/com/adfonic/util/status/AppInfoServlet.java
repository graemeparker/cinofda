package com.adfonic.util.status;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.Date;
import java.util.jar.Manifest;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.LoggerFactory;

public class AppInfoServlet extends HttpServlet {

    private static final String RR_PARAM_NAME = "registry-key-name";

    public static final String RR_DEFAULT_KEY = "resource-registry-default-context-key";

    private final org.slf4j.Logger log = LoggerFactory.getLogger(AppInfoServlet.class);

    private static final long serialVersionUID = 1L;

    private static final String UNKNOWN = "unknown";

    private ArtifactInfo artfInfo;

    private ResourceRegistry<?> registry;

    @Override
    public void destroy() {
        super.destroy();
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        String applicationPackage = config.getInitParameter("application-package");
        if (applicationPackage != null) {
            artfInfo = ArtifactInfo.getByPackageName(applicationPackage);
        }
        String applicationClass = config.getInitParameter("application-class");
        if (artfInfo == null && applicationClass != null) {
            artfInfo = ArtifactInfo.getByClassName(applicationPackage);
        }
        String propertiesResource = config.getInitParameter("build-properties");
        if (artfInfo == null && propertiesResource != null) {
            artfInfo = ArtifactInfo.getByBuildProperties(propertiesResource);
        }
        if (artfInfo == null) {
            artfInfo = getByServletContext(config.getServletContext());
        }
        if (artfInfo == null) {
            log.warn("ArtifactInfo not created");
            artfInfo = new ArtifactInfo("unknown", "unknown");
        }

        ServletContext servletContext = config.getServletContext();

        String rrServletInitKey = config.getInitParameter(RR_PARAM_NAME);
        if (rrServletInitKey != null) {
            ResourceRegistry<?> registry = (ResourceRegistry<?>) servletContext.getAttribute(rrServletInitKey);
            if (registry != null) {
                log.debug("ResourceRegistry found in ServletContext by ServletConfig InitParameter: " + rrServletInitKey);
                this.registry = registry;
            } else {
                log.warn("ResourceRegistry not found in ServletContext by ServletConfig InitParameter: " + rrServletInitKey);
            }
        }

        String rrContextInitKey = servletContext.getInitParameter(RR_PARAM_NAME);
        if (rrContextInitKey != null) {
            ResourceRegistry<?> registry = (ResourceRegistry<?>) servletContext.getAttribute(rrContextInitKey);
            if (registry != null) {
                log.debug("ResourceRegistry found in ServletContext by ServletContext InitParameter: " + rrContextInitKey);
                this.registry = registry;
            } else {
                this.registry = new ResourceRegistry();
                servletContext.setAttribute(rrContextInitKey, this.registry);
                log.debug("ResourceRegistry added into ServletContext by ServletContext InitParameter: " + rrContextInitKey);
            }
        }

        //Lastly try default value
        if (registry == null) {
            ResourceRegistry<?> registry = (ResourceRegistry<?>) servletContext.getAttribute(RR_DEFAULT_KEY);
            if (registry != null) {
                log.debug("ResourceRegistry found in ServletContext by default: " + RR_DEFAULT_KEY);
                this.registry = registry;
            } else {
                this.registry = new ResourceRegistry();
                servletContext.setAttribute(RR_DEFAULT_KEY, this.registry);
                log.debug("ResourceRegistry added into ServletContext by default: " + RR_DEFAULT_KEY);
            }
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String contentType = req.getHeader("Content-Type");
        if (contentType != null && contentType.startsWith("application/json")) {
            //do nothing
        } else {
            PrintWriter writer = resp.getWriter();
            writer.println("host = " + ResourceRegistry.getLocalHostname());
            writer.println("name = " + artfInfo.getName());
            writer.println("version = " + artfInfo.getVersion());
            writer.println("timestamp = " + new Date());
            if (this.registry != null) {
                for (ResourceStatus status : this.registry) {
                    Serializable id = status.getResource().getId();
                    boolean works = status.isFine();
                    writer.println(id + ".works = " + works);
                    writer.println(id + ".millis = " + status.getCheckMillis());
                    String message = status.getMessage();
                    if (message != null) {
                        writer.println(id + ".message = " + message);
                    }
                    if (!works) {
                        writer.println(id + ".exception = " + status.getException());
                    }
                }
            }
        }

    }

    public ArtifactInfo getByServletContext(ServletContext context) {
        ArtifactInfo artifactInfo = null;
        InputStream ctxStream = context.getResourceAsStream("/META-INF/MANIFEST.MF");
        if (ctxStream != null) {
            try {
                artifactInfo = ArtifactInfo.getByManifest(new Manifest(ctxStream));
            } catch (IOException iox) {
                log.warn("Cannot load /META-INF/MANIFEST.MF " + iox);
            }
        } else {
            log.debug("MANIFEST not found in ServletContext");
        }

        if (artifactInfo == null && context.getServletContextName() != null) {
            artifactInfo = new ArtifactInfo(context.getServletContextName(), UNKNOWN);
        }

        return artifactInfo;
    }

}
