/* $Id: Validator.java 509 2012-09-20 14:43:25Z dobashi $
 * create: 2004/12/28
 * (c)2004 Lavans Networks Inc. All Rights Reserved.
 */
package com.lavans.lacoder2.lang;

import java.util.List;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lavans.lacoder2.util.Config;

/**
 * @author dobashi
 * @version 1.00
 */
public class Validator {
	private static Logger logger = LoggerFactory.getLogger(Validator.class.getName());

	/**
	 * 汎用validator
	 * @param item
	 * @return
	 */
	public static boolean isValid(String key, String item){
		// check item is null
		if(StringUtils.isEmpty(item)){
			return false;
		}

		boolean result = false;
		String regex = Config.getInstance().getNodeValue("validator/pattern[@name='"+ key +"']");
		result = item.matches(regex);
		return result;
	}

	/**
	 * メールアドレスモバイルチェック用一覧。
	 */
	private static List<String> domainList = null;
	static{
		try {
			domainList = Config.getInstance("mobile.xml").getNodeValueList("/root/mobile_mail/domain");
		} catch (RuntimeException e) {
			logger.info("携帯メールドメイン指定無し");
		}
	}

	public static boolean isValidMail(String item){
		return isValid("mail", item);
	}

	/**
	 * メールアドレス、ドメイン部がモバイルかチェック
	 * @param item
	 * @return
	 */
	public static boolean isValidMailMobile(String item){
		if(!isValidMail(item)){
			return false;
		}
		for(String domain: domainList){
			if(item.contains(domain)){
				return true;
			}
		}
		return false;
	}

			//		if(item.matches("[\\d]{2,5}-[\\d]{1,4}-[\\d]{4}")){
//		if(item.matches("[\\d]{3}-[\\d]{4}")){
}
