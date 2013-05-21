package com.lavans.lacoder2.enums;

/**
 * 銘柄の種類を表すEnum
 * @author hiroftak
 */
public enum TypeCode {

	/** 株式 */
	STOCK,
	/** 指数 */
	INDEX,
	/** 為替 */
	EXCHG,
	/** 外貨MMF */
	MCMMF,
	/** 債券 */
	DBNTR,
	/** ETF */
	ETF;

	/**
	 * nameを条件にEnumを取得
	 * @param name
	 * @return
	 */
	public TypeCode getTypeCode(String name) {
		for (TypeCode typeCode : values()) {
			if (typeCode.name().equals(name)) {
				return typeCode;
			}
		}
		return null;
	}
}
