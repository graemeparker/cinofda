package com.byyd.middleware.iface.dao;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Value;

/**
 * This class is a Factory/Cache class for instances of FetchStrategy. Upon invokation of the init() method, the class will read the content
 * of the directory injected as fetchStrategiesDirectory (a resource path, which defaults to /fetch-strategies) and parse all files found whose extension is in the list of
 * supported extensions injected as fetchStrategiesFilesExtentions (which defaults to "properties"). It will parse each file into a FetchStrategy object
 * and cache that object linked to the name of the file it was parsed from.
 *
 * @author pierre
 *
 */
public class FetchStrategyFactory {
    private static final transient Logger LOG = Logger.getLogger(FetchStrategyFactory.class.getName());

    @Value("${persistence.fetchStrategies.directory:/fetch-strategies}")
    private String fetchStrategiesDirectory;
    @Value("${persistence.fetchStrategies.files.extensions:properties}")
    private String fetchStrategiesFilesExtentions;

    private final Map<String, FetchStrategyImpl> fetchStrategies = new HashMap<String, FetchStrategyImpl>();
    private List<String> extensions = null;

    /**
    * Standard constructor
    */
    public FetchStrategyFactory() {
        super();
    }
    /**
    * Init method, to be invoked by Spring through its init-method bean attribute.
    */
    public void init() {
        try {
            extensions = Arrays.asList(StringUtils.split(fetchStrategiesFilesExtentions, " ,;\t"));

            Enumeration<URL> en = this.getClass().getClassLoader().getResources(fetchStrategiesDirectory);
            if (en.hasMoreElements()) {
                processFetchStrategiesFiles(en.nextElement());
            }else {
                LOG.info("No fetch strategies found in " + fetchStrategiesDirectory);
            }
        }catch (IOException ioe){
            LOG.info("Directory " + fetchStrategiesDirectory + " does not exist!");
        }catch (Exception e) {
            LOG.log(Level.WARNING, "Initialization failed", e);
        }
    }
    
    private void processFetchStrategiesFiles(URL resourceURL) throws URISyntaxException {
        File resourceDir = new File(resourceURL.toURI());
        String[] filenames = resourceDir.list();
        for(String filename : filenames) {
            LOG.info("Processing " + filename);
            String extension = FilenameUtils.getExtension(filename);
            if (StringUtils.isEmpty(extension)) {
                LOG.info("No extension. Skipping");
            }else if (!extensions.contains(extension)) {
                LOG.info("Extension \"" + extension + "\" not in authorized list. Skipping");
            }else{
                loadFetchStrategy(FilenameUtils.getBaseName(filename));
            }
        }
    }
    /**
    * Method creating a FetchStrategy object based on a FetchStrategyName instance.
    *
    * @param strategyName the name of the strategy
    * @return the FetchStrategy created
    * @throws Exception
    * @see loadFetchStrategy(String)
    */
    public FetchStrategyImpl loadFetchStrategy(FetchStrategyName strategyName) {
        return loadFetchStrategy(strategyName.getName());
    }
    /**
    * Method creating a FetchStrategy object based on a strategy name. The logic is as follows:
    * - loop through the supported extensions, and figure out what the properties file's full name is
    * - load file into a Properties object
    * - instanciate a FetchStrategy object based on the strategy name and Properties object
    *
    * @param strategyName the name of the strategy to load
    * @return the FetchStrategy object created
    * @throws IllegalArgumentException if no properties files matching the strategyName can be found
    */
    public FetchStrategyImpl loadFetchStrategy(String strategyName) {
        LOG.info("Loading Fetch Strategy \"" + strategyName + "\"");
        Properties prop = new Properties();
        for(String extension : extensions) {
            String fileLocation = fetchStrategiesDirectory + "/" + strategyName + "." + extension;
            try {
                InputStream is = this.getClass().getResourceAsStream(fileLocation);
                if (is != null) {
                    LOG.info("Will be loaded from file " + fileLocation);
                    prop.load(is);
                    break;
                }
            } catch(Exception e) {
                //do nothing
            }
        }
        if(prop.isEmpty()) {
            throw new IllegalArgumentException("Could not load or empty properties file for Fetch Strategy \"" + strategyName + "\"");
        }
        FetchStrategyImpl strategy = new FetchStrategyImpl(strategyName, prop);
        fetchStrategies.put(strategyName, strategy);
        return strategy;
    }

    /**
    * Standard getter
    * @param strategy a FetchStrategyImpl or the name of the strategy
    * @return the cache FetchStrategy instance, or null if none are found
    */
    public FetchStrategyImpl getFetchStrategy(FetchStrategy strategy) {

        if(strategy instanceof FetchStrategyImpl) {
            return (FetchStrategyImpl)strategy;
        }
        if(strategy instanceof FetchStrategyName) {
            return getFetchStrategy(((FetchStrategyName)strategy).getName());
        }

        return null;
    }
    /**
    * Standard getter
    * @param strategyName the name of the strategy
    * @return the cache FetchStrategy instance, or null if none are found
    */
    public FetchStrategyImpl getFetchStrategy(String strategyName) {
        return fetchStrategies.get(strategyName);
    }
}
