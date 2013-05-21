package com.lavans.lacoder2.remote.connector.impl;

import java.util.List;
import java.util.Map;

import com.lavans.lacoder2.di.annotation.Scope;
import com.lavans.lacoder2.di.annotation.Type;
import com.lavans.lacoder2.remote.connector.Selector;
import com.lavans.lacoder2.remote.node.ServerGroup;


@Scope(Type.PROTOTYPE)
public class OrderedSelector extends RoundrobinSelector implements Selector{
	/**
	 * 有効な接続先を探して、見つかればConnectした状態のhttpClientを返します。
	 * Orderedは毎回カレントを0に初期化してからRoundrobinするのと同等。
	 * 
	 * @throws RuntimeExcetion 接続先が一つも見つからない場合
	 */
	@Override
	public List<ServerConnectInfo> getClients(ServerGroup group, String url,
			Map<String, String> postData) {
		current=-1;
		return super.getClients(group, url, postData);
	}
}
