package com.lavans.lacoder2.http.mock;

import java.io.IOException;
import java.util.Map;


import org.slf4j.Logger;

import com.lavans.lacoder2.http.HttpResponse;
import com.lavans.lacoder2.http.SimpleHttpClient;
import com.lavans.lacoder2.lang.LogUtils;

public class MockSimpleHttpClient extends SimpleHttpClient{
	private static Logger logger = LogUtils.getLogger();
	
	private String urlStr=null;
	
	protected MockSimpleHttpClient(){
	}
	public void setParameters(String urlStr, String charset, String postData, Method method, int timeout, Map<String, String> requestProperties){
		logger.info(urlStr);
		this.urlStr = urlStr;
	}
	
	protected void connect() throws IOException{
		logger.info(urlStr);
	}

	public HttpResponse request(){
		HttpResponse response = new HttpResponse();
		response.setData(("{'response':'"+ urlStr +"'}").getBytes());
//		response.setContents("{'response':'"+ urlStr +"'}");
		return response;
	}
}
