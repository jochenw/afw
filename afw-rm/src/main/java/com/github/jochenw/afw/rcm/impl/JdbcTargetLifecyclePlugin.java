package com.github.jochenw.afw.rcm.impl;

import java.sql.Connection;

import com.github.jochenw.afw.rcm.api.AbstractInitializable;
import com.github.jochenw.afw.rcm.api.JdbcConnectionProvider;
import com.github.jochenw.afw.rcm.api.RmLifecyclePlugin;
import com.github.jochenw.afw.rcm.api.RmTargetLifecyclePlugin;
import com.github.jochenw.afw.rcm.util.Exceptions;


@RmLifecyclePlugin
public class JdbcTargetLifecyclePlugin extends AbstractInitializable implements RmTargetLifecyclePlugin {
	public static class JdbcContext extends Context {
		private final JdbcTargetLifecyclePlugin plugin;

		public JdbcContext(JdbcTargetLifecyclePlugin pPlugin) {
			super(ID);
			plugin = pPlugin;
		}
		
		private Connection connection;
		public Connection getConnection() {
			if (connection == null) {
				final JdbcConnectionProvider connProvider = plugin.getComponentFactory().requireInstance(JdbcConnectionProvider.class);
				try {
					final Connection conn = connProvider.open();
					conn.setAutoCommit(false);
					connection = conn;
				} catch (Throwable t) {
					throw Exceptions.show(t);
				}
			}
			return connection;
		}
	}

	public static final String ID = JdbcTargetLifecyclePlugin.class.getName();
	
	@Override
	public Context start() {
		return new JdbcContext(this);
	}

	@Override
	public void commit(Context pContext) {
		final JdbcContext ctx = (JdbcContext) pContext;
		if (ctx.connection != null) {
			try {
				ctx.connection.commit();
			} catch (Throwable t) {
				throw Exceptions.show(t);
			}
		}
	}

	@Override
	public void rollback(Context pContext) {
		final JdbcContext ctx = (JdbcContext) pContext;
		if (ctx.connection != null) {
			try {
				ctx.connection.rollback();
			} catch (Throwable t) {
				throw Exceptions.show(t);
			}
		}
	}

}
