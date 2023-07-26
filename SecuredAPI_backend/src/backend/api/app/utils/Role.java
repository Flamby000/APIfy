package backend.api.permission;

import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import backend.api.interfaces.Action;
import backend.api.interfaces.Application;

public class Role {
	public static final String SUPERMAN_ROLE = "Superman";

	@SuppressWarnings("unused")
	private final Connection db;
	@SuppressWarnings("unused")
	private final Application app;

	private final String name;
	private final String description;
	private final Date creationDate;
	private final int id;


	
    public Role(Connection db, Application app, int id, String name, String description, Date creationDate) {
    	Objects.requireNonNull(db);
    	Objects.requireNonNull(app);
		this.db = db;
		this.app = app;
		this.name = name;
		this.description = description;
		this.creationDate = creationDate;
		this.id = id;
    }

	public Map<String, String> toMap() {
		var map = new HashMap<String, String>();
		map.put("role_name", name);
		map.put("role_description", description);
		map.put("creationDate", creationDate.toString());
		map.put("id", String.valueOf(id));
		return map;
	}

	public Map<String, Object> toMap(Connection db, Application app, String actionId) {
		//var map = new HashMap<String, String>();
		// map.put("role_name", name);
		// map.put("role_description", description);
		// map.put("creationDate", creationDate.toString());
		// map.put("id", String.valueOf(id));
		// map.put("authorized_methods", Role.authorizedMethods(db, app, id, actionId))
		
		return Map.of(
			"role_name", name,
			"role_description", description,
			"creationDate", creationDate.toString(),
			"id", String.valueOf(id),
			"authorized_methods", Role.authorizedMethods(db, app, id, actionId)
		);
		
	}
	public static Role create(Connection db, Application app, String description, String name) throws Exception {
		try {
			var statement = db.prepareStatement(String.format("INSERT INTO %srole (role_name, role_description) VALUES (?, ?);", app.prefix()));
			statement.setString(1, name);
			statement.setString(2, description);
			statement.executeUpdate();
			statement.close();
			
			// Get role ID from DB
			statement = db.prepareStatement(String.format("SELECT role_id FROM %srole WHERE role_name = ?;", app.prefix()));
			statement.setString(1, name);
			var result = statement.executeQuery();
			if(!result.next()) {
				result.close();
				statement.close();
				throw new Exception("The role wasn't created");
			}
			var role_id = result.getInt("role_id");


			return new Role(db, app, role_id, name, description, new Date(System.currentTimeMillis()));
		} catch(Exception e) {
			throw new Exception(e.getMessage());
		}
	}
	
	public static void updateField(Connection db, Application app, String id, String column, String value) throws Exception {
		try {
			var statement = db.prepareStatement(String.format("UPDATE %srole SET %s = ? WHERE role_id = ?;", app.prefix(), column));
			statement.setString(1, value);
			statement.setInt(2, Integer.parseInt(id));
			statement.executeUpdate();
			statement.close();
		} catch(Exception e) {
			throw new Exception(e.getMessage());
		}
	}
	
	// Delete
	public static void delete(Connection db, Application app, String id) throws Exception {
		try {
			var statement = db.prepareStatement(String.format("DELETE FROM %srole WHERE role_id = ?;", app.prefix()));
			statement.setInt(1, Integer.parseInt(id));
			statement.executeUpdate();
			statement.close();
		} catch(Exception e) {
			throw new Exception(e.getMessage());
		}
	}

	public List<User> users() {
		// All users with the role id
		try {
			var statement = db.prepareStatement(String.format("SELECT * FROM %suser NATURAL JOIN %suser_role WHERE role_id = ?;", app.prefix(), app.prefix()));
			statement.setInt(1, id);
			var result = statement.executeQuery();
			var users = new ArrayList<User>();
			while(result.next()) {
				var user = new backend.api.permission.User(db, app, result.getInt("user_id"), result.getString("username"), null, result.getString("email"), result.getString("first_name"),result.getString("last_name"), result.getString("phone"),result.getDate("user_created_at"));
				users.add(user);
			}
			result.close();
			statement.close();
			return users;
		} catch(Exception e) {
			e.printStackTrace();
			return List.of();
		}
	}

	public static Role get(Connection db, Application app, String id) {
		try {
			var statement = db.prepareStatement(String.format("SELECT * FROM %srole WHERE role_id = ?;", app.prefix()));
			statement.setInt(1, Integer.parseInt(id));
			var result = statement.executeQuery();
			if(!result.next()) {
				result.close();
				statement.close();
				return null;
			}
			var role = new Role(db, app, result.getInt("role_id"), result.getString("role_name"), result.getString("role_description"), result.getDate("role_created_at"));
			result.close();
			statement.close();
			return role;
		} catch(Exception e) {
			//e.printStackTrace();
			return null;
		}
	}

	public static List<Role> roles(Connection db, Application app) {
		try {
			var statement = db.prepareStatement(String.format("SELECT * FROM %srole;", app.prefix()));
			var result = statement.executeQuery();
			var roles = new ArrayList<Role>();
			while(result.next()) {
				var role = new Role(db, app, result.getInt("role_id"), result.getString("role_name"), result.getString("role_description"), result.getDate("role_created_at"));
				roles.add(role);
			}
			result.close();
			statement.close();
			return roles;
		} catch(Exception e) {
			e.printStackTrace();
			return List.of();
		}
	}

	
	public boolean canPerform(Action action) {
		return false;
	}
	
	
	public static List<String> authorizedMethods(Connection db, Application app, int roleId, String actionId) {
		// list of methods in table api_role_permission
		try {
			var statement = db.prepareStatement(String.format("SELECT method FROM %srole_permission WHERE role_id = ? AND action_id = ?;", app.prefix()));
			statement.setInt(1, roleId);
			statement.setString(2, actionId);
			var result = statement.executeQuery();
			var methods = new ArrayList<String>();
			while(result.next()) {
				methods.add(result.getString("method"));
			}
			result.close();
			statement.close();
			return methods;
		} catch (SQLException e) {
			e.printStackTrace();
			return List.of();
		}
	}
	


	
	public static List<backend.api.permission.Action> permissions(Connection db, Application app, String role_id) throws SQLException {
			var statement = db.prepareStatement(String.format(""
					+ "SELECT action_id, action_description, library_id, module_id "
					+ "FROM %saction_role NATURAL JOIN %saction NATURAL JOIN %slibrary "
					+ "WHERE role_id = ?"
					+ "ORDER BY CONCAT(module_id, library_id, action_id) ASC"
					+ ";", app.prefix(), app.prefix(), app.prefix()));
			statement.setString(1, role_id);
			var result = statement.executeQuery();
			var permissions = new ArrayList<backend.api.permission.Action>();
			while(result.next()) {
				var action_id = result.getString("action_id");
				var action_description = result.getString("action_description");
				var library_id = result.getString("library_id");
				var module_id = result.getString("module_id");
				var action = new backend.api.permission.Action(action_id, library_id, module_id, action_description);
				permissions.add(action);
			}
			return permissions;
	}

	
	
	public String name() { return name; }
	public String description() { return description; }
	public Date creationDate() { return creationDate; }
	public int id() { return id; }


}
