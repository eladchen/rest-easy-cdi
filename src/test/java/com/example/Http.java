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
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public class Http {
	private static final OkHttpClient client = new OkHttpClient();
	private static final ObjectMapper objectMapper = new ObjectMapper();

	static void request(Request.Builder requestBuilder, Consumer<Response> responseConsumer) {
		try (final Response response = client.newCall(requestBuilder.build()).execute()) {
			responseConsumer.accept(response);
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	static String requestBody(Request.Builder requestBuilder) {
		final AtomicReference<String> responseBody = new AtomicReference<>();

		request(requestBuilder, response -> {
			responseBody.set(Http.responseBody(response));
		});

		return responseBody.get();
	}

	static String responseBody(final Response response) {
		try {
			final ResponseBody responseBody = response.body();

			return responseBody != null ? responseBody.string() : "";
		} catch (IOException e) {
			return null;
		}
	}

	static Map<String, Object> jsonAsMap(String json) {
		try {
			return objectMapper.readValue(json, new TypeReference<HashMap<String, Object>>() {});
		} catch (Exception e) {
			return null;
		}
	}
}
