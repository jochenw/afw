package com.github.jochenw.afw.db.api;

import java.util.List;
import java.util.NoSuchElementException;

public interface ITable {
	public class Name extends DefaultId {
		private static final long serialVersionUID = 7941990742907771217L;

		public Name(String pId) {
			super(pId);
		}
	}

	Name getName();
	String getQName();
	ISchema getSchema();
	public List<IColumn> getColumns();
	public IColumn getColumn(IColumn.Name pName);
	public IColumn getColumn(String pName);
	public IColumn requireColumn(IColumn.Name pName) throws NoSuchElementException;
	public IColumn requireColumn(String pName) throws NoSuchElementException;
}
