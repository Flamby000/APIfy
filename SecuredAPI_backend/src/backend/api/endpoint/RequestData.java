package backend.api.endpoint;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Objects;
import com.sun.net.httpserver.HttpExchange;


public class RequestData {
	
    private static final String INVALID = "none";
	
	String params;
	private final String action;
	private final String library;
	private final String module;
	private final String id;
	String token;
	
	
	public RequestData(HttpExchange exchange) throws IOException {
		Objects.requireNonNull(exchange, "Exchange cannot be null");
		
		// Get parameters
		var isr = new InputStreamReader(exchange.getRequestBody(), "UTF-8");
		var br = new BufferedReader(isr);
		var payloadBuilder = new StringBuilder();
		br.lines().forEach(payloadBuilder::append);
		br.close();
		isr.close();
		params = java.net.URLDecoder.decode(payloadBuilder.toString(), "UTF-8");
		System.out.println(params);
		
		// Get module/library/action/id values
		var path = exchange.getRequestURI().getPath();
		var pathParts = path.split("/");
		if(pathParts.length < 3) throw new IllegalStateException("The URL must be like /api/module/library/action");
		
		module = pathParts[2];
		Objects.requireNonNull(module, "Module cannot null");
		if(module.isEmpty()) throw new IllegalStateException("Module cannot be empty");

		library = pathParts[3];
		Objects.requireNonNull(library, "Library cannot null");
		if(library.isEmpty()) throw new IllegalStateException("Library cannot be empty");
		
		action = pathParts[4];
		Objects.requireNonNull(action, "Action cannot null");
		if(action.isEmpty()) throw new IllegalStateException("Action cannot be empty");
		
		id = pathParts.length == 4 ? pathParts[5] : INVALID;
	}
	
	
	

	
}
