/**
 * $Id: ConnectionPool.java 509 2012-09-20 14:43:25Z dobashi $
 *
 * Copyright Lavans Networks Inc.
 */
package com.lavans.lacoder2.sql;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lavans.lacoder2.lang.StringUtils;
import com.lavans.lacoder2.sql.bind.BindConnection;
import com.lavans.lacoder2.sql.bind.impl.BindConnectionImpl;
import com.lavans.lacoder2.sql.pool.PooledConnection;
import com.lavans.lacoder2.sql.stats.ConnectionCounter;
import com.lavans.lacoder2.sql.stats.StatsConnection;

/**
 * ConnectionPool。
 * DBManagerからのみ利用される。
 *
 * @author	dobashi
 * @version 1.0
 * @version 1.1 dobashi20040707 接続の有効性はcreateStatement()だけでチェックできるはず
 */
public class ConnectionPool{
	/** ロガー。debug用 */
	private static Logger logger = LoggerFactory.getLogger(ConnectionPool.class);

	/**
	 * DB接続情報
	 */
	private final ConnectInfo connectInfo;
	public ConnectInfo getConnectInfo(){
		return connectInfo;
	}

	/**
	 * NativeDriver.
	 * ConnectionPoolは接続先ごとにインスタンス化されるので、
	 * DriverManagerに管理させる必要はなく、ConnectionPool毎に
	 * 実Driverインスタンスを持つ。
	 */
	private Driver driver = null;

	/**
	 * コネクション最大数。
	 * デフォルトは10。lacoder.xmlのinit_connectionsで変更可能。
	 */
	private int max_connections  = 10;

	/**
	 * 初期コネクション数。
	 * デフォルトは2。lacoder.xmlのinit_connectionsで変更可能。
	 */
	private int init_connections = 2;

	/**
	 * 空きコネクションリスト。
	 */
	private final List<PooledConnection> poolList = Collections.synchronizedList(new ArrayList<PooledConnection>());
	/**
	 * 使用中コネクションリスト。
	 */
	private final List<PooledConnection> useList  = Collections.synchronizedList(new ArrayList<PooledConnection>());

	/**
	 * トランザクション管理
	 */
	private final ThreadLocal<PooledConnection> transactionList = new ThreadLocal<PooledConnection>();

	protected static final String MSG_ERR_TOOMANYCONNECTIONS = "接続数が最大値を超えています。";
	protected static final String SQLSTATE_CONNECTION_EXCEPTION = "08000";
	public static final int ERR_CONNECTION_OVERFLOW = 1;

	/**
	 * 強制チェックフラグ。
	 */
	private boolean isForceCheck=false;
	/**
	 * 強制チェック用SQL。
	 */
	private String validSql=null;


	/**
	 * コンストラクタ。
	 **/
	public ConnectionPool(String driverName, String url,String user,String pass){
		connectInfo = new ConnectInfo();
		connectInfo.setDriverName(driverName);
		connectInfo.setUrl(url);
		connectInfo.setUser(user);
		connectInfo.setPass(pass);
	}

	public ConnectionPool(ConnectInfo connectInfo){
		this.connectInfo = connectInfo;
	}

	/**
	 * 接続数最大値設定
	 **/
	public void setMaxConnections(int value){
		max_connections = value;
	}

	/**
	 * 接続数初期値設定
	 **/
	public void setInitConnections(int value){
		init_connections = value;
	}

	/**
	 * 初期化
	 **/
	public void init()
		throws ClassNotFoundException,IllegalAccessException,InstantiationException, SQLException
	{
		driver = (Driver)Class.forName(connectInfo.getDriverName()).newInstance();
		for(int i=0; i<init_connections; i++){
			poolList.add(createConnection());
		}
	}

