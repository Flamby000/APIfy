package backend.api.interfaces;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.json.JSONObject;


public class Parameter<T> {
	private final String name;
	private final T value;
	private final Class<T> type ;
	private final String description;
	private final boolean must;
	
	public static String PATCH_FIELDS = "PATCH_FIELDS";

	public Parameter(Class<T> type, String name, T value, String description) {
		Objects.requireNonNull(name);
		Objects.requireNonNull(value);
		
		this.value = value;
		this.name = name;
		this.type = type;
		this.description = description;
		must = false;
	}
	

	public Parameter(Class<T> type, String name, T value) {
		Objects.requireNonNull(name);
		Objects.requireNonNull(value);
		
		this.value = value;
		this.name = name;
		this.type = type;

		this.description = null;
		must = false;
	}
	
	
	public Parameter(Class<T> type, String name, String description, T defaultValue, boolean must) {
		Objects.requireNonNull(name);
		Objects.requireNonNull(description);
		
		this.value = defaultValue;
		this.must = this.value == null ? false : must;
		this.name = name;
		this.type = type;

		this.description = description;
	}
	
	
	public static JSONObject patchParams() {
		return null;
	}
	

	public Map<String, String> toMap() {
		return Map.of("name", name, 
				"value", value == null ? "null" : value.toString(), 
				"description", description, 
				"must", String.valueOf(must),
				"type", type.getSimpleName()
				);
	}
	
	static public Parameter<?> find(List<Parameter<?>> params, String name) {
		return params.stream().filter((param) -> param.name().equals(name)).findFirst().orElse(null);
	}
	
	
	public String name() { return name; }
	public String description() { return description; }
	public boolean must() { return must; }
	@SuppressWarnings("rawtypes")
	public Class type() { return type; }
	public T value() { return value; }
	
	public String stringifyValue() {
		return value.toString();
	}

	@Override
	public String toString() {
		return String.format("Parameter [name=%s, value=%s, description=%s, must=%s]", name, value, description, must);
	}
	
}
