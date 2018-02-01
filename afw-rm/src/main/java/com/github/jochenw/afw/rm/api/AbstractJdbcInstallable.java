package com.github.jochenw.afw.rm.api;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

import com.github.jochenw.afw.rm.api.RmResourcePlugin.ResourceInstallationRequest;
import com.github.jochenw.afw.rm.util.Exceptions;
import com.github.jochenw.afw.rm.util.Strings;


public abstract class AbstractJdbcInstallable extends AbstractInstallable {
	private JdbcConnectionProvider connectionProvider;

	@Override
	public void init(ComponentFactory pFactory) {
		super.init(pFactory);
		connectionProvider = pFactory.requireInstance(JdbcConnectionProvider.class);
	}

	protected abstract void install(ResourceInstallationRequest pRequest, Connection pConn) throws SQLException;

	protected void execute(Connection pConn, String pStatement, Object... pArgs) throws SQLException {
		try (PreparedStatement stmt = pConn.prepareStatement(pStatement)) {
			execute(stmt, pArgs);
		} catch (Throwable t) {
			throw Exceptions.show(t);
		}
	}

	protected void execute(PreparedStatement pStmt, Object... pArgs) throws SQLException {
		setArgs(pStmt, pArgs);
		getLogger().debug("SQL: " + getString(pStmt) + " " + Strings.toString(pArgs));
		pStmt.executeUpdate();
	}

	protected String getString(PreparedStatement pStmt) {
		if ("org.hsqldb.jdbc.JDBCPreparedStatement".equals(pStmt.getClass().getName())) {
			try {
				Field sqlField = pStmt.getClass().getDeclaredField("sql");
				if (!sqlField.isAccessible()) {
					sqlField.setAccessible(true);
				}
				return (String) sqlField.get(pStmt);
			} catch (Throwable t) {
				throw Exceptions.show(t);
			}
		} else {
			return pStmt.toString();
		}
	}
	
	protected void setArgs(PreparedStatement pStmt, Object... pArgs) throws SQLException {
		if (pArgs != null) {
			for (int i = 0;  i < pArgs.length;  i++) {
				setArg(pStmt, i+1, pArgs[i]);
			}
		}
	}

	protected void setArg(PreparedStatement pStmt, int pIndex, Object pArg) throws SQLException {
		if (pArg == null) {
			pStmt.setNull(pIndex, Types.NULL);
		} else {
			if (pArg instanceof String) {
				pStmt.setString(pIndex, (String) pArg);
			} else if (pArg instanceof Long) {
				pStmt.setLong(pIndex, ((Long) pArg).longValue());
			} else if (pArg instanceof Integer) {
				pStmt.setInt(pIndex, ((Integer) pArg).intValue());
			} else if (pArg instanceof Short) {
				pStmt.setInt(pIndex, ((Short) pArg).shortValue());
			} else if (pArg instanceof Byte) {
				pStmt.setByte(pIndex, ((Byte) pArg).byteValue());
			} else {
				throw new IllegalArgumentException("Invalid argument type: " + pArg.getClass().getName());
			}
		}
	}
	@Override
	public void install(ResourceInstallationRequest pRequest) {
		try (Connection conn = connectionProvider.open()) {
			install(pRequest, conn);
		} catch (SQLException e) {
			throw Exceptions.show(e);
		}
	}
}
