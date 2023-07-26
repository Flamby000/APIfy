package backend.api.app;

import java.util.List;
import java.util.Objects;


/**
 * Library interface represents a library of the application.
 */
public interface Library {

	/**
	 * Get the name of the library.
	 * @return the name of the library.
	 */
	default String name() { return this.getClass().getSimpleName(); }

	/**
	 * Get the description of the library.
	 * @return the description of the library.
	 */
	String description();

	/**
	 * Get the version of the library.
	 * @return the version of the library.
	 */
	String version();

	/**
	 * Get the author of the library.
	 * @return the author of the library.
	 */
	String author();

	/**
	 * Get the author url of the library.
	 * @return the author url of the library.
	 */
	String url();

	/**
	 * Get the list of the libarry's actions.
	 * @return the list of the library's actions.
	 */
	List<Action> actions();

	/**
	 * Get the action with the given name.
	 * @param name is the name of the action.
	 * @return the action with the given name.
	 */
	default Action getAction(String name) {
		Objects.requireNonNull(name);
		return actions().stream()
			.filter(action -> action.name().equals(name))
			.findFirst()
			.orElse(null);
	}
}
