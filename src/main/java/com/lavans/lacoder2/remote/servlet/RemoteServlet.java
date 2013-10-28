/* $Id: RemoteServlet.java 509 2012-09-20 14:43:25Z dobashi $
 * created: 2005/08/03
 */
package com.lavans.lacoder2.remote.servlet;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import com.google.common.base.Stopwatch;
import com.lavans.lacoder2.controller.util.WriteUtils;
import com.lavans.lacoder2.di.BeanManager;
import com.lavans.lacoder2.lang.ClassUtils;
import com.lavans.lacoder2.util.Config;

/**
 *
 * @author dobashi
 */
@WebServlet(urlPatterns="/rs/*")
public class RemoteServlet extends HttpServlet {
	/**
	 *
	 */
	private static final long serialVersionUID = -4697771162210548502L;

	/** logger */
	private static final Logger logger = LoggerFactory.getLogger(RemoteServlet.class.getName());

	private static final String ENCODING_ORG="ISO-8859-1";
	private static final String ENCODING_JSON="UTF-8";
	/** invoker */
	private RemoteInvoker invoker = BeanManager.getBean(RemoteInvoker.class);

	/**
	 * GET method.
	 * Just write message. Do not invoke remote method.
	 *
	 * デバッグ時はgetも有効です。
	 *
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		logger.debug(request.getPathInfo() +"?"+ request.getQueryString());
		if(Config.isDebug()){
			doPost(request, response);
		}else{
			OutputStream os = response.getOutputStream();
			os.write("{\"message\": \"lacoder2 remote\"}".getBytes());
			os.flush();
		}
	}

	private String adujstEncode(String str){
		if(str==null) return null;
		try {
			return new String(str.getBytes(ENCODING_ORG),ENCODING_JSON);
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * POST method.
	 * リモート受付。受け付けたjsonからservice,method,inを取り出して実行します。
	 *
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		MDC.put(RemoteInvoker.UID, request.getParameter(RemoteInvoker.UID));

		Stopwatch stopwatch = new Stopwatch();
		stopwatch.start();

		// request parameter
		request.setCharacterEncoding(ENCODING_JSON);
		// invoke
		String pathInfo = request.getPathInfo();
//		String parameterTypesStr = adujstEncode(request.getParameter("parameterTypes"));
		String argsStr = adujstEncode(request.getParameter("args"));
//		logger.debug("parameterTypes: "+ parameterTypesStr+ ": args"+ argsStr);
//		Class<?>[] parameterTypes = JSON.decode(parameterTypesStr, new Class<?>[]{}.getClass());
//		String[] argsStrs = argsStr.substring(1, argsStr.length()-1).split(",");
		Object args[] = ObjectSerializer.deserialize(argsStr);

//		Object args[] = new Object[parameterTypes.length];
//		for(int i=0; i<parameterTypes.length; i++){
//			args[i] = JSON.decode("["+argsStrs[i]+"]", parameterTypes[i]);
//		}
//		Object args = JSON.decode(argsStr, parameterTypes);
		try{
			byte data[] = invoker.invoke(pathInfo, ClassUtils.toClass(args), args);
			// Write response
			response.setCharacterEncoding("UTF-8");
			WriteUtils writeUtils = BeanManager.getBean(WriteUtils.class);
			writeUtils.writeBytes(response, "application/octet-stream", data);
		}catch(Throwable e){
			logger.error(pathInfo, e);
		}



		// log
		stopwatch.stop();
		logger.info(pathInfo+" "+ stopwatch.elapsed(TimeUnit.MILLISECONDS)+"ms"); // "+argsStr);
	}
}
