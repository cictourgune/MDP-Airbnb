package org.tourgune.mdp.airbnb.database;

public class Field {

	public static enum FieldType {
		STRING,
		INTEGER,
		FLOAT,
		DOUBLE
	}
	
	private String tableName;
	private String fieldName;
	private String fieldAlias;
	private FieldType fieldType;
	private Object value;
	private boolean isFunction;
	
	public Field(boolean isFunction) {
		this.isFunction = isFunction;
	}
	public String getTableName() {
		return tableName;
	}
	public void setTableName(String tableName) {
		this.tableName = tableName;
	}
	public String getFieldName() {
		return fieldName;
	}
	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}
	public String getFieldAlias() {
		return fieldAlias;
	}
	public void setFieldAlias(String fieldAlias) {
		this.fieldAlias = fieldAlias;
	}
	public FieldType getFieldType() {
		return fieldType;
	}
	public void setFieldType(FieldType fieldType) {
		this.fieldType = fieldType;
	}
	public Object getValue() {
		return value;
	}
	public void setValue(Object value) {
		this.value = value;
	}
	public boolean isFunction() {
		return isFunction;
	}

	@Override
	public String toString() {
		return "Field [tableName=" + tableName + ", fieldName=" + fieldName
				+ ", fieldType=" + fieldType + ", value=" + value
				+ ", isFunction=" + isFunction + "]";
	}
}
