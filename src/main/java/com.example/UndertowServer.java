package com.example;

import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.server.handlers.PathHandler;
import io.undertow.servlet.Servlets;
import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.api.DeploymentManager;
import io.undertow.servlet.api.InstanceFactory;
import io.undertow.servlet.api.InstanceHandle;
import io.undertow.servlet.api.ListenerInfo;
import io.undertow.servlet.api.ServletInfo;
import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;

import javax.servlet.ServletContextListener;
import javax.servlet.ServletException;
import java.util.ArrayList;
import java.util.List;

public class UndertowServer {
    private DeploymentInfo deploymentInfo;
    private List<ServletInfo> servletInfoList;
    private Weld weld;
    private WeldContainer weldContainer;
    private Undertow undertow;

    public UndertowServer() {
        servletInfoList = new ArrayList<>();
        deploymentInfo = Servlets.deployment();
    }

    public void addServlet( ServletInfo servletInfo ) {
        servletInfoList.add( servletInfo );
    }

    public void setWeld( Weld weld ) {
        this.weld = weld;
    }

    public void setDeploymentInfo( DeploymentInfo deploymentInfo ) {
        this.deploymentInfo = deploymentInfo;
    }

    public void setWeldContainer( WeldContainer weldContainer ) {
        this.weldContainer = weldContainer;
    }

    public void start( Undertow.Builder undertowBuilder ) throws RuntimeException {
        try {
            if ( undertow == null ) {
                final PathHandler pathHandler = Handlers.path( Handlers.redirect( "/" ) );

                WeldContainer container = null;

                if ( weldContainer != null ) {
                    container = weldContainer;
                }
                else if ( weld != null ) {
                    container = weld.initialize();
                }

                if ( container != null ) {
                    weldContainer = container;

                    useWeldContainer( container, deploymentInfo );
                }

                for ( ServletInfo servletInfo : servletInfoList ) {
                    deploymentInfo.addServlet( servletInfo );
                }

                HttpHandler httpHandler = deploy( deploymentInfo );

                for ( ServletInfo servletInfo : servletInfoList ) {
                    for ( String mapping : servletInfo.getMappings() ) {
                        pathHandler.addPrefixPath( mapping, httpHandler );
                    }
                }

                undertow = undertowBuilder.setHandler( pathHandler ).build();

                undertow.start();

                Runtime.getRuntime().addShutdownHook( new Thread( this::stop ) );
            }
        }
        catch ( Exception e ) {
            throw new RuntimeException( e );
        }
    }

    public void start() throws RuntimeException {
        start( Undertow.builder().addHttpListener( 8000, "0.0.0.0" ) );
    }

    public void stop() {
        if ( undertow != null ) {
            if ( weldContainer != null ) {
                weld.shutdown();
            }

            undertow.stop();

            undertow = null;
            weldContainer = null;
        }
    }

    static HttpHandler deploy( DeploymentInfo deploymentInfo ) throws ServletException {
        DeploymentManager deploymentManager = Servlets.defaultContainer().addDeployment( deploymentInfo );

        deploymentManager.deploy();

        return deploymentManager.start();
    }

    static DeploymentInfo useWeldContainer( final WeldContainer weldContainer, final DeploymentInfo deploymentInfo ) {
        // Use the explicit signature – programmatic: true (spelling error)
        final ServletContextListener listener = new WeldListener( weldContainer );

        final InstanceFactory<ServletContextListener> listenerFactory = () -> new InstanceHandle<ServletContextListener>() {
            @Override
            public ServletContextListener getInstance() {
                return listener;
            }

            @Override
            public void release() {
                weldContainer.shutdown();
            }
        };

        deploymentInfo.addListener( new ListenerInfo( listener.getClass(), listenerFactory, true ) );

        return deploymentInfo;
    }
}