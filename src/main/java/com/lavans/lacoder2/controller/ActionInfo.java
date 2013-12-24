package com.lavans.lacoder2.controller;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lavans.lacoder2.controller.impl.DefaultAction;
import com.lavans.lacoder2.di.BeanManager;
import com.lavans.lacoder2.lang.StringUtils;

public class ActionInfo {
	/** logger */
	private static final Logger logger = LoggerFactory.getLogger(ActionInfo.class);
	static final String METHOD_SPLITTER ="-";
	private static final String DEFAULT_METHOD="execute";
	private static final WebAppConfig actionConfig = BeanManager.getBean(WebAppConfig.class);

	private static final Map<String, ActionInfo> cache = new ConcurrentHashMap<>();

	/**
	 * アクションへのパスからアクションクラスを作成。
	 * パスにはsuffix(.do)は含まれていない
	 *
	 * path: com.company.project.presentation.admin.action.main
	 * action: MenuActionのインスタンス
	 * method: MenuAction#input()
	 *
	 * @param actionName
	 * @return
	 * @throws RuntimeException アクションクラス、メソッドの生成が出来なかった時。
	 */
	static ActionInfo getInfo(String actionURI) {
		// cahce
		ActionInfo info = cache.get(actionURI);
		if(info!=null){
			return info;
		}

		// create
		info = new ActionInfo();
		setPath(info, actionURI);
		setName(info, actionURI);
		try {
			setClassAndMethod(info);
			setFilter(info, actionURI);
		} catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			throw new RuntimeException(e);
		}

		cache.put(actionURI, info);
		return info;
	}

	/**
	 * Path作成
	 * @param info
	 * @param actionURI
	 */
	private static void setPath(ActionInfo info, String actionURI){
		// actionURI="/admin/action/main/Menu!input"
		info.relativePath = actionURI.substring(0,actionURI.lastIndexOf("/"));
		// path="com.company.project.presentation.admin.action.main";
		info.path = actionConfig.actionPath + info.relativePath.replace("/",".");
		// delete first "." (if web-app action-path is not defined).
		if(info.path.startsWith(".")) info.path = info.path.substring(1);
	}

	/**
	 * 名前情報を作成します。
	 * @param actionURI
	 * @return
	 */
	private static void setName(ActionInfo info, String actionURI){
		// actionName = "MenuAction"
		// methodName = "input"
		String lastPath = actionURI.substring(actionURI.lastIndexOf("/")+1);
		if(lastPath.contains(METHOD_SPLITTER)){
			// set method name
			String names[] = lastPath.split(METHOD_SPLITTER);
			info.actionName = names[0];
			info.methodName = names[1];
		}else{
			// does not have "!", use default method
			info.actionName = lastPath;
			info.methodName = DEFAULT_METHOD;
		}
		// prefix "Action"
		info.actionName +="Action";

		// classname is always start with upper case.
		if(Character.isLowerCase(info.actionName.charAt(0))){
			info.actionName = StringUtils.capitalize(info.actionName);
		}
	}

	/**
	 * ClassクラスとMethodクラスをActionInfoにセットします。
	 * @param info
	 * @param nameInfo
	 * @throws NoSuchMethodException
	 * @throws SecurityException
	 */
	private static void setClassAndMethod(ActionInfo info) throws NoSuchMethodException, SecurityException{
		// Get Action instance
		String actionFqdn=info.path +"."+ info.actionName;
		logger.debug(actionFqdn);

		try {
			// Action has instance fields (request, response, errors...). Prototype(create every new instace) only.
			info.actionClass = Class.forName(actionFqdn);
		} catch (ClassNotFoundException e) {
			// クラスが存在しない場合デフォルトのアクション
			// このパッケージに対応したものにする?
			logger.debug("["+ actionFqdn +"] is not exist. Use default action.");
			info.actionClass = DefaultAction.class;
			info.methodName=DEFAULT_METHOD;
		}
		info.method = info.actionClass.getMethod(info.methodName);
	}

	/**
	 * set filters
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 * @throws SecurityException
	 * @throws NoSuchMethodException
	 * @throws InvocationTargetException
	 * @throws IllegalArgumentException
	 */
	private static void setFilter(ActionInfo info, String actionURI) throws InstantiationException, IllegalAccessException, NoSuchMethodException, SecurityException, IllegalArgumentException, InvocationTargetException{
		info.filterList = new ArrayList<>();
		for(Class<? extends ActionFilter> filterClass: actionConfig.allFilterList){
			ActionFilter filter = BeanManager.getBean(filterClass);
			if(filter.isFilter(actionURI)){
				info.filterList.add(filter);
			}
		}
	}

	/**
	 * Actionのインスタンスを生成します。
	 * @return
	 */
	public Object createAction(){
		Object action;
		try {
			action = actionClass.newInstance();
			if(action instanceof DefaultAction){
				((DefaultAction) action).setInfo(actionName, methodName);
			}
		} catch (InstantiationException | IllegalAccessException e) {
			throw new RuntimeException(e);
		}

		return action;
	}
	public Class<?> getActionClass(){
		return actionClass;
	}
	public Method getMethod(){
		return method;
	}
	public String getPath(){
		return path;
	}

	String actionName, methodName;
	Class<?> actionClass;
	Method method;
	String path,relativePath;
	List<ActionFilter> filterList;

	@Override
	public String toString(){
		return "ActionInfo:["+actionClass.getSimpleName()+"#"+method.getName()+"()]";
	}
}
