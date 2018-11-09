package com.example.servlets.http;

import com.example.beans.TextProcessing;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@RequestScoped
public class EchoServlet extends HttpServlet {
    @Inject
    @Named( "upperCase" )
    TextProcessing upperCaseTextProcessing;

	protected void doGet( HttpServletRequest req, HttpServletResponse resp ) throws IOException {
		resp.setContentType( "text/plain" );

		resp.getWriter().append( upperCaseTextProcessing.processText( req.getParameter( "msg" ) ) );
	}
}
