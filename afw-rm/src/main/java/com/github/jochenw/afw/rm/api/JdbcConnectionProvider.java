package com.github.jochenw.afw.rm.api;

import java.sql.Connection;
import java.sql.SQLException;

public interface JdbcConnectionProvider {
	Connection open() throws SQLException;
	void close(Connection pConnection) throws SQLException;
}
