package backend.api.endpoint;

import java.io.IOException;
import java.util.Objects;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;


public class RequestHandler implements HttpHandler {
	
	/*
	 * Handle the client requests
	 */
    @Override
    public void handle(HttpExchange exchange) throws IOException {
    	Objects.requireNonNull(exchange, "Exchange cannot be null");
    	
        // Set CORS headers
    	allowCORS(exchange);
    	
        if ("POST".equals(exchange.getRequestMethod())) {
        	
            // Read the request payload as JSON
			var requestData = new RequestData(exchange);
            
            // Send the response
            var response = "200 OK";
            
            exchange.sendResponseHeaders(200, response.getBytes().length);
            exchange.getResponseBody().write(response.getBytes());
            exchange.close();
            
            
        } else {
            // Handle non-POST requests with a 405 Method Not Allowed response
            var response = "Method Not Allowed";
            exchange.sendResponseHeaders(405, response.getBytes().length);
            exchange.getResponseBody().write(response.getBytes());
            exchange.close();
        }
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