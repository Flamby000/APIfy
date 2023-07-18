package backend.api.module.permission;

import java.util.List;

import backend.api.interfaces.Action;
import backend.api.interfaces.Library;

public record Permission() implements Library {

	@Override
	public String description() { return "Used to manage permissions : the users and their roles"; }

	@Override
	public String version() { return "1.0"; }

	@Override
	public String author() { return "Max Ducoudré"; }

	@Override
	public String url() { return "github.com/Flamby000"; }

	@Override
	public List<Action> actions() {
		return List.of(
			new User(),
			new Role(),
			new RoleAssign()
		);		
		
	}
	
}
