package com.github.jochenw.afw.core.template;

import static org.junit.Assert.*;

import java.io.StringWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.github.jochenw.afw.core.template.ITemplateEngine.Template;

public class SimpleTemplateEngineTest {
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
	
}
