package com.adfonic.util.status;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import org.slf4j.LoggerFactory;

/**
 *
 * @author mvanek
 *
 */
public class ArtifactInfo {

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(AppInfoServlet.class.getName());

    private final String name;

    private final String version;
    
    public ArtifactInfo(String name, String version) {
        this.name = name;
        this.version = version;
    }

    public static ArtifactInfo getByClassName(String className) {
        try {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            if (classLoader == null) {
                classLoader = ArtifactInfo.class.getClassLoader();
            }
            Class<?> klass = Class.forName(className, false, classLoader);
            return getByPackageName(klass.getPackage().getName());
        } catch (ClassNotFoundException cnfx) {
            LOG.warn("Class not found: '" + className + "'");
        }
        return null;
    }

    public static ArtifactInfo getByPackageName(String packageName) {
        Package pckg = Package.getPackage(packageName);
        if (pckg != null) {
            String version = pckg.getImplementationVersion();
            if (version == null) {
                version = pckg.getSpecificationVersion();
            }
            String name = pckg.getImplementationTitle();
            if (name == null) {
                name = pckg.getSpecificationTitle();
            }
            if (name != null || version != null) {
                return new ArtifactInfo(name, version);
            }
        } else {
            LOG.warn("MANIFEST not found by package: " + packageName);
        }
        return null;
    }

    public static ArtifactInfo getByManifest(Manifest manifest) {
        Attributes attributes = manifest.getMainAttributes();
        String version = attributes.getValue("Implementation-Version");
        if (version == null) {
            version = attributes.getValue("Specification-Version");
            if (version == null) {
                version = attributes.getValue("Bundle-Version");
            }
        }
        String name = attributes.getValue("Implementation-Title");
        if (name == null) {
            name = attributes.getValue("Specification-Title");
            if (name == null) {
                name = attributes.getValue("Bundle-Name");
            }
        }
        if (name != null || version != null) {
            return new ArtifactInfo(name, version);
        } else {
            return null;
        }
    }

    public String getName() {
        return name;
    }

    public String getVersion() {
        return version;
    }

    public static ArtifactInfo getByBuildProperties(String propertiesResource) {
        return getByBuildProperties(propertiesResource, "build.artifactId", "build.version");
    }

    public static ArtifactInfo getByBuildProperties(String propertiesResource, String nameProperty, String versionProperty) {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        if (classLoader == null) {
            classLoader = ArtifactInfo.class.getClassLoader();
        }
        InputStream stream = classLoader.getResourceAsStream(propertiesResource);
        if (stream != null) {
            Properties properties = new Properties();
            try {
                properties.load(stream);
            } catch (IOException iox) {
                LOG.warn("Properties resource load failed: " + propertiesResource, iox);
            }
            String version = properties.getProperty(versionProperty);
            String name = properties.getProperty(nameProperty);
            if (name != null || version != null) {
                return new ArtifactInfo(name, version);
            }

        } else {
            LOG.warn("Properties resource not found: " + propertiesResource);
        }
        return null;
    }
}
