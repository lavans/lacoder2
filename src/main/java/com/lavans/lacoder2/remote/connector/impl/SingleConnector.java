/* $Id: GroupConnector.java 509 2012-09-20 14:43:25Z dobashi $
 */
package com.lavans.lacoder2.remote.connector.impl;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lavans.lacoder2.di.BeanManager;
import com.lavans.lacoder2.di.annotation.Scope;
import com.lavans.lacoder2.di.annotation.Type;
import com.lavans.lacoder2.http.HttpResponse;
import com.lavans.lacoder2.lang.StringUtils;
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
public class SingleConnector implements Connector{
	/** logger */
	private static Logger logger = LoggerFactory.getLogger(SingleConnector.class.getName());
	/** リモートメソッド呼び出しツールクラス */
	private RemoteInvoker invoker = BeanManager.getBean(RemoteInvoker.class);
	/** 接続するServerグループ */
	private ServerGroup group;
	/** 接続先選択クラス */
	private Selector selector;

	/**
	 * サーバーと接続してメソッド呼び出しを1回行います。
	 * 接続先はSelectorから取得します。
	 *
	 * @return java.util.Map 接続先urlと結果Objectを格納。
	 * getCustomer
	 */
	@Override
	public Object execute(Method method, Object[] args){
		String url = invoker.toUrl(method);
		Map<String, String> postData = invoker.makePostData(args);
		List<ServerConnectInfo> infoList = selector.getClients(group, url, postData);
		ServerConnectInfo info = infoList.get(0);
		String methodStr = info.serverNode.getUri() + method.getDeclaringClass().getSimpleName()+"#"+method.getName();
		try{
			HttpResponse response = info.client.request();
			Object result = ObjectSerializer.deserialize(response.getContentsAsBinary());
			// ログへ出力
			logger.debug("remote execute["+ methodStr +"] return [" + chopResult(result) +"]");

			ResultChecker.checkThrowable(result);
			return result;
		}catch (IOException e) {
			logger.debug("remote execute["+ methodStr +"] error"+ e.getMessage());
			throw new RuntimeException(e);
		}
	}

	private String chopResult(Object src){
		if(src==null) return null;
		return StringUtils.substring(src.toString(), 0, 1000);
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
		return getClass().getSimpleName()
				+":"+ group.getName()
				+":"+ selector.getClass().getSimpleName();
	}
}
