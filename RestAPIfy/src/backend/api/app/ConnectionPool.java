package backend.api.app;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map.Entry;

import org.json.JSONObject;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;


/**
 * ConnectionPool class represents the database connection pool of the application.
 */
public class ConnectionPool {
	
	/**
	 * The pool size of the connection pool. (number of possible connections)
	 */
	private final static int POOL_SIZE = 10;

	/**
	 * The prefix of the database tables.
	 */
	private String prefix;

	/**
	 * The list of the free connections.
	 */	
	private final List<Entry<Connection, Boolean>> connections = new ArrayList<>();
	
	/**
	 * The database url.
	 */
	private String dbUrl;

	/**
	 * The database user.
	 */
	private String dbUser;

	/**
	 * The database password.
	 */
	private String dbPass;
	
	/**
	 * The constructor of the connection pool.
	 */
	public ConnectionPool() {
		
		try (BufferedReader reader = new BufferedReader(new FileReader(Application.CONFIG_FILE))) {
            var jsonString = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) jsonString.append(line);
            
            var json = new JSONObject(jsonString.toString());

            Objects.requireNonNull(json);
            
    	    dbUrl = "jdbc:mysql://"+json.getString("db_hostname")+"/"+json.getString("db_name");
            dbUser = json.getString("db_user");
            dbPass = json.getString("db_password");
            prefix = json.getString("table_prefix");

            
		} catch(IOException e) {
			System.err.print(e);
		}
		
		for(int i = 0; i < POOL_SIZE; i++) addConnection();
	}
	
	/**
	 * Add a connection to the connection pool.
	 */
	private void addConnection() {
	    try {
	    	 Class.forName("com.mysql.cj.jdbc.Driver");
	    	 connections.add(Map.entry(DriverManager.getConnection(dbUrl, dbUser, dbPass), true));
      
	    } catch (Exception e) {System.err.print(e);}
	}
	
	/**
	 * Get the database table prefix of the application.
	 * @return the table prefix of the application.
	 */
	public String prefix() { return prefix;}
	
	/**
	 * Get a free connection from the connection pool.
	 * @return a free connection from the connection pool.
	 */
	public Connection getFreeConnection() {
		// return the first connection to TRUE and set it to FALSE
		for(Entry<Connection, Boolean> entry : connections) {
			if(entry.getValue()) {
				connections.set(connections.indexOf(entry), Map.entry(entry.getKey(), false));
				return entry.getKey();
			}
		}
		return null;
	}
	
	/**
	 * Free a connection from the connection pool.
	 * @param connection is the connection to free.
	 */
	public void freeConnection(Connection connection) {
		for(Entry<Connection, Boolean> entry : connections) {
			if(entry.getKey().equals(connection)) {
				connections.set(connections.indexOf(entry), Map.entry(entry.getKey(), true));
				return;
			}
		}
	}
}
