package backend.api.permission;

import java.sql.Connection;
import java.sql.Date;
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


	public String name() { return name; }
	public String description() { return description; }
	public Date creationDate() { return creationDate; }
	public int id() { return id; }
}
