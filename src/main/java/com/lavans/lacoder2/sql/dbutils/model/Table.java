package com.lavans.lacoder2.sql.dbutils.model;

import java.util.ArrayList;
import java.util.List;

public class Table {
	private String name;
	private List<Column> columnList = new ArrayList<>();
	private long count;
	private long size;
	/** schema sql */
	private String schema;

	
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public void addColumn(Column column){
		columnList.add(column);
	}
	
	public List<Column> getColumnList() {
		return columnList;
	}
	public void setColumnList(List<Column> columnList) {
		this.columnList = columnList;
	}
	public long getCount() {
		return count;
	}
	public void setCount(long count) {
		this.count = count;
	}
	public long getSize() {
		return size;
	}
	public void setSize(long size) {
		this.size = size;
	}
	public String getSchema() {
		return schema;
	}
	public void setSchema(String schema) {
		this.schema = schema;
	}
}
