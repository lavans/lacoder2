package com.lavans.lacoder2.enums;

/**
 * 国コードのEnum
 * @author hiroftak
 */
public enum CountryCode {

	/** 日本 */
	JP,
	/** 米国 */
	US,
	/** 中国 */
	CN,
	/** 香港 */
	HK,
	/** 韓国 */
	KR,
	/** ロシア */
	RU,
	/** ベトナム */
	VN,
	/** インドネシア */
	ID,
	/** シンガポール */
	SG,
	/** タイ */
	TH,
	/** マレーシア */
	MY;

	/**
	 * nameを条件にEnumを取得
	 * @param name
	 * @return
	 */
	public CountryCode getCountryCode(String name) {
		for (CountryCode countryCode : values()) {
			if (countryCode.name().equals(name)) {
				return countryCode;
			}
		}
		return null;
	}
}
