package backend.api.interfaces;

import java.util.List;


public interface Library {
	default String name() { return this.getClass().getSimpleName(); }
	String description();
	String version();
	String author();
	String url();
	List<Action> actions();
	default Action getAction(String name) {
		return actions().stream()
			.filter(action -> action.name().equals(name))
			.findFirst()
			.orElse(null);
	}
}
