package com.lavans.lacoder2.controller.impl;

import java.util.Map;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lavans.lacoder2.controller.AccessLogger;
import com.lavans.lacoder2.util.ParameterUtils;

public class DefaultAccessLogger implements AccessLogger {
	private static Logger logger = LoggerFactory.getLogger(DefaultAccessLogger.class);

	@Override
	public void preLog(HttpServletRequest request, String actionURI) {

	}
	@Override
	public void log(HttpServletRequest request, String actionURI, long time) {
		// Sort map
		Map<String, String[]> map = new TreeMap<>();
		map.putAll(request.getParameterMap());
		// Create log string.
		logger.info(actionURI +"\t"+ time + " ms\t"+ ParameterUtils.toStoreString(map));
	}
}
