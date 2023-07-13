package backend.api.endpoint;

/*
DATABASE :


 */

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.sun.net.httpserver.HttpExchange;
import org.json.JSONObject;

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
	
	
	
	public RequestData(HttpExchange exchange, Application app, PreparedStatement statement, ResponseData response) throws IOException {
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
		try {statement.setString(6, params());} catch(Exception e) {}

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
		try {statement.setString(2, action);} catch(Exception e) {}

		id = pathParts.length == 7 ? pathParts[6] : INVALID;
		// TODO : add ID in db ?
		
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
	
	@SuppressWarnings("unchecked")
	public List<Parameter<?>> getParameters(List<Parameter<?>> expectedParameters, ResponseData response) throws IOException {
		var result = new ArrayList<Parameter<?>>();

		var json = params();
		if((json == null || json.isEmpty()) && expectedParameters.size() == 0) return expectedParameters;
		
		// Check if the JSON is valid
		if(json == null || json.isEmpty()) {
			response.appendError("parameters_expected", "The action need parameters");
			response.send(412);
			return null;
		}
		
		var object = new JSONObject(json);
		
		var mustCount = expectedParameters.stream().filter((parameter) -> parameter.must()).count();
		var nonMustCount =  expectedParameters.stream().filter((parameter) -> !parameter.must()).count();
		var sentCount = object.keySet().stream().count();

		// check the number of needed parameters
		if(sentCount < mustCount || sentCount > mustCount+nonMustCount) {
			if(nonMustCount == 0) response.appendError("parameter_count_wrong", "The request expect " + mustCount + " parameters");
			else response.appendError("parameter_count_wrong", "The request expect between " + mustCount + " and " + (nonMustCount+mustCount) + " parameters");
			
			try { response.send(412); } catch (IOException e) {e.printStackTrace();}
			return null;
		}

		
		// Iterate all expected
		for(var parameter : expectedParameters) {

			// Check parameter presence
			if(!object.has(parameter.name()) && parameter.must()) {
				if(parameter.value() != null ) {
					result.add(new Parameter<>(parameter.type(), parameter.name(), parameter.value()));
				} else {
					response.appendError("parameter_missing", "The parameter \"" + parameter.name() + "\" is missing");
					try { response.send(412); } catch (IOException e) {e.printStackTrace();}
					return null;
				}
			}

			// Check parameter type
			if(object.has(parameter.name()) && (object.isNull(parameter.name()))) {
				response.appendError("bad_parameter_type", "The parameter \"" + parameter.name() + "\" must be of type " + parameter.type());
				try { response.send(412); } catch (IOException e) {e.printStackTrace();}
				return null;
			}
			
			
			// Check the type of the argument
			if(object.has(parameter.name()) && (!parameter.type().equals(object.get(parameter.name()).getClass()))) {
				
				response.appendError("bad_parameter_type", "The parameter \"" + parameter.name() + "\" is " +object.get(parameter.name()).getClass().getCanonicalName()+ " and must be of type " + parameter.type().getCanonicalName());
				try { response.send(412); } catch (IOException e) {e.printStackTrace();}
				return null;
			}

		}
		
		//  send 400 if there is parameters not expected 
		var notExpected = object.keySet().stream().filter((key) -> expectedParameters.stream().noneMatch((parameter) -> parameter.name().equals(key))).collect(Collectors.toList());
		if(notExpected.size() > 0) {
			response.appendError("parameter_not_expected", "The parameter \"" + notExpected.get(0) + "\" is not expected");
			try { response.send(412); } catch (IOException e) {e.printStackTrace();}
			return null;
		}
		
		
		// Iterate object to set result for the action
		object.keySet().forEach((key) -> {
			var value = object.get(key);
			var parameter = expectedParameters.stream().filter((p) -> p.name().equals(key)).findFirst().get();
			result.add(new Parameter<>(parameter.type(), parameter.name(), value));
		});
		
		return result;
	}
	
	public static boolean requireId(ResponseData response, String id){
		if(id == RequestData.INVALID) {
			response.appendError("id_missing", "The id of your request is missing");
			try {
				response.send(412);
			} catch(Exception e) {}
			return true;
		}
		return false;
	}

	
}
