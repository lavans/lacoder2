package com.lavans.lacoder2.sql;

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

	public boolean isStatistics() {
		return isStatistics;
	}
	public void setStatistics(boolean isStatistics) {
		this.isStatistics = isStatistics;
	}
	public boolean isLogging() {
		return isLogging;
	}
	public void setLogging(boolean isLogging) {
		this.isLogging = isLogging;
	}
	public String getDriverName() {
		return driverName;
	}
	public void setDriverName(String driverName) {
		this.driverName = driverName;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getUser() {
		return user;
	}
	public void setUser(String user) {
		this.user = user;
	}
	public String getPass() {
		return pass;
	}
	public void setPass(String pass) {
		this.pass = pass;
	}

}
