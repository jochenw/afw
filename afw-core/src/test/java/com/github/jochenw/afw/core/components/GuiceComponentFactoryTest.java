package com.github.jochenw.afw.core.components;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import javax.inject.Inject;
import javax.inject.Named;

import org.junit.Assert;
import org.junit.Test;

import com.github.jochenw.afw.core.components.ComponentFactoryBuilder.Binder;
import com.github.jochenw.afw.core.components.ComponentFactoryBuilder.Module;
import com.github.jochenw.afw.core.log.ILog;
import com.github.jochenw.afw.core.log.ILogFactory;
import com.github.jochenw.afw.core.log.simple.SimpleLogFactory;



public class GuiceComponentFactoryTest {
	public static class Component {
		private @Inject IComponentFactory componentFactory;
		@SuppressWarnings("rawtypes")
		private @Inject List list;
		private Map<?,?> map1;
		private Map<?,?> map2;
		private @Inject ILog log;
		private @Inject @Named("com.github.jochenw.afw.core.components.SomeComponent") ILog log2;
		private @Inject ILog log3;

		@SuppressWarnings("rawtypes")
		public @Inject void init(@Named(value="1") Map pMap1, Map pMap2) {
			map1 = pMap1;
			map2 = pMap2;
		}

		public Component() {
		}
	}
	
	@Test
	public void test() {
		final Map<String,Object> map1 = new HashMap<>();
		final Map<String,Object> map2 = new HashMap<>();
		final ILogFactory logFactory = new SimpleLogFactory();
		final ComponentFactoryBuilder<?> cfb = new ComponentFactoryBuilder<>()
				.module(new Module() {
					@Override
					public void bind(Binder b) {
						b.bind(List.class, ArrayList.class);
						b.bind(Component.class, Component.class);
						b.bind(Map.class, "1", map1);
						@SuppressWarnings("rawtypes")
						final Supplier<Map> supplier = () -> { return map2; };
						b.bind(Map.class, supplier);
						b.bind(ILogFactory.class, logFactory);
					}
				})
				.constructor((map) -> {
					final GuiceComponentFactory gcf = new GuiceComponentFactory();
					gcf.setBindings(map);
					return gcf;
				});
		final IComponentFactory cf = cfb.build();
		Assert.assertNotNull(cf);
		final Component component = cf.requireInstance(Component.class);
		Assert.assertNotNull(cf);
		Assert.assertSame(component, cf.getInstance(Component.class));
		Assert.assertNotNull(cf.requireInstance(IComponentFactory.class));
		Assert.assertSame(cf, cf.getInstance(IComponentFactory.class));
		Assert.assertSame(cf, component.componentFactory);
        Assert.assertNotNull(component.list);
        Assert.assertTrue(component.list instanceof ArrayList);
        Assert.assertSame(map1, component.map1);
        Assert.assertSame(map2, component.map2);
        Assert.assertNotNull(component.log);
        Assert.assertEquals(Component.class.getName(), component.log.getId());
        Assert.assertNotNull(component.log2);
        Assert.assertEquals("com.github.jochenw.afw.core.components.SomeComponent", component.log2.getId());
        Assert.assertNotNull(component.log3);
        Assert.assertEquals(Component.class.getName(), component.log3.getId());
        Assert.assertNotSame(component.log3, component.log);
	}
}
