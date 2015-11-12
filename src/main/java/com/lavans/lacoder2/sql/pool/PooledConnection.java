/* $Id: PooledConnection.java 509 2012-09-20 14:43:25Z dobashi $
 * create: 2005/01/27
 * (c)2005 Lavans Networks Inc. All Rights Reserved.
 */
package com.lavans.lacoder2.sql.pool;

import java.sql.Array;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.DatabaseMetaData;
import java.sql.NClob;
import java.sql.PreparedStatement;
import java.sql.SQLClientInfoException;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Savepoint;
import java.sql.Statement;
import java.sql.Struct;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lavans.lacoder2.sql.ConnectionPool;
import com.lavans.lacoder2.sql.bind.BindCallableStatement;
import com.lavans.lacoder2.sql.bind.BindConnection;
import com.lavans.lacoder2.sql.bind.BindPreparedStatement;

import lombok.Getter;
import lombok.val;


/**
 * con.close()を呼ばなくても、con.close()すると
 * コネクションを返却するように振る舞うためのラッパークラス。
 *
 * 各種Statementを作成するときは、PooledStatementでラップする。
 * PooledStatement#close()では本クラスのremoveStatement()を呼び出すので、
 * Connection#close()が呼ばれずに何度もcreateStatement()/st.close()を
 * 繰り返すような場合に本クラスのstatementList()が肥大していくのを防ぐ。
 * DriverWrapper経由で呼び出された場合など、lavansutilの外側で
 * さらにConnectionPoolするような場合に必要である。
 * @author dobashi
 *
 */
public class PooledConnection implements BindConnection {
	/** ロガー。debug用 */
	private static Logger logger = LoggerFactory.getLogger(PooledConnection.class);

	private BindConnection con=null;
	private ConnectionPool pool = null;

	/** 有効期限 */
	private long expire;
	/** 有効期限が切れているか判定します */
	public boolean isExpired(){
		return expire<System.currentTimeMillis();
	}

	/** close()時にすべてのStatementを自動的に閉じる。 */
	private final List<PooledStatement> statementList = Collections.synchronizedList(new ArrayList<PooledStatement>());

	@Getter private Date lastAccessTime;

	/**
	 * コンストラクタ
	 */
	public PooledConnection(ConnectionPool pool, BindConnection bcon) {
		this.pool = pool;
		this.con = bcon;
		expire=System.currentTimeMillis()+pool.getConnectInfo().getMaxLife();
		if(logger.isDebugEnabled()){
			val sdf = new SimpleDateFormat("HH:mm:ss.SSS");
			logger.debug("now:" + sdf.format(new Date()) + ", expire: " + sdf.format(new Date(expire)));
		}
	}

	@Override
	public String toString(){
		String base = super.toString();
		return base.substring(base.lastIndexOf("."));
	}
	/**
	 * @param schema
	 * @throws SQLException
	 * @see java.sql.Connection#setSchema(java.lang.String)
	 */
	@Override
	public void setSchema(String schema) throws SQLException {
		con.setSchema(schema);
	}

	/**
	 * @return
	 * @throws SQLException
	 * @see java.sql.Connection#getSchema()
	 */
	@Override
	public String getSchema() throws SQLException {
		return con.getSchema();
	}

	/**
	 * @param executor
	 * @throws SQLException
	 * @see java.sql.Connection#abort(java.util.concurrent.Executor)
	 */
	@Override
	public void abort(Executor executor) throws SQLException {
		con.abort(executor);
	}

	/**
	 * @param executor
	 * @param milliseconds
	 * @throws SQLException
	 * @see java.sql.Connection#setNetworkTimeout(java.util.concurrent.Executor, int)
	 */
	@Override
	public void setNetworkTimeout(Executor executor, int milliseconds)
			throws SQLException {
		con.setNetworkTimeout(executor, milliseconds);
	}

	/**
	 * @return
	 * @throws SQLException
	 * @see java.sql.Connection#getNetworkTimeout()
	 */
	@Override
	public int getNetworkTimeout() throws SQLException {
		return con.getNetworkTimeout();
	}

