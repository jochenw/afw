package com.github.jochenw.afw.core.scripts;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.github.jochenw.afw.core.util.Objects;

import com.github.jochenw.afw.core.io.IReadable;
import com.github.jochenw.afw.core.util.Holder;

import groovy.lang.Binding;
import groovy.lang.GroovyCodeSource;
import groovy.lang.GroovyShell;

public class GroovyScriptEngine implements IScriptEngine {
	public static class GroovyScript implements Script {
		private final groovy.lang.Script gScript;

		public GroovyScript(groovy.lang.Script pScript) {
			gScript = pScript;
		}

		protected @Nonnull Map<String,Object> asParameters(Map<String,Object> pParameters) {
			if (pParameters == null) {
				return Collections.emptyMap();
			} else {
				return pParameters;
			}
		}
		@Override
		public <O> O call(Map<String, Object> pParameters) {
			final @Nonnull Map<String,Object> parameters = asParameters(pParameters);
			gScript.setBinding(new Binding(parameters));
			@SuppressWarnings("unchecked")
			final O o = (O) gScript.run();
			return o;
		}

		@Override
		public void run(@Nonnull Map<String, Object> pParameters) {
			final @Nonnull Map<String,Object> parameters = asParameters(pParameters);
			gScript.setBinding(new Binding(parameters));
		}
		
	}
	private final GroovyShell gsh = new GroovyShell();
	@Override
	public boolean isScriptable(@Nonnull IReadable pReadable) {
		return pReadable.isReadable()  &&  pReadable.getName().endsWith(".groovy");
	}

	@Override
	public @Nonnull Script getScript(@Nonnull IReadable pReadable, @Nullable Charset pCharset) {
		final IReadable ir = Objects.requireNonNull(pReadable, "IReadable");
		final Charset charSet = Objects.notNull(pCharset, StandardCharsets.UTF_8);
		final Holder<Script> holder = new Holder<Script>();
		ir.read((r) -> {
			holder.set(new GroovyScript(gsh.parse(r, pReadable.getName())));
		}, charSet);
		return Objects.requireNonNull(holder.get());
	}
}
