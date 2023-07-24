package backend.api.module.permission;

import java.sql.Connection;
import java.util.List;
import java.util.Map;

import backend.api.endpoint.ResponseData;
import backend.api.interfaces.Action;
import backend.api.interfaces.Application;
import backend.api.interfaces.Method;
import backend.api.interfaces.Parameter;

public record ActionAssign() implements Action {
	public String description() {return "Manipulate role action assignation";}
	

	@Override
	public Map<String, String> methodsDoc() {
		return Map.of(
			Method.POST, "Assign the role to the user",
			Method.DELETE, "Unassigne the role of the user"
		);
	}
	
	@Override
	public List<String> deleteFields() {
		return List.of("role_id", "action_id");
	}
	
	@Override
	public List<Parameter<?>> parameters() {
		return List.of(
			new Parameter<>(Integer.class, "role_id", "The role assigned action", null, true),
			new Parameter<>(String.class, "action_id", "The action to assign", null, true)
		);
	}

    @Override
	public void post(Application app, ResponseData res, Connection db, List<Parameter<?>> params) {
        var roleId = (Integer) Parameter.find(params, "role_id").value();
        var actionId = (String) Parameter.find(params, "action_id").value();
        
        try {
            var statement = db.prepareStatement(String.format("INSERT INTO %saction_role (role_id, action_id) VALUES (?, ?);", app.prefix()));
            statement.setInt(1, roleId);
            statement.setString(2, actionId);
            statement.executeUpdate();
            res.send(200);
        } catch(Exception e) {
            res.err("assign_action_failed", e.getMessage());
            res.send(409);
        }
    }

    @Override
	public void delete(Application app, ResponseData res, Connection db, List<Parameter<?>> params, String id){
        var roleId = (Integer) Parameter.find(params, "role_id").value();
        var actionId = (String) Parameter.find(params, "action_id").value();
        
        try {
            var statement = db.prepareStatement(String.format("DELETE FROM %saction_role WHERE role_id = ? AND action_id = ?;", app.prefix()));
            statement.setInt(1, roleId);
            statement.setString(2, actionId);
            statement.executeUpdate();
            res.send(200);
        } catch(Exception e) {
            res.err("unassign_action_failed", e.getMessage());
            res.send(500);
        }
    }

}
