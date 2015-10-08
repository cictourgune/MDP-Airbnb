package org.tourgune.mdp.airbnb.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.tourgune.mdp.airbnb.database.Conditions.GlueType;
import org.tourgune.mdp.airbnb.database.Field.FieldType;
import org.tourgune.mdp.airbnb.exception.NotImplementedError;
import org.tourgune.mdp.airbnb.utils.Args;
import org.tourgune.mdp.airbnb.utils.Constants;

public abstract class InsertStatement implements DMLStatement {

	protected Connection conn;
	
	protected String table;
	protected List<Field> fields;
	
	protected boolean ignore;	// si es true, el SQL generado será "INSERT IGNORE ...", y si no, simplemente "INSERT ..."
	
	public InsertStatement(String table) {
		this.table = table;
		this.fields = new ArrayList<Field>();
		ignore = false;
	}
	
	public Connection getConnection() {
		return conn;
	}

	public void setConnection(Connection conn) {
		this.conn = conn;
	}

	public void ignore() {
		ignore = true;
	}
	public void notIgnore() {
		ignore = false;
	}
	
	@Override
	public DMLStatement alias(String alias) {
		throw new NotImplementedError(Constants.DB_MSG_NOT_IMPLEMENTED_INSERT);
	}
	
	@Override
	public DMLStatement alias(String fname, String alias) {
		throw new NotImplementedError(Constants.DB_MSG_NOT_IMPLEMENTED_INSERT);
	}
	
	@Override
	public DMLStatement field(String fname) {
		throw new NotImplementedError();
	}

	@Override
	public DMLStatement field(String fname, int fvalue) {
		Field f = new Field(false);
		f.setTableName(table);
		f.setFieldName(fname);
		f.setFieldType(FieldType.INTEGER);
		f.setValue((Integer) fvalue);
		fields.add(f);
		return this;
	}
	
	@Override
	public DMLStatement field(String fname, float fvalue) {
		Field f = new Field(false);
		f.setTableName(table);
		f.setFieldName(fname);
		f.setFieldType(FieldType.FLOAT);
		f.setValue((Float) fvalue);
		fields.add(f);
		return this;
	}
	
	@Override
	public DMLStatement field(String fname, double fvalue) {
		Field f = new Field(false);
		f.setTableName(table);
		f.setFieldName(fname);
		f.setFieldType(FieldType.DOUBLE);
		f.setValue((Double) fvalue);
		fields.add(f);
		return this;
	}
	
	@Override
	public DMLStatement field(String fname, String fvalue) {
		Field f = new Field(false);
		f.setTableName(table);
		f.setFieldName(fname);
		f.setFieldType(FieldType.STRING);
		f.setValue((Object) fvalue);
		fields.add(f);
		return this;
	}
	
	@Override
	public DMLStatement field(String fname, Functions func) {
		Field f = new Field(true);
		f.setTableName(table);
		f.setFieldName(fname);
		f.setValue((Object) func);
		fields.add(f);
		return this;
	}
	
	public DMLStatement storedProcedure(String rawSql) {
		Field f = new Field(true);
		f.setValue((Object) rawSql);
		fields.add(f);
		return this;
	}
	
	@Override
	public DMLStatement where(String fname, Condition cond) {
		throw new NotImplementedError(Constants.DB_MSG_NOT_IMPLEMENTED_INSERT);
	}
	@Override
	public DMLStatement where(String table, String fname, Condition cond) {
		throw new NotImplementedError(Constants.DB_MSG_NOT_IMPLEMENTED_INSERT);
	}
	@Override
	public DMLStatement where(String fname, Condition cond, GlueType glue) {
		throw new NotImplementedError(Constants.DB_MSG_NOT_IMPLEMENTED_INSERT);
	}
	@Override
	public DMLStatement where(String table, String fname, Condition cond, Conditions.GlueType glue) {
		throw new NotImplementedError(Constants.DB_MSG_NOT_IMPLEMENTED_INSERT);
	}
	
	public DMLStatement join(String tableName, String leftFieldName, String rightFieldName) {
		throw new NotImplementedError(Constants.DB_MSG_NOT_IMPLEMENTED_INSERT);
	}

	@Override
	public String prepare() {
		StringBuilder sql = new StringBuilder("INSERT ");
		
		prepareInsert(sql);
		if (!fields.isEmpty())
			prepareValues(sql);
		
		return sql.toString();
	}

	@Override
	public PreparedStatement compile(String sql) throws SQLException {
		return compile(sql, null);
	}
	
	@Override
	public PreparedStatement compile(String sql, int[] params) throws SQLException {
		Args.checkNotNull(Constants.DB_CONN_IS_NULL, conn);
		Args.checkNotNull(Constants.DB_SQL_IS_NULL, sql);
		
		PreparedStatement ps = null;
		Field curField = null;
		
		if (params != null && params.length > 0) {
			switch (params.length) {
			case 1:
				ps = conn.prepareStatement(sql, params[0]);
				break;
			case 2:
				ps = conn.prepareStatement(sql, params[0], params[1]);
				break;
			case 3:
				ps = conn.prepareStatement(sql, params[0], params[1], params[2]);
				break;
			}
		} else
			ps = conn.prepareStatement(sql);
		
		int paramIndex = 1;
		
		for (Iterator<Field> it = fields.iterator(); it.hasNext();) {
			curField = it.next();
			if (!curField.isFunction()) {
				switch (curField.getFieldType()) {
				case STRING:
					ps.setString(paramIndex++, (String) curField.getValue());
					break;
				case INTEGER:
					ps.setInt(paramIndex++, (Integer) curField.getValue());
					break;
				case FLOAT:
					ps.setFloat(paramIndex++, (Float) curField.getValue());
					break;
				case DOUBLE:
					ps.setDouble(paramIndex++, (Double) curField.getValue());
				}
			}
		}
		
		return ps;
	}

	protected void prepareInsert(StringBuilder sql) {
		if (ignore)
			sql.append("IGNORE ");
		sql.append("INTO " + quote(table) + " ");
	}

	protected abstract void prepareValues(StringBuilder sql);
	
	protected abstract String quote(String fname);
}
