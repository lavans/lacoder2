package com.lavans.lacoder2.sql.dao;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.val;


import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.PropertyUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.gaffer.PropertyUtil;

import com.lavans.lacoder2.di.BeanManager;
import com.lavans.lacoder2.lang.StringUtils;
import com.lavans.lacoder2.util.PageInfo;
import com.lavans.lacoder2.util.Pager;

/**
 *
 * @author dobashi
 *
 * @param <T>
 */
public class BaseDao{
	/** logger */
	private static Logger logger = LoggerFactory.getLogger(BaseDao.class);
	/** Common dao */
	private CommonDao dao = BeanManager.getBean(CommonDao.class);

	private static final String DEFAULT_CONNECTION_NAME = "default";

	/**
	 * Constructor.
	 */
	public  BaseDao(){
	}

	/**
	 * load
	 */
	public <T> T load(Class<T> clazz, Object pk){
		return load(clazz, pk, DEFAULT_CONNECTION_NAME);
	}
	public <T> T load(Class<T> clazz, Object pk, String connectionName){
		String sql = getSql(clazz, "load");

		List<T> list = dao.list(clazz, sql, getAttributeMap(pk), connectionName);
		if(list.size()==0){
			logger.debug("target not found.");
			return null;
		}

		return list.get(0);
	}


	/**
	 * load
	 */
	public <T> T loadBak(Class<T> clazz, Object pk){
		return loadBak(clazz, pk, DEFAULT_CONNECTION_NAME);
	}
	public <T> T loadBak(Class<T> clazz, Object pk, String connectionName){
		String sql = getSql(clazz, "loadBak");

		List<T> list = dao.list(clazz, sql, getAttributeMap(pk), connectionName);
		if(list.size()==0){
			logger.debug("target not found.");
			return null;
		}

		return list.get(0);
	}


	/**
	 * nextval
	 * @param entity
	 * @return
	 * @throws SQLException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws NoSuchMethodException
	 * @throws InvocationTargetException
	 */
	public <T> long nextval(Class<T> clazz){
		return nextval(clazz, DEFAULT_CONNECTION_NAME);
	}
	public <T> long nextval(Class<T> clazz, String connectionName){
		return doNextval("nextval", clazz, connectionName);
	}
	private <T> long doNextval(String sqlName, Class<T> clazz, String connectionName){
		// get next sequence
		String sql = getSql(clazz, sqlName);
		List<Map<String, Object>> seqResult = dao.executeQuery(sql, null, connectionName);
		long seq = (Long)seqResult.get(0).values().toArray()[0];
		return seq;
	}

	/**
	 * insert
	 * @param entity
	 * @return
	 * @throws SQLException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws NoSuchMethodException
	 * @throws InvocationTargetException
	 */
	public <T> int insert(T entity) {
		return insert(entity, DEFAULT_CONNECTION_NAME);
	}
	public <T> int insert(T entity, String connectionName) {
		//
		String sql = getSql(entity.getClass(), "insert");
		int result = dao.executeUpdate(sql, getAttributeMap(entity), connectionName);
		if(result!=1){
			logger.debug("insert failure.");
		}

		return result;
	}

	/**
	 * update
	 * @param entity
	 * @return
	 * @throws SQLException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws NoSuchMethodException
	 * @throws InvocationTargetException
	 */
	public <T> int update(T entity) {
		return update(entity, DEFAULT_CONNECTION_NAME);
	}
	public <T> int update(T entity, String connectionName) {
		// update
		String sql = getSql(entity.getClass(), "update");
		int result = dao.executeUpdate(sql, getAttributeMap(entity), connectionName);
		if(result!=1){
			logger.debug("update failure.["+ result +"]");
		}

		return result;
	}

	/**
	 * delete one entity with pk.
	 *
	 * @param clazz
	 * @param pk
	 * @return
	 */
	public int delete(Class<?> clazz, Object pk) {
		return delete(clazz, pk, DEFAULT_CONNECTION_NAME);
	}
	public int deleteBak(Class<?> clazz, Object pk) {
		return delete(clazz, pk, DEFAULT_CONNECTION_NAME);
	}
	public int delete(Class<?> clazz, Object pk, String connectionName) {
		return doDelete("delete", clazz, pk, connectionName);
	}
	public int deleteBak(Class<?> clazz, Object pk, String connectionName) {
		return doDelete("deleteBak", clazz, pk, connectionName);
	}
	private int doDelete(String sqlName, Class<?> clazz, Object pk, String connectionName) {
		// delete
		String sql = getSql(clazz, "delete");

		int result = dao.executeUpdate(sql, getAttributeMap(pk), connectionName);
		if(result!=1){
			logger.debug("delete failure.["+ result +"]");
		}

		return result;
	}

