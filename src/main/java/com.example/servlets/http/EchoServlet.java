package com.example.servlets.http;

import com.example.beans.TextProcessing;
import com.example.beans.TextProcessor;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@RequestScoped
public class EchoServlet extends HttpServlet {
	@Inject
	BeanManager manager; // This is injected properly

	@Inject
	@TextProcessor
    TextProcessing upperCaseTextProcessing; // This is NOT injected properly

	protected void doGet( HttpServletRequest req, HttpServletResponse resp ) throws IOException {
		resp.setContentType( "text/plain" );

		resp.getWriter().append( upperCaseTextProcessing.processText( req.getParameter( "msg" ) ) );
	}
}
