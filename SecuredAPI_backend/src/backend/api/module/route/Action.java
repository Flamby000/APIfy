package backend.api.module.route;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

import backend.api.endpoint.RequestData;
import backend.api.endpoint.ResponseData;
import backend.api.interfaces.Application;
import backend.api.interfaces.Method;

public record Action() implements backend.api.interfaces.Action {

	@Override
	public String description() {return "Handle action data";}

	
	@Override
	public Map<String, String> methodsDoc() {
		return Map.of(
			Method.GET, "Get the action data"
		);
	}
	
	@Override
	public void get(Application app, ResponseData res, Connection db, String id, String token) {
		if(!id.equals(RequestData.INVALID)) {
			try {
				var action = backend.api.permission.Action.action(db, app, id);

				if(action == null) {
					res.err("action_not_found", "The action with the given id was not found");
					res.send(404);
				} else {
					res.addMap("action", action.toMap(app));

					// Add the role affected by this action
					var roles = action.roles(db, app);
					res.addArray("roles", roles.stream().map(role -> role.toMap(db, app, action.name())).toList());
					
					
					// module with field name() = action.module()  (stream API)
					var actionInst = app.modules()
									.stream()
									.filter(
										m -> m.name()
												.equals(action.module())
											)
										.findFirst()
										.orElse(null)
										.libraries().stream().filter(
											l -> l.name()
													.equals(action.library())
												)
												.findFirst()
												.orElse(null)
												.actions().stream().filter(
													a -> a.name()
															.equals(action.name())
														)
														.findFirst()
														.orElse(null);

					res.addBool("is_guest_action", actionInst.isGuestAction());
					res.addMap("methods", actionInst.methodsDoc());
					res.addArray("parameters", actionInst.parameters().stream().map(param -> param.toMap()).toList());

					res.addArray("patchable_fields", actionInst.patchableFields());
					res.addArray("delete_parameters", actionInst.deleteParameters().stream().map(param -> param.toMap()).toList());
					
					// Get history of this action was called
					var history = backend.api.permission.Action.history(db, app, action.name());
					res.addArray("history", history.stream().map(h -> h.toMap()).toList());
					
					res.send(200);
				}
			} catch (Exception e) {
				res.err("get_action_failed", e.getMessage());
				res.send(500);
			}

		} else {
			try {
				res.addArray("actions", backend.api.permission.Action.actions(db, app).stream().map(action -> action.toMap(app)).toList());
				res.send(200);
			} catch (SQLException e) {
				res.err("get_actions_failed", e.getMessage());
				res.send(500);
			}
		}
		

	}



	
}
