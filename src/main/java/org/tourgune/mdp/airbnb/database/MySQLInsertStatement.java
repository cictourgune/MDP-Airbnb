package org.tourgune.mdp.airbnb.database;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.tourgune.mdp.airbnb.utils.Constants;

public class MySQLInsertStatement extends InsertStatement {

	private List<String> values;
	
	public MySQLInsertStatement(String table) {
		super(table);
		values = new ArrayList<String>();
	}

	@Override
	protected void prepareValues(StringBuilder sql) {
		Iterator<Field> it = fields.iterator();
		Field curField = null;
		sql.append("(");
		while (it.hasNext()) {
			curField = it.next();
			sql.append(quote(curField.getFieldName()));
			if (curField.isFunction())
				values.add(encodeFunction((Functions) curField.getValue()));
			else
				values.add("?");
			if (it.hasNext())
				sql.append(", ");
		}
		sql.append(") VALUES (");
		// en Java 8 tenemos StringJoiner, String.join(), y otros métodos que hacen de nuestra vida mucho más sencilla
		// pero queremos compatibilidad con Java 7, así que no podemos usarlos :(
		for (Iterator<String> valuesIt = values.iterator(); valuesIt.hasNext();) {
			sql.append(valuesIt.next());
			if (valuesIt.hasNext())
				sql.append(", ");
		}
		sql.append(")");
	}

	@Override
	protected String quote(String fname) {
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

	private String encodeFunction(Object value) {
		if (value == Functions.CUR_DATE)	// si se añaden nuevas funciones, mejor usar un switch
			return Constants.MYSQL_FUNC_CURDATE;
		return "";
	}
}
