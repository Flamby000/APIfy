package backend.api.interfaces;

import java.io.IOException;
import java.util.List;

import backend.api.endpoint.ResponseData;

public interface Action {
	default String name() { return this.getClass().getSimpleName(); }
	default boolean isGuestAction() { return false; }
	default String method() { return "POST"; }

	
	String description();
	List<Parameter<?>> parameters();
	
	
	
	void execute(Application app, ResponseData response, List<Parameter<?>> params, String id) throws IOException;
}
