package com.github.jochenw.afw.core.exec;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.github.jochenw.afw.core.function.Functions.FailableConsumer;
import com.github.jochenw.afw.core.function.Functions.FailableSupplier;
import com.github.jochenw.afw.core.util.FileUtils;
import com.github.jochenw.afw.core.util.Objects;
import com.github.jochenw.afw.core.util.Streams;

/**
 * Interface of an object, that handles the external processes output (stdout, or stderr).
 */
@FunctionalInterface
public interface ProcessOutputHandler extends FailableConsumer<InputStream,Throwable> {
	/** Creates an {@link ProcessOutputHandler}, that forwards the external processes output
	 * to the given {@link OutputStream}.
	 * @param pOut The {@link OutputStream target stream}, to which data shall be written.
	 * @return The created {@link ProcessOutputHandler}.
	 * @throws NullPointerException The parameter {@code pOut} is null.
	 */
	public static ProcessOutputHandler of(@Nonnull OutputStream pOut) {
		final @Nonnull OutputStream out = Objects.requireNonNull(pOut, "OutputStream");
		return (in) -> Streams.copy(in, out);
	}
	/** Creates an {@link ProcessOutputHandler}, that forwards the external processes output
	 * to the given {@link Writer}.
	 * @param pWriter The {@link Writer target stream}, to which data shall be written.
	 * @param pCharset The character set, which should be used for conversion of
	 *   bytes into characters. May be null, in which case {@link StandardCharsets#UTF_8}
	 *   will be used as the default.
	 * @return The created {@link ProcessOutputHandler}.
	 * @throws NullPointerException The parameter {@code pWriter} is null.
	 */
	public static ProcessOutputHandler of(@Nonnull Writer pWriter, @Nullable Charset pCharset) {
		final @Nonnull Writer w = Objects.requireNonNull(pWriter, "Writer");
		final @Nonnull Charset cs = Objects.notNull(pCharset, null);
		return (in) -> Streams.copy(in, w, cs);
	}
	/** Creates an {@link ProcessOutputHandler}, that forwards the external processes output
	 * to an {@link OutputStream}, that is returned by the given {@link FailableSupplier
	 * supplier}.
	 * @param pSupplier The {@link FailableSupplier supplier}, that returns an
	 * {@link OutputStream}, to which data shall be written.
	 * @return The created {@link ProcessOutputHandler}.
	 * @throws NullPointerException The parameter {@code pSupplier} is null.
	 */
	public static ProcessOutputHandler of(@Nonnull FailableSupplier<OutputStream,?> pSupplier) {
		final @Nonnull FailableSupplier<OutputStream,?> supplier = Objects.requireNonNull(pSupplier, "Supplier");
		return (in) -> {
			final @Nonnull OutputStream out = supplier.get();
			Streams.copy(in, out);
		};
	}
	/** Creates an {@link ProcessOutputHandler}, that forwards the external processes output
	 * to a {@link Writer}, that is returned by the given {@link FailableSupplier
	 * supplier}.
	 * @param pSupplier The {@link FailableSupplier supplier}, that returns a
	 * {@link Writer}, to which data shall be written.
	 * @param pCharset The character set, which should be used for conversion of
	 *   bytes into characters. May be null, in which case {@link StandardCharsets#UTF_8}
	 *   will be used as the default.
	 * @return The created {@link ProcessOutputHandler}.
	 * @throws NullPointerException The parameter {@code pSupplier} is null.
	 */
	public static ProcessOutputHandler of(@Nonnull FailableSupplier<Writer,?> pSupplier,
			                       @Nullable Charset pCharset) {
		final @Nonnull FailableSupplier<Writer,?> supplier = Objects.requireNonNull(pSupplier, "Supplier");
		final @Nonnull Charset cs = Objects.notNull(pCharset, null);
		return (in) -> {
			final @Nonnull Writer w = supplier.get();
			Streams.copy(in, w, cs);
		};
	}
	/** Creates an {@link ProcessOutputHandler}, that forwards the external processes output
	 * to the given {@link Path file}.
	 * @param pPath The {@link Path output file}, to which data shall be written.
	 * @return The created {@link ProcessOutputHandler}.
	 * @throws NullPointerException The parameter {@code pPath} is null.
	 */
	public static ProcessOutputHandler of(@Nonnull Path pPath) {
		final @Nonnull Path path = Objects.requireNonNull(pPath, "Path");
		FileUtils.createDirectoryFor(path);
		return of(() -> new BufferedOutputStream(Files.newOutputStream(path)));
	}
	/** Creates an {@link ProcessOutputHandler}, that writes the external processes output
	 * to the given {@link Path file}, converting the byte stream to a character
	 * stream.
	 * @param pPath The {@link Path output file}, to which data shall be written.
	 * @param pCharset The character set, which should be used for conversion of
	 *   bytes into characters. May be null, in which case {@link StandardCharsets#UTF_8}
	 *   will be used as the default.
	 * @return The created {@link ProcessOutputHandler}.
	 * @throws NullPointerException The parameter {@code pPath} is null.
	 */
	public static ProcessOutputHandler of(@Nonnull Path pPath, @Nullable Charset pCharset) {
		final @Nonnull Path path = Objects.requireNonNull(pPath, "Path");
		FileUtils.createDirectoryFor(path);
		return of(() -> Files.newBufferedWriter(path), pCharset);
	}
	/** Creates an {@link ProcessOutputHandler}, that writes the external processes output
	 * to the given {@link File file}, converting the byte stream to a character
	 * stream.
	 * @param pFile The {@link File output file}, to which data shall be written.
	 * @param pCharset The character set, which should be used for conversion of
	 *   bytes into characters. May be null, in which case {@link StandardCharsets#UTF_8}
	 *   will be used as the default.
	 * @return The created {@link ProcessOutputHandler}.
	 * @throws NullPointerException The parameter {@code pPath} is null.
	 */
	public static ProcessOutputHandler of(@Nonnull File pFile, @Nullable Charset pCharset) {
		final @Nonnull Path path = Objects.requireNonNull(pFile, "File").toPath();
		return of(path, pCharset);
	}
}
