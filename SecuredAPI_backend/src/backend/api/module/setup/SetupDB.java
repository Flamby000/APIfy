package backend.api.module.setup;
import java.io.IOException;
import java.util.List;

import backend.api.endpoint.ResponseData;
import backend.api.interfaces.Action;
import backend.api.interfaces.Application;
import backend.api.interfaces.Parameter;


public record SetupDB() implements Action {

	@Override
	public String description() {
		return "Set up the database for the installation";
	}
	
	@Override
	public List<Parameter<?>> parameters() {
		return List.of(
			new Parameter<>(String.class, "db_hostname", "The host name/url of the database", null, true),
			new Parameter<>(String.class, "db_name", "The name of the database to connect to", null, true),
			new Parameter<>(String.class, "db_username", "The user using the database with admin rights", null, false),
			new Parameter<>(String.class, "db_password", "The password of the user", null, true)
		);
	}
	
	
	@Override
	public boolean isGuestAction() { return true; }

	
	@Override
	public void execute(Application app, ResponseData response, List<Parameter<?>> params, String id) throws IOException {
		//response.appendResult(new Parameter<String>("message", "Request success !"));
		//response.appendResult(new Parameter<Integer>("valor", 1));
		
		//if(RequestData.requireId(response, id)) return;
		//var db = app.db();
		//var param =  params
		//System.out.println(param);

		response.send(200);
	}


}


	
