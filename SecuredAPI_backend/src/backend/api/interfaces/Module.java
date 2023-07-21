package backend.api.interfaces;

import java.util.List;

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
	
	
	
	
		
}
