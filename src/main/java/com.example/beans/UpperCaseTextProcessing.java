package com.example.beans;

// http://buraktas.com/create-qualifiers-cdi-beans/ (Auto)

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class UpperCaseTextProcessing implements TextProcessing {
	@Override
	public String processText( String text ) {
		return text.toUpperCase();
	}
}