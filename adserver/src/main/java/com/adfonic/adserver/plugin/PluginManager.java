package com.adfonic.adserver.plugin;

public interface PluginManager {
    /**
     * Get a Plugin by name.  The supplied name may either be the short prefix
     * style (i.e. "smaato") or the full bean name (i.e. "smaatoPlugin").
     * This method is case-insensitive, so you may pass "IAm" or "iam" and
     * the results will be identical.
     * @param name the name of the plugin
     * @return the plugin if found, otherwise null
     */
    Plugin getPluginByName(String name);

    /**
     * Get a Plugin by type.  This will iterate through all mapped
     * Plugins and return the first one it finds of the given class.
     * @param clazz the Plugin class to look for
     * @return the Plugin if found, otherwise null
     */
    <T extends Plugin> T getPluginByType(Class<T> clazz);
}
