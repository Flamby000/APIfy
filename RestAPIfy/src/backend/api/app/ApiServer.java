package backend.api.app;

import java.io.BufferedReader;
import java.io.FileReader;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.Objects;

import org.json.JSONObject;

import com.sun.net.httpserver.HttpServer;

/**
 * ApiServer class represents the API server and the main of the application
 */
public class ApiServer {

	/**
	 * The port of the API server.
	 */
    public static final int PORT = 4080;

	/**
	 * The main of the application that start the API server.
	 * @param args are the arguments of the main.
	 */
    public static void start(List<Module> modules){
    	try {
    		Integer port = null;
    		try (BufferedReader reader = new BufferedReader(new FileReader(Application.CONFIG_FILE))) {
                var jsonString = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) jsonString.append(line);
                var json = new JSONObject(jsonString.toString());            
                port = json.getInt("api_port");
    		}
    		Objects.requireNonNull(port);
    		
    		
	    	/* Start the HTTP server */
	        var server = HttpServer.create(new InetSocketAddress(port), 0);
	        
	        var app = new Application();
	        modules.forEach((module) -> app.addModule(module));
	        
	        
	        server.createContext("/api", new RequestHandler(app));
	        //server.createContext("/setup", new RequestHandler());
	        server.setExecutor(null);
	        server.start();
	        
	        System.out.println("Server listening on port " + port);
    	} catch(Exception e) {
    		System.err.println(e);
    	}
    }
}