package com.lavans.lacoder2.remote.connector.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;

import com.lavans.lacoder2.http.SimpleHttpClient;
import com.lavans.lacoder2.lang.LogUtils;
import com.lavans.lacoder2.remote.connector.Selector;
import com.lavans.lacoder2.remote.node.ServerGroup;
import com.lavans.lacoder2.remote.node.ServerNode;


/**
 * 自ノード以外への接続。
 * 管理Serverが別になるならいらないかも。
 * 
 * @author sbisec
 *
 */
public class AllSelector implements Selector{
	/** logger */
	private static Logger logger = LogUtils.getLogger();

	/**
	 * 有効な接続先を探して、見つかればConnectした状態のhttpClientを返します。
	 * @throws RuntimeExcetion 接続先が一つも見つからない場合
	 */
	@Override
	public List<ServerConnectInfo> getClients(ServerGroup group, String pathInfo,
			Map<String, String> postData) {
		List<ServerConnectInfo> list = new ArrayList<>();
		for(ServerNode node: getServerList(group)){
			try {
				SimpleHttpClient client = SimpleHttpClient.Builder
						.simpleHttpClient(node.getUri() + pathInfo)
						.withPostData(postData)
						.build();
				list.add(new ServerConnectInfo(node, client));
			}catch(IOException e){
				logger.warn(e.getMessage());
				// 接続に失敗したらofflineにする。
				node.setOffline();
				ServerGroup.save();
			}
		}
		return list;
	}
	/**
	 * Server一覧を返します。
	 * 
	 * @param group 現在有効なServerNode一覧
	 */
	protected List<ServerNode> getServerList(ServerGroup group){
		return group.getOnlineList();
	}
}
