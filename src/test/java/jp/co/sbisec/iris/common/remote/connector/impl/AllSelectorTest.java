package jp.co.sbisec.iris.common.remote.connector.impl;

import java.util.List;

import mockit.Mocked;

import static org.testng.Assert.*;
import org.testng.annotations.Test;

import com.lavans.lacoder2.di.BeanManager;
import com.lavans.lacoder2.http.SimpleHttpClient;
import com.lavans.lacoder2.lang.FileUtils;
import com.lavans.lacoder2.remote.connector.impl.AllSelector;
import com.lavans.lacoder2.remote.connector.impl.ServerConnectInfo;
import com.lavans.lacoder2.remote.node.ServerGroup;

public class AllSelectorTest {
	/** テスト対象 */
	AllSelector selector = new AllSelector();

	@Mocked
	private SimpleHttpClient client;

	@Test
	public void getClients() {
		// setup
		// 設定ファイルの読み込み
		String filename = FileUtils.makeResourceFileName(this.getClass());
		ServerGroup.load(filename);
		// SimpleHttpClientをモックに差し替え
		BeanManager.load(filename);

		//
		ServerGroup group = ServerGroup.getInstance("all-group");
		List<ServerConnectInfo> list = selector.getClients(group, "url", /* postData */null);

		// check
		assertEquals(list.size(), group.getNodeList().size());
	}

	@Test
	public void getServerList() {
	}
}
