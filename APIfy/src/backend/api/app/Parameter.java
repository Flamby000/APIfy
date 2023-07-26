package backend.api.app;

import java.util.List;
import java.util.Map;
import java.util.Objects;


/**
 * Parameter class represents a parameter of an action.
 * @param <T> is the type of the parameter.
 */
public class Parameter<T> {

	/**
	 * The name of the parameter.
	 */
	private final String name;

	/**
	 * The value (of default value) of the parameter.
	 */
	private final T value;

	/**
	 * The type of the value of the parameter.
	 */
	private final Class<T> type ;

	/**
	 * The description of the parameter.
	 */
	private final String description;

	/**
	 * True if the parameter is mandatory, false otherwise.
	 */
	private final boolean must;
	

	//public static String PATCH_FIELDS = "PATCH_FIELDS";


	/**
	 * The constructor of the parameter.
	 * @param type is the type of the parameter.
	 * @param name is the name of the parameter.
	 * @param value is the value of the parameter.
	 * @param description is the description of the parameter.
	 * @param must is true if the parameter is mandatory, false otherwise.
	 */
	public Parameter(Class<T> type, String name, T value, String description) {
		Objects.requireNonNull(name);
		Objects.requireNonNull(value);
		
		this.value = value;
		this.name = name;
		this.type = type;
		this.description = description;
		must = false;
	}
	
	/**
	 * The constructor of the parameter.
	 * @param type is the type of the parameter.
	 * @param name is the name of the parameter.
	 * @param value is the value of the parameter.
	 */
	public Parameter(Class<T> type, String name, T value) {
		Objects.requireNonNull(name);
		Objects.requireNonNull(value);
		
		this.value = value;
		this.name = name;
		this.type = type;

		this.description = null;
		must = false;
	}
	
	
	/**
	 * The constructor of the parameter.
	 * @param type is the type of the parameter.
	 * @param name is the name of the parameter.
	 * @param description is the description of the parameter.
	 * @param defaultValue is the default value of the parameter.
	 * @param must is true if the parameter is mandatory, false otherwise.
	 */
	public Parameter(Class<T> type, String name, String description, T defaultValue, boolean must) {
		Objects.requireNonNull(name);
		Objects.requireNonNull(description);
		
		this.value = defaultValue;
		this.must = this.value == null ? false : must;
		this.name = name;
		this.type = type;

		this.description = description;
	}
	
	/**
	 * Convert the parameter to a map.
	 * @return the map of the parameter.
	 */
	public Map<String, String> toMap() {
		return Map.of("name", name, 
				"value", value == null ? "null" : value.toString(), 
				"description", description, 
				"must", String.valueOf(must),
				"type", type.getSimpleName()
				);
	}
	

	/**
	 * Find the specified parameter in the list of parameters.
	 * @param params is the list of parameters.
	 * @param name is the name of the parameter to find.
	 * @return the parameter with the given name.
	 */
	static public Parameter<?> find(List<Parameter<?>> params, String name) {
		return params.stream().filter((param) -> param.name().equals(name)).findFirst().orElse(null);
	}
	
	
	/**
	 * Get the name of the parameter.
	 * @return the name of the parameter.
	 */
	public String name() { return name; }

	/**
	 * Get the description of the parameter.
	 * @return the description of the parameter.
	 */
	public String description() { return description; }

	/**
	 * Check if the parameter is mandatory.
	 * @return true if the parameter is mandatory, false otherwise.
	 */
	public boolean must() { return must; }

	/**
	 * Get the type of the parameter.
	 * @return the type of the parameter.
	 */
	@SuppressWarnings("rawtypes")
	public Class type() { return type; }

	/**
	 * Get the value of the parameter.
	 * @return the value of the parameter.
	 */
	public T value() { return value; }
	

	/**
	 * Get the value of the parameter as a string.
	 * @return the value of the parameter as a string.
	 */
	public String stringifyValue() {
		return value.toString();
	}

	/**
	 * Stringify the parameter.
	 * @return The stringified the parameter 
	 */
	@Override
	public String toString() {
		return String.format("Parameter [name=%s, value=%s, description=%s, must=%s]", name, value, description, must);
	}
	
}
