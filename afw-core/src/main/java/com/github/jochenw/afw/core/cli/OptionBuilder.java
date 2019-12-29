package com.github.jochenw.afw.core.cli;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.github.jochenw.afw.core.util.Objects;


public abstract class OptionBuilder<O> {
	private final Class<O> type;
	private boolean required;
	private List<String> names = new ArrayList<String>();
	private String defaultValue;
	private List<String> descriptions = new ArrayList<String>();
	private boolean immutable;

	public OptionBuilder(Class<O> pType) {
		type = pType;
	}

	protected void assertMutable() {
		if (immutable) {
			throw new IllegalStateException("This object is no longer mutable.");
		}
	}

	public OptionBuilder<O> required() {
		return required(true);
	}

	public OptionBuilder<O> required(boolean pRequired) {
		assertMutable();
		required = pRequired;
		return this;
	}

	public boolean isRequired() {
		return required;
	}

	public OptionBuilder<O> name(String pName) {
		Objects.requireNonNull(pName, "Name");
		assertMutable();
		names.add(pName);
		return this;
	}

	public OptionBuilder<O> names(String... pNames) {
		Objects.requireAllNonNull(pNames, "Name");
		assertMutable();
		names.addAll(Arrays.asList(pNames));
		return this;
	}

	public String[] getNames() {
		return names.toArray(new String[names.size()]);
	}

	public OptionBuilder<O> defaultValue(String pDefaultValue) {
		Objects.requireNonNull(pDefaultValue, "Default Value");
		assertMutable();
		defaultValue = pDefaultValue;
		return this;
	}

	public String getDefaultValue() {
		return defaultValue;
	}

	public OptionBuilder<O> description(String pDescription) {
		Objects.requireNonNull(pDescription, "Description");
		assertMutable();
		descriptions.add(pDescription);
		return this;
	}
	
	public OptionBuilder<O> description(String... pDescriptions) {
		Objects.requireAllNonNull(pDescriptions, "Description");
		assertMutable();
		descriptions.addAll(Arrays.asList(pDescriptions));
		return this;
	}

	public String[] getDescriptions() {
		return descriptions.toArray(new String[descriptions.size()]);
	}

	public Class<O> getType() {
		return type;
	}

	protected void configure(Option<O> pOption) {
		final String defaultValue = getDefaultValue();
		if (defaultValue != null  &&  pOption.getDefaultValue() == null) {
			pOption.setDefaultValue(defaultValue);
		}
		pOption.setDescription(getDescriptions());
		pOption.setNames(getNames());
		pOption.setRequired(isRequired());
	}
	
	public abstract Option<O> build();

	public abstract Options end();
}
