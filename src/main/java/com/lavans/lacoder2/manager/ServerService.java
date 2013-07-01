package com.lavans.lacoder2.manager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.cglib.proxy.Enhancer;

import org.slf4j.Logger;

import com.lavans.lacoder2.di.BeanManager;
import com.lavans.lacoder2.lang.LogUtils;
import com.lavans.lacoder2.manager.dto.AccessGetStatusIn;
import com.lavans.lacoder2.manager.dto.AccessGetStatusOut;
import com.lavans.lacoder2.manager.dto.AccessSetStatusIn;
import com.lavans.lacoder2.manager.dto.AccessSetStatusOut;
import com.lavans.lacoder2.manager.dto.ServerExecIn;
import com.lavans.lacoder2.manager.dto.ServerExecOut;
import com.lavans.lacoder2.manager.dto.ServerGetStatsIn;
import com.lavans.lacoder2.manager.dto.ServerGetStatsOut;
import com.lavans.lacoder2.remote.node.ServerGroup;
import com.lavans.lacoder2.remote.node.ServerNode;
import com.lavans.lacoder2.stats.Statistics;

public class ServerService {
	private static final Logger logger = LogUtils.getLogger();

	/**
	 * Get Service from id
	 *
	 * @param id
	 * @return
	 */
	public static ServerService getService(String groupName, String nodeName) {
		// intercept by CGLIB
		Enhancer enhancer = new Enhancer();
		enhancer.setSuperclass(ServerService.class);
		enhancer.setCallback(new ManagerInterceptor(groupName, nodeName));
		Object service = enhancer.create();

		return (ServerService) service;
	}

	private Statistics stats = BeanManager.getBean(Statistics.class);
	public ServerGetStatsOut getStats(ServerGetStatsIn in){
		logger.debug("");
		ServerGetStatsOut out = new ServerGetStatsOut();
		out.setRecords(stats.getRecords());
		return out;
	}

	public ServerExecOut exec(ServerExecIn in){
		logger.debug("");

		CommandExecutor executor = BeanManager.getBean(CommandExecutor.class);
		ServerExecOut out = new ServerExecOut();
		executor.exec(in.getCommand());
		out.setStdout(executor.getStdout());
		out.setStderr(executor.getStderr());
		return out;
	}

	/**
	 * 接続先一覧を返します。
	 *
	 * @param in
	 * @return
	 */
	public AccessGetStatusOut getAccessMap(AccessGetStatusIn in) {
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
	public AccessSetStatusOut setAccessStatus(AccessSetStatusIn in) {
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
