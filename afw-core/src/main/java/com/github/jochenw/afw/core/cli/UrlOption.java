package com.github.jochenw.afw.core.cli;

import java.net.MalformedURLException;
import java.net.URL;

import org.jspecify.annotations.NonNull;

import com.github.jochenw.afw.core.cli.Cli.UsageException;
import com.github.jochenw.afw.core.util.Strings;


/** Implementation of {@link Option} for {@link URL} values.
 * @param <B> The options bean type.
 */
public class UrlOption<B> extends Option<B,URL> {
	/** Creates a new instance.
	 * @param pCli The {@link Cli}, that creates this option.
	 * @param pPrimaryName The options primary name. Always non-null.
	 * @param pSecondaryNames The options secondary names, if any.
	 */
	@SuppressWarnings("null")
	protected UrlOption(@NonNull Cli<B> pCli, @NonNull String pPrimaryName, @NonNull String[] pSecondaryNames) {
		super(pCli, URL.class, pPrimaryName, pSecondaryNames);
	}

	@Override
	public URL getValue(@NonNull String pOptValue) throws UsageException {
		try {
			return Strings.asUrl(pOptValue);
		} catch (MalformedURLException mue) {
			throw new UsageException("Invalid value for option " + getPrimaryName() + "; Expected valid URL, got " + pOptValue);
		}
	}
}
