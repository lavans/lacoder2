package com.lavans.lacoder2.di;

import java.io.FileNotFoundException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.lavans.lacoder2.lang.StringUtils;
import com.lavans.lacoder2.util.Config;

/**
 * Service Manager Implementation for XML file(default).
 *
 * @author dobashi
 *
 */
public class BeanManager {
	/** logger */
	private static final Logger logger = LoggerFactory.getLogger(BeanManager.class);

	/**  */
	private static Map<String, String> packageNameMap = new ConcurrentHashMap<String, String>();
	/** cache of all service */
	private static Map<String, BeanInfo> beanMap = new ConcurrentHashMap<String, BeanInfo>();

	private BeanManager() {
	}

	static {
		try{
			load(Config.CONFIG_FILE);
		}catch (RuntimeException e){
			if(e.getCause() instanceof FileNotFoundException){
				logger.info("Default config ["+ Config.CONFIG_FILE +"] is not loaded.");
			}else{
				throw e;
			}
		}
	}

	/**
	 * Init bean & group info.
	 *
	 * @throws FileNotFoundException
	 */
	public static void init() {
		packageNameMap.clear();
		beanMap.clear();
		logger.info("Clear bean mapping information.");
	}

	/**
	 * Load configuration file.
	 *
	 * @param filename
	 * @throws FileNotFoundException
	 */
	public static void load(String filename) {
		Config config = Config.getInstance(filename);
		if(config.getNode("di") == null) {
			return;
		}
		// package map
		loadPackages(config);
		// bean map
		loadBeans(config);
	}

	/**
	 * read package config
	 * @param config
	 * @param di
	 */
	private static void loadPackages(Config config){
		NodeList packageList = config.getNodeList("di/group");
		for (int i = 0; i < packageList.getLength(); i++) {
			Element node = (Element) packageList.item(i);
			String group = node.getAttribute("name");
			String packageName = node.getAttribute("package");
			logger.info("group[" + group + "] package[" + packageName + "]");
			packageNameMap.put(group, packageName);
		}
	}

	/**
	 * read bean config
	 * @param config
	 * @param di
	 */
	private static void loadBeans(Config config){
		NodeList nodeList = config.getNodeList("di//bean");
		for (int i = 0; i < nodeList.getLength(); i++) {
			Element node = (Element) nodeList.item(i);
			Node parent = node.getParentNode();
			BeanInfo bean = new BeanInfo();
			bean.id = node.getAttribute("id");
			bean.className = node.getAttribute("class");
			// Bean's parent is "group"
			if (parent.getNodeName().equals("group")) {
				String packageName = ((Element) parent).getAttribute("package");
				// add package info to id
				if (!StringUtils.isEmpty(packageName)) {
					bean.id = packageName + "." + bean.id;
				}
			}
			logger.info("id[" + bean.id + "] class[" + bean.className + "]");
			beanMap.put(bean.id, bean);
		}
	}

	/**
	 * get bean class. パッケージ指定有り
	 *
	 * @param name
	 * @param id
	 * @return
	 */
	static Class<? extends Object> getBeanClass(String group, String id) {
		return getBeanClass(toFullId(group, id));
	}

	/**
	 * beanのreflectionクラスを返す。
	 *
	 * @param id
	 * @return
	 */
//	public static <T> Class<T> getBeanClass(String id) {
//		return (Class<T>) getBeanClass(id);
//	}

	/**
	 * beanのreflectionクラスを返す。
	 *
	 * @param id
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T> Class<T> getBeanClass(String id) {
		// Get FQDN bean info
		BeanInfo bean = getBeanInfo(id);

		return (Class<T>)bean.getClazz();
	}

	/**
	 * get bean instance. パッケージ指定有り
	 *
	 * @param name
	 * @param id
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T> T getBean(String group, String id) {
		return (T)getBean(toFullId(group, id));
	}

	/**
	 * beanのsingletonインスタンスを返す
	 *
	 * @param id
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T> T getBean(Class<T> clazz) {
		// Get FQDN bean info
		BeanInfo bean = getBeanInfo(clazz.getName());

		// now support only singleton
		return (T) bean.getInstance();
	}

	/**
	 * beanのsingletonインスタンスを返す
	 *
	 * @param id
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T> T getBean(String id) {
		// Get FQDN bean info
		BeanInfo bean = getBeanInfo(id);

		return (T)bean.getInstance();
	}

	/**
	 * Make full class name.
	 *
	 * @param group
	 * @param id
	 * @return
	 */
	static String toFullId(String group, String id) {
		String packageName = packageNameMap.get(group);
		if (!StringUtils.isEmpty(packageName)) {
			id = packageName + "." + id;
		}
		return id;
	}

	private static BeanInfo getBeanInfo(String id) {
		// Get FQDN bean info
		BeanInfo bean = beanMap.get(id);
		if (bean == null) {
			// if id is not defined in config file then id is className
			bean = new BeanInfo();
			bean.id = bean.className = id;
			beanMap.put(id, bean);
		}
		return bean;
	}

	/**
	 * Replace singleton instance.
	 *
	 * @param idClass
	 * @param instance
	 */
	public static void setSingletonInstance(Class<?> idClass, Object instance) {
		BeanInfo bean = new BeanInfo();
		bean.id = idClass.getName();
		bean.singletonInstance = instance;
		beanMap.put(bean.id, bean);
	}

	/**
	 * Set bean definition.
	 *
	 * @param idClass
	 * @param instance
	 */
	public static void setBean(String id, String className){
		BeanInfo bean = new BeanInfo(id, className);
		beanMap.put(id, bean);
	}
	public static void setBean(Class<?> idClass, Class<?> beanClass){
		BeanInfo bean = new BeanInfo(idClass.getName(), beanClass.getName());
		beanMap.put(bean.id, bean);
	}
}
