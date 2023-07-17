package backend.api.module.setup;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
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
			}
			statement.close();

			// TODO : get role of Application.SUPER_ADMIN and apply it to every actions

			module.libraries().forEach((library) -> 
			{
				// Check if library.name() is already present in database
				try {
				    var libStatement = db.prepareStatement(String.format("SELECT * FROM %slibrary WHERE library_id = ?;", app.prefix()));
					libStatement.setString(1, library.name());
					var libResult = libStatement.executeQuery();

					// if there is no library, insert
					if(!libResult.next()) {
						libStatement.close();
						libStatement = db.prepareStatement(String.format("INSERT INTO %slibrary (library_id, module_id, description, version, author, author_url) VALUES (?, ?, ?, ?, ?, ?);", app.prefix()));
						libStatement.setString(1, library.name());
						libStatement.setString(2, id);
						libStatement.setString(3, library.description());
						libStatement.setString(4, library.version());
						libStatement.setString(5, library.author());
						libStatement.setString(6, library.url());
						libStatement.executeUpdate();
					} else {
						libStatement.close();
						libStatement = db.prepareStatement(String.format("UPDATE %slibrary SET description = ?, version = ?, author = ?, author_url = ?, updated_at = ? WHERE library_id = ?;", app.prefix()));
						libStatement.setString(1, library.description());
						libStatement.setString(2, library.version());
						libStatement.setString(3, library.author());
						libStatement.setString(4, library.url());
						libStatement.setTimestamp(5, new java.sql.Timestamp(System.currentTimeMillis()));
						libStatement.setString(6, library.name());
						libStatement.executeUpdate();
					}

					libStatement.close();

					
					library.actions().forEach((action) -> {
						//	action_id	library_id	description	is_guest_action	updated_at	

						// Check if action.name() is already present in database
						try {
							var actionStatement = db.prepareStatement(String.format("SELECT * FROM %saction WHERE action_id = ?;", app.prefix()));
							actionStatement.setString(1, action.name());
							var actionResult = actionStatement.executeQuery();

							// if there is no action, insert
							if(!actionResult.next()) {
								actionStatement.close();
								actionStatement = db.prepareStatement(String.format("INSERT INTO %saction (action_id, library_id, description, is_guest_action) VALUES (?, ?, ?, ?);", app.prefix()));
								actionStatement.setString(1, action.name());
								actionStatement.setString(2, library.name());
								actionStatement.setString(3, action.description());
								actionStatement.setBoolean(4, action.isGuestAction());
								actionStatement.executeUpdate();
							} else {
								actionStatement.close();
								actionStatement = db.prepareStatement(String.format("UPDATE %saction SET description = ?, is_guest_action = ?, updated_at = ? WHERE action_id = ?;", app.prefix()));
								actionStatement.setString(1, action.description());
								actionStatement.setBoolean(2, action.isGuestAction());
								actionStatement.setTimestamp(3, new java.sql.Timestamp(System.currentTimeMillis()));
								actionStatement.setString(4, action.name());
								actionStatement.executeUpdate();

							} 
							actionStatement.close();
						} catch (SQLException e) {
							e.printStackTrace();
							res.appendError("set_action_failed", e.getMessage());
							res.send(500);
							return;
						}
					});
					
					
				} catch (SQLException e) {
					e.printStackTrace();
					res.appendError("set_library_failed", e.getMessage());
					res.send(500);
					return;
				}

			});


		} catch(Exception e) {
			res.appendError("set_module_failed", e.getMessage());
			e.printStackTrace();
			res.send(500);
			return;
		}

		
		res.send(200);
	}
	
	
		
}
