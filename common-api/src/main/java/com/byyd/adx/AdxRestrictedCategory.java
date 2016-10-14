package com.byyd.adx;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public enum AdxRestrictedCategory {

    Alcohol(AdxRestrictedCategory.ADX_ALCOHOL, AdxRestrictedCategory.IAB_BEER, AdxRestrictedCategory.IAB_WINE);

    private final Integer adxId;

    private final List<String> iabCategories;

    //compiler copies primitive value 33 to enum instance definition, if here will be Integer.valueOf(33) enum will be created with null parameter!!!
    public static final int ADX_ALCOHOL = 33;

    public static final String IAB_BEER = "IAB8-5"; // Strings are copied by compiler 

    public static final String IAB_WINE = "IAB8-18";

    private static Map<Integer, AdxRestrictedCategory> byAdxIds; // cannot initialize here (it is too late)
    private static Map<String, Collection<AdxRestrictedCategory>> byIabIds;

    private AdxRestrictedCategory(int adxId, String... iabCategories) {
        this.adxId = adxId;
        this.iabCategories = Arrays.asList(iabCategories);
        addSelf(this); // adxIds.put(adxId, this); // cannot be done
    }

    private static void addSelf(AdxRestrictedCategory self) {
        if (byAdxIds == null) {
            // have to initialize here 
            byAdxIds = new HashMap<Integer, AdxRestrictedCategory>();
            byIabIds = new HashMap<String, Collection<AdxRestrictedCategory>>();
        }
        byAdxIds.put(self.getAdxId(), self);
        List<String> iabIds = self.getIabCategories();
        for (String iabId : iabIds) {
            Collection<AdxRestrictedCategory> collection = byIabIds.get(iabId);
            if (collection == null) {
                collection = new ArrayList<AdxRestrictedCategory>();
                byIabIds.put(iabId, collection);
            }
            collection.add(self);
        }
    }

    public List<String> getIabCategories() {
        return iabCategories;
    }

    public Integer getAdxId() {
        return adxId;
    }

    /**
     * @return null or AdxRestrictedCategory
     */
    public static AdxRestrictedCategory getByAdxId(Integer adxId) {
        return byAdxIds.get(adxId);
    }

    /**
     * @return never null
     */
    public static Collection<AdxRestrictedCategory> getByIabId(String iabId) {
        Collection<AdxRestrictedCategory> collection = byIabIds.get(iabId);
        if (collection != null) {
            return collection;
        } else {
            return Collections.EMPTY_SET;
        }
    }
}
