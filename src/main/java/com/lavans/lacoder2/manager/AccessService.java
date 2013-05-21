package com.lavans.lacoder2.manager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.cglib.proxy.Enhancer;

import org.slf4j.Logger;

import com.lavans.lacoder2.lang.LogUtils;
import com.lavans.lacoder2.manager.dto.AccessGetStatusIn;
import com.lavans.lacoder2.manager.dto.AccessGetStatusOut;
import com.lavans.lacoder2.manager.dto.AccessSetStatusIn;
import com.lavans.lacoder2.manager.dto.AccessSetStatusOut;
import com.lavans.lacoder2.remote.node.ServerGroup;
import com.lavans.lacoder2.remote.node.ServerNode;

public class AccessService {
	private static final Logger logger = LogUtils.getLogger();

	/**
	 * Get Service from id
	 * 
	 * @param id
	 * @return
	 */
	public static AccessService getService(String groupName, String nodeName) {
		// intercept by CGLIB
		Enhancer enhancer = new Enhancer();
		enhancer.setSuperclass(AccessService.class);
		enhancer.setCallback(new ManagerInterceptor(groupName, nodeName));
		Object service = enhancer.create();

		return (AccessService) service;
	}

	/**
	 * 接続先一覧を返します。
	 * 
	 * @param in
	 * @return
	 */
	public AccessGetStatusOut getStatus(AccessGetStatusIn in) {
		Map<String, List<ServerNode>> accessMap = new HashMap<>();
		for (ServerGroup serverGroup : ServerGroup.getAll()) {
			accessMap.put(serverGroup.getName(), serverGroup.getNodeList());
		}
		AccessGetStatusOut out = new AccessGetStatusOut();
		out.setAccessMap(accessMap);
		return out;
	}

	/**
	 * 接続ON/OFFを設定します。
	 * 
	 * @param in
	 * @return 設定結果
	 */
	public AccessSetStatusOut setStatus(AccessSetStatusIn in) {
		logger.debug(in.getGroupName() + ":" + in.getNodeName() + ":" + in.isOnline());

		ServerGroup serverGroup = ServerGroup.getInstance(in.getGroupName());
		ServerNode node = serverGroup.find(in.getNodeName());
		AccessSetStatusOut out = new AccessSetStatusOut();
		if (node != null) {
			node.setOnline(in.isOnline());
			out.getMessages().put("result", "ok");
			ServerGroup.save();
		} else {
			out.getMessages().put("result", "ng");
		}
		return out;
	}

}
