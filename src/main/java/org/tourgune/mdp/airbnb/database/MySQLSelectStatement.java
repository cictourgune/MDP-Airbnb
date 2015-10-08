package org.tourgune.mdp.airbnb.database;

import java.util.Iterator;

import org.tourgune.mdp.airbnb.database.Conditions.ConditionType;
import org.tourgune.mdp.airbnb.database.Conditions.GlueType;
import org.tourgune.mdp.airbnb.utils.Constants;

public class MySQLSelectStatement extends SelectStatement {

	public MySQLSelectStatement(String table) {
		super(table);
	}

	public Iterator<Field> fieldIterator() {
		return fields.iterator();
	}
	
	public Iterator<Condition> condIterator() {
		return conds.iterator();
	}
	
	@Override
	protected void prepareSelect(StringBuilder targetSql) {
		Iterator<Field> fieldIt = fields.iterator();
		Field curField = null;
		while (fieldIt.hasNext()) {
			curField = fieldIt.next();
			
			if (curField.isFunction())
				if (curField.getValue() instanceof Functions)
					targetSql.append(encodeFunction((Functions) curField.getValue()));
				else
					targetSql.append((String) curField.getValue());
			else
				targetSql.append(quote(curField.getTableName()) + "." + quote(curField.getFieldName()));
			
			if (curField.getFieldAlias() != null)
				targetSql.append(" " + Constants.MYSQL_ALIAS_SETTER + " " + quote(curField.getFieldAlias()));
			
			if (fieldIt.hasNext())
				targetSql.append(", ");
		}
	}

	@Override
	protected void prepareWhere(StringBuilder targetSql) {
		targetSql.append(" WHERE ");
		Iterator<Condition> condIt = conds.iterator();
		while (condIt.hasNext()) {
			Condition cond = condIt.next();
			Field field = cond.getField();
			targetSql.append(quote(field.getTableName()) + "." + quote(field.getFieldName()));
			targetSql.append(encodeCondition(cond.getCondType()));
			if (field.isFunction())
				targetSql.append(encodeFunction((Functions) field.getValue()));
			else if (cond.getCondType() != ConditionType.NULL && cond.getCondType() != ConditionType.NOTNULL)
				targetSql.append("?");
			if (condIt.hasNext())
				targetSql.append(encodeGlue(cond.getGlue()));
		}
	}
	
	private String encodeCondition(ConditionType condType) {
		String strCondType = "";
		switch (condType) {
		case EQUAL:
			strCondType = " " + Constants.MYSQL_COND_EQUAL + " ";
			break;
		case NOT_EQUAL:
			strCondType = " " + Constants.MYSQL_COND_NOTEQUAL + " ";
			break;
		case NULL:
			strCondType = " " + Constants.MYSQL_COND_NULL + " ";
			break;
		case NOTNULL:
			strCondType = " " + Constants.MYSQL_COND_NOTNULL + " ";
			break;
		}
		return strCondType;
	}

	private String encodeGlue(GlueType glue) {
		String strGlue = "";
		switch (glue) {
		case AND:
			strGlue = " " + Constants.MYSQL_GLUE_AND + " ";
			break;
		case OR:
			strGlue = " " + Constants.MYSQL_GLUE_OR + " ";
			break;
		}
		return strGlue;
	}

	private String encodeFunction(Functions value) {
		if (value == Functions.CUR_DATE)	// si se añaden nuevas funciones, mejor usar un switch
			return Constants.MYSQL_FUNC_CURDATE;
		return "";
	}
	
	/**
	 * Quotes a table or field name with a whitelist approach.
	 * Allowed characters:
	 * 	<ul>
	 * 		<li>Numbers: <code>0-9</code></li>
	 * 		<li>Letters: <code>a-z</code> and <code>A-Z</code></li>
	 * 		<li>Underscore: <code>_</code></li>
	 * 	</ul>
	 * 
	 * @param fname value to be quoted
	 * @return quoted value
	 */
	@Override protected String quote(String fname) {
		char curChar = 0;
		StringBuilder sb = new StringBuilder("`");
		
		for (int i = 0; i < fname.length(); i++) {
			curChar = fname.charAt(i);
			if ((curChar >= '0' && curChar <= '9') ||
				(curChar >= 'A' && curChar <= 'Z') ||
				(curChar >= 'a' && curChar <= 'z') ||
				curChar == '_')
				sb.append(curChar);
		}
		
		return sb.append('`').toString();
	}
}
