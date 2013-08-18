package com.lavans.lacoder2.cache.mock;

import java.util.HashMap;
import java.util.Map;

import com.lavans.lacoder2.cache.CacheHandler;
import com.lavans.lacoder2.cache.CacheHandlerInit;


public class MockCacheHandler implements
	CacheHandler<String, Object>,
	CacheHandlerInit<String, Object>
	{

	@Override
	public long getMaxCacheSize() {
		return 100;
	}

	@Override
	public Object load(String key) {
		return "value";
	}

	@SuppressWarnings("serial")
	@Override
	public Map<String, Object> init() {
		return new HashMap<String, Object>(){{
			put("initKey", "initValue");
		}};
	}

}
