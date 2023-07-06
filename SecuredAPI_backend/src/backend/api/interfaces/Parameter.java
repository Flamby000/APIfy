package backend.api.interfaces;

import java.util.Objects;

public record Parameter(String name, String type, String description, boolean must) {
	public Parameter {
		Objects.requireNonNull(name);
		if(name.isEmpty()) throw new IllegalArgumentException("'name' cannot empty be empty in parameters");
		
		Objects.requireNonNull(type);
		if(type.isEmpty()) throw new IllegalArgumentException("'type' cannot empty be empty in parameters");
		
		Objects.requireNonNull(description);
		if(description.isEmpty()) throw new IllegalArgumentException("'description' cannot empty be empty in parameters");
	}
	

}
