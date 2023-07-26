package backend.api.app;

import java.util.Objects;

/**
 * Method class represents the methods supported by the actions.
 */
public record Method() {

	/**
	 * The PUT method.
	 */
	public final static String PUT = "PUT";

	/**
	 * The POST method.
	 */
	public final static String POST = "POST";

	/**
	 * The GET method.
	 */
	public final static String GET = "GET";

	/**
	 * The HEAD method.
	 */
	public final static String HEAD = "HEAD";

	/**
	 * The DELETE method.
	 */
	public final static String DELETE = "DELETE";

	/**
	 * The PATCH method.
	 */
	public final static String PATCH = "PATCH";

	/**
	 * The OPTIONS method.
	 */
	public final static String OPTIONS = "OPTIONS";
	
	
	/**
	 * Check if the method exists.
	 * @param method is the method to check.
	 * @return true if the method exists, false otherwise.
	 */
	public static boolean exists(String method) {
		Objects.requireNonNull(method);
        return method.equals(PUT) || method.equals(POST) || method.equals(GET) || method.equals(HEAD) || method.equals(DELETE) || method.equals(PATCH) || method.equals(OPTIONS);
	}
	
	/**
	 * Check if the method needs parameters.
	 * @param method is the method to check.
	 * @return true if the method needs parameters, false otherwise.
	 */
	public static boolean needParameters(String method) {
		Objects.requireNonNull(method);
		return method.equals(PUT) || method.equals(POST) || method.equals(PATCH) || method.equals(DELETE);
	}
}
