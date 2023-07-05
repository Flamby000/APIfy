package backend.api.interfaces;

import backend.api.endpoint.RequestData;
import backend.api.endpoint.ResponseData;

public interface Action {
	String name();
	String description();
	ResponseData execute(RequestData data);

}
