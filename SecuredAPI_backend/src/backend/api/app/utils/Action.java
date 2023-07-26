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
	
	public List<String> methods(Application app) {
		var action = app
		.modules().stream()
			.filter(m -> m.name().equals(module_id))
			.findFirst()
			.orElse(null)
				.libraries().stream()
					.filter(l -> l.name().equals(library_id))
					.findFirst()
					.orElse(null)
						.actions().stream().filter(a -> a.name().equals(action_id))
							.findFirst()
							.orElse(null);
		return action.methods();
	}



	public Map<String, Object> toMap(Application app) {		
		return Map.of(
			"action_id", action_id,
			"action_description", action_description,
			"library_id", library_id,
			"module_id", module_id,
			"methods", methods(app)
		);
	}
	
	public Map<String, Object> toMap(Connection db, Application app, int roleId) {		
		return Map.of(
			"action_id", action_id,
			"action_description", action_description,
			"library_id", library_id,
			"module_id", module_id,
			"methods", methods(app),
			"authorized_methods", backend.api.permission.Role.authorizedMethods(db, app, roleId, this.name())

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
	

	public List<Role> roles(Connection db, Application app) throws SQLException {
		try {
			var statement = db.prepareStatement(String.format("SELECT * FROM %saction_role NATURAL JOIN %srole WHERE action_id = ?;", app.prefix(), app.prefix()));
			statement.setString(1, action_id);
			var result = statement.executeQuery();
			var roles = new ArrayList<Role>();
			while(result.next()) {
				roles.add(new Role(db, app,
					result.getInt("role_id"),
					result.getString("role_name"),
					result.getString("role_description"),
					result.getDate("role_created_at")
				));
			}
			return roles;
		} catch(SQLException e) {
			return List.of();
		}
	}

	public static List<SessionRequest> history(Connection db, Application app, String actionId) {

		// create SessionRequest list from %drequest
		try {
			var statement = db.prepareStatement(String.format("SELECT * FROM %srequest WHERE action_id = ?;", app.prefix()));
			statement.setString(1, actionId);
			var result = statement.executeQuery();
			var requests = new ArrayList<SessionRequest>();
			while(result.next()) {
				requests.add(new SessionRequest(
					db,
					app,
					result.getInt("request_id"),
					result.getString("session_id"),
					result.getString("action_id"),
					result.getInt("code"),
					result.getBoolean("success"),
					result.getString("method"),
					result.getString("in_parameters"),
					result.getString("out_parameters"),
					result.getDate("request_created_at")
				));
			}
			return requests;
		} catch(SQLException e) {
			e.printStackTrace();
			return List.of();
		}
		
	}

	public static Action action(Connection db, Application app, String id) {
		try {


			// Order by module, library, action ids
			var statement = db.prepareStatement(String.format("SELECT * FROM %saction NATURAL JOIN %slibrary  WHERE action_id = ? ORDER BY module_id, library_id, action_id;", app.prefix(), app.prefix()));
			statement.setString(1, id);
			var result = statement.executeQuery();
			if(result.next()) {
				return new Action(
					result.getString("action_id"),
					result.getString("library_id"),
					result.getString("module_id"),
					result.getString("action_description")
				);
			} else {
				
				return null;
			}
		} catch(SQLException e) {
			return null;
		}
	}



}
