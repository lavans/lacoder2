package com.lavans.lacoder2.remote.connector.impl;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Map;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lavans.lacoder2.di.BeanManager;
import com.lavans.lacoder2.http.HttpResponse;
import com.lavans.lacoder2.http.SimpleHttpClient;
import com.lavans.lacoder2.remote.connector.Connector;
import com.lavans.lacoder2.remote.connector.Selector;
import com.lavans.lacoder2.remote.node.ServerGroup;
import com.lavans.lacoder2.remote.node.ServerNode;
import com.lavans.lacoder2.remote.servlet.ObjectSerializer;
import com.lavans.lacoder2.remote.servlet.RemoteInvoker;

public class FixedConnector implements Connector{
	/** logger */
	private static Logger logger = LoggerFactory.getLogger(FixedConnector.class.getName());
	/** リモートメソッド呼び出しツールクラス */
	private RemoteInvoker invoker = BeanManager.getBean(RemoteInvoker.class);

	/** 接続するServerNode */
	private ServerNode serverNode;

	/**
	 * 接続処理。
	 * リモートサーバーにHTTP接続してメソッド実行します。
	 *
	 * GET用のテストコード
	 * String query = ParameterUtils.toStoreString(ParameterUtils.convertToStringArrayMap(postData),"UTF-8");
	 * .withQuery(query)
	 *
	 */
	@Override
	public Object execute(Method method, Object[] args) {
		String url = invoker.toUrl(method);
		Map<String, String> postData = invoker.makePostData(args);

		try{
			SimpleHttpClient client = SimpleHttpClient.Builder
					.simpleHttpClient(serverNode.getUri() + url)
					.withOutput(postData)
					.build();
			HttpResponse response = client.request();
			Object result = ObjectSerializer.deserialize(response.getContentsAsBinary());
			// ログへ出力
			logger.info("remote execute["+ serverNode.getUri() +"] return [" + result +"]");
			ResultChecker.checkThrowable(result);

			return result;
//			String result = response.getContents();
//			String log = result.length()>100?result.substring(0,100)+"...":result;
		}catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public ServerNode getServerNode() {
		return serverNode;
	}

	public void setServerNode(ServerNode node) {
		this.serverNode = node;
	}

	@Override
	public void setSelector(Selector selector) {
	}

	@Override
	public void setServerGroup(ServerGroup group) {
	}

}
