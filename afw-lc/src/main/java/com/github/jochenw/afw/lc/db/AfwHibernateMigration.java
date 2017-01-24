package com.github.jochenw.afw.lc.db;

import java.sql.Connection;

import javax.inject.Inject;

import org.hibernate.Session;
import org.hibernate.SessionBuilder;
import org.hibernate.Transaction;

import com.github.jochenw.afw.core.util.Exceptions;
import com.github.jochenw.afw.lc.ComponentFactory;
import com.github.jochenw.afw.lc.ISessionProvider;

public abstract class AfwHibernateMigration extends AfwMigration {
	@Inject private ISessionProvider sessionProvider;

	@Override
	public void execute(ComponentFactory pComponentFactory, Connection pConnection) {
		final SessionBuilder sb = sessionProvider.getSessionFactory().withOptions();
		Session session = null;
		Transaction transaction = null;
		Throwable th = null;
		try {
			session = sb.connection(pConnection).openSession();
			transaction = session.beginTransaction();
			execute(pComponentFactory, session);
			transaction.commit();
			transaction = null;
			session.close();
			session = null;
		} catch (Throwable t) {
			th = t;
		} finally {
			if (transaction != null) {
				try {
					transaction.rollback();
				} catch (Throwable t) {
					if (th == null) {
						th = t;
					}
				}
			}
			if (session != null) {
				try {
					session.close();
				} catch (Throwable t) {
					if (th == null) {
						th = t;
					}
				}
			}
		}
		if (th != null) {
			throw Exceptions.show(th);
		}
	}

	protected abstract void execute(ComponentFactory pComponentFactory, Session pSession);
}
