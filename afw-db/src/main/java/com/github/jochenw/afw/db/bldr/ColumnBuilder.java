package com.github.jochenw.afw.db.bldr;

import java.util.Objects;

import com.github.jochenw.afw.core.util.AbstractBuilder;
import com.github.jochenw.afw.core.util.Strings;
import com.github.jochenw.afw.db.api.IColumn;
import com.github.jochenw.afw.db.api.ITable;
import com.github.jochenw.afw.db.api.IColumn.Name;
import com.github.jochenw.afw.db.api.IColumn.Type;
import com.github.jochenw.afw.db.api.Schema.IColumnBuilder;

public class ColumnBuilder extends AbstractBuilder implements IColumn, IColumnBuilder {
	private final TableBuilder tableBuilder;
	private IColumn.Name name;
	private IColumn.Type type;
	private boolean nullable;

	ColumnBuilder(TableBuilder pTableBuilder, IColumn.Name pName, IColumn.Type pType) {
		tableBuilder = pTableBuilder;
		name = pName;
		type = pType;
	}

	@Override
	public ColumnBuilder name(String pName) {
		Strings.requireNonEmpty(pName, "Name");
		return name(new IColumn.Name(pName));
	}

	@Override
	public ColumnBuilder name(IColumn.Name pName) {
		Objects.requireNonNull(pName, "Name");
		assertMutable();
		final IColumn col= tableBuilder.getColumn(pName);
		if (col != null  &&  col != this) {
			throw new IllegalStateException("Duplicate column name: " + col.getQName());
		}
		name = pName;
		return this;
	}

	@Override
	public ColumnBuilder type(Type pType) {
		Objects.requireNonNull(pType, "Type");
		assertMutable();
		type = pType;
		return this;
	}

	@Override
	public ColumnBuilder nullable() {
		return nullable(true);
	}

	@Override
	public ColumnBuilder notNull() {
		return nullable(false);
	}

	@Override
	public ColumnBuilder nullable(boolean pNullable) {
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
