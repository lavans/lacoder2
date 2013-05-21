/* $Id: IAttributeContainer.java 509 2012-09-20 14:43:25Z dobashi $
 * create: 2004/10/18
 * (c)2004 Lavans Networks Inc. All Rights Reserved.
 */
package com.lavans.lacoder2.sql.dao;

import java.io.Serializable;
import java.util.Map;

/**
 * TODO 廃止してcommons.BeanUtilsで代替
 * 
 * @author dobashi
 * @version 1.00
 */
public interface AttributeContainer extends Serializable{
	public Map<String, Class<?>> getAttributeInfo();
	public Map<String, Object> getAttributeMap();
}
