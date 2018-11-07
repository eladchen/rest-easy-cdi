package com.example.beans;

import javax.enterprise.context.ApplicationScoped;

// http://buraktas.com/create-qualifiers-cdi-beans/ (Auto)

@ApplicationScoped
@TextProcessor
public class UpperCaseTextProcessing implements TextProcessing {
	public UpperCaseTextProcessing() {}

	@Override
	public String processText( String text ) {
		return text.toUpperCase();
	}
}