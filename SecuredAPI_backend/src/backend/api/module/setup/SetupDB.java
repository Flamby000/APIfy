package backend.api.module.setup;
import java.util.List;

import backend.api.endpoint.ResponseData;
import backend.api.interfaces.Action;
import backend.api.interfaces.Parameter;


public record SetupDB() implements Action {

	@Override
	public String description() {
		return "Set up the database for the installation";
	}
	
	@Override
	public List<Parameter> parameters() {
		return List.of(
			new Parameter("db_hostname", "String", "The host name/url of the database", true),
			new Parameter("db_name", "String", "The name of the database to connect to", true),
			new Parameter("db_username", "String", "The user using the database with admin rights", true),
			new Parameter("db_password", "String", "The password of the user", true)
		);
	}
	
	@Override
	public boolean isGuestAction() { return true; }

	
	@Override
	public ResponseData execute(String id) {
		// TODO Auto-generated method stub
		return null;
	}


}


	
