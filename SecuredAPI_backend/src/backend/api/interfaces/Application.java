package backend.api.interfaces;

import java.util.List;
import java.sql.Connection;

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

	public void close(Connection connection) {
		connectionPool.freeConnection(connection);
	}

	public Module getModule(String name) {
		return modules.stream().filter(module -> module.name().equals(name)).findFirst().orElse(null);
	}

}
