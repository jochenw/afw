package com.github.jochenw.afw.lc;



import org.hibernate.Session;
import org.junit.Assert;
import org.junit.Test;

import com.github.jochenw.afw.core.log.log4j.Log4j2LogFactory;

public class DefaultSessionProviderTest {
	private ComponentFactory newComponentFactory() {
		return AfwLc.newComponentFactoryBuilder().applicationName("hibtest").instanceName("ht").usingHibernate()
				.logFactory(new Log4j2LogFactory())
				.build();
	}
	@Test
	public void test() {
		final ComponentFactory cf = newComponentFactory();
		final ISessionProvider sp = cf.getInstance(ISessionProvider.class);
		Assert.assertNotNull(sp);
		Assert.assertNotNull(sp.getSessionFactory());
		Assert.assertNotNull(sp.getDialect());
		final Session session = sp.newSession();
		Assert.assertNotNull(session);
		sp.close(session);
	}
}
