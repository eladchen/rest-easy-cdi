package com.example;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jboss.weld.environment.ContainerInstance;
import org.jboss.weld.environment.se.WeldContainer;
import org.jboss.weld.environment.servlet.Listener;
import org.jboss.weld.environment.servlet.WeldServletLifecycle;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

// This is just a bridge, until INSERT_ISSUE_URL_HERE is resolved.
public class WeldListener extends Listener implements ServletContextListener {
    private static final Logger logger = LogManager.getLogger( WeldListener.class );

    private ContainerInstance containerInstance;

    // No-Arg constructor to enable "newInstance" calls.
    public WeldListener() {}

    public WeldListener( WeldContainer containerInstance ) {
        this.containerInstance = containerInstance;
    }

    @Override
    public void contextInitialized( ServletContextEvent sce ) {
        final ServletContext servletContext = sce.getServletContext();

        Object weldContainer = oneOf( containerInstance, servletContext.getAttribute( Listener.CONTAINER_ATTRIBUTE_NAME ) );

        if ( weldContainer == null ) {
            logger.warn( "Couldn't find any weld container." );
        }
        else if ( !( weldContainer instanceof ContainerInstance ) ) {
            logger.warn( "WeldContainer is not an instance of {}", ContainerInstance.class.getName() );
        }
        else {
            WeldContainer container = (WeldContainer) weldContainer;

            logger.debug( "Using WeldContainer with ID: {}", container.getId() );

            // This is the actual fix.. without, we can't use a our own container.
            servletContext.setAttribute( WeldServletLifecycle.BEAN_MANAGER_ATTRIBUTE_NAME, container.getBeanManager() );

            containerInstance = container;
        }

        super.contextInitialized( sce );
    }

    @Override
    public void contextDestroyed( ServletContextEvent sce ) {
        if ( containerInstance != null ) {
            containerInstance.shutdown();
        }

        try {
            super.contextDestroyed( sce );
        }
        catch ( Exception ignore ) {}
    }

    private Object oneOf( Object... objects ) {
        Object object = null;

        for ( int i = 0; i < objects.length; i += 1 ) {
            object = objects[ i ];

            if ( object != null ) {
                break;
            }
        }

        return object;
    }
}
