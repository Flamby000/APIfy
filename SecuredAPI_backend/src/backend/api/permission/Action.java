package backend.api.permission;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import backend.api.interfaces.Application;

public class Action {
	private final String action_id;
	private final String library_id;
	private final String module_id;
	
	private final String action_description;

	public Action(String action_id, String library_id, String module_id, String action_description) {
		this.action_id = action_id;
		this.library_id = library_id;
		this.module_id = module_id;
		this.action_description = action_description;
	}

	public String name() {
		return action_id;
	}

	public String description() {
		return action_description;
	}

	public String library() {
		return library_id;
	}

	public String module() {
		return module_id;
	}
	

	public Map<String, Object> toMap() {
		return Map.of(
			"action_id", action_id,
			"action_description", action_description,
			"library_id", library_id,
			"module_id", module_id
		);
	}
	
	public static List<Action> actions(Connection db, Application app) throws SQLException {
		var statement = db.prepareStatement(String.format("SELECT * FROM %saction NATURAL JOIN %slibrary;", app.prefix(), app.prefix()));
		var result = statement.executeQuery();
		var actions = new ArrayList<Action>();
		while(result.next()) {
			actions.add(new Action(
				result.getString("action_id"),
				result.getString("library_id"),
				result.getString("module_id"),
				result.getString("action_description")
			));
		}
		return actions;

	}
	




}
