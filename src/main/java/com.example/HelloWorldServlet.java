package com.example;

import com.example.beans.UpperCaseTextProcessing;

import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class HelloWorldServlet extends HttpServlet {
	@Inject
	BeanManager manager; // This is injected properly

	@Inject
	@Named("upperCase")
	UpperCaseTextProcessing upperCaseTextProcessing; // This is NOT injected properly

	protected void doGet( HttpServletRequest req, HttpServletResponse resp ) throws IOException {
		resp.setContentType( "text/plain" );

		resp.getWriter().append( upperCaseTextProcessing.processText( "hello" ) );
	}
}
