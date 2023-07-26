package backend.api.app.core.setup;
import java.util.List;
import java.util.Map;

import backend.api.app.Action;
import backend.api.app.Application;
import backend.api.app.Method;
import backend.api.app.Parameter;
import backend.api.app.ResponseData;
import backend.api.app.utils.Role;

import java.sql.Connection;
import java.sql.SQLException;


public record SetupDB() implements Action {

	@Override
	public String description() {return "Set up the database for the installation";}
	
	@Override
	public Map<String, String> methodsDoc() {
		return Map.of(
			Method.POST, "Setup the database"
		);
	}
	
	
	
	@Override
	public List<Parameter<?>> parameters() {
		return List.of(
			new Parameter<>(String.class, "admin_password", "The password of the website administrator", null, true),
			new Parameter<>(String.class, "admin_mail", "The email of the website administrator", null, true),
			new Parameter<>(String.class, "admin_username", "The username of the website administrator", "admin", true),
			new Parameter<>(String.class, "admin_firstname", "The firstname of the website administrator", null, true),
			new Parameter<>(String.class, "admin_lastname", "The lastname of the website administrator", null, true)
		);
	}
	
	
	

	@Override
	public void post(Application app, ResponseData res, Connection db, List<Parameter<?>> params, String token) {
		
		//if(RequestData.requireId(response, id)) return;
		
		// Create tables 
		try {
			// Table module
			var statement = db.prepareStatement(String.format(
				"CREATE TABLE IF NOT EXISTS %smodule ("
				+ "module_id VARCHAR(255) PRIMARY KEY NOT NULL, "
				+ "module_description VARCHAR(255) NOT NULL, "
				+ "module_version VARCHAR(10), "
				+ "module_author VARCHAR(255), "
				+ "module_author_url VARCHAR(255), "
				+ "module_installed_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, "
				+ "module_updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP"
				+ ");", app.prefix())
			);
			statement.executeUpdate();
			statement.close();

			// Table library
			statement = db.prepareStatement(String.format(
					"CREATE TABLE IF NOT EXISTS %slibrary ("
				  + "library_id VARCHAR(255) NOT NULL, "
				  + "module_id VARCHAR(255) NOT NULL REFERENCES module(module_id) ON DELETE CASCADE, "
				  + "library_description VARCHAR(255) NOT NULL, "
				  + "library_version VARCHAR(10), "
				  + "library_author VARCHAR(255), "
				  + "library_author_url VARCHAR(255), "
				  + "library_installed_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, "
				  + "library_updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,"
				  + "PRIMARY KEY (module_id, library_id)"
				  + ");", app.prefix())
				  );
			statement.executeUpdate();
			statement.close();

			// table action
			statement = db.prepareStatement(String.format(
				"CREATE TABLE IF NOT EXISTS %saction ("
			  + "action_id VARCHAR(255) NOT NULL, "
			  + "library_id VARCHAR(255) NOT NULL REFERENCES library(library_id) ON DELETE CASCADE, "
			  + "action_description VARCHAR(255) NOT NULL, "
			  + "is_guest_action BOOLEAN NOT NULL, "
			  + "action_installed_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, "
			  + "action_updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,"
			  + "PRIMARY KEY (action_id, library_id)"
			  + ");", app.prefix())
			  );
			statement.executeUpdate();
			statement.close();

			
			// Table user
			statement = db.prepareStatement(String.format(
				"CREATE TABLE IF NOT EXISTS %suser ("
			  + "user_id INT(10) PRIMARY KEY NOT NULL AUTO_INCREMENT, "
			  + "username VARCHAR(255) UNIQUE, "
			  + "password VARCHAR(255) NOT NULL, "
			  + "email VARCHAR(255) NOT NULL UNIQUE, "
			  + "first_name VARCHAR(255), "
			  + "last_name VARCHAR(255), "
			  + "phone VARCHAR(255), "
			  + "user_created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP "
			  + ");", app.prefix())
			  );
			statement.executeUpdate();
			statement.close();

			
			// Insert admin user
			Integer adminId = null;
			try {
				statement = db.prepareStatement(String.format(
					"INSERT INTO %suser (email, password, first_name, last_name, username) "
				   +"values (?, ?, ?, ?, ?);", app.prefix())
				  );
				statement.setString(1, (String) Parameter.find(params, "admin_mail").value());
				statement.setString(2, (String) Parameter.find(params, "admin_password").value());
				statement.setString(3, (String) Parameter.find(params, "admin_firstname").value());
				statement.setString(4, (String) Parameter.find(params, "admin_lastname").value());
				statement.setString(5, (String) Parameter.find(params, "admin_username").value());
				
				statement.executeUpdate();
				statement.close();

				statement = db.prepareStatement(String.format("SELECT user_id FROM %suser WHERE email = ?;", app.prefix()));
				statement.setString(1, (String) Parameter.find(params, "admin_mail").value());
				var result = statement.executeQuery();
				result.next();
				adminId = result.getInt("user_id");
				result.close();
				statement.close();

			} catch(Exception e) {
				res.warn("db_installation_admin_failed", 
				String.format("The admin user %s creation failed : ", Parameter.find(params, "admin_mail").value()));
				res.warn("db_error", e.getMessage());
			}



			// Table role
			statement = db.prepareStatement(String.format(
				"CREATE TABLE IF NOT EXISTS %srole ("
			  + "role_id INT(10) PRIMARY KEY NOT NULL AUTO_INCREMENT, "
			  + "role_name VARCHAR(255) NOT NULL UNIQUE,"
			  + "role_description VARCHAR(255) NOT NULL, "
			  + "role_created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP "
			  + ");", app.prefix())
			  );
			statement.executeUpdate();
			statement.close();

			// user_role
			statement = db.prepareStatement(String.format(
				"CREATE TABLE IF NOT EXISTS %suser_role ("
			  + "user_id VARCHAR(255) NOT NULL REFERENCES user(user_id) ON DELETE CASCADE, "
			  + "role_id VARCHAR(255) NOT NULL REFERENCES role(role_id) ON DELETE CASCADE, "
			  + "PRIMARY KEY (user_id, role_id) "
			  + ");", app.prefix())
			  );
			statement.executeUpdate();
			statement.close();
		
			Integer roleId = null;
			try {
				statement.close();
	
				// insert Superman role
				statement = db.prepareStatement(String.format(
					"INSERT INTO %srole (role_name, role_description) VALUES ('%s', 'The superman role with all rights');", app.prefix(), Role.SUPERMAN_ROLE)
				  );
				statement.executeUpdate();
				statement.close();

				// get Superman role id
				statement = db.prepareStatement(String.format(
					"SELECT role_id FROM %srole WHERE role_name = '%s';", app.prefix(), Role.SUPERMAN_ROLE)
				  );
				var result = statement.executeQuery();
				result.next();
				roleId = result.getInt("role_id");
				result.close();
				statement.close();
				

			} catch(Exception e) {
				res.warn("db_superadmin_creation_failed", "The superman role creation failed");
			}
			
			// action_role
			statement = db.prepareStatement(String.format(
				"CREATE TABLE IF NOT EXISTS %saction_role ("
			  + "action_id VARCHAR(255) NOT NULL REFERENCES action(action_id) ON DELETE CASCADE, "
			  + "role_id VARCHAR(255) NOT NULL REFERENCES role(role_id) ON DELETE CASCADE, "
			  + "PRIMARY KEY (action_id, role_id) "
			  + ");", app.prefix())
			  );
			statement.executeUpdate();
			statement.close();
			

			if(roleId != null && adminId != null) {
				// Superman role for admin
				statement = db.prepareStatement(String.format(
					"INSERT INTO %suser_role (user_id, role_id) VALUES (?, ?);", app.prefix())
				  );
				statement.setInt(1, adminId);
				statement.setInt(2, roleId);
				statement.executeUpdate();
				statement.close();
			}


			// Table session
			statement = db.prepareStatement(String.format(
				"CREATE TABLE IF NOT EXISTS %ssession ("
			  + "session_id VARCHAR(255) PRIMARY KEY NOT NULL, "
			  + "user_id VARCHAR(255) REFERENCES user(user_id) ON DELETE CASCADE, "
			  + "session_created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP "
			  + ");", app.prefix())
			  );
			statement.executeUpdate();
			statement.close();

			// Table requests
			statement = db.prepareStatement(String.format(
				"CREATE TABLE IF NOT EXISTS %srequest ("
			  + "request_id INT(10) PRIMARY KEY NOT NULL AUTO_INCREMENT, "
			  + "session_id VARCHAR(255) DEFAULT NULL REFERENCES session(session_id) ON DELETE CASCADE, "
			  + "action_id VARCHAR(255) REFERENCES action(action_id) ON DELETE CASCADE, "
			  + "code INT(3) NOT NULL, "
			  + "success BOOLEAN NOT NULL, "
			  + "method VARCHAR(30) NOT NULL, "
			  + "in_parameters VARCHAR(2000) DEFAULT NULL, "
			  + "out_parameters VARCHAR(2000), "
			  + "request_created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP"
			  + ");", app.prefix())
			  );
			statement.executeUpdate();
			statement.close();
			
			
/*
CREATE TABLE IF NOT EXISTS api_permission (
	action_id VARCHAR(255) REFERENCES action(action_id) ON DELETE CASCADE,
	method VARCHAR(255),
	PRIMARY KEY(action_id, method)
);

CREATE TABLE IF NOT EXISTS api_role_permission (
	action_id VARCHAR(255) REFERENCES action(action_id) ON DELETE CASCADE,
	method VARCHAR(255),
	role_id INT(10) REFERENCES role(role_id) ON DELETE CASCADE,
	PRIMARY KEY(action_id, method, role_id)
);
 */
			
			// Table permissions
			
			statement = db.prepareStatement(String.format(
				"CREATE TABLE IF NOT EXISTS %spermission ("
			  + "action_id VARCHAR(255) REFERENCES action(action_id) ON DELETE CASCADE, "
			  + "method VARCHAR(255),"
			  + "PRIMARY KEY(action_id, method)"
			  + ");", app.prefix()));
			statement.executeUpdate();
			statement.close();
			
			statement = db.prepareStatement(String.format(
					"CREATE TABLE IF NOT EXISTS %srole_permission ("
				  + "action_id VARCHAR(255) REFERENCES action(action_id) ON DELETE CASCADE, "
				  + "method VARCHAR(255),"
				  + "role_id INT(10) REFERENCES role(role_id) ON DELETE CASCADE,"
				  + "PRIMARY KEY(action_id, method, role_id)"
				  + ");", app.prefix()));
				statement.executeUpdate();
				statement.close();
			

		} catch (SQLException e) {
			e.printStackTrace();
			res.err("db_error", e.getMessage());
			res.send(500);
			return;
		}
        
		
        	  
        	  
		res.addString("message", "The database was correctly set up");
		res.send(201);
	}


	public static boolean isDBSetUp(Connection db, Application app) {

		try {
			var statement = db.prepareStatement("SHOW TABLES;");
			var result = statement.executeQuery();
			while(result.next()) {
				if(result.getString(1).equals(String.format("%suser_role", app.prefix()))) return true;
			}
			result.close();
			statement.close();
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
		return false;

	}

}


	
