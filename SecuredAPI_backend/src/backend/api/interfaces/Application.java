package backend.api.interfaces;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import backend.api.module.Core;





public class Application {
	private final List<Module> modules;
	
	
	public Application() {
		modules = List.of(
			new Core()
		);
	}

	public void openDBConnection() {
		/* Read in db.json file the object containing the connection parameters
		 * and open the connection.
		 */


	}
	
	public List<Module> modules() { return modules; }
	
	public String name() { return "api"; }
	
	
	public Module getModule(String name) {
		return modules.stream()
			.filter(module -> module.name().equals(name))
			.findFirst()
			.orElse(null);
	}
	

    private static Map<String, String> parseJson(String json) {
        Map<String, String> jsonData = new HashMap<>();

        int startIndex = json.indexOf("{") + 1;
        int endIndex = json.indexOf("}");
        String[] keyValuePairs = json.substring(startIndex, endIndex).split(",");

        for (String pair : keyValuePairs) {
            String[] keyValue = pair.split(":");
            String key = keyValue[0].replace("\"", "").trim();
            String value = keyValue[1].replace("\"", "").trim();
            jsonData.put(key, value);
        }

        return jsonData;
    }

	
}
