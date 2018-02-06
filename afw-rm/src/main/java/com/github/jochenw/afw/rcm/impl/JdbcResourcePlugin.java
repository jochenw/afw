package com.github.jochenw.afw.rcm.impl;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.lang.reflect.UndeclaredThrowableException;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.SQLException;

import com.github.jochenw.afw.rcm.api.AbstractInitializable;
import com.github.jochenw.afw.rcm.api.ComponentFactory;
import com.github.jochenw.afw.rcm.api.JdbcConnectionProvider;
import com.github.jochenw.afw.rcm.api.RmResourceInstallationPlugin;
import com.github.jochenw.afw.rcm.api.RmResourcePlugin;
import com.github.jochenw.afw.rcm.api.SqlExecutor;
import com.github.jochenw.afw.rcm.api.SqlReader;


@RmResourceInstallationPlugin
public class JdbcResourcePlugin extends AbstractInitializable implements RmResourcePlugin {
	private JdbcConnectionProvider connectionProvider;
	private SqlReader sqlReader;
	private SqlExecutor sqlExecutor;
	private String charsetName;

	@Override
	public String getContextId() {
		return JdbcTargetLifecyclePlugin.ID;
	}

	@Override
	public boolean isInstallable(ResourceInstallationRequest pRequest) {
		return "sql".equals(pRequest.getResource().getType());
	}

	@Override
	public void install(ResourceInstallationRequest pRequest) {
		try (InputStream is = pRequest.getRepository().open(pRequest.getResourceRef());
			 BufferedInputStream bis = new BufferedInputStream(is);
			 Reader r = new InputStreamReader(bis, charsetName);
			 BufferedReader br = new BufferedReader(r);
			 Connection conn = getConnectionProvider().open()) {
			while(sqlReader.hasNextLine(br)) {
				final String line = sqlReader.nextLine(br);
				sqlExecutor.execute(conn, line);
			}
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		} catch (SQLException e) {
			throw new UndeclaredThrowableException(e);
		}
	}

	protected JdbcConnectionProvider getConnectionProvider() {
		if (connectionProvider == null) {
			connectionProvider = getComponentFactory().getInstance(JdbcConnectionProvider.class);
		}
		return connectionProvider;
	}
	
	@Override
	public void init(ComponentFactory pComponentFactory) {
		super.init(pComponentFactory);
		final ComponentFactory componentFactory = pComponentFactory;
		sqlReader = componentFactory.requireInstance(SqlReader.class);
		sqlExecutor = componentFactory.requireInstance(SqlExecutor.class);
		charsetName = componentFactory.requireInstance(String.class, Charset.class.getName());
	}

}
