package com.lavans.lacoder2.controller;

import javax.servlet.http.HttpServletRequest;

public interface AccessLogger {
	void preLog(HttpServletRequest request, String actionURI);
	void log(HttpServletRequest request, String actionURI, long time);

}