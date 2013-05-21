package jp.co.sbisec.iris.common.lang;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import org.slf4j.Logger;
import org.testng.annotations.Test;

import com.lavans.lacoder2.lang.LogUtils;
import com.lavans.lacoder2.lang.PeriodUtils;

public class PeriodUtilsTest {
	private static Logger logger = LogUtils.getLogger();

	private static final long SECOND = 1000L;
	private static final long MINUTE = 60*SECOND;
	private static final long HOUR = 60*MINUTE;
	private static final long DAY = 24*HOUR;

	@Test
	public void prettyFormat() {
		String str;
		str = PeriodUtils.prettyFormat(MINUTE);
		logger.info(str);
		assertEquals(str, "1m");
		
		str = PeriodUtils.prettyFormat(4*DAY+3*HOUR+2*MINUTE+1*SECOND);
		logger.info(str);
		assertEquals(str, "4d3h2m1s");

		str = PeriodUtils.prettyFormat(1000*HOUR);
		assertEquals(str, String.format("%dd%dh", 1000/24, 1000%24));
		logger.info(str);
	}
	
	@Test
	public void prettyParse正常(){
		long actual = PeriodUtils.prettyParse("10m10s");
		long expected = 10*MINUTE+10*SECOND;
		assertEquals(actual, expected);
		
		actual = PeriodUtils.prettyParse("3d2h1m99s");
		expected = 3*DAY+ 2*HOUR + 1*MINUTE + 99*SECOND;
		assertEquals(actual, expected);
	}

	public void prettyParseパラメータ異常(){
		try {
			long actual = PeriodUtils.prettyParse("10m10");
			logger.error("Wrong format parsed." + actual);
			assertTrue(false);
		} catch (IllegalArgumentException e) {
			assertTrue(true);
		}
	}
}
