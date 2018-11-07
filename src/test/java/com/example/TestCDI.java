package com.example;

import io.undertow.Undertow;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TestCDI {
	private static final OkHttpClient client = new OkHttpClient();
	private static final int port = 6789;

	private Undertow server;

	private String requestMessage( String message ) throws Exception {
		final String url = String.format( "http://0.0.0.0:%d/?msg=%s", port, message );
		final Request request = new Request.Builder().url( url ).build();

		try ( Response response = client.newCall( request ).execute() ) {
			final ResponseBody responseBody = response.body();

			return responseBody != null ? responseBody.string() : "";
		}
	}

	private void assignAndStartServer( Undertow undertowServer ) {
		server = undertowServer;

		server.start();
	}

	private void assertMessageIsUpperCased( Undertow undertowServer, String message ) throws Exception {
		assignAndStartServer( undertowServer );

		assertEquals( message.toUpperCase(), requestMessage( message ) );
	}

	@AfterEach
	void stopServer() {
		if ( server != null ) {
			server.stop();
		}
	}

	@Test
	void testJaxRsCDI() throws Exception {
		assertMessageIsUpperCased( Main.undertowJaxRs( port ), "jaxServlet" );
	}

	@Test
	void testHttpServletCDI() throws Exception {
		assertMessageIsUpperCased( Main.undertowSimpleServlet( port ), "httpServlet" );
	}
}
