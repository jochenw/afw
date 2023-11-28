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


/**
 * This template compiler is used internally by the {@link SimpleTemplateEngine}
 * for parsing template files.
 * @param <M> Type of the data model.
 */
public class SimpleTemplateCompiler<M extends Map<String,Object>> {
	/**
	 * Abstract base class for deriving block constructs in the compiled template.
	 * @param <T> The data model type.
	 */
	public abstract static class Block<T> {
		private final List<Consumer<Context<T>>> list = new ArrayList<>();
		private final int lineNumber;
		/** Creates a new block, that begins at the given line.
		 * @param pLineNumber The blocks start line.
		 */
		protected Block(int pLineNumber) {
			lineNumber = pLineNumber;
		}
		/** Adds a consumer to the list of context consumers.
		 * @param pConsumer The consumer, which is being added.
		 */
		public void add(Consumer<Context<T>> pConsumer) {
			list.add(pConsumer);
		}
		/**
		 * Returns the list of context consumers.
		 * @return The list of context consumers.
		 */
		public List<Consumer<Context<T>>> getList() {
			return list;
		}
		/** Returns the blocks line number.
		 * @return The blocks line number.
		 */
		public int getLineNumber() {
			return lineNumber;
		}
	}
	/** The outer block is the templates root block.
	 * @param <T> The data model type.
	 */
	public static class OuterBlock<T> extends Block<T> {
		/** Creates a new instance with line number 1.
		 */
		public OuterBlock() {
			super(1);
		}
	}
	/** An if block represents an optional part of the template.
	 * @param <T> The data model type.
	 */
	public static class IfBlock<T> extends Block<T> {
		private final ElExpression expr;
		private final List<Consumer<Context<T>>> elseList = new ArrayList<>();
		private boolean haveElse;

		/** Creates anew instance with the given line number, and condition.
		 * @param pLineNumber The if blocks line number.
		 * @param pExpr The if blocks condition.
		 */
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
		/** Starts an else-subblock.
		 */
		public void startElse() {
			haveElse = true;
		}
		/** Returns, whether there is an else-subblock.
		 * @return True, if there is an else-subblock.
		 */
		public boolean hasElse() {
			return haveElse;
		}
		/** Returns the condition.
		 * @return The condition, that determines, whether the if block is
		 * included in the output, or not.
		 */
		public ElExpression getExpression() {
			return expr;
		}
		/**
		 * Returns the list of context consumers.
		 * @param pValue True, if the main block is being returned.
		 *   False for the else subblock.
		 * @return The list of context consumers.
		 */
		public List<Consumer<Context<T>>> getList(boolean pValue) {
			return pValue ? getList() : elseList;
		}
	}
	/**  A for block represents a repeatable part of the template.
	 * @param <T> The data model type.
	 */
	public static class ForBlock<T> extends Block<T> {
		private final String listVar, loopVar;
		/** Creates a new instance with the given loop variable, and list variable.
		 * @param pLineNumber The for block's line number. (Number of the blocks declaration)
		 * @param pLoopVar The loop variable.
		 * @param pListVar The list variable.
		 */
		public ForBlock(int pLineNumber, String pLoopVar, String pListVar) {
			super(pLineNumber);
			listVar = pListVar;
			loopVar = pLoopVar;
		}
		/** Returns the list variable.
		 * @return The list variable.
		 */
		public String getListVar() { return listVar; }
		/** Returns the loop variable.
		 * @return The loop variable.
		 */
		public String getLoopVar() { return loopVar; }
	}
	private final PropertyResolver propertyResolver;
	private final ElReader elReader;
	private final ElEvaluator elEvalutor;
	private final String uri;

