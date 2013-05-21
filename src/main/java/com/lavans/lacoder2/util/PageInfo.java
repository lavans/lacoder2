/* $Id: PageInfo.java 554 2012-10-15 15:21:46Z dobashi $
 * created: 2005/08/08
 */
package com.lavans.lacoder2.util;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lavans.lacoder2.lang.StringUtils;


/**
 * Pager情報。
 * 現在のページ番号、１ページあたりの行数など、
 * Daoクラスから一覧読み込みを行うときに必要な情報を保持する。
 * @author dobashi
 */
public class PageInfo implements Parameterizable, Serializable{
	/**
	 * logger
	 */
	private static Logger logger = LoggerFactory.getLogger(PageInfo.class);

	/**
	 *
	 */
	private static final long serialVersionUID = 5562045120082975793L;

	/**
	 * Default select rows.
	 *
	 */
	private static Integer DEFAULT_ROWS = 10;
	private static Integer[] DEFAULT_ROWS_SELECT = new Integer[]{10,50,100};

	/** 無制限 */
	public static final int ROWS_UNLIMITED = -1;

	public static final String PAGE="page";
	public static final String ROWS="rows";
	public static final String PREV="prev";
	public static final String NEXT="next";

	// read rows_select from lacoder.xml.
	static{
		Config config = Config.getInstance();

		String rowsStr = config.getNodeValue("pager/rows");
		try {
			DEFAULT_ROWS = Integer.parseInt(rowsStr);
		} catch (NumberFormatException e) {
			// defalutの件数
		}

		// rows_select(CSV)
		String rowsSelectStr = config.getNodeValue("pager/rows-select");
		String rowsSelects[] = StringUtils.splitTrim(rowsSelectStr, ",");
		try{
			// When definition rows_select
			if(rowsSelects.length>0){
				Integer[] newRowsSelects = new Integer[rowsSelects.length];
				for(int i=0; i<rowsSelects.length; i++){
					newRowsSelects[i] = new Integer(rowsSelects[i]);
				}
				DEFAULT_ROWS_SELECT = newRowsSelects;
			}
		}catch (NumberFormatException e) {
			logger.warn(Config.CONFIG_FILE + ":pager/rows-select is not defined.["+rowsSelectStr+"]");
		}
	}

	/** 現在のページ。0オリジン。 */
	private int page=0;

	/** 1ページに表示する行数:default10。 */
	private int rows = 10;
	private Integer[] rowsSelect = new Integer[]{10,50,100};

	public Integer[] getRowsSelect(){
		return rowsSelect;
	}
	/**
	 * コンストラクタ。表示行数指定無し。
	 */
	public PageInfo() {
		rows = DEFAULT_ROWS;
		rowsSelect = DEFAULT_ROWS_SELECT;
	}

	/**
	 * コンストラクタ。表示行数指定。
	 */
	public PageInfo(int rows) {
		super();
		this.rows = rows;
	}

	/** ページ数取得 */
	public int getPage(){
		return page;
	}
	/** ページ数設定 */
	public void setPage(int x){
		page = x;
	}
	/**
	 * @param rows 表示行数を設定。
	 */
	public void setRows(int rows) {
		this.rows = rows;
	}

	/**
	 * 表示行数取得。
	 * @author dobashi
	 * @return int 表示行数
	 */
	public int getRows() {
		// 無制限ならintの上限を返す
		if(rows == ROWS_UNLIMITED){
			return Integer.MAX_VALUE;
		}
		return rows;
	}

	/**
	 * Htmlへ書き出す必要があるパラメータの取得。
	 * @see com.tradersfs.carrera.common.HtmlParameterizable#getParameters()
	 */
	public Map<String, String[]> getParameters() {
		return getParameters("");
	}
	public Map<String, String[]> getParameters(String prefix) {
		Map<String, String[]> result = new HashMap<String, String[]>();
		result.put(prefix+PAGE, new String[]{String.valueOf(page)});
		result.put(prefix+ROWS, new String[]{String.valueOf(rows)});
		return result;
	}

	/**
	 * パラメータから自クラスの状態をセットする。
	 * @see com.tradersfs.carrera.common.HtmlParameterizable#setParameters(java.util.Map)
	 */
	public void setParameters(Map<String, String[]> map) {
		setParameters(map, "");
	}
	public void setParameters(Map<String, String[]> map, String prefix) {
		// ページ設定
		try{
			page = Integer.parseInt((map.get(prefix+PAGE))[0]);
		}catch (Exception e) {
			page = 0;
		}
		// 行数
		try{
			if(map.get(prefix+ROWS)!=null) rows = Integer.parseInt((map.get(prefix+ROWS))[0]);
		}catch (Exception e) {
			// 変更しない
		}

		// フォームボタンの場合のPREV/NEXT
		if(map.get(prefix+PREV)!=null) page -= 1;
		if(map.get(prefix+NEXT)!=null) page += 1;
	}
}
