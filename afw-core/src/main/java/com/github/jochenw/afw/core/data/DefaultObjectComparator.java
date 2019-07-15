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

public class DefaultObjectComparator implements IObjectComparator {
	public static class Context {
		private final Listener listener;
		private final StringBuilder sb = new StringBuilder();
		public Context(Listener pListener) {
			listener = pListener;
		}
		public int getContext() {
			return sb.length();
		}
		public void restoreContext(int pPreviousContext) {
			if (pPreviousContext > sb.length()) {
				throw new IllegalStateException("Unable to restore context to length"
						+ pPreviousContext + ", current length is " + sb.length());
			}
			sb.setLength(pPreviousContext);
		}
		public void addContext(String pContextExtension) {
			if (sb.length() > 0) {
				sb.append('.');
			}
			sb.append(pContextExtension);
		}
		public void difference(String pDescription) {
			listener.difference(sb.toString(), pDescription);
		}
	}
	public abstract static class MapWrapper {
		private final String prefix, suffix;
		private final Object object;
		protected MapWrapper(String pPrefix, String pSuffix, Object pObject) {
			prefix = pPrefix;
			suffix = pSuffix;
			object = pObject;
		}
	    public String getPrefix() { return prefix; }
		public String getSuffix() { return suffix; }
		public Object getObject() { return object; }
		public abstract void forEach(BiConsumer<String,Object> pConsumer);
	}
	public abstract static class ListWrapper {
		private final String prefix, suffix;
		protected ListWrapper(String pPrefix, String pSuffix) {
			prefix = pPrefix;
			suffix = pSuffix;
		}
	    public String getPrefix() { return prefix; }
		public String getSuffix() { return suffix; }
		public abstract void forEach(Consumer<Object> pConsumer);
	}

	@Override
	public void compare(Listener pListener, Object pExpectedObject, Object pActualObject) {
		compare(new Context(pListener), pExpectedObject, pActualObject);
	}

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
	 */
	protected boolean isEqual(Object pExpectedObject, Object pActualObject) {
		return pExpectedObject.equals(pActualObject);
	}

	protected boolean isAtomic(Object pObject) {
		return pObject instanceof String
			||  pObject instanceof Number
			||  pObject instanceof Boolean;
	}

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

	protected boolean isMap(Object pObject) {
		return pObject instanceof Map;
	}

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

	protected boolean isList(Object pObject) {
		return pObject instanceof Iterable
				||  pObject instanceof Iterator
				||  pObject.getClass().isArray();
	}

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
