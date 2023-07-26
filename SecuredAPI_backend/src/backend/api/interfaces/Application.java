package backend.api.interfaces;

import java.util.List;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import backend.api.db.ConnectionPool;
import backend.api.module.Core;

public class Application {
	private final List<Module> modules;
	private final ConnectionPool connectionPool;
	public final static String CONFIG_FILE = "config.json";

	// constante DEBUG ON
	
	
	public Application() {
		connectionPool = new ConnectionPool();
		modules = List.of(new Core());
	}

	
	public void addModule(Module module) {
		modules.add(module);
	}
	
	public List<Module> modules() {
		return List.copyOf(modules);
	}

	public String name() {
		return "api";
	}

	public String prefix() { return connectionPool.prefix();}

	public Connection db() {
		Connection conn = null;
		while (true) {
			conn = connectionPool.getFreeConnection();
			if (conn != null)
				return conn;
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	

	
	public PreparedStatement createLogStatement(Connection db) {
		PreparedStatement logStatement = null;
		try {
			logStatement = db.prepareStatement(String.format(""
					+ "INSERT INTO %srequest (session_id, action_id, code, success, method, in_parameters, out_parameters) "
					+ "VALUES (?,?,?,?,?,?,?);"
					, prefix()));
			
			logStatement.setNull(1, 0);
			logStatement.setNull(2, 0);
			logStatement.setNull(3, 0);
			logStatement.setNull(4, 0);
			logStatement.setNull(5, 0);
			logStatement.setNull(6, 0);
			logStatement.setNull(7, 0);
	
		} catch (SQLException e) {
			return null;
		}
		return logStatement;
	}
	
	public void registerConnection(Connection db, String token, PreparedStatement logStatement) {
		try {
			
			// Check if token exists
			var tokenStatement = db.prepareStatement(String.format("SELECT session_id FROM %ssession WHERE session_id = ?;", prefix()));
			tokenStatement.setString(1, token);
			var tokenResult = tokenStatement.executeQuery();
			if(!tokenResult.next()) {
				tokenResult.close();
				tokenStatement.close();
				
				// Insert token
				tokenStatement = db.prepareStatement(String.format("INSERT INTO %ssession (session_id) VALUES (?);", prefix()));
				tokenStatement.setString(1, token);
				tokenStatement.executeUpdate();
				tokenStatement.close();
			}
			
			try {logStatement.setString(1, token);} catch(Exception e) {}

		} catch(Exception e) {
			e.printStackTrace();
			
		}

	}

	public void close(Connection connection) {
		connectionPool.freeConnection(connection);
	}

	public Module getModule(String name) {
		return modules.stream().filter(module -> module.name().equals(name)).findFirst().orElse(null);
	}


	public String encrypt(String input) throws NoSuchAlgorithmException {
		// Encrypt SHA-256
		var sha256Digest  = java.security.MessageDigest.getInstance("SHA-256");
		byte[] hash = sha256Digest.digest(input.getBytes(StandardCharsets.UTF_8));

		// Convert byte array into signum representation

        // Convert the byte array to a hexadecimal string representation
        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
	}



}
