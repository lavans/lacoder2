package com.lavans.lacoder2.lang;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogUtils {
	private static final Logger logger = LoggerFactory.getLogger(LogUtils.class);
	public static Logger getLogger(){
		String classNmae  = new Throwable().getStackTrace()[1].getClassName();
		logger.debug(classNmae);
		return LoggerFactory.getLogger(classNmae);
	}
}
