package com.lavans.lacoder2.sql.dbutils.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

import com.lavans.lacoder2.sql.ConnectInfo;
import com.lavans.lacoder2.sql.dbutils.dbms.DbmsFactory;
import com.lavans.lacoder2.sql.dbutils.dbms.DbmsUtils;
import com.lavans.lacoder2.sql.dbutils.enums.DbmsType;
import com.lavans.lacoder2.util.Config;

/**
 * DB接続情報。sql.ConnectInfoの拡張。
 * DBMS種別を持ってDB毎に異なるDriverクラス名やurl接続文字列の処理を行う。
 *
 * @author sbisec
 */
@Data
@EqualsAndHashCode(callSuper=true)
@Slf4j
public class DbmsConnectInfo extends ConnectInfo{
	private static final String TYPE="type";
	private static final String IP="ip";
	private static final String NAME="name";
	private static final String USER="user";
	private static final String PASS="pass";
	private static final String STATISTICS="statistics";

	public static DbmsConnectInfo load(String configFile, String sectionName){
		if(!sectionName.endsWith("/")) { sectionName+="/"; }
		Config config = Config.getInstance(configFile);
		DbmsConnectInfo connectInfo = new DbmsConnectInfo();
		connectInfo.setDbmsType(DbmsType.valueOf(config.getNodeValue(sectionName+TYPE).toUpperCase()));
		connectInfo.setIp(config.getNodeValue(sectionName+IP));
		connectInfo.setDbName(config.getNodeValue(sectionName+NAME));
		connectInfo.setUser(config.getNodeValue(sectionName+USER));
		connectInfo.setPass(config.getNodeValue(sectionName+PASS));
		connectInfo.setStatistics(Boolean.valueOf(config.getNodeValue(sectionName+STATISTICS)));
		log.info(connectInfo.getName());
		return connectInfo;
	}
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
}
