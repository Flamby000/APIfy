package backend.api.endpoint;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

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
	
	
	public void send(int code) throws IOException {
		
		// Build JSON response 
		var sb = new StringBuilder();
		sb.append("{")
		  .append("\"success\":")
		  .append(success())
		  .append(",")
		  .append("\"errors\":[")
		  .append(errors.entrySet().stream()
		    .map(error -> String.format("{\"error\":\"%s\",\"description\":\"%s\"}",
		        error.getKey(), error.getValue()))
		    .collect(Collectors.joining(",")))
		  .append("]")
		  .append(",")
		  .append("\"result\":");

		if(result.size() == 0) sb.append("false");
		else {
		sb.append("{")
		  .append(result.stream()
		    .map(parameter -> String.format("\"%s\":\"%s\"", parameter.name(), parameter.stringifyValue()))
		    .collect(Collectors.joining(",")))
		  .append("}");
		}
		sb.append("}");
		  

		
		// Send response to client
		var response = sb.toString();
        exchange.sendResponseHeaders(code, response.getBytes().length);
        exchange.getResponseBody().write(response.getBytes());
        exchange.close();
        over = true;
	}
	

	
	public boolean isClosed() { return over; }
	
}
