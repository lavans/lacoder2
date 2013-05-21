package com.lavans.lacoder2.cache;

import java.util.Map;

public interface CacheHandlerInit<K,V> {
	/**
	 * キャッシュの初期読み込みを行います。
	 * 起動時のキャッシュが必要ない場合はnullを返します。
	 * 
	 */
	Map<K,V> init();
}
