package backend.api.module.setup;

import java.io.IOException;
import java.sql.Connection;
import java.util.List;

import backend.api.endpoint.RequestData;
import backend.api.endpoint.ResponseData;
import backend.api.interfaces.Action;
import backend.api.interfaces.Application;
import backend.api.interfaces.Parameter;

public record UpdateModule() implements Action {

	@Override
	public String description() {return "Set up the database for the installation";}
	

	
	@Override 
	public String method() { return "POST"; } // TODO : Use PATCH instead of POST
	
	@Override
	public boolean isGuestAction() { return false; }

	
	@SuppressWarnings("resource")
	@Override
	public void execute(Application app, ResponseData res, List<Parameter<?>> params, Connection db, String id)
			throws IOException {
		RequestData.requireId(res, id);
		if(res.isClosed()) return;
		
		
		var modules = app.modules();
		// Get the module with module.name() == id in modules (null if not in list)
		var module = modules.stream().filter(m -> m.name().equals(id)).findFirst().orElse(null);
		if(module == null) {
			res.appendError("module_not_found", "The module " + id + " is not implemented");
			res.send(404);
			return;
		}
		
		// insert it on database is not already present
		try {
			var statement = db.prepareStatement(String.format("SELECT * FROM %smodule WHERE module_id = ?;", app.prefix()));
			statement.setString(1, id);
			var result = statement.executeQuery();
			if(!result.next()) {
				statement.close();

				// Insert the module
				statement = db.prepareStatement(String.format("INSERT INTO %smodule (module_id, description, version, author, author_url) VALUES (?, ?, ?, ?, ?);", app.prefix()));
				statement.setString(1, id);
				statement.setString(2, module.description());
				statement.setString(3, module.version());
				statement.setString(4, module.author());
				statement.setString(5, module.url());
				statement.executeUpdate();
				statement.close();
			} else {
				statement.close();

				// Update the module
				statement = db.prepareStatement(String.format("UPDATE %smodule SET description = ?, version = ?, author = ?, author_url = ?, updated_at = ? WHERE module_id = ?;", app.prefix()));
				statement.setString(1, module.description());
				statement.setString(2, module.version());
				statement.setString(3, module.author());
				statement.setString(4, module.url());
				statement.setTimestamp(5, new java.sql.Timestamp(System.currentTimeMillis()));
				statement.setString(6, id);
				statement.executeUpdate();
				statement.close();
			}
			


		} catch(Exception e) {
			res.appendError("set_module_failed", e.getMessage());
			e.printStackTrace();
			res.send(500);
			return;
		}

		




		
		

		res.send(200);
	}
	
	
		
}
