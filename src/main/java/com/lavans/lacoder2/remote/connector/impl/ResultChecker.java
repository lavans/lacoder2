package com.lavans.lacoder2.remote.connector.impl;

public class ResultChecker {
	static void checkThrowable(Object result){
		if(result instanceof RuntimeException){
			throw (RuntimeException)result;
		}else if(result instanceof Throwable){
			throw new RuntimeException((Throwable)result);
		}
	}

}
