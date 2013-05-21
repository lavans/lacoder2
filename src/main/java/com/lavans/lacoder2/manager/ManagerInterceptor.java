/* $Id: RemoteInterceptor.java 509 2012-09-20 14:43:25Z dobashi $ */
package com.lavans.lacoder2.manager;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

import net.arnx.jsonic.JSON;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import org.slf4j.Logger;

import com.google.common.base.Stopwatch;
import com.lavans.lacoder2.di.BeanManager;
import com.lavans.lacoder2.lang.LogUtils;
import com.lavans.lacoder2.lang.PeriodUtils;
import com.lavans.lacoder2.remote.connector.ConnectManager;
import com.lavans.lacoder2.remote.connector.Connector;
import com.lavans.lacoder2.remote.connector.impl.FixedConnector;
import com.lavans.lacoder2.remote.node.ServerGroup;
import com.lavans.lacoder2.remote.node.ServerNode;

public class ManagerInterceptor implements MethodInterceptor,InvocationHandler{
	/** Logger */
	private static Logger logger = LogUtils.getLogger();
	/** ConnectManager */
	private ConnectManager connectManager = BeanManager.getBean(ConnectManager.class);

	private Connector connector;
	public ManagerInterceptor(String groupName, String nodeName){
		connector = getConnector(groupName, nodeName);
	}
	
	@Override
	public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
		Stopwatch stopwatch=null;
		String json = connector.execute(method, args);
		if(logger.isDebugEnabled()){
			stopwatch = new Stopwatch().start();
		}
		Object out = JSON.decode(json, method.getReturnType());
		if(logger.isDebugEnabled()){
			logger.debug("decode time:"+PeriodUtils.prettyFormat(stopwatch.stop().elapsed(TimeUnit.MILLISECONDS)));
		}
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

}
