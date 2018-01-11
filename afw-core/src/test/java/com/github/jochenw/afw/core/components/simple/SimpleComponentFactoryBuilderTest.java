package com.github.jochenw.afw.core.components.simple;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Provider;

import org.junit.Assert;
import org.junit.Test;

import com.github.jochenw.afw.core.ResourceLocator;
import com.github.jochenw.afw.core.components.ComponentFactory;
import com.github.jochenw.afw.core.components.ComponentFactoryBuilder;
import com.github.jochenw.afw.core.components.ComponentFactoryBuilder.Binder;
import com.github.jochenw.afw.core.components.ComponentFactoryBuilder.Module;
import com.github.jochenw.afw.core.log.ILogFactory;
import com.github.jochenw.afw.core.log.simple.SimpleLogFactory;
import com.github.jochenw.afw.core.props.IPropertyFactory;


public class SimpleComponentFactoryBuilderTest {
	@Test
	public void testSimpleBuild() {
		final ComponentFactoryBuilder cfb = new SimpleComponentFactoryBuilder()
				.applicationName("test")
				.instanceName("scf")
				.resourcePrefix("com/github/jochenw/afw/test")
				.logFactory(new SimpleLogFactory());
		final ComponentFactory scf = cfb.build();
		validate(scf);
		final IPropertyFactory pf = scf.requireInstance(IPropertyFactory.class);
		Assert.assertTrue(pf.getPropertyMap().isEmpty());
	}

	@Test
	public void testSimpleBuildXmlProperties() {
		final ComponentFactoryBuilder cfb = new SimpleComponentFactoryBuilder()
				.applicationName("test")
				.instanceName("scfxml")
				.resourcePrefix("com/github/jochenw/afw/test")
				.logFactory(new SimpleLogFactory());
		final ComponentFactory scf = cfb.build();
		validate(scf);
		final IPropertyFactory pf = scf.requireInstance(IPropertyFactory.class);
		Assert.assertEquals("http://www.softwaresummit.test.com/", pf.getProperty("url.coloradosoftwaresummit").getValue());
	}

	@Test
	public void testSimpleModule() {
		final Object obj1 = new Object();
		final Object obj2 = new Object();
		final List<?> list1 = new ArrayList<>();
		final List<?> list2 = new ArrayList<>();
		final Module module = new Module() {
			@Override
			public void configure(Binder pBinder) {
				pBinder.bindClass(Object.class);
				pBinder.bind(Object.class, "obj1", obj1);
				pBinder.bind(Object.class, "obj2", obj2);
				pBinder.bindProvider(Object.class, "obj1p", new Provider<Object>() {
					@Override
					public Object get() {
						return obj1;
					}
				});
				pBinder.bindProvider(Object.class, "obj2p", new Provider<Object>() {
					@Override
					public Object get() {
						return obj2;
					}
				});
				pBinder.bind(List.class, list1);
				pBinder.bind(List.class, "list2", list2);
				pBinder.bindClass(Map.class, HashMap.class, true);
			}
		};
		final ComponentFactoryBuilder cfb = SimpleComponentFactoryBuilder.builder()
				.applicationName("test")
				.instanceName("mod")
				.resourcePrefix("com/github/jochenw/afw/test")
				.logFactory(new SimpleLogFactory())
				.module(module);
		final ComponentFactory cf = cfb.build();
		validate(cf);
		Assert.assertSame(obj1, cf.getInstance(Object.class, "obj1"));
		Assert.assertSame(obj2, cf.getInstance(Object.class, "obj2"));
		Assert.assertSame(obj1, cf.getInstance(Object.class, "obj1p"));
		Assert.assertSame(obj2, cf.getInstance(Object.class, "obj2p"));
		final Object obj3 = cf.getInstance(Object.class);
		final Object obj4 = cf.getInstance(Object.class);
		Assert.assertNotSame(obj3, obj1);
		Assert.assertNotSame(obj3, obj2);
		Assert.assertNotSame(obj3, obj4);
		Assert.assertSame(list1, cf.requireInstance(List.class));
		Assert.assertSame(list2, cf.requireInstance(List.class, "list2"));
		final Map<?,?> map1 = cf.getInstance(Map.class);
		final Map<?,?> map2 = cf.requireInstance(Map.class);
		Assert.assertSame(map1, map2);
	}

	private void validate(ComponentFactory pCf) {
		Assert.assertNotNull(pCf);
		Assert.assertNotNull(pCf.getInstance(ResourceLocator.class));
		Assert.assertNotNull(pCf.getInstance(ILogFactory.class));
		Assert.assertNotNull(pCf.getInstance(IPropertyFactory.class));
	}
}
