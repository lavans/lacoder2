package com.lavans.lacoder2.manager.dto;

import java.io.Serializable;

public class CacheGetIn implements Serializable{
	private String cacheName;

	public String getCacheName() {
		return cacheName;
	}

	public void setCacheName(String cacheName) {
		this.cacheName = cacheName;
	}
}
