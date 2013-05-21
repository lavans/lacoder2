package com.lavans.lacoder2.cache;


import java.io.IOException;
import java.util.concurrent.TimeUnit;


import org.slf4j.Logger;

import com.lavans.lacoder2.di.annotation.Scope;
import com.lavans.lacoder2.di.annotation.Type;
import com.lavans.lacoder2.http.SimpleHttpClient;
import com.lavans.lacoder2.lang.LogUtils;
import com.lavans.lacoder2.lang.PeriodUtils;

/**
 * ローカルにあるHtmlキャッシュ用ハンドリングクラス
 *
 * @author sbisec
 *
 */
@Scope(Type.PROTOTYPE)
public class HtmlCacheHandler implements CacheHandler<String, String>{
	private static final Logger logger = LogUtils.getLogger();
	/**
	 * 一覧キャッシュサイズの設定ファイル定義名
	 */
	@Override
	public long getMaxCacheSize(){
		return Long.MAX_VALUE;
	}

	/**
	 * キャッシュキーのデコード。
	 *
	 * @param key
	 * @return
	 */
	@Override
	public String decode(String key){
		return key;
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
	 * 有効期限を返します。
	 *
	 * @param in キャッシュデータ
	 * @param cached 最終取得した日時
	 */
	@Override
	public ValidTerm getValidTerm(String key){
		// htmlキャッシュではロード処理を分散させるため、
		// ValidTermでの期限端数調整処理を行わせたくない。
		// なので端数をオフセットとして渡して上げる
		long offset = System.currentTimeMillis()%htmlCacheConfig.term;
		logger.debug("term:"+PeriodUtils.prettyFormat(htmlCacheConfig.term)+" offset:"+ PeriodUtils.prettyFormat(offset));
		return new ValidTerm(htmlCacheConfig.term, TimeUnit.MILLISECONDS, offset, TimeUnit.MILLISECONDS);
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