	/**
	 * DBへのコネクション作成。
	 * 設定ファイルが間違っていてDriverManager#getConnection()の戻りが遅いときに、
	 * 何度もcreateConnectionするのを防ぐためsynchronizedとする。
	 * ただし、DoSアタックによるスタックオーバーフローは避けられないので
	 * 間違った設定ファイルのまま運用すべきでない。
	 */
	protected synchronized PooledConnection createConnection()
	  throws SQLException
	{
		// from java.sql.DriverManager
		Properties info = new Properties();
		if (connectInfo.getUser() != null) {
		    info.put("user", connectInfo.getUser());
		}
		if (connectInfo.getPass() != null) {
		    info.put("password", connectInfo.getPass());
		}

		// 最大数チェック
		if((poolList.size() + useList.size()) >= max_connections){
			throw new SQLException(MSG_ERR_TOOMANYCONNECTIONS, SQLSTATE_CONNECTION_EXCEPTION, ERR_CONNECTION_OVERFLOW);
		}
		Connection conn = null;
		logger.info(connectInfo.toString());
		try {
			conn = driver.connect(connectInfo.getUrl(), info);
		} catch (SQLException e) {
			throw e;
		}

		// 統計情報を収集するならStatsConnectionでラップ
		if(connectInfo.isStatistics()){
			conn = new StatsConnection(conn);
		}

		// BindConnectionでラップ
		BindConnection bcon = new BindConnectionImpl(conn);

		// Connection#close()で物理的に閉じずにDBManagerに
		// 返却するためPooledConnectionでラップする。
		// ConnectionPoolではなくDBManagerを通すのは
		// 統計情報取得時に貸し出し管理を行うため。
		PooledConnection pcon = new PooledConnection(this,bcon);

		return pcon;
	}

	/**
	 * DBへのコネクションチェック。
	 * force_checkを再導入することでDB(postgres)を再起動したときにも
	 * 自動で再接続出来る事を確認。
	 */
	protected boolean checkConnection(Connection conn)
	{
		boolean result = false;
		Statement st = null;
		//ResultSet rs = null;
		try
		{
			st = conn.createStatement();
			if(isForceCheck){
				st.executeQuery(validSql);
			}

			result = true;			// 例外がなければOK
		}catch (SQLException e) {
			// ここでキャッチしておかないとgetConnection()自身が
			// 例外を生成してしまう。
		}finally{
			try{
				st.close();
				//rs.close();
			}catch(Exception e){
			}
		}

		return result;
	}

	/**
	 * DBへのコネクション取得
	 */
	public PooledConnection getConnection() throws SQLException{
		return getConnection(true);
	}
	/**
	 * DBへのコネクション取得
	 *
	 * @param countStats StatsでgetConnection()をカウントするかどうか。startTransaction()からはカウントしない。
	 * @return
	 * @throws SQLException
	 */
	private PooledConnection getConnection(boolean countStats) throws SQLException {
		PooledConnection conn=null;
		// トランザクション中なら該当するコネクションを返す
		conn = transactionList.get();
		if(conn!=null){
			// トランザクション中のカウント
			if(connectInfo.isStatistics()){
				ConnectionCounter.getInstance().getConnectionTran();
			}
			// 接続チェックはしない。トランザクション中にエラーになるようであれば
			// 利用側がエラーハンドリングする必要がある。
			return conn;
		}

		// 空きプールから捜す
		synchronized(poolList){
			if(poolList.size()>0){			// プールがあれば
				conn=poolList.remove(0);	//
				if(!checkConnection(conn)){		// 接続失敗したら
					try{
						conn.getRealConnection().close();
					}catch(Exception e){
					}
					conn=null;
				}
			}
		}

		if(conn==null){
			conn=createConnection();
		}
		if(logger.isDebugEnabled()) logger.debug(conn.toString());
		useList.add(conn);
		if(logger.isDebugEnabled()) logger.debug(useList.toString());

		// 接続状態管理をする場合 ----------------
		if(connectInfo.isStatistics() && countStats){
			ConnectionCounter.getInstance().getConnection();
		}

		return conn;
	}

	/**
	 * DBへのコネクション返却
	 */
	public void releaseConnection(Connection conn) throws SQLException{
		releaseConnection(conn, true);
	}
	/**
	 * DBへのコネクション返却
	 *
	 * @param conn
	 * @param countStats StatsでgetConnection()をカウントするかどうか。commit/rollback()からはカウントしない。
	 * @throws SQLException
	 */
	private void releaseConnection(Connection conn, boolean countStats) throws SQLException{
		// トランザクション中かどうかチェック
		PooledConnection connTran = transactionList.get();
		if(connTran==conn){
			// トランザクション中のカウント
			if(connectInfo.isStatistics()){
				ConnectionCounter.getInstance().releaseConnectionTran();
			}
			// トランザクション中の場合はなにもしない
			return;
		}

		conn.setAutoCommit(true);			// 返却時は必ずcommit()されている状態にする。
		// statement.close()を行ってないものがいたら閉じておく
		if(conn instanceof PooledConnection){
			((PooledConnection)conn).clearStatementList();
		}
		if(logger.isDebugEnabled()) logger.debug(conn.toString() + useList.toString());
		if(!useList.remove(conn)){		// 使用中リストに存在しなければ
			throw new SQLException("This is not my connection.");
		}

		// 接続状態管理をする場合 ----------------
		if(connectInfo.isStatistics() && countStats){
			ConnectionCounter.getInstance().releaseConnection();
		}

		poolList.add((PooledConnection)conn);
	}

