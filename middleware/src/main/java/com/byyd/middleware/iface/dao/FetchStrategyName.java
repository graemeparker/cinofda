package com.byyd.middleware.iface.dao;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * The sole purpose of this class is to avoid signature clashes because of something like String... stragegyName at the end of a method
 * 
 * @author pierre
 *
 */
public class FetchStrategyName implements FetchStrategy {
    
    private static final ConcurrentMap<String, FetchStrategyName> NAMES = new ConcurrentHashMap<String, FetchStrategyName>();
    
    private String name = null;
    
    private FetchStrategyName(String name) {
        this.name = name;
    }
    
    /**
     * http://dmy999.com/article/34/correct-use-of-concurrenthashmap
     * @param name
     * @return
     */
    public static FetchStrategyName getInstance(String name) {
        FetchStrategyName fsn = NAMES.get(name);
        if(fsn == null) {
            FetchStrategyName nfsn = new FetchStrategyName(name);
            fsn = NAMES.putIfAbsent(name, nfsn);
            if(fsn == null) {
                fsn = nfsn;
            }
        }
        return fsn;
    }
    
    public String getName() {
        return this.name;
    }
}
