/* $Id: RemoteInterceptor.java 509 2012-09-20 14:43:25Z dobashi $ */
package com.lavans.lacoder2.di.interceptor;

import java.lang.reflect.Method;

import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import org.slf4j.Logger;

import com.lavans.lacoder2.di.BeanManager;
import com.lavans.lacoder2.lang.LogUtils;
import com.lavans.lacoder2.remote.connector.ConnectManager;
import com.lavans.lacoder2.remote.connector.Connector;
import com.lavans.lacoder2.remote.node.ServerGroup;

public class RemoteInterceptor implements MethodInterceptor{
	/** Logger */
	private static Logger logger = LogUtils.getLogger();
	/** ConnectManager */
	private ConnectManager connectManager = BeanManager.getBean(ConnectManager.class);

	private ServerGroup serverGroup;
	public RemoteInterceptor(ServerGroup serverGroup){
		this.serverGroup = serverGroup;
	}

	@Override
	public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
		if(isLocalOnly(method)){
			return method.invoke(obj, args);
		}
		// Remote execute
		Connector connector = connectManager.getConnector(serverGroup);
		if(connector == null){
			logger.error("No connector is valid");
			return null;
		}

		return connector.execute(method, args);
//		Object json = connector.execute(method, args);
//		Object out = JSON.decode((String)json, method.getReturnType());
//		return out;
	}

	private static final String FINALIZE="finalize";
	/**
	 * Check if method is local only or remote method.
	 *
	 * @param method
	 * @return true: local
	 * 			 false: remote
	 */
	private boolean isLocalOnly(Method method){
		if(method.getName().equals(FINALIZE)){
			return true;
		}

		return false;
	}
}
