package com.lavans.lacoder2.controller;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.xpath.XPathExpressionException;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lavans.lacoder2.controller.impl.DefaultAccessLogger;
import com.lavans.lacoder2.controller.impl.DefaultExceptionHandler;
import com.lavans.lacoder2.di.BeanManager;
import com.lavans.lacoder2.di.annotation.Scope;
import com.lavans.lacoder2.di.annotation.Type;
import com.lavans.lacoder2.lang.StringUtils;
import com.lavans.lacoder2.util.Config;


/**
 * Action config setting.
 *
 * @author sbisec
 *
 */
@Scope(Type.SINGLETON)
public class WebAppConfig {
	/** logger */
	private static Logger logger = LoggerFactory.getLogger(WebAppConfig.class);
	private static final String CONFIG_SECTION="web-app/";
	private static final String CONFIG_ENCODING	=CONFIG_SECTION+"encoding";
	private static final String CONFIG_ACTION_PATH	=CONFIG_SECTION+"action-path";
	private static final String CONFIG_JSP_PATH		=CONFIG_SECTION+"jsp-path";
	private static final String CONFIG_EXTENSION	=CONFIG_SECTION+"extension";
	private static final String CONFIG_SERVICE_PATH	=CONFIG_SECTION+"service-path";
	private static final String CONFIG_FILTER		=CONFIG_SECTION+"filter";
	private static final String CONFIG_LOGGER		=CONFIG_SECTION+"logger";
	private static final String CONFIG_EXCEPTION_HANDLER	=CONFIG_SECTION+"exception-handler";
	private static final String CONFIG_UPLOAD_FILESIZE		=CONFIG_SECTION+"upload-filesize";
	private static final long DEFULAT_UPLOAD_FILESIZE = 1024*1024*10; // 10MB

	/** Default charset */
	private static String DEFAULT_ENCODING="UTF-8";

	/** default extention */
	private static String DEFAULT_EXTENSION="html";

	/** config xml */
	private Config config = Config.getInstance();

	/** charset encoding */
	String encoding;
	public String getEncoding(){
		return encoding;
	}


	/**
	 * Return path to Action classes like "com.company.project.action"
	 */
	String actionPath;

	/**
	 * Return path to jsp files like "/WEB-INF/jsp".
	 */
	String jspPath;

	String servicePath;
	public String getServicePath(){
		return servicePath;
	}
	/** extention */
	@SuppressWarnings("serial")
	Set<String> extenstions = new HashSet<String>(){{
		add(DEFAULT_EXTENSION);
	}};

	/** filter classes List<FilterClassName> */
	List<Class<? extends ActionFilter>> allFilterList = new ArrayList<>();

	/** access-logger */
	AccessLogger accessLogger;

	/** exception-handler */
	ExceptionHandler exceptionHandler;

	/** Upload file size. */
	long uploadFileSizeMax;

	private WebAppConfig(){
		init();
	}
	/**
	 *  load from default xml file
	 */
	private void init(){
		logger.debug("=== web-app configuration start ===");
		encoding = getValue(CONFIG_ENCODING, DEFAULT_ENCODING);
		actionPath = getValue(CONFIG_ACTION_PATH,"");
		jspPath = getValue(CONFIG_JSP_PATH,"");
		servicePath = getValue(CONFIG_SERVICE_PATH,"");

		if(jspPath.endsWith("/")) jspPath = jspPath.substring(0,jspPath.length()-1);
		// if path has value, add "."
//		if(!StringUtils.isEmpty(actionPath)){ actionPath += "."; }
//		if(!StringUtils.isEmpty(jspPath)){ jspPath += "."; }

		// get filter map
		List<String> filterNames = config.getNodeValueList(CONFIG_FILTER);
		for(String filterName: filterNames){
			Class<? extends ActionFilter> filter = BeanManager.getBeanClass(filterName.trim());
			allFilterList.add(filter);
			logger.info("allFilter:"+filter.getName());
		}

		// specified classes
		exceptionHandler = (ExceptionHandler)getSpecifiedClass(CONFIG_EXCEPTION_HANDLER, DefaultExceptionHandler.class);
		logger.info("exceptionHandler:"+exceptionHandler.getClass().getName());
		accessLogger = (AccessLogger)getSpecifiedClass(CONFIG_LOGGER, DefaultAccessLogger.class);
		logger.info("accessLogger:"+accessLogger.getClass().getName());

		// Action extension. Default: "html"
		extenstions = getExtentions(config.getNodeValueList(CONFIG_EXTENSION));

		uploadFileSizeMax = config.getNodeValueLong(CONFIG_UPLOAD_FILESIZE, DEFULAT_UPLOAD_FILESIZE);

		logger.info("extionsion:"+extenstions);

		logger.debug("=== web-app configuration end ===");
	}

	/**
	 * Get extention config.
	 *
	 * @param src
	 * @return
	 */
	private Set<String> getExtentions(List<String> extentionList){
		Set<String> extentionSet = new HashSet<>();
		for(String extensionStr: extentionList){
			String extensions[] = StringUtils.splitTrim(extensionStr, ",");
			extentionSet.addAll(Arrays.asList(extensions));
		}

		// if extension is not defined then use default.
		if(extentionSet.isEmpty()){
			extentionSet.add(DEFAULT_EXTENSION);
		}

		return extentionSet;
	}

	/**
	 * Get value from config. If value is not set or empty, set defaultValue.
	 *
	 * @param key
	 * @param defaultValue
	 * @return
	 */
	private String getValue(String key, String defaultValue){
		String result = config.getNodeValue(key);
		if(StringUtils.isEmpty(result)){
			result = defaultValue;
		}
		return result;
	}

	/**
	 * Get specified class from lacoder.xml.
	 *
	 * @param xql
	 * @param defaultClass
	 * @return
	 * @throws FileNotFoundException
	 * @throws XPathExpressionException
	 */
	private Object getSpecifiedClass(String xql, Class<?> defaultClass) {
		Config config = Config.getInstance();
		Object result = null;
		// exception handler
		String clazz = config.getNodeValue(xql);
		if(!StringUtils.isEmpty(clazz)){
			result = BeanManager.getBean(clazz);
		}
		if(result==null){
			result = BeanManager.getBean(defaultClass);
		}
		return result;
	}
}
