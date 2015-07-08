package com.lavans.lacoder2.controller.impl;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lavans.lacoder2.controller.ExceptionHandler;
import com.lavans.lacoder2.controller.util.ErrorUtils;



public class DefaultExceptionHandler implements ExceptionHandler{
	/** logger */
	private static Logger logger = LoggerFactory.getLogger(ExceptionHandler.class);

	public DefaultExceptionHandler(){

	}

	public void handle(HttpServletRequest request, HttpServletResponse response, Throwable t) throws ServletException,
			IOException {
		logger.error(ErrorUtils.getRequestDetailString(request), t);
		if(response.isCommitted()) return;

		request.setAttribute("exception", t);
		request.getServletContext().getRequestDispatcher("/WEB-INF/jsp/error/error.jsp").forward(request, response);
	}
}
