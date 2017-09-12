package com.github.jochenw.afw.lc.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.hibernate.Session;
import org.hibernate.cfg.Configuration;
import org.hibernate.jdbc.Work;

import com.github.jochenw.afw.core.log.ILogFactory;
import com.github.jochenw.afw.core.log.IMLog;
import com.github.jochenw.afw.lc.ComponentFactory;
import com.github.jochenw.afw.lc.ISessionProvider;
import com.github.jochenw.afw.lc.InjLog;

public class HibernateSchemaCreator {
	@InjLog IMLog log;

	public void execute(ComponentFactory pComponentFactory, Session pSession) {
		if (log == null) {
			final ILogFactory logFactory = pComponentFactory.getInstance(ILogFactory.class);
			log = logFactory.getLog(HibernateSchemaCreator.class, "execute");
		}
		log.entering();
		ISessionProvider sessionProvider = pComponentFactory.requireInstance(ISessionProvider.class);
		final Configuration configuration = sessionProvider.getConfiguration();
		final String[] sql = configuration.generateSchemaCreationScript(sessionProvider.getDialect());

		pSession.doWork(new Work(){
			@Override
			public void execute(Connection pConn) throws SQLException {
				for (String s : sql) {
					log.debug(s);
					try (PreparedStatement stmt = pConn.prepareStatement(s)) {
						stmt.executeUpdate();
					}
				}
			}
		});
		log.exiting();
	}
}
