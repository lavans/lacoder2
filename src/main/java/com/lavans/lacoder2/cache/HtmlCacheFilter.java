package com.lavans.lacoder2.cache;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


import org.slf4j.Logger;

import com.lavans.lacoder2.cache.HtmlCacheHandler.HtmlCacheConfig;
import com.lavans.lacoder2.controller.ActionFilter;
import com.lavans.lacoder2.controller.ActionInfo;
import com.lavans.lacoder2.controller.WebAppConfig;
import com.lavans.lacoder2.controller.util.WriteUtils;
import com.lavans.lacoder2.di.BeanManager;
import com.lavans.lacoder2.lang.LogUtils;
import com.lavans.lacoder2.lang.PeriodUtils;
import com.lavans.lacoder2.util.Config;
import com.lavans.lacoder2.util.ParameterUtils;

/**
 * Htmlキャッシュフィルター。
 * 設定ファイルからHtmlキャッシュの設定情報を読み込み、Htmlキャッシュハンドラーにセットします。
 * 複数のHtmlキャッシュが必要な場合は別のFilterクラスを作成し、それぞれ設定項目名などを
 * 定義する必要が有ります。
 *
 * @author sbisec
 */
public class HtmlCacheFilter extends ActionFilter {
	/** logger */
	private static final Logger logger = LogUtils.getLogger();

	/** 設定ファイルのキー名 */
	private static final String CONFIG_BASE_URL ="HtmlCacheFilter.baseUrl";
	private static final String CONFIG_KEY_URL ="HtmlCacheFilter.keyUrl";
	private static final String CONFIG_TERM ="HtmlCacheFilter.term";
	private static final String CONFIG_OFFSET_TIME_MAX ="HtmlCacheFilter.offsetTimeMax";

	/** デフォルトの設定値 */
	private static final String DEFAULT_BASE_URL ="http://localhost:8080";
	private static final String DEFAULT_KEY_URL ="/cache/"; // ":cache/"
	private static final String DEFAULT_TERM ="10s";
	private static final long DEFAULT_OFFSET_TIME_MAX =10;
//	private static final long DEFAULT_TERM =TimeUnit.SECONDS.toMillis(10);

	/** webアプリケーション基本設定 */
	private static WebAppConfig webAppConfig = BeanManager.getBean(WebAppConfig.class);

	/** Htmlキャッシュ設定情報 */
	private static HtmlCacheConfig htmlCacheConfig;
	static{
		htmlCacheConfig = initConfig();
	}
	/**
	 * 設定ファイルからこのfilterの設定情報を取得します。
	 * @return
	 */
	private static HtmlCacheConfig initConfig(){
		logger.info("created.");
		Config config = Config.getInstance();
		HtmlCacheConfig htmlCacheConfig = new  HtmlCacheConfig();
		htmlCacheConfig.baseUrl = config.getParameter(CONFIG_BASE_URL, DEFAULT_BASE_URL);
		htmlCacheConfig.keyUrl = config.getParameter(CONFIG_KEY_URL, DEFAULT_KEY_URL);
		htmlCacheConfig.term = PeriodUtils.prettyParse(config.getParameter(CONFIG_TERM, DEFAULT_TERM));
		htmlCacheConfig.offsetTimeMax = config.getParameterLong(CONFIG_OFFSET_TIME_MAX, DEFAULT_OFFSET_TIME_MAX);
		htmlCacheConfig.charset = webAppConfig.getEncoding();
		return htmlCacheConfig;
	}

	private CacheManager<String, String> cache = BeanManager.getBean(CacheManager.class.getName());

	/**
	 * コンストラクタ
	 */
	private HtmlCacheFilter(){
		HtmlCacheHandler handler = BeanManager.getBean(HtmlCacheHandler.class);
		handler.setConfig(htmlCacheConfig);
		cache.setCacheHandler(handler);
	}


	/**
	 * cacheするurlかどうか判定します。
	 */
	@Override
	public boolean isFilter(String uri) {
		logger.debug(uri);
		return uri.startsWith(htmlCacheConfig.keyUrl);
	}

	@Override
	public void preAction(HttpServletRequest request,
			HttpServletResponse response, ActionInfo info) throws Exception {
		String key = request.getRequestURI().replace(htmlCacheConfig.keyUrl, "/")+"?"+ParameterUtils.toStoreString(request.getParameterMap(),htmlCacheConfig.charset);
		String html = cache.get(key);
		WriteUtils writeUtils = BeanManager.getBean(WriteUtils.class);
		writeUtils.writeHtml(response, html, webAppConfig.getEncoding());
	}

}
