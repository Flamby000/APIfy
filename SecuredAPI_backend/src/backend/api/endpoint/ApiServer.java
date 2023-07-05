package backend.api.endpoint;

import java.io.IOException;
import java.net.InetSocketAddress;

import com.sun.net.httpserver.HttpServer;

public class ApiServer {
    public static final int PORT = 8080;

    public static void main(String[] args) throws IOException {
    	
    	/* Start the HTTP server */
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
        server.createContext("/", new RequestHandler());
        server.setExecutor(null);
        server.start();
        
        System.out.println("Server listening on port " + PORT);
    }
}