package com.github.jochenw.afw.core.jdbc;

import java.sql.SQLException;

/** Implementation of {@link Dialect} for the
 * <a href="http://www.h2database.com/">H2 database</a>.
 */
public class H2Dialect implements Dialect {

	@Override
	public boolean isDroppedTableDoesnExistError(SQLException pError) {
		return "42S02".equals(pError.getSQLState())  &&  42102 == pError.getErrorCode();
	}
}
