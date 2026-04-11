package com.github.jochenw.afw.di.impl;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.github.jochenw.afw.di.api.DefaultAnnotationProvider;
import com.github.jochenw.afw.di.api.GoogleAnnotationProvider;
import com.github.jochenw.afw.di.api.IAnnotationProvider;
import com.github.jochenw.afw.di.api.JakartaAnnotationProvider;
import com.github.jochenw.afw.di.api.JavaxAnnotationProvider;

class DefaultAnnotationProviderTest {
	@SuppressWarnings("unused")
	public static class TestClass {
		private @jakarta.inject.Inject String jakartaInjectedField;
		private @javax.inject.Inject String javaxInjectedField;
		private @com.google.inject.Inject String googleInjectedField;
		private @jakarta.inject.Inject @jakarta.inject.Named(value="jakarta") void jakartaSetter() {}
		private @javax.inject.Inject @javax.inject.Named(value="javax") void javaxSetter() {}
		private @com.google.inject.Inject @com.google.inject.name.Named(value="google") void googleSetter() {}
	}

	public static void test(IAnnotationProvider pProvider, String pIds) {
		test(pProvider, null, pIds);
	}
	public static void test(IAnnotationProvider pProvider, String pInvalidId, String... pIds) {
		for (String id : pIds) {
			final Field field;
			try {
				field = TestClass.class.getDeclaredField(id + "InjectedField");
			} catch (Throwable t) {
				throw DiUtils.show(t);
			}
			assertTrue(pProvider.isInjectable(field));
			assertNull(pProvider.getNamedValue(field));
			final Method method;
			try {
				method = TestClass.class.getDeclaredMethod(id + "Setter");
			} catch (Throwable t) {
				throw DiUtils.show(t);
			}
			assertTrue(pProvider.isInjectable(method));
			assertEquals(id, pProvider.getNamedValue(method));
		}
	}
	
	@Test
	void test() {
		final DefaultAnnotationProvider dap = DefaultAnnotationProvider.getInstance();
		final List<IAnnotationProvider> list = dap.getAnnotationProviders();
		assertEquals(3, list.size());
		final JakartaAnnotationProvider jkap = (JakartaAnnotationProvider) list.get(0);
		assertNotNull(jkap);
		test(jkap, "jakarta");
		final JavaxAnnotationProvider jxap = (JavaxAnnotationProvider) list.get(1);
		test(jxap, "javax");
		final GoogleAnnotationProvider gap = (GoogleAnnotationProvider) list.get(2);
		test(gap, "google");
		test(dap, "jakarta", "javax", "google");
	}
}
