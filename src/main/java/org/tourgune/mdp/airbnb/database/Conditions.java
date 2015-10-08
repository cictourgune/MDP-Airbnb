package org.tourgune.mdp.airbnb.database;

import org.tourgune.mdp.airbnb.database.Field.FieldType;

public class Conditions {
	public static enum ConditionType {
		EQUAL,
		NOT_EQUAL,
		NULL,
		NOTNULL,
	}
	public static enum GlueType {
		AND,
		OR
	}
	
	public static Condition is(Functions func) {
		Field f = null;
		f = new Field(true);
		f.setValue((Object) func);
		return new Condition(f, ConditionType.EQUAL);
	}
	public static Condition is(String val) {
		Field f = null;
		f = new Field(false);
		f.setFieldType(FieldType.STRING);
		f.setValue((Object) val);
		return new Condition(f, ConditionType.EQUAL);
	}
	public static Condition is(int val) {
		Field f = new Field(false);
		f.setFieldType(FieldType.INTEGER);
		f.setValue((Object) val);
		return new Condition(f, ConditionType.EQUAL);
	}
	
	public static Condition isNull() {
		Field f = new Field(false);
		return new Condition(f, ConditionType.NULL);
	}
	
	public static Condition isNotNull() {
		Field f = new Field(false);
		return new Condition(f, ConditionType.NOTNULL);
	}
}
