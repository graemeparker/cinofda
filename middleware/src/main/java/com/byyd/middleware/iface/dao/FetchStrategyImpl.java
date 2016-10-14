package com.byyd.middleware.iface.dao;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;

import com.byyd.middleware.iface.exception.FetchStrategyException;

/**
 * This class is an abstraction for reading "Fetch Strategies" defined in properties files with the following syntax:
 *
 * CanonicalClassName=[field1{,field2, ... fieldN}|*]
 * {CanonicalClassName.fieldName.joinType=[inner|left|right]
 *
 * - CanonicalClassName: full name of a class of the Domain package
 * - field1{,field2, ... fieldN}: list of one or more fields mapping lazily loaded relationships)
 *   to eagerly load when creating objects of type CanonicalClassName
 * - to trigger a load of all possible fields, use CanonicalClassName=*
 * - Each field can specify a join type using the CanonicalClassName.fieldName.joinType key.
 *   If none is specified, "inner" is the default
 *
 * Note that this abstraction MUST remain independant of any persistence API. All member variables and methods invoked
 * have to be generic, not specific to any particular API.
 *
 * Instances of this class can be created programatically and used directly from service-level or application-level calls.
 *
 * FetchStrategyImpl fetchStrategy = new FetchStrategyImpl();
 * fetchStrategy.addEagerlyLoadedFieldForClass(Creative.class, FetchStrategyImpl.ALL_FIELDS_MARKER);
 * fetchStrategy.setJoinTypeForField(Creative.class, "categories", FetchStrategyImpl.JoinType.LEFT);
 * List<Creative> list = advertiserManager.getAllCreatives(fetchStrategy);
 *
 * Also, note that FetchStrategyImpl classes are recursive. Whenever setting a particulat field to be eagerly loaded
 * for a class, it will look for eager fields to be loaded on that field as well, and so on. This means that there must
 * be an unbroken line between top and bottom level fields specified as eager. For instance, say you want to load a Creative
 * and have the platforms field of the Model objects carried by the Segment object held by Creative, you have to do this:
 *
 *    FetchStrategyImpl fs = new FetchStrategyImpl();
 *      fs.addEagerlyLoadedFieldForClass(Creative.class, "segment");
 *    fs.addEagerlyLoadedFieldForClass(Segment.class, "models");
 *    fs.addEagerlyLoadedFieldForClass(Model.class, "platforms");
 *
 * If you do not specify "models" for the Segment class, nothing specified for Model will ever be processed.
 *
 * @author Pierre Adriaans
 *
 */
public class FetchStrategyImpl implements FetchStrategy {
    private static final transient Logger LOG = Logger.getLogger(FetchStrategyImpl.class.getName());

    public static final String ALL_FIELDS_MARKER = "*";

    // Enum defining join types in an API-independant way. Each member's value is the syntax used in the properties files
    public enum JoinType {
        INNER("inner"),
        LEFT("left"),
        RIGHT("right");

        private final String value;
        JoinType(String value) {
            this.value = value;
        }
        @Override
        public String toString() {
            return value;
        }

        public static JoinType getValue(String sz) {
            if(StringUtils.isEmpty(sz)) {
                return null;
            }
            if("inner".equals(sz)) {
                return INNER;
            }
            if("left".equals(sz)) {
                return LEFT;
            }
            if("right".equals(sz)) {
                return RIGHT;
            }
            return null;
        }
    }

    public static final String JOIN_TYPE_SUFFIX = ".joinType";
    public static final String RECURSIVE_PROCESSING_SUFFIX = ".recursive";

    // Name of the Strategy. Typically, the name of the properties files defining it
    private String name = null;
    // Canonical class name --> List<String>
    private final Map<String, List<String>> eagerlyLoadedFields = new HashMap<String, List<String>>();
    // Canonical class name-field name --> JoinType
    private final Map<String, JoinType> joinTypes = new HashMap<String, JoinType>();
    // Canonical class name-field name --> Boolean
    private final Map<String, Boolean> recursiveFieldProcessing = new HashMap<String, Boolean>();

    private boolean verifyFields = true;

    /**
     * Standard constructor. Use this version to programatically create instances that are not named, typically throwaways.
     */
    public FetchStrategyImpl() {
        this(null);
    }
    /**
    * Standard constructor. Use this version to programatically create instances that are named
    * @param name the name of the Fetch Strategy
    */
    public FetchStrategyImpl(String name) {
        this(name, null);
    }
    /**
    * Standard constructor.
    * @param name the name of the Fetch Strategy
    * @param properties the parsed properties files defining it
    */
    public FetchStrategyImpl(String name, Properties properties) {
        super();
        this.name = name;

        if(properties != null) {
            processPropertyKeys(properties, properties.keys());
        }
    }
    
