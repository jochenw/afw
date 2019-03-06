package com.github.jochenw.afw.core.template;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.nio.charset.Charset;

import com.github.jochenw.afw.core.util.Exceptions;

public interface ITemplateEngine<M> {
	public interface Template<T> {
		public default void write(T pModel, OutputStream pOut, Charset pCharset) {
			try (Writer w = new OutputStreamWriter(pOut, pCharset)) {
				write(pModel, w);
			} catch (IOException e) {
				throw Exceptions.show(e);
			}
		}
		public void write(T pModel, Writer pWriter);
	}

	public default Template<M> compile(String pTemplateText) {
		return getTemplate(new StringReader(pTemplateText));
	}
	public Template<M> getTemplate(String pUri);
	public Template<M> getTemplate(Reader pReader);
}
