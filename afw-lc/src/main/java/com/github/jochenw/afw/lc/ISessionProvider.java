package com.github.jochenw.afw.lc;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.dialect.Dialect;

public interface ISessionProvider {
	SessionFactory getSessionFactory();
	Dialect getDialect();
	Session newSession();
	void close(Session pSession);
	Configuration getConfiguration();
}
