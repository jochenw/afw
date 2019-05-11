package com.github.jochenw.afw.cli;

public abstract class Option<O> {
	private final Class<O> type;
	private boolean required, given;
	private O value;
	private String strValue;
	private String[] names;
	private String defaultValue;
	private String[] description;

	protected Option(Class<O> pType) {
		type = pType;
	}
	
	public Class<O> getType() {
		return type;
	}

	public String getDefaultValue() {
		return defaultValue;
	}

	protected void setDefaultValue(String pDefaultValue) {
		defaultValue = pDefaultValue;
	}

	public boolean isRequired() {
		return required;
	}

	protected void setRequired(boolean pRequired) {
		required = pRequired;
	}

	public boolean isGiven() {
		return given;
	}

	protected void setGiven(boolean pGiven) {
		given = pGiven;
	}

	public O getValue() {
		return value;
	}

	protected void setValue(O pValue) {
		value = pValue;
	}

	public String getStrValue() {
		return strValue;
	}

	protected void setStrValue(String pStrValue) {
		strValue = pStrValue;
		setGiven(true);
		final O value = asValue(pStrValue);
		setValue(value);
	}

	public String[] getNames() {
		return names;
	}

	protected void setNames(String[] pNames) {
		names = pNames;
	}

	public String[] getDescription() {
		return description;
	}

	protected void setDescription(String[] pDescription) {
		description = pDescription;
	}

	protected abstract O asValue(String pStrValue);
}
