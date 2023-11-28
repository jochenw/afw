package com.github.jochenw.afw.core.data;

import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;


/**
 * Default implementation of {@link IObjectComparator}.
 */
public class DefaultObjectComparator implements IObjectComparator {
	/** This object is used to calculate the location of a difference.
	 *(The first argument of {@link IObjectComparator.Listener#difference(String, String)}.
	 */
	public static class Context {
		private final Listener listener;
		private final StringBuilder sb = new StringBuilder();
		/** Creates a new instance with the given listener.
		 * @param pListener The listener, which is being invoked
		 *   in case of differences.
		 */
		public Context(Listener pListener) {
			listener = pListener;
		}
		/** Returns the current length of the location (context)
		 * string.
		 * @return The current length of the location (context)
		 * string.
		 */
		public int getContext() {
			return sb.length();
		}
		/** Restores a previous location string by truncating the
		 * current location string to the given length.
		 * @param pPreviousContext The length of the new,
		 *   truncated location string.
		 */
		public void restoreContext(int pPreviousContext) {
			if (pPreviousContext > sb.length()) {
				throw new IllegalStateException("Unable to restore context to length"
						+ pPreviousContext + ", current length is " + sb.length());
			}
			sb.setLength(pPreviousContext);
		}
		/** Extends the current location string by appending the given
		 * suffix (typically the name of a property, which is currently
		 * being compared recursively.
		 * @param pContextExtension The suffix, which is being appended
		 *   to the location string.
		 */
		public void addContext(String pContextExtension) {
			if (sb.length() > 0) {
				sb.append('.');
			}
			sb.append(pContextExtension);
		}
		/** Called to report a difference with the given description,
		 * and the current location string.
		 * @param pDescription The difference description.
		 */
		public void difference(String pDescription) {
			listener.difference(sb.toString(), pDescription);
		}
	}

	/** A wrapper for map-like objects, or collections of
	 * key/value pairs.
	 */
	public abstract static class MapWrapper {
		private final String prefix, suffix;
		private final Object object;
		/** Creates a new {@link Map map} wrapper.
		 * @param pPrefix The prefix to use for string serialization of the wrapped object.
		 * @param pSuffix The suffix to use for string serialization of the wrapped object.
		 * @param pObject The wrapped object.
		 */
		protected MapWrapper(String pPrefix, String pSuffix, Object pObject) {
			prefix = pPrefix;
			suffix = pSuffix;
			object = pObject;
		}
		/** Returns the prefix, which is being used to
		 * create a textual representation of the underlying object.
		 * @return The prefix, which is being used to
		 * create a textual representation of the underlying object.
		 */
	    public String getPrefix() { return prefix; }
		/** Returns the suffix, which is being used to
		 * create a textual representation of the underlying object.
		 * @return The suffix, which is being used to
		 * create a textual representation of the underlying object.
		 */
		public String getSuffix() { return suffix; }
		/** Returns the wrapped object.
		 * @return The underlying object.
		 */
		public Object getObject() { return object; }
		/** Called to iterate over the key/value pairs in the wrapped
		 * object, in no particular order.
		 * @param pConsumer The consumer, which is being invoked for
		 *   every key/value pair in the wrapped object.
		 */
		public abstract void forEach(BiConsumer<String,Object> pConsumer);
	}
	/** A wrapper for map-like objects, or ordered collections of
	 * objects.
	 */
	public abstract static class ListWrapper {
		private final String prefix, suffix;
		/** Creates a new {@link List list} wrapper.
		 * @param pPrefix The prefix to use for string serialization of the wrapped object.
		 * @param pSuffix The suffix to use for string serialization of the wrapped object.
		 */
		protected ListWrapper(String pPrefix, String pSuffix) {
			prefix = pPrefix;
			suffix = pSuffix;
		}
		/** Returns the prefix, which is being used to
		 * create a textual representation of the underlying object.
		 * @return The prefix, which is being used to
		 * create a textual representation of the underlying object.
		 */
	    public String getPrefix() { return prefix; }
		/** Returns the suffix, which is being used to
		 * create a textual representation of the underlying object.
		 * @return The suffix, which is being used to
		 * create a textual representation of the underlying object.
		 */
		public String getSuffix() { return suffix; }
		/** Called to iterate over the elements in the wrapped
		 * object, in the wrapped objects natural order.
		 * @param pConsumer The consumer, which is being invoked for
		 *   every element in the wrapped object.
		 */
		public abstract void forEach(Consumer<Object> pConsumer);
	}

	@Override
	public void compare(Listener pListener, Object pExpectedObject, Object pActualObject) {
		compare(new Context(pListener), pExpectedObject, pActualObject);
	}

