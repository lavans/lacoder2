package com.lavans.lacoder2.remote.servlet;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.beanutils.MethodUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import com.lavans.lacoder2.di.BeanManager;
import com.lavans.lacoder2.di.ServiceManager;
import com.lavans.lacoder2.remote.servlet.converter.Converter;
import com.lavans.lacoder2.remote.servlet.converter.RelativeConverter;

public class RemoteInvoker {
	/** logger */
	private static Logger logger = LoggerFactory.getLogger(RemoteInvoker.class);

	/** リモート接続UIDキー. */
	public static  final String UID ="_uid";

	/** service-method splitter in url. */
	private static  final String METHOD_SPLITTER ="-";

	/** URLインターフェース方式:Relative固定 */
	private Converter converter = BeanManager.getBean(RelativeConverter.class);

	/**
	 * Invoke execution.
	 *
	 * @param request
	 * @return
	 */
	public byte[] invoke(String url, Class<?>[] parameterTypes, Object[] args){
		logger.debug(url + " paramTypes:"+  Arrays.toString(parameterTypes) +" args:"+ Arrays.toString(args));

		Object out=null;
		ServiceInfo info = null;
		try {
			// Get method to execute
			info = ServiceInfo.getInstance(url, parameterTypes, args, converter);
			out = MethodUtils.invokeMethod(info.service, info.methodName, args);
		} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException e) {
			// Bug in calling method
			throw new RuntimeException(info.service.getClass().getSimpleName()+"#"+info.methodName+"("+args+")", e);
		} catch (InvocationTargetException e){
			// Exception in service method
			logger.error(e.getMessage(), e.getCause());
			out = e.getCause();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			out = e;
		}
		return ObjectSerializer.serialize(out);
	}

	/**
	 * リモート実行するメソッドをURL文字列に変換する。
	 * @param method
	 * @return
	 */
	public String toUrl(Method method){
		String serviceName =  converter.toUrl(method.getDeclaringClass().getName());
		return serviceName + METHOD_SPLITTER + method.getName();
	}

	/**
	 * メソッド引数からPOST用パラメータを作成します。
	 * 引数はjsonエンコードされ、PL/BLで一意のUIDをセットします。
	 *
	 * @param args リモート実行メソッドの引数
	 * @return post用パラメータ。
	 */
	public Map<String, String> makePostData(Object[] args){
		// 引数は一つDtoInのみ
		Map<String, String> postData = new HashMap<String, String>();
		postData.put("args", ObjectSerializer.serializeString(args));
		postData.put(RemoteInvoker.UID, MDC.get(UID));
		return postData;
	}

	/**
	 * UIDの生成とローカルスレッドへの保存。
	 * keyがHttpServletRequestの場合はセッションIDを使用し、それ以外なら
	 * 現在時刻とkey#hashCode()とを使用する。
	 * ローカルスレッドへの保存はslf4jのMDCを利用し、そのままログ出力できるようにする。
	 *
	 * @param key UID生成のキーとなるオブジェクト。
	 * @return
	 */
	public void setUid(Object key){
		String id=null;
		if(key instanceof HttpServletRequest){
			id = ((HttpServletRequest)key).getSession().getId();
		}else{
			id = Long.toString(System.currentTimeMillis(), Character.MAX_RADIX)+"-"+
					Integer.toString(key.hashCode(),Character.MAX_RADIX);
		}
		MDC.put(UID, id);
	}

	/**
	 *
	 * @author sbisec
	 *
	 */
	public static class ServiceInfo{
		Object service;
		String methodName;

		/**
		 * URLから実行メソッド情報の取得
		 * @param url
		 * @return
		 * @throws NoSuchMethodException
		 * @throws SecurityException
		 */
		static ServiceInfo getInstance(String url, Class<?>[] parameterTypes, Object[] args, Converter converter) throws NoSuchMethodException, SecurityException{
			String urls[] = url.split(METHOD_SPLITTER);
			String serviceName = toServiceName(urls[0], converter);

			ServiceInfo info = new ServiceInfo();
			info.service = ServiceManager.getServiceLocal(serviceName);
			info.methodName = urls[1];
			logger.debug(serviceName+"#"+info.methodName+"()");

			return info;
		}

		/**
		 * Make service name from url.
		 * "member.MemberService" -> "com.example.project.service.member.MemberService".
		 *
		 * @param url
		 * @return
		 */
		private static String toServiceName(String url, Converter converter){
			// iris-common managerクラスの場合はfqdn固定
			String serviceName = url.replace("/", ".");
			if(serviceName.startsWith(".com.lavans.lacoder2.manager")){
				return serviceName.substring(1);
			}
			return converter.toServiceName(serviceName);
		}
	}
}
