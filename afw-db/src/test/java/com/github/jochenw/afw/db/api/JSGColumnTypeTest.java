package com.github.jochenw.afw.db.api;


import static org.junit.Assert.*;

import java.sql.Types;

import org.junit.Test;

import com.github.jochenw.afw.db.api.IColumn.Type;

public class JSGColumnTypeTest {
	@Test
	public void testBooleanType() {
		assertType("BOOL", Types.BOOLEAN, Type.BOOL);
	}

	@Test
	public void testNumericTypes() {
		assertType("BYTE", Types.TINYINT, Type.BYTE);
		assertType("SHORT", Types.SMALLINT, Type.SHORT);
		assertType("INT", Types.INTEGER, Type.INT);
		assertType("LONG", Types.BIGINT, Type.LONG);
	}

	@Test
	public void testStringTypes() {
		assertType("STRING", Types.VARCHAR, 20, Type.STRING(20));
		assertType("STRING", Types.VARCHAR, 50, Type.STRING(50));
		assertType("STRING", Types.VARCHAR, 4000, Type.STRING(4000));
		assertType("CHAR", Types.CHAR, 20, Type.CHAR(20));
		assertType("CHAR", Types.CHAR, 50, Type.CHAR(50));
		assertType("CHAR", Types.CHAR, 4000, Type.CHAR(4000));
	}
	
	private void assertType(String pName, int pSqlType, Type pType) {
		assertType(pName, pSqlType, -1, pType);
	}

	private void assertType(String pName, int pSqlType, int pLength, Type pType) {
		assertEquals(pName, pType.getName());
		assertEquals(pSqlType, pType.getSqlType());
		assertEquals(pLength, pType.getLength());
	}
}
