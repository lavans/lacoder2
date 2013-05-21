/* $Id: Scope.java 509 2012-09-20 14:43:25Z dobashi $ */
package com.lavans.lacoder2.di.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.lavans.lacoder2.sql.DBManager;



@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Transactional {
	String [] values() default DBManager.DEFAULT_DATABASE;
}
