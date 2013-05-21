package com.lavans.lacoder2.remote.connector.impl;

import com.lavans.lacoder2.http.SimpleHttpClient;
import com.lavans.lacoder2.remote.node.ServerNode;


/**
 * ServerNodtとSimpleHttpClientの入れ物。
 * @author sbisec
 *
 */
public class ServerConnectInfo {
	ServerNode serverNode;
	SimpleHttpClient client;
	ServerConnectInfo(ServerNode serverNode, SimpleHttpClient client){
		this.serverNode = serverNode;
		this.client = client;
	}
	
	public ServerNode getServerNode(){
		return serverNode;
	}

	public SimpleHttpClient getHttpClient(){
		return client;
	}
}
