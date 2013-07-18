package com.lavans.lacoder2.cache;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import net.arnx.jsonic.JSON;

import org.testng.annotations.Test;

import com.lavans.lacoder2.cache.ValidTerm;

public class ValidTermTest {
	@Test
	public void testEncodeDate() {
		System.out.println(new Date(ValidTerm.PAST_TERM));
//		System.out.println(JSON.encode(new ));
		System.out.println("----------");
		
		ValidTerm vt = new ValidTerm();
		System.out.println(vt.getExpireDate());
		System.out.println(JSON.encode(vt));
		
		vt = new ValidTerm(1, TimeUnit.DAYS);
		System.out.println(vt.getExpireDate());
		System.out.println(JSON.encode(vt));
		
	}
}