	/** Creates a new instance with the given property resolver, EL expression parser, EL expression
	 * evaluator, and the given template URI.
	 * @param pResolver The property resolver, which is being used to resolve variables in the model.
	 * @param pReader The EL expression parser.
	 * @param pEvaluator The EL expression evaluator.
	 * @param pUri The template URI.
	 */
	public SimpleTemplateCompiler(PropertyResolver pResolver, ElReader pReader, ElEvaluator pEvaluator, String pUri) {
		propertyResolver = pResolver;
		elReader = pReader;
		elEvalutor = pEvaluator;
		uri = pUri;
	}

	private List<Block<M>> stack;
	private Block<M> currentBlock;
	private int lineNumber;

	/** Called to parse, and compile the template.
	 * @param pLines The template lines.
	 * @return The parsed, and compiled, template.
	 */
	public SimpleTemplate<M> compile(String[] pLines) {
		stack = new ArrayList<>();
		currentBlock = new OuterBlock<M>();
		for (int i = 0;  i < pLines.length;  i++) {
			final String line = pLines[i];
			lineNumber = i+1;
			if (line.startsWith("<%")  &&  line.endsWith("%>")) {
				final String command = line.substring("<%".length(), line.length()-"%>".length()).trim();
				if (command.startsWith("if")) {
					final String expr = command.substring(2);
					final ElExpression elExpr;
					try {
						elExpr = elReader.parse(expr);
					} catch (RuntimeException re) {
						throw new IllegalStateException("Failed to parse if expression in " + uri
								                        + ", line " + lineNumber + ": " + re.getMessage());
					}
					final IfBlock<M> ifBlock = new IfBlock<M>(lineNumber, elExpr);
					stack.add(currentBlock);
					currentBlock = ifBlock;
				} else if (command.startsWith("else")) {
					if ("else".equals(command.trim())) {
						if (currentBlock instanceof IfBlock) {
							final IfBlock<M> ifBlock = (IfBlock<M>) currentBlock;
							if (ifBlock.hasElse()) {
								throw new IllegalStateException("Unexpected <%else%> at " + uri
										                        + ", line " + lineNumber + " (Expected <%/if%>)");
							} else {
								ifBlock.startElse();
							}
						} else if (currentBlock instanceof ForBlock) {
							throw new IllegalStateException("Unexpected <%else%> at " + uri
									                        + ", line " + lineNumber + " (Expected <%/for%>)");
						} else {
							throw new IllegalStateException("Unexpected <%else%> at " + uri
									                        + ", line " + lineNumber);
						}
					} else {
						throw new IllegalStateException("Failed to parse else statement at " + uri +
								                        ", line " + lineNumber);
					}
				} else if (command.startsWith("/if")) {
					if ("/if".equals(command.trim())) {
						if (currentBlock instanceof IfBlock) {
							final IfBlock<M> ifBlock = (IfBlock<M>) currentBlock;
							if (stack.isEmpty()) {
								throw new IllegalStateException("At " + uri + ", line " + lineNumber
										                        + ": /if outside of any block.");
							}
							currentBlock = stack.remove(stack.size()-1);
							currentBlock.add((c) -> {
								final ElExpression el = ifBlock.getExpression();
								final Boolean b = evaluate(el, c.getModel());
								final List<Consumer<Context<M>>> list = ifBlock.getList(b != null  &&  b.booleanValue());
								list.forEach((cons) -> cons.accept(c));
							});
						} else if (currentBlock instanceof ForBlock) {
							throw new IllegalStateException("Unexpected <%/if%> at " + uri
									                        + ", line " + lineNumber + " (Expected <%/for%>)");
						} else {
							throw new IllegalStateException("Unexpected <%/if%> at " + uri
									                        + ", line " + lineNumber);
						}
					} else {
						throw new IllegalStateException("Failed to parse /if statement at " + uri
								                        + ", line " + lineNumber);
					}
				} else if (command.startsWith("for")) {
					final StringTokenizer st = new StringTokenizer(command.substring("for".length()), " ");
					final String loopVar;
					final String listVar;
					if (st.hasMoreTokens()) {
						loopVar = st.nextToken().trim();
						if (st.hasMoreTokens()) {
							final String inToken = st.nextToken();
							if (!"in".equals(inToken)  &&  !":".equals(inToken)) {
								throw new IllegalStateException("Expected <loopVar> in <listVar> at " + uri
										                        + ", line " + lineNumber);
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
						throw new IllegalStateException("Unable to parse for statement at " + uri
								                        + ", line " + lineNumber);
					}
					if (loopVar.indexOf('.') != -1) {
						throw new IllegalStateException("Invalid loop variable in for statement at " + uri
								+ ", line " + lineNumber
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
								throw new IllegalStateException("At " + uri + ", line " + lineNumber
										                        + ": Unexpected /for outside of any block.");
							}
							currentBlock = stack.remove(stack.size()-1);
							currentBlock.add((c) -> {
								final Map<String,Object> contextMap = c.getModel();
								try {
									final Object o = propertyResolver.getValue(contextMap, forBlock.getListVar());
									final Iterable<Object> list;
									if (o == null) {
										throw new IllegalStateException("List variable " + forBlock.getListVar()
										                                + " resolved to null at " + uri
										                                + ", line " + forBlock.getLineNumber());
									} else if (o instanceof Object[]) {
										final Object[] array = (Object[]) o;
										list = Arrays.asList(array);
									} else if (o instanceof Iterable) {
										@SuppressWarnings("unchecked")
										final Iterable<Object> iter = (Iterable<Object>) o;
										list = iter;
									} else {
										throw new IllegalStateException("List variable resolved to an instance of "
												+ o.getClass() + " at " + uri + ", line "
												+ forBlock.getLineNumber() + " (which isn't iterable).");
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
				} else {
					throw new IllegalStateException("Invalid command at line " + lineNumber + ": " + command);
				}
			} else {
				compile(line);
			}
		}
		if (!stack.isEmpty()) {
			final Block<M> block = stack.remove(stack.size()-1);
			if (block instanceof IfBlock) {
				throw new IllegalStateException("At end of file " + uri
						                        + ": Unterminated if statement, beginning at line " + block.lineNumber);
			} else if (block instanceof ForBlock) {
				throw new IllegalStateException("At end of file " + uri
						                        + ": Unterminated for statement, beginning at line " + block.lineNumber);
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

	/** Called to evaluate an EL expression, applying the given model.
	 * @param pExpression The EL expression, that is being evaluated.
	 * @param pModel The model, that is being applied.
	 * @return The evaluated result.
	 */
	protected boolean evaluate (ElExpression pExpression, M pModel) {
		final Object o = elEvalutor.evaluate(pExpression, pModel);
		if (o == null) {
			throw new IllegalStateException("At " + uri + ", line " + lineNumber
                    + ": Expression evaluates to null.");
		}
		if (o instanceof Boolean) {
			return ((Boolean) o).booleanValue();
		} else if (o instanceof String) {
			return Boolean.parseBoolean((String) o);
		} else {
			throw new IllegalStateException("At " + uri + ", line " + lineNumber
					                        + ": Expression evaluates to invalid object "
					                        + o.getClass().getName());
		}
	}

	/** Called to compile the given template line.
	 * @param pLine The line, that is being compiled.
	 */
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
					throw new IllegalStateException("Unterminated variable reference at " + uri + ", line " + num);
				}
				final int nextOffset = value.indexOf("${");
				if (nextOffset != -1  &&  nextOffset < endOffset) {
					throw new IllegalStateException("Nested variable reference at " + uri + ", line " + num);
				}
				final String varName = value.substring(0, endOffset);
				value = value.substring(endOffset+"}".length());
				currentBlock.add((c) -> {
					final String variable = varName;
					final Object o = propertyResolver.getValue(c.getModel(), variable);
					if (o == null) {
						throw new NullPointerException("Variable resolved to null at " + uri + ", line " + num + ": " + variable);
					}
					c.write(o.toString());
				});
			}
		}
	}
}
