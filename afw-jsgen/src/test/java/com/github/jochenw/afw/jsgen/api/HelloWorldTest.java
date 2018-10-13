package com.github.jochenw.afw.jsgen.api;

import static com.github.jochenw.afw.jsgen.api.JSGSource.q;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import org.junit.Assert;
import org.junit.Test;

import com.github.jochenw.afw.jsgen.api.JSGFactory.NamedResource;
import com.github.jochenw.afw.jsgen.impl.AbstractSourceWriter;


public class HelloWorldTest {
	private static final String HELLO_WORLD_JAVA = "package com.foo.myapp;\n" + 
			"\n" + 
			"import java.lang.String;\n" + 
			"import java.lang.System;\n" + 
			"\n" + 
			"public class Main {\n" + 
			"    public static void main(String pArgs)  {\n" + 
			"        System.out.println(\"Hello, world!\");\n" + 
			"    }\n" + 
			"}\n";

	/**
	 * Tests creating the following class:
	 * <pre>
	 *   package com.foo.myapp;
	 *
	 *   public class Main {
	 *       public static void main(String[] pArgs) throws Exception {
	 *           System.out.println("Hello, world!");
	 *       }
	 *   }
	 * </pre>
	 */
	@Test
	public void testHelloWorldJava() throws Exception {
		final JSGFactory factory = new JSGFactory();
		final JSGSourceBuilder jsb = factory.newSource("com.foo.myapp.Main").makePublic();
		final JSGMethodBuilder mainMethod = jsb.newMethod("main").makePublic().makeStatic();
		mainMethod.parameter(JSGQName.STRING_ARRAY, "pArgs");
		mainMethod.body().addLine(System.class, ".out.println(", q("Hello, world!"), ");");
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		final JSGSourceWriter jsgw = new AbstractSourceWriter() {
			@Override
			protected OutputStream open(NamedResource pResource) throws IOException {
				return baos;
			}
		};
		jsgw.write(factory);
		final String got = baos.toString(StandardCharsets.UTF_8.name());
		Assert.assertEquals(HELLO_WORLD_JAVA, got);
	}

}
