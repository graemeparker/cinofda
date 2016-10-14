package com.adfonic.adserver.plugin;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import au.com.bytecode.opencsv.CSVReader;

import com.adfonic.adserver.TargetingContext;
import com.adfonic.domain.cache.dto.adserver.AdserverPluginDto;
import com.adfonic.domain.cache.dto.adserver.adspace.PublicationDto;
import com.adfonic.util.FileUpdateMonitor;

@Component
public class PluginCategoryManager {
    private static final transient Logger LOG = Logger.getLogger(PluginCategoryManager.class.getName());
    @Value("${PluginCategoryManager.dataFile}")
    private File dataFile;
    @Value("${PluginCategoryManager.checkForUpdatesPeriodSec}")
    private int checkForUpdatesPeriodSec;
    private final AtomicReference<Map<String,Map<Long,String>>> cacheRef = new AtomicReference<Map<String,Map<Long,String>>>();
    private FileUpdateMonitor fileUpdateMonitor;

    @PostConstruct
    public void initialize() throws java.io.IOException {
        reloadData();
        
        // Start up a file update monitor to watch for changes to the data file
        fileUpdateMonitor = new FileUpdateMonitor(dataFile, checkForUpdatesPeriodSec, new Runnable() {
                public void run() {
                    try {
                        reloadData();
                    } catch (java.io.IOException e) {
                        LOG.log(Level.SEVERE, "Failed to reload data", e);
                    }
                }
            });
        fileUpdateMonitor.start();
    }

    @PreDestroy
    public void destroy() {
        if (fileUpdateMonitor != null) {
            fileUpdateMonitor.stop();
        }
    }
    
    private void reloadData() throws java.io.IOException {
        if (LOG.isLoggable(Level.INFO)) {
            LOG.info("Reloading data from " + dataFile.getCanonicalPath());
        }
        FileReader fileReader = new FileReader(dataFile);
        CSVReader reader = null;
        try {
            Map<String,Map<Long,String>> cache = new HashMap<String,Map<Long,String>>();
            
            reader = new CSVReader(fileReader);
            // ID,Adfonic (reference only),<pluginSystemName>,<pluginSystemName>,...
            // Read in the header line
            String[] line = reader.readNext();

            // Dynamically read in the plugin system name for each position
            Map<Integer,String> pluginSystemNameByPosition = new HashMap<Integer,String>();
            for (int k = 2; k < line.length; ++k) {
                String pluginSystemName = line[k];
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.fine("Position " + k + " is " + pluginSystemName);
                }
                pluginSystemNameByPosition.put(k, pluginSystemName);
            }

            // Now read in the data
            int count = 0;
            while ((line = reader.readNext()) != null) {
                ++count;
                long id = Long.parseLong(line[0]);
                // Skip the 2nd column, it's just a visual reference of the category "path"
                // Iterate through each of the columns and assign the value to the respective
                // per-plugin map, created on the fly
                for (int k = 2; k < line.length; ++k) {
                    if (StringUtils.isBlank(line[k])) {
                        continue;
                    }
                    String pluginSystemName = pluginSystemNameByPosition.get(k);
                    Map<Long,String> pluginCategoryById = cache.get(pluginSystemName);
                    if (pluginCategoryById == null) {
                        pluginCategoryById = new HashMap<Long,String>();
                        cache.put(pluginSystemName, pluginCategoryById);
                    }
                    pluginCategoryById.put(id, line[k]);
                }
            }
            if (LOG.isLoggable(Level.INFO)) {
                LOG.info("Read " + count + " category mappings for " + pluginSystemNameByPosition.values());
            }
            
            // Swap in the new cache
            cacheRef.set(cache);
        } finally {
            fileReader.close();
            try{if (reader!=null) reader.close();}catch(IOException ioe){}
        }
    }

    /**
     * Get a mapped plugin-specific category
     * @param AdserverPluginDto the respective plugin
     * @param adfonicCategoryId the id of our category
     * @return the mapped category name, or null if not mapped
     */
    public String getPluginCategory(AdserverPluginDto adserverPlugin, Long adfonicCategoryId) {
        Map<Long,String> map = cacheRef.get().get(adserverPlugin.getSystemName());
        return map == null ? null : map.get(adfonicCategoryId);
    }

    /**
     * Get the set of mapped plugin-specific categories
     * @param context the current TargetingContext, which is required to provide
     * us with access to the AdserverDomainCache and DomainCache (the current
     * instance of AdserverDomainCache is used to cache the results so we don't
     * derive them each time)
     * @param pub the Publication in question
     * @param AdserverPluginDto the respective plugin
     * @return a set of mapped category names; may be empty, but will never return null
     */
    public Set<String> getPluginCategories(TargetingContext context, PublicationDto pub, AdserverPluginDto adserverPlugin) {
        // First see if it has already been cached
        Set<String> answer = context.getAdserverDomainCache().getCachedPluginCategories(pub.getId(), adserverPlugin.getSystemName());
        if (answer != null) {
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("Found cached " + adserverPlugin.getSystemName() + " categories for Publication id=" + pub.getId() + ": " + answer);
            }
            return answer;
        }

        // Not cached yet...we can derive it
        answer = getPluginCategories(adserverPlugin, context.getDomainCache().getExpandedCategoryIds(pub.getCategoryId()));

        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("Derived " + adserverPlugin.getSystemName() + " categories for Publication id=" + pub.getId() + ": " + answer);
        }
        
        // Cache it for next time
        context.getAdserverDomainCache().cachePluginCategories(pub.getId(), adserverPlugin.getSystemName(), answer);
        
        return answer;
    }
        
    /**
     * Get the set of mapped plugin-specific categories
     * @param AdserverPluginDto the respective plugin
     * @param adfonicCategoryIds the ids of our categories
     * @return a set of mapped category names; may be empty, but will never return null
     */
    public Set<String> getPluginCategories(AdserverPluginDto adserverPlugin, Collection<Long> adfonicCategoryIds) {
        Map<Long,String> map = cacheRef.get().get(adserverPlugin.getSystemName());
        if (map == null) {
            return Collections.<String>emptySet();
        }
        Set<String> answer = null;
        for (Long id : adfonicCategoryIds) {
            String pluginCategory = map.get(id);
            if (pluginCategory != null) {
                if (answer == null) {
                    answer = new HashSet<String>();
                }
                answer.add(pluginCategory);
            }
        }
        return answer == null ? Collections.<String>emptySet() : answer;
    }
}
