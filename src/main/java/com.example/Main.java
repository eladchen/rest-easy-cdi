package com.example;

import com.example.servlets.http.EchoServlet;
import com.example.servlets.jaxrs.RestEasyCDIApp;

import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.server.handlers.resource.ClassPathResourceManager;
import io.undertow.server.handlers.resource.ResourceManager;
import io.undertow.servlet.Servlets;
import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.api.DeploymentManager;
import io.undertow.servlet.api.ServletInfo;

import org.jboss.resteasy.cdi.CdiInjectorFactory;
import org.jboss.resteasy.plugins.server.servlet.HttpServlet30Dispatcher;
import org.jboss.weld.environment.servlet.Listener;

import javax.servlet.ServletException;

public class Main {
	private static final int port = 8000;

	public static void main( String[] args ) throws ServletException {
		//undertowSimpleServlet( port ).start();

		undertowJaxRs( port ).start();
	}

	static Undertow undertowJaxRs( int port ) throws ServletException {
		final DeploymentInfo deploymentInfo = deploymentInfo();
		final ServletInfo servletInfo = Servlets.servlet( HttpServlet30Dispatcher.class );

        // "resteasy.injector.factory" isn't needed with v3 container according the the user-guide (48.4)
        // https://docs.jboss.org/resteasy/docs/3.6.2.Final/userguide/html/CDI.html#d4e2794
        // *** This isn't right when using an embedded setup such as this one ***
        servletInfo.addInitParam( "resteasy.injector.factory", CdiInjectorFactory.class.getName() );

        // The rest of the jax-rs servlet setup..
        servletInfo.addInitParam( "javax.ws.rs.Application", RestEasyCDIApp.class.getName() );
        servletInfo.setLoadOnStartup( 1 );
        servletInfo.setAsyncSupported( true );
        servletInfo.setEnabled( true );
        servletInfo.addMapping( "/*" );

		// What is this used for?
		// deploymentInfo.addListener( Servlets.listener( org.jboss.resteasy.plugins.server.servlet.ResteasyBootstrap.class ) );

		return undertow( "0.0.0.0", port, deploy( deploymentInfo, servletInfo ) );
	}

	static Undertow undertowSimpleServlet( int port ) throws ServletException {
		ServletInfo servletInfo = Servlets.servlet( EchoServlet.class ).addMapping( "/*" );
		HttpHandler httpHandler = deploy( deploymentInfo(), servletInfo );

		httpHandler = Handlers.path( Handlers.redirect( "/" ) ).addPrefixPath( "/", httpHandler );

		return undertow( "0.0.0.0", port, httpHandler );
	}

	// *** Shared logic across setups ***
    static void addWeld( DeploymentInfo deploymentInfo, ServletInfo servletInfo ) {
        deploymentInfo.addListener( Servlets.listener( Listener.class ) );
    }

	static DeploymentInfo deploymentInfo() {
		final DeploymentInfo deploymentInfo = Servlets.deployment();
		final ClassLoader classLoader = Main.class.getClassLoader();
        final ResourceManager resourceManager = new ClassPathResourceManager( classLoader );

		deploymentInfo.setClassLoader( classLoader );
		deploymentInfo.setResourceManager( resourceManager );
		deploymentInfo.setDeploymentName( "cdi-example.war" );
		deploymentInfo.setSecurityDisabled( true );
		deploymentInfo.setContextPath( "/" );

		return deploymentInfo;
	}

	static HttpHandler deploy( DeploymentInfo deploymentInfo, ServletInfo servletInfo ) throws ServletException {
        deploymentInfo.addServlet( servletInfo );

	    addWeld( deploymentInfo, servletInfo );

		DeploymentManager deploymentManager = Servlets.defaultContainer().addDeployment( deploymentInfo );

		deploymentManager.deploy();

		return deploymentManager.start();
	}

	static Undertow undertow( String host, int port, HttpHandler httpHandler ) {
		return Undertow.builder().addHttpListener( port, host ).setHandler( httpHandler ).build();
	}
}
