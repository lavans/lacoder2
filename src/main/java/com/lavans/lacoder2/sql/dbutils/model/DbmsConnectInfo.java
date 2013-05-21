package com.lavans.lacoder2.sql.dbutils.model;

import com.lavans.lacoder2.sql.ConnectInfo;
import com.lavans.lacoder2.sql.dbutils.dbms.DbmsFactory;
import com.lavans.lacoder2.sql.dbutils.dbms.DbmsUtils;
import com.lavans.lacoder2.sql.dbutils.enums.DbmsType;

/**
 * DB接続情報。sql.ConnectInfoの拡張。
 * DBMS種別を持ってDB毎に異なるDriverクラス名やurl接続文字列の処理を行う。
 * 
 * @author sbisec
 */
public class DbmsConnectInfo extends ConnectInfo{
	private DbmsType dbmsType;
	private String ip;
	private int port;
	/** データベース名 */
	private String dbName;
	
	/** 
	 * この設定情報につける名前。当面は接続URLにする。
	 * 保存できるようになったら任意の名前を付けられるように拡張する。
	 */
	public String getName(){
		return getUrl();
	}

	@Override
	public String getUrl(){
		return DbmsFactory.getDbmsUtils(dbmsType).getUrl(ip, port, dbName);
	}

	public DbmsType getDbmsType() {
		return dbmsType;
	}

	public void setDbmsType(DbmsType dbmsType) {
		this.dbmsType = dbmsType;
		DbmsUtils dbmsUtils = DbmsFactory.getDbmsUtils(dbmsType);
		port = dbmsUtils.getDefaultPort();
		super.setDriverName(dbmsUtils.getDriverName());
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}
	
	public String getDBName() {
		return dbName;
	}

	public void setDBName(String dbName) {
		this.dbName = dbName;
	}

}
