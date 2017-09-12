package com.github.jochenw.afw.db.api;

import java.io.Serializable;
import java.sql.Types;

public interface IColumn {
	public static class Name extends DefaultId {
		private static final long serialVersionUID = 7941990742907771217L;

		public Name(String pId) {
			super(pId);
		}
	}

	public static class Type implements Serializable {
		private static final long serialVersionUID = -3824479166527028305L;
		private final int sqlType;
		private final int length;
		private final String name;

		private Type(String pName, int pSqlType) {
			this(pName,  pSqlType, -1);
		}
		private Type(String pName, int pSqlType, int pLength) {
			sqlType = pSqlType;
			length = pLength;
			name = pName;
		}

		public static final Type BOOL = new Type("BOOL", Types.BOOLEAN);
		public static final Type INT = new Type("INT", Types.INTEGER);
		public static final Type LONG = new Type("LONG", Types.BIGINT);
		public static final Type SHORT = new Type("SHORT", Types.SMALLINT);
		public static final Type BYTE = new Type("BYTE", Types.TINYINT);
		public static Type STRING(int pLength) { return new Type("STRING", Types.VARCHAR, checkLength(pLength)); }
		public static Type CHAR(int pLength) { return new Type("CHAR", Types.CHAR, checkLength(pLength)); }

		private static int checkLength(int pLength) {
			if (pLength < 0) {
				throw new IllegalArgumentException("Invalid length: " + pLength);
			}
			return pLength;
		}
		
		public int getSqlType() { return sqlType; }
		public int getLength() { return length; }
		public String getName() { return name; }
	}

	Name getName();
	String getQName();
	Type getType();
	ITable getTable();
	boolean isNullable();
}
