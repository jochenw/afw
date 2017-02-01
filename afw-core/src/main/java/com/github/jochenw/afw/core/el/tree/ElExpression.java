package com.github.jochenw.afw.core.el.tree;

import java.io.Serializable;

public class ElExpression implements Serializable {
	private static final long serialVersionUID = 8384812496202649423L;
	private final OrExpression orExpression;
	private final int numPlaceholders;

	public ElExpression(OrExpression pOrExpression) {
		this(pOrExpression, 0);
	}

	public ElExpression(OrExpression pOrExpression, int pNumPlaceholders) {
		orExpression = pOrExpression;
		numPlaceholders = pNumPlaceholders;
	}

	public OrExpression getOrExpression() {
		return orExpression;
	}

	public int getNumberOfPlaceholders() {
		return numPlaceholders;
	}
}
