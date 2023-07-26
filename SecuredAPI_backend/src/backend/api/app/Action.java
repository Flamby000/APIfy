package backend.api.interfaces;

import java.sql.Connection;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;

import backend.api.endpoint.ResponseData;

public interface Action {
	default String name() { return this.getClass().getSimpleName(); }
	default boolean isGuestAction() { return false; }
	
	default List<String> methods() { 
		return List.copyOf(methodsDoc().keySet());
	}
	
	Map<String, String> methodsDoc();

	
	String description();
	default List<Parameter<?>> parameters() {return List.of();}
	
	
	default List<String> patchableFields() {
		return List.of();
	}
	
	default List<Parameter<?>> deleteParameters() {
		return List.of();
	}

	
	
	
	default void post(Application app, ResponseData response, Connection db, List<Parameter<?>> params, String token) {
		response.err("action_doesnt_support_method", "The action " + name() + " dosn't support POST method.");
		response.send(400);
	};
	default void patch(Application app, ResponseData response, Connection db, JSONObject patchFields, String id, String token) {
		response.err("action_doesnt_support_method", "The action " + name() + " dosn't support PATCH method.");
		response.send(400);
	};
	default void get(Application app, ResponseData response, Connection db, String id, String token){
		response.err("action_doesnt_support_method", "The action " + name() + " dosn't support GET method.");
		response.send(400);
	};
	default void delete(Application app, ResponseData response, Connection db, List<Parameter<?>> deleteFields, String id, String token) {
		response.err("action_doesnt_support_method", "The action " + name() + " dosn't support DELETE method.");
		response.send(400);
	};
	default void put(Application app, ResponseData response, Connection db, List<Parameter<?>> params, String token){
		response.err("action_doesnt_support_method", "The action " + name() + " dosn't support PUT method.");
		response.send(400);
	};
	
	
}
