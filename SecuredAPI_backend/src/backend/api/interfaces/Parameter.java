package backend.api.interfaces;

import java.util.Objects;

public class Parameter<T> {
	private final String name;
	private final T value;
	private final String description;
	private final boolean must;
	
	public Parameter(String name, T value, String description) {
		Objects.requireNonNull(name);
		Objects.requireNonNull(value);
		
		this.value = value;
		this.name = name;
		this.description = description;
		must = false;
	}
	
	public Parameter(String name, T value) {
		Objects.requireNonNull(name);
		Objects.requireNonNull(value);
		
		this.value = value;
		this.name = name;
		this.description = null;
		must = false;
	}
	
	
	public Parameter(String name, String description, T defaultValue, boolean must) {
		Objects.requireNonNull(name);
		Objects.requireNonNull(description);
		
		this.value = defaultValue;
		this.must = must;
		this.name = name;
		this.description = description;
	}
	
	public String name() { return name; }
	public String description() { return description; }
	public boolean must() { return must; }
	
	
	public String stringifyValue() {
		return value.toString();
	}
}
