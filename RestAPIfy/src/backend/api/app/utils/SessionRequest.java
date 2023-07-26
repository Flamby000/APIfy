package backend.api.app.utils;

import java.sql.Connection;
import java.sql.Date;
import java.util.Map;

import backend.api.app.Application;

/**
 * SessionRequest class represents a session request.
 */
public record SessionRequest(
		Connection db, 
		Application app, 
		int requestId,
		String sessionId, 
		String actionId,
		int code,
		boolean success,
		String method,
		String inParameters,
		String outParameters,
		Date creationDate
		) {
	
	
	/**
	 * Convert the session request to a map.
	 * @return the map of the session request.
	 */
	public Map<String, String> toMap() {
		return Map.of(
			"request_id", String.valueOf(requestId),
			"session_id", sessionId,
			"action_id", actionId,
			"code", String.valueOf(code),
			"success", String.valueOf(success),
			"method", method,
			"in_parameters", inParameters.toString(),
			"out_parameters", outParameters.toString(),
			"creation_date", creationDate.toString()
		);
	}
}
