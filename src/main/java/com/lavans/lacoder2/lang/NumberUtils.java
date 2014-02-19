package com.lavans.lacoder2.lang;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;

public class NumberUtils extends org.apache.commons.lang3.math.NumberUtils{
	/** 数値フォーマッター */
	private static NumberFormat nf  = NumberFormat.getInstance(Locale.JAPANESE);

	/** 値段フォーマットの埋め文字 * */
	public static final String	HYPHEN	= "-";

	/**
	 * パラメータに従って数値を修飾する
	 *
	 * @param value
	 *            修飾する対象の数値
	 * @param scale
	 *            小数点以下桁数。NumberFormatに任せる場合は-1を指定する。
	 * 			  NumberFormatを使用した場合、小数点桁数を指定できない。
	 * @param roundingMode 丸め指定。
	 * @param plusSign
	 *            true:正の場合+を付加する false:何もしない
	 * @return 修飾後の文字列
	 */
	public static String numberFormat(BigDecimal value, int scale, boolean plusSign) {
		return numberFormat(value, scale, RoundingMode.HALF_UP, plusSign);
	}
	public static String numberFormat(BigDecimal value, int scale, RoundingMode roundingMode, boolean plusSign) {
		//対象の数値が未設定の場合"-"(ハイフン)を返す
		if (value == null) {
			return HYPHEN;
		}

		String result = null;
		// 数値のフォーマット
		if(scale<0){
			// scaleの指定がマイナス指定なら小数点桁数指定なしでフォーマットする。
			result = nf.format(value);
		}else{
			// scaleが0以上なら指定した桁数のDecimalFormatを使う
			result = formatScale(value, scale, roundingMode);
		}

		// 符号毎の色を付ける
		if (plusSign && (value.signum() == 1)) {
			// +
			result = "+" + result;
		}

		return result;
	}

	/**
	 *
	 * @param value
	 * @return
	 */
	public static String numberFormat(BigDecimal value) {
		//対象の数値が未設定の場合"-"(ハイフン)を返す
		if (value == null) {
			return HYPHEN;
		}
		String result = nf.format(value);
		return result;
	}


	/**
	 * 小数点桁数指定でフォーマットする。
	 * @param decimal
	 * @param scale
	 * @return String
	 */
	public static String formatScale(BigDecimal value, int scale, RoundingMode roundingMode) {
		//対象の数値が未設定の場合"-"(ハイフン)を返す
		if (value == null) {
			return HYPHEN;
		}

		String result;
		value = value.setScale(scale, roundingMode);

		NumberFormat scaleFormat = NumberFormat.getInstance();
		scaleFormat.setMinimumFractionDigits(scale);
		scaleFormat.setMaximumFractionDigits(scale);

//		StringBuffer fill = new StringBuffer("");
//		if (scale > 0) {
//			fill.append(".");
//			for (int i = 0; i < scale; i++) {
//				fill.append("0");
//			}
//		}
//		DecimalFormat df = new DecimalFormat("#,##0" + fill.toString());
		result = scaleFormat.format(value);
		return result;
	}

	/**
	 * 符号付きフォーマット
	 * @param value
	 * @return
	 */
	public static String formatSigned(int value){
		NumberFormat nf = NumberFormat.getInstance();
		String result = nf.format(value);
		if(value>0){
			result = "+"+result;
		}
		return result;
	}

	/**
	 * 数値かどうか判定する。
	 * 桁区切りカンマ(,)を許可しない
	 *
	 * @param src
	 * @return
	 */
	public static boolean isNumeric(String src) {
		return isNumeric(src, false);
	}

	/**
	 * 数値かどうか判定する。
	 * 桁区切りカンマ(,)を許可を指定する。true: 許可 false:非許可
	 *
	 * @param src
	 * @return
	 */
	public static boolean isNumeric(String src, boolean hasComma) {
		if (src == null) {
			return false;
		}

		src = src.trim();
		for (int i = 0; i < src.length(); i++) {
			// 数値またはピリオド以外なら
			if (!Character.isDigit(src.charAt(i)) && src.charAt(i) != '.' &&
				// カンマ非許可ならカンマ以外もアウト
				!(hasComma && src.charAt(i) == ',')) {
				return false;
			}
		}

		return true;
	}

	/**
	 * 文字列配列をIntegerの配列に変換。
	 * 文字列が"n-m"の形式の時はnからmの間の数値も生成する。
	 * 改行は無視します。
	 *
	 * @param src
	 * @return
	 */
	public static Integer[] toIntegerArray(String[] strs) {
		ArrayList<Integer> list = new ArrayList<>();
		for (String str: strs) {
			if(str.isEmpty()) continue;
			Integer[] values = parseRange(str);
			for(int value: values){
				list.add(value);
			}
		}
		return list.toArray(new Integer[]{});
	}

	/**
	 * 配列の中に"-"が含まれている要素があれば前後の数字を元にその範囲内も含めて配列にする
	 *
	 * @param strs
	 * @return
	 */
	public static Integer[] parseRange(String str) {
		// "-"が含まれているか
		if(!str.contains("-")){
			return new Integer[]{Integer.valueOf(str)};
		}

		// 含まれていればハイフンで分割を行い、範囲内の数値をListに追加していく
		String range[] = StringUtils.splitTrim(str, "-");
		int start = Integer.valueOf(range[0]);
		int end = Integer.valueOf(range[1]);

		Integer[] result = new Integer[end-start];
		for(int i=0; i<=result.length; i++){
			result[i] = i+start;
		}
		return result;
	}

}
