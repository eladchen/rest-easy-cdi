package com.example.servlets;

public final class Routes {
	// Shared
    public static final String ROOT_PATH = "/servlets";
	public static final String ECHO_UPPERCASE = "/uppercase";
	public static final String ECHO_LOWERCASE = "/lowercase";

	// HTTP Servlet
	public static final String HTTP_SERVLET = ROOT_PATH + "/http";

	// JAX-RS Servlet (Shared)
	public static final String JAX_RS_SERVLET = ROOT_PATH + "/jax-rs";
	public static final String ROOT_RESOURCE = "/examples";

	// JAX-RS click resource
	public static final String JAX_RS_CLICK_RESOURCE = ROOT_RESOURCE + "/click";

	// JAX-RS echo resource
	public static final String JAX_RS_ECHO_RESOURCE = ROOT_RESOURCE + "/echo";
}
