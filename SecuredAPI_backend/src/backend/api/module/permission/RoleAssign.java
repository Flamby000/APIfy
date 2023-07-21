package backend.api.module.permission;

import java.sql.Connection;
import java.util.List;
import java.util.Map;

import backend.api.endpoint.ResponseData;
import backend.api.interfaces.Action;
import backend.api.interfaces.Application;
import backend.api.interfaces.Method;
import backend.api.interfaces.Parameter;

public record RoleAssign() implements Action {

	@Override
	public String description() {return "Manipulate user role assignation";}
	

	@Override
	public Map<String, String> methodsDoc() {
		return Map.of(
			Method.POST, "Assign the role to the user",
			Method.DELETE, "Unassigne the role of the user"
		);
	}
	
	
	@Override
	public List<Parameter<?>> parameters() {
		return List.of(
			new Parameter<>(Integer.class, "role_id", "The user assigned role", null, true),
			new Parameter<>(Integer.class, "user_id", "The user to assign role", null, true)
		);
	}
	/*
	@Override
	public void get(Application app, ResponseData res, Connection db, String id){
		RequestData.requireId(res, id);
		res.addArray("users", backend.api.permission.Role.users(db, app, id).stream().map(user -> user.toMap()).collect(Collectors.toList()));
		res.send(200);
	}
	*/
	
	@Override
	public void post(Application app, ResponseData res, Connection db, List<Parameter<?>> params) {
		var userId = (Integer) Parameter.find(params, "user_id").value();
		var roleId = (Integer) Parameter.find(params, "role_id").value();
		
		try {
			var statement = db.prepareStatement(String.format("INSERT INTO %suser_role (user_id, role_id) VALUES (?, ?);", app.prefix()));
			statement.setInt(1, userId);
			statement.setInt(2, roleId);
			statement.executeUpdate();
			statement.close();
			res.addString("message", "Role assigned");
			res.send(201);
			return;
		} catch(Exception e) {
			res.appendError("role_already_assigned", String.format("The role %d is already assign to user %d", roleId, userId));
			res.send(409);
			return;
		}
		
	};
	

	@Override
	public void delete(Application app, ResponseData res, Connection db, List<Parameter<?>> params, String id){
		var userId = (Integer) Parameter.find(params, "user_id").value();
		var roleId = (Integer) Parameter.find(params, "role_id").value();
		
		try {
			var statement = db.prepareStatement(String.format("DELETE FROM %suser_role WHERE user_id = ? AND role_id = ?;", app.prefix()));
			statement.setInt(1, userId);
			statement.setInt(2, roleId);
			statement.executeUpdate();
			statement.close();
			res.addString("message", "Role unassigned");
			res.send(200);
			return;
		} catch(Exception e) {
			res.appendError("role_not_assigned", String.format("The role %d is not assign to user %d (%s)", roleId, userId));
			res.send(404);
			return;
		}
	};
	


}
