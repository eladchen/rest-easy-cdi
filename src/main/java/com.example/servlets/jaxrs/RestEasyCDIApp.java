package com.example.servlets.jaxrs;

import com.example.servlets.jaxrs.resources.EchoResource;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import java.util.HashSet;
import java.util.Set;

@ApplicationPath( "/" )
public class RestEasyCDIApp extends Application {
	@Override
	public Set<Class<?>> getClasses() {
		final Set<Class<?>> classSet = new HashSet<>();

		classSet.add( EchoResource.class );

		return classSet;
	}
}
