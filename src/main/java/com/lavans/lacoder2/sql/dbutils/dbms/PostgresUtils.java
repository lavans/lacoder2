package com.lavans.lacoder2.sql.dbutils.dbms;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import org.slf4j.Logger;

import com.lavans.lacoder2.di.BeanManager;
import com.lavans.lacoder2.di.annotation.Scope;
import com.lavans.lacoder2.di.annotation.Type;
import com.lavans.lacoder2.lang.LogUtils;
import com.lavans.lacoder2.sql.dao.CommonDao;
import com.lavans.lacoder2.sql.dbutils.model.Column;
import com.lavans.lacoder2.sql.dbutils.model.Table;

@Scope(Type.PROTOTYPE)
public class PostgresUtils implements DbmsUtils{
	private static Logger logger = LogUtils.getLogger();
	private String dbName;
	public void setDbName(String dbName){
		this.dbName = dbName;
	}
	/**
	 * Retrn JDBC Drive class name.
	 */
	public String getDriverName(){
		return "org.postgresql.Driver";
	}
	
	/**
	 * Return JDBC Connection String.
	 * 
	 * @param ip
	 * @param port
	 * @param name
	 * @return
	 */
	public String getUrl(String host, int port, String name){
		return "jdbc:postgres:@$host:$port:$name"
				.replace("$host", host)
				.replace("$port", String.valueOf(port))
				.replace("$name", name);
	}
	
	/**
	 * Return default database port.
	 */
	public int getDefaultPort(){
		return 5432;
	}
	
	private CommonDao dao = BeanManager.getBean(CommonDao.class);

	/**
	 * バージョンを返します。
	 */
	private static final String SQL_VERSION = "SELECT * FROM V$VERSION";
	public String getVersion(){
		List<Map<String, Object>> list = dao.executeQuery(SQL_VERSION, null, dbName);
		return list.get(0).get("BANNER").toString();
	}

	/**
	 * テーブル名一覧を返します。
	 */
	private static final String SQL_TABLENAMES = "SELECT * FROM tab WHERE tabtype='TABLE' ORDER BY TNAME";
	public List<String> getTableNames(){
		List<String> tableNames = new ArrayList<>();
		List<Map<String, Object>> list = dao.executeQuery(SQL_TABLENAMES, null, dbName);
		for(Map<String, Object> map: list){
			tableNames.add(map.get("TNAME").toString());
		}
		return tableNames;
	}
	
	/**
	 * テーブル情報を返します。
	 */
	private static final String SQL_TABLE =
			"";
	public Table getTable(String tableName){
		Table table = new Table();
		table.setName(tableName);
		
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("table", tableName);
		
		List<Map<String, Object>> list = dao.executeQuery(SQL_TABLE, params, dbName);
		for(Map<String, Object> map: list){
			table.addColumn(makeTarget(map));
		}
		return table;
	}
	
	private Column makeTarget(Map<String, Object> map){
		logger.info(map.toString());
		Column column = new Column();
		column.setName(map.get("COLUMN_NAME").toString());
		column.setLength(((BigDecimal)map.get("DATA_LENGTH")).intValue());
		column.setDbType(map.get("DATA_TYPE").toString());
		if(column.getDbType().equals("CHAR") || column.getDbType().equals("VARCHAR2")){
			column.setDbType(column.getDbType()+"("+column.getLength()+")");
		}
		column.setJavaType(getJavaType(column.getDbType()));
		column.setNullable(map.get("NULLABLE").equals("Y")?true:false);
		return column;
	}
	
	/**
	 * TODO
	 * DBのTYPEからJavaのTypeを返す
	 * @return
	 */
	private String getJavaType(String dbType){
		if(dbType.startsWith("NUMBER")){
			return "BigDecimal";
		}
		if(dbType.startsWith("TIMESTAMP")){
			return "Date";
		}
		return "String";
	}

	/**
	 * limit offset sql 作成。
	 */
	@Override
	public String makeLimitOffset(String sql, int limit, int offset) {
		if(limit>0){
			sql += " LIMIT "+ limit;
		}
		if(offset>0){
			sql += " OFFSET "+ offset;
		}
		return sql;
	}
}
