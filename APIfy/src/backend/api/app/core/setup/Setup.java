package backend.api.app.core.setup;

import java.util.List;

import backend.api.app.Action;
import backend.api.app.Library;

public record Setup() implements Library{

	@Override
	public String description() { return "Used to setup the API and fill the database with default user and data"; }

	@Override
	public String version() { return "1.0"; }

	@Override
	public String author() { return "Max Ducoudré"; }

	@Override
	public String url() { return "github.com/Flamby000"; }

	@Override
	public List<Action> actions() {
		return List.of(
			new SetupDB(),
			new isSetup()
		);		
		
	}
	
}
