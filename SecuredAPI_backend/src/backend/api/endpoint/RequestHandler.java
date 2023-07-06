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
		var requestData = new RequestData(exchange, app, response);
		if(response.isClosed()) return;
		

    	
        //if ("POST".equals(exchange.getRequestMethod())) {
			
          
            // Send the response
        response.send(200); // 200 OK
            
            
        //} else {
            // Handle non-POST requests with a 405 Method Not Allowed response
            //response.send(405); // 405 Method not allowed;
        //}
    }
    
    
    
	/**
	 * Avoid the CORS on client request
	 * @param exchange to allow CORS
	 */
	public static void allowCORS(HttpExchange exchange) {
        var headers = exchange.getResponseHeaders();
        headers.add("Access-Control-Allow-Origin", "*"); // Allow requests from any origin
        headers.add("Access-Control-Allow-Methods", "POST"); // Allow only POST requests
        headers.add("Access-Control-Allow-Headers", "Content-Type"); // Allow Content-Type header
	}
	
}