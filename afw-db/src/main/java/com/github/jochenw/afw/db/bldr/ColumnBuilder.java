package com.github.jochenw.afw.db.bldr;

import java.util.Objects;

import com.github.jochenw.afw.core.util.AbstractBuilder;
import com.github.jochenw.afw.core.util.Strings;
import com.github.jochenw.afw.db.api.IColumn;
import com.github.jochenw.afw.db.api.ITable;
import com.github.jochenw.afw.db.api.IColumn.Name;
import com.github.jochenw.afw.db.api.IColumn.Type;

public class ColumnBuilder extends AbstractBuilder implements IColumn {
	private final TableBuilder tableBuilder;
	private IColumn.Name name;
	private IColumn.Type type;
	private boolean nullable;

	ColumnBuilder(TableBuilder pTableBuilder, IColumn.Name pName, IColumn.Type pType) {
		tableBuilder = pTableBuilder;
		name = pName;
		type = pType;
	}

	ColumnBuilder name(String pName) {
		Strings.requireNonEmpty(pName, "Name");
		return name(new IColumn.Name(pName));
	}

	ColumnBuilder name(IColumn.Name pName) {
		Objects.requireNonNull(pName, "Name");
		assertMutable();
		final IColumn col= tableBuilder.getColumn(pName);
		if (col != null  &&  col != this) {
			throw new IllegalStateException("Duplicate column name: " + col.getQName());
		}
		name = pName;
		return this;
	}

	ColumnBuilder type(Type pType) {
		Objects.requireNonNull(pType, "Type");
		assertMutable();
		type = pType;
		return this;
	}

	ColumnBuilder nullable() {
		return nullable(true);
	}

	ColumnBuilder notNull() {
		return nullable(false);
	}

	ColumnBuilder nullable(boolean pNullable) {
		assertMutable();
		nullable = pNullable;
		return nullable(true);
	}
	
	@Override
	public Name getName() {
		return name;
	}

	@Override
	public String getQName() {
		return tableBuilder.getQName() + "." + name;
	}
	
	@Override
	public ITable getTable() {
		return null;
	}

	@Override
	public Type getType() {
		return type;
	}

	@Override
	public boolean isNullable() {
		return nullable;
	}
}
