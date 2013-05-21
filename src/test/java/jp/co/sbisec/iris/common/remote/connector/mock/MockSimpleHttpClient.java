package jp.co.sbisec.iris.common.remote.connector.mock;

import java.io.IOException;
import java.util.Map;

import com.lavans.lacoder2.http.HttpResponse;
import com.lavans.lacoder2.http.SimpleHttpClient;


/**
 * SimpleHttpClientのモック。常に成功する。
 * 
 * @author sbisec
 *
 */
public class MockSimpleHttpClient extends SimpleHttpClient{
//	private static Logger logger = LogUtils.getLogger();
	
	private String urlStr=null;
	
	protected MockSimpleHttpClient(){
	}
	public void setParameters(String urlStr, String charset, String postData, Method method, int timeout, Map<String, String> requestProperties){
		this.urlStr = urlStr;
	}
	
	protected void connect() throws IOException{
	}

	public HttpResponse request(){
		HttpResponse response = new HttpResponse();
		response.setData(("{'response':'"+ urlStr +"'}").getBytes());
		return response;
	}
}
