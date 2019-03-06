package com.github.jochenw.afw.core.template;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.function.Consumer;

import com.github.jochenw.afw.core.el.ElEvaluator;
import com.github.jochenw.afw.core.el.ElReader;
import com.github.jochenw.afw.core.el.PropertyResolver;
import com.github.jochenw.afw.core.el.tree.ElExpression;
import com.github.jochenw.afw.core.template.SimpleTemplate.Context;
import com.github.jochenw.afw.core.util.Generics;

public class SimpleTemplateCompiler<M extends Map<String,Object>> {
	public abstract static class Block<T> {
		private final List<Consumer<Context<T>>> list = new ArrayList<>();
		private final int lineNumber;
		protected Block(int pLineNumber) {
			lineNumber = pLineNumber;
		}
		public void add(Consumer<Context<T>> pConsumer) {
			list.add(pConsumer);
		}
		public List<Consumer<Context<T>>> getList() {
			return list;
		}
		public int getLineNumber() {
			return lineNumber;
		}
	}
	public static class OuterBlock<T> extends Block<T> {
		public OuterBlock() {
			super(1);
		}
	}
	public static class IfBlock<T> extends Block<T> {
		private final ElExpression expr;
		private final List<Consumer<Context<T>>> elseList = new ArrayList<>();
		private boolean haveElse;

		public IfBlock(int pLineNumber, ElExpression pExpr) {
			super(pLineNumber);
			expr = pExpr;
		}

		@Override
		public void add(Consumer<Context<T>> pConsumer) {
			if (haveElse) {
				elseList.add(pConsumer);
			} else {
				super.add(pConsumer);
			}
		}
		public void startElse() {
			haveElse = true;
		}
		public boolean hasElse() {
			return haveElse;
		}
		public ElExpression getExpression() {
			return expr;
		}
		public List<Consumer<Context<T>>> getList(boolean pValue) {
			return pValue ? getList() : elseList;
		}
	}
	public static class ForBlock<T> extends Block<T> {
		private final String listVar, loopVar;
		public ForBlock(int pLineNumber, String pLoopVar, String pListVar) {
			super(pLineNumber);
			listVar = pListVar;
			loopVar = pLoopVar;
		}
		public String getListVar() { return listVar; }
		public String getLoopVar() { return loopVar; }
	}
	private final PropertyResolver propertyResolver;
	private final ElReader elReader;
	private final ElEvaluator elEvalutor;
	private final String uri;

	public SimpleTemplateCompiler(PropertyResolver pResolver, ElReader pReader, ElEvaluator pEvaluator, String pUri) {
		propertyResolver = pResolver;
		elReader = pReader;
		elEvalutor = pEvaluator;
		uri = pUri;
	}

	private List<Block<M>> stack;
	private Block<M> currentBlock;
	private int lineNumber;

