package org.tourgune.mdp.airbnb.database;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public interface DMLStatement {
	public DMLStatement field(String fname);
	public DMLStatement field(String fname, int fvalue);
	public DMLStatement field(String fname, float fvalue);
	public DMLStatement field(String fname, double fvalue);
	public DMLStatement field(String fname, String fvalue);
	public DMLStatement field(String fname, Functions func);
	// TODO more to be added
	
	public DMLStatement storedProcedure(String rawSql);
	// TODO more to be added
	
	public DMLStatement alias(String alias);
	public DMLStatement alias(String fname, String alias);
	
	public DMLStatement where(String fname, Condition cond);
	public DMLStatement where(String table, String fname, Condition cond);
	public DMLStatement where(String fname, Condition cond, Conditions.GlueType glue);
	public DMLStatement where(String table, String fname, Condition cond, Conditions.GlueType glue);
	// TODO more to be added
	
	public DMLStatement join(String tableName, String leftFieldName, String rightFieldName);
	
	public String prepare();
	public PreparedStatement compile(String sql) throws SQLException;
	public PreparedStatement compile(String sql, int[] params) throws SQLException;
}
