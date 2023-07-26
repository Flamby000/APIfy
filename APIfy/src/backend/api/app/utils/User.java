package backend.api.app.utils;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import backend.api.app.Action;
import backend.api.app.Application;


/**
 * User class represents a user.
 */
public class User {

    /**
     * The unique identifier of the user.
     */
    public final int id;

    /**
     * The username of the user.
     */
    public final String username;

    /**
     * The password of the user.
     */
    public final String password;

    /**
     * The email of the user.
     */
    public final String email;

    /**
     * The first name of the user.
     */
    public final String firstName;

    /**
     * The last name of the user.
     */
    public final String lastName;

    /**
     * The phone of the user.
     */
    public final String phone;

    /**
     * The creation date of the user.
     */
    public final Date creationDate;


    /**
     * The database connection.
     */
    public final Connection db;

    /**
     * The application.
     */
    public final Application app;
    

    /**
     * User constructor.
     * @param db is the database connection.
     * @param app is the application.
     * @param id is the unique identifier of the user.
     * @param username is the username of the user.
     * @param password is the password of the user.
     * @param email is the email of the user.
     * @param firstName is the first name of the user.
     * @param lastName is the last name of the user.
     * @param phone is the phone of the user.
     * @param creationDate is the creation date of the user.
     */
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

    /**
     * User constructor.
     * @param db is the database connection.
     * @param app is the application.
     * @param token is the token of the user.
     * @throws SQLException if the user is not found.
     */
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

    /**
     * The unique identifier getter.
     * @return the unique identifier of the user.
     */
    public int id() { return id; }

    /**
     * The username getter.
     * @return the username of the user.
     */
    public String username() { return username; }

    /**
     * The password getter.
     * @return the password of the user.
     */
    public String password() { return password; }

    /**
     * The email getter.
     * @return the email of the user.
     */
    public String email() { return email; }

    /**
     * The first name getter.
     * @return the first name of the user.
     */
    public String firstName() { return firstName; }

    /**
     * The last name getter.
     * @return the last name of the user.
     */
    public String lastName() { return lastName; }

    /**
     * The phone getter.
     * @return the phone of the user.
     */
    public String phone() { return phone; }

    /**
     * The creation date getter.
     * @return the creation date of the user.
     */
    public Date creationDate() { return creationDate; }

    /**
     * Update a field of the user.
     * @param db is the database connection.
     * @param app is the application.
     * @param id is the unique identifier of the user.
     * @param column is the column to update.
     * @param value is the new value of the column.
     * @throws SQLException if the update failed.
     */
    public static void updateField(Connection db, Application app, String id, String column, String value) throws SQLException {
        Objects.requireNonNull(db);
        Objects.requireNonNull(app);
        Objects.requireNonNull(id);
        Objects.requireNonNull(column);
        Objects.requireNonNull(value);


        var statement = db.prepareStatement(String.format(
                "UPDATE %suser SET %s = ? WHERE user_id = ?;"
                , app.prefix(), column));
        statement.setString(1, value);
        statement.setInt(2, Integer.parseInt(id));
        statement.executeUpdate();
        statement.close();
    }

    /**
     * Delete a user.
     * @param db is the database connection.
     * @param app is the application.
     * @param id is the unique identifier of the user.
     * @return true if the user is deleted, false otherwise.
     */
    public static boolean delete(Connection db, Application app, String id) {
        Objects.requireNonNull(db);
        Objects.requireNonNull(app);
        Objects.requireNonNull(id);
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
    
    /**
     * Convert the user to a map.
     * @return the map of the user.
     */
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
    
    /**
     * List of the user roles.
     * @return the list of the user roles.
     */
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

    /**
     * Get a user by its unique identifier.
     * @param db is the database connection.
     * @param app is the application.
     * @param id is the unique identifier of the user.
     * @return the user if it exists, null otherwise.
     */
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

    /**
     * Get all the users.
     * @param db is the database connection.
     * @param app is the application.
     * @return the list of all the users.
     */
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

    /**
     * Check if the user is a superman.
     * @return true if the user is a superman, false otherwise.
     */
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

    /**
     * Check if the user can perform an action.
     * @param action is the action to check.
     * @return true if the user can perform the action, false otherwise.
     */
    public boolean canPerform(Action action) {
        Objects.requireNonNull(action);
    
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
