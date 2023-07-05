package backend.api.interfaces;

import java.util.ArrayList;

public interface Module {
	String name();
	String description();
	String version();
	String author();
	String url();
	ArrayList<Library> libraries();
}
