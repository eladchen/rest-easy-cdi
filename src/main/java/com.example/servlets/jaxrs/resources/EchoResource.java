package com.example.servlets.jaxrs.resources;

import com.example.beans.TextProcessing;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.spi.BeanManager;

import javax.inject.Inject;
import javax.inject.Named;

import javax.ws.rs.BeanParam;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import java.util.concurrent.CompletableFuture;

@Path("/")
@Produces(MediaType.TEXT_PLAIN)
@RequestScoped
public class EchoResource {
    @Inject
    @Named("upperCase")
    TextProcessing upperCaseTextProcessing;

    @Inject
    BeanManager manager;

    @GET
    public void echo( @Suspended AsyncResponse response, @BeanParam Aggregator queryParams ) {
        final Response.ResponseBuilder responseBuilder = Response.ok();

        if ( queryParams.async ) {
            CompletableFuture.runAsync( () -> {
                try {
                    Thread.sleep( 1500 );
                }
                catch ( InterruptedException e ) {
                    e.printStackTrace();
                }

                responseBuilder.entity( upperCaseTextProcessing.processText( queryParams.message ) );

                response.resume( responseBuilder.build() );
            } );
        }
        else {
            responseBuilder.entity( upperCaseTextProcessing.processText( queryParams.message ) );

            response.resume( responseBuilder.build() );
        }
    }

    public static class Aggregator {
        @QueryParam("async")
        public boolean async;

        @QueryParam("msg")
        public String message;

        @QueryParam("lower")
        @DefaultValue("false")
        public boolean lower;
    }
}
