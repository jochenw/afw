package com.github.jochenw.afw.core.template;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.function.Consumer;

import com.github.jochenw.afw.core.template.ITemplateEngine.Template;

public class SimpleTemplate<M> implements Template<M> {
	public static class Context<T> {
		private T model;
		private PrintWriter writer;

		public T getModel() {
			return model;
		}

		public void setModel(T pModel) {
			model = pModel;
		}

		public PrintWriter getWriter() {
			return writer;
		}

		public void setWriter(PrintWriter pWriter) {
			writer = pWriter;
		}

		public void write(String pValue) {
			writer.write(pValue);
		}

		public void writeln(String pValue) {
			writer.write(pValue);
			writeln();
		}

		public void writeln() {
			writer.write("\n");
		}
}

	private final Iterable<Consumer<Context<M>>> lines;

	public SimpleTemplate(Iterable<Consumer<Context<M>>> pLines) {
		lines = pLines;
	}
	
	@Override
	public void write(M pModel, Writer pWriter) {
		final Context<M> ctx = new Context<M>();
		ctx.setModel(pModel);
		final PrintWriter pw = new PrintWriter(pWriter);
		ctx.setWriter(pw);
		lines.forEach((l) -> l.accept(ctx));
		pw.flush();
	}
}
