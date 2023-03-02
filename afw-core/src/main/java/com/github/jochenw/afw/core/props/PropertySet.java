package com.github.jochenw.afw.core.props;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.SortedMap;
import java.util.function.Consumer;

import com.github.jochenw.afw.core.util.Objects;


/** <p>An alternative to the {@link Properties} class.
 * This class does support comments on properties,
 * and it does support UTF-8 as the character set for
 * property files.</p>
 * <p>Basically a {@link PropertySet} is a set of entries,
 * roughly like a {@link Map}. The main differences are:
 * <ol>
 *   <li>The {@link PropertySet.Entry entries} of a property
 *     set are triplets, not key/value pairs. The third
 *     element of the triplet is an (optional) comment, that
 *     describes the property.</li>
 *   
 * </ol>
 */
public class PropertySet {
	/** An entry in the property set. Basically a triplet, consisting of a key,
	 * a value, and an optional comment.
	 */
	public static class Entry {
		private final String key;
		/** The entries value.
		 */
		protected String value;
		/** The entries comment (Optional, may be null.)
		 */
		protected String comment;
		/** Creates a new entry with the given key, value, and comment.
		 * @param pKey The entries key.
		 * @param pValue The entries value,
		 * @param pComment The entries comment,
		 */
		public Entry(String pKey, String pValue, String pComment) {
			key = Objects.requireNonNull(pKey, "Key");
			value = Objects.requireNonNull(pValue, "Value");
			comment = pComment;
		}
		/** Returns the entries key.
		 * @return The entries key. Never null.
		 */
		public String getKey() { return key; }
		/** Returns the entries value.
		 * @return The entries value. Never null.
		 */
		public String getValue() { return value; }
		/** Returns the entries comment.
		 * @return The entries comment, if any, or null.
		 */
		public String getComment() { return comment; }
	}

	/** A mutable entry. This is, what {@link #edit(Consumer)} will see.
	 */
	public static class MutableEntry extends Entry {
		private MutableEntry successor;

		/** Creates a new instance with the given key, value, comment,
		 * successor, and predecessor.
		 * @param pKey The entries key.
		 * @param pValue The entries value,
		 * @param pComment The entries comment,
		 * @param pSuccessor The entries successor
		 */
		public MutableEntry(String pKey, String pValue, String pComment,
				            MutableEntry pSuccessor) {
			super(pKey, pValue, pComment);
			successor = pSuccessor;
		}
		/** Sets the entries value.
		 * @param pValue The entries value.
		 * @throws NullPointerException The value is null.
		 */
		public void setValue(String pValue) { value = Objects.requireNonNull(pValue, "Value"); }
		/** Sets the entries comment.
		 * @param pComment The entries comment. May be null.
		 */
		public void setComment(String pComment) { comment = pComment; }
	}

	/** Creates a new, empty instance.
	 */
	public PropertySet() {
	}
	/** Creates a new instance, which is a copy of the given instance. (Contains the same
	 * entries.)
	 * @param pSource The property set, which is being copied.
	 */
	public PropertySet(PropertySet pSource) {
		pSource.foreach((e) -> put(e.getKey(), e.getValue(), e.getComment()));
	}
	private final Map<String,MutableEntry> map = new HashMap<>();
	private MutableEntry first;
	private MutableEntry last;

	/** If the property set contains, so far, no entry with the
	 * given key: Adds a new entry to the end of the entry list,
	 * with the given value, and comment.
	 * Otherwise, updates the existing entry to have the given
	 * value, and comment. In the latter case, the entries position
	 * in the entry list is left unchanged.
	 * @param pKey The key of the created, or updated entry.
	 * @param pValue The value of the created, or updated entry.
	 * @param pComment The comment of the created, or updated entry.
	 * @return If a new entry has been created: Returns null.
	 *   Otherwise, returns the existing entry, with the previous
	 *   value, and comment.
	 */
	public Entry put(String pKey, String pValue, String pComment) {
		final MutableEntry entry = map.get(pKey);
		if (entry == null) {
			// Create a new entry, and add it to the end of the linked entry list.
			final MutableEntry newEntry = new MutableEntry(pKey, pValue, pComment, null);
			if (last != null) {
				last.successor = newEntry;
			}
			if (first == null) {
				first = newEntry;
			}
			last = newEntry;
			map.put(pKey, newEntry);
			return null;
		} else {
			final Entry result = new Entry(pKey, entry.value, entry.comment);
			entry.value = pValue;
			entry.comment = pComment;
			return result;
		}
	}

