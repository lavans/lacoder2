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
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import lombok.Getter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lavans.lacoder2.sql.bind.BindConnection;
import com.lavans.lacoder2.sql.bind.impl.BindConnectionImpl;
import com.lavans.lacoder2.sql.dbutils.model.ConnectInfo;
import com.lavans.lacoder2.sql.dbutils.model.Database;
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
	@Getter
	protected final ConnectInfo connectInfo;
	@Getter
	private final Database database;

	/**
	 * NativeDriver.
	 * ConnectionPoolは接続先ごとにインスタンス化されるので、
	 * DriverManagerに管理させる必要はなく、ConnectionPool毎に
	 * 実Driverインスタンスを持つ。
	 */
	private Driver driver = null;

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

	public ConnectionPool(ConnectInfo connectInfo){
		this.database = new Database(connectInfo);
		this.connectInfo = connectInfo;
		init();
	}

	/**
	 * 初期化
	 **/
	public void init() {
		try {
			driver = (Driver) Class.forName(connectInfo.getDriverName()).newInstance();
		} catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
			throw new RuntimeException("DB init failed." + connectInfo, e);
		}
		for (int i = 0; i < connectInfo.getMinSpare(); i++) {
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
	protected synchronized PooledConnection createConnection() {
		checkMaxCount();
		logger.info("create "+ connectInfo.getUrl());

		Properties info = new Properties();
		info.put("user", connectInfo.getUser());
		info.put("password", connectInfo.getPass());

		Connection con= null;
		try {
			con= driver.connect(connectInfo.getUrl(), info);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}

		// 統計情報を収集するなら
		if(connectInfo.isStats()){
			con= new StatsConnection(con);
		}
		BindConnection bcon = new BindConnectionImpl(con);
		PooledConnection pcon = new PooledConnection(this,bcon);

		logger.info("create "+ connectInfo.getUrl());

		return pcon;
	}

	protected void checkMaxCount(){
		// 最大数チェック
		if(sumSize() > connectInfo.getMax()){
			throw new RuntimeException(MSG_ERR_TOOMANYCONNECTIONS + String.format("現在:%d, 最大:%d", sumSize(), connectInfo.getMax()));
		}
	}

	/**
	 * DBへのコネクションチェック。
	 */
	protected boolean checkPooledConnection(PooledConnection conn) throws SQLException{
		logger.debug("connectInfo.getMaxLifeMills "+ connectInfo.getMaxLife() +"isExpired"+conn.isExpired());
		if(connectInfo.getMaxLife()>0 && conn.isExpired()){
			return false;
		}
		return checkConnection(conn);
	}

	/**
	 * DBへのコネクションチェック。
	 * force_checkを再導入することでDB(postgres)を再起動したときにも
	 * 自動で再接続出来る事を確認。
	 */
	protected boolean checkConnection(Connection conn){
		Statement st = null;
		try {
			st = conn.createStatement();
			if(connectInfo.isCheck()){
				st.executeQuery(database.getValidSql());
			}
			return true;			// 例外がなければOK
		}catch (SQLException e) {
			logger.debug("create statement failed", e);
			// ここでキャッチしておかないとgetConnection()自身が
			// 例外を生成してしまう。
		}finally{
			try{ st.close(); }catch(Exception e){ }
		}
		return false;
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
			if(connectInfo.isStats()){
				ConnectionCounter.getInstance().getConnectionTran();
			}
			// 接続チェックはしない。トランザクション中にエラーになるようであれば
			// 利用側がエラーハンドリングする必要がある。
			return conn;
		}

		// 空きプールから捜す
		conn = detach(poolList);
		if(conn!=null && !checkPooledConnection(conn)){		// 接続失敗したら切断処理をしておく。
			try{ conn.getRealConnection().close(); }catch(Exception e){ }
			conn=null;
		}

		if(conn==null){
			conn=createConnection();
		}
		if(logger.isDebugEnabled()) logger.debug(conn.toString());
		useList.add(conn);
		if(logger.isDebugEnabled()) logger.debug(useList.toString());

		// 接続状態管理をする場合 ----------------
		if(connectInfo.isStats() && countStats){
			ConnectionCounter.getInstance().getConnection();
		}

		adjustConnections();

		return conn;
	}

	/**
	 * リストをロックして一つ目の要素を返します。
	 *
	 * @param src
	 * @return リストが空ならnull
	 */
	private <T> T detach(List<T> src){
		synchronized(src){
			if(src.size()>0){
				return src.remove(0);
			}
		}
		return null;
	}

	/**
	 * DBへのコネクション返却
	 */
	public void releaseConnection(PooledConnection conn) throws SQLException{
		releaseConnection(conn, true);
	}
	/**
	 * DBへのコネクション返却
	 *
	 * @param conn
	 * @param countStats StatsでgetConnection()をカウントするかどうか。commit/rollback()からはカウントしない。
	 * @throws SQLException
	 */
	private void releaseConnection(PooledConnection conn, boolean countStats) throws SQLException{
		// トランザクション中かどうかチェック
		PooledConnection connTran = transactionList.get();
		if(connTran==conn){
			// トランザクション中のカウント
			if(connectInfo.isStats()){
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
		if(connectInfo.isStats() && countStats){
			ConnectionCounter.getInstance().releaseConnection();
		}

		// コネクション無効
		if(connectInfo.getMaxLife()>0 && conn.isExpired()){
			conn.physicalClose();
			adjustConnections();
		}else if(poolList.size()>=connectInfo.getMaxSpare()){
			// スペアが十分にある場合
			conn.physicalClose();
		}else{
			// コネクション返却
			poolList.add(conn);
		}
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
			if(connectInfo.isStats()){
				ConnectionCounter.getInstance().startTransactionTran();
			}
			return;
		}

		PooledConnection conn = getConnection(false);
		conn.setAutoCommit(false);
		transactionList.set(conn);
		// 接続状態管理をする場合 ----------------
		if(connectInfo.isStats()){
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
			if(connectInfo.isStats()){
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
		if(connectInfo.isStats()){
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
			if(connectInfo.isStats()){
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
		if(connectInfo.isStats()){
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
	 * @return
	 */
	protected Driver getDriver() {
		return driver;
	}

	/**
	 * @return
	 */
	protected boolean isStats() {
		return connectInfo.isStats();
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
		int result=physicalClose(poolList);
		result += physicalClose(useList);
		return result;
	}

	/**
	 * Close physical connections.
	 *
	 * @param list
	 * @return
	 * @throws SQLException
	 */
	private int physicalClose(List<PooledConnection> list) throws SQLException{
		int result=0;
		synchronized (list) {
			for(PooledConnection con: list){
				con.physicalClose();
				result++;
			}
			list.clear();
		}
		return result;
	}

	private ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);
	private void adjustConnections(){
		logger.debug(String.format("call adjust pool%d, use%d, spare%d, max%d",
				poolList.size(), useList.size(), connectInfo.getMinSpare(), connectInfo.getMax()));
		executorService.schedule(new Runnable() {
			private boolean needsSpare(){
				return poolList.size() < connectInfo.getMinSpare();
			}
			private boolean hasMaxConnections(){
				return (poolList.size() + useList.size())>=connectInfo.getMax();
			}

			@Override
			public void run() {
				while(needsSpare() && !hasMaxConnections()){
					logger.info("pool" + poolList.size() + "use:" + useList.size());
					poolList.add(createConnection());
				}
			}
		}, 1, TimeUnit.SECONDS);
	}

	protected int sumSize(){
		return poolList.size() + useList.size();
	}

	public void close(){
		executorService.shutdown();
	}
}
