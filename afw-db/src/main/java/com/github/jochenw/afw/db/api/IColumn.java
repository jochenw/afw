package com.github.jochenw.afw.db.api;

public interface IColumn {
	public class Name extends DefaultId {
		private static final long serialVersionUID = 7941990742907771217L;

		public Name(String pId) {
			super(pId);
		}
	}

	Name getName();
	ITable getTable();

}
