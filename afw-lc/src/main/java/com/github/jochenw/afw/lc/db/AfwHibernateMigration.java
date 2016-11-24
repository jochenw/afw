package com.github.jochenw.afw.lc.db;

import java.sql.Connection;

import javax.inject.Inject;

import org.hibernate.Session;
import org.hibernate.SessionBuilder;

import com.github.jochenw.afw.core.util.Exceptions;
import com.github.jochenw.afw.lc.ComponentFactory;
import com.github.jochenw.afw.lc.ISessionProvider;

public abstract class AfwHibernateMigration extends AfwMigration {
	@Inject private ISessionProvider sessionProvider;

	@Override
	public void execute(ComponentFactory pComponentFactory, Connection pConnection) {
		final SessionBuilder sb = sessionProvider.getSessionFactory().withOptions();
		Session session = null;
		Throwable th = null;
		try {
			session = sb.connection(pConnection).openSession();
			execute(pComponentFactory, session);
			session.close();
			session = null;
		} catch (Throwable t) {
			th = t;
		} finally {
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
