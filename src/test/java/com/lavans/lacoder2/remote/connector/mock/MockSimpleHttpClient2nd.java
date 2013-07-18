package com.lavans.lacoder2.remote.connector.mock;

import java.io.IOException;
import java.util.Map;


import org.slf4j.Logger;

import com.lavans.lacoder2.http.HttpResponse;
import com.lavans.lacoder2.http.SimpleHttpClient;
import com.lavans.lacoder2.lang.LogUtils;

/**
 * 2回目に成功する("127.0.0.1"には接続できない)HttpClient
 * @author sbisec
 *
 */
public class MockSimpleHttpClient2nd extends SimpleHttpClient{
	private static Logger logger = LogUtils.getLogger();
	
	private static final String ERROR_SERVER="127.0.0.1";
	private String urlStr=null;
	
	protected MockSimpleHttpClient2nd(){
	}
	public void setParameters(String urlStr, String charset, String postData, Method method, int timeout, Map<String, String> requestProperties){
		logger.info(urlStr);
		this.urlStr = urlStr;
	}
	
	protected void connect() throws IOException{
		logger.info(urlStr);
		if(urlStr.contains(ERROR_SERVER)){
			throw new IOException("Connection refused."+ERROR_SERVER);
		}
	}
	@Override
	public HttpResponse request(){
		if(urlStr.contains(ERROR_SERVER)){
			throw new RuntimeException("I/O ERROR."+ERROR_SERVER);
		}
		HttpResponse response = new HttpResponse();
		response.setData(("{'response':'"+ urlStr +"'}").getBytes());
		return response;
	}
}
