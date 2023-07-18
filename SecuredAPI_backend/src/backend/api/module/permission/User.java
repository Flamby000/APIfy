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
				
			} else {
				/* Search "id" user */
				res.send(501);
			}
		}else if(method.equals(Method.DELETE)) {
			
			res.send(501);
		}
	
		
	}	
}
