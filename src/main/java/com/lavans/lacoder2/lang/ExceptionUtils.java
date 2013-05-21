package com.lavans.lacoder2.lang;

import java.io.PrintWriter;
import java.io.StringWriter;

public class ExceptionUtils {

	/**
	 * スタックトレースを文字列で取得します。
	 *
	 * @param request リクエスト
	 * @param         スタックトレース
	 */
	public static String getStackTraceString(Throwable e) {

		StringWriter sw = new StringWriter();
		PrintWriter  pw  = new PrintWriter(sw);
		e.printStackTrace(pw);

		return sw.toString();
	}
}
