package com.lavans.lacoder2.controller.impl;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.lavans.lacoder2.controller.ActionFilter;
import com.lavans.lacoder2.controller.ActionInfo;
import com.lavans.lacoder2.enums.RealDelay;
import com.lavans.lacoder2.security.auth.AuthenticationFailedException;
import com.lavans.lacoder2.security.auth.AuthenticationManager;


/**
 * Iris認証フィルタ<br>
 * いかなるリクエストも必ずこのフィルタを通して認証を実施する。
 *
 * @author tnoda
 */
public class DefaultAuthenticationFilter extends ActionFilter {

	/**
	 * 全URIを認証する
	 *
	 * @param uri. requestUri
	 * @return true: filter this uri. false: do not filter.
	 */
	public boolean isFilter(String uri){

		if (-1 < uri.indexOf("/manager")) return false;

		return true;
	}

	/**
	 * Iris認証を実施する。<br>
	 * hash=xxxからなるハッシュ値のマッチングを実施し、アンマッチの場合は例外を発行する。
	 *
	 * @return
	 */
	public void preAction(HttpServletRequest request, HttpServletResponse response, ActionInfo info) throws Exception {

		AuthenticationManager manager = AuthenticationManager.getInstance();

		String    hash = request.getParameter("hash");
		RealDelay type = RealDelay.getEnum(request.getParameter("type"));

		if (hash == null || hash.trim().length() == 0) {
			throw new AuthenticationFailedException("Parameter:[hash] is missing.");
		}

		if (type == null) {
			throw new AuthenticationFailedException("Parameter:[type] is missing.");
		}

		if (! manager.accept(hash, type)) {
			throw new AuthenticationFailedException("Parameter:[hash] is invalid:[" + hash + "]");
		}
	}
}
