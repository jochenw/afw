package com.github.jochenw.afw.lc.ht.schema;

import org.hibernate.Session;

import com.github.jochenw.afw.core.log.ILogFactory;
import com.github.jochenw.afw.core.log.IMLog;
import com.github.jochenw.afw.lc.ComponentFactory;
import com.github.jochenw.afw.lc.beans.Tenant;
import com.github.jochenw.afw.lc.db.AfwHibernateMigration;

public class V0_0_2__CreateTenants extends AfwHibernateMigration {
	IMLog log;

	@Override
	protected void execute(ComponentFactory pComponentFactory, Session pSession) {
		log = pComponentFactory.getInstance(ILogFactory.class).getLog(V0_0_2__CreateTenants.class, "execute");
		final Tenant tenant0 = new Tenant();
		tenant0.setName("Default");
		pSession.save(tenant0);
		log.debug("Created tenant0 with id " + tenant0.getId());
		final Tenant tenant1 = new Tenant();
		tenant1.setName("Test");
		pSession.save(tenant1);
		log.debug("Created tenant0 with id " + tenant1.getId());
	}

}
