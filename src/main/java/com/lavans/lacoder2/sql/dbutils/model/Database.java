package com.lavans.lacoder2.sql.dbutils.model;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;


import org.slf4j.Logger;

import com.lavans.lacoder2.di.BeanManager;
import com.lavans.lacoder2.lang.LogUtils;
import com.lavans.lacoder2.sql.ConnectionPool;
import com.lavans.lacoder2.sql.DBManager;
import com.lavans.lacoder2.sql.dao.CommonDao;
import com.lavans.lacoder2.sql.dbutils.dbms.DbmsFactory;
import com.lavans.lacoder2.sql.dbutils.dbms.DbmsUtils;

public class Database {
	private static final Logger logger = LogUtils.getLogger();
	private DbmsUtils dbmsUtils;
	private ConnectionPool pool;
	private String name;
	
	public static Database connect(DbmsConnectInfo connectInfo){
		Database database = new Database();
		database.dbmsUtils = DbmsFactory.getDbmsUtils(connectInfo.getDbmsType());
		database.pool = new ConnectionPool(connectInfo);
		try {
			database.pool.init();
		} catch (ClassNotFoundException | IllegalAccessException
				| InstantiationException | SQLException e) {
			logger.error("DB初期化に失敗しました。", e);
			new RuntimeException(e);
		}
		database.name = connectInfo.getName();
		database.dbmsUtils.setDbName(database.name);
		DBManager.addConnectionPool(database.name, database.pool);
		return database;
	}
	/** SQL実行用dao */
	private CommonDao commonDao = BeanManager.getBean(CommonDao.class);

	/** 任意のSQL実行 */
	public List<Map<String, Object>> executeSql(String sql){
		return commonDao.executeQuery(sql, null, name);
	}
	
	public String getVersion(){
		return dbmsUtils.getVersion();
	}
	
	/**
	 * テーブル名一覧取得
	 * @return
	 */
	public List<String> getTableNames(){
		return dbmsUtils.getTableNames();
	}
	
	public Table getTable(String tableName){
		return dbmsUtils.getTable(tableName);
	}

}
