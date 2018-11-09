package com.example;

import com.example.beans.TextProcessing;
import com.example.beans.UpperCaseTextProcessor;
import com.example.beans.UpperCaseTextProcessing;
import com.example.servlets.jaxrs.resources.EchoResource;

import javax.ws.rs.core.Application;
import java.util.HashSet;
import java.util.Set;

public class RestEasyCDIApp extends Application {
	@Override
	public Set<Class<?>> getClasses() {
		final Set<Class<?>> classSet = new HashSet<>();

		classSet.add( EchoResource.class );
		classSet.add( UpperCaseTextProcessing.class );
		classSet.add( TextProcessing.class );
		classSet.add( UpperCaseTextProcessor.class );

		return classSet;
	}
}
