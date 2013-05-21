package com.lavans.lacoder2.sql.dao;

import static com.lavans.lacoder2.sql.dao.ConditionTypeEnum.CLOSE_BRACKET;
import static com.lavans.lacoder2.sql.dao.ConditionTypeEnum.EQUAL;
import static com.lavans.lacoder2.sql.dao.ConditionTypeEnum.FUZZY_SEARCH;
import static com.lavans.lacoder2.sql.dao.ConditionTypeEnum.GREATER;
import static com.lavans.lacoder2.sql.dao.ConditionTypeEnum.GREATER_EQUAL;
import static com.lavans.lacoder2.sql.dao.ConditionTypeEnum.IS_NOT_NULL;
import static com.lavans.lacoder2.sql.dao.ConditionTypeEnum.IS_NULL;
import static com.lavans.lacoder2.sql.dao.ConditionTypeEnum.LESS;
import static com.lavans.lacoder2.sql.dao.ConditionTypeEnum.LESS_OR_EQUAL;
import static com.lavans.lacoder2.sql.dao.ConditionTypeEnum.NOT_EQUAL;
import static com.lavans.lacoder2.sql.dao.ConditionTypeEnum.OPEN_BRACKET;
import static com.lavans.lacoder2.sql.dao.ConditionTypeEnum.OR;
import static com.lavans.lacoder2.sql.dao.ConditionTypeEnum.OR_FUZZY_SEARCH;
import static com.lavans.lacoder2.sql.dao.ConditionTypeEnum.OR_PREFIX_SEARCH;
import static com.lavans.lacoder2.sql.dao.ConditionTypeEnum.OR_SUFFIX_SEARCH;
import static com.lavans.lacoder2.sql.dao.ConditionTypeEnum.PREFIX_SEARCH;
import static com.lavans.lacoder2.sql.dao.ConditionTypeEnum.SUFFIX_SEARCH;

import java.util.LinkedHashMap;
import java.util.Map;

import com.lavans.lacoder2.lang.StringUtils;


/**
 * 検索条件クラス
 * Map<String, String[]>を毎回newしないでいいようにするための
 * コンビニクラス
 *
 * @author dobashi
 *
 */
public class Condition implements Cloneable{
	/** Parameter with sort order */
	private LinkedHashMap<String, String[]> params = new LinkedHashMap<>();

	private String orderBy=null;
	private int limit=0;
	private int offset=0;

	public Condition(){
		super();
	}

	/**
	 * Copy Constructor.
	 * copy from Map.
	 * @param params
	 */
	public Condition(Condition src){
		if(src==null){
			return;
		}
		this.params = new LinkedHashMap<>(src.getMap());
		orderBy(src.getOrderBy());
		limit(src.limit);
		offset(src.offset);
	}

	/**
	 * Constructor.
	 * copy from Map.
	 * @param params
	 */
	public Condition(Map<String, String[]> params){
		this.params = new LinkedHashMap<>(params);
	}

	public Map<String, String[]> getMap(){
		return params;
	}

	public Condition equal(String field, String value){
		params.put(field+"."+EQUAL.toString(), new String[]{value});
		return this;
	}
	public Condition notEqual(String field, String value){
		params.put(field+"."+NOT_EQUAL.toString(), new String[]{value});
		return this;
	}
	public Condition or(String field, String value){
		repeatable(field, value, OR);
		return this;
	}
	public Condition greater(String field, String value){
		params.put(field+"."+GREATER.toString(), new String[]{value});
		return this;
	}
	public Condition greaterEqual(String field, String value){
		params.put(field+"."+GREATER_EQUAL.toString(), new String[]{value});
		return this;
	}
	public Condition less(String field, String value){
		params.put(field+"."+LESS.toString(), new String[]{value});
		return this;
	}
	public Condition lessOrEqual(String field, String value){
		params.put(field+"."+LESS_OR_EQUAL.toString(), new String[]{value});
		return this;
	}
	/**
	 * ORあいまい検索
	 * ORは同じキーで複数の条件が指定されるケースがある
	 * @param field
	 * @param value
	 * @return
	 */
	public Condition orFuzzySearch(String field, String value){
		repeatable(field, value, OR_FUZZY_SEARCH);
		return this;
	}
	public Condition orPrefixSearch(String field, String value){
		repeatable(field, value, OR_PREFIX_SEARCH);
		return this;
	}
	public Condition orSuffixSearch(String field, String value){
		repeatable(field, value, OR_SUFFIX_SEARCH);
		return this;
	}
	
	/**
	 * OR検索の場合等、同名のキーで繰り返し登録できるようにキーに連番を振って登録する。
	 * @param field
	 * @param value
	 * @param type
	 */
	private void repeatable(String field, String value, ConditionTypeEnum type){
		String baseKey = field+"."+type.toString();
		String key;
		int i=0;
		do{
			key=baseKey+"."+i++;
		}while(params.containsKey(key));
		
		params.put(key, new String[]{value});
	}
	
	public Condition fuzzySearch(String field, String value){
		params.put(field+"."+FUZZY_SEARCH.toString(), new String[]{value});
		return this;
	}
	public Condition prefixSearch(String field, String value){
		params.put(field+"."+PREFIX_SEARCH.toString(), new String[]{value});
		return this;
	}
	public Condition suffixSearch(String field, String value){
		params.put(field+"."+SUFFIX_SEARCH.toString(), new String[]{value});
		return this;
	}
	public Condition isNull(String field){
		params.put(field+"."+IS_NULL.toString(), new String[]{Boolean.TRUE.toString()});
		return this;
	}
	public Condition isNotNull(String field){
		params.put(field+"."+IS_NOT_NULL.toString(), new String[]{Boolean.TRUE.toString()});
		return this;
	}

	/**
	 * multiple selection. array version.
	 * @param field
	 * @param values
	 */
	public Condition in(String field, String... values){
		params.put(field+".multiple", values);
		return this;
	}

	/**
	 * multiple selection. CSV(comma separate value) version.
	 * カンマ区切り複数選択。
	 *
	 * @param field
	 * @param value
	 */
	public Condition in(String field, String value){
		params.put(field+".multiple", StringUtils.splitTrim(value,","));
		return this;
	}

	/**
	 * Brackets, Parentheses open (
	 */
	public Condition openBrackets(){
		params.put("."+OPEN_BRACKET.toString(), new String[]{Boolean.TRUE.toString()});
		return this;
	}

	/**
	 * Brackets, Parentheses close )
	 */
	public Condition closeBrackets(){
		params.put("."+CLOSE_BRACKET.toString(), new String[]{Boolean.TRUE.toString()});
		return this;
	}

	@Override
	public String toString(){
		StringBuilder str = new StringBuilder();
		str.append("{");
		for(Map.Entry<String, String[]> entry: params.entrySet()){
			str.append(entry.getKey()+"=["+StringUtils.join(entry.getValue(),",")+"]");
		}
		str.append("}");
		return str.toString();
	}

	public String getOrderBy() {
		return orderBy;
	}

	/**
	 * set "ORDER BY".
	 * @param orderBy Use small charactors. if it is capital, convert to "_" letters.
	 * @return
	 */
	public Condition orderBy(String orderBy) {
		this.orderBy = orderBy;
		return this;
	}

	public Condition orderByDesc(String orderBy) {
		this.orderBy = orderBy +" desc";
		return this;
	}

	public int getLimit() {
		return limit;
	}

	public Condition limit(int limit) {
		this.limit = limit;
		return this;
	}

	public int getOffset() {
		return offset;
	}

	public Condition offset(int offset) {
		this.offset = offset;
		return this;
	}
}
