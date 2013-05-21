package com.lavans.lacoder2.cache;

import java.util.Date;

/**
 * キャッシュデータ格納クラス。
 * キャッシュデータの種別ごとに有効期限をチェックするため、
 * 各キャッシュの取得時間をセットで持ちます。
 * 
 * 
 * @author sbisec
 *
 * @param <O>
 */
public class CacheValue<O> {
	/** キャッシュデータ。DtoOut */
	private O out;
	/** キャッシュ格納日。有効期限チェックに使用します。 */
	private Date expire;
	public CacheValue(){
	}
	/**
	 * キャッシュデータを格納します。
	 * 格納した日付もセットで保存します。
	 * 
	 * @param out
	 */
	public CacheValue(O out, Date expire){
		this.out = out;
		this.expire = expire;
	}
	public O getOut() {
		return out;
	}
	public Date getExpire() {
		return expire;
	}

	public void setOut(O out) {
		this.out = out;
	}
	public void setExpire(Date expire) {
		this.expire = expire;
	}
}