	/**
	 * delete some entites with condition.
	 *
	 * @param clazz entity's class
	 * @param pk
	 * @return
	 */
	public int deleteAny(Class<?> clazz, Condition cond) {
		return deleteAny(clazz, cond, DEFAULT_CONNECTION_NAME);
	}
	public int deleteAnyBak(Class<?> clazz, Condition cond) {
		return deleteAny(clazz, cond, DEFAULT_CONNECTION_NAME);
	}
	public int deleteAny(Class<?> clazz, Condition cond, String connectionName) {
		return doDeleteAny("deleteAny", clazz, cond, connectionName);
	}
	public int deleteAnyBak(Class<?> clazz, Condition cond, String connectionName) {
		return doDeleteAny("deleteAnyBak", clazz, cond, connectionName);
	}
	private int doDeleteAny(String sqlName, Class<?> clazz, Condition cond, String connactionName) {
		// copy for editng key. ex) "name" to "%name%"
		Condition condWork = new Condition(cond);

		// delete
		String sql = getSql(clazz, sqlName);
		sql = sql.replace("$condition", DaoUtils.makeWherePhrase(condWork));
		int result = dao.executeUpdate(sql, DaoUtils.convertSearchCond(condWork, getAttributeInfo(clazz)), connactionName);

		return result;
	}

	/**
	 * list with conditions.
	 * @param searchCondMap
	 * @return
	 */
	public <T> List<T> list(Class<T> clazz, Condition cond) {
		return doList("list", clazz, cond, DEFAULT_CONNECTION_NAME);
	}
	public <T> List<T> list(Class<T> clazz, Condition cond, String connectionName) {
		return doList("list", clazz, cond, connectionName);
	}

	/**
	 * List PK only.
	 * @param clazz
	 * @param cond
	 * @return
	 */
	public <T> List<T> listPk(Class<T> clazz, Condition cond) {
		return doList("listPk", clazz, cond, DEFAULT_CONNECTION_NAME);
	}
	public <T> List<T> listPk(Class<T> clazz, Condition cond, String connectionName) {
		return doList("listPk", clazz, cond, connectionName);
	}

	/**
	 * List from _BAK table.
	 * @param clazz
	 * @param cond
	 * @return
	 */
	public <T> List<T> listBak(Class<T> clazz, Condition cond) {
		return doList("listBak", clazz, cond, DEFAULT_CONNECTION_NAME);
	}
	public <T> List<T> listBak(Class<T> clazz, Condition cond, String connectionName) {
		return doList("listBak", clazz, cond, connectionName);
	}

	/**
	 * list execution
	 * @param sqlName
	 * @param clazz
	 * @param cond
	 * @return
	 */
	private <T> List<T> doList(String sqlName, Class<T> clazz, Condition cond, String connectionName) {
		// copy for editng key. ex) "name" to "%name%"
		Condition condWork = new Condition(cond);

		// list sql
		String sql = getSql(clazz, sqlName);
		sql += DaoUtils.makeWherePhrase(condWork);
		sql += DaoUtils.makeOrderByPhrase(condWork);
		sql = DaoUtils.makeLimitOffset(condWork, sql);

		Map<String, Object> params = DaoUtils.convertSearchCond(condWork, getAttributeInfo(clazz));
		List<T> list = dao.list(clazz, sql, params, connectionName);

		return list;
	}

	/**
	 * Paging list.
	 *
	 * @param clazz
	 * @param cond
	 * @param pageInfo
	 * @return
	 */
	public <T> Pager<T> pager(Class<T> clazz, Condition cond, PageInfo pageInfo) {
		return doPager("count", "pager", clazz, cond, pageInfo, DEFAULT_CONNECTION_NAME);
	}
	public <T> Pager<T> pager(Class<T> clazz, Condition cond, PageInfo pageInfo, String connectionName) {
		return doPager("count", "pager", clazz, cond, pageInfo, connectionName);
	}

	/**
	 * Paging list PK only.
	 *
	 * @param clazz
	 * @param cond
	 * @param pageInfo
	 * @return
	 */
	public <T> Pager<T> pagerPk(Class<T> clazz, Condition cond, PageInfo pageInfo) {
		return doPager("count", "pagerPk", clazz, cond, pageInfo, DEFAULT_CONNECTION_NAME);
	}
	public <T> Pager<T> pagerPk(Class<T> clazz, Condition cond, PageInfo pageInfo, String connectionName) {
		return doPager("count", "pagerPk", clazz, cond, pageInfo, connectionName);
	}

	/**
	 * Paging list from _BAK table.
	 *
	 * @param clazz
	 * @param cond
	 * @param pageInfo
	 * @return
	 */
	public <T> Pager<T> pagerBak(Class<T> clazz, Condition cond, PageInfo pageInfo) {
		return doPager("countBak", "pagerBak", clazz, cond, pageInfo, DEFAULT_CONNECTION_NAME);
	}
	public <T> Pager<T> pagerBak(Class<T> clazz, Condition cond, PageInfo pageInfo, String connectionName) {
		return doPager("countBak", "pagerBak", clazz, cond, pageInfo, connectionName);
	}

