package com.example.beans;

import javax.enterprise.context.Dependent;
import javax.inject.Named;

// http://buraktas.com/create-qualifiers-cdi-beans/ (Auto)

@Dependent
@Named( "upperCase" )
public class UpperCaseTextProcessing implements TextProcessing {
	@Override
	public String processText( String text ) {
		return text.toUpperCase();
	}
}