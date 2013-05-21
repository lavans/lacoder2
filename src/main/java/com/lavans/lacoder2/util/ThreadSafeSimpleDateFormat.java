/* $Id: ThreadSafeSimpleDateFormat.java,v 1.2 2006/07/18 05:33:38 yuk Exp $
 * createed: 2006/06/27
 *
 * Copyright Lavans Networks Inc.
 */
package com.lavans.lacoder2.util;

import java.text.AttributedCharacterIterator;
import java.text.DateFormatSymbols;
import java.text.FieldPosition;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

/**
 * スレッドセーフにしたSimpleDateFormatクラス。
 * このクラス内で複数のSimpleDateFormatのインスタンスを作成し、
 * format()やparse()が呼ばれると空いているインスタンスを
 * 探して処理を行う。format()にsynchronizedをかけないので、
 * 高負荷な状況でもあまり速度低下を起こさずスレッドセーフとなる。
 *
 * SimpleDateFormatのインスタンスに状態を持たせるメソッドは
 * 現時点ではsetLenient()のみ対応。下記の機能には未対応。
 * applyLocalizedPattern(String arg0)
 * set2DigitYearStart(Date arg0)
 * setCalendar(Calendar arg0)
 * setDateFormatSymbols(DateFormatSymbols arg0)
 * setNumberFormat(NumberFormat arg0)
 * setTimeZone(TimeZone arg0)
 *
 * @author dobashi
 */
public class ThreadSafeSimpleDateFormat extends SimpleDateFormat{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/** Localeごとにinstanceを保存 */
	private static Map<Locale, Map<String, List<SimpleDateFormat>>> localeInstanceMap =
			Collections.synchronizedMap(new HashMap<Locale, Map<String, List<SimpleDateFormat>>>());

	private String format = null;
	private boolean isLenient = true;
	private Locale locale = null;

	/**
	 * コンストラクタ。
	 * @param format
	 */
	public ThreadSafeSimpleDateFormat(String format){
		this.format = format;
		locale = Locale.getDefault();
	}

	public ThreadSafeSimpleDateFormat(String format, Locale locale){
		this.format = format;
		this.locale = locale;
	}

	/**
	 * プールしたSimpleDateFormatの取得。
	 * @result =
	 */
	private SimpleDateFormat getSdf(){
		List<SimpleDateFormat> poolList = findPoolList();
		
		SimpleDateFormat sdf = null;
		synchronized(poolList){
			for(int i=0; i<poolList.size(); i++){
				sdf = (SimpleDateFormat)poolList.get(i);
				if(sdf.isLenient()==isLenient){
					poolList.remove(sdf);
					return sdf;
				}
			}
		}

		sdf = new SimpleDateFormat(format, locale);
		sdf.setLenient(isLenient);
		return sdf;
	}
	
	/**
	 * locale,formatからpoolListを探す。
	 * 
	 * @return
	 */
	private List<SimpleDateFormat> findPoolList(){
		// Locale毎のMapからformat毎のmapをとりだす。
		Map<String, List<SimpleDateFormat>> formatMap = localeInstanceMap.get(locale);
		if(formatMap==null){
			formatMap = Collections.synchronizedMap(new HashMap<String, List<SimpleDateFormat>>());
			localeInstanceMap.put(locale, formatMap);
		}

		// format毎のMapからインスタンスリストを取り出す。
		List<SimpleDateFormat> poolList = formatMap.get(format);
		if(poolList==null){
			poolList = Collections.synchronizedList(new LinkedList<SimpleDateFormat>());
		}
		
		return poolList;
	}

	/**
	 * プールへのSimpleDateFormatの返却。
	 * @param sdf
	 */
	private void releaseSdf(SimpleDateFormat sdf){
		List<SimpleDateFormat> poolList = findPoolList();
		// synchronizedListなので排他制御不要
		poolList.add(sdf);
	}

	/**
	 *
	 */
	public Object clone() {
		return new ThreadSafeSimpleDateFormat(format);
	}

	/* (非 Javadoc)
	 * @see java.text.SimpleDateFormat#equals(java.lang.Object)
	 */
	public boolean equals(Object arg0) {
		SimpleDateFormat sdf = getSdf();
		boolean result =  sdf.equals(arg0);
		releaseSdf(sdf);
		return result;
	}
	/* (非 Javadoc)
	 * @see java.text.SimpleDateFormat#format(java.util.Date, java.lang.StringBuffer, java.text.FieldPosition)
	 */
	public StringBuffer format(Date arg0, StringBuffer arg1, FieldPosition arg2) {
		SimpleDateFormat sdf = getSdf();
		StringBuffer result =  sdf.format(arg0, arg1, arg2);
		releaseSdf(sdf);
		return result;
	}
	/* (非 Javadoc)
	 * @see java.text.SimpleDateFormat#formatToCharacterIterator(java.lang.Object)
	 */
	public AttributedCharacterIterator formatToCharacterIterator(Object arg0) {
		SimpleDateFormat sdf = getSdf();
		AttributedCharacterIterator result =  sdf.formatToCharacterIterator(arg0);
		releaseSdf(sdf);
		return result;
	}
	/* (非 Javadoc)
	 * @see java.text.SimpleDateFormat#get2DigitYearStart()
	 */
	public Date get2DigitYearStart() {
		SimpleDateFormat sdf = getSdf();
		Date result =  sdf.get2DigitYearStart();
		releaseSdf(sdf);
		return result;
	}
	/* (非 Javadoc)
	 * @see java.text.DateFormat#getCalendar()
	 */
	public Calendar getCalendar() {
		SimpleDateFormat sdf = getSdf();
		Calendar result =  sdf.getCalendar();
		releaseSdf(sdf);
		return result;
	}
	/* (非 Javadoc)
	 * @see java.text.SimpleDateFormat#getDateFormatSymbols()
	 */
	public DateFormatSymbols getDateFormatSymbols() {
		SimpleDateFormat sdf = getSdf();
		DateFormatSymbols result =  sdf.getDateFormatSymbols();
		releaseSdf(sdf);
		return result;
	}
	/* (非 Javadoc)
	 * @see java.text.DateFormat#getNumberFormat()
	 */
	public NumberFormat getNumberFormat() {
		SimpleDateFormat sdf = getSdf();
		NumberFormat result =  sdf.getNumberFormat();
		releaseSdf(sdf);
		return result;
	}
	/* (非 Javadoc)
	 * @see java.text.DateFormat#getTimeZone()
	 */
	public TimeZone getTimeZone() {
		SimpleDateFormat sdf = getSdf();
		TimeZone result =  sdf.getTimeZone();
		releaseSdf(sdf);
		return result;
	}
	/* (非 Javadoc)
	 * @see java.text.SimpleDateFormat#hashCode()
	 */
	public int hashCode() {
		SimpleDateFormat sdf = getSdf();
		int result =  sdf.hashCode();
		releaseSdf(sdf);
		return result;
	}

