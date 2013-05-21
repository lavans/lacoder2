package com.lavans.lacoder2.remote.connector;

import java.lang.reflect.Method;

import com.lavans.lacoder2.remote.node.ServerGroup;


public interface Connector {
	/**
	 * Execute remote-procedure-call.
	 *
	 * @param method execute method
	 * @param args Method arguments.
	 * @return
	 */
	String execute(Method method, Object[] args);
	
	void setSelector(Selector selector);
	void setServerGroup(ServerGroup group);
}
