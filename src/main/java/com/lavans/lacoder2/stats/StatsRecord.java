/* $Id: StatsRecord.java 509 2012-09-20 14:43:25Z dobashi $
 * create: 2004/07/27
 * (c)2004 Lavans Networks Inc. All Rights Reserved.
 */
package com.lavans.lacoder2.stats;

import java.util.Set;
import java.util.TreeSet;

/**
 * @author dobashi
 * @version 1.00
 */
public class StatsRecord {
	private String key=null;
	private int callCount = 0;
	private long totalCostTime = 0;
	private long maxCostTime = 0;
	private Set<String> methodNames = new TreeSet<String>();

	/**
	 * @param l
	 */
	public void addData(long l) {
		callCount++;
		totalCostTime += l;
		maxCostTime = Math.max(maxCostTime,  l);
	}

	public double getAverage(){
		return totalCostTime / (double)callCount;
	}
	/**
	 * @return
	 */
	public int getCallCount() {
		return callCount;
	}

	/**
	 * @param i
	 */
	public void setCallCount(int i) {
		callCount = i;
	}

	/**
	 * @return
	 */
	public Set<String> getMethodNames() {
		return methodNames;
	}

	/**
	 * @param set
	 */
	public void setMethodNames(Set<String> set) {
		methodNames = set;
	}

	/**
	 * @param set
	 */
	public void addMethodNames(String s) {
		methodNames.add(s);
	}

	/**
	 * @return
	 */
	public String getKey() {
		return key;
	}

	/**
	 * @param string
	 */
	public void setKey(String string) {
		key = string;
	}
	/**
	 * @return
	 */
	public long getTotalCostTime() {
		return totalCostTime;
	}

	/**
	 * @return maxCostTime
	 */
	public long getMaxCostTime() {
		return maxCostTime;
	}
	
	
	// for BeanUtils/JSON
	public void setTotalCostTime(long totalCostTime) {
		this.totalCostTime = totalCostTime;
	}

	public void setMaxCostTime(long maxCostTime) {
		this.maxCostTime = maxCostTime;
	}
	
	@Override
	public String toString(){
		return key+"["+callCount+"]";
	}
}
