package com.lavans.lacoder2.remote.connector;
//import static org.testng.Assert.assertEquals;
//import static org.testng.Assert.assertFalse;
//import static org.testng.Assert.assertTrue;
//
//import java.lang.reflect.Method;
//
//
//import org.slf4j.Logger;
//import org.testng.annotations.BeforeMethod;
//import org.testng.annotations.Test;
//
//import com.lavans.lacoder2.di.BeanManager;
//import com.lavans.lacoder2.lang.FileUtils;
//import com.lavans.lacoder2.lang.LogUtils;
//import com.lavans.lacoder2.remote.connector.ConnectManager;
//import com.lavans.lacoder2.remote.connector.Connector;
//import com.lavans.lacoder2.remote.node.ServerGroup;
//
//public class ConnectManagerTest {
//	private static Logger logger = LogUtils.getLogger();
//	private ConnectManager connectManager;
//
//	@BeforeMethod
//	public void setup(){
//		String file = FileUtils.makeResourceFileName(this.getClass());
//		BeanManager.load(file);
//		ServerGroup.clear();
//		ServerGroup.load(file);
//		connectManager = new ConnectManager();
//	}
//
//	@Test
//	public void orderedは何回呼んでも1stを返す() {
//		logger.info("----------------------------------------------------");
//		ServerGroup group = ServerGroup.getInstance("ordered-group");
//		Object out1 = request(group);
//		Object out2 = request(group);
//		assertEquals(out1,  out2);
//	}
//
//	@Test
//	public void roundrobinは呼ぶ度に異なる() {
//		logger.info("*----------------------------------------------------");
//		ServerGroup group = ServerGroup.getInstance("roundrobin-group");
//		Object out1 = request(group);
//		Object out2 = request(group);
//		Object out3 = request(group);
//		Object out4 = request(group);
//		logger.info("- - - - - - - - - - - - - -");
//		logger.info(out1.toString());
//		logger.info(out2.toString());
//		logger.info(out3.toString());
//		logger.info(out4.toString());
//		Object out5 = request(group);
//		logger.info("- - - - - - - - - - - - - -");
//		logger.info(out5.toString());
//		assertFalse(out1.equals(out2));
//		assertFalse(out1.equals(out3));
//		assertFalse(out1.equals(out4));
//		assertTrue(out1.equals(out5));
//
//	}
//
//	// 127.0.0.1はエラー
//	// @Test
//	public void orderedは何回呼んでも2ndを返す() {
//		logger.info("----------------------------------------------------");
//		String file = FileUtils.makeResourceFileName(this.getClass(),"2nd");
//		BeanManager.load(file);
//		ServerGroup group = ServerGroup.getInstance("ordered-group");
//		Object out1 = request(group);
//		Object out2 = request(group);
//		assertEquals(out1,  out2);
//	}
//
//	//@Test
//	public void roundrobinは1stを除いて呼ぶ度に異なる() {
//		logger.info("----------------------------------------------------");
//		String file = FileUtils.makeResourceFileName(this.getClass(),"2nd");
//		BeanManager.load(file);
//		ServerGroup group = ServerGroup.getInstance("roundrobin-group");
//		Object out1 = request(group);
//		Object out2 = request(group);
//		Object out3 = request(group);
//		Object out4 = request(group);
//
//		logger.info("- - - - - - - - - - - - - -");
//		logger.info(out1.toString());
//		logger.info(out2.toString());
//		logger.info(out3.toString());
//		logger.info("- - - - - - - - - - - - - -");
//		logger.info(out4.toString());
//
//
//		assertFalse(out1.equals(out2));
//		assertFalse(out1.equals(out3));
//		assertTrue(out1.equals(out4));
//	}
//
//	@Test
//	public void allで全部接続() {
//		logger.info("----------------------------------------------------");
//		ServerGroup group = ServerGroup.getInstance("all-group");
//		Object out1 = request(group);
//
//		logger.info("- - - - - - - - - - - - - -");
//		logger.info(out1.toString());
//
//		assertTrue(out1.contains("server1"));
//		assertTrue(out1.contains("server2"));
//		assertTrue(out1.contains("server3"));
//		assertTrue(out1.contains("server4"));
//	}
//
//	@Test
//	public void othersは自分以外(){
//		// 設定ファイルには<self-node>server3</self-node>と設定済み。
//		ServerGroup group = ServerGroup.getInstance("others-group");
//		String out1 = request(group);
//
//		logger.info("- - - - - - - - - - - - - -");
//		logger.info(out1.toString());
//
//		assertTrue(out1.contains("server1"));
//		assertTrue(out1.contains("server2"));
//		assertFalse(out1.contains("server3"));
//		assertTrue(out1.contains("server4"));
//
//	}
//
//	private Object request(ServerGroup group){
//		Connector con = connectManager.getConnector(group);
//		logger.info(con.toString());
//
//		Method method = this.getClass().getMethods()[0];
//		Object out = con.execute(method, new Object[]{group});
//		logger.info(out.toString());
//		return out;
//	}
//}
