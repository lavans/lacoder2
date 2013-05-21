package com.lavans.lacoder2.cache;

import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import net.arnx.jsonic.JSON;
import net.arnx.jsonic.JSONException;

import org.slf4j.Logger;

import com.google.common.base.Stopwatch;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.lavans.lacoder2.di.BeanManager;
import com.lavans.lacoder2.di.annotation.Scope;
import com.lavans.lacoder2.di.annotation.Type;
import com.lavans.lacoder2.lang.LogUtils;
import com.lavans.lacoder2.lang.StringUtils;
import com.lavans.lacoder2.stats.Statistics;
import com.lavans.lacoder2.util.Config;

@Scope(Type.PROTOTYPE)
public class CacheManager<K,V> {
	/** logger */
	private static final Logger logger = LogUtils.getLogger();
	/** キャッシュサイズデフォルト */
	public static final long DEFAULT_CACHE_SIZE=Long.MAX_VALUE;

	public static final String CONFIG_CACHE_STATS = "CacheManager.statistics";
	private static boolean isStatistics=false;
	static {
		try {
			isStatistics = Boolean.parseBoolean(Config.getInstance().getParameter(CONFIG_CACHE_STATS));
		} catch (Exception e) {
		}
	}
	
	/** キャッシュマネージャーのインスタンスを管理 */
	private static Map<String, CacheManager<?, ?>> cacheMap = new ConcurrentHashMap<>();

	/**
	 * キャッシュマネージャーを返します。
	 * 
	 * @param cacheName キャッシュ名。キャッシュハンドラーの"Handler"を除いた部分になります。
	 * 例："ChartCacheHandler" -> "ChartCache"
	 * @return
	 */
	public static CacheManager<?,?> getInstance(String cacheName){
		return cacheMap.get(cacheName);
	}

	/**
	 * キャッシュ名一覧を返します。
	 * 
	 * @param cacheName
	 * @return
	 */
	public static Collection<String> getCacheNames(){
		return cacheMap.keySet();
	}

	
	/** キャッシュハンドラー */
	private CacheHandler<K,V> handler;
	/** キャッシュインスタンス作成。 */
	private LoadingCache<String, CacheValue<V>> cache;
	/** 統計情報 */
	private Statistics stats = BeanManager.getBean(Statistics.class);

	/**
	 * コンストラクタ。
	 */
	public CacheManager(){
	}

	/**
	 * キャッシュハンドラーの設定
	 * @param handler
	 */
	public void setCacheHandler(CacheHandler<K, V> handler){
		this.handler = handler;
		cache = createCache();
		// インスタンス管理
		String name = handler.getClass().getSimpleName().replace("Handler","");
		if(StringUtils.isEmpty(name)){
			String names[] = handler.getClass().getName().split("\\.");
			name = names[names.length-1];
		}
		init();
		cacheMap.put(name, this);
	}

	/**
	 * キャッシュインスタンス作成。
	 * @return
	 */
	private LoadingCache<String, CacheValue<V>> createCache(){
		logger.debug(handler.toString());
		return CacheBuilder.newBuilder().
			maximumSize(handler.getMaxCacheSize()).
			build(new CacheLoader<String, CacheValue<V>>(){
				@Override
				public CacheValue<V> load(String keyStr) throws Exception {
					K in = handler.decode(keyStr);
					V out = null;
					if(isStatistics){
						Stopwatch stopwatch = new Stopwatch().start();
						out = handler.load(in);
						stopwatch.stop();
						stats.addData("LOAD :"+keyStr, stopwatch.elapsed(TimeUnit.MILLISECONDS), CacheManager.class.getName());
					}else{
						out = handler.load(in);
					}
					return new CacheValue<V>(out, handler.getValidTerm(in).getExpireDate());
				}
			});
	}

	/**
	 * キャッシュからのデータ取得処理。
	 *
	 * @author sbisec
	 *
	 * @param <O>
	 */
	public V get(K in) {
		V out;
		try {
			String key = convertKey(in);
			// ダミー応答の必要があるなら
			if(isAlternative(key)){
				logger.info("cache Alternative."+key);
				return getAlternativeValue(in);
			}
			out = getValue(key);
		} catch (JSONException | ExecutionException e) {
			throw new RuntimeException(e);
		}
		return out;
	}


	/**
	 * ダミーキーの入れ物
	 */
	private Collection<String> alternativeKeyList = new ConcurrentSkipListSet<>();

