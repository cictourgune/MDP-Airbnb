package org.tourgune.mdp.airbnb.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import org.tourgune.mdp.airbnb.config.Config;
import org.tourgune.mdp.airbnb.exception.ConfigSyntaxException;
import org.tourgune.mdp.airbnb.exception.DatabaseException;
import org.tourgune.mdp.airbnb.utils.Args;

public abstract class Database {
	
	protected String host;
	protected int port;
	protected String username;
	protected String password;
	
	protected String dbName;
	
	protected Connection conn;
	protected PreparedStatement lastExecutedStatement;
	
	public static String defaultHost = "127.0.0.1";
	public static int defaultPort = 3306;	// puerto por defecto de MySQL, aunque puede ser sobreescrito por subclases
	
	protected Database(String host, int port, String username, String password, String dbName) {
		Args.checkNotNull("Database username cannot be null", username);
		Args.checkNotNull("Database password cannot be null", password);
		Args.checkNotNull("Database name cannot be null", dbName);
		this.host = host == null ? getDefaultHost() : host;
		this.port = port == 0 ? getDefaultPort() : port;
		this.username = username;
		this.password = password;
		this.dbName = dbName;
		this.conn = null;
		this.lastExecutedStatement = null;
	}
	
	public static Database create() throws DatabaseException {
		Config conf = Config.getInstance();
		Map<String, String> params = conf.getParams("Database");
		String engine = params.get("engine");
		Args.checkNotNull("Config parameter 'engine' is mandatory", engine);	// si es null tirará NullPointerException
		
		if (engine.equalsIgnoreCase("mysql")) {		// si en el futuro añadimos más motores de base de datos, mejor usar un switch en vez de un if
			String host = params.get("host");
			int port = 0;
			String username = params.get("username");
			String password = params.get("password");
			String dbName = params.get("database");
			try {
				if (params.get("port") != null)
					port = Integer.parseInt(params.get("port"));
			} catch (NumberFormatException e) {
				throw new DatabaseException(new ConfigSyntaxException("Param 'port' must be an integer number"));
			}
			return new MySQLDatabase(host, port, username, password, dbName);
		} else
			throw new DatabaseException("Provided database engine ('" + engine + "') not supported");
	}
	
	public void close() throws SQLException {
		if (lastExecutedStatement != null)
			lastExecutedStatement.close();
		if (conn != null)
			conn.close();
	}
	
	protected final void closeLastStatementIfNeeded(PreparedStatement newPs) throws SQLException {
		if (lastExecutedStatement != null && !lastExecutedStatement.toString().equals(newPs.toString()))
			lastExecutedStatement.close();
		lastExecutedStatement = newPs;
	}
	
	protected abstract String getDefaultHost();
	protected abstract int getDefaultPort();
	
	public abstract void connect() throws SQLException;
	public abstract void disconnect() throws SQLException;
	public abstract int insert(InsertStatement i) throws SQLException;
	public abstract ResultSet insertAndGet(InsertStatement i) throws SQLException;
	public abstract ResultSet query(SelectStatement s) throws SQLException;
	public abstract SelectStatement newSelect();
	public abstract SelectStatement newSelect(String tableName);
	public abstract InsertStatement newInsert();
	public abstract InsertStatement newInsert(String tableName);
}
