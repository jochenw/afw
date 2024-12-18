package com.github.jochenw.afw.core.scripts;

import java.nio.charset.Charset;
import java.util.Collections;
import java.util.Map;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import org.codehaus.groovy.ast.ASTNode;

import com.github.jochenw.afw.core.util.Objects;
import com.github.jochenw.afw.core.util.Streams;
import com.github.jochenw.afw.core.io.IReadable;
import com.github.jochenw.afw.core.util.Holder;

import groovy.lang.Binding;
import groovy.lang.GroovyRuntimeException;
import groovy.lang.GroovyShell;


/** Implementation of {@link IScriptEngine} for Groovy
 * scripts.
 */
public class GroovyScriptEngine implements IScriptEngine {
	/** Creates a new instance.
	 */
	public GroovyScriptEngine() {}

	/** Implementation of {@link IScriptEngine.Script} for Groovy
	 * scripts.
	 */
	public static class GroovyScript implements Script {
		private final groovy.lang.Script gScript;

		/**
		 * Creates a new instance, which can be used
		 * to evaluate the given script.
		 * @param pScript The Groovy script, that can
		 *   be evaluated.
		 */
		public GroovyScript(groovy.lang.Script pScript) {
			gScript = pScript;
		}

		/** Converts the given map of parameters into a parameter map.
		 * (Nothing much to do here, mostly null handling.
		 * @param pParameters The map of parameters.
		 * @return The created parameter map.
		 */
		protected @NonNull Map<String,Object> asParameters(@Nullable Map<String,Object> pParameters) {
			if (pParameters == null) {
				return Objects.requireNonNull(Collections.emptyMap());
			} else {
				return pParameters;
			}
		}
		@Override
		public <O> O call(@Nullable Map<String, Object> pParameters) {
			final @NonNull Map<String,Object> parameters = asParameters(pParameters);
			gScript.setBinding(new Binding(parameters));
			@SuppressWarnings("unchecked")
			final O o = (O) run();
			return o;
		}

		/** Called for actual execution of the Groovy acript.
		 * @return The script evaluations result.
		 */
		protected Object run() {
			try {
				return gScript.run();
			} catch (GroovyRuntimeException gre) {
				final ASTNode node = gre.getNode();
				if (node == null) {
					throw gre;
				} else {
					@SuppressWarnings("unused")
					final String text = node.getText();
					@SuppressWarnings("unused")
					final int lineNumber = node.getLineNumber();
					@SuppressWarnings("unused")
					final int columnNumber = node.getColumnNumber();
					throw gre;
				}
			}
		}

		@Override
		public void run(@Nullable Map<String, Object> pParameters) {
			final @NonNull Map<String,Object> parameters = asParameters(pParameters);
			gScript.setBinding(new Binding(parameters));
			run();
		}
		
	}
	private final GroovyShell gsh = new GroovyShell();
	@Override
	public boolean isScriptable(@NonNull IReadable pReadable) {
		String name = pReadable.getName();
		return pReadable.isReadable()  &&  (name.endsWith(".groovy") || name.endsWith(".grv"));
	}

	@Override
	public @NonNull Script getScript(@NonNull IReadable pReadable, @Nullable Charset pCharset) {
		final IReadable ir = Objects.requireNonNull(pReadable, "IReadable");
		final Charset charSet = Objects.notNull(pCharset, Streams.UTF_8);
		final Holder<Script> holder = new Holder<Script>();
		ir.read((r) -> {
			holder.set(new GroovyScript(gsh.parse(r, pReadable.getName())));
		}, charSet);
		return Objects.requireNonNull(holder.get());
	}
}
