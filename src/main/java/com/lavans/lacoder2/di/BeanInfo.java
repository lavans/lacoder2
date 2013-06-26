package com.lavans.lacoder2.di;

import java.lang.reflect.Constructor;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lavans.lacoder2.di.annotation.Scope;
import com.lavans.lacoder2.di.annotation.Type;
import com.lavans.lacoder2.lang.StringUtils;

public class BeanInfo {
	/** logger */
	private static final Logger logger = LoggerFactory.getLogger(BeanInfo.class);
	/** bean info */
	String id;
	String className;
	String initMethod;
	Object singletonInstance = null;
	Type scope = Type.SINGLETON;

	private Class<? extends Object> clazz = null;


	public BeanInfo(){
	}

	public BeanInfo(String id, String className){
		this.id=id;
		this.className=className;
	}

	/**
	 * Classクラスを返す。一度読み込んだらキャッシュして再利用。
	 *
	 * @return
	 */
	public Class<? extends Object> getClazz() {
		if (clazz != null) {
			return clazz;
		}

		// load from ClassLoader
		try {
			// load class
			clazz = Class.forName(className);
			// get @Scpope
			Scope scopeAnno = clazz.getAnnotation(Scope.class);
			if (scopeAnno != null) {
				scope = scopeAnno.value();
			}
		} catch (ClassNotFoundException e) {
			logger.error("bean class is not found[" + className + "]", e);
		}

		return clazz;
	}

	/**
	 * instanceを返す。一度読み込んだらキャッシュして再利用。
	 *
	 * Scope=request/sessionを実装するとActionServletとの関係が蜜になるので今はまだ実装しない。
	 * ActionServletにstatic methodを用意してThreadLocalで実装できそう(未検証)。
	 * sessionに入れるとセッションレプリケーションのパフォーマンスが落ちるので 実装はしばらく様子見
	 *
	 * @return
	 */
	public Object getInstance() {
		// singletonで既に作成済みなら
		if (scope.equals(Type.SINGLETON) && singletonInstance != null) {
			return singletonInstance;
		}

		// load from ClassLoader
		Object instance = newInstance();

		// Call initialize method.
		callInitMethod();

		// Save singleton instance.
		if (scope.equals(Type.SINGLETON)) {
			singletonInstance = instance;
		}

		return instance;
	}

	/**
	 * make new instace
	 *
	 * @return instance
	 */
	private Object newInstance() {
		try {
			Constructor<?> constructor = getClazz().getDeclaredConstructor();
			constructor.setAccessible(true);
			Object instance = constructor.newInstance();
			logger.debug("create newInstance:" + className);
			return instance;
		} catch (Exception e) {
			logger.error("Bean instance cannot created [" + className + "]", e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * Call initialize method.
	 */
	private void callInitMethod() {
		if (StringUtils.isEmpty(initMethod)) {
			return;
		}
		try {
			clazz.getMethod(initMethod, (Class<?>[]) null).invoke(singletonInstance, (Object[]) null);
		} catch (Exception e) {
			logger.error("init method call error [" + className + "#" + initMethod + "()]", e);
		}
	}

	/**
	 * override toString().
	 */
	@Override
	public String toString() {
		return "BeanInfo [id=" + id + ", className=" + className
				+ ", initMethod=" + initMethod + ", clazz=" + clazz
				+ ", instance=" + singletonInstance + "]";
	}


}
