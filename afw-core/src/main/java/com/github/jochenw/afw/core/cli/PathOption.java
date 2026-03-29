package com.github.jochenw.afw.core.cli;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.jspecify.annotations.NonNull;

import com.github.jochenw.afw.core.cli.Cli.UsageException;

/** Implementation of {@link Option} for path values.
 * @param <B> The options bean type.
 */
public class PathOption<B> extends Option<B,Path> {
	private boolean dirRequired, existsRequired, fileRequired;

	/** Creates a new instance.
	 * @param pCli The {@link Cli}, that creates this option.
	 * @param pPrimaryName The options primary name. Always non-null.
	 * @param pSecondaryNames The options secondary names, if any.
	 */
	@SuppressWarnings("null")
	protected PathOption(@NonNull Cli<B> pCli, @NonNull String pPrimaryName, @NonNull String[] pSecondaryNames) {
		super(pCli, Path.class, pPrimaryName, pSecondaryNames);
	}

	@Override
	public Path getValue(@NonNull String pOptValue) throws UsageException {
		final Path p = Paths.get(pOptValue);
		if (isDirRequired()  &&  !Files.isDirectory(p)) {
			throw new UsageException("Invalid value for option " + getPrimaryName() + "; Expected existing directory, got " + p);
		}
		if (isExistsRequired()  &&  !Files.exists(p)) {
			throw new UsageException("Invalid value for option " + getPrimaryName() + "; Expected existing file, or directory, got " + p);
		}
		if (isFileRequired()  &&  !Files.isRegularFile(p)) {
			throw new UsageException("Invalid value for option " + getPrimaryName() + "; Expected existing file, got " + p);
		}
		return p;
	}

	/** Returns, whether a path value is only valid, if it refers
	 * to an existing directory.
	 * @return True, if a path value is only valid, if it refers
	 * to an existing directory.
	 * @see #dirRequired(boolean)
	 * @see #dirRequired()
	 * @see #isExistsRequired()
	 * @see #isFileRequired()
	 */
	public boolean isDirRequired() { return dirRequired; }

	/** Returns, whether a path value is only valid, if it refers
	 * to an existing file, or directory.
	 * @return True, if a path value is only valid, if it refers
	 * to an existing file, or directory.
	 * @see #existsRequired(boolean)
	 * @see #existsRequired()
	 * @see #isFileRequired()
	 * @see #isDirRequired()
	 */
	public boolean isExistsRequired() { return existsRequired; }

	/** Returns, whether a path value is only valid, if it refers
	 * to an existing file.
	 * @return True, if a path value is only valid, if it refers
	 * to an existing file.
	 * @see #fileRequired(boolean)
	 * @see #fileRequired()
	 * @see #isDirRequired()
	 * @see #isExistsRequired()
	 */
	public boolean isFileRequired() { return fileRequired; }

	/** Specifies, that a path value is only valid, if it refers
	 * to an existing directory. Equivalent to
	 * <pre>
	 *   dirRequired(true)
	 * </pre>
	 * @return This option.
	 * @see #dirRequired(boolean)
	 * @see #isDirRequired()
	 * @see #existsRequired()
	 * @see #fileRequired()
	 */
	public PathOption<B> dirRequired() { return dirRequired(true); }

	/** Sets, whether a path value is only valid, if it refers
	 * to an existing directory.
	 * @param pRequired True, if a path value is only valid, if
	 *   it refers to an existing directory.
	 * @return This option.
	 * @see #dirRequired()
	 * @see #isDirRequired()
	 * @see #existsRequired()
	 * @see #fileRequired()
	 */
	public PathOption<B> dirRequired(boolean pRequired) {
		assertMutable();
		dirRequired = pRequired;
		return this;
	}

	/** Specifies, that a path value is only valid, if it refers
	 * to an existing file, or directory. Equivalent to
	 * <pre>
	 *   existsRequired(true)
	 * </pre>
	 * @return This option.
	 * @see #existsRequired(boolean)
	 * @see #isExistsRequired()
	 * @see #dirRequired()
	 * @see #fileRequired()
	 */
	public PathOption<B> existsRequired() { return existsRequired(true); }

	/** Sets, whether a path value is only valid, if it refers
	 * to an existing file, or directory.
	 * @param pRequired True, if a path value is only valid, if
	 *   it refers to an existing file, or directory.
	 * @return This option.
	 * @see #existsRequired()
	 * @see #isExistsRequired()
	 * @see #existsRequired(boolean)
	 * @see #dirRequired(boolean)
	 */
	public PathOption<B> existsRequired(boolean pRequired) {
		assertMutable();
		existsRequired = pRequired;
		return this;
	}

	/** Specifies, that a path value is only valid, if it refers
	 * to an existing file. Equivalent to
	 * <pre>
	 *   fileRequired(true)
	 * </pre>
	 * @return This option.
	 * @see #fileRequired(boolean)
	 * @see #isFileRequired()
	 * @see #dirRequired()
	 * @see #existsRequired()
	 */
	public PathOption<B> fileRequired() { return fileRequired(true); }

	/** Sets, whether a path value is only valid, if it refers
	 * to an existing file.
	 * @param pRequired True, if a path value is only valid, if
	 *   it refers to an existing file.
	 * @return This option.
	 * @see #fileRequired()
	 * @see #isFileRequired()
	 * @see #existsRequired(boolean)
	 * @see #dirRequired(boolean)
	 */
	public PathOption<B> fileRequired(boolean pRequired) {
		assertMutable();
		fileRequired = pRequired;
		return this;
	}
}