	public BindConnection getRealConnection(){
		return con;
	}

	/**
	 * 子Statementで明示的にclose()をかけた時に、こちらで
	 * 保存しておいたリストから削除する。
	 * @param st
	 * @return
	 */
	public boolean removeStatement(PooledStatement st){
		logger.debug("before:"+ statementList.size());
		boolean result = statementList.remove(st);
		logger.debug("after:"+ statementList.size());

		return result;
	}

	/**
	 * BindConnectionImpleに処理委譲。
	 * @see com.lavans.lacoder2.sql.bind.impl.lavans.util.jdbc.bind.BindConnectionImpl#bindPrepareStatement(java.lang.String)
	 */
	@Override
	public BindPreparedStatement bindPrepareStatement(String sql)
			throws SQLException {
		BindPreparedStatement bst = con.bindPrepareStatement(sql);
		PooledBindPreparedStatement pst = new PooledBindPreparedStatement(this,bst);
		statementList.add(pst);
		return pst;
	}

	/**
	 * BindConnectionImpleに処理委譲。
	 * @see com.lavans.lacoder2.sql.bind.impl.lavans.util.jdbc.bind.BindConnectionImpl#bindPrepareStatement(java.lang.String)
	 */
	@Override
	public BindCallableStatement bindPrepareCall(String sql)
			throws SQLException {
		BindCallableStatement bst = con.bindPrepareCall(sql);
		PooledBindCallableStatement pst = new PooledBindCallableStatement(this,bst);
		statementList.add(pst);
		return pst;
	}

	/**
	 * DBManagerを通じてコネクションプールに返却。
	 */
	@Override
	public void close() throws SQLException{
		lastAccessTime = new Date();
		pool.releaseConnection(this);
//		DBManager.releaseConnection(this,pool);
		// DBManager#releaseConnection()->ConnectionPool#releaseConnection()の中で
		// clearStatementList()を呼び出すのでここで呼ぶ必要はない。
		// ConnectionPoolクラスでpoolListに移し終わっているので、呼んではいけない。
	}

	/**
	 * Close physical connection.
	 * Clear all statement and close real connection.
	 *
	 * @throws SQLException
	 *
	 */
	public void physicalClose() throws SQLException{
		clearStatementList();
		con.close();
	}

	/**
	 * 保存しておいたstatementのクリア。
	 *
	 */
	public void clearStatementList(){
		// close()をかけると本クラスのremoveStatementを呼ぶ。
		while(statementList.size()>0){
			//logger.warn("statementList.size()>0 st.close()の呼び忘れが考えられる", new Exception());
			Statement st = statementList.remove(0);
			if(!(st instanceof PooledStatement)){
				logger.error("PooledでないStatementを保持");
			}else{
				PooledStatement pst = (PooledStatement)st;
				logger.warn("this:"+this.toString() +" st.parent:"+pst.parent.toString());
			}
			try {
				st.close();
			} catch (Exception e) {
			}
		}
		statementList.clear();
	}

