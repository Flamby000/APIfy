package backend.api.app.core.auth;

import java.util.List;

import backend.api.app.Action;
import backend.api.app.Library;

public record Auth() implements Library {

	@Override
	public String description() { return "Used to authenticate users and sessions"; }

	@Override
	public String version() { return "1.0"; }

	@Override
	public String author() { return "Max Ducoudré"; }

	@Override
	public String url() { return "github.com/Flamby000"; }

	@Override
	public List<Action> actions() {
		return List.of(
			new Register(),
			new Login()
		);
	}

	

}
