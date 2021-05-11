package com.github.jochenw.afw.core.components;

import static org.junit.Assert.*;

import java.io.StringWriter;
import java.util.Properties;

import org.junit.Test;

import com.github.jochenw.afw.core.inject.IComponentFactory;
import com.github.jochenw.afw.core.log.ILogFactory;
import com.github.jochenw.afw.core.log.simple.SimpleLogFactory;
import com.github.jochenw.afw.core.props.DefaultPropertyFactory;
import com.github.jochenw.afw.core.props.IPropertyFactory;

/**
 * Test for {@link Application}.
 */
public class ApplicationTest {
	/** Test case for creating an instance of {@link Application}.
	 */
	@Test
	public void testApplicationCreate() {
		final StringWriter sw = new StringWriter();
		final SimpleLogFactory lf = new SimpleLogFactory(sw);
		final Properties props = new Properties();
		final IPropertyFactory pf = new DefaultPropertyFactory(props);
		final Application app = new Application((b) -> { b.bind(Properties.class).toInstance(props); },
				        () -> { return lf; },
				        () -> { return pf; });
		final IComponentFactory cf = app.getComponentFactory();
		assertNotNull(cf);
		assertSame(lf, app.getLogFactory());
		assertSame(lf, cf.requireInstance(ILogFactory.class));
		assertSame(pf, app.getPropertyFactory());
		assertSame(pf, cf.requireInstance(IPropertyFactory.class));
		assertSame(props, cf.requireInstance(Properties.class));
	}
}
