package com.lavans.lacoder2.cache;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

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
	@SuppressWarnings("unchecked")
	public static <K,V> CacheManager<K,V> getInstance(String cacheName){
		return (CacheManager<K,V>)cacheMap.get(cacheName);
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
	private LoadingCache<K,V> cache;
	/** 統計情報 */
	private final Statistics stats = BeanManager.getBean(Statistics.class);

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
	private LoadingCache<K, V> createCache(){
		logger.debug(handler.toString());
		return CacheBuilder.newBuilder().
			maximumSize(handler.getMaxCacheSize()).
			build(new CacheLoader<K, V>(){
				@Override
				public V load(K key) throws Exception {
					V value = null;
					if(isStatistics){
						Stopwatch stopwatch = new Stopwatch().start();
						value = handler.load(key);
						stopwatch.stop();
						stats.addData("LOAD :"+key.toString(), stopwatch.elapsed(TimeUnit.MILLISECONDS), CacheManager.class.getName());
					}else{
						value = handler.load(key);
					}
					return value;
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
	public V get(K key) {
		V value = cache.getIfPresent(key);
		if(value!=null){
			// キャッシュヒットログ
			logger.debug("cache hit."+key);
			if(isStatistics){
				stats.addData("CACHE:" + key , 0, CacheManager.class.getName());
			}
		}else{
			try {
				value = cache.get(key);
			} catch (ExecutionException e) {
				throw new RuntimeException(e);
			}
			logger.debug("cache load."+key);
			logger.debug(asMap().keySet().toString());
		}
		return value;
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
	public void put(K key, V value){
		cache.put(key, value);
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
	public void invalidate(K key){
		cache.invalidate(key);
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

	public void refresh(K key){
		cache.refresh(key);
	}
	public long size(){
		return cache.size();
	}
	public ConcurrentMap<K, V> asMap(){
		return cache.asMap();
	}

	/**
	 * Get raw data from Guava cache.
	 *
	 * @param key encoded to json.
	 * @return
	 */
	public V getRaw(K key){
		try {
			return cache.get(key);
		} catch (ExecutionException e) {
			throw new RuntimeException(e);
		}
	}
}
