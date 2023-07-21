package backend.api.module.permission;

import java.sql.Connection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.json.JSONObject;

import backend.api.endpoint.RequestData;
import backend.api.endpoint.ResponseData;
import backend.api.interfaces.Action;
import backend.api.interfaces.Application;
import backend.api.interfaces.Method;
import backend.api.interfaces.Parameter;

public record User() implements Action {
	
	@Override
	public String description() {return "Manipulate user data";}
	
	
	@Override
	public List<Parameter<?>> parameters() {
		return List.of();
	}
	
	@Override
	public List<String> patchableFields() {
		return List.of("username", "email", "first_name", "last_name", "phone");
	}	

	@Override
	public Map<String, String> methodsDoc() {
		return Map.of(
			Method.GET, "Get the user data",
			Method.DELETE, "Delete the target user",
			Method.PATCH, "Update the user data"
		);
	}
	
	
	@Override
	public void get(Application app, ResponseData res, Connection db, String id){
		if(id == RequestData.INVALID) {
			res.addArray("users", backend.api.permission.User.users(db, app).stream().map(user -> user.toMap()).collect(Collectors.toList()));
			res.send(200);
			return;
			
		} else {
			/* Search "id" user */
			if(RequestData.requireId(res, id)) return;
			var user = backend.api.permission.User.user(db, app, Integer.valueOf(id));
			if(user != null) {
				res.addMap("user", user.toMap());
				res.addArray("roles", user.roles().stream().map(role -> role.toMap()).collect(Collectors.toList()));
				res.send(200);
				return;
			} else {
				res.appendError("not_found", "The user wasn't found");
				res.send(404);
				return;
			}
		}
		
	}
	
	
	@Override
	public void patch(Application app, ResponseData res, Connection db, JSONObject patchFields, String id) {
		if(RequestData.requireId(res, id)) return;
		for(var key : patchFields.keySet()) {
			var value = patchFields.get(key);
			try {
				backend.api.permission.User.updateField(db, app, id, key, value.toString());
			} catch (Exception e) {
				res.err("field_not_valid", "The field " + key + " cannot be updated ! " + e.getMessage());
				res.send(406);
				return;
			}
		}
		
		res.addString("message", "User updated");
		res.send(200);
	};

	@Override
	public void delete(Application app, ResponseData res, Connection db, List<Parameter<?>> params, String id){
		if(backend.api.permission.User.delete(db, app, id)) {
			res.addString("message", "User deleted");
			res.send(200);
			return;
		} else {
			res.appendError("not_found", "The user to delete wasn't found");
			res.send(404);
			return;
		}				
			
	};
	

}