    @SuppressWarnings("rawtypes")
    private void processPropertyKeys(Properties properties, Enumeration keys) {
        while(keys.hasMoreElements()) {
            String key = (String)keys.nextElement();
            if (!key.endsWith(JOIN_TYPE_SUFFIX) && !key.endsWith(RECURSIVE_PROCESSING_SUFFIX)){
                try {
                    processPropertyKey(key, properties);
                } catch(ClassNotFoundException e) {
                    if (LOG.isLoggable(Level.FINE)) {
                        LOG.fine(key + "Not found. Skipping");
                    }
                }
            }
        }
    }
    private void processPropertyKey(String key, Properties properties) throws ClassNotFoundException {
        // The key must be a canonical class name
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("Testing " + key + " as a class name");
        }
        Class.forName(key, false, Thread.currentThread().getContextClassLoader());
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine(key + "Found");
        }
        String className = key;
        String sz = properties.getProperty(className);
        if (StringUtils.isEmpty(sz) && LOG.isLoggable(Level.FINE)) {
            LOG.fine("No eagerly fetched fields specified. Skipping");
        }else{
            eagerlyLoadedFields.put(className, getEagerlyLoadedFieldList(properties, className, sz));
         
            String recursiveClassProcessingKey = className + "." + RECURSIVE_PROCESSING_SUFFIX;
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("Looking for a Recursive Class Processing specification with " + recursiveClassProcessingKey);
            }
            String szRP = properties.getProperty(recursiveClassProcessingKey);
            if(StringUtils.isEmpty(szRP)) {
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.fine("None found");
                }
            } else {
                LOG.log(Level.FINE, "Found {0}", szRP);
                setRecursiveProcessingForClass(className, BooleanUtils.toBooleanObject(szRP));
            }
        }
    }
    
    private List<String> getEagerlyLoadedFieldList(Properties properties, String className, String sz) {
        List<String> eagerlyLoadedFieldList = new ArrayList<String>();
        StringTokenizer tok = new StringTokenizer(sz," ;,:\t");
        while(tok.hasMoreTokens()) {
            String fieldName = tok.nextToken();
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("Processing " + fieldName);
            }
            eagerlyLoadedFieldList.add(fieldName);
            // Join- type
            String joinTypeKey = className + "." + fieldName + JOIN_TYPE_SUFFIX;
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("Looking for a Join Type specification with " + joinTypeKey);
            }
            String joinType = properties.getProperty(joinTypeKey);
            if(StringUtils.isEmpty(joinType)) {
                LOG.fine("None found");
            } else {
                LOG.log(Level.FINE, "Found {0}", joinType);
                setJoinTypeForField(className, fieldName, JoinType.getValue(joinType));
            }
      
            String recursiveFieldProcessingKey = className + "." + fieldName + RECURSIVE_PROCESSING_SUFFIX;
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("Looking for a Recursive Field Processing specification with " + recursiveFieldProcessingKey);
            }
            String szRP = properties.getProperty(recursiveFieldProcessingKey);
            if(StringUtils.isEmpty(szRP)) {
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.fine("None found");
                }
            } else {
                LOG.log(Level.FINE, "Found {0}", szRP);
                setRecursiveProcessingForField(className, fieldName, BooleanUtils.toBooleanObject(szRP));
            }
        }
        return eagerlyLoadedFieldList;
    }

    /**
    * Builds a key as a canonical class name/field name combination
    *
    * @param className canonical class name
    * @param fieldName field name
    * @return a key built by concatenation both parameters
    */
    protected String makeKeyForClassAndField(String className, String fieldName) {
        return className + "-" + fieldName;
    }

    public String getName() {
        return name;
    }

    public boolean isCacheable() {
        return !StringUtils.isEmpty(name);
    }

    public FetchStrategyImpl setVerifyFields(boolean value) {
        this.verifyFields = value;
        return this;
    }

    public List<String> getEagerlyLoadedFieldsForClass(Class<?> clazz) {
        return getEagerlyLoadedFieldsForClass(clazz.getCanonicalName());
    }

    public List<String> getEagerlyLoadedFieldsForClass(String className) {
        return eagerlyLoadedFields.get(className);
    }

    public JoinType getJoinType(Class<?> clazz, String fieldName) {
        return getJoinType(clazz.getCanonicalName(), fieldName);
    }

    public JoinType getJoinType(String className, String fieldName) {
        return joinTypes.get(makeKeyForClassAndField(className, fieldName));
    }

    public Boolean getRecursiveProcessing(Class<?> clazz, String fieldName) {
        return getRecursiveProcessing(clazz.getCanonicalName(), fieldName);
    }

    public Boolean getRecursiveProcessing(String className, String fieldName) {
        Boolean value = recursiveFieldProcessing.get(makeKeyForClassAndField(className, fieldName));
        if(value == null) {
            return true;
        }
        return value;
    }

    public Boolean getRecursiveProcessing(Class<?> clazz) {
        return getRecursiveProcessing(clazz.getCanonicalName());
    }

    public Boolean getRecursiveProcessing(String className) {
        Boolean value = recursiveFieldProcessing.get(className);
        if(value == null) {
            return true;
        }
        return value;
    }

    //public FetchStrategyImpl addEagerlyLoadedFieldForClass(Class<?> clazz, String fieldName) {
    //    return addEagerlyLoadedFieldForClass(clazz, fieldName, null);
    //}
    public FetchStrategyImpl addEagerlyLoadedFieldForClass(Class<?> clazz, String fieldName, JoinType joinType) {
        return addEagerlyLoadedFieldForClass(clazz.getCanonicalName(), fieldName, joinType);
    }

    public boolean isFieldNameValid(String className, String fieldName) {
        try {
            if(!fieldName.equals(FetchStrategyImpl.ALL_FIELDS_MARKER)) {
                Class<?> metamodelClazz = Class.forName(className + "_", false, Thread.currentThread().getContextClassLoader());
                metamodelClazz.getDeclaredField(fieldName);
            }
        } catch(Exception e) {
            LOG.severe("While validating field \"" + fieldName + "\" for class \"" + className + "\":\n" + ExceptionUtils.getStackTrace(e));
            return false;
        }
        return true;
    }

    public FetchStrategyImpl addEagerlyLoadedFieldForClass(String className, String fieldName, JoinType joinType) {
        if(verifyFields && !isFieldNameValid(className, fieldName)) {
            throw new FetchStrategyException("Error: \"" + fieldName + "\" is not a valid field name for class \"" + className + "\"");
        }
        List<String> list = eagerlyLoadedFields.get(className);
        if(list == null) {
            list = new ArrayList<String>();
            eagerlyLoadedFields.put(className, list);
        }
        if (!list.contains(fieldName)){
            list.add(fieldName);
            if(joinType != null) {
                setJoinTypeForField(className, fieldName, joinType);
            }
        }
        return this;
    }

    public FetchStrategyImpl removeEagerlyLoadedFieldForClass(Class<?> clazz, String fieldName) {
        return removeEagerlyLoadedFieldForClass(clazz.getCanonicalName(), fieldName);
    }

    public FetchStrategyImpl removeEagerlyLoadedFieldForClass(String className, String fieldName) {
        List<String> list = eagerlyLoadedFields.get(className);
        if(list != null) {
            list.remove(fieldName);
        }
        removeJoinTypeForField(className, fieldName);
        return this;
    }

    public FetchStrategyImpl setJoinTypeForField(Class<?> clazz, String fieldName, JoinType joinType) {
        return setJoinTypeForField(clazz.getCanonicalName(), fieldName, joinType);
    }

    public FetchStrategyImpl setJoinTypeForField(String className, String fieldName, JoinType joinType) {
        joinTypes.put(makeKeyForClassAndField(className, fieldName), joinType);
        return this;
    }

    public FetchStrategyImpl removeJoinTypeForField(Class<?> clazz, String fieldName) {
        return removeJoinTypeForField(clazz.getCanonicalName(), fieldName);
    }

    public FetchStrategyImpl removeJoinTypeForField(String className, String fieldName) {
        joinTypes.remove(makeKeyForClassAndField(className, fieldName));
        return this;
    }

    public FetchStrategyImpl setRecursiveProcessingForField(Class<?> clazz, String fieldName, Boolean value) {
        return setRecursiveProcessingForField(clazz.getCanonicalName(), fieldName, value);
    }

    public FetchStrategyImpl setRecursiveProcessingForField(String className, String fieldName, Boolean value) {
        recursiveFieldProcessing.put(makeKeyForClassAndField(className, fieldName), value);
        return this;
    }

    public FetchStrategyImpl removeRecursiveProcessingForField(Class<?> clazz, String fieldName) {
        return removeRecursiveProcessingForField(clazz.getCanonicalName(), fieldName);
    }

    public FetchStrategyImpl removeRecursiveProcessingForField(String className, String fieldName) {
        joinTypes.remove(makeKeyForClassAndField(className, fieldName));
        return this;
    }


    public FetchStrategyImpl setRecursiveProcessingForClass(Class<?> clazz, Boolean value) {
        return setRecursiveProcessingForClass(clazz.getCanonicalName(), value);
    }

    public FetchStrategyImpl setRecursiveProcessingForClass(String className, Boolean value) {
        recursiveFieldProcessing.put(className, value);
        return this;
    }

    public FetchStrategyImpl removeRecursiveProcessingForClass(Class<?> clazz) {
        return removeRecursiveProcessingForClass(clazz.getCanonicalName());
    }

    public FetchStrategyImpl removeRecursiveProcessingForClass(String className) {
        recursiveFieldProcessing.remove(className);
        return this;
    }


    //public FetchStrategyImpl allEager(Class<?> clazz) {
    //    return allEager(clazz.getCanonicalName());
    //}
    public FetchStrategyImpl allEager(Class<?> clazz,  JoinType joinType) {
        return allEager(clazz.getCanonicalName(), joinType);
    }

    //public FetchStrategyImpl allEager(String className) {
    //    return addEagerlyLoadedFieldForClass(className, ALL_FIELDS_MARKER);
    //}

    public FetchStrategyImpl allEager(String className, JoinType joinType) {
        return addEagerlyLoadedFieldForClass(className, ALL_FIELDS_MARKER, joinType);
    }

}
