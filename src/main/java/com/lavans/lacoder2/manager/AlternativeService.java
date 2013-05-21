package com.lavans.lacoder2.manager;

import com.lavans.lacoder2.cache.CacheManager;
import com.lavans.lacoder2.manager.dto.AlternativeAddIn;
import com.lavans.lacoder2.manager.dto.AlternativeAddOut;
import com.lavans.lacoder2.manager.dto.AlternativeDelIn;
import com.lavans.lacoder2.manager.dto.AlternativeDelOut;
import com.lavans.lacoder2.manager.dto.AlternativeGetIn;
import com.lavans.lacoder2.manager.dto.AlternativeGetOut;

import net.sf.cglib.proxy.Enhancer;

public class AlternativeService {
	//private static final Logger logger = LogUtils.getLogger();

	/**
	 * Get Service from id
	 * 
	 * @param id
	 * @return
	 */
	public static AlternativeService getService(String groupName, String nodeName) {
		// intercept by CGLIB
		Enhancer enhancer = new Enhancer();
		enhancer.setSuperclass(AlternativeService.class);
		enhancer.setCallback(new ManagerInterceptor(groupName, nodeName));
		Object service = enhancer.create();

		return (AlternativeService) service;
	}

	/**
	 * オルタナティブ応答を返す正規表現キーの一覧を返します。
	 * 
	 * @param in
	 * @return
	 */
	public AlternativeGetOut get(AlternativeGetIn in){
		AlternativeGetOut out = new AlternativeGetOut();
		out.setAlternativeKeyList(CacheManager.getInstance(in.getCacheName()).getAlternativeKeyList());
		return out;
	}

	/**
	 * オルタナキーを追加します。
	 * @param in
	 * @return
	 */
	public AlternativeAddOut add(AlternativeAddIn in){
		AlternativeAddOut out = new AlternativeAddOut();
		boolean result = CacheManager.getInstance(in.getCacheName()).getAlternativeKeyList().add(in.getKey());
		out.setResult(result);
		return out;
	}

	/**
	 * オルタナキーを追加します。
	 * @param in
	 * @return
	 */
	public AlternativeDelOut del(AlternativeDelIn in){
		AlternativeDelOut out = new AlternativeDelOut();
		boolean result = CacheManager.getInstance(in.getCacheName()).getAlternativeKeyList().remove(in.getKey());
		out.setResult(result);
		return out;
	}
}
