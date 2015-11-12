package com.lavans.lacoder2.sql.dao;

import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.BeanUtilsBean;
import org.apache.commons.beanutils.ConvertUtilsBean;
import org.apache.commons.beanutils.Converter;
import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lavans.lacoder2.lang.StringUtils;
import com.lavans.lacoder2.sql.DBManager;
import com.lavans.lacoder2.sql.bind.BindConnection;
import com.lavans.lacoder2.sql.bind.BindPreparedStatement;
import com.lavans.lacoder2.util.PageInfo;
import com.lavans.lacoder2.util.Pager;

/**
 *
 * @author dobashi
 *
 * @param <T>
 */
public class CommonDao{
	/** logger */
	private static Logger logger = LoggerFactory.getLogger(CommonDao.class);
	/** default connection name */
	private static final String DEFALUT_CONNECTION = "default";
	//, seq;
	/**
	 * Constructor.
	 */
	public  CommonDao(){
	}

	/**
	 * Executes the given SQL statement, which returns ResultSet object. This method convert ResultSet to List<Map<String, Object>>
	 *
	 * @return converted data.
	 */
	public List<Map<String, Object>> executeQuery(String sql){
		return executeQuery(sql, null, DEFALUT_CONNECTION);
	}
	public List<Map<String, Object>> executeQuery(String sql, Map<String, Object> params){
		return executeQuery(sql, params, DEFALUT_CONNECTION);
	}
	public List<Map<String, Object>> executeQuery(String sql, Map<String, Object> params, String connectionName){
		//logger.debug(sql);
		List<Map<String, Object>> result = new ArrayList<Map<String,Object>>();
		BindConnection con = null;
		BindPreparedStatement st = null;
		try {
			con = DBManager.getConnection(connectionName);
			st = con.bindPrepareStatement(sql);
			st.setParams(params);
			// execute SQL.
			ResultSet rs = st.executeQuery();
			result = rsToMapList(rs);
			rs.close();
			logger.debug("result count = "+ result.size());
		}catch (SQLException e) {
			// SQLException needs rethrow.
			throw new RuntimeException(e);
		} finally {
			free(st, con);
		}
		return result;
	}

	private void free(Statement st, Connection con){
		try { if(st!=null) st.close(); } catch (Exception e) { logger.error("",e); }
		try { if(con!=null) con.close(); } catch (Exception e) { logger.error("",e); }
	}
	/**
	 * ResultSetからList<Map<String, Object>>に変換
	 * @param rs
	 * @return
	 * @throws SQLException
	 */
	private List<Map<String, Object>> rsToMapList(ResultSet rs) {
		StopWatch stopWatch = new StopWatch();
		stopWatch.start();
		List<Map<String, Object>> result = null;

		try {
			result = new ArrayList<Map<String,Object>>();
			// For sppedup. read metadata only once.
			if(!rs.next()){
				return result;
			}
			ResultSetMetaData metaData = rs.getMetaData();
			List<String> columnNameList = new ArrayList<>();
			for(int i=1; i<metaData.getColumnCount()+1; i++){
				columnNameList.add(metaData.getColumnName(i));
			}

			do{
				Map<String, Object> record = new LinkedHashMap<String, Object>();
				for(int i=0; i<columnNameList.size(); i++){
					record.put(columnNameList.get(i), rs.getObject(i+1));
				}
				result.add(record);
			}while (rs.next());

		} catch (SQLException e) {
			throw new RuntimeException(e);
		}

		stopWatch.stop();
		logger.debug("Metadata -> Map time count["+ result.size() +"] time "+ stopWatch.getTime()+ "ms");

		return result;
	}

	/**
	 *  Executes the given SQL statement, which returns effective rows(INSERT/DELETE/UPDATE) or returns nothing(DDL);
	 */
	public int executeUpdate(String sql){
		return executeUpdate(sql, null, DEFALUT_CONNECTION);
	}
	public int executeUpdate(String sql, Map<String, Object> params){
		return executeUpdate(sql, params, DEFALUT_CONNECTION);
	}
	public int executeUpdate(String sql, Map<String, Object> params, String connectionName){
		logger.debug(sql);
		int result = -1;
		BindConnection con = null;
		BindPreparedStatement st = null;
		try {
			con = DBManager.getConnection(connectionName);
			st = con.bindPrepareStatement(sql);
			st.setParams(params);
			// execute SQL.
			result = st.executeUpdate();
		}catch (SQLException e) {
			throw new RuntimeException(e);
		} finally {
			free(st, con);
		}
		return result;
	}

