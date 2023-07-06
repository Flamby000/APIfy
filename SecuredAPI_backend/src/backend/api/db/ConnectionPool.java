package backend.api.db;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map.Entry;

import backend.api.interfaces.Application;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ConnectionPool {
	
	private final static String DB_CONFIG = "db.json";
	private final static int POOL_SIZE = 10;
	
	private final List<Entry<Connection, Boolean>> connections = new ArrayList<>();
	
	private String dbUrl;
	private String dbUser;
	private String dbPass;
	
	public ConnectionPool() {
		
		try (BufferedReader reader = new BufferedReader(new FileReader(DB_CONFIG))) {
            var jsonString = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) jsonString.append(line);
            
            var jsonContent = jsonString.toString();
    	    dbUrl = "jdbc:mysql://"+Application.extractValueFromJson(jsonContent, "db_hostname")+"/"+Application.extractValueFromJson(jsonContent, "db_name");
            dbUser = Application.extractValueFromJson(jsonContent, "db_user");
            dbPass = Application.extractValueFromJson(jsonContent, "db_password");
            
            
		} catch(IOException e) {System.err.print(e);}
		
		for(int i = 0; i < POOL_SIZE; i++) addConnection();
	}
	
	private void addConnection() {
	    try {
	    	 Class.forName("com.mysql.cj.jdbc.Driver");
	    	 connections.add(Map.entry(DriverManager.getConnection(dbUrl, dbUser, dbPass), true));
      
	    } catch (Exception e) {System.err.print(e);}
	}
	
	public Connection getFreeConnection() {
		for(Entry<Connection, Boolean> entry : connections) {
			if(entry.getValue()) {
				entry.setValue(false);
				return entry.getKey();
			}
		}
		return null;
	}
	
	public void freeConnection(Connection connection) {
		for(Entry<Connection, Boolean> entry : connections) {
			if(entry.getKey().equals(connection)) {
				entry.setValue(true);
				return;
			}
		}
	}
	
 
}
