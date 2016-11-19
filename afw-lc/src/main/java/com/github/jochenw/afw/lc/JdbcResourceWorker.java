package com.github.jochenw.afw.lc;

import java.sql.Connection;

import javax.inject.Inject;

import com.github.jochenw.afw.core.SimpleResourceWorker;

public class JdbcResourceWorker extends SimpleResourceWorker {
	public abstract static class JdbcRunnable extends ResRunnable {
		public void run(SimpleResourceTracker pTracker) {
			if (pTracker instanceof JdbcResourceTracker) {
				run((JdbcResourceTracker) pTracker);
			} else {
				throw new IllegalStateException("Not implemented");
			}
		}
		public abstract void run(JdbcResourceTracker pTracker);
	}
	public abstract static class JdbcCallable<T> extends ResCallable<T> {
		public T call(SimpleResourceTracker pTracker) {
			if (pTracker instanceof JdbcResourceTracker) {
				return call((JdbcResourceTracker) pTracker);
			} else {
				throw new IllegalStateException("Not implemented");
			}
		}
		public abstract T call(JdbcResourceTracker pTracker);
	}
	public class JdbcResourceTracker extends SimpleResourceTracker{
		private Connection conn;

		public Connection getConnection() {
			if (conn == null) {
				conn = connectionProvider.newConnection();
				track(conn);
			}
			return conn;
		}
	}

	@Inject private IConnectionProvider connectionProvider;

	public void run(JdbcRunnable pRunnable) {
		super.run(pRunnable);
	}

	public <T> T call(JdbcCallable<T> pCallable) {
		return super.call(pCallable);
	}

	@Override
	protected JdbcResourceTracker newTracker() {
		return new JdbcResourceTracker();
	}
}
