package org.tourgune.mdp.airbnb.database;

import org.tourgune.mdp.airbnb.database.Conditions.ConditionType;
import org.tourgune.mdp.airbnb.database.Conditions.GlueType;

public class Condition {
	private Field field;
	private ConditionType condType;
	private GlueType glue;

	public Condition(Field field, ConditionType condType) {
		this.field = field;
		this.condType = condType;
		this.glue = GlueType.AND;
	}
	
	public Condition(Field field, ConditionType condType, GlueType glue) {
		this.field = field;
		this.condType = condType;
		this.glue = glue;
	}

	public GlueType getGlue() {
		return glue;
	}

	public void setGlue(GlueType glue) {
		this.glue = glue;
	}
	
	public Field getField() {
		return field;
	}

	public void setField(Field field) {
		this.field = field;
	}

	public ConditionType getCondType() {
		return condType;
	}

	public void setCondType(ConditionType condType) {
		this.condType = condType;
	}
}
