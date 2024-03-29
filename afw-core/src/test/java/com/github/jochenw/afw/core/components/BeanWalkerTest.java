/**
 * 
 */
package com.github.jochenw.afw.core.components;

import static org.junit.Assert.*;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.jspecify.annotations.NonNull;
import org.junit.Test;

/**
 * @author jwi
 *
 */
public class BeanWalkerTest {
	private static class TestBean {
		private static class InnerBean {
			private static class InnerMostBean {
				@SuppressWarnings("unused")
				private Long number;
			}
			private InnerMostBean innerMostBean;
		}
		private InnerBean innerBean;
		@SuppressWarnings("unused")
		private int anInteger;
		@SuppressWarnings("unused")
		private String foo, bar;
	}

	private static class TestContext extends BeanWalker.Context {
		private final Consumer<String> consumer;
		public TestContext(Consumer<String> pConsumer) {
			consumer = pConsumer;
		}
		public void append(String pValue) {
			consumer.accept(pValue);
		}
	}

	/** Test for {@link BeanWalker#walk(BeanWalker.BeanVisitor, Object)}.
	 */
	@Test
	public void testWalk() {
		final TestBean testBean = new TestBean();
		testBean.innerBean = new TestBean.InnerBean();
		testBean.innerBean.innerMostBean = new TestBean.InnerBean.InnerMostBean();
		testBean.innerBean.innerMostBean.number = Long.valueOf(6);
		testBean.anInteger = 47;
		testBean.foo = "foo";
		testBean.bar = "bar";

		final List<String> list = new ArrayList<>();
		final BeanWalker.BeanVisitor<TestContext> visitor = new BeanWalker.BeanVisitor<BeanWalkerTest.TestContext>() {
			@Override
			public @NonNull TestContext startWalking(@NonNull Object pObject) {
				final TestContext testContext = new TestContext((s) -> { list.add(s); System.out.println(s); });
				testContext.append("startWalking: " + getName(pObject.getClass()));
				return testContext;
			}

			@Override
			public void endWalking(@NonNull TestContext pContext, @NonNull Object pObject) {
				pContext.append("endWalking: " + getName(pObject.getClass()));
			}

			@Override
			public boolean isAtomic(TestContext pContext, String pFieldName, Class<?> pType) {
				final boolean result =
						   pType == String.class
						|| pType == Boolean.class
						|| pType == Boolean.TYPE
						|| pType == Integer.class
						|| pType == Integer.TYPE
						|| pType == Long.class
						|| pType == Long.TYPE
						|| pType == Short.class
						|| pType == Short.TYPE
						|| pType == Byte.class
						|| pType == Byte.TYPE
						|| pType == Double.class
						|| pType == Double.TYPE
						|| pType == Float.class
						|| pType == Float.TYPE;
				pContext.append("isAtomic: " + pFieldName + ", " + getName(pType) + " -> " + String.valueOf(result));
				return result;
			}

			@Override
			public boolean isComplex(TestContext pContext, String pFieldName, Class<?> pType) {
				final boolean result =
						    pType == TestBean.InnerBean.class
						 || pType == TestBean.InnerBean.InnerMostBean.class;
				pContext.append("isComplex: " + pFieldName + ", " + getName(pType) + " -> " + String.valueOf(result));
				return result;
			}

			@Override
			public void visitAtomicProperty(TestContext pContext, Field pField, Supplier<Object> pSupplier,
					Consumer<Object> pConsumer) {
				pContext.append("visitAtomicProperty: " + pField.getName() + ", " + pSupplier.get().toString());
			}

			@Override
			public void startVisiting(TestContext pContext, Object pBean) {
				pContext.append("startVisiting: " + pBean.getClass().getName());
			}

			@Override
			public void endVisiting(TestContext pContext, Object pBean) {
				pContext.append("endVisiting: " + pBean.getClass().getName());
			}

			protected String getName(Class<?> pType) {
				return pType.getName();
			}
		};
		final BeanWalker beanWalker = new BeanWalker();
		beanWalker.setFieldComparator((f1, f2) -> f1.getName().compareTo(f2.getName()));
		beanWalker.walk(visitor, testBean);
		final String[] EXPECT = {
				"startWalking: com.github.jochenw.afw.core.components.BeanWalkerTest$TestBean", 
				"isAtomic: anInteger, int -> true", 
				"visitAtomicProperty: anInteger, 47",
				"isAtomic: bar, java.lang.String -> true",
				"visitAtomicProperty: bar, bar",
				"isAtomic: foo, java.lang.String -> true",
				"visitAtomicProperty: foo, foo",
				"isAtomic: innerBean, com.github.jochenw.afw.core.components.BeanWalkerTest$TestBean$InnerBean -> false",
				"isComplex: innerBean, com.github.jochenw.afw.core.components.BeanWalkerTest$TestBean$InnerBean -> true",
				"startVisiting: com.github.jochenw.afw.core.components.BeanWalkerTest$TestBean$InnerBean",
				"isAtomic: innerMostBean, com.github.jochenw.afw.core.components.BeanWalkerTest$TestBean$InnerBean$InnerMostBean -> false",
				"isComplex: innerMostBean, com.github.jochenw.afw.core.components.BeanWalkerTest$TestBean$InnerBean$InnerMostBean -> true",
				"startVisiting: com.github.jochenw.afw.core.components.BeanWalkerTest$TestBean$InnerBean$InnerMostBean",
				"isAtomic: number, java.lang.Long -> true",
				"visitAtomicProperty: number, 6",
				"endVisiting: com.github.jochenw.afw.core.components.BeanWalkerTest$TestBean$InnerBean$InnerMostBean",
				"endVisiting: com.github.jochenw.afw.core.components.BeanWalkerTest$TestBean$InnerBean",
				"endWalking: com.github.jochenw.afw.core.components.BeanWalkerTest$TestBean"};
		assertEquals(EXPECT.length, list.size());
		for (int i = 0;  i < EXPECT.length;  i++) {
			assertEquals(EXPECT[i], list.get(i));
		}
	}

}
