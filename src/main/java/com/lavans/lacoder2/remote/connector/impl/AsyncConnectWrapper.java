package com.lavans.lacoder2.remote.connector.impl;

import java.lang.reflect.Method;
import java.util.Timer;
import java.util.TimerTask;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lavans.lacoder2.remote.connector.Connector;
import com.lavans.lacoder2.remote.connector.Selector;
import com.lavans.lacoder2.remote.node.ServerGroup;

/**
 * Async connector.
 * TODO FutureTask
 *
 * @author sbisec
 *
 */
public class AsyncConnectWrapper implements Connector {
	/** logger */
	private static Logger logger = LoggerFactory.getLogger(GroupConnector.class.getName());

	/** deleagte */
	private Connector connector;
	public AsyncConnectWrapper(Connector connector){
		this.connector = connector;
	}

	/**
	 * Execution method.
	 */
	@Override
	public String execute(Method method, Object[] args){
		TimerTask task = new AsyncTimerTask(method, args);
		Timer timer = new Timer();
		timer.schedule(task, 1);
		//timer.cancel();

		return null;
	}

	/**
	 * Execute in other thread.
	 * 
	 * @author dobashi
	 *
	 */
	private class AsyncTimerTask extends TimerTask{
		private Method method;
		private Object[] args;
		/**
		 * constructor.
		 *
		 * @param className
		 * @param methodName
		 * @param paramTypes
		 * @param args
		 */
		public AsyncTimerTask(Method method, Object[] args){
			this.method = method;
			this.args = args;
		}
		/**
		 * execution method
		 */
		public void run() {
			try {
				connector.execute(method, args);
			} catch (Exception e) {
				logger.error("Async execution is failed.", e);
			}
		};
	}
	
	@Override
	public void setServerGroup(ServerGroup group) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setSelector(Selector selector) {
		throw new UnsupportedOperationException();
	}

	@Override
	public String toString(){
		return getClass().getSimpleName();
	}

}
