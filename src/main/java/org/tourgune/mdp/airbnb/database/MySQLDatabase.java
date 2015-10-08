package org.tourgune.mdp.airbnb.database;

import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.tourgune.mdp.airbnb.exception.DatabaseException;
import org.tourgune.mdp.airbnb.utils.Args;
import org.tourgune.mdp.airbnb.utils.Constants;

public class MySQLDatabase extends Database {
	
	protected MySQLDatabase(String host, int port, String username, String password, String dbName) throws DatabaseException {
		super(host, port, username, password, dbName);
	}
	
	@Override
	public void connect() throws SQLException {
		conn = DriverManager.getConnection(Constants.MYSQL_JDBC_PREFIX + host + ":" + port + "/" + dbName + "?" + Constants.MYSQL_JDBC_PARAMS,
				username, password);
	}

	@Override
	public void disconnect() throws SQLException {
		Args.checkNotNull("Database connection is null. Did you call connect()?", conn);	// si 'conn' es null, tira NullPointerException
		conn.close();
	}

	@Override
	public int insert(InsertStatement i) throws SQLException {
		synchronized (this) {
			String sql = i.prepare();
			PreparedStatement ps = i.compile(sql);
			closeLastStatementIfNeeded(ps);
			return ps.executeUpdate();
		}
	}
	
	@Override
	public ResultSet insertAndGet(InsertStatement i) throws SQLException {
		synchronized (this) {
			String sql = i.prepare();
			PreparedStatement ps = i.compile(sql, new int[]{Statement.RETURN_GENERATED_KEYS});
			closeLastStatementIfNeeded(ps);
			ps.executeUpdate();
			return ps.getGeneratedKeys();
		}
	}

	@Override
	public ResultSet query(SelectStatement s) throws SQLException {
		synchronized (this) {
			String sql = s.prepare();
			PreparedStatement ps = s.compile(sql);
			closeLastStatementIfNeeded(ps);
			return ps.executeQuery();
		}
	}

	@Override
	public SelectStatement newSelect() {
		return newSelect(null);
	}
	@Override
	public SelectStatement newSelect(String tableName) {
		SelectStatement stat = new MySQLSelectStatement(tableName);
		Args.checkNotNull("Database connection is null. Did you call connect()?", conn);
		stat.setConnection(conn);
		return stat;
	}
	
	@Override
	public InsertStatement newInsert() {
		return newInsert(null);
	}
	@Override
	public InsertStatement newInsert(String tableName) {
		InsertStatement stat = new MySQLInsertStatement(tableName);
		Args.checkNotNull("Database connection is null. Did you call connect()?", conn);
		stat.setConnection(conn);
		return stat;
	}
	
	@Override
	protected String getDefaultHost() {
		return "127.0.0.1";
	}

	@Override
	protected int getDefaultPort() {
		return 3306;	// puerto por defecto de MySQL
	}
}
