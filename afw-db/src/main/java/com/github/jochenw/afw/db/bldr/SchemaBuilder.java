package com.github.jochenw.afw.db.bldr;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;

import com.github.jochenw.afw.core.util.AbstractBuilder;
import com.github.jochenw.afw.db.api.DefaultId;
import com.github.jochenw.afw.db.api.ISchema;
import com.github.jochenw.afw.db.api.ITable;
import com.github.jochenw.afw.db.api.ITable.Name;
import com.github.jochenw.afw.db.api.Schema.ISchemaBuilder;
import com.github.jochenw.afw.db.api.Schema.ITableBuilder;


public class SchemaBuilder extends AbstractBuilder implements ISchema, ISchemaBuilder {
	public static final Comparator<String> DEFAULT_NAME_COMPARATOR =
			(s1, s2) -> s1.compareToIgnoreCase(s2);
	private final List<TableBuilder> tables = new ArrayList<>();
	private Comparator<String> nameComparator = DEFAULT_NAME_COMPARATOR;
	private Name name;

	public SchemaBuilder(Name pName) {
		name = pName;
	}
	
	@Override
	public Name getName() {
		return name;
	}

	@Override
	public ISchemaBuilder name(Name pName) {
		assertMutable();
		name = pName;
		return this;
	}

	@Override
	public ISchemaBuilder name(String pName) {
		return name(new Name(pName));
	}

	@Override
	public List<ITable> getTables() {
		final List<?> tableObjects = tables;
		@SuppressWarnings("unchecked")
		final List<ITable> tabs = (List<ITable>) tableObjects;
		return Collections.unmodifiableList(tabs);
	}

	@Override
	public ITable getTable(com.github.jochenw.afw.db.api.ITable.Name pName) {
		Objects.requireNonNull(pName, "Name");
		for (TableBuilder tb : tables) {
			if (compareNames(pName, tb.getName()) == 0) {
				return tb;
			}
		}
		return null;
	}

	@Override
	public ITable getTable(String pName) {
		Objects.requireNonNull(pName, "Name");
		for (TableBuilder tb : tables) {
			if (compareNames(pName, tb.getName()) == 0) {
				return tb;
			}
		}
		return null;
	}

	@Override
	public ITable requireTable(ITable.Name pName) throws NoSuchElementException {
		final ITable tab = getTable(pName);
		if (tab == null) {
			throw new NoSuchElementException("No such table in schema: " + pName);
		}
		return tab;
	}

	@Override
	public ITable requireTable(String pName) throws NoSuchElementException {
		final ITable tab = getTable(pName);
		if (tab == null) {
			throw new NoSuchElementException("No such table in schema: " + pName);
		}
		return tab;
	}

	@Override
	public int compareNames(Object pName1, Object pName2) {
		final String name1 = asName(pName1);
		final String name2 = asName(pName2);
		if (name1 == null) {
			return name2 == null ? 0 : -1;
		}
		if (name2 == null) {
			return 1;
		}
		return nameComparator.compare(name1, name2);
	}

	private String asName(Object pObject) {
		if (pObject == null) {
			return null;
		}
		if (pObject instanceof DefaultId) {
			return ((DefaultId) pObject).getId();
		}
		return pObject.toString();
	}

	@Override
	public ITableBuilder table(com.github.jochenw.afw.db.api.ITable.Name pName) {
		assertMutable();
		final TableBuilder tb = new TableBuilder(this, pName);
		tables.add(tb);
		return tb;
	}

	@Override
	public ITableBuilder table(String pName) {
		return table(new ITable.Name(pName));
	}
}
