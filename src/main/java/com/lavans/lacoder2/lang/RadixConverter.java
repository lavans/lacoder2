package com.lavans.lacoder2.lang;

import lombok.val;
import lombok.extern.slf4j.Slf4j;

/**
 * n進数文字列コンバーター。nは2-64まで。
 * 0-9a-zA-Z\-_を使うのでURLSafeな文字列になります。
 *
 * @author mdobashi
 *
 */
@Slf4j
public class RadixConverter {
	private static final String DIGITS_ALL = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ-_";
	public static final Integer MAX_RADIX = DIGITS_ALL.length();
	private final int radix;
	private final String digits;

	public RadixConverter(){
		this(MAX_RADIX);
	}
	public RadixConverter(Integer radix){
		if(radix>MAX_RADIX) {
			val m = "radix must be under $max [$radix]"
				.replace("$max",MAX_RADIX.toString())
				.replace("$radix", radix.toString());
			throw new IllegalArgumentException(m);
		}
		this.radix = radix;
		digits = DIGITS_ALL.substring(0,radix);
	}

	/**
	 * long\u5024\u3092n\u9032\u6570\u306b\u5909\u63db\u3057\u307e\u3059\u3002
	 *
	 * @param l
	 * @param s
	 * @return
	 */
	public String toString(long l){
		return toString(l,"");
	}
	private String toString(long l, String s){
//		log.debug(l+":"+ s);

		if(l<digits.length()){
//			log.debug("last:"+digits.charAt((int)l));
			return charAt(digits,l)+s;
		}
		return toString(l/radix, charAt(digits, l%radix)+s);
	}
	private char charAt(String s, long l){
		return s.charAt((int)l);
	}

	public long toLong(String str){
		return toLong(0L, str);
	}

	private long toLong(long l, String s){
		log.debug(l+":"+ s);
		if(s.length()==1){ return l + digits.indexOf(head(s)); }
		val add = digits.indexOf(head(s))*radix;
		log.debug("head:"+head(s)+",next:"+ tail(s) + ",add:"+add);
		return toLong(l*radix+add, tail(s));
	}

	private char head(String s){ return s.charAt(0); }
	private String tail(String s){ return s.substring(1); };
}