	/**
	 * @throws java.sql.SQLException
	 */
	@Override
	public void clearWarnings() throws SQLException {
		con.clearWarnings();
	}
	/**
	 * @throws java.sql.SQLException
	 */
	@Override
	public void commit() throws SQLException {
		con.commit();
	}
	/**
	 * @return
	 * @throws java.sql.SQLException
	 */
	@Override
	public Statement createStatement() throws SQLException {
		Statement st = con.createStatement();
		PooledStatement pst = new PooledStatement(this,st);
		statementList.add(pst);
		return pst;
	}
	/**
	 * @param arg0
	 * @param arg1
	 * @return
	 * @throws java.sql.SQLException
	 */
	@Override
	public Statement createStatement(int arg0, int arg1) throws SQLException {
		Statement st = con.createStatement(arg0, arg1);
		PooledStatement pst = new PooledStatement(this,st);
		statementList.add(pst);
		return pst;
	}
	/**
	 * @param arg0
	 * @param arg1
	 * @param arg2
	 * @return
	 * @throws java.sql.SQLException
	 */
	@Override
	public Statement createStatement(int arg0, int arg1, int arg2)
			throws SQLException {
		Statement st = con.createStatement(arg0, arg1, arg2);
		PooledStatement pst = new PooledStatement(this,st);
		statementList.add(pst);
		return pst;
	}
	/**
	 * @return
	 * @throws java.sql.SQLException
	 */
	@Override
	public boolean getAutoCommit() throws SQLException {
		return con.getAutoCommit();
	}
	/**
	 * @return
	 * @throws java.sql.SQLException
	 */
	@Override
	public String getCatalog() throws SQLException {
		return con.getCatalog();
	}
	/**
	 * @return
	 * @throws java.sql.SQLException
	 */
	@Override
	public int getHoldability() throws SQLException {
		return con.getHoldability();
	}
	/**
	 * @return
	 * @throws java.sql.SQLException
	 */
	@Override
	public DatabaseMetaData getMetaData() throws SQLException {
		return con.getMetaData();
	}
	/**
	 * @return
	 * @throws java.sql.SQLException
	 */
	@Override
	public int getTransactionIsolation() throws SQLException {
		return con.getTransactionIsolation();
	}
	/**
	 * @return
	 * @throws java.sql.SQLException
	 */
	@Override
	public Map<String, Class<?>> getTypeMap() throws SQLException {
		return con.getTypeMap();
	}
	/**
	 * @return
	 * @throws java.sql.SQLException
	 */
	@Override
	public SQLWarning getWarnings() throws SQLException {
		return con.getWarnings();
	}

