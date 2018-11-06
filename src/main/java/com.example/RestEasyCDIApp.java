package com.example;

import com.example.resources.EchoResource;

import javax.ws.rs.core.Application;
import java.util.HashSet;
import java.util.Set;

public class RestEasyCDIApp extends Application {
	@Override
	public Set<Class<?>> getClasses() {
		final Set<Class<?>> classSet = new HashSet<>();

		classSet.add( EchoResource.class );

		return classSet;
	}
}