	public SimpleTemplate<M> compile(String[] pLines) {
		stack = new ArrayList<>();
		currentBlock = new OuterBlock<M>();
		for (int i = 0;  i < pLines.length;  i++) {
			final String line = pLines[i];
			lineNumber = i+1;
			if (line.startsWith("<%")  &&  line.endsWith("%>")) {
				final String command = line.substring("<%".length(), line.length()-"%>".length());
				if (command.startsWith("if")) {
					final String expr = command.substring(2);
					final ElExpression elExpr;
					try {
						elExpr = elReader.parse(expr);
					} catch (RuntimeException re) {
						throw new IllegalStateException("Failed to parse if expression in line " + lineNumber + ": " + re.getMessage());
					}
					final IfBlock<M> ifBlock = new IfBlock<M>(lineNumber, elExpr);
					stack.add(currentBlock);
					currentBlock = ifBlock;
				} else if (command.startsWith("else")) {
					if ("else".equals(command.trim())) {
						if (currentBlock instanceof IfBlock) {
							final IfBlock<M> ifBlock = (IfBlock<M>) currentBlock;
							if (ifBlock.hasElse()) {
								throw new IllegalStateException("Unexpected <%else%> at line " + lineNumber + " (Expected <%/if%>)");
							} else {
								ifBlock.startElse();
							}
						} else if (currentBlock instanceof ForBlock) {
							throw new IllegalStateException("Unexpected <%else%> at line " + lineNumber + " (Expected <%/for%>)");
						} else {
							throw new IllegalStateException("Unexpected <%else%> at line " + lineNumber);
						}
					} else {
						throw new IllegalStateException("Failed to parse else statement at line " + lineNumber);
					}
				} else if (command.startsWith("/if")) {
					if ("/if".equals(command.trim())) {
						if (currentBlock instanceof IfBlock) {
							final IfBlock<M> ifBlock = (IfBlock<M>) currentBlock;
							if (stack.isEmpty()) {
								throw new IllegalStateException("Unexpected /if outside of any block.");
							}
							currentBlock = stack.remove(stack.size()-1);
							currentBlock.add((c) -> {
								final ElExpression el = ifBlock.getExpression();
								final Boolean b = (Boolean) elEvalutor.evaluate(el, c.getModel());
								final List<Consumer<Context<M>>> list = ifBlock.getList(b != null  &&  b.booleanValue());
								list.forEach((cons) -> cons.accept(c));
							});
						} else if (currentBlock instanceof ForBlock) {
							throw new IllegalStateException("Unexpected <%/if%> at line " + lineNumber + " (Expected <%/for%>)");
						} else {
							throw new IllegalStateException("Unexpected <%/if%> at line " + lineNumber);
						}
					} else {
						throw new IllegalStateException("Failed to parse /if statement at line " + lineNumber);
					}
				} else if (command.startsWith("for")) {
					final StringTokenizer st = new StringTokenizer(command.substring("for".length()), " ");
					final String loopVar;
					final String listVar;
					if (st.hasMoreTokens()) {
						loopVar = st.nextToken().trim();
						if (st.hasMoreTokens()) {
							final String inToken = st.nextToken();
							if (!"in".equals(inToken)) {
								throw new IllegalStateException("Expected <loopVar> in <listVar> at line " + lineNumber);
							}
							if (st.hasMoreTokens()) {
								listVar = st.nextToken();
							} else {
								listVar = null;
							}
						} else {
							listVar = null;
						}
					} else {
						loopVar = null;
						listVar = null;
					}
					if (loopVar == null  ||  listVar == null) {
						throw new IllegalStateException("Unable to parse for statement at line " + lineNumber);
					}
					if (loopVar.indexOf('.') != -1) {
						throw new IllegalStateException("Invalid loop variable in for statement at line " + lineNumber
							+ " (Expected atomic variable name without '.', got " + loopVar + ")");
					}
					final ForBlock<M> ifBlock = new ForBlock<M>(lineNumber, loopVar, listVar);
					stack.add(currentBlock);
					currentBlock = ifBlock;
				} else if (command.startsWith("/for")) {
					if ("/for".equals(command.trim())) {
						if (currentBlock instanceof ForBlock) {
							final ForBlock<M> forBlock = (ForBlock<M>) currentBlock;
							if (stack.isEmpty()) {
								throw new IllegalStateException("Unexpected /for outside of any block.");
							}
							currentBlock = stack.remove(stack.size()-1);
							currentBlock.add((c) -> {
								final Map<String,Object> contextMap = c.getModel();
								try {
									final Object o = propertyResolver.getValue(contextMap, forBlock.getListVar());
									final Iterable<Object> list;
									if (o == null) {
										throw new IllegalStateException("List variable " + forBlock.getListVar() + " resolved to null at line " + forBlock.getLineNumber());
									} else if (o instanceof Object[]) {
										final Object[] array = (Object[]) o;
										list = Arrays.asList(array);
									} else if (o instanceof Iterable) {
										@SuppressWarnings("unchecked")
										final Iterable<Object> iter = (Iterable<Object>) o;
										list = iter;
									} else {
										throw new IllegalStateException("List variable resolved to an instance of "
												+ o.getClass() + " at line " + forBlock.getLineNumber() + " (which isn't iterable).");
									}
									final Map<String,Object> map = new HashMap<>(contextMap);
									@SuppressWarnings("unchecked")
									final M m = (M) map;
									c.setModel((M) m);
									for (Object instance : list) {
										map.put(forBlock.getLoopVar(), instance);
										forBlock.getList().forEach((cons) -> cons.accept(c));
									}
								} finally {
									@SuppressWarnings("unchecked")
									final M m = (M) contextMap;
									c.setModel(m);
								}
							});
						} else if (currentBlock instanceof IfBlock) {
							throw new IllegalStateException("Unexpected <%/for%> at line " + lineNumber + " (Expected <%/if%>)");
						} else {
							throw new IllegalStateException("Unexpected <%/for%> at line " + lineNumber);
						}
					} else {
						throw new IllegalStateException("Failed to parse /if statement at line " + lineNumber);
					}
				}
			} else {
				compile(line);
			}
		}
		if (!stack.isEmpty()) {
			final Block<M> block = stack.remove(stack.size()-1);
			if (block instanceof IfBlock) {
				throw new IllegalStateException("At end of file: Unterminated if statement, beginning at line " + block.lineNumber);
			} else if (block instanceof ForBlock) {
				throw new IllegalStateException("At end of file: Unterminated for statement, beginning at line " + block.lineNumber);
			}
		}
		if (currentBlock == null  ||  !(currentBlock instanceof OuterBlock)) {
			throw new IllegalStateException("At end of file: Expected outer block, got " + currentBlock);
		}
		final List<Consumer<Context<Map<String,Object>>>> list = Generics.cast(currentBlock.list);
		final SimpleTemplate<Map<String,Object>> st = new SimpleTemplate<Map<String,Object>>(list);
		@SuppressWarnings("unchecked")
		final SimpleTemplate<M> result = (SimpleTemplate<M>) st;
		return result;
	}

	protected void compile(String pLine) {
		String value = pLine;
		for(;;) {
			int offset = value.indexOf("${");
			if (offset == -1) {
				final String v = value;
				currentBlock.add((c) -> c.writeln(v));
				return;
			} else {
				final int num = lineNumber;
				final String v = value.substring(0, offset);
				value = value.substring(offset+"${".length());
				currentBlock.add((c) -> c.write(v));
				final int endOffset = value.indexOf("}");
				if (endOffset == -1) {
					throw new IllegalStateException("Unterminated variable reference at line " + num);
				}
				final int nextOffset = value.indexOf("${");
				if (nextOffset != -1  &&  nextOffset < endOffset) {
					throw new IllegalStateException("Nested variable reference at line " + num);
				}
				final String varName = value.substring(0, endOffset);
				value = value.substring(endOffset+"}".length());
				currentBlock.add((c) -> {
					final String variable = varName;
					final Object o = propertyResolver.getValue(c.getModel(), variable);
					if (o == null) {
						throw new NullPointerException("Variable resolved to null at line " + num + ": " + variable);
					}
					c.write(o.toString());
				});
			}
		}
	}
}
