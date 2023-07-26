package backend.api.app;

import java.util.List;
import java.util.Objects;

import backend.api.app.core.Core;

import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Application class represents the application with all its modules/libraries/actions.
 */
public class Application {

	/**
	 * The list of modules of the application.
	 */
	private final List<Module> modules;

	/**
	 * The database connection pool of the application.
	 */
	private final ConnectionPool connectionPool;

	/**
	 * The configuration file path of the application.
	 */
	public final static String CONFIG_FILE = "config.json";
	
	
	/**
	 * The constructor of the application.
	 */
	public Application() {
		connectionPool = new ConnectionPool();
		modules = List.of(new Core());
	}

	/**
	 * Add a module to the application.
	 * @param module is the module to add.
	 */
	public void addModule(Module module) {
		modules.add(module);
	}
	
	/**
	 * Get the list of modules of the application.
	 * @return the list of modules of the application.
	 */
	public List<Module> modules() {
		return List.copyOf(modules);
	}

	/**
	 * Get the name of the application.
	 * @return  the name of the application.
	 */
	public String name() {
		return "api";
	}

	/**
	 * Get the database table prefix of the application.
	 * @return the table prefix of the application.
	 */
	public String prefix() { return connectionPool.prefix();}

	/**
	 * Get a free database connection.
	 * @return a free database connection.
	 */
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
	

	
	/**
	 * Create a log statement.
	 * @param db is the database connection.	
	 * @return the log statement.
	 */
	public PreparedStatement createLogStatement(Connection db) {
		Objects.requireNonNull(db);
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
	
	/**
	 * Register a connection to the API
	 * @param db  is the database connection.
	 * @param token is the token of the user.
	 * @param logStatement is the log statement for the request
	 */
	public void registerConnection(Connection db, String token, PreparedStatement logStatement) {
		Objects.requireNonNull(db);
		Objects.requireNonNull(token);
		Objects.requireNonNull(logStatement);
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

	/**
	 * Free a connection to the API
	 * @param connection is the connection to free.
	 */
	public void close(Connection connection) {
		Objects.requireNonNull(connection);
		connectionPool.freeConnection(connection);
	}

	/**
	 * Get a module by its name.
	 * @param name is the name of the module.
	 * @return the module with the given name.
	 */
	public Module getModule(String name) {
		Objects.requireNonNull(name);
		return modules.stream().filter(module -> module.name().equals(name)).findFirst().orElse(null);
	}

	/**
	 * Encrypt a string with SHA-256
	 * @param input is the string to encrypt.
	 * @return the encrypted string.
	 * @throws NoSuchAlgorithmException if the algorithm is not found.
	 */
	public String encrypt(String input) throws NoSuchAlgorithmException {
		Objects.requireNonNull(input);
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
