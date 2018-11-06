package com.example.beans;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;

@ApplicationScoped
@Named( "upperCase" )
public class UpperCaseTextProcessing implements TextProcessing {
	public UpperCaseTextProcessing() {}

	@Override
	public String processText( String text ) {
		return text.toUpperCase();
	}
}
