package com.lavans.lacoder2.util;

import static org.junit.Assert.*;
import lombok.val;

import org.junit.Test;

public class ParameterUtilsTest {

	@Test
	public void パラメータに等号記号がない場合() {
		String paramStr = "123";
		val result =  ParameterUtils.toMap(paramStr);
		assertTrue(result.size()==1);
	}

}
