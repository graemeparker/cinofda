package com.adfonic.adserver.plugin;

import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PluginManagerImpl implements PluginManager {
    private static final transient Logger LOG = Logger.getLogger(PluginManagerImpl.class.getName());

    private final Map<String,Plugin> plugins = new TreeMap<String,Plugin>();
    
    @Autowired
    public PluginManagerImpl(Map<String,Plugin> plugins) {
        // Convert the autowired map to use case-insensitive, shortened keys
        for (Map.Entry<String,Plugin> entry : plugins.entrySet()) {
            String name = entry.getKey().toLowerCase();
            Plugin plugin = entry.getValue();
            this.plugins.put(name, plugin);
            
            // Also store it in short form, i.e. "smaatoInterface" -> "smaato"
            if (name.endsWith("plugin")) {
                name = name.substring(0, name.length() - "Plugin".length());
                this.plugins.put(name, plugin);
            }
        }

        if (LOG.isLoggable(Level.FINE)) {
            // Log what we've got
            for (Map.Entry<String,Plugin> entry : this.plugins.entrySet()) {
                LOG.fine(entry.getKey() + " => " + entry.getValue().getClass().getName());
            }
        }
    }

    /** @{inheritDoc} */
    public Plugin getPluginByName(String name) {
        name = name.toLowerCase();
        
        // First see if the supplied name corresponds to an entry in the map
        Plugin plugin = plugins.get(name);
        if (plugin != null) {
            return plugin;
        }

        return null;
    }

    /** @{inheritDoc} */
    @SuppressWarnings("unchecked")
	public <T extends Plugin> T getPluginByType(Class<T> clazz) {
        for (Plugin plugin : plugins.values()) {
            if (clazz.isInstance(plugin)) {
                return (T)plugin;
            }
        }
        return null;
    }
}
