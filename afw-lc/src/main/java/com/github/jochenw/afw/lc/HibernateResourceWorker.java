package com.github.jochenw.afw.lc;

import java.util.Objects;

import javax.inject.Inject;

import org.hibernate.Session;

import com.github.jochenw.afw.core.SimpleResourceWorker;
import com.github.jochenw.afw.lc.JdbcResourceWorker.JdbcResourceTracker;

public class HibernateResourceWorker extends SimpleResourceWorker {
	public abstract static class HibernateRunnable extends ResRunnable {
		public void run(SimpleResourceTracker pTracker) {
			if (pTracker instanceof JdbcResourceTracker) {
				run((JdbcResourceTracker) pTracker);
			} else {
				throw new IllegalStateException("Not implemented");
			}
		}
		public abstract void run(JdbcResourceTracker pTracker);
	}
	public abstract static class HibernateCallable<T> extends ResCallable<T> {
		public T call(SimpleResourceTracker pTracker) {
			if (pTracker instanceof JdbcResourceTracker) {
				return call((JdbcResourceTracker) pTracker);
			} else {
				throw new IllegalStateException("Not implemented");
			}
		}
		public abstract T call(JdbcResourceTracker pTracker);
	}
	public class HibernateResourceTracker extends SimpleResourceTracker{
		private Session session;

		public Session getSession() {
			if (session == null) {
				session = sessionProvider.newSession();
				track(session);
			}
			return session;
		}
	}

	@Inject private ISessionProvider sessionProvider;

	public void run(HibernateRunnable pRunnable) {
		super.run(pRunnable);
	}

	public <T> T call(HibernateCallable<T> pCallable) {
		return super.call(pCallable);
	}

	@Override
	protected HibernateResourceTracker newTracker() {
		return new HibernateResourceTracker();
	}

	@Override
	protected void assertTrackable(Object pResource) {
		Objects.requireNonNull(pResource);
		if (!(pResource instanceof Session)) {
			super.assertTrackable(pResource);
		}
	}

	@Override
	protected void closeResource(Object pResource) throws Throwable {
		Objects.requireNonNull(pResource);
		if (pResource instanceof Session) {
			sessionProvider.close((Session) pResource);
		} else {
			super.closeResource(pResource);
		}
	}


}
