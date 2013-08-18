package com.lavans.lacoder2.cache;


import java.io.IOException;

import com.lavans.lacoder2.di.annotation.Scope;
import com.lavans.lacoder2.di.annotation.Type;
import com.lavans.lacoder2.http.SimpleHttpClient;

/**
 * ローカルにあるHtmlキャッシュ用ハンドリングクラス
 *
 * @author sbisec
 *
 */
@Scope(Type.PROTOTYPE)
public class HtmlCacheHandler implements CacheHandler<String, String>{
	/**
	 * 一覧キャッシュサイズの設定ファイル定義名
	 */
	@Override
	public long getMaxCacheSize(){
		return Long.MAX_VALUE;
	}

	/**
	 * 実ロード処理
	 */
	@Override
	public String load(String key){
		try {
			return SimpleHttpClient.Builder
					.simpleHttpClient(htmlCacheConfig.baseUrl+key)
					.withCharset(htmlCacheConfig.charset)
					.build()
					.request()
					.getContents();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 設定情報格納クラス定義。
	 * @author sbisec
	 *
	 */
	public static class HtmlCacheConfig{
		String charset;
		// valid term(millisecond)
		long term;
		String baseUrl;
		String keyUrl;
		long offsetTimeMax;
	}

	private HtmlCacheConfig htmlCacheConfig;
	public void setConfig(HtmlCacheConfig htmlCacheConfig){
		this.htmlCacheConfig = htmlCacheConfig;
	}
}
