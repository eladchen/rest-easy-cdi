package com.example.servlets.jaxrs.resources;

import com.example.click.Click;
import com.example.click.ClickDao;
import com.example.servlets.Routes;

import javax.enterprise.context.RequestScoped;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

@RequestScoped
@Path(Routes.JAX_RS_CLICK_RESOURCE)
@Produces(MediaType.APPLICATION_JSON)
public class ClickResource {
    @Context
    HttpServletRequest servletRequest;

    @POST
    @Path( "/click" )
    public CompletionStage<Response> create() {
        final String xff = servletRequest.getHeader( "x-forwarded-for" );
        final String remoteAddr = servletRequest.getRemoteAddr();

        return CompletableFuture.supplyAsync( () -> {
            final ClickDao clickDao = clicksDao();
            final String ip = Optional.ofNullable( xff ).orElse( remoteAddr );
            final boolean exists = clickDao.findByIp( ip ) != null;
            final Click click = clickDao.persist( ip );

            Response.ResponseBuilder responseBuilder;

            if ( exists ) {
                responseBuilder = Response.ok();
            }
            else {
                try {
                    responseBuilder = Response.created( new URI( "/click/" + String.valueOf( click.getId() ) ) );
                }
                catch ( URISyntaxException e ) {
                    responseBuilder = Response.serverError();
                }
            }

            return responseBuilder.entity( click ).build();
        } );
    }

    @GET
    @Path( "/click/{id: \\d+}" )
    public CompletionStage<Response> read( @PathParam( "id" ) int id ) {
        return CompletableFuture.supplyAsync( () -> {
            final Click click = clicksDao().find( id );

            Response response;

            if ( click == null ) {
                response = Response.noContent().status( Response.Status.NOT_FOUND ).build();
            }
            else {
                response = Response.ok( click, MediaType.APPLICATION_JSON_TYPE ).build();
            }

            return response;
        } );
    }

    @PUT
    @Path( "/click/{id: \\d+}" )
    @Consumes(MediaType.APPLICATION_JSON)
    public CompletionStage<Response> update( @PathParam( "id" ) int id, Map<String, Integer> changes ) {
        return CompletableFuture.supplyAsync( () -> {
            // Error handling is for the weak.
            return Response.ok( clicksDao().update( id, changes.get( "count" ) ) ).build();
        } );
    }

    @DELETE
    @Path( "/click/{id : \\d+}" )
    public CompletionStage<Response> delete( @PathParam( "id" ) int id ) {
        return CompletableFuture.supplyAsync( () -> {
            clicksDao().remove( id );

            return Response.noContent().build();
        } );
    }

    @GET
    @Path( "/clicks" )
    public CompletionStage<Response> clicks() {
        return CompletableFuture.supplyAsync( () -> Response.ok().entity( clicksDao().findAll() ).build() );
    }

    private ClickDao clicksDao() {
        return new ClickDao();
    }
}