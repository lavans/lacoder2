/**
 * $Id: DBManager.java 509 2012-09-20 14:43:25Z dobashi $
 *
 * Copyright Lavans Networks Inc.
 */
package com.lavans.lacoder2.sql;

import java.io.FileNotFoundException;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import lombok.val;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lavans.lacoder2.sql.bind.BindConnection;
import com.lavans.lacoder2.sql.dbutils.model.ConnectInfo;
import com.lavans.lacoder2.sql.dbutils.model.Database;

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
	 * Database一覧。ConnectionPoolを保存する配列。
	 */
	private static Map<String, ConnectionPool>  dbMap = new ConcurrentHashMap<String, ConnectionPool>();

	/**
	 * 初期化。
	 */
	static{
		load("lacoder2.xml", DEFAULT_DATABASE);
	}
	
	/**
	 * 初期化。
	 * @throws FileNotFoundException
	 */
	public static void load(String config, String dbName){
	  val info = new ConnectInfo(config, dbName);
		val pool = new ConnectionPool(info); 
		dbMap.put(dbName, pool);
		logger.debug( "create ConnectionPool["+ dbName +"]");
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
	public static Database getDatabase(){
		return dbMap.get(DEFAULT_DATABASE).getDatabase();
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
	
	/**
	 * 物理切断。
	 * すべてのコネクションプールを対象とする。
	 *
	 * @return
	 */
	public static int physicalClose() throws SQLException{
		int result=0;
		for(ConnectionPool pool: dbMap.values()){
			result += pool.physicalClose();
		}
		return result;
	}
}
