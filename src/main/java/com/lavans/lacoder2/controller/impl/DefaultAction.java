package com.lavans.lacoder2.controller.impl;

/**
 * Default action.
 * Return jsp name same as action.
 * ex)
 * "test/TestAction" -> return "test/Test.jsp"
 * 
 * @author sbisec
 *
 */
public class DefaultAction {
	private String actionName;
	private String methodName;
	public void setInfo(String actionName, String methodName){
		this.actionName = actionName;
		this.methodName = methodName;
	}
	public String execute(){
		// replace last of name to "jsp"
		String suffix = methodName.equals("execute")?"":"-"+methodName;
		suffix += ".jsp";
		return actionName.replace("Action",suffix); // .replace(actionPath, jspPath);
	}
}
