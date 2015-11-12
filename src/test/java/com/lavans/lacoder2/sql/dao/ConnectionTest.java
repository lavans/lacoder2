package com.lavans.lacoder2.sql.dao;

import lombok.val;

public class ConnectionTest {

	@org.junit.Test
	public void testDB(){
		val dao = new CommonDao();
		dao.executeQuery("select sysdate from dual");

	}
}
