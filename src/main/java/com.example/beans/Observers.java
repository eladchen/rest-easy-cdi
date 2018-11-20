package com.example.beans;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jboss.weld.environment.se.beans.ParametersFactory;
import org.jboss.weld.environment.se.bindings.Parameters;
import org.jboss.weld.environment.se.events.ContainerInitialized;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.BeforeDestroyed;
import javax.enterprise.context.Destroyed;
import javax.enterprise.context.Initialized;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.event.Observes;
import javax.servlet.http.HttpServletRequest;

public class Observers {
    private static final Logger logger = LogManager.getLogger( Observers.class );

    public void applicationScope( @Observes @Initialized(ApplicationScoped.class) ContainerInitialized event, @Parameters ParametersFactory parameters ) {
        logger.debug( "Application parameters are '{}'", String.join( ", ", parameters.getArgs() ) );
    }

    // Request observing example:
    // @see org.jboss.weld.module.web.servlet.HttpContextLifecycle
    // ===========

    public void requestScopeInit( @Observes @Initialized(RequestScoped.class) HttpServletRequest httpServletRequest ) {
        logger.info( "HTTP Request to {} is initialized", httpServletRequest.getRequestURL().toString() );
    }

    public void requestScopeBeforeDestroyed( @Observes @BeforeDestroyed(RequestScoped.class) HttpServletRequest httpServletRequest ) {
        logger.info( "HTTP Request to {} is about to be destroyed", httpServletRequest.getRequestURL().toString() );
    }

    public void requestScopeDestroyed( @Observes @Destroyed(RequestScoped.class) HttpServletRequest httpServletRequest ) {
        logger.info( "HTTP Request to {} is destroyed", httpServletRequest.getRequestURL().toString() );
    }
}
