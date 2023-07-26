package backend.api.app.utils;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import backend.api.app.Application;


/**
 * Action class represents an action in the application.
 */
public class Action {

	/**
	 * Action id is the unique identifier of the action.
	 */
	private final String action_id;

	/**
	 * Library id is the unique identifier of the library that contains the action.
	 */
	private final String library_id;

	/**
	 * Module id is the unique identifier of the module that contains the action.
	 */
	private final String module_id;
	
	/**
	 * Action description is the description of the action.
	 */
	private final String action_description;


	/**
	 * Action constructor.
	 * @param action_id is the unique identifier of the action.
	 * @param library_id is the unique identifier of the library that contains the action.
	 * @param module_id is the unique identifier of the module that contains the action.
	 * @param action_description is the description of the action.
	 */
	public Action(String action_id, String library_id, String module_id, String action_description) {
		Objects.requireNonNull(action_id);
		Objects.requireNonNull(library_id);
		Objects.requireNonNull(module_id);
		Objects.requireNonNull(action_description);

		this.action_id = action_id;
		this.library_id = library_id;
		this.module_id = module_id;
		this.action_description = action_description;
	}

	/**
	 * Action id getter.
	 * @return the unique identifier of the action.
	 */
	public String name() {
		return action_id;
	}

	/**
	 * Action description getter.
	 * @return the description of the action.
	 */
	public String description() {
		return action_description;
	}

	/**
	 * Library id getter.
	 * @return the unique identifier of the library that contains the action.
	 */
	public String library() {
		return library_id;
	}

	/**
	 * Module id getter.
	 * @return the unique identifier of the module that contains the action.
	 */
	public String module() {
		return module_id;
	}
	
	/**
	 * Methods getter.
	 * @return the list of methods of the action.
	 */
	public List<String> methods(Application app) {
		Objects.requireNonNull(app);
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


	/**
	 * Convert the action to a map.
	 * @param app is the application.
	 * @return the map of the action.
	 */
	public Map<String, Object> toMap(Application app) {		
		Objects.requireNonNull(app);
		return Map.of(
			"action_id", action_id,
			"action_description", action_description,
			"library_id", library_id,
			"module_id", module_id,
			"methods", methods(app)
		);
	}
	
	/**
	 * Convert the action to a map with all the authorized methods of the role
	 * @param db is the database connection.
	 * @param app is the application.
	 * @param roleId is the role id to get the authorized methods.
	 * @return the action converted to a map with all the authorized methods of the role.
	 */
	public Map<String, Object> toMap(Connection db, Application app, int roleId) {		
		Objects.requireNonNull(db);
		Objects.requireNonNull(app);
		return Map.of(
			"action_id", action_id,
			"action_description", action_description,
			"library_id", library_id,
			"module_id", module_id,
			"methods", methods(app),
			"authorized_methods", backend.api.app.utils.Role.authorizedMethods(db, app, roleId, this.name())

		);
	}

	/**
	 * Get all the actions of the application.
	 * @param db is the database connection.
	 * @param app is the application.
	 * @return the list of actions of the application.
	 * @throws SQLException if the query fails.
	 */
	public static List<Action> actions(Connection db, Application app) throws SQLException {
		Objects.requireNonNull(db);
		Objects.requireNonNull(app);
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
	
	/**
	 * Get all the roles of the action.
	 * @param db is the database connection.
	 * @param app is the application.
	 * @return the list of roles of the action.
	 * @throws SQLException if the query fails.
	 */
	public List<Role> roles(Connection db, Application app) throws SQLException {
		Objects.requireNonNull(db);
		Objects.requireNonNull(app);
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

	/**
	 * Get all the requests of the action.
	 * @param db is the database connection.
	 * @param app is the application.
	 * @return the list of requests of the action.
	 * @throws SQLException if the query fails.
	 */
	public static List<SessionRequest> history(Connection db, Application app, String actionId) {
		Objects.requireNonNull(db);
		Objects.requireNonNull(app);
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

	/**
	 * Get the action with the given id.
	 * @param db is the database connection.
	 * @param app is the application.
	 * @param id is the action id.
	 * @return the action with the given id.
	 */
	public static Action action(Connection db, Application app, String id) {
		Objects.requireNonNull(db);
		Objects.requireNonNull(app);
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
