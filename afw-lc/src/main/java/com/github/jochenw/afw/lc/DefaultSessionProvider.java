package com.github.jochenw.afw.lc;

import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.cfgxml.spi.LoadedConfig;
import org.hibernate.boot.registry.BootstrapServiceRegistry;
import org.hibernate.boot.registry.BootstrapServiceRegistryBuilder;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.dialect.Dialect;
import org.hibernate.engine.jdbc.connections.spi.ConnectionProvider;
import org.hibernate.engine.spi.SessionFactoryImplementor;

import com.github.jochenw.afw.core.ResourceLocator;


public class DefaultSessionProvider implements ISessionProvider {
	@Inject IConnectionProvider connectionProvider;
	@Inject ResourceLocator resourceLocator;
	@Inject Properties properties;
	private Dialect dialect;
	private SessionFactory sessionFactory;

	@PostConstruct
	public void start() {
		final BootstrapServiceRegistry bsr = new BootstrapServiceRegistryBuilder().build();
		final StandardServiceRegistryBuilder ssrBuilder = new StandardServiceRegistryBuilder( bsr );

		final MetadataSources metadataSources = new MetadataSources( bsr );

		final URL configUrl = resourceLocator.requireResource("hibernate.cfg.xml");
		ssrBuilder.configure(configUrl);
		ssrBuilder.applySettings( getHibernateProperties() );

		final LoadedConfig loadedConfig = ssrBuilder.getAggregatedCfgXml();
		final StandardServiceRegistry ssr = ssrBuilder.build();
		sessionFactory = new MetadataSources().buildMetadata(ssr).buildSessionFactory();
		dialect = ((SessionFactoryImplementor) sessionFactory).getDialect();
	}

	@SuppressWarnings("rawtypes")
	protected Map getHibernateProperties() {
		final Map<String,Object> map = new HashMap<>();
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

			@Override @SuppressWarnings("rawtypes")
			public boolean isUnwrappableAs( Class unwrapType) {
				throw new IllegalStateException("Not implemented");
			}

			public <T> T unwrap(Class<T> unwrapType) {
				throw new IllegalStateException("Not implemented");
			}

			@Override
			public Connection getConnection() throws SQLException {
				return connectionProvider.newConnection();
			}

			@Override
			public void closeConnection(Connection pConnection) throws SQLException {
				connectionProvider.close(pConnection);
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

}
