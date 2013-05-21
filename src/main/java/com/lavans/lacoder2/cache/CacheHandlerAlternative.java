package com.lavans.lacoder2.cache;

public interface CacheHandlerAlternative<K,V> {
	/**
	 * ダミーデータを返します。
	 * @param in
	 * @return
	 */
	
	V getAlternative(K in);
}
