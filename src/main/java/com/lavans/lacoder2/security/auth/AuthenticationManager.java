package com.lavans.lacoder2.security.auth;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lavans.lacoder2.enums.RealDelay;
import com.lavans.lacoder2.lang.ExceptionUtils;
import com.lavans.lacoder2.util.Config;
import com.lavans.lacoder2.util.ThreadSafeSimpleDateFormat;

public class AuthenticationManager {

	/** Logger */
	private static final Logger logger = LoggerFactory.getLogger(AuthenticationManager.class);

	/** 日付フォーマット */
	private static final DateFormat DF = new ThreadSafeSimpleDateFormat("yyyyMMdd");

	/** Iris認証ハッシュ値設定ファイル */
	private static final Config config = Config.getInstance("iris-authentication.xml");

	/** ハッシュ アルゴリズム */
	private static String algorithm = null;

	/** Real用ハッシュ項目 */
	private static List<String> realHashValues = null;
	/** Delay用ハッシュ項目 */
	private static List<String> delayHashValues = null;

	/** Real用ハッシュ値リスト */
	private static volatile List<String> realHashList = null;
	/** Delay用ハッシュ値リスト */
	private static volatile List<String> delayHashList = null;

	/**
	 * シングルトンインスタンス
	 */
	private static final AuthenticationManager _instance = new AuthenticationManager();

	/**
	 * デフォルトコンストラクタ<br>
	 * 利用不可
	 */
	private AuthenticationManager() {

		// 初期化
		init();
	}

	/**
	 * IrisAuthenticationManagerのインスタンスを取得します。
	 *
	 * @return IrisAuthenticationManager
	 */
	public static AuthenticationManager getInstance() {
		return _instance;
	}

	/**
	 * 認証を実施します。
	 *
	 * @param key
	 * @param type
	 * @return 認証可否
	 */
	public boolean accept(String key, RealDelay type) {

		// Real
		if (RealDelay.REAL.equals(type)) {

			return realHashList.contains(key);

		// Delay
		} else {

			return delayHashList.contains(key);
		}
	}

	/**
	 * 当日ハッシュを返します。
	 * リアル／ディレイの種別を文字列で指定します。
	 *
	 * @param type "real"/"delay" それ以外はRuntimeException
	 * @return 当日ハッシュ
	 * @throws RuntimeException "real"/"delay"以外の文字の場合
	 */
	public String getHash(String type) {
		RealDelay realDelay = RealDelay.getEnum(type);
		if(realDelay == null){
			throw new RuntimeException("Invalid RealDelay type["+ type +"]");
		}
		return getHash(realDelay);
	}

	/**
	 * 当日ハッシュを取得します。
	 *
	 * @param type
	 * @return 当日ハッシュ
	 */
	public String getHash(RealDelay type) {

		int todayIndex = 0;

		// Real
		if (RealDelay.REAL.equals(type)) {

			return realHashList.get(todayIndex);

		// Delay
		} else {

			return delayHashList.get(todayIndex);
		}
	}

	/**
	 * 初期化を行います。
	 */
	private void init() {

		// ハッシュアルゴリズム
		algorithm = getConfigProperty("algorithm");

		// Real用ハッシュ項目をリストに設定
		realHashValues = new ArrayList<String>();
		realHashValues.add(getConfigProperty("str1"));
		realHashValues.add(getConfigProperty("str2"));
		realHashValues.add(getConfigProperty("str3"));
		realHashValues.add(getConfigProperty("real"));

		// Delay用ハッシュ項目をリストに設定
		delayHashValues = new ArrayList<String>();
		delayHashValues.add(getConfigProperty("str1"));
		delayHashValues.add(getConfigProperty("str2"));
		delayHashValues.add(getConfigProperty("str3"));
		delayHashValues.add(getConfigProperty("delay"));

		// 日付厳密化
		DF.setLenient(false);
		// ハッシュ値を生成
		// シングルトンインスタンス生成時（サーバ起動時を想定）に、
		// システム日付にてハッシュ値を生成しておく
		resetHash();
	}

	/**
	 * ハッシュ値を算出し、キャッシュ情報を入れ替えます。<br>
	 * システム日にて算出を行います。
	 *
	 * @return Boolean 成否
	 * @throws Exception
	 */
	public synchronized Boolean resetHash() {

		return resetHash(DF.format(new Date()));
	}

