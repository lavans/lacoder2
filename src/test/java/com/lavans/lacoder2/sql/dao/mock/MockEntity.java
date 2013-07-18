package com.lavans.lacoder2.sql.dao.mock;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import com.lavans.lacoder2.util.Parameterizable;


public class MockEntity implements Serializable, Cloneable{
	/** serialID */
	private static final long serialVersionUID = 1L;

	/**
	 * Constants definition.
	 */
	public static final String _NAME = "MockEntity";
	public static final String SEQ_NO = "seqNo";
	public static final String DELETE_FLAG = "deleteFlag";
	public static final String CATEGORIES = "categories";

	/**
	 * Get attribute info
	 */
	public static Map<String, Class<?>> getAttributeInfo(){
		Map<String, Class<?>> map = new HashMap<String, Class<?>>();
		map.put(SEQ_NO, String.class);
		map.put(DELETE_FLAG, String.class);
		map.put(CATEGORIES, String.class);

		return map;
	}

	// instance variables.
	/** シーケンス番号 */
	private String seqNo = "";
	/** 削除フラグ */
	private String deleteFlag = "";
	/** 関連カテゴリ */
	private String categories = "";

	/**
	 * Get Map<String, String[]> paramegters from Attributes for HTTP
	 */
	public Map<String, String[]> getParameters() {
		return getParameters("");
	}
	public Map<String, String[]> getParameters(String prefix) {
		Map<String, String[]> map = new HashMap<String, String[]>();
		map.put(prefix+SEQ_NO, new String[]{seqNo});
		map.put(prefix+DELETE_FLAG, new String[]{deleteFlag});
		map.put(prefix+CATEGORIES, new String[]{categories});

		return map;
	}

	/**
	 * Set Attributes from Map<String, String[]> paramegters for HTTP
	 */
	public void setParameters(Map<String, String[]> map){
		setParameters(map, "");
	}
	public void setParameters(Map<String, String[]> map, String prefix){
		try{ if(map.get(prefix+SEQ_NO)!=null) seqNo = map.get(prefix+SEQ_NO)[0];}catch(Exception e){}
		try{ if(map.get(prefix+DELETE_FLAG)!=null) deleteFlag = map.get(prefix+DELETE_FLAG)[0];}catch(Exception e){}
		try{ if(map.get(prefix+CATEGORIES)!=null) categories = map.get(prefix+CATEGORIES)[0];}catch(Exception e){}
	}

	/**
	 * Get Map<String, Object> paramegters from Attributes for SQL
	 */
	public Map<String, Object> getAttributeMap() {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put(SEQ_NO,seqNo);
		map.put(DELETE_FLAG,deleteFlag);
		map.put(CATEGORIES,categories);

		return map;
	}

	/**
	 * Clone method.
	 */
	public Object clone() throws CloneNotSupportedException{
		return super.clone();
	}

	// accessors
	/**
	 * @return シーケンス番号を戻します。
	 */
	public String getSeqNo(){
		return seqNo;
	}

	/**
	 *  シーケンス番号を設定します。
	 */
	public void setSeqNo(String seqNo){
		this.seqNo=seqNo;
	}

	/**
	 * @return 削除フラグを戻します。
	 */
	public String getDeleteFlag(){
		return deleteFlag;
	}

	/**
	 *  削除フラグを設定します。
	 */
	public void setDeleteFlag(String deleteFlag){
		this.deleteFlag=deleteFlag;
	}

	/**
	 * @return 関連カテゴリを戻します。
	 */
	public String getCategories(){
		return categories;
	}

	/**
	 *  関連カテゴリを設定します。
	 */
	public void setCategories(String categories){
		this.categories=categories;
	}

	/**
	 * get primary key.
	 * @return
	 */
	public PK getPk(){
		return new PK(seqNo);
	}

	/**
	 * primary key class definition.
	 * @author dobashi
	 */
	public static class PK implements Parameterizable{
		private static final long serialVersionUID = 1L;

		/** シーケンス番号 */
		private String seqNo = "";

		/**
		 * Constructor.
		 */
		public PK() {
		}
		/**
		 * Constructor.
		 */
		public PK(String seqNo) {
			this.seqNo=seqNo;
		}

		/**
		 * override Object#hashCode().
		 */
		@Override
		public int hashCode() {
			return toString().hashCode();
		}

		/**
		 * override Object#equals().
		 */
		@Override
		public boolean equals(Object obj) {
			// 参照先が同じならtrue
			if(this==obj) return true;
			// このクラス(または派生クラス)のPKでないならfalse
			if(!(obj instanceof MockEntity.PK)){
				return false;
			}
			// PK型で受け取る
			MockEntity.PK o = (MockEntity.PK)obj;
			// 各PKを繋いだ文字列で比較する
			return toString().equals(o.toString());
		}

		/**
		 * toString.
		 * PKの各属性を":"で連結して返す。
		 */
		@Override
		public String toString() {
			return getSeqNo();
		}

		/**
		 * Get Map<String, String[]> paramegters from Attributes for HTTP
		 */
		public Map<String, String[]> getParameters() {
			return getParameters("");
		}
		@Override
		public Map<String, String[]> getParameters(String prefix) {
			Map<String, String[]> map = new HashMap<String, String[]>();
			map.put(prefix+SEQ_NO, new String[]{seqNo});

			return map;
		}

		/**
		 * Set Attributes from Map<String, String[]> paramegters for HTTP
		 */
		public void setParameters(Map<String, String[]> map){
			setParameters(map, "");
		}
		@Override
		public void setParameters(Map<String, String[]> map, String prefix){
			try{ if(map.get(prefix+SEQ_NO)!=null) seqNo = map.get(prefix+SEQ_NO)[0];}catch(Exception e){}
		}

		/**
		 * Get Map<String, Object> paramegters from Attributes for SQL
		 */
		public Map<String, Object> getAttributeMap() {
			Map<String, Object> map = new HashMap<String, Object>();
			map.put(SEQ_NO,seqNo);

			return map;
		}

		// accessors
		/**
		 * @return シーケンス番号を戻します。
		 */
		public String getSeqNo(){
			return seqNo;
		}
	
		/**
		 *  シーケンス番号を設定します。
		 */
		public void setSeqNo(String seqNo){
			this.seqNo=seqNo;
		}

	}
}
