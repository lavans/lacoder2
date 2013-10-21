package com.lavans.lacoder2.lang;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PeriodUtils extends org.apache.commons.lang3.time.DateUtils{
	/** logger */
	private static Logger logger = LoggerFactory.getLogger(PeriodUtils.class);
	/**
	 * 引数で渡された時間(ミリ秒)をいい感じにフォーマットします。
	 * 123000 ->
	 * 時刻ではなくて経過時間などをフォーマットするユーティリティです。
	 * そもそもJodaTimeを使うべきか。
	 *
	 * @param time
	 * @return
	 */
	public static String prettyFormat(long period){
		if(period==0){
			return "0ms";
		}
		StringBuffer sb = new StringBuffer();

		sb.append(makePrettyString(period, 1000, "ms"));
		period/=1000;
		sb.insert(0,makePrettyString(period, 60, "s"));
		period/=60;
		sb.insert(0,makePrettyString(period, 60, "m"));
		period/=60;
		sb.insert(0,makePrettyString(period, 24, "h"));
		period/=24;
		sb.insert(0,makePrettyString(period, 0, "d"));
		return sb.toString();
	}

	/**
	 * 一つの時間単位分の文字列を作成します。
	 *
	 * @param src
	 * @param unit
	 * @param unitStr
	 * @return
	 */
	private static String makePrettyString(long src, long unit, String unitStr){
		if(src==0){
			return "";
		}
		if(unit==0){
			return src+unitStr;
		}
		long unitTime = src%unit;
		if(unitTime==0){
			return "";
		}
		return unitTime+unitStr;
	}

	private static Map<String, TimeUnit> unitMap = new HashMap<String, TimeUnit>(){
		private static final long serialVersionUID = 1L;
		{
			put("ms", TimeUnit.MILLISECONDS);
			put("s", TimeUnit.SECONDS);
			put("m", TimeUnit.MINUTES);
			put("h", TimeUnit.HOURS);
			put("d", TimeUnit.DAYS);
		}
	};

	/**
	 * 時間をパースします。"4d3h2m1s"のような文字列を読み取り、ミリ秒に変換します。
	 *
	 * @param str
	 * @return
	 */
	public static long prettyParse(String str){
		logger.debug(str);
		if(StringUtils.isNumeric(str)){
			return Long.parseLong(str);
		}
		String strs[] = StringUtils.splitByCharacterType(str);
		if(strs.length%2!=0){
			throw new IllegalArgumentException("Invalid format +'"+ str +"'.");
		}

		long result=0;
		for(int i=0; i<strs.length; i+=2){
			result += parseData(strs[i], strs[i+1]);
		}
		return result;
	}

	private static long parseData(String durationStr, String unitStr){
		long duration = Long.parseLong(durationStr);
		TimeUnit unit = unitMap.get(unitStr);
		if(unit==null){
			throw new IllegalArgumentException("'"+durationStr+unitStr + "'is not valid period.");
		}
		return unit.toMillis(duration);
	}
}
