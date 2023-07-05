package backend.api.endpoint;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;


public class Endpoint {

	public static void main(String[] args) {
		// LISTEN POST ON LOCALHOST:3000
		var port = 3002;
		try (var serverSocket = new ServerSocket(port)) {
			System.out.println("Rest API start : localhost:" + port);
			while (true) {
				var clientSocket = serverSocket.accept();
				handleRequest(clientSocket);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
    private static void handleRequest(Socket clientSocket) {
		// Read JSON body
		try {
			InputStream input = clientSocket.getInputStream();
			byte[] buffer = input.readAllBytes();
			String request = new String(buffer);
			System.out.println(request);
		} catch (IOException e) {
			e.printStackTrace();
		}

		// Send response
		String response = "HTTP/1.1 200 OK\r\n\r\nHello World!";
		try {
			clientSocket.getOutputStream().write(response.getBytes("UTF-8"));
			clientSocket.getOutputStream().flush();
			clientSocket.getOutputStream().close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


}
