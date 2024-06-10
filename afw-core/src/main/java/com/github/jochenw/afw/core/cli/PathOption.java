package com.github.jochenw.afw.core.cli;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Consumer;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import com.github.jochenw.afw.core.util.Objects;

/** Implementation of {@link Option} for path values.
 * @param <O> Type of the option bean.
 */
public class PathOption<O> extends Option<Path,O> {
	private boolean existsRequired, fileRequired, dirRequired;

	/** Creates a new instance with the given {@link Cli},
	 * and {@code end handler}.
	 * @param pCli The {@link Cli}, which is creating this option.
	 * @param pEndHandler The {@code end handler}, which is being
	 *   invoked upon invocation of {@link Option#end()}.
	 * @param pPrimaryName The options primary name.
	 * @param pSecondaryNames The options secondary names.
	 */
	protected PathOption(@NonNull Cli<O> pCli, @NonNull Consumer<Option<?,O>> pEndHandler,
                         @NonNull String pPrimaryName,
                         @NonNull String @Nullable [] pSecondaryNames) {
		super(pCli, pEndHandler, pPrimaryName, pSecondaryNames);
	}

	@Override
	public @NonNull Path getValue(@NonNull String pStrValue) {
		return Objects.requireNonNull(Paths.get(pStrValue));
	}

	/** Returns, whether the option value must specify an
	 * existing file, or directory.
	 * @return True, if the option value must specify an
	 * existing file, or directory.
	 */
	public boolean isExistsRequired() {
		return existsRequired;
	}

	/** Returns, whether the option value must specify an
	 * existing file.
	 * @return True, if the option value must specify an
	 * existing file.
	 */
	public boolean isFileRequired() {
		return fileRequired;
	}

	/** Returns, whether the option value must specify an
	 * existing directory.
	 * @return True, if the option value must specify an
	 * existing file.
	 */
	public boolean isDirRequired() {
		return dirRequired;
	}

	/** Called to specify, that the option value must specify an
	 * existing file, or directory.
	 * @return This option.
	 */
	public PathOption<O> existsRequired() {
		existsRequired = true;
		return this;
	}

	/** Called to specify, that the option value must specify an
	 * existing file.
	 * @return This option.
	 */
	public PathOption<O> fileRequired() {
		fileRequired = true;
		return this;
	}

	/** Called to specify, that the option value must specify an
	 * existing directory.
	 * @return This option.
	 */
	public PathOption<O> dirRequired() {
		dirRequired = true;
		return this;
	}
}