package jp.co.sbisec.iris.common.http;

import static org.testng.Assert.assertTrue;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Map;

import mockit.Mocked;

import org.slf4j.Logger;

import com.lavans.lacoder2.http.HttpResponse;
import com.lavans.lacoder2.http.SimpleHttpClient;
import com.lavans.lacoder2.lang.LogUtils;

public class SimpleHttpClientTest {
	private static final Logger logger = LogUtils.getLogger();

//	@Test
	public void static_getメソッド実行() {
		String html = SimpleHttpClient.get("http://www.google.com/search?q=simplehttpclient");
		logger.debug(html);
		assertTrue(html.contains("doctype html"));
	}

//	@Test
	public void static_postメソッド実行() {
		String html = SimpleHttpClient.post("http://lavans.com/", "q=simplehttpclient");
//		logger.debug(html);
		assertTrue(html.contains("html"));
	}

	/**
	 * gzip圧縮確認。
	 * lavans.comのapahceのmod_deflate有効にして
	 * @throws IOException
	 */
//	@Test
	public void gzip圧縮確認() throws IOException {
		// 非圧縮
		Map<String, String> map = new HashMap<>();
		map.put("Accept-Encoding", "");
 		SimpleHttpClient client = SimpleHttpClient.Builder
				.simpleHttpClient("http://lavans.com/")
				.withRequestProperties(map)
				.build();
		HttpResponse response = client.request();
		long before = Long.parseLong(response.getHeaderFields().get("Content-Length").get(0));
		logger.info("before:"+before);
		// 圧縮
		SimpleHttpClient clientA = SimpleHttpClient.Builder
				.simpleHttpClient("http://lavans.com/")
				.build();
 		HttpResponse responseA = clientA.request();
 		long after = Long.parseLong(responseA.getHeaderFields().get("Content-Length").get(0));
		logger.info("after:"+after);

		assertTrue(after<before);
	}


	//	@Test
	public void 存在しないIPに接続したらbuildの時点で例外()  {
		try {
			@SuppressWarnings("unused")
			SimpleHttpClient client = SimpleHttpClient.Builder.simpleHttpClient("http://noexists.lavans.com/").build();
			assertTrue(false);
		} catch (IOException e) {
			assertTrue(true);
		}
	}

//	@Test
	public void request() {
//		throw new RuntimeException("Test not implemented");
	}

//	@Test
	public void URLエンコードなしでget() {
		String str = SimpleHttpClient.get("http://localhost:8080/iris-manager/as/cache/Cache.html?a=%&b=あああ");
		logger.info(str);
	}
	
	@Mocked
	HttpURLConnection con;
//	@Test
	public void 改行ありでpost() {
//		Map<String,String> postData = new HashMap<>();
//		postData.put("a","あああ\nいいい\nううう");
//		postData.put("b","あああ\nいいい\nううう");
//		postData.put("c","あああ\nいいい\nううう");
		String postData = "a=あああ\nいいい\nううう&b=あああ\nいいい\nううう";
		String str = SimpleHttpClient.post("http://localhost:8080/iris-manager/as/cache/Cache.html?a=%&b=あああ", postData);
		logger.info(str);
	}
}
