package backend.api.module.setup;
import java.io.IOException;
import java.util.List;

import backend.api.endpoint.ResponseData;
import backend.api.interfaces.Action;
import backend.api.interfaces.Application;
import backend.api.interfaces.Parameter;



import java.sql.Connection;
import java.sql.SQLException;


public record SetupDB() implements Action {

	@Override
	public String description() {return "Set up the database for the installation";}
	
	@Override
	public List<Parameter<?>> parameters() {
		return List.of(
			new Parameter<>(String.class, "admin_password", "The password of the website administrator", null, true),
			new Parameter<>(String.class, "admin_mail", "The email of the website administrator", null, true),
			new Parameter<>(String.class, "admin_firstname", "The firstname of the website administrator", null, true),
			new Parameter<>(String.class, "admin_lastname", "The lastname of the website administrator", null, true)
		);
	}
	
	
	
	@Override
	public boolean isGuestAction() { return true; }
	
	@Override
	public void execute(Application app, ResponseData res, List<Parameter<?>> params, Connection db, String id) throws IOException {
		//response.appendResult(new Parameter<String>("message", "Request success !"));
		//response.appendResult(new Parameter<Integer>("valor", 1));
		
		//if(RequestData.requireId(response, id)) return;
		
		//params.forEach((param) -> System.out.println(param));
		
		// Find parameter with name "param1"

		// Create tables 
		try {
			// Table module
			var statement = db.prepareStatement(String.format(
				"CREATE TABLE IF NOT EXISTS %smodule ("
				+ "module_id VARCHAR(255) PRIMARY KEY NOT NULL, "
				+ "description VARCHAR(255) NOT NULL, "
				+ "version VARCHAR(10) NOT NULL, "
				+ "author VARCHAR(255) NOT NULL, "
				+ "author_url VARCHAR(255) NOT NULL, "
				+ "installed_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, "
				+ "updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP"
				+ ");", app.prefix())
			);
			statement.executeUpdate();
			statement.close();

			// Table library
			statement = db.prepareStatement(String.format(
					"CREATE TABLE IF NOT EXISTS %slibrary ("
				  + "library_id VARCHAR(255) PRIMARY KEY NOT NULL, "
				  + "module_id VARCHAR(255) NOT NULL REFERENCES module(module_id) ON DELETE CASCADE, "
				  + "description VARCHAR(255) NOT NULL, "
				  + "version VARCHAR(10) NOT NULL, "
				  + "author VARCHAR(255) NOT NULL, "
				  + "author_url VARCHAR(255) NOT NULL, "
				  + "installed_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, "
				  + "updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP"
				  + ");", app.prefix())
				  );
			statement.executeUpdate();
			statement.close();

			// table action
			statement = db.prepareStatement(String.format(
				"CREATE TABLE IF NOT EXISTS %saction ("
			  + "action_id VARCHAR(255) PRIMARY KEY NOT NULL, "
			  + "library_id VARCHAR(255) NOT NULL REFERENCES library(library_id) ON DELETE CASCADE, "
			  + "description VARCHAR(255) NOT NULL, "
			  + "is_guest_action BOOLEAN NOT NULL, "
			  + "installed_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, "
			  + "updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP"
			  + ");", app.prefix())
			  );
			statement.executeUpdate();
			statement.close();

			
			// Table user
			statement = db.prepareStatement(String.format(
				"CREATE TABLE IF NOT EXISTS %suser ("
			  + "user_id INT(10) PRIMARY KEY NOT NULL AUTO_INCREMENT, "
			  + "username VARCHAR(255) NOT NULL, "
			  + "password VARCHAR(255) NOT NULL, "
			  + "email VARCHAR(255) NOT NULL, "
			  + "first_name VARCHAR(255) NOT NULL, "
			  + "last_name VARCHAR(255) NOT NULL, "
			  + "phone VARCHAR(255) NOT NULL, "
			  + "created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP "
			  + ");", app.prefix())
			  );
			statement.executeUpdate();
			statement.close();
			
			// insert admin user
			/*statement = db.prepareStatement(String.format(
				"INSERT INTO %suser (email, password, first_name, last_name) "
			   +"values (?, ?, ?, ?);", app.prefix())
			  );
			  */
			
			System.out.println(Parameter.find(params, "admin_mail").value().getClass().getCanonicalName());
			//statement.setString(1, Parameter.find(params, "admin_mail").value());
			//statement.setString(2, Parameter.find(params, "admin_password").value());
			//statement.setString(3, Parameter.find(params, "admin_firstname").value());
			//statement.setString(4, Parameter.find(params, "admin_lastname").value());
			
			//statement.executeUpdate();
			//statement.close();


			// Table session
			statement = db.prepareStatement(String.format(
				"CREATE TABLE IF NOT EXISTS %ssession ("
			  + "id INT(10) PRIMARY KEY NOT NULL AUTO_INCREMENT, "
			  + "user_id VARCHAR(255) NOT NULL REFERENCES user(user_id) ON DELETE CASCADE, "
			  + "created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP "
			  + ");", app.prefix())
			  );
			statement.executeUpdate();
			statement.close();

			// Table role
			statement = db.prepareStatement(String.format(
				"CREATE TABLE IF NOT EXISTS %srole ("
			  + "role_id INT(10) PRIMARY KEY NOT NULL AUTO_INCREMENT, "
			  + "name VARCHAR(255) NOT NULL, "
			  + "description VARCHAR(255) NOT NULL, "
			  + "created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP "
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

			// insert Superman role
			statement = db.prepareStatement(String.format(
				"INSERT INTO %srole (name, description) VALUES ('Superman', 'The superman role with all rights');", app.prefix())
			  );
			statement.executeUpdate();
			statement.close();
			
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
			
		} catch (SQLException e) {
			e.printStackTrace();
			res.err("db_error", e.getMessage());
			return;
		}
        
		
        	  
        	  
		res.addString("message", "The database was correctly set up");
		res.send(200);
	}


}


	
