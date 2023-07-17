package backend.api.module;
import java.util.List;

import backend.api.interfaces.Library;
import backend.api.interfaces.Module;
import backend.api.module.setup.Setup;

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
			new Setup()	
		);		
	}
	


}
