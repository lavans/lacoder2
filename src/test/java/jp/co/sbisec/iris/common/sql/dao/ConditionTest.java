package jp.co.sbisec.iris.common.sql.dao;

import static org.testng.Assert.assertTrue;

import java.util.Map;

import jp.co.sbisec.iris.common.sql.dao.mock.MockEntity;

import org.slf4j.Logger;
import org.testng.annotations.Test;

import com.lavans.lacoder2.lang.LogUtils;
import com.lavans.lacoder2.sql.dao.Condition;
import com.lavans.lacoder2.sql.dao.DaoUtils;

public class ConditionTest {
	private static Logger logger = LogUtils.getLogger();
	@Test
	public void orFuzzySearch() {
		Condition cond = new Condition().equal("deleteFlag", "0")
		.openBrackets()
		.fuzzySearch("categories", "Aカテゴリ")
		.orFuzzySearch("categories", "Bカテゴリ")
		.orFuzzySearch("categories", "Cカテゴリ")
		.orFuzzySearch("categories", "Dカテゴリ")
		.closeBrackets();
		
		logger.info(cond.toString());
				
		// list sql
		Condition condWork = new Condition(cond);
		String sql = "SELECT * FROM TRD_NEWS_HEADLINE";
		sql += DaoUtils.makeWherePhrase(condWork);
		sql += DaoUtils.makeOrderByPhrase(condWork);
		sql = DaoUtils.makeLimitOffset(condWork, sql);

//		CommonDao dao = BeanManager.getBean(CommonDao.class);
		Map<String, Object> params = DaoUtils.convertSearchCond(condWork, MockEntity.getAttributeInfo());
		logger.info(sql);
		logger.info(params.toString());
		
		assertTrue(sql.contains("categories.fuzzySearch"));
		assertTrue(sql.contains("categories.orFuzzySearch.0"));
		assertTrue(sql.contains("categories.orFuzzySearch.1"));
		assertTrue(sql.contains("categories.orFuzzySearch.2"));

		assertTrue(params.get("categories.fuzzySearch").equals("%Aカテゴリ%"));
		assertTrue(params.get("categories.orFuzzySearch.0").equals("%Bカテゴリ%"));
		assertTrue(params.get("categories.orFuzzySearch.1").equals("%Cカテゴリ%"));
		assertTrue(params.get("categories.orFuzzySearch.2").equals("%Dカテゴリ%"));
//		List<Map<String, Object>> result = dao.executeQuery(sql, params);
	}

	@Test
	public void multiple() {
		Condition cond = new Condition().equal("deleteFlag", "0")
		.in("categories", "A", "B", "C");
		
		logger.info(cond.toString());
				
		// list sql
		Condition condWork = new Condition(cond);
		String sql = "SELECT * FROM TABLE";
		sql += DaoUtils.makeWherePhrase(condWork);
		sql += DaoUtils.makeOrderByPhrase(condWork);
		sql = DaoUtils.makeLimitOffset(condWork, sql);
		logger.info(sql);

		Map<String, Object> params = DaoUtils.convertSearchCond(condWork, MockEntity.getAttributeInfo());
		logger.info(params.toString());
		
		assertTrue(sql.contains("categories.multiple.0"));
		assertTrue(sql.contains("categories.multiple.1"));
		assertTrue(sql.contains("categories.multiple.2"));

		assertTrue(params.get("categories.multiple.0").equals("A"));
		assertTrue(params.get("categories.multiple.1").equals("B"));
		assertTrue(params.get("categories.multiple.2").equals("C"));
		//		List<Map<String, Object>> result = dao.executeQuery(sql, params);
	}

	@Test
	public void list() {
		Condition cond = new Condition().equal("deleteFlag", "0")
		.in("categories", "A,B, C");
		
				
		// list sql
		Condition condWork = new Condition(cond);
		logger.info(condWork.toString());
		String sql = "SELECT * FROM TABLE";
		sql += DaoUtils.makeWherePhrase(condWork);
		sql += DaoUtils.makeOrderByPhrase(condWork);
		sql = DaoUtils.makeLimitOffset(condWork, sql);
		logger.info(sql);

		Map<String, Object> params = DaoUtils.convertSearchCond(condWork, MockEntity.getAttributeInfo());
		logger.info(params.toString());
		
		// check
		assertTrue(sql.contains("categories.multiple.0"));
		assertTrue(sql.contains("categories.multiple.1"));
		assertTrue(sql.contains("categories.multiple.2"));

		assertTrue(params.get("categories.multiple.0").equals("A"));
		assertTrue(params.get("categories.multiple.1").equals("B"));
		assertTrue(params.get("categories.multiple.2").equals("C"));
//		List<Map<String, Object>> result = dao.executeQuery(sql, params);
	}
}
