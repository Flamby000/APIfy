package backend.api.permission;

import java.sql.Connection;
import java.sql.Date;
import java.util.Objects;

import backend.api.interfaces.Action;
import backend.api.interfaces.Application;

public class Role {
	public static final String SUPERMAN_ROLE = "Superman";

	private final Connection db;
	private final Application app;

	private final String name;
	private final String description;
	private final Date creationDate;
	private final int id;


	
    public Role(Connection db, Application app, int id, String name, String description, Date creationDate) {
    	Objects.requireNonNull(db);
    	Objects.requireNonNull(app);
		this.db = db;
		this.app = app;
		this.name = name;
		this.description = description;
		this.creationDate = creationDate;
		this.id = id;
    }

	public boolean canPerform(Action action) {
		return false;
	}


	public String name() { return name; }
	public String description() { return description; }
	public Date creationDate() { return creationDate; }
	public int id() { return id; }
}
