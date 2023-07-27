package backend.api;

import java.util.List;

import backend.api.app.ApiServer;

public class Main {

	public static void main(String[] args) {
		ApiServer.start(List.of());
	}

}
