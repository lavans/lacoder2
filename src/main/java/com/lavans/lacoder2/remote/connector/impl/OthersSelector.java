package com.lavans.lacoder2.remote.connector.impl;

import java.util.ArrayList;
import java.util.List;

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
public class OthersSelector extends AllSelector implements Selector{
	/**
	 * Server一覧を返します。
	 * 現在有効になっているServer一覧から自ノードを除いたものを返します。
	 * 
	 * @param group 現在有効なServerNode一覧
	 */
	@Override
	protected List<ServerNode> getServerList(ServerGroup group){
		List<ServerNode> serverList = new ArrayList<>();
		for(ServerNode node: group.getOnlineList()){
			if(!node.isSelf()){
				serverList.add(node);
			}
		}
		return serverList;
	}
}