	/**
	 * ハッシュ値を算出し、キャッシュ情報を入れ替えます。<br>
	 * 指定日にて算出を行います。
	 *
	 * @return Boolean 成否
	 * @throws Exception
	 */
	public synchronized Boolean resetHash(String date) {

		try {

			// 日付不正
			if (! valiedDate(date)) {

				logger.warn("AuthenticationManager#resetHash:[" + date + "] ivalid date.");

				return Boolean.FALSE;
			}

			// Real用ハッシュ値リストを生成
			List<String> newRealHashList = new ArrayList<String>(3);
			// 当日
			newRealHashList.add(generateHash(date, realHashValues));
			// 一日後
			newRealHashList.add(generateHash(getAdjustedDateByDate(date, 1), realHashValues));
			// 一昨日
			newRealHashList.add(generateHash(getAdjustedDateByDate(date, -1), realHashValues));
			// ハッシュ値リストを交換
			realHashList = newRealHashList;

			logger.info("AuthenticationManager#resetHash:[" + date + "] real completed:[" + realHashList + "].");

			// Delay用ハッシュ値リストを生成
			List<String> newDelayHashList = new ArrayList<String>(3);
			// 当日
			newDelayHashList.add(generateHash(date, delayHashValues));
			// 一日後
			newDelayHashList.add(generateHash(getAdjustedDateByDate(date, 1), delayHashValues));
			// 一昨日
			newDelayHashList.add(generateHash(getAdjustedDateByDate(date, -1), delayHashValues));
			// ハッシュ値リストを交換
			delayHashList = newDelayHashList;

			logger.info("AuthenticationManager#resetHash:[" + date + "] delay completed:[" + delayHashList + "].");

			return Boolean.TRUE;

		} catch (Exception e) {

			logger.error("Exception at AuthenticationManager#resetHash:[" + date + "].");
			logger.error(ExceptionUtils.getStackTraceString(e));

			return Boolean.FALSE;

		} finally {
		}
	}

	private String getAdjustedDateByDate(String date, int adjust) throws Exception {

		Calendar cal = Calendar.getInstance();
		cal.setTime(DF.parse(date));

		// 指定日付に対し、指定日数を+-
		cal.add(Calendar.DATE, adjust);

		return DF.format(cal.getTime());
	}

	private boolean valiedDate(String date) {

		try {
			DF.parse(date);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

// TODO 他VMへの通知
//	/**
//	 * ハッシュ値を算出し、キャッシュ情報を入れ替えます。<br>
//	 * 指定日にて算出を行います。<br>
//	 * APJ全台に対して行います。
//	 *
//	 * @return Map ApjConnection/Boolean
//	 * @throws Exception
//	 */
//	public Map resetHashAll(String date) throws Exception {
//
//		try {
//
//			ApJAllConnector connector = ApJAllConnector.getInstance();
//			Map resultMap = connector.execute(
//				AspHashServiceAp.class.getName(),	// クラス名
//				"resetHash",				// メソッド名
//				new Class[]{String.class},					// 引数の型
//				new Object[]{date}					// 引数
//			);
//
//			return resultMap;
//
//		} finally {
//		}
//	}

	/**
	 * ASP設定より指定キーの値を取得します。
	 *
	 * @param key キー
	 * @return 設定値
	 * @throws Exception
	 */
	private static String getConfigProperty(String key) throws IllegalStateException {

		String value = config.getParameter(key);

		logger.info("iris-authentication key:[" + key + "], value:[" + value + "]");

		if (value == null || value.trim().length() == 0) {
			throw new IllegalStateException("ハッシュ項目の設定がありません:[" + key + "]");
		}

		return value;
	}

	/**
	 * ハッシュ値を生成します。
	 *
	 * @param algorithm アルゴリズム
	 * @param firstValue 初回値
	 * @param hashValues 初回以降ハッシュ項目
	 * @return String ハッシュ値
	 */
	private String generateHash(String firstValue, List<String> hashValues) throws NoSuchAlgorithmException {

		StringBuffer buf = new StringBuffer(40);

		try {

			MessageDigest digest = MessageDigest.getInstance(algorithm);

			digest.update(firstValue.getBytes());

			int size = hashValues.size();

			for (int i = 0; i < size; i++) {
				digest.update(((String) hashValues.get(i)).getBytes());
			}

			byte[] ret = digest.digest();

			for (int i = 0; i < ret.length; i++) {

				int bt = ret[i] & 0xff;

				// バイト値が0x10以下か判定
				if (bt < 0x10) {
					// 0x10以下の場合、文字列バッファに0を追加
					buf.append("0");
				}

				buf.append(Integer.toHexString(bt));
			}

		// 例外発生
		} catch(Exception e) {

			throw new RuntimeException("Exception at generateHash:[" + algorithm + ", " + firstValue + ", " + hashValues + "]", e);
		}

		String hash = buf.toString();

		logger.info("AuthenticationManager#generateHash:[" + algorithm + ", " + firstValue + " -> " + hash + "]");

		return hash;
	}
}
