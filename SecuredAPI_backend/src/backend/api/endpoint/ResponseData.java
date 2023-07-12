package backend.api.endpoint;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.json.JSONObject;

import com.sun.net.httpserver.HttpExchange;

import backend.api.interfaces.Parameter;

public class ResponseData {
	private final HttpExchange exchange;
	private boolean over;
	
	private final Map<String, String> errors = new HashMap<>();
	private final List<Parameter<?>> result = new ArrayList<>();
	
	public ResponseData(HttpExchange exchange) {
		Objects.requireNonNull(exchange);
		this.exchange = exchange;
		over = false;
	}
	
	public void appendError(String error, String description) {
		errors.put(error, description);
	}
	
	public void appendResult(Parameter<?> parameter) {
		result.add(parameter);
	}
	
	public boolean success() {
		return errors.size() == 0;
	}
	
	public void addString(String name, String value) {
		appendResult(new Parameter<>(String.class, name, value));
	}
	
	public void addInt(String name, int value) {
		appendResult(new Parameter<>(Integer.class, name, value));
	}
	
	public void addBool(String name, boolean value) {
		appendResult(new Parameter<>(Boolean.class, name, value));
	}

	public void addArray(String name, List<?> value) {
		appendResult(new Parameter<>(List.class, name, value));
	}

	public void addMap(String name, Map<?, ?> value) {
		appendResult(new Parameter<>(Map.class, name, value));
	}
	
	
	public void err(String error, String description) {
		appendError(error, description);
	}
	
	public void send(int code) throws IOException {
		
		var res = new JSONObject();
		res.put("success", success());
		res.put("errors", errors.entrySet().stream()
		    .map(error -> new JSONObject()
		        .put("error", error.getKey())
		        .put("description", error.getValue()))
		    .collect(Collectors.toList()));

		res.put("result", result.stream()
			.map(parameter -> new JSONObject()
				.put("name", parameter.name())
				.put("value", parameter.stringifyValue()))
			.collect(Collectors.toList()));
		
		
		
		// Send response to client
		var response = res.toString();
        exchange.sendResponseHeaders(code, response.getBytes().length);
        exchange.getResponseBody().write(response.getBytes());
        exchange.close();
        over = true;
	}
	

	
	public boolean isClosed() { return over; }
	
}
