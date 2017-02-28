package com.github.jochenw.afw.db.api;

import java.util.List;
import java.util.NoSuchElementException;

public interface ISchema {
	public class Name extends DefaultId {
		private static final long serialVersionUID = 7941990742907771217L;

		public Name(String pId) {
			super(pId);
		}
	}

	Name getName();
	public List<ITable> getTables();
	public ITable getTable(ITable.Name pName);
	public ITable getTable(String pName);
	public ITable requireTable(ITable.Name pName) throws NoSuchElementException;
	public ITable requireTable(String pName) throws NoSuchElementException;
}
