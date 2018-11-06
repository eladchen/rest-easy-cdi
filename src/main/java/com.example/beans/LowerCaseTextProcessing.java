package com.example.beans;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;

@ApplicationScoped
@Named("lowerCase")
public class LowerCaseTextProcessing implements TextProcessing {
	public LowerCaseTextProcessing() {}

	@Override
	public String processText( String text ) {
		return text.toLowerCase();
	}
}
