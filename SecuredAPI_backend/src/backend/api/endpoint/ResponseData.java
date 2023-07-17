package backend.api.endpoint;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.json.JSONObject;

import com.sun.net.httpserver.HttpExchange;

import backend.api.interfaces.Application;
import backend.api.interfaces.Parameter;

public class ResponseData {
	private final HttpExchange exchange;
	private boolean over;
	
	private final Map<String, String> errors = new HashMap<>();
	private final Map<String, String> warnings = new HashMap<>();

	private final List<Parameter<?>> result = new ArrayList<>();
	
	private final PreparedStatement statement;
	private final Application app;
	private final Connection db;
	
	public ResponseData(HttpExchange exchange, Application app, PreparedStatement statement, Connection db) {
		Objects.requireNonNull(exchange);
		Objects.requireNonNull(exchange);
		this.exchange = exchange;
		over = false;
		this.db = db;
		this.app = app;
		this.statement = statement;
	}
	
	public void appendError(String error, String description) {
		errors.put(error, description);
	}
	public void err(String error, String description) {
		appendError(error, description);
	}
	
	public void appendResult(Parameter<?> parameter) {
		result.add(parameter);
	}
	
	public void appendWarning(String warning, String description) {
		warnings.put(warning, description);
	}
	public void warn(String warning, String description) {
		appendWarning(warning, description);
	}
	
	public boolean success() {
		return errors.size() == 0;
	}
	
	public void addString(String name, String value) {
		appendResult(new Parameter<>(String.class, name, value));
	}
	
	public void addInt(String name, int value) {
		appendResult(new Parameter<>(Integer.class, name, value));
	}
	
	public void addBool(String name, boolean value) {
		appendResult(new Parameter<>(Boolean.class, name, value));
	}

	public void addArray(String name, List<?> value) {
		appendResult(new Parameter<>(List.class, name, value));
	}

	public void addMap(String name, Map<?, ?> value) {
		appendResult(new Parameter<>(Map.class, name, value));
	}
	
	

	
	public void send(int code) {
		
		var res = new JSONObject();
		res.put("success", success());
		res.put("errors", errors.entrySet().stream()
		    .map(error -> new JSONObject()
		        .put("error", error.getKey())
		        .put("description", error.getValue()))
		    .collect(Collectors.toList()));

		res.put("data", result.stream()
			.map(parameter -> new JSONObject()
				.put("name", parameter.name())
				.put("value", parameter.stringifyValue()))
			.collect(Collectors.toList()));

		res.put("warnings", warnings.entrySet().stream()
		    .map(warning -> new JSONObject()
		        .put("warning", warning.getKey())
		        .put("description", warning.getValue()))
		    .collect(Collectors.toList()));
		
		
		// Send response to client
		var response = res.toString();	
		
		try {
			statement.setInt(3, code);
			statement.setBoolean(4, success());
			statement.setString(7, response);
		} catch (SQLException e) {e.printStackTrace();}

        try {
			exchange.sendResponseHeaders(code, response.getBytes().length);

			exchange.getResponseBody().write(response.getBytes());
		} catch (IOException e) {
			e.printStackTrace();
			// TODO : END APP
		}
        exchange.close();
        
		try {
			statement.executeUpdate();
			statement.close();
		} catch (SQLException e) {e.printStackTrace();}
		
		app.close(this.db);
        over = true;
	}
	

	
	public boolean isClosed() { return over; }
	
}
