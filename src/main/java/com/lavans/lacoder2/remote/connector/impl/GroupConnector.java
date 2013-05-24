/* $Id: GroupConnector.java 509 2012-09-20 14:43:25Z dobashi $
 * created: 2005/11/04
 */
package com.lavans.lacoder2.remote.connector.impl;

import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lavans.lacoder2.di.BeanManager;
import com.lavans.lacoder2.di.annotation.Scope;
import com.lavans.lacoder2.di.annotation.Type;
import com.lavans.lacoder2.http.HttpResponse;
import com.lavans.lacoder2.remote.connector.Connector;
import com.lavans.lacoder2.remote.connector.Selector;
import com.lavans.lacoder2.remote.node.ServerGroup;
import com.lavans.lacoder2.remote.servlet.ObjectSerializer;
import com.lavans.lacoder2.remote.servlet.RemoteInvoker;


/**
 * すべてのApJに接続してコマンドを送信するクラス。
 *
 * @author dobashi
 */
@Scope(Type.PROTOTYPE)
public class GroupConnector implements Connector{
	/** logger */
	private static Logger logger = LoggerFactory.getLogger(GroupConnector.class.getName());
	/** エラー文字列 */
	private static final String ERROR="{'result':'error'}";
	/** リモートメソッド呼び出しツールクラス */
	private RemoteInvoker invoker = BeanManager.getBean(RemoteInvoker.class);
	/** 接続先一覧 */
	private ServerGroup group;
	/** 接続先選択クラス */
	private Selector selector;

	/**
	 * 全ApJサーバーと接続してメソッド呼び出しを行う。
	 *
	 * @return java.util.Map 接続先urlと結果Objectを格納。
	 * getCustomer
	 */
	@Override
	public Object execute(Method method, Object[] args){
		String url = invoker.toUrl(method);
		Map<String, String> postData = invoker.makePostData(args);
		// 結果を格納するリスト
		Map<String, Object> resultMap = new LinkedHashMap<>(group.getOnlineList().size());
		List<ServerConnectInfo> infoList;
		infoList = selector.getClients(group, url, postData);

		for(ServerConnectInfo info: infoList){
			String methodStr = info.serverNode.getUri() + method.getDeclaringClass().getSimpleName()+"#"+method.getName();
			try {
				HttpResponse response = info.client.request();
				Object result = ObjectSerializer.deserialize(response.getContents());
				// 結果を格納
				resultMap.put(info.serverNode.getName(), result);
				logger.info("remote execute["+ methodStr+"] return [" + result +"]");
			} catch (Exception e) {
				resultMap.put(info.serverNode.getName(), ERROR);
				logger.info( "remote execute failed.["+ methodStr +"]", e);
			}
		}
		return resultMap;
	}

	@Override
	public void setServerGroup(ServerGroup group) {
		this.group = group;
	}

	@Override
	public void setSelector(Selector selector) {
		this.selector = selector;
	}

	@Override
	public String toString(){
		logger.info("GroupConnector");
		return getClass().getSimpleName()
				+":"+ group.getName()
				+":"+ selector.getClass().getSimpleName();
	}
}
