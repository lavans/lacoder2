package com.lavans.lacoder2.util;

/**
 * Htmlの文字列整形クラス
 *
 * @author hiroftak
 */
public class HtmlFormat {

	/**
	 * HTMLエンコードをして返します
	 *
	 * @param html
	 * @return
	 */
	public static String getHTMLEncode(String html) {

		html = html.replaceAll("&", "&amp;");
		html = html.replaceAll("\"", "&quot;");
		html = html.replaceAll("'", "&#39;");
		html = html.replaceAll("<", "&lt;");
		html = html.replaceAll(">", "&gt;");

		return html;
	}
}
