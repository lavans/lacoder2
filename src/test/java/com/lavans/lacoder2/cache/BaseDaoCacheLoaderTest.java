package com.lavans.lacoder2.cache;

import static org.testng.Assert.assertEquals;
import mockit.Deencapsulation;
import mockit.Expectations;
import mockit.Mocked;

import org.testng.annotations.Test;

import com.lavans.lacoder2.cache.BaseDaoCacheLoader;
import com.lavans.lacoder2.sql.dao.BaseDao;

public class BaseDaoCacheLoaderTest {
	/** テスト対象 */
	BaseDaoCacheLoader<String, Object> target;

	/** モック */
	@Mocked
	BaseDao baseDao;
	
	/** テスト用パラメータ */
	Class<Object> clazz = Object.class;

	@Test
	public void コンストラクタで指定したクラスが保存されている() {
		target = new BaseDaoCacheLoader<String, Object>(clazz);

		// 検証
		Class<Object> actual = Deencapsulation.getField(target, "clazz");

		assertEquals(actual, clazz);
	}


	@Test
	public void loadが正常に呼び出されている() throws Exception {
		target = new BaseDaoCacheLoader<String, Object>(clazz);
		Deencapsulation.setField(target, baseDao);

		// モックの期待動作
		new Expectations() {{
			baseDao.load(clazz, "1"); result = "result";
		}};

		// テスト対象クラスの呼び出し
		Object result = target.load("1");
		
		// 検証
		assertEquals(result, "result");
	}
}
