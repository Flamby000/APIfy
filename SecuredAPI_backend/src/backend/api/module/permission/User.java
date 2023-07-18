package backend.api.module.permission;

import java.io.IOException;
import java.sql.Connection;
import java.util.List;
import java.util.stream.Collectors;

import backend.api.endpoint.RequestData;
import backend.api.endpoint.ResponseData;
import backend.api.interfaces.Action;
import backend.api.interfaces.Application;
import backend.api.interfaces.Method;
import backend.api.interfaces.Parameter;

public record User() implements Action {
	
	@Override
	public String description() {return "Manipulate user data";}
	
	@Override
	public List<String> methods() { return List.of(Method.GET, Method.DELETE);}
	
	@Override
	public void execute(Application app, ResponseData res, List<Parameter<?>> params, Connection db, String id, String method) throws IOException {
		if(method.equals(Method.GET)) {
			if(id == RequestData.INVALID) {
				res.addArray("users", backend.api.permission.User.users(db, app).stream().map(user -> user.toMap()).collect(Collectors.toList()));
				res.send(200);
				return;
				
			} else {
				/* Search "id" user */
				if(RequestData.requireId(res, id)) return;
				var user = backend.api.permission.User.user(db, app, Integer.valueOf(id));
				if(user != null) {
					res.addMap("user", user.toMap());
					res.addArray("roles", user.roles().stream().map(role -> role.toMap()).collect(Collectors.toList()));
					res.send(200);
					return;
				} else {
					res.appendError("not_found", "The user wasn't found");
					res.send(404);
					return;
				}
			}
		}else if(method.equals(Method.DELETE)) {
			
			//if(RequestData.requireId(res, id)) return;

			if(backend.api.permission.User.delete(db, app, id)) {
				res.addString("message", "User deleted");
				res.send(200);
				return;
			} else {
				res.appendError("not_found", "The user to delete wasn't found");
				res.send(404);
				return;
			}
		}
		res.appendError("unhandled_error", "");

		res.send(500);
	}	
}
