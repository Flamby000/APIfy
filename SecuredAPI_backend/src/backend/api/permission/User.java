package backend.api.permission;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import backend.api.interfaces.Action;
import backend.api.interfaces.Application;

public class User {
    public final int id;
    public final String username;
    public final String password;
    public final String email;
    public final String firstName;
    public final String lastName;
    public final String phone;
    public final Date creationDate;

    public final Connection db;
    public final Application app;
    
    public User(Connection db, Application app, int id, String username, String password, String email, String firstName, String lastName, String phone, Date creationDate) {
        Objects.requireNonNull(db);
        Objects.requireNonNull(app);
    	
    	this.id = id;
        this.username = username;
        this.password = password;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.phone = phone;
        this.creationDate = creationDate;
        this.db = db;
        this.app = app;
    }

    public User(Connection db, Application app, String token) throws SQLException {
        Objects.requireNonNull(db);
        Objects.requireNonNull(app);
        
        PreparedStatement statement;

        statement = db.prepareStatement(String.format(""
        + "SELECT * FROM %ssession WHERE session_id = ?;"
        , app.prefix()));
        
        
        statement.setString(1, token);
        var result = statement.executeQuery();
        if(!result.next()) {
            result.close();
            statement.close();
            throw new SQLException("User not found");
        } else {
            // if the user_id field data is null, return null
            if(result.getInt("user_id") == 0) {
                result.close();
                statement.close();
                throw new SQLException("User not found");
            } else {
                // if the user_id field data is not null, return the user
                var userStatement = db.prepareStatement(String.format(""
                + "SELECT * FROM %suser WHERE user_id = ?;"
                , app.prefix()));
                userStatement.setInt(1, result.getInt("user_id"));
                var userResult = userStatement.executeQuery();
                if(!userResult.next()) {
                    userResult.close();
                    userStatement.close();
                    throw new SQLException("User not found");
                } else {
                    this.id = userResult.getInt("user_id");
                    this.username = userResult.getString("username");
                    this.password = userResult.getString("password");
                    this.email = userResult.getString("email");
                    this.firstName = userResult.getString("first_name");
                    this.lastName = userResult.getString("last_name");
                    this.phone = userResult.getString("phone");
                    this.creationDate = userResult.getDate("user_created_at");
                    this.db = db;
                    this.app = app;
                    userResult.close();
                    userStatement.close();
                }
            }
        }
    }

    public int id() { return id; }
    public String username() { return username; }
    public String password() { return password; }
    public String email() { return email; }
    public String firstName() { return firstName; }
    public String lastName() { return lastName; }
    public String phone() { return phone; }
    public Date creationDate() { return creationDate; }

    
    public static void updateField(Connection db, Application app, String id, String column, String value) throws SQLException {
        var statement = db.prepareStatement(String.format(
                "UPDATE %suser SET %s = ? WHERE user_id = ?;"
                , app.prefix(), column));
        statement.setString(1, value);
        statement.setInt(2, Integer.parseInt(id));
        statement.executeUpdate();
        statement.close();
    }

    public static boolean delete(Connection db, Application app, String id) {
        try {
            var statement = db.prepareStatement(String.format(
                    "DELETE FROM %suser WHERE user_id = ?;"
                    , app.prefix()));
            statement.setInt(1, Integer.parseInt(id));
            statement.executeUpdate();
            statement.close();
            return true;
        } catch (SQLException e) {
            return false;
        }
    }
    
    public Map<String, String> toMap() {
        var map = new HashMap<String, String>();
        map.put("id", String.valueOf(id));
        map.put("username", username);
        //map.put("password", password);
        map.put("email", email);
        map.put("first_name", firstName);
        map.put("last_name", lastName);
        map.put("phone", phone);
        map.put("creationDate", creationDate.toString());
        return map;
        
    }
    

    public List<Role> roles() {
        /* Get roles of the user from api_action_role and api_user_role*/
        
        try {
            var statement = db.prepareStatement(String.format(
                    "SELECT * FROM %suser NATURAL JOIN %suser_role NATURAL JOIN %srole WHERE user_id = ?;"
                    , app.prefix(), app.prefix(), app.prefix()));
            statement.setInt(1, id);
            var result = statement.executeQuery();
            
            var roles = new ArrayList<Role>();
            
            while(result.next()) {
                roles.add(new Role(db, app, result.getInt("role_id"), result.getString("role_name"), result.getString("role_description"), result.getDate("role_created_at")));
            }
            
            result.close();
            statement.close();
            
            return roles;
        } catch (SQLException e) {
            return null;
        }
    }

    public static User user(Connection db, Application app, int id) {
        try {
            var statement = db.prepareStatement(String.format(
                    "SELECT * FROM %suser WHERE user_id = ?;"
                    , app.prefix()));
            statement.setInt(1, id);
            var result = statement.executeQuery();
            if(!result.next()) {
                result.close();
                statement.close();
                return null;
            } else {
                var user = new User(db, app, result.getInt("user_id"), result.getString("username"), result.getString("password"), result.getString("email"), result.getString("first_name"), result.getString("last_name"), result.getString("phone"), result.getDate("user_created_at"));
                result.close();
                statement.close();
                return user;
            }
        } catch (SQLException e) {
            return null;
        }
    }

    public static List<User> users(Connection db, Application app) {
    	try {
	        var statement = db.prepareStatement(String.format(
	                "SELECT * FROM %suser;"
	                , app.prefix()));
	        var result = statement.executeQuery();
	        
	        var users = new ArrayList<User>();
	
	        while(result.next()) {
	            users.add(new User(db, app, result.getInt("user_id"), result.getString("username"), result.getString("password"), result.getString("email"), result.getString("first_name"), result.getString("last_name"), result.getString("phone"), result.getDate("user_created_at")));
	        }
	
	        result.close();
	        statement.close();
	
	        return users;
    	} catch(Exception e) { return null;}
    }
    

    
    public boolean isSuperMan() {
        try {
            var statement = db.prepareStatement(String.format(
                    "SELECT role_id "
                  + "FROM %suser NATURAL JOIN %suser_role NATURAL JOIN %srole "
                  + "WHERE user_id = ? AND role_name = ?;"
                  , app.prefix(), app.prefix(), app.prefix()));
            statement.setInt(1, id);
            statement.setString(2, Role.SUPERMAN_ROLE);
            var result = statement.executeQuery();

            if(result.next()) {
                result.close();
                statement.close();
                return true;
            } else {
                result.close();
                statement.close();
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public boolean canPerform(Action action) {
       
    	try {
			var statement = db.prepareStatement(String.format(
					"SELECT action_id "
				  + "FROM %suser NATURAL JOIN %suser_role NATURAL JOIN %saction_role NATURAL JOIN %srole_permission "
				  + "WHERE user_id = ? AND action_id = ?;"
				  , app.prefix(), app.prefix(), app.prefix(), app.prefix()));
            statement.setInt(1, id);
            statement.setString(2, action.name());
            var result = statement.executeQuery();
            
            if(result.next()) {
                result.close();
                statement.close();
                return true;
            } else {
                result.close();
                statement.close();
               return false;
            }
			
		} catch (SQLException e) {
            e.printStackTrace();
			return false;
		}
    }
	
    

}
