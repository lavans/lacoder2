package com.lavans.lacoder2.controller.util;

import javax.servlet.http.HttpServletRequest;

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
		  String ip = request.getHeader("x-forwarded-for");
		  ip = StringUtils.isEmpty(ip) ? request.getRemoteAddr() : ip;
		  return ip;
	}

}