	/** Called to compute the differences between two map-like objects.
	 * @param pContext The computation context.
	 * @param pExpMap The expected map-like object.
	 * @param pActMap The actual map-like object.
	 */
	protected void compare(Context pContext, MapWrapper pExpMap, MapWrapper pActMap) {
		final Map<String,Object> expMap = new HashMap<>();
		pExpMap.forEach((s,o) -> expMap.put(s, o));
		final Map<String,Object> actMap = new HashMap<>();
		pActMap.forEach((s,o) -> actMap.put(s, o));
		final int contextLength = pContext.getContext();
		final Consumer<String> contextAdder = (s) -> {
			if (contextLength == 0) {
				pContext.addContext(s);
			} else {
				pContext.addContext(".");
				pContext.addContext(s);
			}
		};
		final List<String> expectedKeysMissing = new ArrayList<String>();
		expMap.forEach((s,o) -> {
			if (actMap.containsKey(s)) {
				final Object actObject = actMap.remove(s);
				contextAdder.accept(s);
				compare(pContext, o, actObject);
				pContext.restoreContext(contextLength);
				actMap.remove(s);
			} else {
				expectedKeysMissing.add(s);
			}
		});
		for (String s : expectedKeysMissing) {
			final Object o = expMap.get(s);
			contextAdder.accept(s);
			pContext.difference("Expected element not found: " + asString(o));
			pContext.restoreContext(contextLength);
		}
		actMap.forEach((s, o) -> {
			contextAdder.accept(s);
			pContext.difference("Unexpected element: " + asString(o));
			pContext.restoreContext(contextLength);
		});
	}

	/** Called to compute the differences between two list-like objects.
	 * @param pContext The computation context.
	 * @param pExpList The expected map-like object.
	 * @param pActList The actual map-like object.
	 */
	protected void compare(Context pContext, ListWrapper pExpList, ListWrapper pActList) {
		final List<Object> expList = new ArrayList<>();
		pExpList.forEach((o) -> expList.add(o));
		final List<Object> actList = new ArrayList<>();
		pActList.forEach((o) -> actList.add(o));
		final int contextLength = pContext.getContext();
		final int num = Math.min(expList.size(), actList.size());
		for (int i = 0;  i < num;  i++) {
			final Object expObject = expList.get(i);
			final Object actObject = actList.get(i);
			pContext.addContext("[" + i + "]");
			compare(pContext, expObject, actObject);
			pContext.restoreContext(contextLength);
		}
		if (expList.size() > actList.size()) {
			for (int i = num;  i < expList.size();  i++) {
				pContext.addContext("[" + i + "]");
				pContext.difference("Expected object not found: " + asString(expList.get(i)));
				pContext.restoreContext(contextLength);
			}
		} else if (actList.size() > expList.size()) {
			for (int i = num;  i < actList.size();  i++) {
				pContext.addContext("[" + i + "]");
				pContext.difference("Unexpected object found: " + asString(actList.get(i)));
				pContext.restoreContext(contextLength);
			}
		}
	}

	/** Called to compute the differences between two objects.
	 * @param pContext The computation context.
	 * @param pExpObject The expected map-like object.
	 * @param pActObject The actual map-like object.
	 */
	protected void compare(Context pContext, Object pExpObject, Object pActObject) {
		if (pExpObject == null) {
			if (pActObject != null) {
				pContext.difference("Expected null, got " + asString(pActObject));
			}
		} else {
			if (pActObject == null) {
				pContext.difference("Expected " + asString(pExpObject) + ", got null");
			}
			if (isMap(pExpObject)) {
			    if (isMap(pActObject)) {
				    final MapWrapper expMap = asMap(pExpObject);
				    final MapWrapper actMap = asMap(pActObject);
				    compare(pContext, expMap, actMap);
			    } else {
				    pContext.difference("Expected a map, got " + asString(pActObject));
			    }
			    return;
			} else if (isList(pExpObject)) {
				if (isList(pActObject)) {
					final ListWrapper expList = asList(pExpObject);
					final ListWrapper actList = asList(pActObject);
					compare(pContext, expList, actList);
				} else {
					pContext.difference("Expected a list, got " + asString(pActObject));
				}
				return;
			} else if (isAtomic(pExpObject)) {
				if (isAtomic(pActObject)) {
					if (!isEqual(pExpObject, pActObject)) {
						pContext.difference("Expected " + asString(pExpObject) + ", got "
								            + asString(pActObject));
					}
				} else {
					pContext.difference("Expected atomic value, got " + asString(pActObject));
				}
				return;
			} else {
				pContext.difference("Expected object is not atomic, but "
						+ pExpObject.getClass().getName());
			}
		}
	}