	/* (非 Javadoc)
	 * @see java.text.DateFormat#parse(java.lang.String)
	 */
	public Date parse(String arg0) throws ParseException {
		SimpleDateFormat sdf = getSdf();
		Date result =  sdf.parse(arg0);
		releaseSdf(sdf);
		return result;
	}
	/* (非 Javadoc)
	 * @see java.text.SimpleDateFormat#parse(java.lang.String, java.text.ParsePosition)
	 */
	public Date parse(String arg0, ParsePosition arg1) {
		SimpleDateFormat sdf = getSdf();
		Date result =  sdf.parse(arg0, arg1);
		releaseSdf(sdf);
		return result;
	}
	/* (非 Javadoc)
	 * @see java.text.Format#parseObject(java.lang.String)
	 */
	public Object parseObject(String arg0) throws ParseException {
		SimpleDateFormat sdf = getSdf();
		Object result =  sdf.parseObject(arg0);
		releaseSdf(sdf);
		return result;
	}
	/* (非 Javadoc)
	 * @see java.text.DateFormat#parseObject(java.lang.String, java.text.ParsePosition)
	 */
	public Object parseObject(String arg0, ParsePosition arg1) {
		SimpleDateFormat sdf = getSdf();
		Object result =  sdf.parseObject(arg0, arg1);
		releaseSdf(sdf);
		return result;
	}
	/* (非 Javadoc)
	 * @see java.text.SimpleDateFormat#toLocalizedPattern()
	 */
	public String toLocalizedPattern() {
		SimpleDateFormat sdf = getSdf();
		String result =  sdf.toLocalizedPattern();
		releaseSdf(sdf);
		return result;
	}

	/* (非 Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		SimpleDateFormat sdf = getSdf();
		String result =  sdf.toString();
		releaseSdf(sdf);
		return "ThreadSafeSDF("+ format +")["+result+"]";
	}

	//
	// 状態設定系
	//

	/* (非 Javadoc)
	 * @see java.text.DateFormat#setLenient(boolean)
	 */
	public void setLenient(boolean isLenient) {
		this.isLenient = isLenient;
	}

	/* (非 Javadoc)
	 * @see java.text.DateFormat#isLenient()
	 */
	public boolean isLenient() {
		return isLenient;
	}

	/* (非 Javadoc)
	 * @see java.text.SimpleDateFormat#applyPattern(java.lang.String)
	 */
	public void applyPattern(String format) {
		this.format = format;
		throw new UnsupportedOperationException();
	}

	/**
	 * この日付フォーマットを記述するパターン文字列を返します。
	 * @see java.text.SimpleDateFormat#toPattern()
	 */
	public String toPattern() {
		return format;
//		SimpleDateFormat sdf = getSdf();
//		String result =  sdf.toPattern();
//		releaseSdf(sdf);
//		return result;
	}

	//
	// 非サポート
	//

	/* (非 Javadoc)
	 * @see java.text.SimpleDateFormat#applyLocalizedPattern(java.lang.String)
	 */
	public void applyLocalizedPattern(String arg0) {
		throw new UnsupportedOperationException();
	}
	/* (非 Javadoc)
	 * @see java.text.SimpleDateFormat#set2DigitYearStart(java.util.Date)
	 */
	public void set2DigitYearStart(Date arg0) {
		throw new UnsupportedOperationException();
	}
	/* (非 Javadoc)
	 * @see java.text.DateFormat#setCalendar(java.util.Calendar)
	 */
	public void setCalendar(Calendar arg0) {
		throw new UnsupportedOperationException();
	}

	/* (非 Javadoc)
	 * @see java.text.SimpleDateFormat#setDateFormatSymbols(java.text.DateFormatSymbols)
	 */
	public void setDateFormatSymbols(DateFormatSymbols arg0) {
		throw new UnsupportedOperationException();
	}
	/* (非 Javadoc)
	 * @see java.text.DateFormat#setNumberFormat(java.text.NumberFormat)
	 */
	public void setNumberFormat(NumberFormat arg0) {
		throw new UnsupportedOperationException();
	}
	/* (非 Javadoc)
	 * @see java.text.DateFormat#setTimeZone(java.util.TimeZone)
	 */
	public void setTimeZone(TimeZone arg0) {
		throw new UnsupportedOperationException();
	}

}
