package com.lavans.lacoder2.http;


import junit.framework.TestCase;

import org.junit.Test;

public class SimpleHttpClientITest extends TestCase{
	String url = "http://server:8080/context/rest/resources/{ID}";
//	@Test
	public void testPutメソッド(){
		String str = SimpleHttpClient.put(url,"");
		System.out.println(str);

	}

	String base = "http://server:8080/context/rest/resources/";
	@Test
	public void testResponse(){
		String str = SimpleHttpClient.get(url+"test");
		System.out.println(str);
	}
	@Test
	public void testResponseB(){
		String str = SimpleHttpClient.get(url+"testb");
		System.out.println(str);
	}
}