	/**
	 * Tests, whether the given objects are equal. The caller must ensure, that both objects
	 * are non-null, and atomic.
	 * @param pExpectedObject The expected object.
	 * @param pActualObject The actual object
	 * @return True, if the objects are considered equal, otherwise false.
	 */
	protected boolean isEqual(Object pExpectedObject, Object pActualObject) {
		return pExpectedObject.equals(pActualObject);
	}

	/** Returns, whether the given object is an atomic data entity.
	 * @param pObject The object, that is being checked.
	 * @return True, if the given object is atomic, otherwise false.
	 */
	protected boolean isAtomic(Object pObject) {
		return pObject instanceof String
			||  pObject instanceof Number
			||  pObject instanceof Boolean;
	}

	/** Converts the given object into a string.
	 * @param pObject The object. that is being serialized.
	 * @return The serialized object.
	 */
	protected String asString(Object pObject) {
		if (pObject == null) {
			return "null";
		} else if (pObject instanceof String) {
			final StringBuilder sb = new StringBuilder();
			sb.append('"');
			sb.append(pObject);
			sb.append('"');
			return sb.toString();
		} else if (pObject instanceof Boolean) {
			return pObject.toString();
		} else if (pObject instanceof Long) {
			return pObject.toString() + "l";
		} else if (pObject instanceof Integer) {
			return pObject.toString() + "i";
		} else if (pObject instanceof Short) {
			return pObject.toString() + "s";
		} else if (pObject instanceof Byte) {
			return pObject.toString();
		} else if (pObject instanceof BigInteger) {
			return pObject.toString() + "bi";
		} else if (pObject instanceof BigDecimal) {
			return pObject.toString() + "bd";
		} else {
			return pObject.getClass().getName() + ": " + pObject.toString();
		}
	}

	/** Tests, whether the given object is a map-like object. If so, it is valid
	 * to invoke {@link #asMap(Object)} with it.
	 * @param pObject The object, that is being tested.
	 * @return True, if {@link #asMap(Object)} may be invoked with the same object.
	 */
	protected boolean isMap(Object pObject) {
		return pObject instanceof Map;
	}

	/** Converts the given object into a {@link ListWrapper}. Assumes,
	 * that {@link #isMap(Object)} has returned true for the same object.
	 * @param pObject The object, that is being tested.
	 * @return A {@link Map map} wrapper.
	 */
	protected MapWrapper asMap(Object pObject) {
		@SuppressWarnings("unchecked")
		final Map<String,Object> map = (Map<String,Object>) pObject;
		return new MapWrapper("{", "}", map) {
			@Override
			public void forEach(BiConsumer<String, Object> pConsumer) {
				for (Map.Entry<String,Object> en : map.entrySet()) {
					pConsumer.accept(en.getKey(), en.getValue());
				}
			}
		};
	}

	/** Tests, whether the given object is a list-like object. If so, it is valid
	 * to invoke {@link #asList(Object)} with it.
	 * @param pObject The object, that is being tested.
	 * @return True, if {@link #asList(Object)} may be invoked with the same object.
	 */
	protected boolean isList(Object pObject) {
		return pObject instanceof Iterable
				||  pObject instanceof Iterator
				||  pObject.getClass().isArray();
	}

	/** Converts the given object into a map-like object. Assumes, that {@link #isMap(Object)}
	 * has returned true for the same object.
	 * @param pObject The object, that is being tested.
	 * @return A {@link Map map} wrapper.
	 */
	protected ListWrapper asList(Object pObject) {
		if (pObject instanceof List) {
			@SuppressWarnings("unchecked")
			final List<Object> list = (List<Object>) pObject;
			return new ListWrapper("<", ">") {
				@Override
				public void forEach(Consumer<Object> pConsumer) {
					final int length = list.size();
					for (int i = 0;  i < length;  i++) {
						pConsumer.accept(list.get(i));
					}
				}
			};
		} else if (pObject instanceof Iterable) {
			@SuppressWarnings("unchecked")
			final Iterable<Object> iterable = (Iterable<Object>) pObject;
			return new ListWrapper("(", ")") {
				@Override
				public void forEach(Consumer<Object> pConsumer) {
					for (Object o : iterable) {
						pConsumer.accept(o);
					}
				}
			};
		} else if (pObject instanceof Iterator) {
			@SuppressWarnings("unchecked")
			final Iterator<Object> iter = (Iterator<Object>) pObject;
			return new ListWrapper("((", "))") {
				@Override
				public void forEach(Consumer<Object> pConsumer) {
					while (iter.hasNext()) {
						pConsumer.accept(iter.next());
					}
				}
				
			};
			
		} else {
			return new ListWrapper("[", "]") {
				@Override
				public void forEach(Consumer<Object> pConsumer) {
					final int length = Array.getLength(pObject);
					for (int i = 0;  i < length;  i++) {
						pConsumer.accept(Array.get(pObject, i));
					}
				}
			};
		}
	}
}
