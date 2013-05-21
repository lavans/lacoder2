/* $Id: Scope.java 509 2012-09-20 14:43:25Z dobashi $ */
package com.lavans.lacoder2.di.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Scope {
	Type value() default Type.SINGLETON;

	// String (like spring)
//	public static final String SINGLETON="singleton";
//	public static final String REQUEST="request";
//	public static final String SESSION="session";
//	public static final String PROTOTYPE="prototype";

}
