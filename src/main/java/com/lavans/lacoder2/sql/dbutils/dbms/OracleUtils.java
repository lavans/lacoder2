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
public class OracleUtils implements DbmsUtils{
	private static Logger logger = LogUtils.getLogger();
	/**
	 * Retrn JDBC Drive class name.
	 */
	public String getDriverName(){
		return "oracle.jdbc.OracleDriver";
	}

	/**
	 * Return JDBC Connection String.
	 *
	 * @param ip
	 * @param port
	 * @param name
	 * @return
	 */
	public String getUrl(String ip, int port, String name){
		return "jdbc:oracle:thin:@$ip:$port:$name"
				.replace("$ip", ip)
				.replace("$port", String.valueOf(port))
				.replace("$name", name);
	}

	/**
	 * Return default database port.
	 */
	public int getDefaultPort(){
		return 1521;
	}

	@Override
	public String getValidSql(){
		return "SELECT sysdate FROM dual";
	}

	private CommonDao dao = BeanManager.getBean(CommonDao.class);

	/**
	 * バージョンを返します。
	 */
	private static final String SQL_VERSION = "SELECT * FROM V$VERSION";
	public String getVersion(String name){
		List<Map<String, Object>> list = dao.executeQuery(SQL_VERSION, null, name);
		return list.get(0).get("BANNER").toString();
	}

	/**
	 * テーブル名一覧を返します。
	 */
	private static final String SQL_TABLENAMES = "SELECT * FROM tab WHERE tabtype='TABLE' ORDER BY TNAME";
	public List<String> getTableNames(String name){
		List<String> tableNames = new ArrayList<>();
		List<Map<String, Object>> list = dao.executeQuery(SQL_TABLENAMES, null, name);
		for(Map<String, Object> map: list){
			tableNames.add(map.get("TNAME").toString());
		}
		return tableNames;
	}

	/**
	 * テーブル情報を返します。
	 */
	private static final String SQL_TABLE =
			"SELECT * FROM USER_TAB_COLUMNS WHERE TABLE_NAME=:table order by column_id";
	public Table getTable(String name, String tableName){
		Table table = new Table();
		table.setName(tableName);

		Map<String, Object> params = new HashMap<String, Object>();
		params.put("table", tableName);

		List<Map<String, Object>> list = dao.executeQuery(SQL_TABLE, params, name);
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
	 * DBのTYPEからJavaのTypeを返す
	 * @return
	 */
	private String getJavaType(String dbType){
		if(dbType.startsWith("NUMBER")){
			return "BigDecimal";
		}
		if(dbType.startsWith("DATE")){
			return "Date";
		}
		return "String";
	}

	/** LIMIT OFFSET用 start*/
	private static final String LIMIT_ORACLE_START=
			"SELECT * FROM (SELECT A.*, ROWNUM AS NUM FROM (\n  ";
	/** LIMIT OFFSET用 end */
	private static final String LIMIT_ORACLE_END=
			"\n) A ) WHERE NUM>:_start AND NUM<=:_end\n";

	/**
	 * limit offset sql 作成。
	 */
	@Override
	public String makeLimitOffset(String sql, int limit, int offset) {
		String start = String.valueOf(offset);
		String end = String.valueOf(offset+limit);
		sql = LIMIT_ORACLE_START
				+ sql
				+ LIMIT_ORACLE_END.replace(":_start", start).replace(":_end", end);
		return sql;
	}

}
