/*
 * Copyright 2018 Jochen Wiedmann
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.jochenw.afw.core.inject.simple;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

import org.junit.Test;

import com.github.jochenw.afw.core.inject.IComponentFactory;
import com.github.jochenw.afw.core.inject.IComponentFactoryAware;
import com.github.jochenw.afw.core.inject.InjectTests;
import com.github.jochenw.afw.core.util.Holder;



/** Test for the {@link SimpleComponentFactoryBuilder}.
 */
public class SimpleComponentFactoryTest {
	/** Runs the standard tests for the {@link SimpleComponentFactoryBuilder}.
	 */
	@Test
	public void testSimpleComponentFactory() {
		InjectTests.testComponentFactory(new SimpleComponentFactoryBuilder());
	}

	/** Tests, whether the {@link IComponentFactoryAware} interface is honoured.
	 */
	@Test
	public void testComponentFactoryAware() {
		final Holder<IComponentFactory> cfHolder = new Holder<>();
		final Object object = new IComponentFactoryAware() {
			@Override
			public void init(IComponentFactory pFactory) {
				assertNotNull(pFactory);
				cfHolder.set(pFactory);
			}
		};
		final IComponentFactory cf = new SimpleComponentFactoryBuilder().module((b) -> {
			b.bind(Object.class, "singleton").toInstance(object);
		}).build();
		final Object o = cf.requireInstance(Object.class, "singleton");
		assertNotNull(o);
		assertSame(object, o);
		assertSame(cf, cfHolder.get());
	}
}
