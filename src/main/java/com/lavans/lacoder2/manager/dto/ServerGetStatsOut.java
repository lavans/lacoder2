package com.lavans.lacoder2.manager.dto;

import java.io.Serializable;
import java.util.Collection;

import com.lavans.lacoder2.stats.StatsRecord;


public class ServerGetStatsOut implements Serializable{
	private Collection<StatsRecord> records;

	public Collection<StatsRecord> getRecords() {
		return records;
	}

	public void setRecords(Collection<StatsRecord> records) {
		this.records = records;
	}
}
