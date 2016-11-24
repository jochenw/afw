package com.github.jochenw.afw.lc.impl;

import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;
import org.junit.Assert;
import org.junit.Test;

import com.github.jochenw.afw.core.log.log4j.Log4j2LogFactory;
import com.github.jochenw.afw.lc.AfwLc;
import com.github.jochenw.afw.lc.ComponentFactory;
import com.github.jochenw.afw.lc.ISessionProvider;
import com.github.jochenw.afw.lc.beans.Tenant;


public class DbInitializerTest {
	private ComponentFactory newComponentFactory() {
		return AfwLc.newComponentFactoryBuilder().applicationName("hibtest").instanceName("ht")
				.usingHibernate().usingFlyway()
				.logFactory(new Log4j2LogFactory())
				.build();
	}

	@Test
	public void test() {
		final ComponentFactory componentFactory = newComponentFactory();
		final ISessionProvider sessionProvider = componentFactory.requireInstance(ISessionProvider.class);
		Assert.assertNotNull(sessionProvider);
		final Session session = sessionProvider.newSession();
		final Query query = session.createQuery("FROM Tenant");
		@SuppressWarnings("unchecked")
		final List<Tenant> list = (List<Tenant>) query.list();
		Assert.assertNotNull(list);
		Assert.assertEquals(2, list.size());
		final Tenant tenant0 = list.get(0);
		final Tenant tenant1 = list.get(1);
		Assert.assertEquals(1, tenant0.getId());
		Assert.assertEquals(2, tenant1.getId());
		Assert.assertEquals("Default", tenant0.getName());
		Assert.assertEquals("Test", tenant1.getName());
	}
}
