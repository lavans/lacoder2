package com.lavans.lacoder2.sql.dbutils.model;

import java.util.List;
import java.util.Map;

import com.lavans.lacoder2.di.BeanManager;
import com.lavans.lacoder2.sql.dao.CommonDao;
import com.lavans.lacoder2.sql.dbutils.dbms.DbmsFactory;
import com.lavans.lacoder2.sql.dbutils.dbms.DbmsUtils;

public class Database {
	private final DbmsUtils dbmsUtils;
	private final String name;
	
	public Database(ConnectInfo connectInfo){
		name = connectInfo.getPoolName();
		dbmsUtils = DbmsFactory.getDbmsUtils(connectInfo.getType());
	}
	/** SQL実行用dao */
	private CommonDao commonDao = BeanManager.getBean(CommonDao.class);

	/** 任意のSQL実行 */
	public List<Map<String, Object>> executeSql(String sql){
		return commonDao.executeQuery(sql, null, name);
	}

	public String getValidSql(){
		return dbmsUtils.getVersion(name);
	}
	
	public String getVersion(){
		return dbmsUtils.getVersion(name);
	}
	
	/**
	 * テーブル名一覧取得
	 * @return
	 */
	public List<String> getTableNames(){
		return dbmsUtils.getTableNames(name);
	}
	
	public Table getTable(String tableName){
		return dbmsUtils.getTable(name, tableName);
	}
}
