package com.lavans.lacoder2.sql.bind.impl;

import static org.junit.Assert.*;

import java.awt.image.RescaleOp;
import java.beans.Statement;

import lombok.extern.slf4j.Slf4j;
import mockit.Mocked;

import org.junit.Test;

@Slf4j
public class BindPreparedStatementImplTest {
	private BindPreparedStatementImpl target = null;

//	@Mocked
//	private Statement st;

	@Test
	public void testログ文字列_行末() {

//		String errstr = "deferred:host mxbackup2.junkemailfilter.com[184.105.182.217] said: 451-DEFER - TB3 - Try a lower numbered MX record - [2  FakeMX NoHeloHost 451-BlkSndr] - X=tarbaby H=static-ip-31-27-191-203.rev.nshk.net [203.191.27.31] 451-HELO=[re11.localdomain] F=[bounce-simbabo=comui.edu.ng@vjapancom.info] 451-T=[simbabo@comui.edu.ng] S=[?$B:#$OL\\$NA0$K$\"$k8=<B!\";v<B$r<u$1;_?(B?$B$a!ZJ";
//		String errstr = "[?$B:#$OL\\$NA0$K$\"$k8=<B!\";v<B$r<u"; // $1;_?(B?$B$a!ZJ";
//		String errstr = "[?$B:#$OL\\$NA0"; // $1;_?(B?$B$a!ZJ";
		String errstr = "\\$\\A"; // $1;_?(B?$B$a!ZJ";
//		String str2 = "abc".replaceAll("abc$", errstr);
//
//		log.info(str2);

//		// setup
		target = new BindPreparedStatementImpl(null, "update member set comment=:comment", null);
//
//		// exec
		target.logParam(":comment", errstr);
//
//		// check
		assertEquals("update member set comment="+ errstr, target.getSql());
	}

	@Test
	public void testログ文字列スペース() {
		String errstr = "\\$\\A"; // $1;_?(B?$B$a!ZJ";

//		// setup
		target = new BindPreparedStatementImpl(null, "update member set comment=:comment and ABC", null);
//
//		// exec
		target.logParam(":comment", errstr);
//
//		// check
		assertEquals("update member set comment="+ errstr +" and ABC", target.getSql());
	}

	@Test
	public void testログ文字列_カンマ() {
		String errstr = "\\$\\A"; // $1;_?(B?$B$a!ZJ";

//		// setup
		target = new BindPreparedStatementImpl(null, "update member set comment=:comment,and ABC", null);
//
//		// exec
		target.logParam(":comment", errstr);
//
//		// check
		assertEquals("update member set comment="+ errstr +",and ABC", target.getSql());
	}

	@Test
	public void testログ文字列_カッコ() {
		String errstr = "\\$\\A"; // $1;_?(B?$B$a!ZJ";
		// setup
		target = new BindPreparedStatementImpl(null, "update member set (comment=:comment)and ABC", null);
		// exec
		target.logParam(":comment", errstr);
		// check
		assertEquals("update member set (comment="+ errstr +")and ABC", target.getSql());
	}

	@Test
	public void testログ文字列_バクすら() {
		String errstr = "\\$\\A"; // $1;_?(B?$B$a!ZJ";
		// setup
		target = new BindPreparedStatementImpl(null, "update member set comment=:comment\\and ABC", null);
		// exec
		target.logParam(":comment", errstr);
		// check
		assertEquals("update member set comment="+ errstr +"\\and ABC", target.getSql());
	}
}
