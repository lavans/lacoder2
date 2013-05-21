package com.lavans.lacoder2.http;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLConnection;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.zip.GZIPInputStream;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.X509TrustManager;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lavans.lacoder2.di.BeanManager;
import com.lavans.lacoder2.di.annotation.Scope;
import com.lavans.lacoder2.di.annotation.Type;
import com.lavans.lacoder2.lang.StringUtils;
import com.lavans.lacoder2.util.ParameterUtils;

/**
 * HttpClient
 *
 * Make easy to get or post request. This class uses URLConnection, so it's simple.
 *
 * Usege:
 * String html = SinpleHttpClient.get(url);	// just get
 * String html = SinpleHttpClient.post(url,data);
 *
 * Or you can write as bellow.
 * <pre>{@code
 * 	SimpleHttpClient client = SimpleHttpClient.Builder
 *			.simpleHttpClient(url);
 *			.withPostData(postData)
 *			.build();
 * HttpResponse response = client.request();
 * String content = response.getContentes
 * }</pre>
 *
 * TODO Post binary with "multipart/form-data"
 * http://blog.oklab.org/?p=136
 *
 * @author dobashi
 *
 */
@Scope(Type.PROTOTYPE)
public class SimpleHttpClient {
	/** logger */
	private static final Logger logger = LoggerFactory.getLogger(SimpleHttpClient.class);

	public enum Method { GET, POST, HEAD }

	// Build parameter
	/** url. required. */
	private String urlStr=null;
	private HttpURLConnection con = null;
	private String charset=null;
	private String postData=null;
	private Method method = Method.GET;
	private int timeout;
	private Map<String, String> requestProperties=null;
	
	protected SimpleHttpClient(){
	}
	protected void setParameters(String urlStr, String charset, String postData, Method method, int timeout, Map<String, String> requestProperties){
		//logger.debug("setParameters");
		this.urlStr = urlStr;
		this.charset = charset;
		this.postData = postData;
		this.method = method;
		this.timeout = timeout;
		this.requestProperties = requestProperties;
	}
	
