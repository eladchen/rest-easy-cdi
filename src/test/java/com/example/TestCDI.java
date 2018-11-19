package com.example;

import io.undertow.Undertow;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TestCDI {
	private static final OkHttpClient client = new OkHttpClient();
	private static final int port = 6789;
	private static final String host = "0.0.0.0";

	private UndertowServer undertowServer;

	private String getResponseBody( final Request request ) {
        try ( Response response = client.newCall( request ).execute() ) {
            final ResponseBody responseBody = response.body();

            return responseBody != null ? responseBody.string() : "";
        }
        catch ( Exception e ) {
            throw new RuntimeException( e );
        }
    }

	private UndertowServer assignAndStartApp( UndertowServer undertowServer ) {
		this.undertowServer = undertowServer;

		this.undertowServer.start( Undertow.builder().addHttpListener( port, host ) );

		return undertowServer;
	}

    private void assertMessageIsUpperCased( String message, Consumer<Request.Builder> beforeRequestBuild ) {
        final String url = String.format( "http://%s:%d/?msg=%s", host, port, message );
        final Request.Builder requestBuilder = new Request.Builder().url( url );

        if ( beforeRequestBuild != null ) {
            beforeRequestBuild.accept( requestBuilder );
        }

        assertEquals( message.toUpperCase(), getResponseBody( requestBuilder.build() ) );
    }

    private void assertMessageIsUpperCased( String message ) {
	    assertMessageIsUpperCased( message, null );
    }

	@AfterEach
	void stopServer() {
		if ( undertowServer != null ) {
			undertowServer.stop();
		}
	}

	@Test
	void testJaxRsCDI() throws Exception {
	    assignAndStartApp( Main.app( new String[]{ "jaxRs" } ) );

	    // Test synchronous response.
	    assertMessageIsUpperCased( "synchronousResponse" );

	    // Test asynchronous response.
        assertMessageIsUpperCased( "asynchronousResponse", builder -> {
            // Nasty trick
            builder.url( builder.build().url().toString() + "&async=true" );
        } );
	}

	@Test
	void testHttpServletCDI() throws Exception {
        assignAndStartApp( Main.app( new String[]{} ) );

		assertMessageIsUpperCased( "httpServlet" );
	}
}
