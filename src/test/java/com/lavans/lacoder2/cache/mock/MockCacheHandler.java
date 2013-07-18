package com.lavans.lacoder2.cache.mock;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.lavans.lacoder2.cache.CacheHandler;
import com.lavans.lacoder2.cache.CacheHandlerAlternative;
import com.lavans.lacoder2.cache.CacheHandlerInit;
import com.lavans.lacoder2.cache.ValidTerm;


public class MockCacheHandler implements
	CacheHandler<String, Object>,
	CacheHandlerAlternative<String, Object>,
	CacheHandlerInit<String, Object>
	{

	@Override
	public long getMaxCacheSize() {
		return 100;
	}

	@Override
	public String decode(String keyStr) {
		return keyStr;
	}

	@Override
	public Object load(String key) {
		return "value";
	}

	@Override
	public ValidTerm getValidTerm(String key) {
		return new ValidTerm(10, TimeUnit.SECONDS);
	}

	@Override
	public Object getAlternative(String in) {
		return "alternativeValue";
	}

	@SuppressWarnings("serial")
	@Override
	public Map<String, Object> init() {
		return new HashMap<String, Object>(){{
			put("initKey", "initValue");
		}};
	}

}
