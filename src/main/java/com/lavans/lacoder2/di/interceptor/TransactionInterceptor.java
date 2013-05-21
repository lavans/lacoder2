/* $Id: RemoteInterceptor.java 509 2012-09-20 14:43:25Z dobashi $ */
package com.lavans.lacoder2.di.interceptor;

import java.lang.reflect.Method;
import java.sql.SQLException;

import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lavans.lacoder2.di.annotation.Transactional;
import com.lavans.lacoder2.remote.connector.ConnectManager;
import com.lavans.lacoder2.sql.DBManager;

public class TransactionInterceptor implements MethodInterceptor{
	/** Logger */
	private static Logger logger = LoggerFactory.getLogger(ConnectManager.class.getName());

	//@Override
	public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
		String[] dbNames = getTargetDBNames(method);
		// check transactional
		if(dbNames==null){
			return proxy.invokeSuper(obj, args);
		}

		// Do transaction
		startTransaction(dbNames);
		try{
			Object result = proxy.invokeSuper(obj, args);
			commit(dbNames);
			return result;
		}catch(Exception e){
			logger.info("Transaction failed.");
			rollback(dbNames);
			throw e;
		}
	}
	
	/**
	 * Get transactional dbNames.
	 * 
	 * @param obj
	 * @param method
	 * @return Return "null" when method is not transactional.
	 */
	private String[] getTargetDBNames(Method method){
		Transactional transactional = method.getAnnotation(Transactional.class);

		// Check also class
		if(transactional==null){
			method.getDeclaringClass().getAnnotation(Transactional.class);
		}
		
		// Anyway null, so there is no need to transaction.
		if(transactional==null){
			return null;
		}
		
		return transactional.values();
	}
	
	/**
	 * start
	 * @param dbNames
	 * @throws SQLException
	 */
	private void startTransaction(String[] dbNames) throws SQLException{
		for(String dbName: dbNames){
			DBManager.startTransaction(dbName);
		}
	}
	private void commit(String[] dbNames) throws SQLException{
		for(String dbName: dbNames){
			DBManager.commit(dbName);
		}
	}
	private void rollback(String[] dbNames) throws SQLException{
		for(String dbName: dbNames){
			DBManager.rollback(dbName);
		}
	}

	
}
