package com.github.jochenw.afw.rm;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.github.jochenw.afw.rm.api.AbstractInitializable;
import com.github.jochenw.afw.rm.api.SqlExecutor;

public class DefaultSqlExecutor extends AbstractInitializable implements SqlExecutor {

	@Override
	public void execute(Connection pConnection, String pStatement) throws SQLException {
		try (PreparedStatement stmt = pConnection.prepareStatement(pStatement)) {
			getLogger().debug("SQL: " + pStatement);
			stmt.executeUpdate();
		}
	}

}
