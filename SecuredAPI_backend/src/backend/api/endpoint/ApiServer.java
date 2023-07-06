package backend.api.endpoint;

import java.net.InetSocketAddress;

import com.sun.net.httpserver.HttpServer;

import backend.api.interfaces.Application;

public class ApiServer {
    public static final int PORT = 4080;

    public static void main(String[] args){
    	
    	try {
	    	/* Start the HTTP server */
	        var server = HttpServer.create(new InetSocketAddress(PORT), 0);
	        
	        server.createContext("/api", new RequestHandler(new Application()));
	        //server.createContext("/setup", new RequestHandler());
	        server.setExecutor(null);
	        server.start();
	        
	        System.out.println("Server listening on port " + PORT);
    	} catch(Exception e) {
    		System.err.println(e);
    	}
    }
}