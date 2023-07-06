package backend.api.endpoint;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Objects;
import com.sun.net.httpserver.HttpExchange;

import backend.api.interfaces.Application;
import backend.api.interfaces.Parameter;


public class RequestData {
	
    public static final String INVALID = "none";
	
	private String params;
	private String action;
	private String library;
	private String module;
	private String id;
	private String token;
	
	
	
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

		if(pathParts.length < 6) {
			response.appendError("invalid_endpoint", "The URL must seems like /api/token/module/library/action");
			response.send(500);
			//throw new IllegalStateException("The URL must seems like /api/token/module/library/action");
			return;
		}
		
		
		if(!app.name().equals(pathParts[1])) {
			response.appendError("application_not_found", "The application " + pathParts[1] + " doesn't exists");
			response.send(400); // Bad request
			return;
		}
		
		module = pathParts[3];
		if(module == null || module.isEmpty()) {
			response.appendError("invalid_module", "The module is not specified");
			response.send(400); // Bad request
			return;
		}
		
		library = pathParts[4];
		if(library == null || library.isEmpty()) {
			response.appendError("invalid_library", "The library is not specified");
			response.send(400); // Bad request
			return;
		}
		
		action = pathParts[5];
		if(action == null || action.isEmpty()) {
			response.appendError("invalid_action", "The action is not specified");
			response.send(400); // Bad request
			return;
		}
		
		id = pathParts.length == 7 ? pathParts[6] : INVALID;
		
		
		token = pathParts[2];
		if(token == null || token.isEmpty()) {
			response.appendError("invalid_token", "The authentication token is not valid");
			response.send(401); // Unauthorized
			return;
		}
		

	}

	
	
	public String moduleName() { return module;}
	public String libraryName() { return library;}
	public String actionName() { return action;}
	public String params() { return params;}
	public String id() { return id;}
	public String token() { return token;}
	
	public List<Parameter<?>> getParameters(List<Parameter<?>> expectedParameters, ResponseData response) throws IOException {

		var json = params();
		System.out.println(json);
		if((json == null || json.isEmpty()) && expectedParameters.size() == 0) return expectedParameters;

		// Check if the JSON is valid
		if(json == null || json.isEmpty()) {
			response.appendError("parameters_expected", "The action need parameters");
			response.send(400);
			return null;
		}
		
		// Iterate all expected
		for(Parameter<?> parameter : expectedParameters) {
			var name = parameter.name();
			var nameJson = Application.extractKeyFromJson(json, name);
			
			System.out.println("nameJson: " + nameJson);
			System.out.println("name: " + name);
			
			if(nameJson == null) {
				response.appendError("parameter_missing", "The parameter \"" + parameter.name() + "\" is missing");
				response.send(400);
				return null;
			}

			
			
			var type = parameter.type();
			var typeJson = Application.getJsonTypeFromValue(nameJson);

			System.out.println("typeJson: " + typeJson );
			System.out.println("type: " + type);
			
			if(typeJson == null) {
				response.appendError("parameter_invalid", "The parameter \"" + parameter.name() + "\" is not from the type " + parameter.type());
				response.send(400);
				return null;
			}


			
		}
		
		
		return null;
	}
	
	public static boolean requireId(ResponseData response, String id){
		if(id == RequestData.INVALID) {
			response.appendError("id_missing", "The id of your request is missing");
			try {
				response.send(400);
			} catch(Exception e) {}
			return true;
		}
		return false;
	}

	
}
