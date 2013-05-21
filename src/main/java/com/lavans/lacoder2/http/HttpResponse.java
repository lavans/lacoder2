package com.lavans.lacoder2.http;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;

/**
 * Http response data.
 * 
 * @author sbisec
 *
 */
public class HttpResponse {
	private int responseCode;
	private Map<String, List<String>> headerFields;
	private String responseMessage;
	private byte[] contents;
	private String contentsString=null;
	private String charset;
	void setCharset(String charset){
		this.charset = charset;
	}
	public Map<String, List<String>> getHeaderFields() {
		return headerFields;
	}
	public void setHeaderFields(Map<String, List<String>> headerFields) {
		this.headerFields = headerFields;
	}
	public int getResponseCode() {
		return responseCode;
	}
	public void setResponseCode(int responseCode) {
		this.responseCode = responseCode;
	}
	public String getResponseMessage() {
		return responseMessage;
	}
	public void setResponseMessage(String responseMessage) {
		this.responseMessage = responseMessage;
	}
	public byte[] getContentsAsBinary() {
		return contents;
	}
	public void setData(byte[] contents) {
		this.contents = contents;
	}
	public String getContents() {
		if(contentsString==null){
			try {
				contentsString = new String(contents, charset);
			} catch (UnsupportedEncodingException e) {
				throw new RuntimeException(e);
			}
		}
		return contentsString;
	}
}
