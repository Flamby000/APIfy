package backend.api.module.permission;

import java.sql.Connection;
import java.util.List;
import java.util.Map;

import backend.api.endpoint.ResponseData;
import backend.api.interfaces.Action;
import backend.api.interfaces.Application;
import backend.api.interfaces.Method;
import backend.api.interfaces.Parameter;

public record Authorize() implements Action {
	public String description() {return "Assign or not a method to a role";}
	

	@Override
	public Map<String, String> methodsDoc() {
		return Map.of(
			Method.POST, "Assign the method to the role",
			Method.DELETE, "Unassign the method of the role"
		);
	}
	
	@Override
	public List<Parameter<?>> deleteParameters() {
		return List.of(
			new Parameter<>(Integer.class, "role_id", "The role assigned action", null, true),
			new Parameter<>(String.class, "action_id", "The action to assign", null, true),
			new Parameter<>(String.class, "method", "The method to remove", null, true)
		);
	}
	
	@Override
	public List<Parameter<?>> parameters() {
		return List.of(
			new Parameter<>(Integer.class, "role_id", "The role assigned action", null, true),
			new Parameter<>(String.class, "action_id", "The action to assign", null, true),
			new Parameter<>(String.class, "method", "The method to add", null, true)

		);
	}
	
	@Override
	public void post(Application app, ResponseData res, Connection db, List<Parameter<?>> params) {
        var roleId = (Integer) Parameter.find(params, "role_id").value();
        var actionId = (String) Parameter.find(params, "action_id").value();
        var method = (String) Parameter.find(params, "method").value();

		try {
			var statement = db.prepareStatement(String.format("INSERT INTO %srole_permission (role_id, action_id, method) VALUES (?, ?, ?);", app.prefix()));
			statement.setInt(1, roleId);
			statement.setString(2, actionId);
			statement.setString(3, method);
			statement.executeUpdate();
			statement.close();
			res.send(201);
		} catch (Exception e) {
			res.err("add_method_failed", e.getMessage());
			res.send(500);
		}	
	}
	
	@Override
	public void delete(Application app, ResponseData res, Connection db, List<Parameter<?>> params, String id){
        var roleId = (Integer) Parameter.find(params, "role_id").value();
        var actionId = (String) Parameter.find(params, "action_id").value();
        var method = (String) Parameter.find(params, "method").value();

		// remove 
		try {
			var statement = db.prepareStatement(String.format("DELETE FROM %srole_permission WHERE role_id = ? AND action_id = ? AND method = ?;", app.prefix()));
			statement.setInt(1, roleId);
			statement.setString(2, actionId);
			statement.setString(3, method);
			statement.executeUpdate();
			statement.close();
			res.send(200);
		} catch (Exception e) {
			res.err("method_not_found", e.getMessage());
			res.send(404);
		}
	}

}
