package com.example;

import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.server.handlers.PathHandler;
import io.undertow.server.handlers.resource.ClassPathResourceManager;
import io.undertow.servlet.Servlets;
import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.api.DeploymentManager;
import io.undertow.servlet.api.ServletInfo;
import org.jboss.resteasy.plugins.server.servlet.HttpServlet30Dispatcher;

import javax.servlet.ServletException;

public class Main {
	private static final int port = 8000;

	public static void main( String[] args ) throws ServletException {
		//undertowSimpleServlet( port ).start();
		undertowJaxRs( port ).start();
	}

	static Undertow undertowJaxRs( int port ) throws ServletException {
		final DeploymentInfo di = deploymentInfo( null );
		final ServletInfo servletInfo = Servlets.servlet( HttpServlet30Dispatcher.class );

		servletInfo.addInitParam( "javax.ws.rs.Application", RestEasyCDIApp.class.getName() );
		servletInfo.setLoadOnStartup( 1 );
		servletInfo.setAsyncSupported( true );
		servletInfo.setEnabled( true );
		servletInfo.addMapping( "/*" );

		// This isn't needed with v3 container according the the user-guide (48.4)
		// https://docs.jboss.org/resteasy/docs/3.6.2.Final/userguide/html/CDI.html#d4e2794
		// servletInfo.addInitParam( "resteasy.injector.factory", CdiInjectorFactory.class.getName() );
		//
		// What is this used for?
		// di.addListener( listener( org.jboss.resteasy.plugins.server.servlet.ResteasyBootstrap.class ) );

		return undertow( "0.0.0.0", port, deploy( di.addServlet( servletInfo ) ) );
	}

	static Undertow undertowSimpleServlet( int port ) throws ServletException {
		ServletInfo si = Servlets.servlet( HelloWorldServlet.class ).addMapping( "/*" );

		PathHandler path = Handlers.path( Handlers.redirect( "/" ) ).addPrefixPath( "/", deploy( deploymentInfo( si ) ) );

		return undertow( "0.0.0.0", port, path );
	}

	// *** Shared logic across setups ***
	static DeploymentInfo deploymentInfo( ServletInfo servletInfo ) {
		final DeploymentInfo deploymentInfo = Servlets.deployment();

		deploymentInfo.setClassLoader( Main.class.getClassLoader() );
		deploymentInfo.setResourceManager( new ClassPathResourceManager( Main.class.getClassLoader() ) );
		deploymentInfo.setDeploymentName( "cdi-example.war" );
		deploymentInfo.setSecurityDisabled( true );
		deploymentInfo.addListener( Servlets.listener( org.jboss.weld.environment.servlet.Listener.class ) );
		deploymentInfo.setContextPath( "/" );

		if ( servletInfo != null ) {
			deploymentInfo.addServlet( servletInfo );
		}

		return deploymentInfo;
	}

	static HttpHandler deploy( DeploymentInfo deploymentInfo ) throws ServletException {
		DeploymentManager deploymentManager = Servlets.defaultContainer().addDeployment( deploymentInfo );

		deploymentManager.deploy();

		return deploymentManager.start();
	}

	static Undertow undertow( String host, int port, HttpHandler httpHandler ) {
		return Undertow.builder().addHttpListener( port, host ).setHandler( httpHandler ).build();
	}
}
