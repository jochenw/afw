package com.github.jochenw.afw.core.el;

import java.io.StringReader;

import com.github.jochenw.afw.core.el.jcc.ELParser;
import com.github.jochenw.afw.core.el.tree.ElExpression;
import com.github.jochenw.afw.core.util.Exceptions;


public class ElReader {
	public ElExpression parse(String pExpression) {
		final ELParser parser = new ELParser(new StringReader(pExpression));
		try {
			return (ElExpression) parser.ElExpression();
		} catch (Throwable t) {
			throw Exceptions.show(t);
		}
	}
}