	/** <p>If the property set contains, so far, no entry with the
	 * given key: Adds a new entry to the end of the entry list,
	 * with the given value, and no comment (comment = null).
	 * Otherwise, updates the existing entry to have the given
	 * value, and no comment. In the latter case, the entries position
	 * in the entry list is left unchanged. In other words, this
	 * method is equivalent to the following:</p>
	 * <pre>put(pKey, pValue, null)</pre>
	 * @param pKey The key of the created, or updated entry.
	 * @param pValue The value of the created, or updated entry.
	 * @return If a new entry has been created: Returns null.
	 *   Otherwise, returns the existing entry, with the previous
	 *   value, and comment.
	 */
	public Entry setValue(String pKey, String pValue) {
		return put(pKey, pValue, null);
	}

	/** <p>If the property set contains, so far, no entry with the
	 * given key: Adds a new entry to the end of the entry list,
	 * with the given comment, and no value (value = "").
	 * Otherwise, updates the existing entry to have the given
	 * comment, leaving the value unchanged. In the latter case,
	 * the entries position in the entry list is left unchanged.
	 * In other words, this method is equivalent to the following:</p>
	 * <pre>
	 *   final Entry entry = getEntry(pKey);
	 *   if (entry == null) {
	 *     put(pKey, "", pComment);
	 *     return null;
	 *   } else {
	 *     return put(pKey, entry.pValue, pComment);
	 *   }
	 * </pre>
	 * @param pKey The key of the created, or updated entry.
	 * @param pComment The comment of the created, or updated entry.
	 * @return If a new entry has been created: Returns null.
	 *   Otherwise, returns the existing entry, with the previous
	 *   value, and comment.
	 */
	public Entry setComment(String pKey, String pComment) {
		 final Entry entry = map.get(pKey);
		 if (entry == null) {
		     put(pKey, "", pComment);
		     return null;
		 } else {
		     return put(pKey, entry.value, pComment);
		 }
	}

	/** Iterates over the entries in the property set in the
	 * order of entry creation.
	 * @param pConsumer A consumer, which is being invoked for all the
	 *   entries in the order of entry creation.
	 */
	public void foreach(Consumer<Entry> pConsumer) {
		for (MutableEntry en = first;  en != null;  en = en.successor) {
			pConsumer.accept(en);
		}
	}

	/** Iterates over the entries in the property set in the
	 * order of entry creation, permitting updates.
	 * @param pConsumer A consumer, which is being invoked for all the
	 *   entries in the order of entry creation.
	 */
	public void edit(Consumer<MutableEntry> pConsumer) {
		for (MutableEntry en = first;  en != null;  en = en.successor) {
			pConsumer.accept(en);
		}
	}

	/** Converts the entry set into a map. The map keys, and the
	 * values are the keys, and values of the entries in the
	 * property set. Basically the map contains the same entries,
	 * without the comments, and no order. This is equivalent to
	 * <pre>
	 *   final Map<String,String> map = new HashMap<>();
	 *   putAll(map);
	 *   return map;
	 * </pre>
	 * @return The converted property set.
	 */
	public Map<String,String> asMap() {
		final Map<String,String> map = new HashMap<String,String>();
		putAll(map);
		return map;
	}

	/** Converts the entry set into [@link Properties properties object}.
	 * The property keys, and the values are the keys, and values of the
	 * entries in the property set. Basically the properties object
	 * contains the same entries, without the comments, and no order.
	 * @return The converted property set.
	 */
	public Properties asProperties() {
		final Properties properties = new Properties();
		foreach((e) -> properties.setProperty(e.getKey(), e.getValue()));
		return properties;
	}

