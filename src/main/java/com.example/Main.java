package com.example;

import com.example.servlets.http.EchoServlet;
import com.example.servlets.jaxrs.RestEasyCDIApp;

import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.server.handlers.PathHandler;
import io.undertow.server.handlers.resource.ClassPathResourceManager;
import io.undertow.server.handlers.resource.ResourceManager;
import io.undertow.servlet.Servlets;
import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.api.DeploymentManager;
import io.undertow.servlet.api.InstanceFactory;
import io.undertow.servlet.api.InstanceHandle;
import io.undertow.servlet.api.ListenerInfo;
import io.undertow.servlet.api.ServletInfo;

import org.jboss.resteasy.cdi.CdiInjectorFactory;
import org.jboss.resteasy.cdi.ResteasyCdiExtension;
import org.jboss.resteasy.plugins.server.servlet.HttpServlet30Dispatcher;
import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;
import org.jboss.weld.environment.servlet.Listener;
import org.jboss.weld.environment.servlet.WeldServletLifecycle;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletException;

import java.util.stream.Stream;

public class Main {
    private static final int port = 8000;

    public static void main( String[] args ) throws ServletException {
        if ( Stream.of( args ).anyMatch( "jaxRs"::equals ) ) {
            undertowJaxRs( port ).start();
        }
        else {
            undertowSimpleServlet( port ).start();
        }

        // initializeWeld();
	}

    public WeldContainer initializeWeld() {
        final Weld weld = new org.jboss.weld.environment.se.Weld();

        Runtime.getRuntime().addShutdownHook( new Thread( weld::shutdown ) );

        return weld.initialize();
    }

    static Undertow undertowJaxRs( int port ) throws ServletException {
        final DeploymentInfo deploymentInfo = deploymentInfo();
        final ServletInfo servletInfo = Servlets.servlet( HttpServlet30Dispatcher.class );

        // Weld I **** you.
        // Weld weld = createBasicWeldContainer();
        // weld.addExtension( new ResteasyCdiExtension() );
        // addWeldListener( weld.initialize(), deploymentInfo );

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

        deploymentInfo.addListener( Servlets.listener( WeldListener.class ) );

        return undertow( "0.0.0.0", port, deploy( deploymentInfo, servletInfo ) );
    }

    static Undertow undertowSimpleServlet( int port ) throws ServletException {
        final DeploymentInfo deploymentInfo = deploymentInfo();
        final ServletInfo servletInfo = Servlets.servlet( EchoServlet.class ).addMapping( "/*" );
        final PathHandler pathHandler = Handlers.path( Handlers.redirect( "/" ) );

        addWeldListener( deploymentInfo );

        pathHandler.addPrefixPath( "/", deploy( deploymentInfo, servletInfo ) );

        return undertow( "0.0.0.0", port, pathHandler );
    }

    // *** Shared logic across setups ***
    static Weld createBasicWeldContainer() {
        return Welder.syntheticWeldContainer( "com.example" );
    }

    static DeploymentInfo addWeldListener( final WeldContainer weldContainer, final DeploymentInfo deploymentInfo ) {
        // io.undertow.servlet.api.ListenerInfo.ListenerInfo()
        // Use the explicit signature – programmatic: true (spelling error)
        final InstanceFactory<Listener> listenerFactory = () -> new InstanceHandle<Listener>() {
            @Override
            public Listener getInstance() {
                return new Listener() {
                    @Override
                    public void contextInitialized( ServletContextEvent sce ) {
                        final ServletContext servletContext = sce.getServletContext();

                        servletContext.setAttribute( Listener.CONTAINER_ATTRIBUTE_NAME, weldContainer );

                        servletContext.setAttribute( WeldServletLifecycle.BEAN_MANAGER_ATTRIBUTE_NAME, weldContainer.getBeanManager() );

                        super.contextInitialized( sce );
                    }
                };
            }

            @Override
            public void release() {
                weldContainer.shutdown();
            }
        };

        deploymentInfo.addListener( new ListenerInfo( Listener.class, listenerFactory, true ) );

        return deploymentInfo;
    }

    static DeploymentInfo addWeldListener( DeploymentInfo deploymentInfo ) {
        addWeldListener( createBasicWeldContainer().initialize(), deploymentInfo );

        return deploymentInfo;
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

        DeploymentManager deploymentManager = Servlets.defaultContainer().addDeployment( deploymentInfo );

        deploymentManager.deploy();

        return deploymentManager.start();
    }

    static Undertow undertow( String host, int port, HttpHandler httpHandler ) {
        return Undertow.builder().addHttpListener( port, host ).setHandler( httpHandler ).build();
    }
}
