package backend.api.db;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map.Entry;

import org.json.JSONObject;

import backend.api.interfaces.Application;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ConnectionPool {
	
	private final static int POOL_SIZE = 10;
	private String prefix;
	
	private final List<Entry<Connection, Boolean>> connections = new ArrayList<>();
	
	private String dbUrl;
	private String dbUser;
	private String dbPass;
	
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
	
	private void addConnection() {
	    try {
	    	 Class.forName("com.mysql.cj.jdbc.Driver");
	    	 connections.add(Map.entry(DriverManager.getConnection(dbUrl, dbUser, dbPass), true));
      
	    } catch (Exception e) {System.err.print(e);}
	}
	public String prefix() { return prefix;}
	
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
	
	public void freeConnection(Connection connection) {
		for(Entry<Connection, Boolean> entry : connections) {
			if(entry.getKey().equals(connection)) {
				connections.set(connections.indexOf(entry), Map.entry(entry.getKey(), true));
				return;
			}
		}
	}
	
 
}
