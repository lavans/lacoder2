package com.lavans.lacoder2.manager;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

import javax.imageio.ImageIO;

import net.arnx.jsonic.util.Base64;
import net.sf.cglib.proxy.Enhancer;

import com.google.common.base.Optional;
import com.lavans.lacoder2.cache.CacheManager;
import com.lavans.lacoder2.cache.CacheValue;
import com.lavans.lacoder2.lang.StringUtils;
import com.lavans.lacoder2.manager.dto.CaccheGetMapOut;
import com.lavans.lacoder2.manager.dto.CacheClearIn;
import com.lavans.lacoder2.manager.dto.CacheClearOut;
import com.lavans.lacoder2.manager.dto.CacheGetDetailIn;
import com.lavans.lacoder2.manager.dto.CacheGetDetailOut;
import com.lavans.lacoder2.manager.dto.CacheGetIn;
import com.lavans.lacoder2.manager.dto.CacheGetKeyListIn;
import com.lavans.lacoder2.manager.dto.CacheGetKeyListOut;
import com.lavans.lacoder2.manager.dto.CacheGetNameListIn;
import com.lavans.lacoder2.manager.dto.CacheGetNameListOut;
import com.lavans.lacoder2.manager.dto.CacheInitIn;
import com.lavans.lacoder2.manager.dto.CacheInitOut;
import com.lavans.lacoder2.manager.dto.CacheRefreshIn;
import com.lavans.lacoder2.manager.dto.CacheRefreshOut;

public class CacheService {
	//private static final Logger logger = LogUtils.getLogger();

	/**
	 * Get Service from id
	 * 
	 * @param id
	 * @return
	 */
	public static CacheService getService(String groupName, String nodeName) {
		// intercept by CGLIB
		Enhancer enhancer = new Enhancer();
		enhancer.setSuperclass(CacheService.class);
		enhancer.setCallback(new ManagerInterceptor(groupName, nodeName));
		Object service = enhancer.create();

		return (CacheService) service;
	}

	/**
	 * ManagerService取得。
	 * cglibを使わずにProxy利用版。
	 * TODO 未検証。インターフェースにする必要があるかも
	 * 
	 * @param groupName
	 * @param nodeName
	 * @return
	 */
//	public static ManagerService getServiceJdk(String groupName, String nodeName) {
//		InvocationHandler handler = new ManagerInterceptor(groupName, nodeName);
//		Class<?> clazz = Proxy.getProxyClass(ManagerService.class.getClassLoader(), ManagerService.class);
//		try {
//			return (ManagerService) clazz.getConstructor(new Class[] { InvocationHandler.class }).newInstance(
//					new Object[] { handler });
//		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
//				| NoSuchMethodException | SecurityException e) {
//			throw new RuntimeException(e);
//		}
//	}

	
	/**
	 * キャッシュ名一覧を返します。
	 * 
	 * @param in　空
	 * @return
	 */
	public CacheGetNameListOut getNameList(CacheGetNameListIn in) {
		CacheGetNameListOut out = new CacheGetNameListOut();
		out.setCacheNameList(CacheManager.getCacheNames());
		return out;
	}

	/**
	 * キャッシュのキー一覧を返します。
	 * 
	 * @param in.cacheName キャッシュ名
	 * @return
	 */
	public CacheGetKeyListOut getKeyList(CacheGetKeyListIn in) {
		CacheGetKeyListOut out = new CacheGetKeyListOut();
		out.setCacheKeyList(CacheManager.getInstance(in.getCacheName()).asMap().keySet());
		return out;
	}

	/**
	 * キャッシュデータを返します。
	 * 
	 * @param in
	 * @return
	 */
	public CacheGetDetailOut getDetail(CacheGetDetailIn in) {
		CacheGetDetailOut out = new CacheGetDetailOut();
		Object value = CacheManager.getInstance(in.getCacheName()).getRaw(in.getKey());

		// Chartの時うまく返せないのでStringに変換してみる
		out.setCacheValue(convert(value));
		return out;
	}

	/**
	 * JSONで送信できる型に変換します。 Chart画像の場合はそのままJSON.encodeできないのでBase64に変換します。
	 * 
	 * @param out
	 * @return
	 */
	private Object convert(Object value) {
		if (value instanceof CacheValue<?>) {
			Object out = ((CacheValue<?>) value).getOut();
			// ChartOutの時のBufferedImageがJSON.encodeできないので回避処理
			if (out.getClass().getSimpleName().equals("ChartOut")) {
				byte[] data = toBytes(out);
				value = makeCacheValue((CacheValue<?>) value, Base64.encode(data));
			} else if (out instanceof Optional) {
				value = makeCacheValue((CacheValue<?>) value, ((Optional<?>) out).get());
			}
		}

		return value;
	}

