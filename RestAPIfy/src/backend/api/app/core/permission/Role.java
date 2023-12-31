package backend.api.app.core.permission;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.json.JSONObject;

import backend.api.app.Action;
import backend.api.app.Application;
import backend.api.app.Method;
import backend.api.app.Parameter;
import backend.api.app.RequestData;
import backend.api.app.ResponseData;

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
	public void get(Application app, ResponseData res, Connection db, String id, String token){

		if(id == RequestData.INVALID) {
			res.addArray("roles", backend.api.app.utils.Role.roles(db, app).stream().map(role -> role.toMap()).collect(Collectors.toList()));

			res.send(200);
			return;
		} else {
			var role = backend.api.app.utils.Role.get(db, app, id);
			if(role == null) {
				res.send(404);
				return;
			}
			res.addMap("role", role.toMap());
			
			res.addArray("users", role.users().stream().map(user -> user.toMap()).collect(Collectors.toList()));
			


			try {
				var permissions =  backend.api.app.utils.Role.permissions(db, app, id).stream().map(permission -> permission.toMap(db, app, Integer.parseInt(id))).collect(Collectors.toList());
				res.addArray("permissions", permissions);
				// res.addArray("authorized_methods", backend.api.permission.Role.authorizedMethods(db, app, id, action.name()));
				

			} catch (SQLException e) {
				res.err("permissions_not_found", "Permissions of the role wasnt found :" + e.getMessage());
				res.send(500);
			}

			
			
			res.send(200);
			return;
		}
	}
	
	
	@Override
	public void post(Application app, ResponseData res, Connection db, List<Parameter<?>> params, String token) {
		var roleDescription = (String) Parameter.find(params, "role_description").value();
		var roleName = (String) Parameter.find(params, "role_name").value();
		
		// create the role
		try {
			var role = backend.api.app.utils.Role.create(db, app, roleDescription, roleName);
			res.addMap("role", role.toMap());
			res.send(201);
		} catch (Exception e) {
			res.appendError("error", e.getMessage());
			res.send(500);
		}
		return;
		
	};


	@Override
	public void patch(Application app, ResponseData res, Connection db, JSONObject patchFields, String id, String token) {
		
		if(RequestData.requireId(res, id)) return;
		for(var key : patchFields.keySet()) {
			var value = patchFields.get(key);
			try {
				backend.api.app.utils.Role.updateField(db, app, id, key, value.toString());
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
	public void delete(Application app, ResponseData res, Connection db, List<Parameter<?>> params, String id, String token){
		if(RequestData.requireId(res, id)) return;

		try {
			backend.api.app.utils.Role.delete(db, app, id);
			res.send(200);
		} catch (Exception e) {
			res.err("not_found", "Role " + id + "not found");
			res.send(404);
		}

		return;
	};
	


	
	
}
