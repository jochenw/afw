package com.github.jochenw.afw.rcm.api;

import java.sql.Connection;
import java.sql.SQLException;

public interface JdbcConnectionProvider {
	Connection open() throws SQLException;
	void close(Connection pConnection) throws SQLException;
}
