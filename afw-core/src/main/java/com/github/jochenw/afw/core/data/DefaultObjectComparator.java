package com.github.jochenw.afw.core.data;

import java.lang.reflect.Array;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
		final Map<String,Object> expMap;
		if (pExpMap.getObject() instanceof Map) {
			@SuppressWarnings("unchecked")
			final Map<String,Object> map = (Map<String,Object>) pExpMap.getObject();
			expMap = map;
		} else {
			final Map<String,Object> map = new HashMap<>();
			expMap = map;
			pExpMap.forEach((s,o) -> map.put(s, o)); 
		}
		final Set<String> expKeys = new HashSet<>(expMap.keySet());
		pActMap.forEach((s,o) -> {
			if (expMap.containsKey(s)) {
				final int ctx = pContext.getContext();
				pContext.addContext(s);
				compare(expMap.get(s), o);
				pContext.restoreContext(ctx);
			} else {
				expKeys.remove(s);
				pContext.difference("Unexpected value: key=" + s + ", value=" + asString(o));
			}
		});
		for (String s : expKeys) {
			pContext.difference("No value present for key=" + s);
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
			} else if (isList(pExpObject)) {
				if (isList(pActObject)) {
					final ListWrapper expList = asList(pExpObject);
					final ListWrapper actList = asList(pActObject);
					compare(pContext, expList, actList);
				} else {
					pContext.difference("Expected a list, got " + asString(pActObject));
				}
			} else if (isAtomic(pExpObject)) {
				if (isAtomic(pActObject)) {
					if (!isEqual(pExpObject, pActObject)) {
						pContext.difference("Expected " + asString(pExpObject) + ", got "
								            + asString(pActObject));
					}
				} else {
					pContext.difference("Expected atomic value, got " + asString(pActObject));
				}
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
		} else if (pObject instanceof Number  ||  pObject instanceof Boolean) {
			return pObject.toString();
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
		return pObject instanceof Iterable  ||  pObject.getClass().isArray();
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