	/**
	 * Simple Get. Convenience method.
	 * @param 接続先URL。
	 * @return 取得したコンテンツ。
	 * @throws RuntimeException IOExceptionが発生した場合。
	 */
	public static String get(String url) {
		try {
			SimpleHttpClient client = Builder.simpleHttpClient(url)
					.build();
			return client.request().getContents();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Simple Post. Convenience method.
	 * @param url 接続先URL。
	 * @param data POSTするデータ。{@code "key1=value1&key2=value2&..."}形式。
	 * @return 取得したコンテンツ。
	 * @throws RuntimeException IOExceptionが発生した場合。
	 */
	public static String post(String url, String data) {
		try {
			SimpleHttpClient client = Builder.simpleHttpClient(url)
					.withPostData(data)
					.build();
			return client.request().getContents();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	public static String post(String url, Map<String,String> params) {
		try {
			SimpleHttpClient client = Builder.simpleHttpClient(url)
					.withPostData(params)
					.build();
			return client.request().getContents();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 接続処理。接続に失敗した場合はIOExceptionを返します。
	 * URL異常などの製造バグはRuntimeExceptionを返します。
	 * 
	 * @throws IOException IOException  if an I/O exception occurs.
	 */
	protected void connect() throws IOException{
		con = createConnection();
		setConnectionOption();
		con.connect();
	}

	private HttpURLConnection createConnection() throws IOException{
		URL url=null;
		try {
			url = new URL(urlStr);
		} catch (MalformedURLException e) {
			// invalid protocol
			throw new RuntimeException(e);
		}
		return (HttpURLConnection)url.openConnection();
	}

	/**
	 * Set connection parameters
	 * @param con
	 */
	private void setConnectionOption() {
		try {
			con.setRequestMethod(method.name());
		} catch (ProtocolException e) {
			// invalid method name
			throw new RuntimeException(e);
		}
		for(Entry<String, String> entry: requestProperties.entrySet()){
			con.setRequestProperty(entry.getKey(), entry.getValue());
		}
		if(method==Method.POST){
			con.setDoOutput(true);
		}
		
		con.setConnectTimeout(timeout);
		con.setReadTimeout(timeout);
	}

	/**
	 * Request to server.
	 *
	 * @return http response.
	 * @throws IOException
	 */
	public HttpResponse request() throws IOException {
		// debug
		logger.debug(urlStr + (postData==null?"":" post["+ postData +"]"));

		// POSTの時
		if(postData!=null){
			doPost(con);
		}
		try(InputStream is = getInputStream(con)){
			return makeResponse(con,is);
		}
	}

	/**
	 * URLコネクションからInputStreamを取り出します。
	 * gzipの場合はGZipInputStreamが返ります。
	 * 
	 * @param con
	 * @return
	 * @throws IOException
	 */
	private InputStream getInputStream(HttpURLConnection con) throws IOException{
		InputStream is = con.getInputStream();
		if(isGzipped(con)){
			is = new GZIPInputStream(is);
		}
		return is;
	}

	/**
	 * gzip圧縮かどうか判定します。
	 * @param con
	 * @return
	 */
	private boolean isGzipped(HttpURLConnection con){
		boolean result=false;
		List<String> contentEncodings = con.getHeaderFields().get("Content-Encoding");
		if (contentEncodings != null){
			for (String contentEncoding: contentEncodings){
				if (contentEncoding.equals("gzip")){
					result = true;
					break;
				}
			}
		}
		return result;
	}

	/**
	 * POSTデータ処理
	 * @param con
	 * @param os
	 * @throws IOException
	 */
	private void doPost(URLConnection con) throws IOException{
		try(PrintStream os = new PrintStream(con.getOutputStream())){
			os.print(postData);
			os.flush();
		}
	}

	/**
	 * Http応答クラスの作成
	 * @param con
	 * @param is
	 * @return
	 * @throws IOException
	 */
	private HttpResponse makeResponse(HttpURLConnection con, InputStream is) throws IOException{
		HttpResponse result = new HttpResponse();
		result.setCharset(charset);
		result.setResponseCode(con.getResponseCode());
		result.setResponseMessage(con.getResponseMessage());
		result.setHeaderFields(con.getHeaderFields());
		result.setData(readBoby(is));
		return result;
	}

	/**
	 * 応答読み込み
	 * @param is
	 * @return
	 * @throws IOException
	 */
	private byte[] readBoby(InputStream is) throws IOException{
		ByteArrayOutputStream os = new ByteArrayOutputStream(4096);
		byte[] buffer = new byte[4096];
		int len;
		while ((len = is.read(buffer)) > 0) {
			os.write(buffer, 0, len);
		}
		os.close();
		return os.toByteArray();
	}
//	/**
//	 * 応答をStringに変換して返す。
//	 * @param is
//	 * @return
//	 * @throws IOException
//	 */
//	private String readContents(BufferedReader is) throws IOException{
//        StringBuilder builder = new StringBuilder();
//		String s;
//		while((s=is.readLine())!=null){
////			logger.info(s);
//			builder.append(s).append("\n");
//		}
//
//		return builder.toString();
//	}

	public static class Builder {
		private static final String DEFAULT_CHARSET="UTF-8";
		private static final int DEFAULT_TIMEOUT = 5000;
		/**
		 * Constructor.
		 * 
		 * @param url string for connect.
		 */
		private Builder(String urlStr){
			this.urlStr = urlStr;
		}
		
		/**
		 * Create new Builder instance with url.
		 * 
		 * TODO staticをやめて単体テストのときにBuilderごと差し替え可能にする。
		 * 
		 * @param url string for connect
		 * @return Builder instance.
		 */
		public static Builder simpleHttpClient(String urlStr){
			return new Builder(urlStr);
		}
		
		/** url. required. */
		private String urlStr=null;
		private String charset=DEFAULT_CHARSET;
		private String query=null;
		private String postData=null;
		private Map<String,String> postParams=null;
		private Method method = Method.GET;
		private int timeout = DEFAULT_TIMEOUT;
		@SuppressWarnings("serial")
		private Map<String, String> requestProperties=new HashMap<String, String>(){{
			put("User-Agent", "HttpClient");
			put("Accept-Encoding", "gzip,deflate");
		}};

		/**
		 * charsetをセットする。ビルダー。
		 * 読み込み時/PostデータURLエンコード時のCharsetを指定します。
		 *
		 * @param url
		 * @return
		 */
		public Builder withCharset(String charset){
			this.charset = charset;
			return this;
		}

		/**
		 * queryをセットする。ビルダー。
		 * GETメソッドの時のQueryString。
		 * urlの後ろに"?aaa=ccc"という形式で付加するのと同じ。
		 *
		 * @param url
		 * @return
		 */
		public Builder withQuery(String query){
			this.query = query;
			return this;
		}

		/**
		 * postDataをセットする。ビルダー。
		 * POSTメソッドで渡すデータ。
		 * postDataをセットしたらメソッドは自動的にPOST
		 *
		 * @param url
		 * @return
		 */
		public Builder withPostData(String postData){
			if(!StringUtils.isEmpty(postData)){
				this.postData = postData;
				this.method = Method.POST;
			}
			return this;
		}

		/**
		 * postDataをセットする。ビルダー。
		 * POSTメソッドで渡すデータ。Map形式。
		 * 既にセット済みのデータがある場合は上書きされる。
		 * デフォルトではUTF-8URLエンコードするので、charset変更の必要がある場合はこのメソッドを呼ぶ前に
		 * setCharset(String)する。
		 *
		 * @param url
		 * @return
		 */
		public Builder withPostData(Map<String, String> postParams){
			this.postParams = postParams;
			this.method = Method.POST;
			return this;
		}

		/**
		 * requetPropetriesをセットする。ビルダー。
		 * {@link URLConnection#setRequestProperty(String, String)}でセットするプロパティ。
		 *
		 * @param url
		 * @return
		 */
		public Builder withRequestProperties(Map<String, String> requestProperties){
			this.requestProperties.putAll(requestProperties);
			return this;
		}

		/**
		 * timeoutをセットする。ビルダー。
		 * connectとreadのtimeout値。
		 *
		 * @param int timeout
		 * @return
		 */
		public Builder withTimeout(int timeout){
			this.timeout = timeout;
			return this;
		}

		/**
		 * Build SimpleHttpClient instance.
		 * Serverに接続して失敗した場合は例外を生成します。
		 * 
		 * @return
		 * @throws IOException if I/O 
		 */
		public SimpleHttpClient build() throws IOException{
			if(query!=null){
				urlStr += "?"+query;
			}
			if(postParams!=null){
				postData = ParameterUtils.toStoreString(ParameterUtils.convertToStringArrayMap(postParams),charset);
			}
			SimpleHttpClient client = BeanManager.getBean(SimpleHttpClient.class);
			client.setParameters(urlStr, charset, postData, method, timeout, requestProperties);
			client.connect();
			return client;
		}
	}
	
	/**
	 * https接続時にssl証明書の検証をオフにする。
	 */
	public static void ignoreSSLVerify() {
		X509TrustManager[] tm = new X509TrustManager[] { new X509TrustManager() {
			public X509Certificate[] getAcceptedIssuers() { return null; }
			public void checkClientTrusted(X509Certificate[] arg0, String arg) {}
			public void checkServerTrusted(X509Certificate[] arg0, String arg) {}
		} };
		SSLContext ssl;
		try {
			ssl = SSLContext.getInstance("SSL");
			ssl.init(null, tm, null);
		} catch (NoSuchAlgorithmException | KeyManagementException e) {
			throw new RuntimeException(e);
		}
		HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
			public boolean verify(String host1, SSLSession session) { return true;}
		});
		HttpsURLConnection.setDefaultSSLSocketFactory(ssl.getSocketFactory());
	}
}
