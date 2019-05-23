package com.example;

import org.jboss.weld.bootstrap.spi.BeanDiscoveryMode;
import org.jboss.weld.config.ConfigurationKey;
import org.jboss.weld.environment.se.Weld;
import org.reflections.Reflections;
import org.reflections.scanners.ResourcesScanner;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

class Welder {
	// TODO: Delete this
    static Set<Class<?>> getPackageClasses( String packageName ) {
        List<ClassLoader> classLoadersList = new LinkedList<>();
        classLoadersList.add( ClasspathHelper.contextClassLoader() );
        classLoadersList.add( ClasspathHelper.staticClassLoader() );

        Reflections reflections = new Reflections( new ConfigurationBuilder()
            .setScanners( new SubTypesScanner( false /* don't exclude Object.class */ ), new ResourcesScanner() )
            .setUrls( ClasspathHelper.forClassLoader( classLoadersList.toArray( new ClassLoader[ 0 ] ) ) )
            .filterInputsBy( new FilterBuilder().include( FilterBuilder.prefix( packageName ) ) ) );

        return reflections.getSubTypesOf( Object.class );
    }

	static Weld syntheticWeldContainer( Class<?>... packageClasses ) {
		final Weld weld = new Weld();

		// No sure what happens when both
		// "disableDiscovery" & "scanClasspathEntries" are on
		weld.scanClasspathEntries();
		weld.disableDiscovery();

		// Do not relay one this API - It doesn't coup well with common IDE
		// folders structures for generated classes.
		// The package name is scanned as a folder instead of a classpath.
		// This needs to be raised in jBoss tracker.
		// weld.addPackages( true, Main.class.getPackage() );

		weld.setBeanDiscoveryMode( BeanDiscoveryMode.ANNOTATED );
		weld.addPackage( true, Welder.class );

		weld.addProperty( ConfigurationKey.BEAN_IDENTIFIER_INDEX_OPTIMIZATION.get(), Boolean.FALSE.toString() );
		weld.addProperty( ConfigurationKey.CONCURRENT_DEPLOYMENT.get(), Boolean.FALSE.toString() );
		weld.addProperty( Weld.ARCHIVE_ISOLATION_SYSTEM_PROPERTY, true );

		return weld;
	}

	static Weld syntheticWeldContainer() {
    	return syntheticWeldContainer( Welder.class );
	}
}