package com.github.jochenw.afw.core.extprop;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.Objects;

/** Default implementation of {@link IExtPropertiesWriter}.
 * Use {@link ExtProperties#writer()}, or
 * {@link ExtProperties#writer(java.nio.charset.Charset, String)}
 * to obtain such an object.
 */
public class DefaultExtPropertiesWriter implements IExtPropertiesWriter {
	private final Charset charset;
	private final String lineSeparator;

	/** Creates a new instance with the given {@link Charset default
	 * character set}, and the given default line separator.
	 * @param pCharset The default character set.
	 * @param pLineSeparator The default line separator.
	 * @throws NullPointerException Either of the parameters is null.
	 */
	public DefaultExtPropertiesWriter(Charset pCharset, String pLineSeparator)
	        throws NullPointerException {
		charset = Objects.requireNonNull(pCharset, "Charset");
		lineSeparator = Objects.requireNonNull(pLineSeparator, "Line separator");
	}

	@Override
	public void write(ExtProperties pProperties, BufferedWriter pWriter)
			throws UncheckedIOException, NullPointerException {
		pProperties.forEach((ep) -> {
			final String key = ep.getKey();
			final String value = ep.getValue();
			final String[] comments = ep.getComments();
			try {
				if (comments != null  &&  comments.length > 0) {
					for (String comment : comments) {
						writeComment(pWriter, comment);
					}
				}
				writeProperty(pWriter, key, value);
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		});
	}

	/** Writes a single comment string.
	 * @param pWriter The output {@link Writer}.
	 * @param pComment The comment string.
	 * @throws IOException Writing to the output {@link Writer}
	 *   has failed.
	 */
	protected void writeComment(BufferedWriter pWriter, String pComment)
	        throws IOException {
		try (StringReader sr = new StringReader(pComment);
			 BufferedReader br = new BufferedReader(sr)) {
			for (;;) {
				final String line = br.readLine();
				if (line == null) {
					break;
				} else {
					pWriter.write("# ");
					for (int i = 0;  i < line.length();  i++) {
						final char c = line.charAt(i);
						String invalidCharacterDescription = null;
						switch(c) {
						case '\r':
							invalidCharacterDescription = "Carriage Return";
							break;
						case '\n':
							invalidCharacterDescription = "Line Feed";
							break;
						default:
							pWriter.write(c);
							break;
						}
						if (invalidCharacterDescription != null) {
							throw new IllegalStateException("Invalid character in comment: " + invalidCharacterDescription);
						}
					}
					pWriter.write(getLineSeparator());
				}
			}
		}
	}

	/** Writes a single property definition, excluding the comment.
	 * @param pWriter The output {@link Writer}.
	 * @param pKey The property key.
	 * @param pValue The property value.
	 * @throws IOException Writing to the output {@link Writer}
	 *   has failed.
	 */
	protected void writeProperty(BufferedWriter pWriter, String pKey, String pValue)
	        throws IOException {
		final String key = Objects.requireNonNull(pKey, "Key");
		final String value = Objects.requireNonNull(pValue, "Value");
		for (int i = 0;  i < key.length();  i++) {
			final char c = key.charAt(i);
			String invalidCharacterDescription = null;
			switch(c) {
			case '\r':
				invalidCharacterDescription = "Carriage Return";
				break;
			case '\n':
				invalidCharacterDescription = "Line Feed";
				break;
			default:
				pWriter.write(c);
				break;
			}
			if (invalidCharacterDescription != null) {
				throw new IllegalStateException("Invalid character in property key: " + invalidCharacterDescription);
			}
		}
		pWriter.write('=');
		try (StringReader sr = new StringReader(value);
				BufferedReader br = new BufferedReader(sr)) {
			String line = br.readLine();
			while (line != null) {
				for (int i = 0;  i < value.length();  i++)  {
					final char c = value.charAt(i);
					switch (c) {
					case '\r':
						if (i+1 < value.length()) {
							final char c2 = value.charAt(i+1);
							if (c2 == '\n') {
								pWriter.write(getLineSeparator());
								i++;
							} else {
								pWriter.write ("\\r");
							}
						} else {
							pWriter.write(getLineSeparator());
						}
						break;
					case '\n':
						pWriter.write(getLineSeparator());
						break;
					case '\t':
						pWriter.write("\\t");
						break;
					case '\f':
						pWriter.write("\\f");
						break;
					case '\\':
						pWriter.write("\\\\");
					default:
						if (c < ' ') {

						} else {
							pWriter.write(c);
						}
						break;
					}
				}
				pWriter.write(getLineSeparator());
				line = br.readLine();
			}
		}
	}

	@Override
	public void write(ExtProperties pProperties, BufferedOutputStream pStream)
			throws UncheckedIOException, NullPointerException {
		final OutputStream stream = Objects.requireNonNull(pStream, "OutputStream");
		write(pProperties, new OutputStreamWriter(stream, getCharset()));
	}

	@Override
	public Charset getCharset() {
		return charset;
	}

	@Override
	public String getLineSeparator() {
		return lineSeparator;
	}
}
