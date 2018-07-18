package com.github.jochenw.afw.db.bldr;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;

import com.github.jochenw.afw.core.util.AbstractBuilder;
import com.github.jochenw.afw.core.util.Strings;
import com.github.jochenw.afw.db.api.IColumn;
import com.github.jochenw.afw.db.api.ISchema;
import com.github.jochenw.afw.db.api.ITable;
import com.github.jochenw.afw.db.api.Schema.IColumnBuilder;
import com.github.jochenw.afw.db.api.Schema.ITableBuilder;
import com.github.jochenw.afw.db.api.IColumn.Type;
import com.github.jochenw.afw.db.api.ISchema.Name;


public class TableBuilder extends AbstractBuilder implements ITable, ITableBuilder {
	private final List<ColumnBuilder> columns = new ArrayList<>();
	private final SchemaBuilder schemaBuilder;
	private Name name;

	public TableBuilder(SchemaBuilder pSchema, ITable.Name pName) {
		Objects.requireNonNull(pSchema, "Schema");
		Objects.requireNonNull(pName, "Name");
		schemaBuilder = pSchema;
		name = pName;
	}

	@Override
	public TableBuilder name(ITable.Name pName) {
		Objects.requireNonNull(pName, "Name");
		final ITable tab = getSchema().getTable(pName);
		if (tab != null  &&  tab != this) {
			throw new IllegalStateException("Duplicate table: " + tab.getQName());
		}
		assertMutable();
		name = pName;
		return this;
	}

	@Override
	public TableBuilder name(String pName) {
		Strings.requireTrimmedNonEmpty(pName, "Name");
		return name(new ITable.Name(pName));
	}

	public String getQName() {
		final ISchema.Name schemaName = getSchema().getName();
		if (schemaName  == null) {
			return name.getId();
		} else {
			return schemaName.getId() + "." + name.getId();
		}
	}

	public ColumnBuilder column(IColumn.Name pName, IColumn.Type pType) {
		Objects.requireNonNull(pName, "Name");
		Objects.requireNonNull(pType, "Type");
		if (getColumn(pName) != null) {
			throw new IllegalStateException("Duplicate column: " + getQName() + "." + pName);
		}
		assertMutable();
		final ColumnBuilder col = new ColumnBuilder(this, pName, pType);
		columns.add(col);
		return col;
	}

	public ColumnBuilder column(String pName, IColumn.Type pType) {
		Strings.requireTrimmedNonEmpty(pName, "Name");
		Objects.requireNonNull(pType, "Type");
		return column(new IColumn.Name(pName), pType);
	}
	
	@Override
	public Name getName() {
		return name;
	}

	@Override
	public ISchema getSchema() {
		return schemaBuilder;
	}

	@Override
	public List<IColumn> getColumns() {
		final List<?> colObjects = columns;
		@SuppressWarnings("unchecked")
		final List<IColumn> cols = (List<IColumn>) colObjects;
		return Collections.unmodifiableList(cols);
	}

	@Override
	public IColumn getColumn(IColumn.Name pName) {
		Objects.requireNonNull(pName, "Name");
		final ISchema schema = schemaBuilder;
		for (ColumnBuilder col : columns) {
			if (schema.compareNames(pName, col.getName()) == 0) {
				return col;
			}
		}
		return null;
	}

	@Override
	public IColumn getColumn(String pName) {
		Objects.requireNonNull(pName, "Name");
		final ISchema schema = schemaBuilder;
		for (ColumnBuilder col : columns) {
			if (schema.compareNames(pName, col.getName()) == 0) {
				return col;
			}
		}
		return null;
	}

	@Override
	public IColumn requireColumn(IColumn.Name pName) throws NoSuchElementException {
		final IColumn col = getColumn(pName);
		if (col == null) {
			throw new NoSuchElementException("Column not found: " + getQName() + "." + pName);
		}
		return col;
	}

	@Override
	public IColumn requireColumn(String pName) throws NoSuchElementException {
		final IColumn col = getColumn(pName);
		if (col == null) {
			throw new NoSuchElementException("Column not found: " + getQName() + "." + pName);
		}
		return col;
	}
}
