/**
 * $Id: DBManager.java 509 2012-09-20 14:43:25Z dobashi $
 *
 * Copyright Lavans Networks Inc.
 */
package com.lavans.lacoder2.sql;

import java.io.FileNotFoundException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.lavans.lacoder2.lang.StringUtils;
import com.lavans.lacoder2.sql.bind.BindConnection;
import com.lavans.lacoder2.sql.cluster.ClusterConnectionPool;
import com.lavans.lacoder2.util.Config;

/**
 * DBManager.
 * ログはdefaultのロガーに書き出すので、別のログファイルに
 * 出したい場合はDBManagerを使う前にLogger#init()を行うこと。
 *
 * @author	dobashi
 * @version	1.0
 */
public class DBManager{
	/** ロガー。debug用 */
	private static Logger logger = LoggerFactory.getLogger(DBManager.class);

	public static final String DEFAULT_DATABASE = "default";
	
	/**
	 * 設定ファイルのセクション名。
	 */
	private static final String CONFIG_SECTION="database";

	/**
	 * Database一覧。ConnectionPoolを保存する配列。
	 */
	private static Map<String, ConnectionPool>  dbMap = null;

	/**
	 * 初期化。
	 */
	static{
		init();
	}

	/**
	 * 初期化。
	 * @throws FileNotFoundException
	 */
	public static void init(){
		// databaseセクションの取得 ----------------------------
		dbMap = new ConcurrentHashMap<String, ConnectionPool>();
		Config config = Config.getInstance();
		// database node
		Element conf = (Element)config.getNode(CONFIG_SECTION);
		NodeList nodeList= null;
		nodeList = config.getNodeList(CONFIG_SECTION+"/*");
		if(nodeList==null){	// 設定ファイルにdatabase指定が無い場合は
			return;			// なにもしない。
		}

		// 統計情報、接続管理 ----------------------------------
		boolean statistics = false;
		try{
			statistics = Boolean.valueOf(conf.getAttribute("statistics")).booleanValue();
		}catch (Exception e) {
			// 失敗したらfalseのまま
		}

		String name=null,driver=null,user=null,pass=null,validSql=null,max=null,init=null;
		List<String> urlList = null;
		boolean isLogging = true;

		for(int i=0; i<nodeList.getLength(); i++){

			Element connectionNode = (Element)nodeList.item(i);

			name   = connectionNode.getAttribute("name");
			//driver = config.getNodeValue("param[@name='driver']/@value", connectionNode);
			driver = config.getNodeValue("driver", connectionNode);
			urlList = config.getNodeValueList("url", connectionNode);
			user   = config.getNodeValue("user", connectionNode);
			pass   = config.getNodeValue("pass", connectionNode);
			init   = config.getNodeValue("init-connections", connectionNode);
			max    = config.getNodeValue("max-connections", connectionNode);
			validSql  = config.getNodeValue("valid-sql", connectionNode);
			isLogging = Boolean.parseBoolean(config.getNodeValue("logging", connectionNode));

			ConnectionPool pool = null;
			// ClusterConnection判定
			if(urlList.size()==0){
				throw new RuntimeException("/database/url is not defined in config xml "+ Config.CONFIG_FILE);
			}else if(urlList.size()>1){
				pool = new ClusterConnectionPool(driver,urlList,user,pass);
			}else{
				pool = new ConnectionPool(driver,urlList.get(0),user,pass);
			}

			// 最大接続数
			try{
				pool.setMaxConnections(Integer.parseInt(max));
			}catch(NumberFormatException e){}
			// 接続初期数
			try{
				pool.setInitConnections(Integer.parseInt(init));
			}catch(NumberFormatException e){}
			// SQL統計情報
			pool.setStatistics(statistics);

			// 強制チェック
			if(!StringUtils.isEmpty(validSql)){
				pool.setValidSql(validSql);
			}
			// SQLロギング
			pool.setLogging(isLogging);

			// 初期化開始
			try{
				pool.init();
			}catch(Exception e){
				logger.error("ConnectionPool init failed.", e);
			}
			dbMap.put(name,pool);
			logger.debug( "create ConnectionPool["+name+"]");
		}
	}

	/**
	 * Add connection pool instance.
	 * @param name
	 * @param pool
	 */
	public static void addConnectionPool(String name, ConnectionPool pool){
		dbMap.put(name,  pool);
	}

