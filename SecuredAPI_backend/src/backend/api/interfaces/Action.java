package backend.api.interfaces;

import java.io.IOException;
import java.sql.Connection;
import java.util.List;

import backend.api.endpoint.ResponseData;

public interface Action {
	default String name() { return this.getClass().getSimpleName(); }
	default boolean isGuestAction() { return false; }
	default String method() { return "POST"; }

	
	String description();
	default List<Parameter<?>> parameters() {return List.of();}
	
	
	
	void execute(Application app, ResponseData response, List<Parameter<?>> params, Connection db, String id) throws IOException;
}