	/**
	 * chart-plのChartOut型から、プロジェクト参照無しでリフレクションで
	 * BufferedImageを取得してbyte[]に変換して返します。
	 * 
	 * @param out
	 * @return
	 */
	private byte[] toBytes(Object out) {
		byte[] data;
		try {
			Method method = out.getClass().getMethod("getImg", (Class<?>[]) null);
			BufferedImage img = (BufferedImage) method.invoke(out, (Object[]) null);
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			ImageIO.write(img, "gif", os);
			os.flush();
			data = os.toByteArray();
			os.close();
		} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException
				| InvocationTargetException | IOException e) {
			throw new RuntimeException(e);
		}
		return data;
	}

	/**
	 * 新しいCacheValueクラスを作ります。
	 * 
	 * @param src
	 *            コピー元。expireDateをコピー。
	 * @param out
	 *            新しい値。
	 * @return
	 */
	private CacheValue<Object> makeCacheValue(CacheValue<?> src, Object out) {
		CacheValue<Object> cacheValue = new CacheValue<>();
		cacheValue.setExpire(src.getExpire());
		cacheValue.setOut(out);

		return cacheValue;
	}

	/**
	 * キャッシュをクリアします。
	 * 
	 * @param in
	 * @return
	 */
	public CacheClearOut clear(CacheClearIn in) {
		CacheClearOut out = new CacheClearOut();

		for (String cacheName : in.getCacheNames()) {
			long size = 0;
			// 正規表現指定無しなら全クリア
			if (StringUtils.isEmpty(in.getRegex())) {
				size = CacheManager.getInstance(cacheName).size();
				CacheManager.getInstance(cacheName).invalidateAll();
			} else {
				size = clearCacheRegex(CacheManager.getInstance(cacheName).asMap(), in.getRegex());
			}
			out.addMessage(cacheName + ":" + size);
		}

		return out;
	}

	/**
	 * 正規表現によるキャッシュクリア
	 * 
	 * @param map
	 * @param regex
	 */
	private long clearCacheRegex(Map<String, ?> map, String regex) {
		long count = 0;
		for (String key : map.keySet()) {
			if (key.matches(regex)) {
				map.remove(key);
				count++;
			}
		}
		return count;
	}

	/**
	 * キャッシュをクリアします。
	 * 
	 * @param in
	 * @return
	 */
	public CacheRefreshOut refresh(CacheRefreshIn in) {
		CacheRefreshOut out = new CacheRefreshOut();

		for (String cacheName : in.getCacheNames()) {
			long size = 0;
			// 正規表現指定無しなら全クリア
			if (StringUtils.isEmpty(in.getRegex())) {
				size = refreshCacheAll(CacheManager.getInstance(cacheName));
			} else {
				size = refreshCacheRegex(CacheManager.getInstance(cacheName), in.getRegex());
			}
			out.addMessage(cacheName + ":" + size);
		}

		return out;
	}

	/**
	 * 全キャッシュリフレッシュ
	 * 
	 * @param map
	 * @param regex
	 */
	private long refreshCacheAll(CacheManager<?, ?> cache) {
		long count = 0;
		for (String key : cache.asMap().keySet()) {
			cache.refresh(key);
			count++;
		}
		return count;
	}

	/**
	 * 正規表現によるキャッシュリフレッシュ
	 * 
	 * @param map
	 * @param regex
	 */
	private long refreshCacheRegex(CacheManager<?, ?> cache, String regex) {
		long count = 0;
		for (String key : cache.asMap().keySet()) {
			if (key.matches(regex)) {
				cache.refresh(key);
				count++;
			}
		}
		return count;
	}

	/**
	 * キャッシュを初期化します。
	 * 
	 * @param in
	 * @return
	 */
	public CacheInitOut init(CacheInitIn in) {
		CacheInitOut out = new CacheInitOut();
		for (String cacheName : in.getCacheNames()) {
			CacheManager.getInstance(cacheName).init();
			out.addMessage(cacheName + ":" + CacheManager.getInstance(cacheName).size());
		}
		return out;
	}

	/**
	 * キャッシュMap全体を返します。 重そうなので使わないかな。
	 * 
	 * @param in
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public CaccheGetMapOut getMap(CacheGetIn in) {
		CaccheGetMapOut out = new CaccheGetMapOut();
		ConcurrentMap<?, ?> map = CacheManager.getInstance(in.getCacheName()).asMap();

		out.setCacheMap((ConcurrentMap<String, Object>) map);
		return out;
	}
}
