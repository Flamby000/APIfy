package backend.api.interfaces;

import java.util.List;

import backend.api.endpoint.ResponseData;

public interface Action {
	default String name() { return this.getClass().getName(); }
	String description();
	default boolean isGuestAction() { return false; }
	default String method() { return "POST"; }
	List<Parameter> parameters();
	ResponseData execute(String id);
}
