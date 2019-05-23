package com.example.servlets.jaxrs.resources;

import com.example.beans.LowerCaseTextProcessing;
import com.example.beans.TextProcessing;
import com.example.beans.UpperCaseTextProcessing;
import com.example.servlets.Routes;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

@Path(Routes.JAX_RS_ECHO_RESOURCE)
@Produces(MediaType.TEXT_PLAIN)
@RequestScoped
public class EchoResource {
	@Inject
	LowerCaseTextProcessing lowerCaseTextProcessing;

	@Inject
	UpperCaseTextProcessing upperCaseTextProcessing;

	@GET
	@Path(Routes.ECHO_LOWERCASE)
	public CompletionStage<Response> lowercase(@BeanParam QueryParams queryParams) {
		return processText(lowerCaseTextProcessing, queryParams.message);
	}

	@GET
	@Path(Routes.ECHO_UPPERCASE)
	public CompletionStage<Response> uppercase(@BeanParam QueryParams queryParams) {
		return processText(upperCaseTextProcessing, queryParams.message);
	}

	CompletionStage<Response> processText(TextProcessing textProcessing, String text) {
		return CompletableFuture.supplyAsync(() -> {
			return Response.ok(textProcessing.processText(text)).build();
		});
	}

	// Using this POJO with "@BeanParam" form is vital
	// If "@QueryParam("msg") String message" form was used instead
	// We could not tell whether "@BeanParam" functionality is broken by the CDI
	public static class QueryParams {
		@QueryParam("msg")
		public String message;
	}
}
