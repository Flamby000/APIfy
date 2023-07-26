package backend.api.app;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import com.sun.net.httpserver.HttpExchange;
import org.json.JSONObject;


/**
 * RequestData class represents the data of a request.
 */
public class RequestData {
	
	/**
	 * The invalid id.
	 */
    public static final String INVALID = "none";
	
	/** 
	 * The parameters of the request as JSON string
	 */
	private String params;

	/**
	 * The action of the request.
	 */
	private String action;

	/**
	 * The library of the request.
	 */
	private String library;

	/**
	 * The module of the request.
	 */
	private String module;

	/**
	 * The id of the object of the request.
	 */
	private String id;

	/**
	 * The user token of the request.
	 */
	private String token;

	/**
	 * The patch fields of the request.
	 */
	private JSONObject patchFields = null;
	
	/**
	 * The constructor of the request data.
	 * @param exchange is the exchange of the request.
	 * @param app is the application.
	 * @param statement for the log 
	 * @param response is the response data for the client.
	 * @throws IOException
	 */
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

	
	/**
	 * Get the module of the request.
	 * @return the module of the request.
	 */
	public String moduleName() { return module;}

	/**
	 * Get the library of the request.
	 * @return the library of the request.
	 */
	public String libraryName() { return library;}

	/**
	 * Get the action of the request.
	 * @return the action of the request.
	 */
	public String actionName() { return action;}

	/**
	 * Get the parameters of the request.
	 * @return the parameters of the request.
	 */
	public String params() { return params;}

	/**
	 * Get the id of the object of the request.
	 * @return the id of the object of the request.
	 */
	public String id() { return id;}

	/**
	 * Get the token of the request.
	 * @return the token of the request.
	 */
	public String token() { return token;}
	
	/**
	 * Get the patch fields of the request.
	 * @return the patch fields of the request.
	 */
	public JSONObject patchFields() { return patchFields; }

	/**
	 * Get the parameters as a list of parameters.
	 * @param response is the response data for the client.
	 * @param method is the method of the request.
	 * @param action is the action of the request.
	 * @param expectedParameters are the expected parameters of the request.
	 * @return the parameters as a list of parameters.
	 * @throws IOException if an error occurs.
	 */
	@SuppressWarnings("unchecked")
	public List<Parameter<?>> getParameters(ResponseData response, String method, Action action, List<Parameter<?>> expectedParameters) throws IOException {
		var result = new ArrayList<Parameter<?>>();
		var json = params();
		if((json == null || json.isEmpty()) && expectedParameters.size() == 0) return expectedParameters;
		
		
		// Check if the JSON is valid
		
		if(json == null || json.isEmpty() ) {
			response.appendError("parameters_expected", "The action need parameters");
			response.send(412);
			return null;
		}
		JSONObject object;
		try {
			object = new JSONObject(json);
		} catch (Exception e) {
			response.appendError("invalid_json", "The provided JSON is invalid");
			response.send(412);
			return null;
		}
		
		
		// extract from object the object with key "patch_fields"
		if(method.equals(Method.PATCH)) {
			
			patchFields = object;
			var possiblesKeys = action.patchableFields();
			var notExpected = object.keySet().stream().filter((key) -> possiblesKeys.stream().noneMatch((possibleKey) -> possibleKey.equals(key))).collect(Collectors.toList());
			if(notExpected.size() > 0) {
				response.appendError("parameter_not_expected", "The parameter \"" + notExpected.get(0) + "\" is not expected");
				response.send(412);
				return null;
			}

		}
		
		
		
		var mustCount = expectedParameters.stream().filter((parameter) -> parameter.must()).count();
		var nonMustCount =  expectedParameters.stream().filter((parameter) -> !parameter.must()).count();
		var sentCount = object.keySet().stream().count();

		// check the number of needed parameters
		if(!method.equals(Method.PATCH)) {
			if(sentCount < mustCount || sentCount > mustCount+nonMustCount) {
				if(nonMustCount == 0) response.appendError("parameter_count_wrong", "The request expect " + mustCount + " parameters");
				else response.appendError("parameter_count_wrong", "The request expect between " + mustCount + " and " + (nonMustCount+mustCount) + " parameters");
				
				response.send(412);
				return null;
			}
		}


		// Iterate all expected
		if(!method.equals(Method.PATCH)) {
			for(var parameter : expectedParameters) {
	
				// Check parameter presence
				if(!object.has(parameter.name()) && parameter.must()) {
					if(parameter.value() != null ) {
						result.add(new Parameter<>(parameter.type(), parameter.name(), parameter.value()));
					} else {
						response.appendError("parameter_missing", "The parameter \"" + parameter.name() + "\" is missing");
						response.send(412);
						return null;
					}
				}
	
				// Check parameter type
				if(object.has(parameter.name()) && (object.isNull(parameter.name()))) {
					response.appendError("bad_parameter_name", "The parameter \"" + parameter.name() + "\" must be of type " + parameter.type());
					response.send(412);
					return null;
				}
	
				
				// default parameter into object
				if(!object.has(parameter.name())) {
					object.put(parameter.name(), parameter.value());

				}
				
				// Check the type of the argument
				try {
					var requestType = object.get(parameter.name()).getClass();

				
					if(requestType.equals(org.json.JSONArray.class)) requestType = List.class;
					if(requestType.equals(org.json.JSONObject.class)) requestType = Map.class;
					
					if(object.has(parameter.name()) && (!parameter.type().equals(requestType))) {
					
						response.appendError("bad_parameter_type", "The parameter \"" + parameter.name() + "\" is " +requestType.getCanonicalName()+ " and must be of type " + parameter.type().getCanonicalName());
						response.send(412);
						return null;
					}
				} catch (Exception e) {
					response.appendError("parameter_missing", "The parameter " + parameter.name() + " is missing");
					response.send(412);
					return null;
					
				}	
			}
		}
		

		//  send 400 if there is parameters not expected 
		//var notExpected = object.keySet().stream().filter((key) -> expectedParameters.stream().noneMatch((parameter) -> parameter.name().equals(key))).collect(Collectors.toList());
		if(!method.equals(Method.PATCH)) {

			var notExpected = object.keySet().stream().filter((key) -> expectedParameters.stream().noneMatch((parameter) -> parameter.name().equals(key))).collect(Collectors.toList());

			if(notExpected.size() > 0) {
				response.appendError("parameter_not_expected", "The parameter \"" + notExpected.get(0) + "\" is not expected");
				response.send(412);
				return null;
			}
			
			
			// Iterate object to set result for the action
			object.keySet().forEach((key) -> {
				var value = object.get(key);
				var parameter = expectedParameters.stream().filter((p) -> p.name().equals(key)).findFirst().get();
				result.add(new Parameter<>(parameter.type(), parameter.name(), value));
			});
		}

		return result;
	}
	

	/**
	 * Check if the id is valid.
	 * @param response is the response data for the client.
	 * @param id is the id to check.
	 * @return true if the id is valid, false otherwise.
	 */
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
