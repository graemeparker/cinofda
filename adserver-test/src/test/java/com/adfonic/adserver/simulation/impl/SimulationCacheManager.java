package com.adfonic.adserver.simulation.impl;

import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.adfonic.cache.CacheException;
import com.adfonic.cache.CacheManager;

public class SimulationCacheManager implements CacheManager {
	private static final String DEFAULT_NAMESPACE = "@default@";
	
	private ConcurrentHashMap<String, Map<String, Object>> cache = new ConcurrentHashMap<>();

	@SuppressWarnings("unchecked")
	@Override
	public <T> T get(String key, Class<T> clazz) throws CacheException {
		return (T) retrieveMap(DEFAULT_NAMESPACE, false).get(key);
	}

	@SuppressWarnings("unchecked")
	private Map<String, Object> retrieveMap(
			String ns, boolean create) {
		Map<String, Object> map = cache.get(ns);
		
		if (map == null) {
			if (create) {
				map = new ConcurrentHashMap<String, Object>();
				cache.put(ns, map);
			} else {
				map = Collections.EMPTY_MAP;
			}
		}
		
		return map;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T get(String key, String cacheName, Class<T> clazz)
			throws CacheException {
		return (T) retrieveMap(cacheName, false).get(key);
	}

	@Override
	public void set(String key, Object value, String cacheName, int ttlSeconds)
			throws CacheException {
		retrieveMap(cacheName, true).put(key, value);
	}

	@Override
	public void set(String key, Object value, int ttlSeconds)
			throws CacheException {
		retrieveMap(DEFAULT_NAMESPACE, true).put(key, value);
	}

	@Override
	public void set(String key, Object value, String cacheName, Date expiry)
			throws CacheException {
		retrieveMap(cacheName, true).put(key, value);
	}

	@Override
	public void set(String key, Object value, Date expiry)
			throws CacheException {
		retrieveMap(DEFAULT_NAMESPACE, true).put(key, value);
	}

	@Override
	public boolean remove(String key) throws CacheException {
		return retrieveMap(DEFAULT_NAMESPACE, true).remove(key) != null;
	}

	@Override
	public boolean remove(String key, String cacheName) throws CacheException {
		return retrieveMap(cacheName, true).remove(key) != null;
	}

	@Override
	public boolean remove(String key, Class<?> clazz) throws CacheException {
		return retrieveMap(DEFAULT_NAMESPACE, true).remove(key) != null;
	}
}
