package com.byyd.middleware.iface.dao;

/**
 * The purpose of this interface is to be implemented by both FetchStrategyName and FetchStrategyImpl, so that either can 
 * be passed to Service and DAO methods. This would allow programmatically created instances of FetchStrategyImpl to be 
 * used in unit testing, for instance.
 *  
 * @author pierre
 *
 */
public interface FetchStrategy {

}
