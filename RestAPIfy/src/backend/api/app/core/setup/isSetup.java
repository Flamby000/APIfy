package backend.api.app.core.setup;

import java.sql.Connection;
import java.util.Map;

import backend.api.app.Action;
import backend.api.app.Application;
import backend.api.app.Method;
import backend.api.app.ResponseData;

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
