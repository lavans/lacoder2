package com.lavans.lacoder2.remote.servlet;

import net.arnx.jsonic.JSON;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import com.lavans.lacoder2.remote.servlet.mock.dto.MockExecIn;

public class RemoteInvokerTest {
	private Logger logger = LoggerFactory.getLogger(this.getClass());

	/**
	 * {
	 *   "service":"com.lavans.lacoder2.remote.servlet.mock.MockService",
	 *   "method":"method",
	 *   "in":{
	 *     "index":1
	 *   }
	 * }
	 * これを投げると
	 * {
	 *   "index":1,
	 *   "title":"title1",
	 *   "body":"body1"
	 * }
	 */
	@Test
	public void invokeMockSingle() {
		String uri = "/mock.MockService-exec";
		MockExecIn inObj = new MockExecIn();
		inObj.index=1;
		String in = JSON.encode(inObj, true);
		logger.info(in);

		//
		RemoteInvoker invoker = new RemoteInvoker();
//		String data = invoker.invoke(uri, in);
//		logger.info(data.toString());
//
//		assertTrue(data.contains("body1"));
//		assertTrue(data.contains("title1"));

	}

}
// {
//   "service":"com.lavans.service.NewsService",
//   "method":"list",
//   "in":{
//     "newsGroupId":"1"
//   }
// }
// というjsonを投げたら
// {
//   "newsGroupId":1,
//   "newsListOutDetailList":{
//     "newsId":1,
//     "title":"タイトル1",
//     "body":"本文1"
//   },{
//     "newsId":2,
//     "title":"タイトル2",
//     "body":"本文2"
//   }
//  という形式のjsonが返ってきて欲しい。
