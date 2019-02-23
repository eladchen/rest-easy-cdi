package com.example.servlets.jaxrs;

import com.example.servlets.jaxrs.resources.ClickResource;
import com.example.servlets.jaxrs.resources.EchoResource;

import javax.ws.rs.core.Application;

import java.util.HashSet;
import java.util.Set;

public class JaxRsApp extends Application {
	@Override
	public Set<Class<?>> getClasses() {
		final Set<Class<?>> classSet = new HashSet<>();

		classSet.add( EchoResource.class );
		classSet.add( ClickResource.class );

		return classSet;
	}
}
