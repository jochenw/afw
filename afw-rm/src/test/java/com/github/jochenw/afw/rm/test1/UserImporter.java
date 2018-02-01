package com.github.jochenw.afw.rm.test1;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.github.jochenw.afw.rm.api.AbstractJdbcInstallable;
import com.github.jochenw.afw.rm.api.Resource;
import com.github.jochenw.afw.rm.api.RmResourcePlugin.ResourceInstallationRequest;


@Resource(title="User Importer", description="Imports user from an external datasource", version="0.0.2")
public class UserImporter extends AbstractJdbcInstallable {
	@Override
	protected void install(ResourceInstallationRequest pRequest, Connection pConn) throws SQLException {
		try (final PreparedStatement stmt = pConn.prepareStatement("INSERT INTO Users (id, uid, email) VALUES (?, ?, ?)")) {
			execute(stmt, Long.valueOf(1l), "jwi", "jwi@company.com");
			execute(stmt, Long.valueOf(2l), "ans", "ans@company.com");
			execute(stmt, Long.valueOf(3l), "thu", "thu@company.com");
		}
	}
}