	/**
	 * Paging list execution.
	 *
	 * @param clazz
	 * @param cond
	 * @param pageInfo
	 * @return
	 */
	private <T> Pager<T> doPager(String countName, String sqlName, Class<T> clazz, Condition cond, PageInfo pageInfo, String connectionName) {
		// copy for editng key. ex) "name" to "%name%"
		Condition condWork = new Condition(cond);

		// query condition
		String condition = DaoUtils.makeWherePhrase(condWork);
		String order = DaoUtils.makeOrderByPhrase(condWork);

		// count
		String seqSql = getSql(clazz, countName);
		seqSql = seqSql.replace("$condition",condition);
		Map<String, Object> params = DaoUtils.convertSearchCond(condWork, getAttributeInfo(clazz));
		List<Map<String, Object>> seqResult = dao.executeQuery(seqSql, params, connectionName);
		long count = ((Number)seqResult.get(0).values().toArray()[0]).longValue();

		// list
		String sql = getSql(clazz, sqlName);
		sql = sql.replace("$condition",condition);
		sql = sql.replace("$order",order);
		if(sql.contains("_limit")){
			params.put("_limit", pageInfo.getRows());
			params.put("_offset", pageInfo.getPage()*pageInfo.getRows());
		}else{
			// for Oracle
			params.put("_start", pageInfo.getPage()*pageInfo.getRows());
			params.put("_end", (pageInfo.getPage()+1)*pageInfo.getRows());

		}
		logger.debug(params.toString());
		List<T> list = dao.list(clazz, sql, params, connectionName);

		// add to pager
		Pager<T> pager = new Pager<T>(pageInfo);
		pager.setTotalCount(count);
		for(T entity: list){
			pager.add(entity);
		}

		return pager;
	}

	/**
	 * Backup. Copy data to _BAK table.
	 */
	public <T> int backup(Class<T> clazz, Object pk){
		return backup(clazz, pk, DEFAULT_CONNECTION_NAME);
	}
	public <T> int backup(Class<T> clazz, Object pk, String connectionName){
		String sql = getSql(clazz, "backup");
		Map<String, Object> attrMap = getAttributeMap(pk);
		int result = dao.executeUpdate(sql, attrMap, connectionName);
		return result;
	}

	/**
	 * Resrore. Copy data from _BAK table.
	 */
	public <T> int restore(Class<T> clazz, Object pk){
		return restore(clazz, pk, DEFAULT_CONNECTION_NAME);
	}
	public <T> int restore(Class<T> clazz, Object pk, String connectionName){
		String sql = getSql(clazz, "restore");
		int result = dao.executeUpdate(sql, getAttributeMap(pk), connectionName);
		return result;
	}

	/**
	 * convert FQDN class name to base xml file name.
	 * com.lavans.lacoder.Test -> com.lavans.lacoder.base.TestBase
	 *
	 * @param fqdn
	 * @return
	 */
	private String getSql(Class<?> clazz, String key){
		String names[] = clazz.getName().split("\\.");
		String baseName = StringUtils.join(Arrays.copyOf(names, names.length-2), ".") + ".dao.base."+ names[names.length-1]+"DaoBase";

		return DaoUtils.getSql(baseName, key);
	}

	/**
	 * Call "getAttributeMap" method of pk (or entity).
	 * @param obj
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private Map<String, Object> getAttributeMap(Object obj) {
		Map<String, Object> result=null;
		Method method;
		try {
			method = obj.getClass().getMethod("getAttributeMap", (Class<?>[])null);
			result = (Map<String, Object>)method.invoke(obj, (Object[])null);
		} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			throw new RuntimeException(e);
		}

		return result;
//		try {
//		val map =  PropertyUtils.describe(obj);
//		map.remove("attributeMap");
//		map.remove("class");
//		map.remove("parameters");
//		return map;
//	} catch (IllegalAccessException | InvocationTargetException
//			| NoSuchMethodException e) {
//		throw new RuntimeException(e);
//	}
	}

	/**
	 * Call static "getAttributeInfo" method of entity.
	 * @param obj
	 * @return
	 */
	private Map<String, Class<?>> getAttributeInfo(Class<?> beanClass) {
		val descriptors =  PropertyUtils.getPropertyDescriptors(beanClass);
		val result = new HashMap<String, Class<?>>();
		for(val desc: descriptors){
			result.put(desc.getName(), desc.getPropertyType());
		}
		return result;
	}
//
//		try {
//			Method method = clazz.getMethod("getAttributeInfo", (Class<?>[])null);
//			result = (Map<String, Class<?>>)method.invoke(null, (Object[])null);
//		} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
//			throw new RuntimeException(e);
//		}
//		return result;
//	}
}
