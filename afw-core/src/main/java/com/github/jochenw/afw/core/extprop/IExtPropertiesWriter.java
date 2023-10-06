/*
 * Copyright 2023 Jochen Wiedmann
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.jochenw.afw.core.extprop;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

/** Interface of an object, which can serialize an extended
 * property set to an {@link OutputStream}, or {@link Writer}.
 * The default implementation is {@link DefaultExtPropertiesWriter}.
 * Use {@link ExtProperties#writer()}, or
 * {@link ExtProperties#writer(java.nio.charset.Charset, String)}
 * to obtain such an object.
 */
public interface IExtPropertiesWriter {
	/** Writes the given property set to the given
	 * {@link BufferedWriter}.
	 * @param pProperties The property set, which is being
	 * serialized.
	 * @param pWriter The output character stream.
	 * @throws UncheckedIOException An I/O error occurred,
	 *   while writing to the output character stream.
	 * @throws NullPointerException Either of the parameters
	 *   is null.
	 */
	void write(ExtProperties pProperties, BufferedWriter pWriter)
		throws UncheckedIOException, NullPointerException;
	/** Writes the given property set to the given
	 * {@link BufferedOutputStream}.
	 * @param pProperties The property set, which is being
	 * serialized.
	 * @param pStream The output character stream.
	 * @throws UncheckedIOException An I/O error occurred,
	 *   while writing to the output character stream.
	 * @throws NullPointerException Either of the parameters
	 *   is null.
	 */
	void write(ExtProperties pProperties, BufferedOutputStream pStream)
	    throws UncheckedIOException, NullPointerException;
	/** Writes the given property set to the given
	 * {@link Writer}.
	 * @param pProperties The property set, which is being
	 * serialized.
	 * @param pWriter The output character stream.
	 * @throws UncheckedIOException An I/O error occurred,
	 *   while writing to the output character stream.
	 * @throws NullPointerException Either of the parameters
	 *   is null.
	 */
	default void write(ExtProperties pProperties, Writer pWriter) {
		if (pWriter instanceof BufferedWriter) {
			write(pProperties, (BufferedWriter) pWriter);
		} else {
			final BufferedWriter bw = new BufferedWriter(pWriter);
			write(pProperties, bw);
			try {
				bw.flush();
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		}
	}
	/** Writes the given property set to the given
	 * {@link OutputStream}.
	 * @param pProperties The property set, which is being
	 * serialized.
	 * @param pStream The output byte stream.
	 * @throws UncheckedIOException An I/O error occurred,
	 *   while writing to the output character stream.
	 * @throws NullPointerException Either of the parameters
	 *   is null.
	 */
	default void write(ExtProperties pProperties, OutputStream pStream)
	        throws UncheckedIOException, NullPointerException {
		final OutputStream stream = Objects.requireNonNull(pStream, "OutputStream");
		if (stream instanceof BufferedOutputStream) {
			write(pProperties, (BufferedOutputStream) stream);
		} else {
			write(pProperties, new BufferedOutputStream(stream));
		}
	}

	/** Returns the default character set, for conversion of
	 * characters into bytes.
	 * @return The default character set, for conversion of
	 * characters into bytes
	 */
	Charset getCharset();

	/** Returns the default line separator.
	 * @return The default line separator.
	 */
	String getLineSeparator();

	/** Writes the given property set to the given {@link Path file}.
	 * @param pProperties The property set, which is being
	 * serialized.
	 * @param pPath The output file.
	 * @throws UncheckedIOException An I/O error occurred,
	 *   while writing to the output character stream.
	 * @throws NullPointerException Either of the parameters
	 *   is null.
	 */
	default void write(ExtProperties pProperties, Path pPath) {
		final Path path = Objects.requireNonNull(pPath, "Path");
		try (BufferedWriter bw = Files.newBufferedWriter(path, getCharset())) {
			write(pProperties, bw);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	/** Writes the given property set to the given {@link File file}.
	 * @param pProperties The property set, which is being
	 * serialized.
	 * @param pFile The output file.
	 * @throws UncheckedIOException An I/O error occurred,
	 *   while writing to the output character stream.
	 * @throws NullPointerException Either of the parameters
	 *   is null.
	 */
	default void write(ExtProperties pProperties, File pFile) {
		final File file = Objects.requireNonNull(pFile, "File");
		try (OutputStream os = new FileOutputStream(file);
			 BufferedOutputStream bos = new BufferedOutputStream(os);
			 OutputStreamWriter osw = new OutputStreamWriter(bos, getCharset());
			 BufferedWriter bw = new BufferedWriter(osw)) {
			write(pProperties, bw);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}
}
