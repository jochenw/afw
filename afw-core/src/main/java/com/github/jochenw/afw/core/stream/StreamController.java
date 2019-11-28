package com.github.jochenw.afw.core.stream;

import java.lang.reflect.Field;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.BiConsumer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.github.jochenw.afw.core.util.Exceptions;
import com.github.jochenw.afw.core.util.Functions.FailableBiConsumer;
import com.github.jochenw.afw.core.util.Reflection;


/** A helper class for implementations of {@link StreamReader},
 * or {@link StreamWriter}. The purpose of the helper class is
 * to provide {@link StreamController.MetaData meta data information} about objects,
 * which are annotated with {@link Streamable}.
 */
public class StreamController {
	/** The information, which is being provided by the {@link StreamController}
	 * for a given streamable type.
	 */
	public static class MetaData {
		/** Doesn't really matter, which Locale we use, so we might as well use
		 * my native one.
		 */
		private static final Locale SORT_LOCALE = Locale.GERMANY;
		private final Map<String,Field> map = new HashMap<>();
		private final List<String>  ids = new ArrayList<>();

		/**
		 * Called to add a given field to the types meta data.
		 * @param pId The fields id. This is the value of the
		 *    {@link Streamable streamable annotations} id attribute (if present),
		 *    or the fields name.
		 * @param pField The field being added.
		 * @return An existing field, which has previously been registered
		 *   with the same id, if present, or null.
		 */
		public Field add(@Nonnull String pId, @Nonnull Field pField) {
			ids.clear();
			return map.putIfAbsent(pId, pField);
		}

		/** Queries the given {@code pObject} for the value of the field with the
		 * given {@code pId}.
		 * @param pId The field id.
		 * @param pObject The object to query. In other words, an object, which is
		 *   currently being written.
		 * @return The objects field value for the field, given by the id.
		 */
		public @Nullable Object getValue(@Nonnull String pId, @Nonnull Object pObject) {
			final Field f = map.get(pId);
			if (f == null) {
				throw new IllegalArgumentException("Invalid field id: " + pId);
			}
			try {
				if (!f.isAccessible()) {
					f.setAccessible(true);
				}
				return f.get(pObject);
			} catch (Throwable t) {
				throw Exceptions.show(t);
			}
		}
		/** Sets the value of the given {@code pObject} for the field with the
		 * given {@code pId}.
		 * @param pId The field id.
		 * @param pObject The object to modify. In other words, an object, which is
		 *   currently being read.
		 * @param pValue The objects field value for the field, given by the id.
		 */
		public void setValue(@Nonnull String pId, @Nonnull Object pObject, @Nullable Object pValue) {
			final Field f = map.get(pId);
			if (f == null) {
				throw new IllegalArgumentException("Invalid field id: " + pId);
			}
			try {
				if (!f.isAccessible()) {
					f.setAccessible(true);
				}
				f.set(pObject, pValue);
			} catch (Throwable t) {
				throw Exceptions.show(t);
			}
		}

		/** Iterates over the streamable fields.
		 * @param pConsumer The consumer to invoke for every streamable field.
		 */
		public void forEach(FailableBiConsumer<String,Field,?> pConsumer) {
			if (ids.isEmpty()) {
				ids.addAll(map.keySet());
				final Collator collator = Collator.getInstance(SORT_LOCALE);
				final Comparator<String> comparator = (s1, s2) -> {
					return collator.compare(s1.toLowerCase(SORT_LOCALE), s2.toLowerCase(SORT_LOCALE));
				};
				Collections.sort(ids, comparator);
			}
			final BiConsumer<String,Field> consumer = (s,f) -> {
				try {
					pConsumer.accept(s,f);
				} catch (Throwable t) {
					throw Exceptions.show(t);
				}
			};
			for (String id : ids) {
				consumer.accept(id, map.get(id));
			}
		}

	}

	private final Map<Class<?>,MetaData> metaDataMap = new HashMap<>();

	/**
	 * Returns the given types meta data.
	 * @param pType The type, for which metadata is being seeked.
	 * @return The given types {@link MetaData}.
	 * @throws IllegalArgumentException The type is not annotated with {@link Streamable}.
	 */
	public MetaData getMetaData(Class<?> pType) {
		return metaDataMap.computeIfAbsent(pType, (t) -> newMetaData(t));
	}

	protected MetaData newMetaData(Class<?> pType) {
		if (!pType.isAnnotationPresent(Streamable.class)) {
			throw new IllegalArgumentException("The type "
					+ pType.getName() + " is not annotated with @Streamable.");
		}
		final MetaData metaData = new MetaData();
		Reflection.findFields(pType,
				              (f) -> {
				            	  final Streamable streamable = f.getAnnotation(Streamable.class);
				            	  if (streamable != null) {
				            		  String id = streamable.id();
				            		  if (id.length() == 0) {
				            			  id = f.getName();
				            		  }
				            		  final Field oldField = metaData.add(id, f);
				            		  if (oldField != null) {
				            			  throw new IllegalStateException("Duplicate field Id "
				            					                          + id + " in type "
				            					                          + pType.getName()
				            					                          + " for fields"
				            					                          + oldField + ", and "
				            					                          + f);
				            		  }
				            	  }
				              });
		return metaData;
	}
}