	/**
	 * @return
	 * @throws java.sql.SQLException
	 */
	@Override
	public boolean isClosed() throws SQLException {
		return con.isClosed();
	}
	/**
	 * @return
	 * @throws java.sql.SQLException
	 */
	@Override
	public boolean isReadOnly() throws SQLException {
		return con.isReadOnly();
	}
	/**
	 * @param arg0
	 * @return
	 * @throws java.sql.SQLException
	 */
	@Override
	public String nativeSQL(String arg0) throws SQLException {
		return con.nativeSQL(arg0);
	}
	/**
	 * @param arg0
	 * @return
	 * @throws java.sql.SQLException
	 */
	@Override
	public CallableStatement prepareCall(String arg0) throws SQLException {
		CallableStatement st = con.prepareCall(arg0);
		PooledCallableStatement pst = new PooledCallableStatement(this,st);
		statementList.add(pst);
		return pst;
	}
	/**
	 * @param arg0
	 * @param arg1
	 * @param arg2
	 * @return
	 * @throws java.sql.SQLException
	 */
	@Override
	public CallableStatement prepareCall(String arg0, int arg1, int arg2)
			throws SQLException {
		CallableStatement st = con.prepareCall(arg0, arg1, arg2);
		PooledCallableStatement pst = new PooledCallableStatement(this,st);
		statementList.add(pst);
		return pst;
	}
	/**
	 * @param arg0
	 * @param arg1
	 * @param arg2
	 * @param arg3
	 * @return
	 * @throws java.sql.SQLException
	 */
	@Override
	public CallableStatement prepareCall(String arg0, int arg1, int arg2,
			int arg3) throws SQLException {
		CallableStatement st = con.prepareCall(arg0, arg1, arg2, arg3);
		PooledCallableStatement pst = new PooledCallableStatement(this,st);
		statementList.add(pst);
		return pst;
	}
	/**
	 * @param arg0
	 * @return
	 * @throws java.sql.SQLException
	 */
	@Override
	public PreparedStatement prepareStatement(String arg0) throws SQLException {
		PreparedStatement st = con.prepareStatement(arg0);
		PooledPreparedStatement pst = new PooledPreparedStatement(this,st);
		statementList.add(pst);
		return pst;
	}
	/**
	 * @param arg0
	 * @param arg1
	 * @return
	 * @throws java.sql.SQLException
	 */
	@Override
	public PreparedStatement prepareStatement(String arg0, int arg1)
			throws SQLException {
		PreparedStatement st = con.prepareStatement(arg0, arg1);
		PooledPreparedStatement pst = new PooledPreparedStatement(this,st);
		statementList.add(pst);
		return pst;

	}
	/**
	 * @param arg0
	 * @param arg1
	 * @param arg2
	 * @return
	 * @throws java.sql.SQLException
	 */
	@Override
	public PreparedStatement prepareStatement(String arg0, int arg1, int arg2)
			throws SQLException {
		PreparedStatement st = con.prepareStatement(arg0, arg1, arg2);
		PooledPreparedStatement pst = new PooledPreparedStatement(this,st);
		statementList.add(pst);
		return pst;

	}
	/**
	 * @param arg0
	 * @param arg1
	 * @param arg2
	 * @param arg3
	 * @return
	 * @throws java.sql.SQLException
	 */
	@Override
	public PreparedStatement prepareStatement(String arg0, int arg1, int arg2,
			int arg3) throws SQLException {
		PreparedStatement st = con.prepareStatement(arg0, arg1, arg2, arg3);
		PooledPreparedStatement pst = new PooledPreparedStatement(this,st);
		statementList.add(pst);
		return pst;

	}
	/**
	 * @param arg0
	 * @param arg1
	 * @return
	 * @throws java.sql.SQLException
	 */
	@Override
	public PreparedStatement prepareStatement(String arg0, int[] arg1)
			throws SQLException {
		PreparedStatement st = con.prepareStatement(arg0, arg1);
		PooledPreparedStatement pst = new PooledPreparedStatement(this,st);
		statementList.add(pst);
		return pst;

	}
	/**
	 * @param arg0
	 * @param arg1
	 * @return
	 * @throws java.sql.SQLException
	 */
	@Override
	public PreparedStatement prepareStatement(String arg0, String[] arg1)
			throws SQLException {
		PreparedStatement st = con.prepareStatement(arg0, arg1);
		PooledPreparedStatement pst = new PooledPreparedStatement(this,st);
		statementList.add(pst);
		return pst;

	}
	/**
	 * @param arg0
	 * @throws java.sql.SQLException
	 */
	@Override
	public void releaseSavepoint(Savepoint arg0) throws SQLException {
		con.releaseSavepoint(arg0);
	}
	/**
	 * @throws java.sql.SQLException
	 */
	@Override
	public void rollback() throws SQLException {
		con.rollback();
	}
	/**
	 * @param arg0
	 * @throws java.sql.SQLException
	 */
	@Override
	public void rollback(Savepoint arg0) throws SQLException {
		con.rollback(arg0);
	}
	/**
	 * @param arg0
	 * @throws java.sql.SQLException
	 */
	@Override
	public void setAutoCommit(boolean arg0) throws SQLException {
		con.setAutoCommit(arg0);
	}
	/**
	 * @param arg0
	 * @throws java.sql.SQLException
	 */
	@Override
	public void setCatalog(String arg0) throws SQLException {
		con.setCatalog(arg0);
	}
	/**
	 * @param arg0
	 * @throws java.sql.SQLException
	 */
	@Override
	public void setHoldability(int arg0) throws SQLException {
		con.setHoldability(arg0);
	}
	/**
	 * @param arg0
	 * @throws java.sql.SQLException
	 */
	@Override
	public void setReadOnly(boolean arg0) throws SQLException {
		con.setReadOnly(arg0);
	}
	/**
	 * @return
	 * @throws java.sql.SQLException
	 */
	@Override
	public Savepoint setSavepoint() throws SQLException {
		return con.setSavepoint();
	}
	/**
	 * @param arg0
	 * @return
	 * @throws java.sql.SQLException
	 */
	@Override
	public Savepoint setSavepoint(String arg0) throws SQLException {
		return con.setSavepoint(arg0);
	}
	/**
	 * @param arg0
	 * @throws java.sql.SQLException
	 */
	@Override
	public void setTransactionIsolation(int arg0) throws SQLException {
		con.setTransactionIsolation(arg0);
	}
	/**
	 * @param arg0
	 * @throws java.sql.SQLException
	 */
	@Override
	public void setTypeMap(Map<String, Class<?>> arg0) throws SQLException {
		con.setTypeMap(arg0);
	}
	/**
	 * @param typeName
	 * @param elements
	 * @return
	 * @throws SQLException
	 * @see java.sql.Connection#createArrayOf(java.lang.String, java.lang.Object[])
	 */
	@Override
	public Array createArrayOf(String typeName, Object[] elements)
			throws SQLException {
		return con.createArrayOf(typeName, elements);
	}

