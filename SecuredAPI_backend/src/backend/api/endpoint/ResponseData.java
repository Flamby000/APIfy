package backend.api.endpoint;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import com.sun.net.httpserver.HttpExchange;

public class ResponseData {
	private final HttpExchange exchange;
	private String response;
	private boolean over;
	
	private final Map<String, String> errors = new HashMap<>();
	
	
	public ResponseData(HttpExchange exchange) {
		Objects.requireNonNull(exchange);
		this.exchange = exchange;
		over = false;
	}
	
	public void appendError(String error, String description) {
		errors.put(error, description);
	}
	
	public boolean success() {
		return errors.size() == 0;
	}
	
	
	public void send(int code) throws IOException {
		
		var sb = new StringBuilder();
		sb.append("{");
		sb.append("\"success\":");
		sb.append(success());
		sb.append(",");
		sb.append("\"errors\":[");
		var i = 0;
		for(var error : errors.entrySet()) {
			sb.append("{");
			sb.append("\"error\":\"");
			sb.append(error.getKey());
			sb.append("\",");
			sb.append("\"description\":\"");
			sb.append(error.getValue());
			sb.append("\"}");
			if(i < errors.size() - 1) sb.append(",");
			i++;
		}
		sb.append("]}");
		
		
		response = sb.toString();
        exchange.sendResponseHeaders(200, response.getBytes().length);
        exchange.getResponseBody().write(response.getBytes());
        exchange.close();
        over = true;
	}
	

	
	public boolean isClosed() { return over; }
	
}