	/**
	 * DBへのコネクション取得
	 */
	public static BindConnection getConnection() throws SQLException {
		return getConnection(DEFAULT_DATABASE);
	}
	/**
	 * DBへのコネクション取得
	 */
	public static BindConnection getConnection(String dbName) throws SQLException {
		ConnectionPool pool = dbMap.get(dbName);
		return pool.getConnection();
	}

	/**
	 * 使用中のコネクション数を返す。
	 * @return
	 */
	public static int getConnectionCount(){
		return getConnectionCount(DEFAULT_DATABASE);
	}

	/**
	 * 使用中のコネクション数を返す。
	 * @return
	 */
	public static int getConnectionCount(String dbName){
		ConnectionPool pool = dbMap.get(dbName);
		return pool.getUseCount();
	}

	/**
	 * 待機中のコネクション数を返す。
	 * @return
	 */
	public static int getPoolCount(){
		return getPoolCount(DEFAULT_DATABASE);
	}

	/**
	 * 待機中のコネクション数を返す。
	 * @return
	 */
	public static int getPoolCount(String dbName){
		ConnectionPool pool = dbMap.get(dbName);
		return pool.getPoolCount();
	}

	/**
	 * コネクション最大数を返す。
	 * @return
	 */
	public static int getMaxConnections(){
		return getMaxConnections(DEFAULT_DATABASE);
	}
	/**
	 * コネクション最大数を返す。DB指定。
	 * @return
	 */
	public static int getMaxConnections(String dbName){
		ConnectionPool pool = dbMap.get(dbName);
		return pool.getMaxConnections();
	}

	/**
	 * コネクション最大数をセットする。DB指定。
	 * @return
	 */
	public static void setMaxConnections(int count){
		setMaxConnections(DEFAULT_DATABASE, count);
	}

	/**
	 * コネクション最大数をセットする。DB指定。
	 * @return
	 */
	public static void setMaxConnections(String dbName, int count){
		ConnectionPool pool = dbMap.get(dbName);
		pool.setMaxConnections(count);
	}

	/**
	 * トランザクションスタート
	 * @return
	 * @throws SQLException
	 */
	public static void startTransaction() throws SQLException{
		startTransaction(DEFAULT_DATABASE);
	}

	/**
	 * トランザクションスタート。DB指定。
	 * @return
	 * @throws SQLException
	 */
	public static void startTransaction(String dbName) throws SQLException{
		ConnectionPool pool = dbMap.get(dbName);
		pool.startTransaction();
	}

	/**
	 * トランザクションコミット
	 * @return
	 * @throws SQLException
	 */
	public static void commit() throws SQLException{
		commit(DEFAULT_DATABASE);
	}

	/**
	 * トランザクションコミット。DB指定。
	 * @return
	 * @throws SQLException
	 */
	public static void commit(String dbName) throws SQLException{
		ConnectionPool pool = dbMap.get(dbName);
		pool.commit();
	}

	/**
	 * トランザクションコミット
	 * @return
	 * @throws SQLException
	 */
	public static void rollback() throws SQLException{
		rollback(DEFAULT_DATABASE);
	}

	/**
	 * トランザクションロールバック。DB指定。
	 * @return
	 * @throws SQLException
	 */
	public static void rollback(String dbName) throws SQLException{
		ConnectionPool pool = dbMap.get(dbName);
		pool.rollback();
	}

	/**
	 * トランザクション実行中かどうかを返す。
	 * @return
	 * @throws SQLException
	 */
	public static boolean isTransaction(){
		return isTransaction(DEFAULT_DATABASE);
	}

	/**
	 * トランザクション実行中かどうかを返す。
	 * @param dbName
	 * @return
	 */
	public static boolean isTransaction(String dbName){
		ConnectionPool pool = dbMap.get(dbName);
		if(pool==null){
			throw new NullPointerException("no such database name:"+ dbName);
		}
		return pool.isTransaction();
	}

	/**
	 * トランザクション実行中かどうかを返す。
	 * すべてのコネクションプールを対象とする。
	 *
	 * @return
	 */
	public static boolean isTransactionAll(){
		for(ConnectionPool pool: dbMap.values()){
			if(pool.isTransaction()){
				return true;
			}
		}
		return false;
	}

	/**
	 * コミット処理。
	 * すべてのコネクションプールを対象とする。
	 *
	 * @return
	 */
	public static void commitAll() throws SQLException{
		for(ConnectionPool pool: dbMap.values()){
			if(pool.isTransaction()){
				pool.commit();
			}
		}
	}
}
