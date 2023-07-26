package backend.api.app.core.auth;

import java.sql.Connection;
import java.util.List;
import java.util.Map;

import backend.api.app.Action;
import backend.api.app.Application;
import backend.api.app.Method;
import backend.api.app.Parameter;
import backend.api.app.ResponseData;

public record Register() implements Action {



	@Override
	public String description() {return "Register user";}
	
	
	@Override
	public Map<String, String> methodsDoc() {
		return Map.of(
			Method.POST, "Register user in database"
		);
	}
	
	@Override
	public boolean isGuestAction() { return true; }
	
	
	@Override
	public List<Parameter<?>> parameters() {
		return List.of(
			new Parameter<>(String.class, "email", "Email of the user", null, true),
			new Parameter<>(String.class, "password", "Password of the user", null, true),
			new Parameter<>(String.class, "firstname", "First name of the user", null, false),
			new Parameter<>(String.class, "lastname", "Last name of the user", null, false),
			new Parameter<>(String.class, "phone", "Phone of the user", null, true),
			new Parameter<>(String.class, "username", "Phone of the user", null, true)
		);
	}
	
	@Override
	public void post(Application app, ResponseData res, Connection db, List<Parameter<?>> params, String token) {
		var email = (String) Parameter.find(params, "email").value();
		var password = (String) Parameter.find(params, "password").value();
		var firstname = (String) Parameter.find(params, "firstname").value();
		var lastname = (String) Parameter.find(params, "lastname").value();
		var phone = (String) Parameter.find(params, "phone").value();
		var username = (String) Parameter.find(params, "username").value();

		// encrypt password
		try {
			password = app.encrypt(password);
		} catch(Exception e) {
			res.err("password_encrypt_failed", "Failed to encrypt password");
			res.send(500);
			return;
		}

		

		// Check if user already exists
		try {
			var statement = db.prepareStatement(String.format("SELECT * FROM %suser WHERE email = ?", app.prefix()));
			statement.setString(1, email);
			var result = statement.executeQuery();
			if(result.next()) {
				res.err("email_already_exists", "The email" + email + " is already taken");
				res.send(409);
				return;
			}
			statement.close();

			// Create user
			statement = db.prepareStatement(String.format("INSERT INTO %suser (email, password, first_name, last_name, phone, username) VALUES (?, ?, ?, ?, ?, ?)", app.prefix()));
			statement.setString(1, email);
			statement.setString(2, password);
			statement.setString(3, firstname);
			statement.setString(4, lastname);
			statement.setString(5, phone);
			statement.setString(6, username);
			statement.executeUpdate();
			statement.close();

			// set the user_id of the session to the user_id of the user
			/*
			statement = db.prepareStatement(String.format("UPDATE %ssession SET user_id = (SELECT user_id FROM %suser WHERE email = ?) WHERE session_id = ?", app.prefix(), app.prefix()));	
			statement.setString(1, email);
			statement.setString(2, token);
			statement.executeUpdate();
			statement.close();
			*/

			res.send(201);

		} catch(Exception e) {
			res.err("internal_error", e.getMessage());
			res.send(500);
		}



	}	
	
}
