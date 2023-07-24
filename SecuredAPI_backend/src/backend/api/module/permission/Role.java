package backend.api.module.permission;

import java.sql.Connection;
import java.sql.SQLException;
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

public record Role() implements Action{

	@Override
	public String description() {return "Manipulate role data";}
	
	
	@Override
	public Map<String, String> methodsDoc() {
		return Map.of(
			Method.POST, "Create a new role",
			Method.GET, "Get the roles data",
			Method.DELETE, "Delete the specified role",
			Method.PATCH, "Modify a role"
		);
	}
	
	@Override
	public List<String> patchableFields() {
		return List.of("role_name", "role_description");
	}	
	
	public List<Parameter<?>> parameters() {
		return List.of(
			new Parameter<>(String.class, "role_description", "The description of the role to create", null, true),
			new Parameter<>(String.class, "role_name", "The name of the role to create", null, true)
		);
	}
	
	
	@Override
	public void get(Application app, ResponseData res, Connection db, String id){

		if(id == RequestData.INVALID) {
			res.addArray("roles", backend.api.permission.Role.roles(db, app).stream().map(role -> role.toMap()).collect(Collectors.toList()));
			res.send(200);
			return;
		} else {
			var role = backend.api.permission.Role.get(db, app, id);
			if(role == null) {
				res.send(404);
				return;
			}
			res.addMap("role", role.toMap());
			
			res.addArray("users", role.users().stream().map(user -> user.toMap()).collect(Collectors.toList()));
			
			
			try {
				var permissions =  backend.api.permission.Role.permissions(db, app, id);
				res.addArray("permissions", permissions.stream().map(permission -> permission.toMap()).collect(Collectors.toList()));
				
			} catch (SQLException e) {
				res.err("permissions_not_found", "Permissions of the role wasnt found :" + e.getMessage());
				res.send(500);
			}

			// Add permissions
			
			
			res.send(200);
			return;
		}
	}
	
	
	@Override
	public void post(Application app, ResponseData res, Connection db, List<Parameter<?>> params) {
		var roleDescription = (String) Parameter.find(params, "role_description").value();
		var roleName = (String) Parameter.find(params, "role_name").value();
		
		// create the role
		try {
			var role = backend.api.permission.Role.create(db, app, roleDescription, roleName);
			res.addMap("role", role.toMap());
			res.send(201);
		} catch (Exception e) {
			res.appendError("error", e.getMessage());
			res.send(500);
		}
		return;
		
	};


	@Override
	public void patch(Application app, ResponseData res, Connection db, JSONObject patchFields, String id) {
		
		if(RequestData.requireId(res, id)) return;
		for(var key : patchFields.keySet()) {
			var value = patchFields.get(key);
			try {
				backend.api.permission.Role.updateField(db, app, id, key, value.toString());
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
		if(RequestData.requireId(res, id)) return;

		try {
			backend.api.permission.Role.delete(db, app, id);
			res.send(200);
		} catch (Exception e) {
			res.err("not_found", "Role " + id + "not found");
			res.send(404);
		}

		return;
	};
	


	
	
}
