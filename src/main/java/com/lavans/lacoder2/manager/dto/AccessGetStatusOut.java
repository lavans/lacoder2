package com.lavans.lacoder2.manager.dto;

import java.util.List;
import java.util.Map;

import com.lavans.lacoder2.remote.node.ServerNode;

public class AccessGetStatusOut {
	private Map<String, List<ServerNode>> accessMap;

	public Map<String, List<ServerNode>> getAccessMap() {
		return accessMap;
	}

	public void setAccessMap(Map<String, List<ServerNode>> accessMap) {
		this.accessMap = accessMap;
	}
}
