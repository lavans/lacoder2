/* $Id: Statistics.java 509 2012-09-20 14:43:25Z dobashi $
 * create: 2004/07/27
 * (c)2004 Lavans Networks Inc. All Rights Reserved.
 */
package com.lavans.lacoder2.stats;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.lavans.lacoder2.lang.MethodUtils;



/**
 * @author dobashi
 * @version 1.00
 */
public class Statistics{
//	private static Logger logger = LoggerFactory.getLogger(Statistics.class);
	/**
	 * keyとTimeDataを対にして保存するMap.
	 */
	private Map<String, StatsRecord> keyMap = new ConcurrentHashMap<String, StatsRecord>();

	private Statistics(){
	}

	/**
	 * 統計データ追加。
	 * @param key
	 * @param costTime
	 */
	public void addData(String key, long costTime, String classname){
		StatsRecord record = keyMap.get(key);
		if(record==null){
			record = new StatsRecord();
			keyMap.put(key,record);
			record.setKey(key);
		}
		record.addData(costTime);
		// executeを呼び出したクラス#メソッド名を取得
		String method = MethodUtils.getMethodName(classname);
		record.addMethodNames(method);

		//logger.info("実行時間:"+ costTime+"msec "+ key);
	}

	/**
	 * 合計実行時間別統計情報。
	 * @return
	 */
	public Collection<StatsRecord> getRecords(){
		return keyMap.values();
	}
}


