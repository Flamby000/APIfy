package backend.api.app;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.util.List;
import java.util.Objects;

import com.sun.net.httpserver.HttpHandler;

import backend.api.app.core.setup.SetupDB;
import backend.api.app.utils.User;

import com.sun.net.httpserver.HttpExchange;

/**
 * RequestHandler class represents the handler of the client requests.
 */
public record RequestHandler(Application app) implements HttpHandler {


	/**
	 * The constructor of the request handler.
	 * @param app is the application.
	 */
	public RequestHandler {
		Objects.requireNonNull(app);
	}

	/**
	 * Handle the client request. It checks the validity of the request and execute the action.
	 * @param exchange is the exchange between the client and the server.
	 * @throws IOException if an I/O error occurs.
	 */
	@Override
	public void handle(HttpExchange exchange) throws IOException {

		// TODO : manage timeout (504 Gateway Timeout)

		Objects.requireNonNull(exchange, "Exchange cannot be null");


		allowCORS(exchange);



		

		var logDB = app.db();
		PreparedStatement logStatement = app.createLogStatement(logDB);

		var response = new ResponseData(exchange, app, logStatement, logDB);
		var request = new RequestData(exchange, app, logStatement, response); // check the syntax and extract the data
		if (response.isClosed()) return; 

		try {logStatement.setString(5, exchange.getRequestMethod());} catch (Exception e) {}

		
		if (!request.actionName().equals("SetupDB")) {

			app.registerConnection(logDB, request.token(), logStatement);

			// handled errors with code 500 with try/catch

			// Check if database is setup
			if (!SetupDB.isDBSetUp(logDB, app)) {
				response.appendError("db_not_setup", "The database is not set up");
				response.send(503);
				return;
			}

			// Check if module/library/action are on the database (error 400)
			// TODO

		}

		// Check if module/library/action are implemented (Error 501)
		var module = app.getModule(request.moduleName());
		if (module == null) {
			response.appendError("module_not_implemented", "The module \"" + request.moduleName() + "\" is not implemented");
			response.send(501);
			return;
		}

		var library = module.getLibrary(request.libraryName());
		if (library == null) {
			response.appendError("library_not_implemented", "The library \"" + request.libraryName() + "\" is not implemented");
			response.send(501);
			return;
		}

		var action = library.getAction(request.actionName());
		if (action == null) {
			response.appendError("action_not_implemented",
					"The action \"" + request.actionName() + "\" is not implemented");
			response.send(501);
			return;
		}

		// Check the validity of the method
		var requestMethod = exchange.getRequestMethod();
		if (!action.methods().contains(requestMethod)) {
			response.appendError("method_not_allowed", "The method \"" + exchange.getRequestMethod() + "\" is not allowed for the action " + action.name() + ". Try the method " + action.methods());
			response.send(405);
			return;
		}

		
		// Check action parameters on instance
		//var expectedParameters = action.parameters();
		List<Parameter<?>> params = List.of();
		//System.out.println(Method.needParameters(requestMethod) + "requestMethod" + requestMethod);

		if(!Method.needParameters(requestMethod)) {

			//if(expectedParameters.size() > 0) {
				//response.appendError("no_parameters_expected", String.format("The method %s expect no parameters", action.name()));
				//response.send(400);
				//return;
			//}
		} else {
			try {
				if(requestMethod.equals(Method.DELETE)) params = request.getParameters(response, requestMethod, action, action.deleteParameters());
				else params = request.getParameters(response, requestMethod, action, action.parameters());
			if (params == null) return;
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
		//System.out.println(params.stream().map((parameter) -> parameter.name() + " : " + parameter.type().getCanonicalName()).collect(Collectors.joining(", ")));

		User user = null;

		if (!request.actionName().equals("SetupDB")) {
			
			if (!action.isGuestAction()) {
				try {
					user = new User(logDB, app, request.token());
				} catch(Exception e) {
					response.appendError("guest_not_allowed", String.format("The action %s is not allowed for unauthenticated users  %s", action.name(), e.getMessage()));
					response.send(401);
					return;
				}
				
				// Check user permission
				var superMan = user.isSuperMan();
				var canPerform = user.canPerform(action);
				if(!canPerform && !superMan) {
					response.appendError("permission_denied", String.format("You don't have the permission to perform %s action", action.name()));
					response.send(403);
					return;
				}
		
				if(superMan && !canPerform) response.warn("superman_abuse", "You made this action because you are superman");
				
			}
		}



		// Execute the action

		try {
			var db = app.db();
			switch(requestMethod) {
				case Method.POST :
					action.post(app, response, db, params, request.token());
					break;
				case Method.GET :
					action.get(app, response, db, request.id(), request.token());
					break;
				case Method.PATCH :
					action.patch(app, response, db, request.patchFields(), request.id(), request.token());
					break;
				case Method.DELETE :
					action.delete(app, response, db, params, request.id(), request.token());
				default :
					response.appendError("method_not_supported", "The server doesn't support " + requestMethod + " method.");
					response.send(400);
			}
					
			//action.execute(app, response, params, db, request.id(), requestMethod, request.patchFields());
			app.close(db);
		} catch (Exception e) {
			e.printStackTrace();
			response.appendError("unhandled_error",
					"Your \"" + exchange.getRequestMethod() + "\" request raise an error : " + e.getMessage());
			response.send(500);
			return;
		}

		if (response.isClosed())
			return; // If the action succeed

		response.appendError("execution_failed", "The action \"" + request.actionName() + "\" failed its execution");
		response.send(501); // Implementation error
	}

	/**
	 * Avoid the CORS on client request
	 * 
	 * @param exchange to allow CORS
	 */
	public static void allowCORS(HttpExchange exchange) {
		// allow every requests
		exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
		exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "*");
		exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "*");

		// allow preflight
		if (exchange.getRequestMethod().equals("OPTIONS")) {
			exchange.getResponseHeaders().add("Access-Control-Max-Age", "1728000");
			exchange.getResponseHeaders().add("Content-Type", "text/plain charset=UTF-8");
			exchange.getResponseHeaders().add("Content-Length", "0");
			exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "*");
			try {
				exchange.sendResponseHeaders(204, -1);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}