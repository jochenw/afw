package com.github.jochenw.afw.core.jdbc;

import java.sql.SQLException;

/** A Dialect object is used to handle incompatibilities between various databases
 * in code, that is supposed to be portable.
 */
public interface Dialect {
	/** Returns, whether the given error indicates, that an attempt has been made
	 * to drop a table, that doesn't exist.
	 * @param pError The exception, that has been thrown.
	 * @return True, if the error indicates, that an attempt has been made
	 * to drop a table, that doesn't exist. Otherwise false.
	 */
	boolean isDroppedTableDoesnExistError(SQLException pError);
}
