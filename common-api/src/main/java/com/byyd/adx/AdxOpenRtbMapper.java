package com.byyd.adx;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Logger;

public class AdxOpenRtbMapper {

    private final Logger logger = Logger.getLogger(getClass().getName());

    private static AdxOpenRtbMapper instance = new AdxOpenRtbMapper();

    private static final String PRODUCT_RESOURCE = "AdX/ad-product-category-to-iabid-map.txt";

    private static final String SENSITIVE_RESOURCE = "AdX/ad-sensitive-category-to-iabid-map.txt";

    private static final String SELLERS_RESOURCE = "AdX/seller-network-id-to-name-map.txt";

    public static AdxOpenRtbMapper instance() {
        return instance;
    }

    private final Map<Integer, Set<String>> productCategoryMap;
    private final Map<String, Set<Integer>> reverseProductCategoryMap;
    private final Map<Integer, Set<String>> sensitiveCategoryMap;
    private final Map<String, Set<Integer>> reverseSensitiveCategoryMap;
    private final Map<Integer, String> sellerNetworkMap;

    private AdxOpenRtbMapper() {

        Properties properties = loadProperties(PRODUCT_RESOURCE);
        this.productCategoryMap = convertToIntegerToStringSetMap(properties);
        logger.info("Loaded " + productCategoryMap.size() + " AdX product category mappings from " + PRODUCT_RESOURCE);
        this.reverseProductCategoryMap = reverseMap(productCategoryMap);

        properties = loadProperties(SENSITIVE_RESOURCE);
        this.sensitiveCategoryMap = convertToIntegerToStringSetMap(properties);
        logger.info("Loaded " + sensitiveCategoryMap.size() + " AdX sensitive category mappings from " + SENSITIVE_RESOURCE);
        this.reverseSensitiveCategoryMap = reverseMap(sensitiveCategoryMap);

        Properties sellerNetworkProps = loadProperties(SELLERS_RESOURCE);
        this.sellerNetworkMap = new HashMap<>();
        for (Entry<Object, Object> entry : sellerNetworkProps.entrySet()) {
            sellerNetworkMap.put(Integer.parseInt((String) entry.getKey()), entry.getValue().toString().trim());
        }
        logger.info("Loaded " + sellerNetworkMap.size() + " Seller network names from " + SELLERS_RESOURCE);
    }

    public String getSellerNetwork(int adxSellerNetworkId) {
        return sellerNetworkMap.get(adxSellerNetworkId);
    }

    /**
     * returned null is unknown/invalid mapping (error) while empty set is intentionally unmapped (ok)
     */
    public Set<String> getIabProductCategories(int adxProductCategoryId) {
        return productCategoryMap.get(adxProductCategoryId);
    }

    /**
     * returned null is unknown/invalid mapping (error) while empty set is intentionally unmapped (ok)  
     */
    public Set<String> getIabSensitiveCategories(int adxSensitiveCategoryId) {
        return sensitiveCategoryMap.get(adxSensitiveCategoryId);
    }

    public Set<Integer> getAdxSensitiveCategories(String iabCategoryId) {
        return reverseSensitiveCategoryMap.get(iabCategoryId);
    }

    public Set<Integer> getAdxProductCategories(String iabCategoryId) {
        return reverseProductCategoryMap.get(iabCategoryId);
    }

    public static Properties loadProperties(String resource) {
        try {
            Properties properties = new Properties();
            properties.load(Thread.currentThread().getContextClassLoader().getResourceAsStream(resource));
            return properties;
        } catch (IOException iox) {
            throw new IllegalArgumentException("Cannot load resource " + resource, iox);
        }
    }

    /*
     * Convert necessary types so that the issue is known at application load time itself
     */
    public static Map<Integer, Set<String>> convertToIntegerToStringSetMap(Properties props) {
        Map<Integer, Set<String>> map = new HashMap<>();
        for (Map.Entry<Object, Object> entry : props.entrySet()) {
            String entryValue = ((String) entry.getValue()).trim();
            // Treat NONE values as special, not needed strictly as you can just avoid mapping, but can be helpful later
            Integer key = Integer.parseInt((String) entry.getKey());
            Set<String> value = entryValue.equals("NONE") ? Collections.EMPTY_SET : new HashSet<String>(Arrays.asList(entryValue.split("\\s*,\\s*")));
            map.put(key, value);
        }
        return map;
    }

    public static Map<String, Set<Integer>> convertToStringToIntegerSetMap(Properties props) {
        Map<String, Set<Integer>> refMap = new HashMap<>();
        for (Map.Entry<Object, Object> entry : props.entrySet()) {
            Set<Integer> values = new HashSet<>();
            for (String value : entry.getValue().toString().trim().split("\\s*,\\s*")) {
                values.add(Integer.parseInt(value));
            }
            refMap.put(entry.getKey().toString(), values);
        }
        return refMap;
    }

    public static Map<String, Set<Integer>> reverseMap(Map<Integer, Set<String>> map) {
        Map<String, Set<Integer>> reverseMap = new HashMap<String, Set<Integer>>();
        for (Entry<Integer, Set<String>> forwardEntry : map.entrySet()) {
            for (String internalCategory : forwardEntry.getValue()) {
                Set<Integer> adXcategorySet = reverseMap.get(internalCategory);
                if (adXcategorySet == null) {
                    reverseMap.put(internalCategory, adXcategorySet = new HashSet<Integer>());
                }
                adXcategorySet.add(forwardEntry.getKey());
            }
        }
        return reverseMap;
    }

    public static void main(String[] args) {
        Set<String> iabProductCategories = AdxOpenRtbMapper.instance().getIabProductCategories(13423);
        System.out.println(iabProductCategories);
    }
}
