## RestAPIfy

--- 
#### What is it ?
RestAPIfy is a simple java webserver that allows you to create a REST API endpoints and route in few lines of code.

---
#### How to use it ?
Very simple, add in your java dependency project the `build/RestAPIfy.jar` file and the file `build/config.json` to configure your database connection.



--- 
#### Develop your java routes
To develop your own endpoints in java, you have to create an object implementing `Module` interface. This module will contain objects implementing `Library` interface and this library will contain objects implementing `Action` interface.

An `Action` contain the code that will be executed when the endpoint is called by the client. You will be able to handle the HTTP method, parameters and send back data with the selected return code.


Here, an example of a simple action :
```java
public class Main {
    public static void main(String[] args) {
        var myModule = new MyModule(
            "My module description",
            "1.0.0",
            "Max Ducoudré",
            "https://github.com/Flamby000");

    	ApiServer.start(List.of(myModule));
    }

    public record MyModule(String description, String version, String author, String url) implements Module {
        @Override
        public List<Library> libraries() {
            return List.of(
                new MyLibrary("An example of library", version, author, url)
            );		
        }
    }

    public record MyLibrary(String description, String version, String author, String url) implements Library {
        @Override
        public List<Action> actions() {
            return List.of(
                new MyAction("This is an example of action")
            );
        }
    }

    public record MyAction(String description) implements Action {

        /* isGuestAction return true to avoir authentication check */
        @Override
        public boolean isGuestAction() { return false;}

        /* List the possible HTTP methods available with the action */
        @Override
        public Map<String, String> methodsDoc() {
            return Map.of(
                Method.POST, "Create something",
                Method.GET, "Get something",
                Method.DELETE, "Delete something",
                Method.PATCH, "Patch something",
                Method.PUT, "Put something"
            );
        }

        /* If the method is in the list of methodsDoc, you can override theses functions */

        /* Handle POST method */
        @Override
        public  void post(Application app, ResponseData response, Connection db, List<Parameter<?>> params, String token) {
            response.send(200);
        };

        /* Handle PATCH method */
        @Override
        public void patch(Application app, ResponseData response, Connection db, JSONObject patchFields, String id, String token) {
            response.send(200);
        };

        /* Handle GET method */
        @Override
        public void get(Application app, ResponseData response, Connection db, String id, String token){
            response.send(200);
        };

        /* Handle DELETE method */
        @Override
        public void delete(Application app, ResponseData response, Connection db, List<Parameter<?>> deleteFields, String id, String token) {
            response.send(200);
        };

        /* Handle PUT method */
        @Override
        public void put(Application app, ResponseData response, Connection db, List<Parameter<?>> params, String token){
            response.send(200);
        };
    }
}
```
The response parameter is a `ResponseData` object that allow you to send back data to the client. You can send back the code with the `send` method and append data, errors and warnings with `append`, `warn` and `err` methods.

The parameter `token` is the token of the user that made the request.
The parameter `id` is the id of the object that you want to get, patch or delete.

The parameter `params` is a list of `Parameter` object that contains the parameters of the request. You can get it with the static method `Parameter.find(List<Parameter<?>> params, String name)`.

The parameter `db` is the connection to the database taht you can use it to make SQL queries.

The parameter `app` is the application object that contains the configuration of the application and database table prefix with `app.prefix()` method.


--- 
#### The database
The database is used to manage the users, their sessions, the logs and the permissions. You have to configure the database authentication in the `config.json` file.


--- 
#### APIManager javascript object 
The APIManager javascript object (in `AdminPanel/js/api.js` file) is a simple object that allow you to make request to the API easily.

Here examples of how to use it to call with the fake `MyModule/MyLibrary/User` action :
```js
    let response = await APIManager.request("Mymodule", "MyLibrary", "User", false, "POST", {id : 1, name: "Dupont", age: 42});

    let response = await APIManager.request("MyModule", "MyLibrary", "User", 1, "GET");

    let response = await APIManager.request("MyModule", "MyLibrary", "User", 1, "PATCH", {name: "Dupont", age: 42});

    let response = await APIManager.request("MyModule", "MyLibrary", "User", 1, "DELETE");
```

The APIManager object also help you to manage the user sessions with multiples functions :
```js
    async function login(login, password, remember_days = 1);
    async function register(email, username = "NULL", password, passwordConfirm, firstname = "NULL", lastname = "NULL", phone = "NULL");
    function logout();
```


---
#### Author 
- Max Ducoudré