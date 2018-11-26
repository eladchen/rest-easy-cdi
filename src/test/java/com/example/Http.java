package com.example;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class Http {
    private static final OkHttpClient client = new OkHttpClient();
    private static final ObjectMapper objectMapper = new ObjectMapper();

    static Function<BiConsumer<String, Request.Builder>, CompletableFuture<Response>> requestFactory( String baseUrl ) {
        return biConsumer -> {
            final Request.Builder builder = new Request.Builder().url( baseUrl );

            biConsumer.accept( baseUrl, builder );

            return CompletableFuture.supplyAsync( () -> {
                try {
                    return client.newCall( builder.build() ).execute();
                }
                catch ( IOException e ) {
                    throw new RuntimeException( e );
                }
            } );
        };
    }

    static Function<BiConsumer<String, Request.Builder>, Response> syncRequestFactory( String baseUrl ) {
        return biConsumer -> {
            try {
                return requestFactory( baseUrl ).apply( biConsumer ).get();
            }
            catch ( InterruptedException | ExecutionException e ) {
                return null;
            }
        };
    }

    static String responseBody( final Response response ) {
        try {
            final ResponseBody responseBody = response.body();
            final String body = responseBody != null ? responseBody.string() : "";

            response.close();

            return body;
        }
        catch ( IOException e ) {
            return null;
        }
    }

    static Map<String, Object> jsonAsMap( String json ) {
        try {
            return objectMapper.readValue( json, new TypeReference<HashMap<String,Object>>() {} );
        }
        catch ( Exception e ) {
            return null;
        }
    }

    static Map<String, Object> jsonAsMap( Response response ) {
        return jsonAsMap( responseBody( response ) );
    }
}
