package backend.api.interfaces;


import java.util.List;
import java.sql.Connection;
import org.json.JSONObject;

import backend.api.db.ConnectionPool;
import backend.api.module.Core;


public class Application {
	private final List<Module> modules;
	private final ConnectionPool connectionPool;
	
	public Application() {
		connectionPool = new ConnectionPool();
		modules = List.of(
			new Core()
		);
	}
	
	public List<Module> modules() { return modules; }
	
	public String name() { return "api"; }
	
	public Connection db() {
		Connection conn = null;
		while(true) {
			conn = connectionPool.getFreeConnection();
			if(conn != null) return conn;
			try { Thread.sleep(10); } catch (InterruptedException e) { e.printStackTrace(); }
		}
	}
	
	public void close(Connection connection) {connectionPool.freeConnection(connection);}
	
	public Module getModule(String name) {
		return modules.stream()
			.filter(module -> module.name().equals(name))
			.findFirst()
			.orElse(null);
	}

	
	

   public static String extractValueFromJson(String jsonObject, String key) {
		int index = jsonObject.indexOf(key);
		if(index == -1) return null;
		int startIndex = jsonObject.indexOf(":", index) + 1;
		int endIndex = jsonObject.indexOf(",", index);
		if(endIndex == -1) endIndex = jsonObject.indexOf("}", index);
		return jsonObject.substring(startIndex, endIndex).replaceAll("\"", "").trim();
    }	

	public static String extractKeyFromJson(String jsonObject, String key) {
		int index = jsonObject.indexOf(key);
		if(index == -1) return null;
		int startIndex = jsonObject.indexOf("\"", index) + 1;
		int endIndex = jsonObject.indexOf(":", index);
		return jsonObject.substring(startIndex, endIndex).replaceAll("\"", "").trim();
	}

	public static String getJsonTypeFromValue(String value) {
		if(value == null) return "null";
		if(value.equals("true") || value.equals("false")) return "boolean";
		if(value.matches("^[0-9]+$")) return "int";
		if(value.matches("^[0-9]+\\.[0-9]+$")) return "double";
		return "string";
	}
	
}
