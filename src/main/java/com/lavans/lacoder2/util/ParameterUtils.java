package com.lavans.lacoder2.util;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;


import org.slf4j.Logger;

import com.lavans.lacoder2.lang.LogUtils;
import com.lavans.lacoder2.lang.StringUtils;

/**
 * IParameterizableのデータをやりとりするユーティリティ群。
 * prefixはIParametarizable#getParameters()に実装することにした
 * そうしないとsetParameters()とかloadStringのprefixとの関係があやふやになる
 *
 * @author dobashi
 *
 */
public class ParameterUtils {
	private static Logger logger = LogUtils.getLogger();
	/**
	 * Map<String, String>からIParameterizable#setParameters()に適した
	 * Map<String, String[]>形式に変換。
	 *
	 * @return
	 */
	public static Map<String, String[]> convertToStringArrayMap(Map<String, String> map){
		// Map<String, String[]>からMap<String, String>に変換
		Map<String, String[]> newMap = new HashMap<String, String[]>();
		for(Map.Entry<String, String> entry: map.entrySet()){
			newMap.put(entry.getKey(), new String[]{entry.getValue()});
		}
		return newMap;
	}

	/**
	 * Stringから読み込み。
	 * 引数で渡された文字列key1=value1&key2=value2..からMap<String, String[]>を作成する
	 *
	 * @param str
	 * @see getStoreString()
	 */
	public static Map<String, String[]> toMap(String str){
		return toMap(str, null);
	}
	public static Map<String, String[]> toMap(String str, String encoding){
		Map<String, String[]> params = new HashMap<String, String[]>();
		if(!str.contains("=")) return params;
		String strs[] = str.split("&");
		for(int i = 0; i < strs.length; i++){
			if(strs[i].equals("")){
				continue;
			}
			if(encoding!=null){
				try {
					strs[i] = new String(URLDecoder.decode(strs[i], "iso-8859-1").getBytes("iso-8859-1"),encoding);
				} catch (UnsupportedEncodingException e) {
					// エンコード指定エラーはバグ
					throw new RuntimeException(e);
				} catch (IllegalArgumentException e) {
					logger.debug(strs[i] +"is not invalid URL encoding." + e.getMessage());
					// このパラメータは無視する
					continue;
				}
			}
			String datas[] = strs[i].split("=",2);
			String key = datas[0];
			String value=datas.length==1?"":datas[1];
			if(params.containsKey(key)){
				// 存在している場合は文字列配列を取り出して最後に追加する
				int length = params.get(key).length;
				String values[] = new String[length+1];
				System.arraycopy(params.get(key), 0, values, 0, length);
				values[length] = value;
				params.put(datas[0], values);
			}else{
				params.put(datas[0], new String[]{ value });
			}
		}

		return params;
	}

	/**
	 * ターゲットをStringに変換。key1=value1形式。
	 * プリフィクス、エンコード無し。
	 * @param target
	 * @return
	 */
	public static String toStoreString(Parameterizable target){
		return toStoreString(target, "", null);
	}

	/**
	 * ターゲットをStringに変換。key1=value1形式。
	 * エンコード無し。
	 * @param target
	 * @prefix キー名の前につけるprefix. 例: 「cond.」をつければcond.memberId=1&cond.memberName=ああ
	 * @return
	 */
	public static String toStoreString(Parameterizable target, String prefix){
		return toStoreString(target, prefix, null);
	}

	/**
	 * ターゲットをkey1=value1形式のStringに変換。
	 * HTML用にvalue部分だけエンコードする。
	 * @param target
	 * @return
	 */
	public static String toStoreString(Parameterizable target, String prefix, String encoding){
		if(target==null) return "";
		return toStoreString(target.getParameters(prefix), encoding);
	}

	/**
	 * Mapからkey1=value1&形式のStringに変換。
	 * HTML用にvalue部分だけエンコードする。
	 * @param target
	 * @return
	 */
	public static String toStoreString(Map<String, String[]> params){
		return toStoreString(params, null, true);
	}
	/**
	 *
	 * @param params
	 * @param encoding
	 * @return
	 */
	public static String toStoreString(Map<String, String[]> params, String encoding){
		return toStoreString(params, encoding, true);
	}
	/**
	 *
	 * @param params
	 * @param encoding
	 * @param ignoreEmpty Weather ignore null or "" value.
	 * @return
	 */
	public static String toStoreString(Map<String, String[]> params, String encoding, boolean ignoreEmpty){
		// nullチェック
		if (params == null){
			return "";
		}

		StringBuilder result = new StringBuilder();
		for(Map.Entry<String, String[]> entry: params.entrySet()){
			String[] values=entry.getValue();
			if(values==null){
				result.append(entry.getKey() + "=null&");
				continue;
			}
			for(String value: values){
				if(ignoreEmpty && StringUtils.isEmpty(value)){
					continue;
				}
				if(!StringUtils.isEmpty(value) && !StringUtils.isEmpty(encoding)){
					try {
						value = URLEncoder.encode(value, encoding);
					} catch (UnsupportedEncodingException e) {}
				}
				result.append(entry.getKey() + "="+ value +"&");
			}
		}

		return result.toString();
	}
}
