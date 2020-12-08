package com.github.jochenw.afw.core.scripts;

import java.nio.charset.Charset;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.github.jochenw.afw.core.io.IReadable;


public interface IScriptEngine {
	public interface Script {
		public <O> @Nullable O call(@Nullable Map<String,Object> pParameters);
		public void run(@Nullable Map<String,Object> pParameters);
	}
	public boolean isScriptable(@Nonnull IReadable pReadable);
	public Script getScript(@Nonnull IReadable pReadable, @Nullable Charset pCharset);
}
