package com.lavans.lacoder2.remote.servlet.converter;

import com.lavans.lacoder2.controller.WebAppConfig;
import com.lavans.lacoder2.di.BeanManager;
import com.lavans.lacoder2.lang.StringUtils;

public class RelativeConverter implements Converter{
	/** Service configuration. */
	private WebAppConfig webAppConfig = BeanManager.getBean(WebAppConfig.class);

	/**
	 * サービスクラスをurlにしたときの文字列を取得。
	 */
	@Override
	public String toUrl(String fqdn) {
		String servicePath = webAppConfig.getServicePath();
		if(StringUtils.isEmpty(servicePath)){
			return fqdn;
		}
		return fqdn.replace(servicePath+".", "");
	}

	/**
	 * サービスのbean idを取得。
	 * toUrlで省略した部分を元に戻す。
	 */
	@Override
	public String toServiceName(String url) {
		return webAppConfig.getServicePath()+url;
	}
}
