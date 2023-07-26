package backend.api.app.core.route;

import java.util.List;

import backend.api.app.Action;
import backend.api.app.Library;

public record Route() implements Library{
	@Override
	public String description() { return "Used to mange routes of the api"; }

	@Override
	public String version() { return "1.0"; }

	@Override
	public String author() { return "Max Ducoudré"; }

	@Override
	public String url() { return "github.com/Flamby000"; }

	@Override
	public List<Action> actions() {
		return List.of(
			new backend.api.app.core.route.Module(),
			new backend.api.app.core.route.Action()
			);
	}

}
