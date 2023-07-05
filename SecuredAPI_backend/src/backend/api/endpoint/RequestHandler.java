package backend.api.endpoint;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;


public class RequestHandler implements HttpHandler {
	
        @Override
        public void handle(HttpExchange exchange) throws IOException {
        	
        	// Only for POST requests
            if ("POST".equals(exchange.getRequestMethod())) {
            	
                // Set CORS headers
                var headers = exchange.getResponseHeaders();
                headers.add("Access-Control-Allow-Origin", "*"); // Allow requests from any origin
                headers.add("Access-Control-Allow-Methods", "POST"); // Allow only POST requests
                headers.add("Access-Control-Allow-Headers", "Content-Type"); // Allow Content-Type header

                // Read the request payload as JSON
                InputStreamReader isr = new InputStreamReader(exchange.getRequestBody(), "UTF-8");
                BufferedReader br = new BufferedReader(isr);
                StringBuilder payloadBuilder = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    payloadBuilder.append(line);
                }
                br.close();
                isr.close();

                String payload = payloadBuilder.toString();
                System.out.println("Received JSON payload: " + payload);
                // Received JSON payload: module=Core&library=Setup&action=setup_db&params=%7B%22db_hostname%22%3A%22localhost%22%2C%22db_username%22%3A%22root%22%2C%22db_password%22%3A%22%22%2C%22db_name%22%3A%22test%22%7D

                // Send the response
                String response = "200 OK";
                
                exchange.sendResponseHeaders(200, response.getBytes().length);
                exchange.getResponseBody().write(response.getBytes());
                exchange.close();
                
                
            } else {
                // Handle non-POST requests with a 405 Method Not Allowed response
                String response = "Method Not Allowed";
                exchange.sendResponseHeaders(405, response.getBytes().length);
                exchange.getResponseBody().write(response.getBytes());
                exchange.close();
            }
        }
    }