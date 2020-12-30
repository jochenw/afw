package com.github.jochenw.afw.core.io;

import static org.junit.Assert.*;

import java.io.StringReader;

import org.junit.Test;

import com.github.jochenw.afw.core.props.Interpolator;
import com.github.jochenw.afw.core.util.DefaultInterpolator;
import com.github.jochenw.afw.core.util.Streams;

public class InterpolatingReaderTest {
	@Test
	public void test() {
		final String input = "abc${foo}123${bar}xyz${baz}";
		final Interpolator interpolator = new DefaultInterpolator((s) -> {
			switch (s) {
			case "foo": return "Foo";
			case "bar": return "a bar";
			case "baz": return "zab";
			default: throw new IllegalStateException();
			}
		});
		final StringReader sr = new StringReader(input);
		final String got = Streams.read(new InterpolatingReader(interpolator, sr));
		assertEquals("abcFoo123a barxyzzab", got);
	}

}
