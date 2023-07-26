package backend.api.app.core.permission;

import java.sql.Connection;
import java.util.List;
import java.util.Map;

import backend.api.app.Action;
import backend.api.app.Application;
import backend.api.app.Method;
import backend.api.app.Parameter;
import backend.api.app.ResponseData;

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
	public List<Parameter<?>> deleteParameters() {
		return List.of(
			new Parameter<>(Integer.class, "role_id", "The role to delete unassign", null, true),
			new Parameter<>(Integer.class, "user_id", "The user to remove the role", null, true)
		);
	}
	
	@Override
	public List<Parameter<?>> parameters() {
		return List.of(
			new Parameter<>(Integer.class, "role_id", "The user assigned role", null, true),
			new Parameter<>(Integer.class, "user_id", "The user to assign role", null, true)
		);
	}

	
	@Override
	public void post(Application app, ResponseData res, Connection db, List<Parameter<?>> params, String token) {
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
	public void delete(Application app, ResponseData res, Connection db, List<Parameter<?>> params, String id, String token){
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
