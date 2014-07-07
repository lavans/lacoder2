package com.lavans.lacoder2.controller.util;

import javax.servlet.http.HttpServletRequest;

import lombok.val;

import com.lavans.lacoder2.lang.StringUtils;

/**
 * ログインユーティリティクラス。
 * @author kobayashi
 *
 */
public class NetworkUtils {
	/**
	 * リモートIPを返す(Proxy対応)
	 * @param request
	 * @return
	 */
	public static String getRemoteAddr(HttpServletRequest request) {
		val result = request.getHeader("x-forwarded-for");
		return StringUtils.defaultIfBlank(result, request.getRemoteAddr());
	}

	/**
	 * リクエストされたHost名を返す。
	 * @param request
	 * @return
	 */
	public static String getRequestedHost(HttpServletRequest request) {
		  val result = request.getHeader("x-forwarded-host");
		  return StringUtils.defaultIfBlank(result, request.getHeader("Host"));
	}
}
