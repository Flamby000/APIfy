package backend.api.module.permission;

import java.io.IOException;
import java.sql.Connection;
import java.util.List;
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
	public List<String> methods() { return List.of(Method.GET, Method.DELETE, Method.POST);}
	
	
	public List<Parameter<?>> parameters() {
		return List.of(
			new Parameter<>(String.class, "role_description", "The description of the role to create", null, true),
			new Parameter<>(String.class, "role_name", "The name of the role to create", null, true)
		);
	}
	
	@Override
	public void execute(Application app, ResponseData res, List<Parameter<?>> params, Connection db, String id, String method,JSONObject patchFields) throws IOException {
		switch(method) {
			case Method.GET :
				if(id == RequestData.INVALID) {
					res.addArray("roles", backend.api.permission.Role.roles(db, app).stream().map(role -> role.toMap()).collect(Collectors.toList()));
					res.send(200);
					return;
				} else {
					// get role
					res.send(501);
					return;
				}
				
				
			case Method.DELETE : 
				if(RequestData.requireId(res, id)) return;
				// TODO : delete the role
				res.send(501);
				return;
				
				
			case Method.POST :
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
				
			default :
				res.send(500);
				return;
		}
		

	}
	
	
	
}
