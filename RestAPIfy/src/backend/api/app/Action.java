package backend.api.app;

import java.sql.Connection;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;


/**
 * Action interface represents an action.
 */
public interface Action {

	/**
	 * Action id getter.
	 * @return the action id.
	 */
	default String name() { return this.getClass().getSimpleName(); }

	/**
	 * Check if the action is a guest action. (an action that can be executed by a guest user)
	 * @return true if the action is a guest action, false otherwise.
	 */
	default boolean isGuestAction() { return false; }
	
	/**
	 * Get the methods supported by the action.
	 * @return the list of methods supported by the action.
	 */
	default List<String> methods() { 
		return List.copyOf(methodsDoc().keySet());
	}
	
	/**
	 * Get the methods documentation of the action.
	 * @return the methods documentation of the action.
	 */
	Map<String, String> methodsDoc();

	/**
	 * Get the description of the action.
	 * @return the description of the action.
	 */
	String description();

	/**
	 * Get the parameters for a POST and PUT requests.
	 * @return the parameters of the action for a POST request.
	 */
	default List<Parameter<?>> parameters() {return List.of();}
	
	/**
	 * Get the parameters for a PATCH request.
	 * @return the parameters of the action for a PATCH request.
	 */
	default List<String> patchableFields() {
		return List.of();
	}
	
	/**
	 * Get the parameters for a DELETE request.
	 * @return the parameters of the action for a DELETE request.
	 */
	default List<Parameter<?>> deleteParameters() {
		return List.of();
	}


	/**
	 * The action code executed when a POST request is received for the action.
	 * @param app is the application.
	 * @param response is the response data for the client
	 * @param db is the database connection.
	 * @param params are the parameters of the request.
	 * @param token is the token of the user.
	 */
	default void post(Application app, ResponseData response, Connection db, List<Parameter<?>> params, String token) {
		response.err("action_doesnt_support_method", "The action " + name() + " dosn't support POST method.");
		response.send(400);
	};

	/**
	 * The action code executed when a PATCH request is received for the action.
	 * @param app is the application.
	 * @param response is the response data for the client
	 * @param db is the database connection.
	 * @param patchFields are the fields to patch.
	 * @param id is the id of the object to patch. (equal RequestData.INVALID if the action is not a get by id action)
	 * @param token is the token of the user.
	 */
	default void patch(Application app, ResponseData response, Connection db, JSONObject patchFields, String id, String token) {
		response.err("action_doesnt_support_method", "The action " + name() + " dosn't support PATCH method.");
		response.send(400);
	};

	/**
	 * The action code executed when a GET request is received for the action.
	 * @param app is the application.
	 * @param response is the response data for the client
	 * @param db is the database connection.
	 * @param id is the id of the object to get. (equal RequestData.INVALID if the action is not a get by id action)
	 * @param token is the token of the user.
	 */
	default void get(Application app, ResponseData response, Connection db, String id, String token){
		response.err("action_doesnt_support_method", "The action " + name() + " dosn't support GET method.");
		response.send(400);
	};

	/**
	 * The action code executed when a DELETE request is received for the action.
	 * @param app is the application.
	 * @param response is the response data for the client
	 * @param db is the database connection.
	 * @param deleteFields are the fields to delete.
	 * @param id is the id of the object to delete. (equal RequestData.INVALID if the action is not a get by id action)
	 * @param token is the token of the user.
	 */
	default void delete(Application app, ResponseData response, Connection db, List<Parameter<?>> deleteFields, String id, String token) {
		response.err("action_doesnt_support_method", "The action " + name() + " dosn't support DELETE method.");
		response.send(400);
	};

	/**
	 * The action code executed when a PUT request is received for the action.
	 * @param app is the application.
	 * @param response is the response data for the client
	 * @param db is the database connection.
	 * @param params are the parameters of the request.
	 * @param token is the token of the user.
	 */
	default void put(Application app, ResponseData response, Connection db, List<Parameter<?>> params, String token){
		response.err("action_doesnt_support_method", "The action " + name() + " dosn't support PUT method.");
		response.send(400);
	};
}