	/**
	 * @return
	 * @throws SQLException
	 * @see java.sql.Connection#createBlob()
	 */
	@Override
	public Blob createBlob() throws SQLException {
		return con.createBlob();
	}

	/**
	 * @return
	 * @throws SQLException
	 * @see java.sql.Connection#createClob()
	 */
	@Override
	public Clob createClob() throws SQLException {
		return con.createClob();
	}

	/**
	 * @return
	 * @throws SQLException
	 * @see java.sql.Connection#createNClob()
	 */
	@Override
	public NClob createNClob() throws SQLException {
		return con.createNClob();
	}

	/**
	 * @return
	 * @throws SQLException
	 * @see java.sql.Connection#createSQLXML()
	 */
	@Override
	public SQLXML createSQLXML() throws SQLException {
		return con.createSQLXML();
	}

	/**
	 * @param typeName
	 * @param attributes
	 * @return
	 * @throws SQLException
	 * @see java.sql.Connection#createStruct(java.lang.String, java.lang.Object[])
	 */
	@Override
	public Struct createStruct(String typeName, Object[] attributes)
			throws SQLException {
		return con.createStruct(typeName, attributes);
	}

	/**
	 * @return
	 * @throws SQLException
	 * @see java.sql.Connection#getClientInfo()
	 */
	@Override
	public Properties getClientInfo() throws SQLException {
		return con.getClientInfo();
	}

	/**
	 * @param name
	 * @return
	 * @throws SQLException
	 * @see java.sql.Connection#getClientInfo(java.lang.String)
	 */
	@Override
	public String getClientInfo(String name) throws SQLException {
		return con.getClientInfo(name);
	}

	/**
	 * @param timeout
	 * @return
	 * @throws SQLException
	 * @see java.sql.Connection#isValid(int)
	 */
	@Override
	public boolean isValid(int timeout) throws SQLException {
		return con.isValid(timeout);
	}

	/**
	 * @param iface
	 * @return
	 * @throws SQLException
	 * @see java.sql.Wrapper#isWrapperFor(java.lang.Class)
	 */
	@Override
	public boolean isWrapperFor(Class<?> iface) throws SQLException {
		return con.isWrapperFor(iface);
	}

	/**
	 * @param properties
	 * @throws SQLClientInfoException
	 * @see java.sql.Connection#setClientInfo(java.util.Properties)
	 */
	@Override
	public void setClientInfo(Properties properties)
			throws SQLClientInfoException {
		con.setClientInfo(properties);
	}

	/**
	 * @param name
	 * @param value
	 * @throws SQLClientInfoException
	 * @see java.sql.Connection#setClientInfo(java.lang.String, java.lang.String)
	 */
	@Override
	public void setClientInfo(String name, String value)
			throws SQLClientInfoException {
		con.setClientInfo(name, value);
	}

	/**
	 * @param <T>
	 * @param iface
	 * @return
	 * @throws SQLException
	 * @see java.sql.Wrapper#unwrap(java.lang.Class)
	 */
	@Override
	@SuppressWarnings("unchecked")
	public <T> T unwrap(Class<T> iface) throws SQLException {
		return (T)this;
	}
}
