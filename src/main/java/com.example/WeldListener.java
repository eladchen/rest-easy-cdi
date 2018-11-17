package com.example;

import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.servlet.Listener;
import org.jboss.weld.environment.servlet.WeldServletLifecycle;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public class WeldListener extends Listener implements ServletContextListener {
    private Weld weld;

    @Override
    public void contextInitialized( ServletContextEvent sce ) {
        weld = Welder.syntheticWeldContainer( "com.example" );

        sce.getServletContext().setAttribute( Listener.CONTAINER_ATTRIBUTE_NAME, weld );

        sce.getServletContext().setAttribute( WeldServletLifecycle.BEAN_MANAGER_ATTRIBUTE_NAME, weld.initialize().getBeanManager() );

        super.contextInitialized( sce );
    }
}