	/** <p>Copies the entry set into a map. The map keys, and the
     * values are the keys, and values of the entries in the
     * property set. Basically the map contains the same entries,
     * without the comments. If the parameter {@code pMap} is
     * an {@link SortedMap}, then the map will retain the
     * order of the property set.</p>
     * @param pMap The map, to which the property set will copy
     *   its entries by invoking {@link Map#put(Object,Object)}
     *   for every entry, in the property sets order.
     */
	public void putAll(Map<String,String> pMap) {
		foreach((e) -> pMap.put(e.getKey(), e.getValue()));
	}

	/** <p>Copies the entry set into another {@link PropertySet}.</p>
     * @param pPropertySet The map, to which the property set will copy
     *   its entries by invoking {@link Map#put(Object,Object)}
     *   for every entry, in the property sets order.
     */
	public void putAll(PropertySet pPropertySet) {
		foreach((e) -> pPropertySet.put(e.getKey(), e.getValue(), e.getComment()));
	}

	/** Returns a thread safe copy of the current property set.
	 * The copy is <em>not</em> backed by the current property
	 * set: Modifications of this property set will <em>not</em>
	 * the copy, and vice versa.
	 * @return A thread safe copy of the current property set.
	 */
	public PropertySet synchronizedPropertySet() {
		final PropertySet result = new PropertySet(this) {
			@Override
			public synchronized Entry put(String pKey, String pValue, String pComment) {
				return super.put(pKey, pValue, pComment);
			}

			@Override
			public synchronized Entry setValue(String pKey, String pValue) {
				return super.setValue(pKey, pValue);
			}

			@Override
			public synchronized Entry setComment(String pKey, String pComment) {
				return super.setComment(pKey, pComment);
			}

			@Override
			public synchronized void foreach(Consumer<Entry> pConsumer) {
				super.foreach(pConsumer);
			}

			@Override
			public synchronized void edit(Consumer<MutableEntry> pConsumer) {
				super.edit(pConsumer);
			}

			@Override
			public synchronized Map<String, String> asMap() {
				return super.asMap();
			}

			@Override
			public synchronized Properties asProperties() {
				return super.asProperties();
			}

			@Override
			public synchronized void putAll(Map<String, String> pMap) {
				super.putAll(pMap);
			}

			@Override
			public synchronized PropertySet synchronizedPropertySet() {
				return super.synchronizedPropertySet();
			}

			@Override
			public synchronized int size() {
				return super.size();
			}

			@Override
			public synchronized boolean isEmpty() {
				return super.isEmpty();
			}

			@Override
			public synchronized void clear() {
				super.clear();
			}

			@Override
			public synchronized Entry getEntry(String pKey) {
				return super.getEntry(pKey);
			}

			@Override
			public synchronized String getValue(String pKey) {
				return super.getValue(pKey);
			}

			@Override
			public synchronized String getComment(String pKey) {
				return super.getComment(pKey);
			}
		};
		return result;
	}

	/** Returns the number of entries in the property set.
	 * @return The number of entries in the property set.
	 */
	public int size() {
		return map.size();
	}

	/** Returns true, if the property set is empty. Otherwise, returns false.
	 * @return True, if the property set is empty, otherwise false.
	 */
	public boolean isEmpty() {
		return map.isEmpty();
	}

	/** Clears the property set.
	 */
	public void clear() {
		map.clear();
		first = null;
		last = null;
	}

	/** Returns the entry with the given key, if any, or null.
	 * @param pKey The requested entries key.
	 * @return The requested entry, if present, or null.
	 */
	public Entry getEntry(String pKey) {
		return map.get(pKey);
	}

	/** Returns the value of the entry with the given key, if any, or null.
	 * @param pKey The requested entries key.
	 * @return The requested value, if that entry exists, or null.
	 */
	public String getValue(String pKey) {
		final Entry entry = map.get(pKey);
		if (entry == null) {
			return null;
		} else {
			return entry.getValue();
		}
	}

	/** Returns the comment of the entry with the given key, if any, or null.
	 * @param pKey The requested entries key.
	 * @return The requested comment, if that entry exists, or null. <em>Note:</em>
	 *   A null value does not indicate, that such an entry does not exist, because
	 *   comments may be null. On the other hand, a non-null value is proof,
	 *   that there is an entry with the given key.
	 */
	public String getComment(String pKey) {
		final Entry entry = map.get(pKey);
		if (entry == null) {
			return null;
		} else {
			return entry.getComment();
		}
	}
}
