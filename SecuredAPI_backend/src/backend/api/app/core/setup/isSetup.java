package backend.api.module.setup;

import java.sql.Connection;
import java.util.Map;

import backend.api.endpoint.ResponseData;
import backend.api.interfaces.Action;
import backend.api.interfaces.Application;
import backend.api.interfaces.Method;

public record isSetup() implements Action {


	@Override
	public String description() {return "Check if the database is setup";}
	

	
	@Override
	public Map<String, String> methodsDoc() {
		return Map.of(
			Method.GET, "Tell is the database is set up"
		);
	}
	
	@Override
	public boolean isGuestAction() { return true; }
	
	@Override
	public void get(Application app, ResponseData res, Connection db, String id, String token){
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
