/**
 * 
 */
package com.github.jochenw.afw.core.components;

import static org.junit.Assert.*;

import java.lang.reflect.Field;
import java.util.function.Consumer;
import java.util.function.Supplier;

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
		private final StringBuilder sb;
		public TestContext(StringBuilder pSb) {
			sb = pSb;
		}
		public void append(String pValue) {
			sb.append(pValue).append("\n");
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

		final StringBuilder sb = new StringBuilder();
		final BeanWalker.BeanVisitor<TestContext> visitor = new BeanWalker.BeanVisitor<BeanWalkerTest.TestContext>() {
			@Override
			public TestContext startWalking(Object pObject) {
				final TestContext testContext = new TestContext(sb);
				testContext.append("endWalking: " + pObject.getClass().getName());
				return testContext;
			}

			@Override
			public void endWalking(TestContext pContext, Object pObject) {
				pContext.append("endWalking: " + pObject.getClass().getName());
			}

			@Override
			public boolean isAtomic(TestContext pContext, Class<?> pType) {
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
				pContext.append("isAtomic: " + pType.getName() + " -> " + String.valueOf(result));
				return result;
			}

			@Override
			public boolean isComplex(TestContext pContext, Class<?> pType) {
				final boolean result =
						    pType == TestBean.InnerBean.class
						 || pType == TestBean.InnerBean.InnerMostBean.class;
				pContext.append("isComplex: " + pType.getName() + " -> " + String.valueOf(result));
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
		};
		final BeanWalker beanWalker = new BeanWalker();
		beanWalker.setFieldComparator((f1, f2) -> f1.getName().compareTo(f2.getName()));
		beanWalker.walk(visitor, testBean);
		final String EXPECT =
				"endWalking: com.github.jochenw.afw.core.components.BeanWalkerTest$TestBean\n" + 
				"isAtomic: int -> true\n" + 
				"visitAtomicProperty: anInteger, 47\n" + 
				"isAtomic: java.lang.String -> true\n" + 
				"visitAtomicProperty: bar, bar\n" + 
				"isAtomic: java.lang.String -> true\n" + 
				"visitAtomicProperty: foo, foo\n" + 
				"isAtomic: com.github.jochenw.afw.core.components.BeanWalkerTest$TestBean$InnerBean -> false\n" + 
				"isComplex: com.github.jochenw.afw.core.components.BeanWalkerTest$TestBean$InnerBean -> true\n" + 
				"startVisiting: com.github.jochenw.afw.core.components.BeanWalkerTest$TestBean$InnerBean\n" + 
				"isAtomic: com.github.jochenw.afw.core.components.BeanWalkerTest$TestBean$InnerBean$InnerMostBean -> false\n" + 
				"isComplex: com.github.jochenw.afw.core.components.BeanWalkerTest$TestBean$InnerBean$InnerMostBean -> true\n" + 
				"startVisiting: com.github.jochenw.afw.core.components.BeanWalkerTest$TestBean$InnerBean$InnerMostBean\n" + 
				"isAtomic: java.lang.Long -> true\n" + 
				"visitAtomicProperty: number, 6\n" + 
				"endVisiting: com.github.jochenw.afw.core.components.BeanWalkerTest$TestBean$InnerBean$InnerMostBean\n" + 
				"endVisiting: com.github.jochenw.afw.core.components.BeanWalkerTest$TestBean$InnerBean\n" + 
				"endWalking: com.github.jochenw.afw.core.components.BeanWalkerTest$TestBean\n";
		assertEquals(EXPECT, sb.toString());
	}

}
