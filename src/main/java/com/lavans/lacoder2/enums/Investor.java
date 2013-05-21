package com.lavans.lacoder2.enums;


/**
 * customer/visitor の type
 * @author tnoda
 */
public enum Investor {
	CUSTOMER("customer"),
	VISITOR("visitor");

	/** customer/visitorのタイプ */
	private final String type;

	/**
	 * コンストラクタ
	 * @param type
	 */
	private Investor(String type) {
		this.type = type;
	}

	/**
	 * type を返す
	 * @return
	 */
	public String getType() {
		return type;
	}

	/**
	 * typeをもとにEnumを取得する
	 * @param type
	 * @return
	 */
	public static Investor getEnum(String type) {
		for (Investor investor : values()) {
			if (investor.getType().equals(type)) {
				return investor;
			}
		}
		return null;
	}
}
