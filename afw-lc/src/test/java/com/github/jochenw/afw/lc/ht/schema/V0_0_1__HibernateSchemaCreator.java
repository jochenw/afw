package com.github.jochenw.afw.lc.ht.schema;

import org.hibernate.Session;

import com.github.jochenw.afw.lc.ComponentFactory;
import com.github.jochenw.afw.lc.db.AfwHibernateMigration;


public class V0_0_1__HibernateSchemaCreator extends AfwHibernateMigration {
	@Override
	protected void execute(ComponentFactory pComponentFactory, Session pSession) {
		new com.github.jochenw.afw.lc.db.HibernateSchemaCreator().execute(pComponentFactory, pSession);
	}

}
