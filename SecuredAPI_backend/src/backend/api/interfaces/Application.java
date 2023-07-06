package backend.api.interfaces;

import java.util.List;

import backend.api.module.Core;

public class Application {
	private final List<Module> modules;
	
	
	public Application() {
		modules = List.of(
			new Core()
		);
	}

	public List<Module> modules() { return modules; }
	public String name() { return "api"; }
	
	
	public Module getModule(String name) {
		return modules.stream()
			.filter(module -> module.name().equals(name))
			.findFirst()
			.orElse(null);
	}
}
