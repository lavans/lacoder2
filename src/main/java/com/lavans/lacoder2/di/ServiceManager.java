package com.lavans.lacoder2.di;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.Enhancer;

import org.slf4j.Logger;

import com.lavans.lacoder2.di.interceptor.RemoteInterceptor;
import com.lavans.lacoder2.di.interceptor.TransactionInterceptor;
import com.lavans.lacoder2.lang.LogUtils;
import com.lavans.lacoder2.remote.node.ServerGroup;


/**
 * Service Manager Implementation for XML file(default).
 * @author dobashi
 *
 */
public class ServiceManager { // implements ServiceManager{
	/** logger */
	private static final Logger logger = LogUtils.getLogger();

	/** Cache of all service */
	private static Map<String, Object> serviceMap = new ConcurrentHashMap<String, Object>();
	private static Map<String, Object> serviceLocalMap = new ConcurrentHashMap<String, Object>();

	/**
	 * Get Service from id with group
	 * @param group
	 * @param id
	 * @return
	 */
	public static <T> T getService(String serverGroup, String group, String id) {
		return getService(serverGroup, BeanManager.toFullId(group, id));
	}

	/**
	 * Get Service from id
	 * @param id
	 * @return
	 */
	public static <T> T getService(String serverGroup, Class<T> clazz) {
		return getService(serverGroup, clazz.getName());
	}
	@SuppressWarnings("unchecked")
	public static <T> T getService(String groupName, String id) {
		ServerGroup serverGroup = ServerGroup.getInstance(groupName);
		if(serverGroup==null){
			throw new RuntimeException("No such group ["+ groupName +"].");
		}
		// check local
		if(serverGroup.isLocal()){
			return (T)getServiceLocal(serverGroup.getLocalClass());
		}
		// search from cache
		Object service = serviceMap.get(id);
		// If service is found, return cache.
		if(service!=null){
			return (T)service;
		}
		// If the service is not cached then create new one.
		Class<? extends Object> clazz;
		try {
			clazz = Class.forName(id);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		};
//		Class<? extends Object> clazz = BeanManager.getBeanClass(id);
		service = createService(clazz, new RemoteInterceptor(serverGroup));
		serviceMap.put(id, service);
//		logger.debug("Add Service "+ id);
		return (T)service;
	}


	/**
	 * Get for local service with Transactional annotation
	 */
	public static <T> T getServiceLocal(String group, String id) {
		return getServiceLocal(BeanManager.toFullId(group, id));
	}

	/**
	 * Get for local service with Transactional annotation
	 *
	 * @param id
	 * @return
	 */
	public static <T> T getServiceLocal(Class<?> clazz) {
		return getServiceLocal(clazz.getName());
	}
	@SuppressWarnings("unchecked")
	public static <T> T getServiceLocal(String id) {
		// search from cache
		Object service = serviceLocalMap.get(id);
		// If service is found, return cache.
		if(service!=null){
			return (T)service;
		}
		// If the service is not cached then create new one.
		Class<? extends Object> clazz = BeanManager.getBeanClass(id);

		service = createService(clazz, new TransactionInterceptor());
		serviceLocalMap.put(id, service);
		if(id.startsWith("Admin")){
			logger.debug("Add ServiceLocal "+ id);
		}
		return (T)service;
	}

	/**
	 * Create enhanced service with callback.
	 * @param id
	 * @param callback
	 * @return
	 */
	private static Object createService(Class<? extends Object> clazz, Callback callback){
		logger.debug(clazz.getSimpleName()+" create.");
		// intercept by CGLIB
		Enhancer enhancer = new Enhancer();
		enhancer.setSuperclass(clazz);
		enhancer.setCallback(callback);
		Object service = enhancer.create();

		return service;
	}
}
