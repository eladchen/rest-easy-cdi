package com.example;

import io.undertow.servlet.api.ServletInfo;

import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.function.Consumer;

import static com.example.servlets.Routes.*;
import static io.undertow.util.URLUtils.normalizeSlashes;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TestServerServlets {
	private static UndertowServer.EphemeralHttpServer ephemeralHttpServer;

	@BeforeAll
	static void beforeTests() {
		ephemeralHttpServer = Main.configureUndertowServer(new UndertowServer.EphemeralHttpServer());

		ephemeralHttpServer.start();
	}

	@AfterAll
	static void afterTests() {
		ephemeralHttpServer.stop();
	}

	static String normalizeServletMapping(ServletInfo servletInfo) {
		final String mapping = servletInfo.getMappings().get(0);

		return normalizeSlashes(mapping.replaceAll("/+(\\**)$", "/"));
	}

	Request.Builder requestBuilder(String uri) {
		final int port = ephemeralHttpServer.getPort();
		final String host = ephemeralHttpServer.getHost();
		final String baseUrl = String.format("http://%s:%d", host, port);

		return new Request.Builder().url(baseUrl + uri);
	}

	String requestBody(String uri) {
		return Http.requestBody(requestBuilder(uri));
	}

	Map<String, Object> requestBodyAsMap(String uri, Consumer<Request.Builder> requestBuilderConsumer) {
		final Request.Builder requestBuilder = requestBuilder(uri);

		requestBuilderConsumer.accept(requestBuilder);

		return Http.jsonAsMap(Http.requestBody(requestBuilder));
	}

	@Test
	void jaxRsServletUppercase() {
		final String message = "test_str";

		assertEquals(message.toUpperCase(), requestBody(JAX_RS_SERVLET + JAX_RS_ECHO_RESOURCE + ECHO_UPPERCASE + "?msg=" + message));
	}

	@Test
	void jaxRsServletLowercase() {
		final String message = "TEST_STR";

		assertEquals(message.toLowerCase(), requestBody(JAX_RS_SERVLET + JAX_RS_ECHO_RESOURCE + ECHO_LOWERCASE + "?msg=" + message));
	}

	@Test
	void httpServletUppercase() {
		final String message = "test_str";

		assertEquals(message.toUpperCase(), requestBody(HTTP_SERVLET + ECHO_UPPERCASE + "?msg=" + message));
	}

	@Test
	void httpServletLowercase() {
		final String message = "TEST_STR";

		assertEquals(message.toLowerCase(), requestBody(HTTP_SERVLET + ECHO_LOWERCASE + "?msg=" + message));
	}

	@Test
	void testPersistence() {
		final String clickRoute = JAX_RS_SERVLET + JAX_RS_CLICK_RESOURCE + "/click/";

		final Map<String, Object> click = requestBodyAsMap(clickRoute, requestBuilder -> {
			requestBuilder.post(RequestBody.create(null, new byte[0]));
		});

		final Map<String, Object> updatedClick = requestBodyAsMap(clickRoute + click.get("id"), requestBuilder -> {
			final long count = (long) (int) click.get("count");
			final String body = String.format("{ \"%s\": %d }", "count", count + 1);

			requestBuilder.put(RequestBody.create(MediaType.parse("application/json"), body));
		});

		assertTrue((int) updatedClick.get("count") > (int) click.get("count"), "Click should be incremented by 1");
	}
}
