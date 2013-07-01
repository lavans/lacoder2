/* $Id: RemoteInterceptor.java 509 2012-09-20 14:43:25Z dobashi $ */
package com.lavans.lacoder2.manager;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import com.lavans.lacoder2.di.BeanManager;
import com.lavans.lacoder2.remote.connector.ConnectManager;
import com.lavans.lacoder2.remote.connector.Connector;
import com.lavans.lacoder2.remote.connector.impl.FixedConnector;
import com.lavans.lacoder2.remote.node.ServerGroup;
import com.lavans.lacoder2.remote.node.ServerNode;

public class ManagerInterceptor implements MethodInterceptor,InvocationHandler{
	/** ConnectManager */
	private ConnectManager connectManager = BeanManager.getBean(ConnectManager.class);

	private Connector connector;
	public ManagerInterceptor(String groupName, String nodeName){
		connector = getConnector(groupName, nodeName);
	}

	@Override
	public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
		if(isLocalOnly(method)){
			return method.invoke(obj, args);
		}

		Object out = connector.execute(method, args);
		return out;
	}


	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		return intercept(proxy, method, args, null);
	}

	/**
	 * 接続先固定コネクタ取得
	 * @param groupName
	 * @param nodeName
	 * @return
	 */
	private Connector getConnector(String groupName, String nodeName){
		ServerGroup group = ServerGroup.getInstance(groupName);
		ServerNode node = group.find(nodeName);
		FixedConnector connector = connectManager.getConnector(node);
		return connector;
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
