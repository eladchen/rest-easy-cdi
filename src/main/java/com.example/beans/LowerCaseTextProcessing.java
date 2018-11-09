package com.example.beans;

import javax.enterprise.context.Dependent;
import javax.inject.Named;

@Dependent
@Named( "lowerCase" )
public class LowerCaseTextProcessing implements TextProcessing {
	@Override
	public String processText( String text ) {
		return text.toLowerCase();
	}
}
