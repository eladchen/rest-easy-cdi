package com.example;

import com.example.servlets.CommonRoutes;
import com.example.servlets.http.EchoServlet;
import com.example.servlets.jaxrs.JaxRsApp;

import io.undertow.server.handlers.resource.ClassPathResourceManager;
import io.undertow.server.handlers.resource.ResourceManager;
import io.undertow.servlet.Servlets;
import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.api.ServletInfo;

import liquibase.Contexts;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.hsqldb.jdbc.JDBCDriver;

import org.jboss.resteasy.cdi.CdiInjectorFactory;
import org.jboss.resteasy.cdi.ResteasyCdiExtension;
import org.jboss.resteasy.plugins.server.servlet.HttpServlet30Dispatcher;
import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.beans.ParametersFactory;
import org.jboss.weld.environment.se.bindings.Parameters;

import javax.enterprise.inject.Produces;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.example.servlets.CommonRoutes.REST_EASY_SERVLET;
import static io.undertow.util.URLUtils.normalizeSlashes;
import static org.jboss.resteasy.plugins.server.servlet.ResteasyContextParameters.RESTEASY_SERVLET_MAPPING_PREFIX;

public class Main {
    private static final Logger logger = LogManager.getLogger( Main.class );

    private static String[] PARAMETERS;

    public static void main( String[] args ) {
        server( args ).start();
    }

    static UndertowServer server( String[] args ) {
        final Set<String> arguments = Stream.of( args ).collect(Collectors.toSet());
        final UndertowServer undertowServer = new UndertowServer();
        final Weld weld = weld();
        final List<ServletInfo> servletsInfoList = new ArrayList<>();
        final ServletInfo restEasyServlet = restEasyServlet();
        final ServletInfo httpServlet = httpServlet();

        if ( !arguments.contains( restEasyServlet.getName() ) ) {
            servletsInfoList.add( restEasyServlet );

            weld.addExtension( new ResteasyCdiExtension() );
        }

        if ( !arguments.contains( httpServlet.getName() ) ) {
            servletsInfoList.add( httpServlet );
        }

        database();

        servletsInfoList.forEach( undertowServer::addServlet );

        undertowServer.setWeld( weld );
        undertowServer.setDeploymentInfo( deploymentInfo() );

        PARAMETERS = args;

        return undertowServer;
    }

    // Make this example more CDI based?
    // This could potentially ease the testing setup.
    private static void database() throws RuntimeException {
        // Create an in-memory database.
        try {
            Class.forName( JDBCDriver.class.getName() );

            try ( Connection connection = DriverManager.getConnection( "jdbc:hsqldb:mem:exampleDB", "sa", "" ) ) {
                final Database database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation( new JdbcConnection( connection ) );
                final Liquibase liquibase = new Liquibase( "liquibase/db-changelog.xml", new ClassLoaderResourceAccessor(), database );

                liquibase.update( new Contexts() );
            }
        }
        catch (Exception e) {
            logger.error( "Failed to configure the database {}", e );

            throw new RuntimeException( e );
        }
    }

    private static DeploymentInfo deploymentInfo() {
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

    private static ServletInfo restEasyServlet() {
        final ServletInfo servletInfo = Servlets.servlet( "restEasyServlet", HttpServlet30Dispatcher.class );

        // "resteasy.injector.factory" isn't needed with v3 container according the the user-guide (48.4)
        // https://docs.jboss.org/resteasy/docs/3.6.2.Final/userguide/html/CDI.html#d4e2794
        // *** This isn't right when using an embedded setup such as this one ***
        servletInfo.addInitParam( "resteasy.injector.factory", CdiInjectorFactory.class.getName() );
        servletInfo.addInitParam( "javax.ws.rs.Application", JaxRsApp.class.getName() );
        servletInfo.addInitParam( RESTEASY_SERVLET_MAPPING_PREFIX, REST_EASY_SERVLET );

        servletInfo.setLoadOnStartup( 1 );
        servletInfo.setAsyncSupported( true );
        servletInfo.setEnabled( true );
        servletInfo.addMapping( normalizeSlashes( REST_EASY_SERVLET ) + "/*" );
        servletInfo.setRequireWelcomeFileMapping( false );

        return servletInfo;
    }

    private static ServletInfo httpServlet() {
        final ServletInfo servletInfo = Servlets.servlet( "httpServlet", EchoServlet.class );

        servletInfo.setRequireWelcomeFileMapping( false );
        servletInfo.addMapping( "/servlets/http" + CommonRoutes.ECHO );

        return servletInfo;
    }

    private static Weld weld() {
        return Welder.syntheticWeldContainer( Main.class.getPackage().getName() );
    }

    @Produces
    @Parameters
    static ParametersFactory parametersFactory() {
        final ParametersFactory parametersFactory = new ParametersFactory();

        parametersFactory.setArgs( PARAMETERS );

        return parametersFactory;
    }
}
