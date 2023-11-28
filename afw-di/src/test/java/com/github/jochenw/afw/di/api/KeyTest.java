package com.github.jochenw.afw.di.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.logging.Logger;

import javax.inject.Named;

import org.junit.Test;

import com.github.jochenw.afw.di.util.Exceptions;

/** Test suite for the {@link Key} class.
 */
public class KeyTest {
	/** Test for {@link Key#of(Type)}.
	 */
	@Test
	public void testOfType() {
		final Key<Map<String,Object>> key = Key.of(Map.class);
		assertNotNull(key);
		assertSame(Map.class, key.getType());
		assertNull(key.getAnnotation());
		assertNull(key.getAnnotationClass());
		assertEquals(Objects.hash(Map.class, null, null), key.hashCode());
		assertEquals("class=" + Map.class.getName(), key.toString());
		assertEquals(Key.of(Map.class), key);
		final Types.Type<Map<String,Object>> type = new Types.Type<Map<String,Object>>() {
		};
		final Key<Map<String,Object>> key2 = Key.of(type.getRawType());
		assertNotNull(key2);
		assertSame(type.getRawType(), key2.getType());
		assertNull(key2.getAnnotation());
		assertNull(key2.getAnnotationClass());
		assertEquals(Objects.hash(type.getRawType(), null, null), key2.hashCode());
		final String typeName = "type=" + Map.class.getName() + "<" + String.class.getName() + ", " + Object.class.getName() + ">";
		assertEquals(typeName, key2.getDescription());	
	}

	/** Test for {@link Key#of(Type, Annotation)}.
	 */
	@Test
	public void testOfTypeAnnotation() {
		testOfTypeAnnotation(Annotations.getProvider("javax.inject"));
		testOfTypeAnnotation(Annotations.getProvider("jakarta.inject"));
		testOfTypeAnnotation(Annotations.getProvider("com.google.inject"));
	}

	private void testOfTypeAnnotation(final IAnnotationProvider ap) {
		final Annotation annotation = ap.newNamed("hash");
		final Key<Map<String,Object>> key = Key.of(Map.class, annotation);
		assertNotNull(key);
		assertSame(Map.class, key.getType());
		assertSame(annotation, key.getAnnotation());
		assertEquals(Objects.hash(Map.class, annotation, null), key.hashCode());
		assertNull(key.getAnnotationClass());
		assertEquals("class=" + Map.class.getName() + ", name=hash", key.getDescription());
		final Function<String,LogInject> loggerAnnotationFinder = (s) -> {
			try {
				final Field field = KeyTest.class.getDeclaredField(s);
				return field.getAnnotation(LogInject.class);
			} catch (Throwable t) {
				throw Exceptions.show(t);
			}
		};
		final LogInject aLoggerAnnotation = loggerAnnotationFinder.apply("aLogger");
		assertNotNull(aLoggerAnnotation);
		final LogInject bLoggerAnnotation = loggerAnnotationFinder.apply("bLogger");
		assertNotNull(bLoggerAnnotation);
		final LogInject cLoggerAnnotation = loggerAnnotationFinder.apply("cLogger");
		assertNotNull(cLoggerAnnotation);
		final Key<Logger> aLoggerKey = Key.of(Logger.class, aLoggerAnnotation);
		assertEquals(aLoggerKey, aLoggerKey);
		assertFalse(Key.of(Logger.class, aLoggerAnnotation).equals(Key.of(Logger.class, bLoggerAnnotation)));
		assertEquals(aLoggerKey, Key.of(Logger.class, cLoggerAnnotation));
		assertEquals("class=" + Logger.class.getName() + ", annotation=" + aLoggerAnnotation, aLoggerKey.getDescription());
	}

	private @LogInject(id="logger.a") Logger aLogger;
	private @LogInject(id="logger.b") Logger bLogger;
	private @LogInject(id="logger.a") Logger cLogger;

	/** Test for {@code Key#of(Type, String)}.
	 */
	@Test
	public void testOfTypeString() {
		testOfTypeString(Annotations.getProvider("javax.inject"));
		testOfTypeString(Annotations.getProvider("jakarta.inject"));
		testOfTypeString(Annotations.getProvider("com.google.inject"));
	}

	private void testOfTypeString(IAnnotationProvider pAnnotationProvider) {
		final Key<Map<String,Object>> key = Key.of(Map.class, pAnnotationProvider.newNamed("hash"));
		assertNotNull(key);
		assertSame(Map.class, key.getType());
		assertTrue(pAnnotationProvider.newNamed("hash").equals(key.getAnnotation()));
		assertNull(key.getAnnotationClass());
		assertEquals(Objects.hash(Map.class, key.getAnnotation(), null), key.hashCode());
		assertEquals(Key.of(Map.class, pAnnotationProvider.newNamed("hash")), key);
		assertEquals("class=" + Map.class.getName() + ", name=hash", key.getDescription());
	}

	/** Test for {@link Key#of(Type, Class)}.
	 */
	@Test
	public void testOfTypeClass() {
		final Key<Map<String,Object>> key = Key.of(Map.class, Named.class);
		assertNotNull(key);
		assertSame(Map.class, key.getType());
		assertNull(key.getAnnotation());
		assertSame(Named.class, key.getAnnotationClass());
		assertEquals(Objects.hash(Map.class, null, Named.class), key.hashCode());
		assertEquals(Key.of(Map.class, Named.class), key);
		assertEquals("class=" + Map.class.getName() + ", annotationClass=" + Named.class.getName(), key.getDescription());
	}

	/** Test for {@link Key#equals(Object)}.
	 */
	@Test
	public void testEquals() {
		testEquals(Annotations.getProvider("javax.inject"));
		testEquals(Annotations.getProvider("jakarta.inject"));
		testEquals(Annotations.getProvider("com.google.inject"));
	}

	@SuppressWarnings("unlikely-arg-type")
	private void testEquals(IAnnotationProvider pAp) {
		final Key<Map<String,Object>> key = Key.of(Map.class);
		final Key<Map<String,Object>> hashKey = Key.of(Map.class, pAp.newNamed("hash"));
		final Key<Map<String,Object>> linkedKey = Key.of(Map.class, pAp.newNamed("linked"));
		final Key<Map<String,Object>> namedKey = Key.of(Map.class, Named.class);
		final Key<Map<String,Object>> loggingKey = Key.of(Map.class, LogInject.class);
		assertFalse(key.equals(null));
		assertTrue(key.equals(key));
		assertFalse(key.equals(key.getType()));
		assertFalse(key.equals(hashKey));
		assertFalse(hashKey.equals(key));
		assertFalse(key.equals(namedKey));
		assertFalse(linkedKey.equals(hashKey));
		assertFalse(hashKey.equals(linkedKey));
		assertFalse(namedKey.equals(key));
		assertFalse(namedKey.equals(loggingKey));
		assertFalse(loggingKey.equals(namedKey));
		assertEquals(Key.of(Map.class, Named.class), namedKey);
		assertEquals(Key.of(Map.class), key);
		assertNotSame(Key.of(Map.class), key);
		assertEquals(Key.of(Map.class, pAp.newNamed("hash")), hashKey);
	}
	
}
