package com.github.jochenw.afw.jsgen.api;

import java.io.IOException;
import java.io.UncheckedIOException;

import javax.annotation.Nonnull;

import com.github.jochenw.afw.jsgen.api.JSGFactory.NamedResource;


public interface JSGSourceWriter {
	public default void write(@Nonnull JSGFactory pFactory) {
		pFactory.forEach((l,o) -> {
			try {
				if (o instanceof JSGSource) {
					write((JSGSource) o);
				} else if (o instanceof NamedResource) {
					write((NamedResource) o);
				} else {
					throw new IllegalStateException("Invalid object type: " + o.getClass().getName());
				}
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		});
	}
	void write(@Nonnull JSGSource pSource) throws IOException;
	void write(@Nonnull NamedResource pResource) throws IOException;
}
