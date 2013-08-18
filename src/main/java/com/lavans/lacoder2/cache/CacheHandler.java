package com.lavans.lacoder2.cache;



/**
 * キャッシュハンドラー。
 * キャッシュする場合は本インターフェースを実装します。
 * キャッシュのキーの同一性判定のためにDtoInをJSON.encode()でStringに変換した
 * 値を使用しています。DtoInをそのままキーにするにはhashCode(),equals()を
 * オーバーライドする必要が有り、実装漏れによるキャッシュミスを防ぐ為です。
 *
 * @author sbisec
 *
 * @param <I>
 * @param <O>
 */
public interface CacheHandler<K, V> {
	/**
	 * キャッシュ最大サイズを返します。
	 * 最大値はLong.MAX_VALUEです。0をセットするとキャッシュしません。
	 * 負の値を返すとCacheManagerに登録したときにエラーになります。
	 *
	 * @return
	 */
	long getMaxCacheSize();

	/**
	 * ロード処理。
	 * 実際にDBや外部サイトにデータを取りにいきます。
	 *
	 * @param key DtoIn
	 * @return
	 */
	V load(K key);
}
