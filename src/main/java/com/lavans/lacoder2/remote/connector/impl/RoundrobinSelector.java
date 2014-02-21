package com.lavans.lacoder2.remote.connector.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


import org.slf4j.Logger;

import com.lavans.lacoder2.di.annotation.Scope;
import com.lavans.lacoder2.di.annotation.Type;
import com.lavans.lacoder2.http.SimpleHttpClient;
import com.lavans.lacoder2.lang.LogUtils;
import com.lavans.lacoder2.remote.connector.Selector;
import com.lavans.lacoder2.remote.node.ServerGroup;
import com.lavans.lacoder2.remote.node.ServerNode;

@Scope(Type.PROTOTYPE)
public class RoundrobinSelector implements Selector{
	/** logger */
	private static Logger logger = LogUtils.getLogger();

	protected int current;
	/**
	 * 有効な接続先を探して、見つかればConnectした状態のhttpClientを返します。
	 * @throws RuntimeExcetion 接続先が一つも見つからない場合
	 */
	@Override
	public List<ServerConnectInfo> getClients(ServerGroup group, String url,
			Map<String, String> postData) {
		List<ServerNode> nodeList = group.getOnlineList();
		while(true){
			ServerNode node = next(nodeList, current);
			current = node.getIndex();
			try {
				SimpleHttpClient client = SimpleHttpClient.Builder
						.simpleHttpClient(node.getUri() + url)
						.withOutput(postData)
						.build();
				List<ServerConnectInfo> list = new ArrayList<>();
				list.add(new ServerConnectInfo(node, client));
				return list;
			}catch(IOException e){
				logger.warn(e.getMessage());
				// 接続に失敗したらofflineにする。
				node.setOffline();
				nodeList.remove(node);
				ServerGroup.save();
			}
		}
		// 有効な接続先がなければRuntimeException
	}

	/**
	 * 次のノードを探す。
	 * 次のノードがエラーになっていたらその次を返す。
	 * カレントノードが最後のノードだったら最初のノードをに戻る。
	 *
	 * @param list
	 * @return
	 * @throws RuntimeException 接続先が見つからない場合
	 */
	private ServerNode next(List<ServerNode> list, int current){
		logger.debug(list.toString());
		if(list.size()==0){
			throw new RuntimeException("No server found.");
		}

		// current+1とマッチするものを探す
		int next = current+1;
		for(ServerNode node: list){
			// nextがエラーの時にはその次でマッチするように>=にする
			if(node.getIndex()>=next && node.isOnline()) return node;
		}

		return list.get(0);
	}
}
