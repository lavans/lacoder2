package com.lavans.lacoder2.experimental.cache;


public interface CacheLoader<K,V>  {
	V load(K key)  throws Exception;
}