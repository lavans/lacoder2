package com.lavans.lacoder2.controller.util;

import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import lombok.extern.slf4j.Slf4j;

import com.lavans.lacoder2.util.ParameterUtils;

@Slf4j
public class ErrorUtils {
	/**
	 * リクエスト情報、ユーザエージェント等詳細な内容を取得します。
	 *
	 * @param  request リクエスト
	 * @return         文字列
	 */
	public static String getRequestDetailString(HttpServletRequest request) {
    	StringBuilder buffer = new StringBuilder(4096);

		// リクエスト情報を付加
		buffer.append("\n");
		buffer.append(request.getMethod() + " "+ request.getRequestURI()).append("\t");
		buffer.append(request.getRemoteAddr()).append("\t");
		buffer.append(request.getSession().getId()).append("\n");
		buffer.append(getRequestHeadersString(request)).append("\n");
		buffer.append(getRequestParameterString(request)).append("\n");
		buffer.append(getRequestAttributeString(request)).append("\n");

		return buffer.toString();
	}

	/**
	 * リクエストパラメータの情報を文字列化して返却します。
	 *
	 * @param request リクエスト
	 * @return        文字列
	 */
	private static String getRequestParameterString(HttpServletRequest request) {

		// リクエストパラメータを取得
		Map<String, String[]> parameterMap = request.getParameterMap();

		StringBuilder buffer = new StringBuilder(512);
		// リクエストパラメータを付加
		List<String> keyList = new LinkedList<>();
		keyList.addAll(parameterMap.keySet());
		Collections.sort(keyList);
		for (String key: keyList){
			String values[] = parameterMap.get(key);
			for(String value: values){
				buffer.append("\n\t\"").append(key).append("\"=\"").append(value).append("\"");
			}
		}

		log.info(ParameterUtils.toStoreString(request.getParameterMap()));

		return "request params:["+ buffer.toString() + "]";
	}

	/**
	 * リクエストアトリビュートの情報を文字列化して返却します。
	 *
	 * @param request リクエスト
	 * @return        文字列
	 */
	private static String getRequestAttributeString(HttpServletRequest request){
		StringBuilder buffer = new StringBuilder(512);

		Enumeration<String> attriKeys = request.getAttributeNames();
		while (attriKeys.hasMoreElements()) {
		    String key =attriKeys.nextElement();
		    Object value = request.getAttribute(key);
		    buffer.append("\n\t").append(key).append("=");

		    // toString()での例外発生を考慮
			try {
				buffer.append(value);
			} catch (Exception e) {
				buffer.append((value != null ? value.getClass().getName() : value)).append(":").append(e.getMessage());
			}
        }

		return "request attributes:[" + buffer.toString() + "]";
	}

	/**
	 * リクエストアヘッダの情報を文字列化して返却します。
	 * Stringのみを対象とします。
	 *
	 * @param request リクエスト
	 * @return        文字列
	 */
	private static String getRequestHeadersString(HttpServletRequest request){

		StringBuilder buffer = new StringBuilder(512);

		Enumeration<String> names = request.getHeaderNames();

		while (names.hasMoreElements()) {
		    String name   = names.nextElement();
		    String header = request.getHeader(name);
		    buffer.append("\n\t").append(name).append("=").append(header);
        }

		return "request headers:["+ buffer.toString() +"]";
	}
}
