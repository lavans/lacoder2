package com.lavans.lacoder2.di.interceptor;

import static org.junit.Assert.*;

import java.lang.reflect.Method;

import lombok.extern.slf4j.Slf4j;
import mockit.Deencapsulation;
import mockit.Mocked;

import org.junit.Before;
import org.junit.Test;

import com.lavans.lacoder2.lang.FileUtils;
import com.lavans.lacoder2.remote.node.ServerGroup;

@Slf4j
public class RemoteInterceptorTest {
	RemoteInterceptor target;

	@Mocked
	ServerGroup mockServerGrop;

	@Before
	public void setup(){
		String filename = FileUtils.makeResourceFileName(this.getClass());
		log.info(filename);
//		ServerGroup.load(filename);
		ServerGroup.load("RemoteInterceptorTest.xml");
		ServerGroup.load("logback-sample.xml");
 		ServerGroup.load(filename);
		target = new RemoteInterceptor(ServerGroup.getInstance("bl"));
//		Deencapsulation.setField(target, mockServerGrop);
	}

	@Test
	public void testIntercept() throws Throwable {
		Object obj = new Object();
		Method method = obj.getClass().getMethod("toString", (Class<?>[])null);

		// exec
		target.intercept(obj, method, null, null);

		// check
		// コネクトエラーにならずに実行されればOK
		assertTrue(true);
	}

}
