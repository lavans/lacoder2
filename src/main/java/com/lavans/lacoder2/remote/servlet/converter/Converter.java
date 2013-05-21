package com.lavans.lacoder2.remote.servlet.converter;

public interface Converter {
	String toUrl(String serviceName);
	String toServiceName(String url);
}
