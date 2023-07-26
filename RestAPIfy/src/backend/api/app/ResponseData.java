package backend.api.app;

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

import org.json.JSONArray;
import org.json.JSONObject;

import com.sun.net.httpserver.HttpExchange;

/**
 * ResponseData class represents the response data for the client.
 */
public class ResponseData {

	/**
	 * The exchange of the response.
	 */
	private final HttpExchange exchange;

	/**
	 * The flag to know if the response is closed.
	 */
	private boolean over;
	
	/**
	 * The list of errors of the response.
	 */
	private final Map<String, String> errors = new HashMap<>();

	/**
	 * The list of warnings of the response.
	 */
	private final Map<String, String> warnings = new HashMap<>();

	/**
	 * The list of result of the response.
	 */
	private final List<Parameter<?>> result = new ArrayList<>();
	
	/**
	 * The db statement for the log.
	 */
	private final PreparedStatement statement;

	/**
	 * The database connection.
	 */
	private final Application app;

	/**
	 * The database connection.
	 */
	private final Connection db;
	
	
	
	/**
	 * The constructor of the response data.
	 * @param exchange is the exchange of the response.
	 * @param app is the application.
	 * @param statement is the statement for the log.
	 * @param db is the database connection.
	 */
	public ResponseData(HttpExchange exchange, Application app, PreparedStatement statement, Connection db) {
		Objects.requireNonNull(exchange);
		Objects.requireNonNull(app);
		Objects.requireNonNull(db);
		this.exchange = exchange;
		over = false;
		this.db = db;
		this.app = app;
		this.statement = statement;
	}
	
	/**
	 * Append an error to the response.
	 * @param error is the error name
	 * @param description is the description of the error.
	 */
	public void appendError(String error, String description) {
		errors.put(error, description);
	}

	/**
	 * Append an error to the response.
	 * @param error is the error name
	 * @param description is the description of the error.
	 */
	public void err(String error, String description) {
		appendError(error, description);
	}
	
	/**
	 * Append a result to the response.
	 * @param parameter is the parameter to append.
	 */
	public void appendResult(Parameter<?> parameter) {
		result.add(parameter);
	}
	
	/**
	 * Append a warning to the response.
	 * @param warning is the warning name
	 * @param description is the description of the warning.
	 */
	public void appendWarning(String warning, String description) {
		warnings.put(warning, description);
	}

	/**
	 * Append a warning to the response.
	 * @param warning is the warning name
	 * @param description is the description of the warning.
	 */
	public void warn(String warning, String description) {
		appendWarning(warning, description);
	}
	
	/**
	 * Check if the response is a success.
	 * @param code is the code of the response.
	 * @return true if the response is a success, false otherwise.
	 */
	public boolean success(int code) {
		return errors.size() == 0 && code < 400; // TODO see codes
	}
	
	/**
	 * Append a string to the result response.
	 * @param name is the name of the field.
	 * @param value is the value of the string.
	 */
	public void addString(String name, String value) {
		appendResult(new Parameter<>(String.class, name, value));
	}
	
	/**
	 * Append an integer to the result response.
	 * @param name is the name of the field.
	 * @param value is the value of the integer.
	 */
	public void addInt(String name, int value) {
		appendResult(new Parameter<>(Integer.class, name, value));
	}
	
	/**
	 * Append a boolean to the result response.
	 * @param name is the name of the field.
	 * @param value is the value of the boolean.
	 */
	public void addBool(String name, boolean value) {
		appendResult(new Parameter<>(Boolean.class, name, value));
	}

	/**
	 * Append a double to the result response.
	 * @param name is the name of the field.
	 * @param value is the value of the double.
	 */
	public void addArray(String name, List<?> value) {
		appendResult(new Parameter<>(JSONArray.class, name, new JSONArray(value)));
	}

	/**
	 * Append a double to the result response.
	 * @param name is the name of the field.
	 * @param value is the value of the double.
	 */
	public void addList(String name, List<?> value) {
		appendResult(new Parameter<>(JSONArray.class, name, new JSONArray(value)));
	}

	/**
	 * Append a double to the result response.
	 * @param name is the name of the field.
	 * @param value is the value of the double.
	 */
	public void addMap(String name, Map<?, ?> value) {
		appendResult(new Parameter<>(JSONObject.class, name, new JSONObject(value)));
	}

	/**
	 * Append a double to the result response.
	 * @param name is the name of the field.
	 * @param value is the value of the double.
	 */
	public void addJSONObject(String name, JSONObject value) {
		appendResult(new Parameter<>(JSONObject.class, name, value));
	}
	
	

	/**
	 * Send the response to the client.
	 * @param code is the code of the response.
	 */
	public void send(int code) {
		
		var res = new JSONObject();
		res.put("success", success(code) );
		res.put("errors", errors.entrySet().stream()
		    .map(error -> new JSONObject()
		        .put("error", error.getKey())
		        .put("description", error.getValue()))
		    .collect(Collectors.toList()));


		res.put("warnings", warnings.entrySet().stream()
		    .map(warning -> new JSONObject()
		        .put("warning", warning.getKey())
		        .put("description", warning.getValue()))
		    .collect(Collectors.toList()));
		
		
		
		/* Data field with all result (as object and not as a list */
		var data = new JSONObject();
		for(var param : result) {
			data.put(param.name(), param.value());
		}
		res.put("data", data);
		
		
		// Send response to client
		var response = res.toString();	
		
		try {
			statement.setInt(3, code);
			statement.setBoolean(4, success(code));
			statement.setString(7, response);
		} catch (SQLException e) {}

        try {
			exchange.sendResponseHeaders(code, response.getBytes().length);

			exchange.getResponseBody().write(response.getBytes());
		} catch (IOException e) {}
        exchange.close();
        
		try {
			statement.executeUpdate();
			statement.close();
		} catch (SQLException e) {}
		
		app.close(this.db);
        over = true;
	}
	

	/**
	 * Check if the response is closed.
	 * @return true if the response is closed, false otherwise.
	 */
	public boolean isClosed() { return over; }

		
	
	
}
