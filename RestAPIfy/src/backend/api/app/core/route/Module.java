package backend.api.app.core.route;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import backend.api.app.Action;
import backend.api.app.Application;
import backend.api.app.Method;
import backend.api.app.RequestData;
import backend.api.app.ResponseData;

public record Module() implements Action {
	@Override
	public String description() {return "Check if the database is setup";}

	@Override
	public boolean isGuestAction() { return true;}
	
	@Override
	public Map<String, String> methodsDoc() {
		return Map.of(
			Method.GET, "Get the modules data",
			Method.PATCH, "Modify a module"
		);
	}
	
	
	@Override
	public void get(Application app, ResponseData res, Connection db, String id, String token){
		if(id.equals(RequestData.INVALID)) {
			// Get all modules
			try {
				
				var modulesInst = new JSONArray();
				
				app.modules().forEach((module) -> {
					var json = new JSONObject();
					json.put("module_id", module.name());
					json.put("module_description", module.description());
					json.put("module_version", module.version());
					json.put("module_author", module.author());
					json.put("module_author_url", module.url());
					//json.put("module_updated_at", module.updatedAt());
					modulesInst.put(json);
				});

				// Check in db if the module is setup
				var modulesDB = new JSONArray();
				var statement = db.prepareStatement(String.format("SELECT * FROM %smodule;", app.prefix()));
				var result = statement.executeQuery();
				while(result.next()) {
					var json = new JSONObject();
					json.put("module_id", result.getString("module_id"));
					json.put("module_description", result.getString("module_description"));
					json.put("module_version", result.getString("module_version"));
					json.put("module_author", result.getString("module_author"));
					json.put("module_author_url", result.getString("module_author_url"));
					json.put("module_updated_at", result.getTimestamp("module_updated_at"));
					json.put("module_installed_at", result.getTimestamp("module_installed_at"));
					modulesDB.put(json);
				}

				var modules = new ArrayList<JSONObject>();
				// for(int i = 0; i < modulesInst.length(); i++) {
				// 	var module = modulesInst.getJSONObject(i);
				// 	var moduleDB = modulesDB.toList().stream().filter((obj) -> ((Map<?, ?>)obj).get("module_id").equals(module.getString("module_id"))).findFirst().orElse(null);
				// 	if(moduleDB == null) {
				// 		module.put("is_setup", false);
				// 	} else {
				// 		module.put("is_setup", true);
				// 		module.put("module_updated_at", ((Map<?, ?>)moduleDB).get("module_updated_at"));
				// 		module.put("module_installed_at", ((Map<?, ?>)moduleDB).get("module_installed_at"));
				// 	}
				// 	modules.put(module);
				// }

				for(int i = 0; i < modulesInst.length(); i++) {
					var moduleDB = modulesDB.getJSONObject(i);
					var module = modulesInst.toList().stream().filter((obj) -> ((Map<?, ?>)obj).get("module_id").equals(moduleDB.getString("module_id"))).findFirst().orElse(null);
					if(module == null) {
						moduleDB.put("is_setup", false);
					} else {
						moduleDB.put("is_setup", true);
						moduleDB.put("module_description", ((Map<?, ?>)module).get("module_description"));
						moduleDB.put("module_version", ((Map<?, ?>)module).get("module_version"));
						moduleDB.put("module_author", ((Map<?, ?>)module).get("module_author"));
						moduleDB.put("module_author_url", ((Map<?, ?>)module).get("module_author_url"));
					}
					modules.add(moduleDB);
				}
					
				res.addList("modules", modules);
				res.send(200);
				return;
			} catch(Exception e) {
				res.appendError("get_modules_failed", e.getMessage());
				res.send(500);
				return;
			}
		
		} else {
			if(RequestData.requireId(res, id)) return;
			
			try {
				var statement = db.prepareStatement(String.format("SELECT * FROM %smodule WHERE module_id = ?;", app.prefix()));
				statement.setString(1, id);
				var result = statement.executeQuery();
				if(!result.next()) {
					res.appendError("module_not_found", "The module " + id + " is not found");
					res.send(404);
					return;
				}
				// Append result to res 
				for(int i = 1; i <= result.getMetaData().getColumnCount(); i++) {
					res.addString(result.getMetaData().getColumnName(i), result.getString(i));
				}
				statement.close();

				res.send(200);
				return;
				
			} catch(Exception e) {
				res.appendError("module_not_found", "The module " + id + " was not found");
				res.send(404);
				return;			
				}
		}
	}
	
	
	
