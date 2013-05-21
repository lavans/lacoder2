/* $Id: EntityManager.jsp 508 2012-09-20 14:41:55Z dobashi $
 * 作成日: 2013-03-08
 *
 */
package com.lavans.lacoder2.sql.dao;

import java.util.List;
import java.sql.SQLException;

import org.slf4j.Logger;

import com.lavans.lacoder2.di.BeanManager;
import com.lavans.lacoder2.lang.LogUtils;
import com.lavans.lacoder2.sql.dao.BaseDao;
import com.lavans.lacoder2.sql.dao.Condition;
import com.lavans.lacoder2.util.PageInfo;
import com.lavans.lacoder2.util.Pager;


/**
 * EntityManager for 銘柄マスタ.
 * @author dobashi
 *
 */
public abstract class EntityManagerBase<E,PK> {
	/** logger */
	private static Logger logger = LogUtils.getLogger();

	/** dao */
	private BaseDao baseDao = BeanManager.getBean(BaseDao.class);

	private Class<E> clazz;
	
	protected abstract Class<E> getEntityClass();
	
	public EntityManagerBase(){
		clazz = getEntityClass();
		logger.debug(clazz.getSimpleName()+"Manager");
	}

	/**
	 * 更新用クラスを返す。
	 * @param datatype
	 * @return
	 */
	public E get(PK pk) throws SQLException{
		if(pk==null) return null;

		E entity = baseDao.load(clazz,pk);
		return entity;
	}

	/**
	 * 銘柄マスタ登録処理。
	 * @return E ID,登録時間等をセットしたEntityそのものを返す。
	 */
	public E insert(E entity) throws SQLException{
		baseDao.insert(entity);
		return entity;
	}

	/**
	 * 銘柄マスタ更新処理。
	 * @return E 更新時間をセットしたEntityそのものを返す。
	 */
	public E update(E entity) throws SQLException{
		baseDao.update(entity);
		return entity;
	}

	/**
	 * 銘柄マスタ削除
	 * @param E
	 * @return
	 * @throws SQLException
	 */
	public int delete(PK pk) throws SQLException{
		// DBから削除
		int result = baseDao.delete(clazz, pk);
		return result;
	}

	/**
	 * 一覧処理。
	 * @param pageInfo
	 * @param cond
	 * @return
	 * @throws SQLException
	 */
	public List<E> list(Condition cond) {
		return baseDao.list(clazz, cond);
	}

	/**
	 * ページング一覧処理。
	 * @param pageInfo
	 * @param cond
	 * @return
	 * @throws SQLException
	 */
	public Pager<E> pager(Condition cond, PageInfo pageInfo) {
		return baseDao.pager(clazz, cond, pageInfo);
	}
	
}