package backend.api.interfaces;

import java.util.ArrayList;


public interface Library {
	String name();
	String description();
	String version();
	String author();
	String url();
	ArrayList<Action> actions();
	
}
