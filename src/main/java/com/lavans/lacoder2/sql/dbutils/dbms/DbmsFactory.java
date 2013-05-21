package com.lavans.lacoder2.sql.dbutils.dbms;

import com.lavans.lacoder2.di.BeanManager;
import com.lavans.lacoder2.sql.dbutils.enums.DbmsType;

public class DbmsFactory {
	public static DbmsUtils getDbmsUtils(DbmsType type){
		// TODO  ひとまずOracle固定
		return BeanManager.getBean(OracleUtils.class);
	}
}
