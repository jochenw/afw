package com.github.jochenw.afw.core.template;

import static org.junit.Assert.*;

import java.io.StringWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

import org.junit.Test;

import com.github.jochenw.afw.core.template.ITemplateEngine.Template;


/**
 * Test suite of the {@link SimpleTemplateEngine}.
 */
public class SimpleTemplateEngineTest {
	/**
	 * Test, whether we can do simple variable interpolation.
	 */
	@Test
	public void testSimpleInterpolation() {
		final String templateText = "Hello, ${user}!\n"
				+ "This is a list of objects: ${map.foo} ${map.bar} ${map.a.b}.\n";
		final String expect = "Hello, World!\n"
				+ "This is a list of objects: 42 baz true.\n";

		final SimpleTemplateEngine ste = SimpleTemplateEngine.newInstance();
		final Template<Map<String,Object>> template = ste.compile(templateText);
		final Map<String,Object> model = new HashMap<>();
		final Map<String,Object> map = new HashMap<>();
		model.put("user", "World");
		map.put("foo", Integer.valueOf(42));
		map.put("bar", "baz");
		final Map<String,Object> aMap = new HashMap<>();
		aMap.put("b", Boolean.TRUE);
		model.put("map", map);
		map.put("a", aMap);
		final String got = run(template, model);
		System.out.print(got);
		assertEquals(expect, got);
	}

	private String run(final Template<Map<String, Object>> pTemplate, final Map<String, Object> pModel) {
		final StringWriter sw = new StringWriter();
		pTemplate.write(pModel, sw);
		final String got = sw.toString();
		return got;
	}

	/**
	 * Test, whether an if expresion works.
	 */
	@Test
	public void testIfExpression() {
		final String templateText = "Number of objects ${numberOfObjects}\n"
				+ "\n"
				+ "<%if numberOfObjects == 0%>\n"
				+ "No details available.\n"
				+ "<%else%>\n"
				+ "Details:\n"
				+ "<%/if%>\n";
		final String expectWithZero = "Number of objects 0\n"
				+ "\n"
				+ "No details available.\n";
		final String expectWithNonZero = "Number of objects 1\n"
				+ "\n"
				+ "Details:\n";
		final SimpleTemplateEngine ste = SimpleTemplateEngine.newInstance();
		final Template<Map<String,Object>> template = ste.compile(templateText);
		final Map<String,Object> model = new HashMap<>();
		model.put("numberOfObjects", Integer.valueOf(0));
		assertEquals(expectWithZero, run(template, model));
		model.put("numberOfObjects", Integer.valueOf(1));
		assertEquals(expectWithNonZero, run(template, model));
		//model.put("numberOfObjects", "0");
		//assertEquals(expectWithZero, run(template, model));
		//model.put("numberOfObjects", "1");
		//assertEquals(expectWithNonZero, run(template, model));
	}

	/** Test of a for loop.
	 */
	@Test
	public void testForLoop() {
		final String templateText = "Number of objects ${numberOfObjects}\n"
				+ "\n"
				+ "<%if numberOfObjects == 0%>\n"
				+ "No details available.\n"
				+ "<%else%>\n"
				+ "Details:\n"
				+ "<%for o in objects%>\n"
				+  "  ${o}\n"
				+ "<%/for%>\n"
				+ "<%/if%>\n";
		final String expectWithZero = "Number of objects 0\n"
				+ "\n"
				+ "No details available.\n";
		final String expectWithNonZero = "Number of objects 3\n"
				+ "\n"
				+ "Details:\n"
				+ "  12\n"
				+ "  3\n"
				+ "  42\n";
		final SimpleTemplateEngine ste = SimpleTemplateEngine.newInstance();
		final Template<Map<String,Object>> template = ste.compile(templateText);
		final Map<String,Object> model = new HashMap<>();
		model.put("numberOfObjects", Integer.valueOf(0));
		model.put("objects", Arrays.asList(Integer.valueOf(12), "3", Integer.valueOf(42)));
		assertEquals(expectWithZero, run(template, model));
		model.put("numberOfObjects", Integer.valueOf(3));
		assertEquals(expectWithNonZero, run(template, model));
		//model.put("numberOfObjects", "0");
		//assertEquals(expectWithZero, run(template, model));
		//model.put("numberOfObjects", "1");
		//assertEquals(expectWithNonZero, run(template, model));
	}

	/** Test of non-atomic properties, and of casting a string to a boolean value.
	 */
	@Test
	public void testNonAtomicProperties() {
		final String templateText =
				"Text line 1\n"
				+ "Text line 2\n"
				+ "<%if feature.foo %>\n"
				+ "Text line 3\n"
				+ "<%/if%>\n";
		final SimpleTemplateEngine ste = new SimpleTemplateEngine();
		final Template<Map<String,Object>> template = ste.compile(templateText);
		final Map<String,Object> map1 = new HashMap<>();
		map1.put("feature.foo", "true");
		final BiConsumer<String,Object> tester1 = (s,o) -> {
			map1.put("feature.foo", o);
			final StringWriter sw = new StringWriter();
			template.write(map1, sw);
			assertEquals(s, sw.toString());
		};
		final String threeLines = "Text line 1\nText line 2\nText line 3\n";
		final String twoLines = "Text line 1\nText line 2\n";
		tester1.accept(threeLines, "true");
		tester1.accept(threeLines, Boolean.TRUE);
		tester1.accept(twoLines, "false");
		tester1.accept(twoLines, Boolean.FALSE);
		map1.clear();
		final Map<String,Object> map2 = new HashMap<>();
		map1.put("feature", map2);
		final BiConsumer<String,Object> tester2 = (s,o) -> {
			map2.put("foo", o);
			final StringWriter sw = new StringWriter();
			template.write(map1, sw);
			assertEquals(s, sw.toString());
		};
		tester2.accept(threeLines, "true");
		tester2.accept(threeLines, Boolean.TRUE);
		tester2.accept(twoLines, Boolean.FALSE);
		tester2.accept(twoLines, "false");		
	}
}
