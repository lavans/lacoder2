package com.lavans.lacoder2.sql;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class ConnectInfo {
	private String driverName = null;
	private String url     = null;
	private String user   = null;
	private String pass   = null;
	/**
	 * SQL統計情報を収集するか。
	 * trueならcreateConnectionしたときにStatsConnectionでラップする。
	 */
	private boolean isStatistics = false;

	/**
	 * SQLログを出力するか。
	 */
	private boolean isLogging = true;
}
