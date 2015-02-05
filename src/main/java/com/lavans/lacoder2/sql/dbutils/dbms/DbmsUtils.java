package com.lavans.lacoder2.sql.dbutils.dbms;

import java.util.List;

import com.lavans.lacoder2.di.annotation.Scope;
import com.lavans.lacoder2.di.annotation.Type;
import com.lavans.lacoder2.sql.dbutils.model.Table;


@Scope(Type.PROTOTYPE)
public interface DbmsUtils {
	String getDriverName();
	String getUrl(String ip, int port, String name);
	int getDefaultPort();
	String getValidSql();
	String makeLimitOffset(String sql, int limit, int offset);
	
	// SQL execute
	String getVersion(String dbName);
	List<String> getTableNames(String dbName);
	Table getTable(String dbName, String tableName);
}
