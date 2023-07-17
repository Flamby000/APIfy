package backend.api.interfaces;

import java.util.List;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import backend.api.db.ConnectionPool;
import backend.api.module.Core;

public class Application {
	private final List<Module> modules;
	private final ConnectionPool connectionPool;
	
	public Application() {
		connectionPool = new ConnectionPool();
		modules = List.of(new Core());
	}

	public List<Module> modules() {
		return modules;
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
	
	public boolean isDBSetup() {
		return true;
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

}
