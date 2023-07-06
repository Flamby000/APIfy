package backend.api.interfaces;

import java.util.List;

public interface Module {
	default String name() { return this.getClass().getName(); }
	String desciption();
	String version();
	String author();
	String url();
    List<Library> libraries();
		
}
