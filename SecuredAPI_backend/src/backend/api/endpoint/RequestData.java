package backend.api.endpoint;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Objects;
import com.sun.net.httpserver.HttpExchange;

import backend.api.interfaces.Application;


public class RequestData {
	
    public static final String INVALID = "none";
	
	String params;
	private final String action;
	private final String library;
	private final String module;
	@SuppressWarnings("unused")
	private final String id;
	String token;
	
	
	
	public RequestData(HttpExchange exchange, Application app, ResponseData response) throws IOException {
		Objects.requireNonNull(exchange, "Exchange cannot be null");
		Objects.requireNonNull(response, "Response cannot be null");
		Objects.requireNonNull(app, "Application cannot be null");
		
		
		// Get parameters
		var isr = new InputStreamReader(exchange.getRequestBody(), "UTF-8");
		var br = new BufferedReader(isr);
		var payloadBuilder = new StringBuilder();
		br.lines().forEach(payloadBuilder::append);
		br.close();
		isr.close();
		params = java.net.URLDecoder.decode(payloadBuilder.toString(), "UTF-8");
		
		// Get module/library/action/id values
		var path = exchange.getRequestURI().getPath();
		var pathParts = path.split("/");

		if(pathParts.length < 6) throw new IllegalStateException("The URL must be like /api/token/module/library/action");
		if(!app.name().equals(pathParts[1])) {
			response.appendError("application_not_found", "The application " + pathParts[1] + " doesn't exists");
		}
		
		module = pathParts[3];
		if(module == null || module.isEmpty()) {
			response.appendError("invalid_module", "The module is not specified");
		}
		
		library = pathParts[4];
		if(library == null || library.isEmpty()) {
			response.appendError("invalid_library", "The library is not specified");
		}
		
		action = pathParts[5];
		if(action == null || action.isEmpty()) {
			response.appendError("invalid_action", "The action is not specified");
		}
		
		id = pathParts.length == 7 ? pathParts[6] : INVALID;
		
		if(!response.success()) {
			response.send(400); // Bad request
			return;
		}
		
		token = pathParts[2];
		if(token == null || token.isEmpty()) {
			response.appendError("invalid_token", "The authentication token is not valid");
			response.send(401); // Unauthorized
		}
	}

	
	
	public String moduleName() { return module;}
	public String libraryName() { return library;}
	public String actionName() { return action;}
	
	
	
	public static void requireId(String id) {
		if(id == RequestData.INVALID) throw new IllegalArgumentException("The action need a valid ID");
	}

	
}
