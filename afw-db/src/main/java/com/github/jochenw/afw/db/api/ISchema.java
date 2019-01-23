package com.github.jochenw.afw.db.api;

import com.github.jochenw.afw.db.bldr.SchemaBuilder;

public class Schema {
	public interface ISchemaBuilder extends ISchema {
		ISchemaBuilder name(ISchema.Name pName);
		ISchemaBuilder name(String pName);
		ITableBuilder table(ITable.Name pName);
		ITableBuilder table(String pName);
	}
	public interface ITableBuilder extends ITable {
		ITableBuilder name(ITable.Name pName);
		ITableBuilder name(String pName);
		IColumnBuilder column(IColumn.Name pName, IColumn.Type pType);
		IColumnBuilder column(String pName, IColumn.Type pType);
	}
	public interface IColumnBuilder extends IColumn {
		IColumnBuilder name(IColumn.Name pName);
		IColumnBuilder name(String pName);
		IColumnBuilder type(IColumn.Type pType);
		IColumnBuilder nullable();
		IColumnBuilder notNull();
		IColumnBuilder nullable(boolean pNullable);
	}

	public static ISchemaBuilder builder() {
		return new SchemaBuilder(null);
	}
}
