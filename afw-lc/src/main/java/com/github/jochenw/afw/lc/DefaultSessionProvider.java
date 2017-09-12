package com.github.jochenw.afw.lc;

import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.Properties;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.dialect.Dialect;
import org.hibernate.engine.jdbc.connections.spi.ConnectionProvider;
import org.hibernate.engine.jdbc.spi.JdbcServices;

import com.github.jochenw.afw.core.ResourceLocator;


public class DefaultSessionProvider implements ISessionProvider {
	@Inject IConnectionProvider connectionProvider;
	@Inject ResourceLocator resourceLocator;
	@Inject Properties properties;
	private Dialect dialect;
	private SessionFactory sessionFactory;
	private Configuration configuration;

	@PostConstruct
	public void start() {
		final URL configUrl = resourceLocator.requireResource("hibernate.cfg.xml");
		configuration = new Configuration();
		configuration.configure(configUrl);
		Properties props = getHibernateProperties();
		configuration.addProperties( props );
		
		final StandardServiceRegistryBuilder ssrBuilder = new StandardServiceRegistryBuilder();
		ssrBuilder.addService(ConnectionProvider.class, newHibernateConnectionProvider());
		ssrBuilder.applySettings( props );

		final StandardServiceRegistry ssr = ssrBuilder.build();
		sessionFactory = configuration.buildSessionFactory(ssr);
		dialect = ssr.getService(JdbcServices.class).getDialect();
	}

	@SuppressWarnings("rawtypes")
	protected Properties getHibernateProperties() {
		final Properties map = new Properties();
		for (Map.Entry en : properties.entrySet()) {
			final String key = (String) en.getKey();
			if (key.startsWith("hibernate.")) {
				map.put(key, en.getValue());
			}
		}
		return map;
	}

	private ConnectionProvider newHibernateConnectionProvider() {
		return new ConnectionProvider() {
			private static final long serialVersionUID = -939900633874329194L;

			@Override
			public boolean isUnwrappableAs(@SuppressWarnings("rawtypes") Class unwrapType) {
				throw new IllegalStateException("Not implemented");
			}

			@Override
			public <T> T unwrap(Class<T> unwrapType) {
				throw new IllegalStateException("Not implemented");
			}

			@Override
			public Connection getConnection() throws SQLException {
				return connectionProvider.newConnection();
			}

			@Override
			public void closeConnection(Connection pConn) throws SQLException {
				connectionProvider.close(pConn);
			}

			@Override
			public boolean supportsAggressiveRelease() {
				throw new IllegalStateException("Not implemented");
			}
		};
	}

	@Override
	public SessionFactory getSessionFactory() {
		return sessionFactory;
	}

	@Override
	public Dialect getDialect() {
		return dialect;
	}

	@Override
	public Session newSession() {
		return sessionFactory.openSession();
	}

	@Override
	public void close(Session pSession) {
		pSession.close();
	}

	@Override
	public Configuration getConfiguration() {
		return configuration;
	}
}
