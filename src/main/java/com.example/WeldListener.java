package com.example;

import org.jboss.resteasy.cdi.ResteasyCdiExtension;
import org.jboss.weld.bootstrap.spi.BeanDiscoveryMode;
import org.jboss.weld.bootstrap.spi.EEModuleDescriptor;
import org.jboss.weld.bootstrap.spi.helpers.EEModuleDescriptorImpl;
import org.jboss.weld.configuration.spi.helpers.ExternalConfigurationBuilder;
import org.jboss.weld.environment.ContainerInstanceFactory;
import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.servlet.services.ServletResourceInjectionServices;
import org.reflections.Reflections;
import org.reflections.scanners.ResourcesScanner;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import static org.jboss.weld.config.ConfigurationKey.BEAN_IDENTIFIER_INDEX_OPTIMIZATION;
import static org.jboss.weld.environment.servlet.Listener.CONTAINER_ATTRIBUTE_NAME;

public class WeldListener implements ServletContextListener {
    private final Weld weld = new Weld();

    protected Set<Class<?>> getPackageClasses( String packageName ) {
        List<ClassLoader> classLoadersList = new LinkedList<>();
        classLoadersList.add( ClasspathHelper.contextClassLoader() );
        classLoadersList.add( ClasspathHelper.staticClassLoader() );

        Reflections reflections = new Reflections( new ConfigurationBuilder()
            .setScanners( new SubTypesScanner( false /* don't exclude Object.class */ ), new ResourcesScanner() )
            .setUrls( ClasspathHelper.forClassLoader( classLoadersList.toArray( new ClassLoader[ 0 ] ) ) )
            .filterInputsBy( new FilterBuilder().include( FilterBuilder.prefix( packageName ) ) ) );

        return reflections.getSubTypesOf( Object.class );
    }

    @Override
    public void contextInitialized( ServletContextEvent sce ) {
        // weld.addPackages( true, Main.class.getPackage() );
        // weld.scanClasspathEntries();
        weld.disableDiscovery();
        weld.addBeanClasses( getPackageClasses( "com.example" ).toArray( new Class<?>[]{} ) );
        weld.setBeanDiscoveryMode( BeanDiscoveryMode.ALL );
        weld.setClassLoader( Main.class.getClassLoader() );
        // weld.containerId( "my-custom-cdi" ); // This is not unique between runs (& not shutdown properly).
        // weld.addExtension( new ResteasyCdiExtension() );
        weld.addServices( new ExternalConfigurationBuilder().add( BEAN_IDENTIFIER_INDEX_OPTIMIZATION.get(), Boolean.FALSE.toString() ).build() );
        weld.addServices( new ServletResourceInjectionServices() {} );
        weld.addServices( new EEModuleDescriptorImpl( sce.getServletContext().getContextPath(), EEModuleDescriptor.ModuleType.WEB ) );

        sce.getServletContext().setAttribute( CONTAINER_ATTRIBUTE_NAME, weld );
    }
}
