package com.github.jochenw.afw.lc.db;

import java.sql.Connection;

import org.flywaydb.core.api.migration.jdbc.JdbcMigration;

import com.github.jochenw.afw.lc.ComponentFactory;
import com.github.jochenw.afw.lc.impl.DbInitializer;

public abstract class AfwMigration implements JdbcMigration {
	public void migrate(Connection pConnection) {
		final ComponentFactory componentFactory = DbInitializer.getComponentFactory();
		componentFactory.configure(this);
		execute(componentFactory, pConnection);
	}

	protected abstract void execute(ComponentFactory pComponentFactory, Connection pConnection);
}
