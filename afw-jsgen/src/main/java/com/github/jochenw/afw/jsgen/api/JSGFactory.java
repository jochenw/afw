package com.github.jochenw.afw.jsgen.api;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;


public class JSGFactory {
	public interface NamedResource {
		boolean isJavaSource();
		boolean isResourceFile();
		JSGLocation getName();
		void writeTo(OutputStream pOut) throws IOException;
	}
	private Map<JSGLocation,Object> resources = new HashMap<>();

	/**
	 * Returns the previously created {@link JSGSourceBuilder source builder} with the
	 * given name.
	 * @param pName Name of the {@link JSGSourceBuilder source builder}, which is being
	 * queried.
	 * @return Previously created {@link JSGSourceBuilder source builder}, if available,
	 *   or null.
	 * @throws NullPointerException The name is null.
	 * @throws IllegalStateException An object has been registered with the given name,
	 *   but the object is not a {@link JSGSourceBuilder source builder}.
	 */
	@Nullable JSGSourceBuilder getSource(@Nonnull JSGQName pName) {
		final Object source = resources.get(pName);
		if (source == null) {
			return null;
		} else if (source instanceof JSGSourceBuilder) {
			return (JSGSourceBuilder) source;
		} else {
			throw new IllegalStateException("Expected JSGSourceBuilder, got " + source.getClass().getName());
		}
	}

	/**
	 * Returns the previously created {@link JSGSourceBuilder source builder} with the
	 * given type.
	 * @param pType Type of the {@link JSGSourceBuilder source builder}, which is being
	 * queried.
	 * @return Previously created {@link JSGSourceBuilder source builder}, if available,
	 *   or null.
	 * @throws NullPointerException The name is null.
	 * @throws IllegalStateException An object has been registered with the given name,
	 *   but the object is not a {@link JSGSourceBuilder source builder}.
	 */
	@Nullable JSGSourceBuilder getSource(@Nonnull Class<?> pType) {
		return getSource(JSGQName.valueOf(pType));
	}

	/**
	 * Returns the previously created {@link JSGSourceBuilder source builder} with the
	 * given type.
	 * @param pType Type of the {@link JSGSourceBuilder source builder}, which is being
	 * queried.
	 * @return Previously created {@link JSGSourceBuilder source builder}, if available,
	 *   or null.
	 * @throws NullPointerException The name is null.
	 * @throws IllegalStateException An object has been registered with the given name,
	 *   but the object is not a {@link JSGSourceBuilder source builder}.
	 */
	@Nullable JSGSourceBuilder getSource(@Nonnull String pType) {
		return getSource(JSGQName.valueOf(pType));
	}

	/**
	 * Creates a new {@link JSGSourceBuilder source builder} with the given name.
	 * @param pName Name of the {@link JSGSourceBuilder source builder}, which is being
	 *   created.
	 * @return The new {@link JSGSourceBuilder source builder}.
	 * @throws NullPointerException The name is null.
	 * @throws IllegalStateException An object has already been registered with the given name.
	 */
	@Nonnull JSGSourceBuilder newSource(@Nonnull JSGQName pName) {
		final Object source = resources.get(pName);
		if (source == null) {
			final JSGSourceBuilder sb = new JSGSourceBuilder(pName);
			resources.put(pName, sb);
			return sb;
		} else {
			throw new IllegalStateException("Source already exists: " + pName);
		}
	}

	/**
	 * Creates a new {@link JSGSourceBuilder source builder} with the given name.
	 * @param pName Name of the {@link JSGSourceBuilder source builder}, which is being
	 *   created.
	 * @return The new {@link JSGSourceBuilder source builder}.
	 * @throws NullPointerException The name is null.
	 * @throws IllegalStateException An object has already been registered with the given name.
	 */
	@Nonnull JSGSourceBuilder newSource(@Nonnull Class<?> pType) {
		return newSource(JSGQName.valueOf(pType));
	}

	/**
	 * Creates a new {@link JSGSourceBuilder source builder} with the given name.
	 * @param pName Name of the {@link JSGSourceBuilder source builder}, which is being
	 *   created.
	 * @return The new {@link JSGSourceBuilder source builder}.
	 * @throws NullPointerException The name is null.
	 * @throws IllegalStateException An object has already been registered with the given name.
	 */
	@Nonnull JSGSourceBuilder newSource(@Nonnull String pType) {
		return newSource(JSGQName.valueOf(pType));
	}

	public void forEach(BiConsumer<JSGLocation,Object> pConsumer) {
		resources.forEach(pConsumer);
	}
}
