package com.lavans.lacoder2.manager.dto;

import java.util.concurrent.ConcurrentMap;

public class CaccheGetMapOut {
	private ConcurrentMap<String,Object> cacheMap;

	public ConcurrentMap<String,Object> getCacheMap() {
		return cacheMap;
	}

	public void setCacheMap(ConcurrentMap<String,Object> cacheMap) {
		this.cacheMap = cacheMap;
	}
}
