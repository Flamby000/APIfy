package backend.api.interfaces;

public record Method() {
	public final static String PUT = "PUT";
	public final static String POST = "POST";
	public final static String GET = "GET";
	public final static String HEAD = "HEAD";
	public final static String DELETE = "DELETE";
	public final static String PATCH = "PATCH";
	public final static String OPTIONS = "OPTIONS";
	
	
	public static boolean exists(String method) {
        return method.equals(PUT) || method.equals(POST) || method.equals(GET) || method.equals(HEAD) || method.equals(DELETE) || method.equals(PATCH) || method.equals(OPTIONS);
	}
	
	
	public static boolean needParameters(String method) {
		return method.equals(PUT) || method.equals(POST) || method.equals(PATCH);
	}
}
