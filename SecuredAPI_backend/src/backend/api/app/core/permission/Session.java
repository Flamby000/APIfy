package backend.api.module.permission;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Map;

import backend.api.endpoint.RequestData;
import backend.api.endpoint.ResponseData;
import backend.api.interfaces.Action;
import backend.api.interfaces.Application;
import backend.api.interfaces.Method;
import backend.api.permission.SessionRequest;

public record Session() implements Action {

    @Override
	public Map<String, String> methodsDoc() {
		return Map.of(
			Method.GET, "Get the session data"
		);
	}

    
	@Override
	public String description() {return "Get the session data";}

    @Override
    public void get(Application app, ResponseData res, Connection db, String id, String token) {
        if(!id.equals(RequestData.INVALID)) {    
            try {
                // Get the session data from %session table
                var statement = db.prepareStatement(String.format(""
                        + "SELECT session_id, session_created_at, email, user_id, "
                        + "(SELECT COUNT(*) FROM %srequest WHERE session_id = ?) AS request_count, "
                        // Last request of the session
                        + "(SELECT request_created_at FROM %srequest WHERE session_id = ? ORDER BY request_created_at DESC LIMIT 1) AS last_request_date "
                        + "FROM %ssession NATURAL JOIN %suser "
                        + "WHERE session_id = ?", app.prefix(), app.prefix(), app.prefix(), app.prefix()));
                statement.setString(1, id);
                statement.setString(2, id);
                statement.setString(3, id);

                var result = statement.executeQuery();
                if(result.next()) {
                    // Add the session data to the response
                    res.addMap("session", Map.of(
                        "session_id", result.getString("session_id"),
                        "session_created_at", result.getString("session_created_at"),
                        "email", result.getString("email"),
                        "user_id", result.getString("user_id"),
                        "request_count", result.getString("request_count"),
                        "last_request_date", result.getString("last_request_date")
                    ));
                    
                    var history = new ArrayList<SessionRequest>();
                    var historyStatement = db.prepareStatement(String.format(""
                            + "SELECT * "
                            + "FROM %srequest "
                            + "WHERE session_id = ? "
                            + "ORDER BY request_created_at DESC", app.prefix()));
                    historyStatement.setString(1, id);
                    var historyResult = historyStatement.executeQuery();
                    while(historyResult.next()) {


                        history.add(new SessionRequest(db, app,
                                historyResult.getInt("request_id"),
                                historyResult.getString("session_id"),
                                historyResult.getString("action_id"),
                                historyResult.getInt("code"),
                                historyResult.getBoolean("success"),
                                historyResult.getString("method"),
                                historyResult.getString("in_parameters"),
                                historyResult.getString("out_parameters"),
                                historyResult.getDate("request_created_at")  
                        ));
                    }

                    res.addArray("history", history.stream().map(h -> h.toMap()).toList());
                    
                    
                    res.send(200);
                } else {
                    res.err("session_not_found", "The session " + id + " doesn't exists");
                    res.send(404);
                }

            } catch(Exception e) {
                res.err("internal_error", e.getMessage());
                res.send(500);
            }

        } else {
            // Get all the sessions
            try {


                var statement = db.prepareStatement(String.format(""
                        + "SELECT session_id, session_created_at, email, user_id,"
                        + "(SELECT COUNT(*) FROM %srequest WHERE session_id = %ssession.session_id) AS request_count, "
                        + "(SELECT request_created_at FROM %srequest WHERE session_id = %ssession.session_id ORDER BY request_created_at DESC LIMIT 1) AS last_request_date "
                        + "FROM %ssession NATURAL JOIN %suser "
                        + "ORDER BY session_created_at DESC", app.prefix(), app.prefix(), app.prefix(), app.prefix(), app.prefix(), app.prefix()));
                var result = statement.executeQuery();
                var sessions = new ArrayList<Map<String, String>>();
                while(result.next()) {
                    sessions.add(Map.of(
                        "session_id", result.getString("session_id"),
                        "session_created_at", result.getString("session_created_at"),
                        "email", result.getString("email"),
                        "user_id", result.getString("user_id"),
                        "request_count", result.getString("request_count"),
                        "last_request_date", result.getString("last_request_date")
                    ));
                }
                res.addArray("sessions", sessions);
                res.send(200);
            } catch(Exception e) {
                res.err("internal_error", e.getMessage());
                res.send(500);
            }
        }   
    }
}
