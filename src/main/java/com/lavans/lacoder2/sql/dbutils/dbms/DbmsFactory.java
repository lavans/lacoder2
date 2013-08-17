package com.lavans.lacoder2.sql.dbutils.dbms;

import com.lavans.lacoder2.di.BeanManager;
import com.lavans.lacoder2.lang.StringUtils;
import com.lavans.lacoder2.sql.dbutils.enums.DbmsType;

public class DbmsFactory {
	public static DbmsUtils getDbmsUtils(DbmsType type){
		String className = StringUtils.capitalize(type.name().toLowerCase());
		return BeanManager.getBean("com.lavans.lacoder2.sql.dbutils.dbms."+className +"Utils");
	}
}
