package com.lavans.lacoder2.remote.connector;

import java.util.List;
import java.util.Map;

import com.lavans.lacoder2.remote.connector.impl.ServerConnectInfo;
import com.lavans.lacoder2.remote.node.ServerGroup;




/**
 * Stateful selector class.
 * RoundRobinSelector have to remind last node.
 * Selector classes has state. Selector is instance for each RemoteNodeGroup.
 * OrderedSelector have to check if node is alive.
 * Selector must know connector.
 *
 *
 * @author dobashi
 *
 */
public interface Selector {
	/**
	 * ServerGroup内から有効な接続先を返します。
	 * 実際にconnectして有効な接続先かどうかをチェックするので
	 * urlとpostDataをつかってSimpleHttpClientを作成します。
	 * このメソッドを呼んだ時点ではデータはpostされません。
	 * 
	 * 接続先が一つも見つからない場合、Single接続ではRuntimeExceptionを返します。
	 * Group接続では空のListを返します。
	 * 
	 * @param group 接続先検索用ServerGroup
	 * @param url 接続先url
	 * @param postData POSTデータ。SimpleHttpClientにセットされるだけで、このメソッドの戻り時点では送信されていません。
	 * @return
	 * @throws RuntimeException Single接続なのに接続先が一つも見つからない場合。
	 */
	List<ServerConnectInfo> getClients(ServerGroup group, String pathInfo, Map<String,String> postData);
}
