/* $Id: MethodUtils.java 509 2012-09-20 14:43:25Z dobashi $
 * create: 2004/08/24
 * (c)2004 Lavans Networks Inc. All Rights Reserved.
 */
package com.lavans.lacoder2.lang;



/**
 * @author dobashi
 * @version 1.00
 */
public class MethodUtils{
	//private static Logger logger = LogUtils.getLogger();
	private static String IGNORE_PACKAGE="com.lavans.lacoder2.";
	/**
	 * メソッド名の取得。
	 * LogRecord#inferCaller()のパクリ。
	 * @see java.util.loggin.LogRecord#inferCaller
	 */
	public static String getMethodName(String classname) {
		return getMethodName(classname,IGNORE_PACKAGE);
	}

	/**
	 * メソッド名の取得。ツールデバッグ用
	 * LogRecord#inferCaller()のパクリ。
	 * @see java.util.loggin.LogRecord#inferCaller
	 */
	public static String getMethodNameTool(String classname) {
		return getMethodName(classname, null);
	}

	/**
	 * メソッド名の取得。
	 * LogRecord#inferCaller()のパクリ。
	 *
	 * @see java.util.loggin.LogRecord#inferCaller
	 *
	 */
	private static String getMethodName(String classname, String ignoreStr) {
		try{
			//logger.debug("classname="+ classname);

			// Get the stack trace.
			StackTraceElement stack[] = (new Throwable()).getStackTrace();
			// First, search back to a method in the Logger class.
			int ix = 0;
			while (ix < stack.length) {
				StackTraceElement frame = stack[ix];
				String cname = frame.getClassName();
//				logger.info(cname);
				if (classname.equals(cname)) {
					break;
				}
				ix++;
			}
			// Now search for the first frame before the "Logger" class.
			while (ix < stack.length) {
				StackTraceElement frame = stack[ix];
				String cname = frame.getClassName();
//				logger.info(cname);
				if (!classname.equals(cname) && ((ignoreStr==null) || !cname.startsWith(ignoreStr))) {
					return simpleName(cname)+"#"+frame.getMethodName() +"():"+ frame.getLineNumber();
				}
				ix++;
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return "";
	}

	private static final String simpleName(String cname){
		return cname.substring(cname.lastIndexOf(".")+1);
	}
}
