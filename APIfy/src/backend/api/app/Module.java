package backend.api.app;

import java.util.List;
import java.util.Map;
import java.util.Objects;


/**
 * Module interface represents a module of the application.
 */
public interface Module {

	/**
	 * Get the name of the module.
	 * @return the name of the module.
	 */
	default String name() { return this.getClass().getSimpleName(); }

	/**
	 * Get the description of the module.
	 * @return the description of the module.
	 */
	String description();

	/**
	 * Get the version of the module.
	 * @return the version of the module.
	 */
	String version();

	/**
	 * Get the author of the module.
	 * @return the author of the module.
	 */
	String author();

	/**
	 * Get the author url of the module.
	 * @return the author url of the module.
	 */
	String url();

	/**
	 * Get the list of the module's libraries.
	 * @return the list of the module's libraries.
	 */
    List<Library> libraries();
    

	/** 
	 * Get the library with the given name.
	 * @param name is the name of the library.
	 * @return the library with the given name.
	 */
	default Library getLibrary(String name) {
		Objects.requireNonNull(name);
		return libraries().stream()
			.filter(library -> library.name().equals(name))
			.findFirst()
			.orElse(null);
	}
	
	/**
	 * Convert the module to a map.
	 * @return the map of the module.
	 */
	default Map<String, String> toMap() {
		return Map.of(
			"module_id", name(),
			"module_description", description(),
			"module_version", version(),
			"module_author", author(),
			"module_author_url", url()
		);
	}
	
	
	
		
}
