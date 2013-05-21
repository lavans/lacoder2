package com.lavans.lacoder2.security.auth;

/**
 * Iris認証失敗時の例外
 *
 * @author tnoda
 */
public class AuthenticationFailedException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public AuthenticationFailedException(String message) {
		super(message);
	}
}
