package com.lavans.lacoder2.experimental.cache;

import com.lavans.lacoder2.di.BeanManager;
import com.lavans.lacoder2.sql.dao.BaseDao;

public class BaseDaoCacheLoader<K, V> implements CacheLoader<K, V>{
	/** dao */
	private BaseDao baseDao= BeanManager.getBean(BaseDao.class);

	private Class<V> clazz;
	public BaseDaoCacheLoader(Class<V> clazz){
		this.clazz=clazz;
	}

	@Override
	public V load(K key) throws Exception {
		return baseDao.load(clazz, key);
	}

}
