/* $Id: Statistics.java 509 2012-09-20 14:43:25Z dobashi $
 * create: 2004/07/27
 * (c)2004 Lavans Networks Inc. All Rights Reserved.
 */
package com.lavans.lacoder2.sql.stats;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lavans.lacoder2.lang.MethodUtils;


/**
 * @author dobashi
 * @version 1.00
 */
public class ConnectionCounter{
	private static Logger logger = LoggerFactory.getLogger(ConnectionCounter.class);

	/**
	 * Singletonのインスタンス。
	 */
	private static ConnectionCounter instance= new ConnectionCounter();

	/**
	 * 接続状況保管用Map。
	 */
	private Map<String, int[]> countConnMap = null;
	/**
	 * トランザクション中
	 */
	private Map<String, int[]> countConnTranMap = null;
	private Map<String, int[]> countTranMap = null;

	/**
	 * 接続数カウント用配列の定義。
	 * count[COUNT_GET]に取得回数が、count[COUNT_RELEASE]に返却回数が入る。
	 */
	private static final int COUNT_GET=0;
	private static final int COUNT_RELEASE=1;
	private static final String CLASSNAME=ConnectionCounter.class.getName();
	private static final int COUNT_STARTTRAN=0;
	private static final int COUNT_COMMIT=1;
	private static final int COUNT_ROLLBACK=2;
	private static final int COUNT_STARTTRAN_TRAN=3; // トランザクション中に再度startTransactionが呼ばれた
	private static final int COUNT_COMMIT_NOTRAN=4;
	private static final int COUNT_ROLLBACK_NOTRAN=5;

	private ConnectionCounter(){
		countConnMap = new ConcurrentHashMap<String, int[]>();
		countConnTranMap = new ConcurrentHashMap<String, int[]>();
		countTranMap = new ConcurrentHashMap<String, int[]>();
	}

	public static ConnectionCounter getInstance(){
		return instance;
	}

	/**
	 * コネクション貸し出し状況カウントアップ
	 *
	 */
	public void getConnection(){
		getConnectionTran(countConnMap, CLASSNAME);
	}
	public void getConnectionTran(){
		getConnectionTran(countConnTranMap, CLASSNAME);
	}
	public void getConnection(String className){
		getConnectionTran(countConnMap, className);
	}
	public void getConnectionTran(String className){
		getConnectionTran(countConnTranMap, className);
	}
	private void getConnectionTran(Map<String, int[]> map, String className){
		String method = MethodUtils.getMethodName(className).split(":")[0];	// 行番号は省く
		synchronized (map) {
			int[] count = map.get(method);
			if(count==null){
				count = new int[2];
				count[COUNT_GET]=1;
				count[COUNT_RELEASE]=0;
				map.put(method,count);
			}else{
				count[COUNT_GET]++;
			}
		}
	}

	/**
	 * コネクション返却時の統計情報
	 *
	 */
	public void releaseConnection(){
		releaseConnection(countConnMap, CLASSNAME);
	}
	public void releaseConnectionTran(){
		releaseConnection(countConnTranMap, CLASSNAME);
	}
	public void releaseConnection(String className){
		releaseConnection(countConnMap, className);
	}
	public void releaseConnectionTran(String className){
		releaseConnection(countConnTranMap, className);
	}
	private void releaseConnection(Map<String, int[]> map, String className){
		String method = MethodUtils.getMethodName(className).split(":")[0];	// 行番号は省く
		synchronized (map) {
			int[] count = map.get(method);
			if(count==null){
				logger.debug("貸し出してないメソッドから返却された。"+ method + map.toString());
				count = new int[2];
				count[COUNT_GET]=0;
				count[COUNT_RELEASE]=1;
				map.put(method,count);
			}else{
				count[COUNT_RELEASE]++;
			}
		}
	}

	/**
	 * トランザクション管理
	 */
	public void startTransaction(){
		countTran(countTranMap, COUNT_STARTTRAN);
	}
	public void commit(){
		countTran(countTranMap, COUNT_COMMIT);
	}
	public void rollback(){
		countTran(countTranMap, COUNT_ROLLBACK);
	}
	public void startTransactionTran(){
		countTran(countTranMap, COUNT_STARTTRAN_TRAN);
	}
	public void commitNoTran(){
		countTran(countTranMap, COUNT_COMMIT_NOTRAN);
	}
	public void rollbackNoTran(){
		countTran(countTranMap, COUNT_ROLLBACK_NOTRAN);
	}
	private void countTran(Map<String, int[]> map, int field){
		String method = MethodUtils.getMethodName(CLASSNAME).split(":")[0];	// 行番号は省く
		synchronized (map) {
			int[] count = map.get(method);
			if(count==null){
				logger.debug("新規:"+ method);
				count = new int[COUNT_ROLLBACK_NOTRAN+1];
				count[COUNT_STARTTRAN]=0;
				count[COUNT_COMMIT]=0;
				count[COUNT_ROLLBACK]=0;
				count[COUNT_STARTTRAN_TRAN]=0;
				count[COUNT_COMMIT_NOTRAN]=0;
				count[COUNT_ROLLBACK_NOTRAN]=0;
				map.put(method,count);
			}
			count[field]++;
		}
	}

	/**
	 * コネクション貸出状況。
	 * コネクション貸し出し中一覧を表示。
	 *
	 * @return String
	 */
	public String viewConnectionPool(){
		// 統計情報を取っていない場合
		if(countConnMap.size()==0){
			return "no stats data.";
		}

		String result = viewConnectionPool(countConnMap);
		result += "Transaction Connection\n";
		result += viewConnectionPool(countConnTranMap);
		result += "Transaction/Commit/Rollback\n";
		result += viewTran(countTranMap);

		return result;
	}

	private String viewConnectionPool(Map<String, int[]> map){
		Collection<String> col = map.keySet();
		List<String> list = new ArrayList<String>(col.size());
		for(Iterator<String> ite=col.iterator(); ite.hasNext();){
			list.add(ite.next());
		}
		Collections.sort(list);

		StringBuffer buf = new StringBuffer();
		buf.append("Connection List\n");
		for(int i=0; i<list.size(); i++){
			int[] count = map.get(list.get(i));
			buf.append(
			  list.get(i)
			  +"\t:"+ count[COUNT_GET]
			  +"\t:"+ count[COUNT_RELEASE]
			  +"\t:"+ (count[COUNT_GET]-count[COUNT_RELEASE]) +"\n");
		}
		return buf.toString();
	}

	private String viewTran(Map<String, int[]> map){
		Collection<String> col = map.keySet();
		List<String> list = new ArrayList<String>(col.size());
		for(Iterator<String> ite=col.iterator(); ite.hasNext();){
			list.add(ite.next());
		}
		Collections.sort(list);

		StringBuffer buf = new StringBuffer();
		buf.append("Connection List\n");
		for(int i=0; i<list.size(); i++){
			int[] count = map.get(list.get(i));
			buf.append(
			  list.get(i)
			  +"\t:"+ count[COUNT_STARTTRAN]
			  +"\t:"+ count[COUNT_COMMIT]
			  +"\t:"+ count[COUNT_ROLLBACK]
			  +"\t("+ count[COUNT_STARTTRAN_TRAN]
			  +"\t:"+ count[COUNT_COMMIT_NOTRAN]
			  +"\t:"+ count[COUNT_ROLLBACK_NOTRAN]
			  +")\t:"+ (count[COUNT_STARTTRAN]-(count[COUNT_COMMIT]+count[COUNT_ROLLBACK])) +"\n");
		}
		return buf.toString();
	}
}


