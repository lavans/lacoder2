package com.lavans.lacoder2.remote.servlet.converter;



public class FqdnConverter implements Converter{

	/**
	 * サービスクラスをurlにしたときの文字列を取得。
	 */
	@Override
	public String toUrl(String fqdn) {
		return fqdn;
	}

	/**
	 * サービスのbean idを取得
	 */
	@Override
	public String toServiceName(String url) {
		return url;
	}
}
