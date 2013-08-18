package com.lavans.lacoder2.cache;

import static org.testng.Assert.*;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import mockit.Deencapsulation;

import org.slf4j.Logger;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.lavans.lacoder2.cache.mock.MockCacheHandler;
import com.lavans.lacoder2.di.BeanManager;
import com.lavans.lacoder2.lang.LogUtils;
import com.lavans.lacoder2.manager.CacheService;
import com.lavans.lacoder2.manager.dto.CacheGetNameListIn;
import com.lavans.lacoder2.stats.Statistics;
import com.lavans.lacoder2.stats.StatsRecord;


public class CacheManagerTest {
	private static final Logger logger = LogUtils.getLogger();
	/** テスト対象 */
	CacheManager<String, Object> target = new CacheManager<>();

	/** テスト用パラメータ */
	final String key = "key";
	final Object value = "value";

	Statistics stats;

	@BeforeTest
	public void setup(){
		// targetインスタンス取得
		target = BeanManager.getBean(CacheManager.class.getName());
		target.setCacheHandler(new MockCacheHandler());

		// 統計
		Deencapsulation.setField(CacheManager.class, "isStatistics", true);
		stats = BeanManager.getBean(Statistics.class);

	}

	@Test
	public void CacheHandlerをセットしたらManagerServiceに登録される() {

		// 検証
		// ManagerServiceに登録されているか
		CacheService service = BeanManager.getBean(CacheService.class);
		Collection<String> list = service.getNameList(new CacheGetNameListIn()).getCacheNameList();
		logger.info(list.toString());
		assertEquals(list.toString(), "[MockCache]");
		logger.info(target.asMap().toString());
	}

	@Test
	public void getで取得できる() {
		Object actual = target.get(key);
		assertEquals(actual, value);
	}

	@Test
	public void asMapで取得できる() {
		target.get(key);
		Object actualFromMap = target.asMap().get(key);
		assertEquals(actualFromMap, value);
	}

	@Test
	public void initで初期化したら初期データが入る() {
		target.init();
		Object actual = target.get("initKey");
		assertEquals(actual, "initValue");
	}

	@Test
	public void キャッシュカウント() {
		final String key="cacheCountKey";

		// 3回コールする
		target.get(key);
		target.get(key);
		target.get(key);

		// キャッシュヒット回数を取得
		int count = getCount(stats.getRecords(), "CACHE:cacheCountKey");

		// 2回ならOK
		assertEquals(count, 2);
	}

	private int getCount(Collection<StatsRecord> list, String key){
		for(StatsRecord record: list){
			logger.info(record.toString());
			if(record.getKey().equals(key)){
				return record.getCallCount();
			}
		}
		return 0;
	}

	@Test
	public void invalidateを使えばすぐ呼んでもキャッシュを使わない事を確認する() {
		final String key="invalidateKey";
		final String statsKey="CACHE:invalidateKey";
		Deencapsulation.setField(CacheManager.class, "isStatistics", true);

		// 初期化
		stats.getRecords().clear();

		// 2回コールしてヒット回数1にする
		target.get(key);
		target.get(key);
		int count = getCount(stats.getRecords(), statsKey);
		assertEquals(count,  1);

		// 無効化しながら3回コールする
		target.invalidate(key);
		target.get(key);
		target.invalidate(key);
		target.get(key);
		target.invalidate(key);
		target.get(key);
		// 以前のテストから増えていない事
		count = getCount(stats.getRecords(), statsKey);
		assertEquals(count,  1);

		// 無効化しないで3回コールする
		target.get(key);
		target.get(key);
		target.get(key);
		count = getCount(stats.getRecords(), statsKey);
		assertEquals(count,  4);
	}

	@Test
	public void invalidateAllで全部無効() {
		Deencapsulation.setField(CacheManager.class, "isStatistics", true);
		final String key1="key1";
		final String key2="key2";
		final String statsKey1="CACHE:invalidateKey1";
		final String statsKey2="CACHE:invalidateKey2";

		target.get(key1);
		target.get(key2);
		// 全部無効
		target.invalidateAll();
		target.get(key1);
		target.get(key2);

		assertEquals(getCount(stats.getRecords(), statsKey1),0);
		assertEquals(getCount(stats.getRecords(), statsKey2),0);
	}

	@Test
	public void putしたら上書きできる事() {
		assertEquals(target.get(key),"value");
		target.put(key, "newValue");
		// MockCacheHandler#load()は常には"value"を返すが
		// キャッシュが効いてるうちはキャッシュを返す
		assertEquals(target.get(key),"newValue");
	}

	@Test
	public void putAllで全部登録できる事() {
		@SuppressWarnings("serial")
		Map<String, Object> map = new HashMap<String, Object>(){{
			put("putKey1", "putValue1");
			put("putKey2", "putValue2");
			put("putKey3", "putValue3");
		}};
		// 一旦全部無効にして再登録
		target.invalidateAll();
		target.putAll(map);

		assertEquals(target.size(),3);
	}

	@Test
	public void refreshしたらload処理が走る事() {
		// Mockが固定で"value"を返すので、セットしたものがrefresh後に"value"になっていればOK
		@SuppressWarnings("serial")
		Map<String, Object> map = new HashMap<String, Object>(){{
			put("putKey1", "putValue1");
			put("putKey2", "putValue2");
			put("putKey3", "putValue3");
		}};
		// 一旦全部無効にして再登録
		target.invalidateAll();
		target.putAll(map);
		target.refresh("putKey1");
		target.refresh("putKey2");
		assertEquals(target.get("putKey1"),"value");
		assertEquals(target.get("putKey2"),"value");
		// 3はrefresh未実行
		assertEquals(target.get("putKey3"),"putValue3");

	}

}
