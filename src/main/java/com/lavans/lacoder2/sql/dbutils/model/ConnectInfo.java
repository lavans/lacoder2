package com.lavans.lacoder2.sql.dbutils.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.val;
import lombok.extern.slf4j.Slf4j;

import com.lavans.lacoder2.lang.PeriodUtils;
import com.lavans.lacoder2.sql.dbutils.dbms.DbmsFactory;
import com.lavans.lacoder2.sql.dbutils.dbms.DbmsUtils;
import com.lavans.lacoder2.sql.dbutils.enums.DbmsType;
import com.lavans.lacoder2.util.Config;

/**
 * DB接続情報。sql.ConnectInfoの拡張。
 * DBMS種別を持ってDB毎に異なるDriverクラス名やurl接続文字列の処理を行う。
 *
 * @author
 */
@Slf4j
@Getter
@EqualsAndHashCode
public class ConnectInfo {
	private static final String TYPE="type";
	private static final String URL="url";
	private static final String HOST="host";
	private static final String PORT="port";
	private static final String NAME="name";
	private static final String USER="user";
	private static final String PASS="pass";
	private static final String IS_STATS="is-stats";
	private static final String IS_CHECK="is-check";
	private static final String MAX_SPARE="max-spare-connections";
	private static final String MIN_SPARE="min-spare-connections";
	private static final String MAX="max-connections";
	private static final String MAX_LIFE="max-life";

	public ConnectInfo(String configFile, String poolName){
		val sectionName = poolName.endsWith("/")?poolName:poolName+"/";
		Config config = Config.getInstance(configFile);
		type = DbmsType.valueOf(config.getNodeValue(sectionName+TYPE).toUpperCase());
		DbmsUtils utils = DbmsFactory.getDbmsUtils(type);
		String host = config.getNodeValue(sectionName + HOST);
		int port = config.getNodeValueInt(sectionName + PORT, utils.getDefaultPort());
		String name = config.getNodeValue(sectionName + NAME);
		String assignedUrl = config.getNodeValue(sectionName + URL);

		this.poolName = poolName;
		driverName = utils.getDriverName();
		url = assignedUrl.isEmpty()?utils.getUrl(host, port, name):assignedUrl;
		user = config.getNodeValue(sectionName + USER);
		pass = config.getNodeValue(sectionName + PASS);
		isStats = config.getNodeValueBoolean(sectionName + IS_STATS);
		isCheck = config.getNodeValueBoolean(sectionName + IS_CHECK);
		minSpare = config.getNodeValueInt(sectionName + MIN_SPARE, 2);
		int maxSpareTmp = config.getNodeValueInt(sectionName + MAX_SPARE, 10);
		if(maxSpareTmp<=minSpare){
			maxSpare = minSpare+5;
			log.info(MAX_SPARE + " must be greater than " + MIN_SPARE + ". Set "+ MAX_SPARE + " to " + maxSpare);
		}else{
			maxSpare = maxSpareTmp;
		}
		max = config.getNodeValueInt(sectionName + MAX, 50);
		maxLife = PeriodUtils.prettyParse(config.getNodeValue(sectionName + MAX_LIFE));
	}

	private final DbmsType type;
	private final String driverName;
	private final String url;
	/** データベース名 */
//	private final String name;
	private final String user;
	private final String pass;
	private final boolean isStats;
	private final boolean isCheck;
	private final int minSpare;
	private final int maxSpare;
	private final int max;
	private final long maxLife;
  /** コネクションプール名 */
	private final String poolName;
}
