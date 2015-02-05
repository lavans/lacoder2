package com.lavans.lacoder2.experimental.dbutils.dbms;

import java.util.List;

import org.slf4j.Logger;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.lavans.lacoder2.lang.LogUtils;
import com.lavans.lacoder2.sql.DBManager;
import com.lavans.lacoder2.sql.dao.CommonDao;
import com.lavans.lacoder2.sql.dbutils.model.ConnectInfo;
import com.lavans.lacoder2.sql.dbutils.model.Database;

public class OracleUtilsTest {
	private Logger logger = LogUtils.getLogger();
	private Database database;
	private ConnectInfo connectInfo;

	@BeforeTest
	public void setup() {
		connectInfo = new ConnectInfo("lacoder.xml","oracle");
		database = DBManager.getDatabase();
	}

	@Test
	public void getTableNames() {
		logger.info(database.getTableNames().toString());
	}

	@Test
	public void getTable() {
		logger.info(database.getTable("KABU_M").getColumnList().toString());
	}

	@Test
	public void testPK(){
//		String sql = "select table_name from USER_CONSTRAINTS ";
		String sql = "select table_name from USER_CONS_COLUMNS ";

//		String sql = "select * from USER_CONSTRAINTS where TABLE_NAME=$table";
		sql = sql.replaceAll("\\$table", "'i_fund_quote'");
		CommonDao dao = new CommonDao();
		List<?> list = dao.executeQuery(sql, null, connectInfo.getPoolName());
		logger.info(list.toString());
	}
}
