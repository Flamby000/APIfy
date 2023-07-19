package backend.api.module.setup;

import java.io.IOException;
import java.sql.Connection;
import java.util.List;

import org.json.JSONObject;

import backend.api.endpoint.ResponseData;
import backend.api.interfaces.Action;
import backend.api.interfaces.Application;
import backend.api.interfaces.Parameter;

public record isSetup() implements Action {


	@Override
	public String description() {return "Check if the database is setup";}
	
	@Override
	public List<String> methods() { return List.of("GET");}
	
	@Override
	public boolean isGuestAction() { return true; }
	
	@Override
	public void execute(Application app, ResponseData res, List<Parameter<?>> params, Connection db, String id, String method,JSONObject patchFields) throws IOException {
		
		if(SetupDB.isDBSetUp(db, app)) {
			res.addString("message", "The database is correctly set up");
			res.send(200);
			return;
		} else {
			res.err("db_not_setup", "The database is not set up");
			res.send(404);
		}
	}
	
	
	
	
}
