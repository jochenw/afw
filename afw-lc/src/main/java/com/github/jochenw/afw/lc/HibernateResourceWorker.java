package com.github.jochenw.afw.lc;

import java.util.Objects;

import javax.inject.Inject;

import org.hibernate.Session;
import org.hibernate.Transaction;

import com.github.jochenw.afw.core.SimpleResourceWorker;
import com.github.jochenw.afw.lc.JdbcResourceWorker.JdbcResourceTracker;

public class HibernateResourceWorker extends SimpleResourceWorker {
	public abstract static class HibernateResRunnable extends ResRunnable {
		public void run(SimpleResourceTracker pTracker) {
			if (pTracker instanceof HibernateResourceTracker) {
				run((HibernateResourceTracker) pTracker);
			} else {
				throw new IllegalStateException("Not implemented");
			}
		}
		public abstract void run(HibernateResourceTracker pTracker);
	}
	public abstract static class HibernateResCallable<T> extends ResCallable<T> {
		public T call(SimpleResourceTracker pTracker) {
			if (pTracker instanceof HibernateResourceTracker) {
				return call((HibernateResourceTracker) pTracker);
			} else {
				throw new IllegalStateException("Not implemented");
			}
		}
		public abstract T call(HibernateResourceTracker pTracker);
	}
	public class HibernateResourceTracker extends SimpleResourceTracker{
		private Session session;
		private Transaction transaction;

		public Session getSession() {
			if (session == null) {
				session = sessionProvider.newSession();
				track(session);
				transaction = session.beginTransaction();
				track(transaction);
			}
			return session;
		}
	}

	@Inject private ISessionProvider sessionProvider;

	public void run(HibernateResRunnable pRunnable) {
		super.run(pRunnable);
	}

	public <T> T call(HibernateResCallable<T> pCallable) {
		return super.call(pCallable);
	}

	@Override
	protected HibernateResourceTracker newTracker() {
		return new HibernateResourceTracker();
	}

	@Override
	protected void assertTrackable(Object pResource) {
		Objects.requireNonNull(pResource);
		if (!(pResource instanceof Session)  &&  !(pResource instanceof Transaction)) {
			super.assertTrackable(pResource);
		}
	}

	@Override
	protected void closeResource(Object pResource, boolean pCommit) throws Throwable {
		Objects.requireNonNull(pResource);
		if (pResource instanceof Session) {
			sessionProvider.close((Session) pResource);
		} else if (pResource instanceof Transaction) {
			final Transaction t = (Transaction) pResource;
			if (pCommit) {
				t.commit();
			} else {
				t.rollback();
			}
		} else {
			super.closeResource(pResource, pCommit);
		}
	}
}
