package backend.api.interfaces;

import java.util.List;


public interface Library {
	default String name() { return this.getClass().getName(); }
	String desciption();
	String version();
	String author();
	String url();
	List<Action> actions();
	
}
