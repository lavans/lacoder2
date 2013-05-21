package com.lavans.lacoder2.cache;

import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * キャッシュの有効期限を表すクラス。
 * 
 * 有効期限5秒に設定すると0秒、5秒、10秒,15秒,...で有効期限が切れます。
 * オフセットを1秒にセットすると1秒、6秒、16秒、...と1秒ずつずれます。
 * 毎日2:00に期限切れになるようにセットするには
 * ValidTerm validTerm = new ValidTerm();
 * validTerm.setTerm(1, TimeUnit.DAYS);
 * validTerm.setOffset(2, TimeUnit.HOURS);
 *
 * @author sbisec
 *
 */
public class ValidTerm {
	/** 有効期限指定：キャッシュクリア無し */
	public static final int FUTURE_TERM = -1;
	/** 有効期限指定：キャッシュ無しで常に更新 */
	public static final int PAST_TERM = 0;
	
	private long term;
	private TimeUnit termUnit=TimeUnit.SECONDS;
	private long offset;
	private TimeUnit offsetUnit=TimeUnit.SECONDS;
	public ValidTerm(){
	}
	public ValidTerm(long term, TimeUnit termUnit){
		setTerm(term, termUnit);
	}
	public ValidTerm(long term, TimeUnit termUnit, long offet, TimeUnit offsetUnit){
		setTerm(term, termUnit);
		setOffset(offet, offsetUnit);
	}
	public long getTerm() {
		return term;
	}
	public void setTerm(long term, TimeUnit termUnit) {
		this.term = term;
		this.termUnit = termUnit;
	}
	public TimeUnit getTermUnit() {
		return termUnit;
	}
	public long getOffset() {
		return offset;
	}
	public void setOffset(long offset, TimeUnit offsetUnit) {
		this.offset = offset;
		this.offsetUnit = offsetUnit;
	}
	public TimeUnit getOffsetUnit() {
		return offsetUnit;
	}
	
	/** 永久時の有効期限 */
	private static final Date FUTURE_DATE = new Date(Long.MAX_VALUE);
	/** リアルタイム時の有効期限 */
	private static final Date PAST_DATE = new Date(0L);
	/**
	 * 有効期限計算。
	 * 有効期限termがFUTURE_TERMなら遠い未来を、
	 * PAST_TERMなら1970/1/1を返します。
	 * 
	 * @return
	 */
	public  Date getExpireDate(){
		int termCheck = new Long(term).intValue();
		switch (termCheck) {
		case FUTURE_TERM:
			return FUTURE_DATE;
		case PAST_TERM:
			return PAST_DATE;
		}

		long now = System.currentTimeMillis();
		long expireTerm = TimeUnit.MILLISECONDS.convert(term,termUnit);
		long offsetDuration = TimeUnit.MILLISECONDS.convert(offset,  offsetUnit);
		long expire = now - now%expireTerm + expireTerm + offsetDuration;

		return new Date(expire);
	}
}
