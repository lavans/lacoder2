package jp.co.sbisec.iris.common.remote.connector;

import java.util.HashMap;
import java.util.Map;


import net.arnx.jsonic.JSON;

import org.slf4j.Logger;
import org.testng.annotations.Test;

import com.lavans.lacoder2.lang.LogUtils;

public class GroupConnectorTest {
	private static Logger logger = LogUtils.getLogger();
	@SuppressWarnings("unchecked")
	@Test
	public void 結果をjsonでencodeしたらどうなるかテスト() {
		String str = "{'result':'pk'}";
		String node = "server1";
		Map<String,String> map = new HashMap<String, String>();
		map.put(node, str);
		String json = JSON.encode(map);
		logger.info(json);
		
		Map<String,Map<String,String>> obj = new HashMap<>();
		obj = JSON.decode(json,obj.getClass());
		logger.info(obj.toString());

		Map<String,String> obj2 = new HashMap<>();
		obj2 = JSON.decode(json,obj2.getClass());
		logger.info(obj2.toString());
		
		Map<String,String> obj3 = new HashMap<>();
		obj3 = JSON.decode(obj2.get(node), obj3.getClass());
		logger.info(obj3.toString());
	}
}