	/**
	 * トランザクションスタート
	 * すでにトランザクションがスタートしていてもOK。
	 *
	 * @throws SQLException
	 */
	public void startTransaction() throws SQLException{
		// すでにトランザクション中の場合
		if(isTransaction()){
			if(connectInfo.isStatistics()){
				ConnectionCounter.getInstance().startTransactionTran();
			}
			return;
		}

		PooledConnection conn = getConnection(false);
		conn.setAutoCommit(false);
		transactionList.set(conn);
		// 接続状態管理をする場合 ----------------
		if(connectInfo.isStatistics()){
			ConnectionCounter.getInstance().startTransaction();
		}

	}

	/**
	 * トランザクションコミット
	 * @throws SQLException
	 */
	public void commit() throws SQLException{
		PooledConnection conn = transactionList.get();
		if(conn==null){
			// 接続状態管理をする場合 ----------------
			if(connectInfo.isStatistics()){
				ConnectionCounter.getInstance().commitNoTran();
			}
			// トランザクション中で無い場合
			// エラーにしない
			return;
		}
		conn.commit();
		transactionList.remove();
		releaseConnection(conn, false);
		// 接続状態管理をする場合 ----------------
		if(connectInfo.isStatistics()){
			ConnectionCounter.getInstance().commit();
		}
	}

	/**
	 * トランザクションロールバック
	 * @throws SQLException
	 */
	public void rollback() throws SQLException{
		PooledConnection conn = transactionList.get();
		if(conn==null){
			// 接続状態管理をする場合 ----------------
			if(connectInfo.isStatistics()){
				ConnectionCounter.getInstance().rollbackNoTran();
			}
			// トランザクション中で無い場合
			// エラーにしない
			return;
		}
		conn.rollback();
		transactionList.remove();
		releaseConnection(conn, false);
		// 接続状態管理をする場合 ----------------
		if(connectInfo.isStatistics()){
			ConnectionCounter.getInstance().rollback();
		}
	}

	/**
	 * トランザクション実行中かどうかを返す。
	 * @return
	 */
	public boolean isTransaction(){
		return transactionList.get()!=null;
	}

	/**
	 * @param b
	 */
	public void setStatistics(boolean isStatistics) {
		connectInfo.setStatistics(isStatistics);
	}

	/**
	 * @return
	 */
//	public Log getLogger() {
//		return logger;
//	}

	/**
	 * @param logger
	 */
//	public void setLogger(Log logger) {
//		ConnectionPool.logger = logger;
//	}

	/**
	 * @return
	 */
	protected int getMaxConnections() {
		return max_connections;
	}

	/**
	 * @return
	 */
	protected Driver getDriver() {
		return driver;
	}

	/**
	 * @return
	 */
	protected List<PooledConnection> getPoolList() {
		return poolList;
	}

	/**
	 * @return
	 */
	protected boolean isStatistics() {
		return connectInfo.isStatistics();
	}

	/**
	 * @return
	 */
	protected List<PooledConnection> getUseList() {
		return useList;
	}

	/**
	 * @param validSql validSql を設定。
	 */
	public void setValidSql(String validSql) {
		this.validSql = validSql;
		if(!StringUtils.isEmpty(validSql)){
			isForceCheck=true;
		}
	}
	/**
	 * @return isLogging を戻します。
	 */
	public boolean isLogging() {
		return connectInfo.isLogging();
	}
	/**
	 * @param isLogging isLogging を設定。
	 */
	public void setLogging(boolean isLogging) {
		connectInfo.setLogging(isLogging);
	}

	/**
	 * 現在使用中のコネクション数を返す。
	 * @return
	 */
	public int getUseCount(){
		return useList.size();
	}

	/**
	 * 現在待機中のコネクション数を返す。
	 * @return
	 */
	public int getPoolCount(){
		return poolList.size();
	}

	/**
	 * Close all physical connections.
	 *
	 * @return
	 * @throws SQLException
	 */
	public int physicalClose() throws SQLException{
		int result=0;
		for(PooledConnection con: useList){
			con.close();
			result++;
		}
		return result;
	}
}
