/**
 *
 */
package com.lavans.lacoder2.lang;

/**
 * irisParamパラメータエラー用Exceptionクラス
 *
 * @author mitamura
 */
public class InvalidParameterException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 *
	 */
	public InvalidParameterException() {
	}

	/**
	 * @param message
	 */
	public InvalidParameterException(String message) {
		super(message);
	}

	/**
	 * @param cause
	 */
	public InvalidParameterException(Throwable cause) {
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public InvalidParameterException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * @param message
	 * @param cause
	 * @param enableSuppression
	 * @param writableStackTrace
	 */
	public InvalidParameterException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
