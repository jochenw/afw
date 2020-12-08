package com.github.jochenw.afw.core.scripts;

import static org.junit.Assert.*;

import java.net.URL;
import java.nio.charset.StandardCharsets;

import org.junit.Test;

import com.github.jochenw.afw.core.scripts.IScriptEngine.Script;
import com.github.jochenw.afw.core.util.Scripts;

public class IScriptEngineTest {
	@Test
	public void test() {
		final URL url = getClass().getResource("simple-script.groovy");
		assertNotNull(url);
		final Script script = Scripts.compile(url, StandardCharsets.UTF_8);
		script.run(null);
		final String got = (String) script.call(null);
		assertEquals("okay", got);
	}
}
