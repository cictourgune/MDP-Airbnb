package org.tourgune.mdp.airbnb.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.tourgune.mdp.airbnb.database.Conditions.ConditionType;
import org.tourgune.mdp.airbnb.exception.NotImplementedError;
import org.tourgune.mdp.airbnb.utils.Args;
import org.tourgune.mdp.airbnb.utils.Constants;

abstract public class SelectStatement implements DMLStatement {

	protected Connection conn;
	
	protected String table;
	
	protected List<Field> fields;
	protected List<Condition> conds;
	
	// Actualmente sólo se permite hacer JOIN con una única tabla
	protected String joinTable;
	protected String leftFieldName, rightFieldName;
	
	public SelectStatement(String table) {
		this.conn = null;
		this.table = table;
		this.fields = new ArrayList<Field>();
		this.conds = new ArrayList<Condition>();
		this.joinTable = null;
		this.leftFieldName = null;
		this.rightFieldName = null;
	}
	
	public Connection getConnection() {
		return conn;
	}

	public void setConnection(Connection conn) {
		this.conn = conn;
	}

	public String getTableName() {
		return table;
	}

	public void setTableName(String table) {
		this.table = table;
	}

	/**
	 * Asigna un alias <strong>al último campo añadido</strong> (vía <code>field()</code>).
	 * 
	 * @param alias
	 * @return
	 */
	@Override
	public DMLStatement alias(String alias) {
		fields.get(fields.size() - 1).setFieldAlias(alias);
		return this;
	}
	/**
	 * Asigna un alias a un campo concreto.
	 * 
	 * @param fname
	 * @param alias
	 * @return
	 */
	@Override
	public DMLStatement alias(String fname, String alias) {
		for (Iterator<Field> it = fields.iterator(); it.hasNext();) {
			Field f = it.next();
			if (f.getFieldName().equals(fname)) {
				f.setFieldAlias(alias);
				break;
			}
		}
		return this;
	}
	
	@Override
	public DMLStatement field(String fname) {
		Args.checkNotNull(Constants.DB_MSG_TABLE_NAME_UNKNOWN + " Call field(String, String), instead of field(String)", this.table);
		return field(this.table, fname);
	}

	@Override
	public DMLStatement field(String table, String fname) {
		Field f = new Field(false);
		f.setTableName(table);
		f.setFieldName(fname);
		fields.add(f);
		return this;
	}
	
	@Override
	public DMLStatement field(String fname, int fvalue) {
		throw new NotImplementedError();
	}
	
	@Override
	public DMLStatement field(String fname, float fvalue) {
		throw new NotImplementedError();
	}
	
	@Override
	public DMLStatement field(String fname, double fvalue) {
		throw new NotImplementedError();
	}

	@Override
	public DMLStatement field(String fname, Functions func) {
		throw new NotImplementedError();
	}
	
	public DMLStatement storedProcedure(String rawSql) {
		Field f = new Field(true);
		f.setValue((Object) rawSql);
		fields.add(f);
		return this;
	}

	@Override
	public DMLStatement where(String fname, Condition cond) {
		Args.checkNotNull(Constants.DB_MSG_TABLE_NAME_UNKNOWN + " Call where(String, String, Condition), instead of where(String, String)", this.table);
		return where(this.table, fname, cond, Conditions.GlueType.AND);
	}
	@Override
	public DMLStatement where(String table, String fname, Condition cond) {
		return where(fname, table, cond, Conditions.GlueType.AND);
	}
	@Override
	public DMLStatement where(String fname, Condition cond, Conditions.GlueType glue) {
		Args.checkNotNull(Constants.DB_MSG_TABLE_NAME_UNKNOWN + " Call where(String, String, Condition, GlueType), instead of where(String, String, Condition)", this.table);
		return where(this.table, fname, cond, glue);
	}
	@Override
	public DMLStatement where(String table, String fname, Condition cond, Conditions.GlueType glue) {
		cond.getField().setTableName(table);
		cond.getField().setFieldName(fname);
		cond.setGlue(glue);
		conds.add(cond);
		return this;
	}

	@Override
	public DMLStatement join(String tableName, String leftFieldName, String rightFieldName) {
		this.joinTable = tableName;
		this.leftFieldName = leftFieldName;
		this.rightFieldName = rightFieldName;
		return this;
	}
	
	@Override
	public String prepare() {
		StringBuilder sql = new StringBuilder("SELECT ");
		
		if (!fields.isEmpty()) {
			prepareSelect(sql);
			prepareFrom(sql);
			if (!conds.isEmpty())
				prepareWhere(sql);
		}
		
		return sql.toString();
	}

	@Override
	public PreparedStatement compile(String sql)  throws SQLException {
		return compile(sql, null);
	}
	
	@Override
	public PreparedStatement compile(String sql, int[] params) throws SQLException {
		Args.checkNotNull(Constants.DB_CONN_IS_NULL, conn);
		Args.checkNotNull(Constants.DB_SQL_IS_NULL, sql);
		
		PreparedStatement ps = null;
		Condition curCond = null;
		Field curField = null;
		
		if (params != null && params.length > 0) {
			switch (params.length) {
			case 1:
				ps = conn.prepareStatement(sql, params[0]);
				break;
			case 2:
				ps = conn.prepareCall(sql, params[0], params[1]);
				break;
			case 3:
				ps = conn.prepareCall(sql, params[0], params[1], params[2]);
				break;
			}
		} else
			ps = conn.prepareStatement(sql);
		
		int paramIndex = 1;
		
		for (Iterator<Condition> it = conds.iterator(); it.hasNext();) {
			curCond = it.next();
			curField = curCond.getField();
			if (!curField.isFunction() && curCond.getCondType() == ConditionType.EQUAL || curCond.getCondType() == ConditionType.NOT_EQUAL) {
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
	
	@Override
	public String toString() {
		return super.toString() + ": " + prepare();
	}

	protected abstract void prepareSelect(StringBuilder targetSql);
	protected abstract void prepareWhere(StringBuilder targetSql);
	
	protected void prepareFrom(StringBuilder targetSql) {
		targetSql.append(" FROM " + quote(table));
		if (this.joinTable != null)
			targetSql.append(" JOIN " + quote(joinTable) +
					" ON " + quote(table) + "." + quote(leftFieldName) +
					" = " + quote(joinTable) + "." + quote(rightFieldName));
	}

	protected abstract String quote(String fname);
}