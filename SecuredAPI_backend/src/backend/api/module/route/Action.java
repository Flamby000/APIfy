package backend.api.module.route;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

import backend.api.endpoint.ResponseData;
import backend.api.interfaces.Application;
import backend.api.interfaces.Method;

public record Action() implements backend.api.interfaces.Action {

	@Override
	public String description() {return "Handle action data";}

	
	@Override
	public Map<String, String> methodsDoc() {
		return Map.of(
			Method.GET, "Get the action data"
		);
	}
	
	public void get(Application app, ResponseData res, Connection db, String id) {
		try {
			res.addArray("actions", backend.api.permission.Action.actions(db, app).stream().map(action -> action.toMap()).toList());
			res.send(200);
		} catch (SQLException e) {
			res.err("get_actions_failed", e.getMessage());
			res.send(500);
		}
		
	}
	

	
}
