package backend.api.app.core;
import java.util.List;

import backend.api.app.Library;
import backend.api.app.Module;
import backend.api.app.core.auth.Auth;
import backend.api.app.core.permission.Permission;
import backend.api.app.core.route.Route;
import backend.api.app.core.setup.Setup;

public record Core() implements Module {

	@Override
	public String description() { return "The main module of the API application"; }

	@Override
	public String version() { return "1.0"; }

	@Override
	public String author() { return "Max Ducoudré"; }

	@Override
	public String url() { return "github.com/Flamby000"; }

	@Override
	public List<Library> libraries() {
		return List.of(
			new Setup(),
			new Permission(),
			new Route(),
			new Auth()
		);		
	}
	


}
