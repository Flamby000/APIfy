package backend.api.interfaces;

import java.io.IOException;
import java.util.List;

import backend.api.endpoint.ResponseData;

public interface Action {
	default String name() { return this.getClass().getSimpleName(); }
	String description();
	default boolean isGuestAction() { return false; }
	default String method() { return "POST"; }
	List<Parameter<?>> parameters();
	void execute(ResponseData response, Application app, String id) throws IOException;
}