	/**
	 *
	 *
	 * @return
	 * @throws SQLException
	 * @throws InvocationTargetException
	 * @throws NoSuchMethodException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 */
	public <T> List<T> list(Class<T> clazz, String sql, Map<String, Object> params) {
		return list(clazz, sql, params, DEFALUT_CONNECTION);
	}
	public <T> List<T> list(Class<T> clazz, String sql, Map<String, Object> params, String connectionName){
		if(StringUtils.isEmpty(sql)){
			throw new RuntimeException("sql is empty["+ sql +"]");
		}

		// execute sql
		List<Map<String, Object>> list = executeQuery(sql, params, connectionName);

		// make entity instance from result data.
		List<T> resultList = new ArrayList<T>();
		for(Map<String, Object> dataMap: list){
			resultList.add(mapToEntity(dataMap, clazz));
		}
		return resultList;
	}

	/**
	 * list for pager.
	 * You have to insert ":offset" and ":limit" like this:
	 * "SELECT * FROM MEMBER OFFSET :offset LIMIT :limit".
	 *
	 * @param <T>
	 * @param countSql	SQL string for count all.
	 * @param pageInfo
	 * @param sql		SQL string for select.
	 * @param params
	 * @return
	 * @throws SQLException
	 */
	public <T> Pager<T> list(Class<T> clazz, String countSql, PageInfo pageInfo, String sql, Map<String, Object> params){
		return list(clazz, countSql, pageInfo, sql, params, DEFALUT_CONNECTION);
	}
	public <T> Pager<T> list(Class<T> clazz, String countSql, PageInfo pageInfo, String sql, Map<String, Object> params, String connectionName){
		// execute count sql
		List<Map<String, Object>> list = executeQuery(countSql);
		int count = (Integer)list.get(0).values().toArray()[0];

		// make select sql
		int start = pageInfo.getPage() * pageInfo.getRows();
//		sql.replace(":offset", String.valueOf(start));
//		sql.replace(":limit",  String.valueOf(pageInfo.getRows()));
		// これでもいける
		params.put(":offset", start);
		params.put(":limit", pageInfo.getRows());

		// execute sql
		list = executeQuery(sql, params);

		// make entity instance from result data.
		Pager<T> pager = new Pager<T>(pageInfo);
		pager.setTotalCount(count);
		for(Map<String, Object> dataMap: list){
			pager.add(mapToEntity(dataMap, clazz));
		}
		return pager;
	}

	/**
	 * SQLから呼び出した汎用Map<String, Object>からEntityに変換
	 * try to set all columns to entity with java.lang.reflection
	 *
	 * @param <T>
	 * @param record
	 * @param clazz
	 * @return T entity
	 */
	private <T> T mapToEntity(Map<String, Object> record, Class<T> clazz)  {
		T entity=null;
		try {
			entity = clazz.newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			logger.error("",e);
		}
		Map<String, Object> camelCaseMap = new HashMap<String, Object>();
		for(Map.Entry<String, Object> column: record.entrySet()){
			camelCaseMap.put(StringUtils.toCamelCase(column.getKey()), column.getValue());
		}

		// キー文字列変換
		camelCaseMap = convertKey(camelCaseMap);

		try {
			BeanUtils.populate(entity, camelCaseMap);
		} catch (IllegalAccessException | InvocationTargetException e) {
			throw new RuntimeException(e);
		}


		return entity;
	}

	/**
	 * プロパティの2文字目が大文字の場合にPropertyDescriptorが1文字目も大文字だと
	 * 判断するようなので、SQLデータの方を1文字目大文字に変更する。
	 * @param src
	 * @return
	 */
	private Map<String, Object> convertKey(Map<String, Object> src){
		// 空チェック
		if(src.isEmpty()){
			return src;
		}

		// 1文字目小文字、2文字目大文字チェック
		String key = src.keySet().iterator().next();
		if(!(Character.isLowerCase(key.charAt(0)) && Character.isUpperCase(key.charAt(1)))){
			return src;
		}

		// 変換
		Map<String, Object> renameMap = new HashMap<>();
		for(Entry<String, Object> entry: src.entrySet()){
			renameMap.put(StringUtils.capitalize(entry.getKey()), entry.getValue());
		}
		return renameMap;
	}

	/**
	 * Nullを許可するコンバーター。
	 */
	private static class NullableConverter implements Converter {
		Converter converter;
		public NullableConverter(Converter converter){
			this.converter = converter;
		}
		@SuppressWarnings("rawtypes")
		@Override
		public Object convert(Class type, Object value) {
			if(value==null){
				return null;
			}
			return converter.convert(type, value);
		}
	}
	static{
		ConvertUtilsBean convertUtilsBean = BeanUtilsBean.getInstance().getConvertUtils();
		Converter bigDecimalConverter = convertUtilsBean.lookup(BigDecimal.class);
		convertUtilsBean.register(new NullableConverter(bigDecimalConverter), BigDecimal.class);

		Converter dateConverter = convertUtilsBean.lookup(Date.class);
		convertUtilsBean.register(new NullableConverter(dateConverter), Date.class);
	}
}
