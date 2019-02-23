package com.example;

import io.undertow.Undertow;
import io.undertow.servlet.api.ServletInfo;

import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

import static com.example.servlets.CommonRoutes.ECHO;
import static com.example.servlets.CommonRoutes.HTTP_SERVLET;
import static com.example.servlets.CommonRoutes.REST_EASY_SERVLET;
import static com.example.servlets.CommonRoutes.STATEFUL;

import static io.undertow.util.URLUtils.normalizeSlashes;

import static org.junit.jupiter.api.Assertions.assertEquals;

class Tests {
    private static final int port = 6789;
    private static final String host = "0.0.0.0";
    private static final String baseUrl = String.format( "http://%s:%d", host, port );
    private static final UndertowServer undertowServer = Main.server( new String[]{} );
    private static final Function<BiConsumer<String, Request.Builder>, Response> request = Http.syncRequestFactory( baseUrl );

    @BeforeAll
    static void beforeTests() {
        undertowServer.start( Undertow.builder().addHttpListener( port, host ) );
    }

    @AfterAll
    static void afterTests() {
        undertowServer.stop();
    }

    static String normalizeServletMapping( ServletInfo servletInfo ) {
        final String mapping = servletInfo.getMappings().get( 0 );

        return normalizeSlashes( mapping.replaceAll( "/+(\\**)$", "/" ) );
    }

    @Test
    void testCDI() {
        assertEquals( "httpServlet".toUpperCase(), Http.responseBody( request.apply( ( String baseUrl, Request.Builder builder ) -> {
            builder.url( baseUrl + HTTP_SERVLET + ECHO + "?msg=httpServlet" );
        } ) ) );

        assertEquals( "restEasyServlet".toUpperCase(), Http.responseBody( request.apply( ( String baseUrl, Request.Builder builder ) -> {
            builder.url( baseUrl + REST_EASY_SERVLET + ECHO + "?msg=restEasyServlet" );
        } ) ) );

        assertEquals( "restEasyServletAsync".toUpperCase(), Http.responseBody( request.apply( ( String baseUrl, Request.Builder builder ) -> {
            builder.url( baseUrl + REST_EASY_SERVLET + ECHO + "?msg=restEasyServletAsync" );
        } ) ) );
    }

    @Test
    void testPersistence() {
        final String clickRoute = REST_EASY_SERVLET + STATEFUL + "/click/";
        final Map<String, Object> createdClick = Http.jsonAsMap( request.apply( ( String serverBaseURL, Request.Builder builder ) -> {
            builder
                .url( serverBaseURL + clickRoute )
                .post( RequestBody.create( null, new byte[ 0 ] ) );
        } ) );
        final Map<String, Object> updatedClick = Http.jsonAsMap( request.apply( ( String serverBaseURL, Request.Builder builder ) -> {
            final long count = (long) (int) createdClick.get( "count" );
            final String body = String.format( "{ \"%s\": %d }", "count", count + 1 );

            builder
                .url( serverBaseURL + clickRoute + String.valueOf( createdClick.get( "id" ) ) )
                .put( RequestBody.create( MediaType.parse( "application/json" ), body ) );
        } ) );

        assertEquals( (int) createdClick.get( "count" ) + 1, (int) updatedClick.get( "count" ), "Click should be incremented by 1" );
    }
}
