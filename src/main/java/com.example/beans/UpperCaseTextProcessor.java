package com.example.beans;

import javax.inject.Qualifier;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({FIELD, METHOD, TYPE, PARAMETER})
@Qualifier
public @interface UpperCaseTextProcessor {}