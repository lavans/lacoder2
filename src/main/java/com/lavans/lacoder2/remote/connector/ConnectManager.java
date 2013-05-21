/* $Id: ConnectManager.java 509 2012-09-20 14:43:25Z dobashi $
 * created: 2005/08/05
 */
package com.lavans.lacoder2.remote.connector;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


import org.slf4j.Logger;

import com.lavans.lacoder2.di.BeanManager;
import com.lavans.lacoder2.lang.LogUtils;
import com.lavans.lacoder2.lang.StringUtils;
import com.lavans.lacoder2.remote.connector.impl.AllSelector;
import com.lavans.lacoder2.remote.connector.impl.AsyncConnectWrapper;
import com.lavans.lacoder2.remote.connector.impl.FixedConnector;
import com.lavans.lacoder2.remote.connector.impl.GroupConnector;
import com.lavans.lacoder2.remote.connector.impl.OrderedSelector;
import com.lavans.lacoder2.remote.connector.impl.OthersSelector;
import com.lavans.lacoder2.remote.connector.impl.RoundrobinSelector;
import com.lavans.lacoder2.remote.connector.impl.SingleConnector;
import com.lavans.lacoder2.remote.node.ServerGroup;
import com.lavans.lacoder2.remote.node.ServerNode;

/**
 * コネクション管理クラス。
 *
 * @author dobashi
 */
public class ConnectManager {
	/** ロガー */
	private static Logger logger = LogUtils.getLogger();

	/**
	 * ApJへの接続を取得。
	 * @return
	 * @throws NullPointerException groupがnullの時
	 */
	public Connector getConnector(ServerGroup group){
		if(group==null){
			throw new NullPointerException("ServerGroup is null");
		}
		// get selector
		Connector connector = map.get(group);
		if(connector == null){
			connector = createConnector(group);
			map.put(group, connector);
		}

		// check sync
		if(!group.isSync()){
			connector = new AsyncConnectWrapper(connector);
		}
		return connector;
	}

	/**
	 * 
	 * @param group
	 * @return
	 */
	private Connector createConnector(ServerGroup group){
		Type type=null;
		if(StringUtils.isEmpty(group.getSelector())){
			type = Type.ORDERED;
		}else{
			type = Type.valueOf(group.getSelector().toUpperCase());
		}
		Selector selector = BeanManager.getBean(type.selecotrClass);
		Connector connector = BeanManager.getBean(type.connectorClass);
		connector.setServerGroup(group);
		connector.setSelector(selector);
		
		logger.info("create "+group.getName() 
				+":"+ connector.getClass().getSimpleName()
				+":"+ selector.getClass().getSimpleName() );
		return connector;
	}
	
	/**
	 * ノード指定でFixedConnectorを返します。
	 * 
	 * @param node
	 * @return
	 */
	public FixedConnector getConnector(ServerNode node){
		if(node==null){
			throw new NullPointerException("ServerNode is null");
		}
		// TODO キャッシュ
		FixedConnector connector = new FixedConnector();
		connector.setServerNode(node);
		return connector;
	}


	/**
	 * Type of connection.
	 * single: Normal connection selected in group by Selector.
	 * group:	Connect to all of one group.
	 * others:	Like group, but skip self.
	 *
	 * @author dobashi
	 *
	 */
	private enum Type {
		LOCAL(null, null), 
		ORDERED(SingleConnector.class, OrderedSelector.class), 
		ROUNDROBIN(SingleConnector.class, RoundrobinSelector.class), // TODO
		ALL(GroupConnector.class, AllSelector.class),
		OTHERS(GroupConnector.class, OthersSelector.class);
		
		private Class<? extends Connector> connectorClass;
		private Class<? extends Selector> selecotrClass;
		private Type(Class<? extends Connector> connector, Class<? extends Selector> selector){
			this.connectorClass = connector;
			this.selecotrClass = selector;
		}
	};

	private Map<ServerGroup, Connector> map = new ConcurrentHashMap<>();
}