	/**
	 * ダミーキー一覧を返します。
	 * @return
	 */
	public Collection<String> getAlternativeKeyList(){
		return alternativeKeyList;
	}

	/**
	 * ダミーを返す必要が有るか判定します。
	 * @param key
	 * @return ダミーを返すならtrue。
	 */
	private boolean isAlternative(String key){
		// Alternativeを返すように実装されているか
		if(!(handler instanceof CacheHandlerAlternative)){
			return false;
		}

		// 登録したダミーキーとマッチするか
		for(String regex: alternativeKeyList){
			if(key.matches(regex)){
				return true;
			}
		}
		return false;
	}

	/**
	 * ダミー値を取得。
	 * @param in
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private V getAlternativeValue(K in){
		return ((CacheHandlerAlternative<K, V>)handler).getAlternative(in);
	}

	/**
	 * outをキャッシュから取得。有効期限が切れていれば無効にしてから再取得。
	 * @param key
	 * @return
	 * @throws ExecutionException
	 */
	private V getValue(String key) throws ExecutionException{
		CacheValue<V> value = cache.getIfPresent(key);
		if(value!=null){
			// キャッシュヒットログ
			logger.info("cache hit."+key);
			// キャッシュの有効期限をチェック
			if(value.getExpire().before(new Date())){
				logger.debug("cache expired."+key);
				cache.invalidate(key);
				value = cache.get(key);
				logger.debug(asMap().keySet().toString());
			}else{
				if(isStatistics){
					stats.addData("CACHE:" + key , 0, CacheManager.class.getName());
				}
			}
		}else{
			value = cache.get(key);
			logger.debug("cache load."+key);
			logger.debug(asMap().keySet().toString());
		}
		return value.getOut();
	}

	/**
	 * Associates {@code value} with {@code key} in this cache. If the cache previously contained a
	 * value associated with {@code key}, the old value is replaced by {@code value}.
	 *
	 * <p>Prefer {@link #get(Object, Callable)} when using the conventional "if cached, return;
	 * otherwise create, cache and return" pattern.
	 *
	 * @since 11.0
	 */
	public void put(K in, V out){
		CacheValue<V> value = new CacheValue<V>(out, handler.getValidTerm(in).getExpireDate());
		cache.put(convertKey(in), value);
	}

	/**
	 * キーをJSONStringに変換。
	 * 元々Stringの場合はそのまま。
	 *
	 * @param in
	 * @return
	 */
	private String convertKey(Object in){
		String key = null;
		if(in instanceof String){
			key = (String)in;
		}else{
			key = JSON.encode(in);
		}
		return key;
	}

	/**
	 * Copies all of the mappings from the specified map to the cache. The effect of this call is
	 * equivalent to that of calling {@code put(k, v)} on this map once for each mapping from key
	 * {@code k} to value {@code v} in the specified map. The behavior of this operation is undefined
	 * if the specified map is modified while the operation is in progress.
	 */
	public void putAll(Map<? extends K, ? extends V> map){
		for (Map.Entry<? extends K, ? extends V> entry : map.entrySet()) {
			put(entry.getKey(), entry.getValue());
		}
	}

	/**
	 * Discards any cached value for key {@code key}.
	 */
	public void invalidate(K in){
		cache.invalidate(convertKey(in));
	}

	/**
	 * Discards any cached value for keys {@code key}.
	 */
	public void invalidateAll(){
		cache.invalidateAll();
	}

	/**
	 * キャッシュデータ初期化処理。
	 * CacheHandler#init()がnullを返す場合はなにもしません。
	 */
	public void init(){
		if(!(handler instanceof CacheHandlerInit)){
			return;
		}
		@SuppressWarnings("unchecked")
		Map<K,V> map = ((CacheHandlerInit<K,V>)handler).init();
		if(map!=null){
			putAll(map);
		}
	}

	public void refresh(String key){
		cache.refresh(key);
	}
	public long size(){
		return cache.size();
	}
	public ConcurrentMap<String, CacheValue<V>> asMap(){
		return cache.asMap();
	}

	/**
	 * Get raw data from Guava cache.
	 *
	 * @param key encoded to json.
	 * @return
	 */
	public CacheValue<V> getRaw(String key){
		try {
			return cache.get(key);
		} catch (ExecutionException e) {
			throw new RuntimeException(e);
		}
	}
}
