package com.lavans.lacoder2.sql.dao;

import java.util.Map;

import com.lavans.lacoder2.lang.StringUtils;


/**
 * Search condition type.
 *
 * @author dobashi
 *
 */
public enum ConditionTypeEnum {
	EQUAL{
		public void processCondition(String key, String field, StringBuilder builder, Map<String, String[]> cond){
			builder.append(" AND "+ field +" = :"+ key);
		}
	},
	NOT_EQUAL{
		public void processCondition(String key, String field, StringBuilder builder, Map<String, String[]> cond){
			builder.append(" AND "+ field +" <> :"+ key);
		}
	},
	OR{
		public void processCondition(String key, String field, StringBuilder builder, Map<String, String[]> cond){
			builder.append(" OR  "+ field +" = :"+ key);
		}
	},
	GREATER{
		public void processCondition(String key, String field, StringBuilder builder, Map<String, String[]> cond){
			builder.append(" AND "+ field +" > :"+ key);
		}
	},
	GREATER_EQUAL{
		public void processCondition(String key, String field, StringBuilder builder, Map<String, String[]> cond){
			builder.append(" AND "+ field +" >= :"+ key);
		}
	},
	LESS{
		public void processCondition(String key, String field, StringBuilder builder, Map<String, String[]> cond){
			builder.append(" AND "+ field +" < :"+ key);
		}
	},
	LESS_OR_EQUAL{
		public void processCondition(String key, String field, StringBuilder builder, Map<String, String[]> cond){
			builder.append(" AND "+ field +" <= :"+ key);
		}
	},
	FUZZY_SEARCH{
		public void processCondition(String key, String field, StringBuilder builder, Map<String, String[]> cond){
			builder.append(" AND "+ field +" LIKE :"+ key);
			// replace keyword
			cond.put(key, new String[]{"%"+cond.get(key)[0]+"%"});
		}
	},
	PREFIX_SEARCH{
		public void processCondition(String key, String field, StringBuilder builder, Map<String, String[]> cond){
			builder.append(" AND "+ field +" LIKE :"+ key);
			// replace keyword
			cond.get(key)[0]=cond.get(key)[0]+"%";
		}
	},
	SUFFIX_SEARCH{
		public void processCondition(String key, String field, StringBuilder builder, Map<String, String[]> cond){
			builder.append(" AND "+ field +" LIKE :"+ key);
			// replace keyword
			cond.get(key)[0]="%"+cond.get(key)[0];
		}
	},
	OR_FUZZY_SEARCH{
		public void processCondition(String key, String field, StringBuilder builder, Map<String, String[]> cond){
			builder.append(" OR  "+ field +" LIKE :"+ key);
			// replace keyword
			cond.put(key, new String[]{"%"+cond.get(key)[0]+"%"});
		}
	},
	OR_PREFIX_SEARCH{
		public void processCondition(String key, String field, StringBuilder builder, Map<String, String[]> cond){
			builder.append(" OR  "+ field +" LIKE :"+ key);
			// replace keyword
			cond.get(key)[0]=cond.get(key)[0]+"%";
		}
	},
	OR_SUFFIX_SEARCH{
		public void processCondition(String key, String field, StringBuilder builder, Map<String, String[]> cond){
			builder.append(" OR  "+ field +" LIKE :"+ key);
			// replace keyword
			cond.get(key)[0]="%"+cond.get(key)[0];
		}
	},
	IS_NULL{
		public void processCondition(String key, String field, StringBuilder builder, Map<String, String[]> cond){
			builder.append(" AND "+ field +" IS NULL");
			// remove key. "is null" does not need value.
			cond.remove(key);
		}
	},
	IS_NOT_NULL{
		public void processCondition(String key, String field, StringBuilder builder, Map<String, String[]> cond){
			builder.append(" AND "+ field +" IS NOT NULL");
			// remove key. "is null" does not need value.
			cond.remove(key);
		}
	},
	MULTIPLE{
		public void processCondition(String key, String field, StringBuilder builder, Map<String, String[]> cond){
			doMultiple(key, field, builder, cond, cond.get(key), Operator.AND);
		}
	},
//	LIST{
//		public void processCondition(String key, String field, StringBuilder builder, Map<String, String[]> cond){
//			// separate all keys
//			doMultiple(key, field, builder, cond, StringUtils.splitTrim(cond.get(key)[0], ","), Operator.AND);
//		}
//	},
	OPEN_BRACKET{
		public void processCondition(String key, String field, StringBuilder builder, Map<String, String[]> cond){
			builder.append(" AND(");
			cond.remove(key);
		}
	},
	CLOSE_BRACKET{
		public void processCondition(String key, String field, StringBuilder builder, Map<String, String[]> cond){
			builder.append(")");
			cond.remove(key);
		}
	},
	ORDER_BY{
		public void processCondition(String key, String field, StringBuilder builder, Map<String, String[]> cond){
		}
	},

	OFFSET{
		public void processCondition(String key, String field, StringBuilder builder, Map<String, String[]> cond){
		}
	},
	
	LIMIT{
		public void processCondition(String key, String field, StringBuilder builder, Map<String, String[]> cond){
		}
	};
	
	@Override
	public String toString(){
		return StringUtils.toCamelCase(name());
	}

	/**
	 * 複数選択
	 * @param key
	 * @param field
	 * @param builder
	 * @param cond
	 */
	private static void doMultiple(
			String key,
			String field,
			StringBuilder builder,
			Map<String, String[]> condMap,
			String values[],
			Operator andor){
		String attributeName = key.substring(0, key.indexOf("."));
		builder.append(andor + field +" IN ( "+ DaoUtils.makeInPhrase(values ,attributeName+".multiple.") +")");
		condMap.remove(key);
		for(int i=0; i<values.length; i++){
			condMap.put(key+"."+i, new String[]{values[i]});
		}
	}
	public abstract void processCondition(String key, String field, StringBuilder builder, Map<String, String[]> cond);
//	EQUAL("="),GREATER_EQUAL(">="),LESS_OR_EQUAL("<="),FUZZY_SEARCH(" LIKE "),LIST,MULTIPLE;
//	String equotation;
//	private CondTypeEnum(String equotation){
//		this.equotation = equotation;
//	}
	
	private enum Operator {
		AND, OR;
		@Override public String toString(){
			return " "+ StringUtils.rightPad(this.name(), 4);
		}
	};
}
