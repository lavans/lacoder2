package com.lavans.lacoder2.manager.dto;

public class CacheClearIn {
	private String cacheNames[];
	private String regex;
	public String[] getCacheNames() {
		return cacheNames;
	}
	public void setCacheNames(String[] cacheNames) {
		this.cacheNames = cacheNames;
	}
	public String getRegex() {
		return regex;
	}
	public void setRegex(String regex) {
		this.regex = regex;
	}
}
