package com.lavans.lacoder2.sql.dbutils.model;

public class Column {
	private String name;
	private String dbType;
	private boolean isNullable;
	private int length;

	private String javaType;
	private String title;
	private String comment;
	
	@Override
	public String toString(){
		return "Column:"+name;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getDbType() {
		return dbType;
	}
	public void setDbType(String dbType) {
		this.dbType = dbType;
	}
	public boolean isNullable() {
		return isNullable;
	}
	public void setNullable(boolean isNullable) {
		this.isNullable = isNullable;
	}
	public int getLength() {
		return length;
	}
	public void setLength(int length) {
		this.length = length;
	}
	public String getJavaType() {
		return javaType;
	}
	public void setJavaType(String javaType) {
		this.javaType = javaType;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getComment() {
		return comment;
	}
	public void setComment(String comment) {
		this.comment = comment;
	}
	
}
