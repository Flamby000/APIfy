package backend.api.module.auth;

import java.sql.Connection;
import java.util.List;
import java.util.Map;

import backend.api.endpoint.ResponseData;
import backend.api.interfaces.Action;
import backend.api.interfaces.Application;
import backend.api.interfaces.Method;
import backend.api.interfaces.Parameter;

public record Login() implements Action {

	

	@Override
	public String description() {return "Login user";}
	
	
	@Override
	public Map<String, String> methodsDoc() {
		return Map.of(
			Method.POST, "Login"
		);
	}
	
	@Override
	public boolean isGuestAction() { return true; }
	
	
	@Override
	public List<Parameter<?>> parameters() {
		return List.of(
			new Parameter<>(String.class, "login", "Email or login of the user", null, true),
			new Parameter<>(String.class, "password", "Password of the user", null, true)
		);
	}
	
	@Override
	public void post(Application app, ResponseData res, Connection db, List<Parameter<?>> params, String token) {
		var login = (String) Parameter.find(params, "login").value();
		var password = (String) Parameter.find(params, "password").value();


		// Check if login is email or username
		try {
			var statement = db.prepareStatement(String.format("SELECT * FROM %suser WHERE email = ? OR username = ?", app.prefix()));
			statement.setString(1, login);
			statement.setString(2, login);
			var result = statement.executeQuery();
			if(!result.next()) {
				res.err("user_not_found", "Invalid login");
				res.send(404);
				return;
			}

			// check password
			var realPassword = result.getString("password");
			if(!app.encrypt(password).equals(realPassword)) {
				res.err("invalid_password", "Wrong password");
				res.send(401);
				return;
			}

			var userId = result.getInt("user_id");
			statement.close();

			// Update user_id field of %ssession table where session_id == token
			statement = db.prepareStatement(String.format("UPDATE %ssession SET user_id = ? WHERE session_id = ?;", app.prefix()));
			statement.setInt(1, userId);
			statement.setString(2, token);
			statement.executeUpdate();
			statement.close();
			
			res.send(200);

		} catch(Exception e) {
			res.err("login_failed", "Failed to login : " + e.getMessage());
			res.send(500);
			return;
		}

		
	}
	
}
