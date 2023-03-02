package com.github.jochenw.afw.core.props;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.function.Consumer;

import org.junit.Test;

import com.github.jochenw.afw.core.props.PropertySet.Entry;


/** Test suite for the {@link PropertySet} class.
 */
public class PropertySetTest {
	/** Tests creating a new, empty, property set.
	 */
	@Test
	public void testCreateEmptyInstance() {
		final PropertySet ps = new PropertySet();
		assertTrue(ps.isEmpty());
		assertEquals(0, ps.size());
	}

	/** Tests creating a property set with a single entries.
	 */
	@Test
	public void testSingleEntry() {
		final PropertySet ps = new PropertySet();
		ps.put("foo", "bar", "Description of foo");
		assertSame(ps, "foo", "bar", "Description of foo");
	}

	/** Tests creating a property set with several entries.
	 */
	@Test
	public void testSeveralEntries() {
		final PropertySet ps = new PropertySet();
		ps.put("foo", "bar", "Description of foo");
		ps.put("answer", "42", "The answer");
		ps.put("boolean", "true", null);
		final String[] entries = new String[] {
				"foo", "bar", "Description of foo",
				"answer", "42", "The answer",
				"boolean", "true", null
			};
		final String[] entriesAfterValueChange = new String[] {
				"foo", "bar", "Description of foo",
				"answer", "42", "The answer",
				"boolean", "false", null
			};
		final String[] entriesAfterCommentChange = new String[] {
				"foo", "bar", "Description of foo",
				"answer", "42", "The famous answer",
				"boolean", "false", null
			};
		final String[] entriesWithBarProperty = new String[] {
				"foo", "bar", "Description of foo",
				"answer", "42", "The answer",
				"boolean", "true", null,
				"bar", "", "The bar"
			};
		final PropertySet sps = ps.synchronizedPropertySet();
		assertNotSame(ps,  sps);
		final Consumer<PropertySet> validator = (vps) -> {
			assertSame(vps, entries);
			vps.setValue("boolean", "false");
			assertSame(vps, entriesAfterValueChange);
			vps.setComment("answer", "The famous answer");
			assertSame(vps, entriesAfterCommentChange);
			vps.setComment("boolean", null);
			assertSame(vps, entriesAfterCommentChange);
			assertNull(vps.getValue("unknown"));
			assertNull(vps.getComment("unknown"));
			// Undo the changes in the comment.
			vps.edit((e) -> {
				if ("answer".equals(e.getKey())) {
					e.setComment("The answer");
				}
			});
			assertSame(vps, entriesAfterValueChange);
			vps.edit((e) -> {
				if ("boolean".equals(e.getKey())) {
					e.setValue("true");
				}
			});
			assertSame(vps, entries);
			final PropertySet extendedPs = new PropertySet(vps);
			extendedPs.setComment("bar", "The bar");
			assertSame(extendedPs, entriesWithBarProperty);
			final PropertySet extendedPs2 = vps.synchronizedPropertySet();
			extendedPs2.setComment("bar", "The bar");
			assertSame(extendedPs2, entriesWithBarProperty);
			assertFalse(vps.isEmpty());
			vps.clear();
			assertTrue(vps.isEmpty());
			assertEquals(0, vps.size());
			assertSame(vps);
		};
		validator.accept(ps);
		validator.accept(sps);
	}

	private void assertSame(PropertySet pPropertySet, String... pTriplets) {
		assertEquals(pPropertySet.size(), pTriplets.length/3);
		for (int i = 0;  i < pTriplets.length;  i += 3) {
			final String key = pTriplets[i];
			final String value = pTriplets[i+1];
			final String comment = pTriplets[i+2];
			final Entry entry = pPropertySet.getEntry(key);
			assertNotNull(entry);
			assertEquals(value, entry.getValue());
			assertEquals(value, pPropertySet.getValue(key));
			if (comment == null) {
				assertNull(entry.getComment());
				assertNull(pPropertySet.getComment(key));
			} else {
				assertEquals(comment, entry.getComment());
				assertEquals(comment, pPropertySet.getComment(key));
			}
		}
		final List<String> list = new ArrayList<>();
		pPropertySet.foreach((e) -> {
			list.add(e.getKey());
			list.add(e.getValue());
			list.add(e.getComment());
		});
		assertEquals(pTriplets.length, list.size());
		for (int i = 0;  i < list.size();  i++) {
			final String s = pTriplets[i];
			if (s == null) {
				assertNull(list.get(i));
			} else {
				assertEquals(s, list.get(i));
			}
		}
		final Map<String,String> map = pPropertySet.asMap();
		final Properties properties = pPropertySet.asProperties();
		final PropertySet ps2 = new PropertySet();
		pPropertySet.putAll(ps2);
		assertEquals(pPropertySet.size(), map.size());
		assertEquals(pPropertySet.size(), properties.size());
		assertEquals(pPropertySet.size(), ps2.size());
		for (int i = 0;  i < pTriplets.length;  i += 3) {
			final String key = pTriplets[i];
			final String value = pTriplets[i+1];
			assertEquals(value, map.get(key));
			assertEquals(value, properties.getProperty(key));
			assertEquals(value, ps2.getValue(key));
			final String comment = pTriplets[i+2];
			if (comment == null) {
				assertNull(ps2.getComment(key));
			} else {
				assertEquals(comment, ps2.getComment(key));
			}
		}
	}
}
