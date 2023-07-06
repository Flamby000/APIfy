package backend.api.endpoint;

import java.io.IOException;
import java.util.Objects;

import com.sun.net.httpserver.HttpHandler;

import backend.api.interfaces.Application;

import com.sun.net.httpserver.HttpExchange;

public record RequestHandler(Application app) implements HttpHandler {

	public RequestHandler {
		Objects.requireNonNull(app);
	}

	/*
	 * Handle the client requests
	 */
	@Override
	public void handle(HttpExchange exchange) throws IOException {
		
		Objects.requireNonNull(exchange, "Exchange cannot be null");

		// Set CORS headers
		allowCORS(exchange);

		var response = new ResponseData(exchange);
		var request = new RequestData(exchange, app, response); // check the syntax and extract the data 
		if (response.isClosed()) return; // If the syntax is incorrect
		
		
		// handled errors with code 500 with try/catch
		
		
		// Check if module/library/action are on the database (error 400)
		// TODO
		
		
		// Check if module/library/action are implemented (Error 501)
		var module = app.getModule(request.moduleName());
		if(module == null) {
			response.appendError("module_not_implemented", "The module \"" + request.moduleName() + "\" is not implemented");
			response.send(501);
			return;
		}

		var library = module.getLibrary(request.libraryName());
		if(library == null) {
			response.appendError("library_not_implemented", "The library \"" + request.libraryName() + "\" is not implemented");
			response.send(501);
			return;
		}
		
		var action = library.getAction(request.actionName());
		if(action == null) {
			response.appendError("library_not_implemented", "The action \"" + request.actionName() + "\" is not implemented");
			response.send(501);
			return;
		}
		
		// Check the validity of the method
		if(!exchange.getRequestMethod().equals(action.method())) {
			response.appendError("method_not_allowed", "The method \"" + exchange.getRequestMethod() + "\" is not allowed for the action " + action.name() + ". Try the method " + action.method());
			response.send(405);
			return;
		}

		
		// Check action parameters on instance

		
		
		if(!action.isGuestAction()) {
		// Check permissions (rights on DB)
		// TODO
			
		}
		

		// Execute the action 
		try {
			action.execute(response, app, request.id());
		} catch(Exception e) {
			response.appendError("unhandled_error", "The method \"" + exchange.getRequestMethod() + "\" raise an error : " + e.getMessage());
			response.send(500);
		}
		
		if (response.isClosed()) return; // If the action succeed


		response.appendError("execution_failed", "The action \"" + request.actionName() + "\" failed its execution");
		response.send(501); // Implementation error
	}

	/**
	 * Avoid the CORS on client request
	 * 
	 * @param exchange to allow CORS
	 */
	public static void allowCORS(HttpExchange exchange) {
		var headers = exchange.getResponseHeaders();
		headers.add("Access-Control-Allow-Origin", "*"); // Allow requests from any origin
		headers.add("Access-Control-Allow-Methods", "POST"); // Allow only POST requests
		headers.add("Access-Control-Allow-Headers", "Content-Type"); // Allow Content-Type header
	}

}