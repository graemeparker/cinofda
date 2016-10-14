package com.adfonic.email;

/**
 * Class that exposes the version of the API. Fetches the
 * "Implementation-Version" manifest attribute from the jar file.
 *
 * <p>Note that some ClassLoaders do not expose the package metadata,
 * hence this class might not be able to determine the version
 * in all environments.
 * 
 *
 * @author Antony Sohal
 * @since 1.5.0
 */
public class Version {
    
    private Version(){
    }

    /**
     * Return the full version string of the present API codebase,
     * or <code>null</code> if it cannot be determined.
     * @see java.lang.Package#getImplementationVersion()
     */
    public static String getVersion() {
        Package pkg = Version.class.getPackage();
        return pkg != null ? pkg.getImplementationVersion() : null;
    }

}
