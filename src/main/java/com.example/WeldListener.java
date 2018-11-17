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

    @Override
    public void contextInitialized( ServletContextEvent sce ) {
        final ServletContext servletContext = sce.getServletContext();

        Object weldContainer = servletContext.getAttribute( Listener.CONTAINER_ATTRIBUTE_NAME );

        if ( weldContainer == null ) {
            logger.warn( "WeldContainer is null." );
        }
        else if ( !( weldContainer instanceof ContainerInstance ) ) {
            logger.warn( "WeldContainer is not an instance of {}", ContainerInstance.class.getName() );
        }
        else {
            WeldContainer container = (WeldContainer)weldContainer;

            logger.debug( "Using WeldContainer with ID: {}", container.getId() );

            servletContext.setAttribute( Listener.CONTAINER_ATTRIBUTE_NAME, weldContainer );

            servletContext.setAttribute( WeldServletLifecycle.BEAN_MANAGER_ATTRIBUTE_NAME, ((WeldContainer)weldContainer).getBeanManager() );
        }

        super.contextInitialized( sce );
    }
}