	@Override
	public void patch(Application app, ResponseData res, Connection db, JSONObject patchFields, String id, String token) {

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
				statement = db.prepareStatement(String.format("INSERT INTO %smodule (module_id, module_description, module_version, module_author, module_author_url) VALUES (?, ?, ?, ?, ?);", app.prefix()));
				statement.setString(1, id);
				statement.setString(2, module.description());
				statement.setString(3, module.version());
				statement.setString(4, module.author());
				statement.setString(5, module.url());
				statement.executeUpdate();
			} else {
				statement.close();

				// Update the module
				statement = db.prepareStatement(String.format("UPDATE %smodule SET module_description = ?, module_version = ?, module_author = ?, module_author_url = ?, module_updated_at = ? WHERE module_id = ?;", app.prefix()));
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
						libStatement = db.prepareStatement(String.format("INSERT INTO %slibrary (library_id, module_id, library_description, library_version, library_author, library_author_url) VALUES (?, ?, ?, ?, ?, ?);", app.prefix()));
						libStatement.setString(1, library.name());
						libStatement.setString(2, id);
						libStatement.setString(3, library.description());
						libStatement.setString(4, library.version());
						libStatement.setString(5, library.author());
						libStatement.setString(6, library.url());
						libStatement.executeUpdate();
					} else {
						libStatement.close();
						libStatement = db.prepareStatement(String.format("UPDATE %slibrary SET library_description = ?, library_version = ?, library_author = ?, library_author_url = ?, library_updated_at = ? WHERE library_id = ?;", app.prefix()));
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
								actionStatement = db.prepareStatement(String.format("INSERT INTO %saction (action_id, library_id, action_description, is_guest_action) VALUES (?, ?, ?, ?);", app.prefix()));
								actionStatement.setString(1, action.name());
								actionStatement.setString(2, library.name());
								actionStatement.setString(3, action.description());
								actionStatement.setBoolean(4, action.isGuestAction());
								actionStatement.executeUpdate();
							} else {
								actionStatement.close();
								actionStatement = db.prepareStatement(String.format("UPDATE %saction SET action_description = ?, is_guest_action = ?, action_updated_at = ? WHERE action_id = ?;", app.prefix()));
								actionStatement.setString(1, action.description());
								actionStatement.setBoolean(2, action.isGuestAction());
								actionStatement.setTimestamp(3, new java.sql.Timestamp(System.currentTimeMillis()));
								actionStatement.setString(4, action.name());
								actionStatement.executeUpdate();
							} 
							actionStatement.close();
							
							action.methods().forEach((method) -> {
								try {
									// check if not exist
									var methodStatement = db.prepareStatement(String.format("SELECT * FROM %spermission WHERE action_id = ? AND method = ?;", app.prefix()));
									methodStatement.setString(1, action.name());
									methodStatement.setString(2, method);
									var methodResult = methodStatement.executeQuery();
									if(!methodResult.next()) {
										methodStatement.close();
										methodStatement = db.prepareStatement(String.format("INSERT INTO %spermission (action_id, method) VALUES (?, ?);", app.prefix()));
										methodStatement.setString(1, action.name());
										methodStatement.setString(2, method);
										methodStatement.executeUpdate();
										methodStatement.close();

									}
									

								}catch (SQLException e) {
									e.printStackTrace();
									res.appendError("set_method_failed", e.getMessage());
									res.send(500);
									return;
								}
							});
							
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
