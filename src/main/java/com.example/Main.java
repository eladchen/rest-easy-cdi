package com.example;

import com.example.servlets.http.EchoServlet;
import com.example.servlets.jaxrs.RestEasyCDIApp;
import io.undertow.server.handlers.resource.ClassPathResourceManager;
import io.undertow.server.handlers.resource.ResourceManager;
import io.undertow.servlet.Servlets;
import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.api.ServletInfo;
import org.jboss.resteasy.cdi.CdiInjectorFactory;
import org.jboss.resteasy.cdi.ResteasyCdiExtension;
import org.jboss.resteasy.plugins.server.servlet.HttpServlet30Dispatcher;
import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.beans.ParametersFactory;
import org.jboss.weld.environment.se.bindings.Parameters;

import javax.enterprise.inject.Produces;
import java.util.stream.Stream;

public class Main {
    public static final String SERVLET_MAPPING_PREFIX = "/";

    private static String[] PARAMETERS;

    public static void main( String[] args ) {
        PARAMETERS = args;

        app( args ).start();
    }

    static UndertowServer app( String[] args ) {
        final UndertowServer app = new UndertowServer();
        final Weld weld = weld();

        ServletInfo servletInfo;

        if ( Stream.of( args ).anyMatch( "jaxRs"::equals ) ) {
            weld.addExtension( new ResteasyCdiExtension() );

            servletInfo = undertowJaxRs();
        }
        else {
            servletInfo = undertowSimpleServlet();
        }

        app.addServlet( servletInfo.addMapping( SERVLET_MAPPING_PREFIX ) );
        app.setWeld( weld );
        app.setDeploymentInfo( deploymentInfo() );

        return app;
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

    static ServletInfo undertowJaxRs() {
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

        return servletInfo;
    }

    static ServletInfo undertowSimpleServlet() {
        return Servlets.servlet( EchoServlet.class );
    }

    static Weld weld() {
        return Welder.syntheticWeldContainer( "com.example" );
    }

    @Produces
    @Parameters
    static ParametersFactory parametersFactory() {
        final ParametersFactory parametersFactory = new ParametersFactory();

        parametersFactory.setArgs( PARAMETERS );

        return parametersFactory;
    }
}
