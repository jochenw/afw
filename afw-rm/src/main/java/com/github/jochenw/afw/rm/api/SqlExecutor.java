package com.github.jochenw.afw.rm.api;

import java.sql.Connection;
import java.sql.SQLException;

public interface SqlExecutor {
	void execute(Connection pConnection, String pStatement) throws SQLException;
}
