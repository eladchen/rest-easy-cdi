package com.example.servlets.http;

import com.example.beans.TextProcessing;
import com.example.beans.LowerCaseTextProcessing;
import com.example.beans.UpperCaseTextProcessing;

import javax.inject.Inject;
import javax.enterprise.inject.spi.BeanManager;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;

import static com.example.servlets.Routes.ECHO_LOWERCASE;

public class EchoServlet extends HttpServlet {
	@Inject
	LowerCaseTextProcessing lowerCaseTextProcessing;

	@Inject
	UpperCaseTextProcessing upperCaseTextProcessing;

	@Inject
	BeanManager manager;

	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		TextProcessing textProcessing;
		resp.setContentType("text/plain");

		if (req.getServletPath().endsWith(ECHO_LOWERCASE)) {
			textProcessing = lowerCaseTextProcessing;
		}
		else {
			textProcessing = upperCaseTextProcessing;
		}

		resp.getWriter().append(textProcessing.processText(req.getParameter("msg")));
	}
}