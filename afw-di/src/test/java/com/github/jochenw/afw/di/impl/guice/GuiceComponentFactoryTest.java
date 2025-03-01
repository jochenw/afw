package com.github.jochenw.afw.di.impl.guice;

import java.util.function.Function;

import org.junit.Test;

import com.github.jochenw.afw.di.api.LinkableBindingBuilder;
import com.github.jochenw.afw.di.impl.AbstractComponentFactory;
import com.github.jochenw.afw.di.impl.ComponentFactoryTests;
import com.github.jochenw.afw.di.impl.ComponentFactoryTests.CreateJavaxMapsObject;
import com.github.jochenw.afw.di.impl.ComponentFactoryTests.TestParentObject;

/** Test for the {@link GuiceComponentFactory}.
 */
public class GuiceComponentFactoryTest {
	private static final Class<? extends AbstractComponentFactory> COMPONENT_FACTORY_TYPE = GuiceComponentFactory.class;

	/** A test method, which tests proper instantiation, and injection of a
	 * {@link CreateJavaxMapsObject}.
	 */
	@Test
	public void testCreateMaps() {
		ComponentFactoryTests.testCreateJavaxMaps(COMPONENT_FACTORY_TYPE);
	}

	/** A test method, which tests proper instantiation, and injection of a
	 * {@link TestParentObject}.
	 */
	@Test
	public void testParent() {
		ComponentFactoryTests.testParent(COMPONENT_FACTORY_TYPE);
	}

	/** A test method, which runs the Java Inject TCK on the component factory.
	 */
	@Test
	public void testTck() {
		ComponentFactoryTests.testTck(COMPONENT_FACTORY_TYPE);
	}

	/** A test method, which checks, whether a module can overwrite a previous
	 * modules bindings.
	 */
	@Test
	public void testModuleOverrides() {
		ComponentFactoryTests.testModuleOverrides(COMPONENT_FACTORY_TYPE);
	}

	/** Test for {@link LinkableBindingBuilder#to(Function)}.
	 */
	@Test
	public void testBindToFunction() {
		ComponentFactoryTests.testBindToFunction(COMPONENT_FACTORY_TYPE);
	}

	/** Test for injecting generics.
	 */
	public void testGenerics() {
		ComponentFactoryTests.testGenerics(COMPONENT_FACTORY_TYPE);
	}
}
