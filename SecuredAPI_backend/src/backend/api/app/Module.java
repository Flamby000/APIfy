package backend.api.interfaces;

import java.util.List;
import java.util.Map;

public interface Module {
	default String name() { return this.getClass().getSimpleName(); }
	String description();
	String version();
	String author();
	String url();
    List<Library> libraries();
    
	default Library getLibrary(String name) {
		return libraries().stream()
			.filter(library -> library.name().equals(name))
			.findFirst()
			.orElse(null);
	}
	
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
