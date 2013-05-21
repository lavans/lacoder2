package com.lavans.lacoder2.enums;


/**
 * real / delay の type
 * @author hiroftak
 */
public enum RealDelay {
	REAL("real"),
	DELAY("delay");

	/** real/delayのタイプ */
	private final String type;

	/**
	 * コンストラクタ
	 * @param type
	 */
	private RealDelay(String type) {
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
	public static RealDelay getEnum(String type) {
		for (RealDelay realDelay : values()) {
			if (realDelay.getType().equals(type)) {
				return realDelay;
			}
		}
		return null;
	}
}
