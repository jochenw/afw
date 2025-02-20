package com.github.jochenw.afw.core.cli;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import org.jspecify.annotations.NonNull;

import com.github.jochenw.afw.core.cli.Cli.UsageException;


/** Implementation of {@link Option} for {@ink LocalDate} values.
 * @param <B> The options bean type.
 */
public class DateTimeOption<B> extends Option<B,LocalDateTime> {
	private String formatPattern;

	/** Creates a new instance.
	 * @param pCli The {@link Cli}, that creates this option.
	 * @param pPrimaryName The options primary name. Always non-null.
	 * @param pSecondaryNames The options secondary names, if any.
	 */
	@SuppressWarnings("null")
	public DateTimeOption(@NonNull Cli<B> pCli, @NonNull String pPrimaryName,
			@NonNull String[] pSecondaryNames) {
		super(pCli, LocalDateTime.class, pPrimaryName, pSecondaryNames);
	}

	/** Sets the options format pattern, according to
	 *  {@link DateTimeFormatter#ofPattern(String)}.
	 *  @param pPattern The format pattern string. May be null, in which case
	 *    the default value {@link DateTimeFormatter#ISO_LOCAL_DATE} is being used.
	 *  @return This option.
	 */
	public DateTimeOption<B> format(String pPattern) {
		formatPattern = pPattern;
		return this;
	}

	@Override
	public LocalDateTime getValue(@NonNull String pOptValue) throws UsageException {
		final DateTimeFormatter dtf = formatPattern == null ?
				DateTimeFormatter.ISO_LOCAL_DATE_TIME : DateTimeFormatter.ofPattern(formatPattern);
		try {
			return LocalDateTime.from(dtf.parse(pOptValue));
		} catch (DateTimeParseException dtpe) {
			final String formatDescription = formatPattern == null ? "ISO_LOCAL_DATE" : formatPattern;
			throw new UsageException("Invalid argument for option " + getPrimaryName()
				+ ": Expected valid date value for pattern (" + formatDescription + "), got " + pOptValue);
		}
	}

}
