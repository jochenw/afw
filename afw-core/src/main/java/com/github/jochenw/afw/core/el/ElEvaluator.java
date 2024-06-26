/*
 * Copyright 2018 Jochen Wiedmann
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
package com.github.jochenw.afw.core.el;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.jspecify.annotations.NonNull;

import com.github.jochenw.afw.core.el.tree.AddExpression;
import com.github.jochenw.afw.core.el.tree.AndExpression;
import com.github.jochenw.afw.core.el.tree.ElExpression;
import com.github.jochenw.afw.core.el.tree.EqualityExpression;
import com.github.jochenw.afw.core.el.tree.MultiplyExpression;
import com.github.jochenw.afw.core.el.tree.OrExpression;
import com.github.jochenw.afw.core.el.tree.RelationalExpression;
import com.github.jochenw.afw.core.el.tree.UnaryExpression;
import com.github.jochenw.afw.core.el.tree.ValueExpression;
import com.github.jochenw.afw.core.el.tree.VariableReferenceExpression;
import com.github.jochenw.afw.core.util.Objects;


/** An instance of this class may be used to resolve the
 * value of a given EL expression. To use the ElEvaluator,
 * you first compile the EL expression to an instance of
 * {@link ElExpression} by invoking {@link ElReader#parse(String)}.
 * The compiled EL expression may then be evaluted by
 * invoking {@link #evaluate(ElExpression, Object)},
 * passing the compiled EL expression, and an object model.
 */
public class ElEvaluator {
	/** An object, which is being used internally to
	 * calculate the expressions value, applying
	 * the given model.
	 */
	public static class Evaluator {
		private final PropertyResolver resolver;
		private final Object model;
		private final List<Object> parameters;

		Evaluator(PropertyResolver pResolver, Object pModel) {
			this(pResolver, pModel, Collections.emptyList());
		}

		Evaluator(PropertyResolver pResolver, Object pModel, List<Object> pParameters) {
			resolver = pResolver;
			model = pModel;
			parameters = pParameters;
		}

		/**
		 * Returns the evaluators model, that will be used to evaluate
		 * expressions.
		 * @return The evalauator's data model.
		 */
		public Object getModel() {
			return model;
		}

		/** Evaluates the given EL expression, applying the evaluators
		 * {@link #getModel()}.
		 * @param pExpression The expression, that is being evaluated.
		 * @return The evaluations result object.
		 */
		protected Object evaluate(ElExpression pExpression) {
			return evaluate(pExpression.getOrExpression());
		}

		/** Evaluates the given or expression, applying the evaluators
		 * {@link #getModel()}.
		 * @param pExpression The expression, that is being evaluated.
		 * @return The evaluations result object.
		 */
		protected Object evaluate(OrExpression pExpression) {
			final List<AndExpression> expressions = pExpression.getAndExpressions();
			Object left = evaluate(expressions.get(0));
			if (expressions.size() == 1) {
				return left;
			} else {
				for (int i = 1;  i < expressions.size();  i++) {
					final Object right = evaluate(expressions.get(i));
					if (left == null  || right == null) {
						throw new IllegalStateException("Unable to determine OR value on NULL values.");
					} else if (left instanceof Boolean  &&  right instanceof Boolean) {
						final boolean b = ((Boolean) left).booleanValue()  ||  ((Boolean) right).booleanValue();
						left = Boolean.valueOf(b);
						if (b) {
							return left;
						}
					} else {
						throw new IllegalStateException("Unable to determine OR value for an instance of "
								+ left.getClass().getName() + ", and an instance of " + right.getClass().getName());
					}
	 			}
			}
			return left;
		}

		/** Evaluates the given and expression, applying the evaluators
		 * {@link #getModel()}.
		 * @param pExpression The expression, that is being evaluated.
		 * @return The evaluations result object.
		 */
		protected Object evaluate(AndExpression pExpression) {
			final List<EqualityExpression> expressions = pExpression.getEqualityExpressions();
			Object left = evaluate(expressions.get(0));
			if (expressions.size() == 1) {
				return left;
			} else {
				for (int i = 1;  i < expressions.size();  i++) {
					final Object right = evaluate(expressions.get(i));
					if (left == null  || right == null) {
						throw new IllegalStateException("Unable to determine AND value on NULL values.");
					} else if (left instanceof Boolean  &&  right instanceof Boolean) {
						final boolean b = ((Boolean) left).booleanValue()  &&  ((Boolean) right).booleanValue();
						left = Boolean.valueOf(b);
						if (!b) {
							return left;
						}
					} else {
						throw new IllegalStateException("Unable to determine AND value for an instance of "
								+ left.getClass().getName() + ", and an instance of " + right.getClass().getName());
					}
	 			}
			}
			return left;
		}

		/** Evaluates the given equality expression, applying the evaluators
		 * {@link #getModel()}.
		 * @param pExpression The expression, that is being evaluated.
		 * @return The evaluations result object.
		 */
		protected Object evaluate(EqualityExpression pExpression) {
			final RelationalExpression leftExp = pExpression.getLeft();
			final Object left = evaluate(leftExp);
			final EqualityExpression.Op op = pExpression.getOp();
			if (op == null) {
				return left;
			} else {
				final RelationalExpression rightExp = pExpression.getRight();
				final Object right = evaluate(rightExp);
				if (left == null  ||  right == null) {
					switch(op) {
					case EQ:
						return Boolean.valueOf(left == null  &&  right == null);
					case NE:
						return Boolean.valueOf(left != null  ||  right != null);
					default:
						throw new IllegalStateException("Invalid equality operation: " + op);
					}
				} else {
					switch(op) {
					case EQ:
						return Boolean.valueOf(left.equals(right));
					case NE:
						return Boolean.valueOf(!left.equals(right));
					default:
						throw new IllegalStateException("Invalid equality operation: " + op);
					}
				}
			}
		}
		
		/** Evaluates the given relational expression, applying the evaluators
		 * {@link #getModel()}.
		 * @param pExpression The expression, that is being evaluated.
		 * @return The evaluations result object.
		 */
		protected Object evaluate(RelationalExpression pExpression) {
			final AddExpression leftExp = pExpression.getLeft();
			final Object left = evaluate(leftExp);
			final RelationalExpression.Op op = pExpression.getOp();
			if (op == null) {
				return left;
			} else {
				final AddExpression rightExp = pExpression.getRight();
				final Object right = evaluate(rightExp);
				if (left == null  ||  right == null) {
					throw new IllegalStateException("Unable to compare NULL values.");
				} else {
					if (left instanceof Long  &&  right instanceof Long) {
						final long lLeft = ((Long) left).longValue();
						final long lRight = ((Long) right).longValue();
						switch(op) {
						case GE:
							return Boolean.valueOf(lLeft >= lRight);
						case GT:
							return Boolean.valueOf(lLeft > lRight);
						case LE:
							return Boolean.valueOf(lLeft <= lRight);
						case LT:
							return Boolean.valueOf(lLeft < lRight);
						default:
							throw new IllegalStateException("Invalid relational operation: " + op);
						}
					} else if (left instanceof Double  && right instanceof Double) {
						final double dLeft = ((Double) left).doubleValue();
						final double dRight = ((Double) right).doubleValue();
						switch(op) {
						case GE:
							return Boolean.valueOf(dLeft >= dRight);
						case GT:
							return Boolean.valueOf(dLeft > dRight);
						case LE:
							return Boolean.valueOf(dLeft <= dRight);
						case LT:
							return Boolean.valueOf(dLeft < dRight);
						default:
							throw new IllegalStateException("Invalid relational operation: " + op);
						}
					} else {
						throw new IllegalStateException("Unable to compare an instance of " + left.getClass().getName()
								+ ", and an instance of " + right.getClass().getName());
					}
				}
			}
		}

		/** Evaluates the given add expression, applying the evaluators
		 * {@link #getModel()}.
		 * @param pExpression The expression, that is being evaluated.
		 * @return The evaluations result object.
		 */
		@SuppressWarnings("unused")
		protected Object evaluate(AddExpression pExpression) {
			final List<?> objects = pExpression.getObjects();
			final MultiplyExpression meLeft = (MultiplyExpression) objects.get(0);
			Object left = evaluate(meLeft);
			for (int i = 1;  i < objects.size();  i += 2) {
				final AddExpression.@NonNull Op op = Objects.requireNonNull((AddExpression.Op) objects.get(i));
				final MultiplyExpression meRight = (MultiplyExpression) objects.get(i+1);
				final Object right = evaluate(meRight);
				if (left == null) {
					throw new IllegalStateException("Unable to add, or subtract from a NULL object.");
				}
				if (right == null) {
					throw new IllegalStateException("Unable to add, or subtract a NULL object.");
				}
				switch(op) {
				  case PLUS:
					  if (left instanceof Long  &&  right instanceof Long) {
						  final long l = ((Long) left).longValue() + ((Long) right).longValue();
						  left = Long.valueOf(l);
					  } else if (left instanceof Double  &&  right instanceof Double) {
						  final double d = ((Double) left).doubleValue() + ((Double) right).doubleValue();
						  left = Double.valueOf(d);
					  } else {
						  throw new IllegalStateException("Unable to add an instance of " + left.getClass().getName()
								  + ", and an instance of " + right.getClass().getName());
					  }
				  case MINUS:
					  if (left instanceof Long  &&  right instanceof Long) {
						  final long l = ((Long) left).longValue() - ((Long) right).longValue();
						  left = Long.valueOf(l);
					  } else if (left instanceof Double  &&  right instanceof Double) {
						  final double d = ((Double) left).doubleValue() - ((Double) right).doubleValue();
						  left = Double.valueOf(d);
					  } else {
						  throw new IllegalStateException("Unable to subtract an instance of " + right.getClass().getName()
								  + " from an instance of " + left.getClass().getName());
					  }
				  default:
					  throw new IllegalStateException("Invalid add operation: " + op);
				}
			}
			return left;
		}

		/** Evaluates the given multiply expression, applying the evaluators
		 * {@link #getModel()}.
		 * @param pExpression The expression, that is being evaluated.
		 * @return The evaluations result object.
		 */
		protected Object evaluate(MultiplyExpression pExpression) {
			final UnaryExpression ueLeft = pExpression.getLeft();
			final Object left = evaluate(ueLeft);
			final MultiplyExpression.Op op = pExpression.getOp();
			if (op == null) {
				return left;
			} else {
				final UnaryExpression ueRight = pExpression.getRight();
				final Object right = evaluate(ueRight);
				if (left == null  ||  right == null) {
					throw new IllegalStateException("Unable to multiply, or divide a NULL object.");
				}
				switch(op) {
				case MULTIPLY:
					if (left instanceof Long  &&  right instanceof Long) {
						final long l = ((Long) left).longValue() * ((Long) right).longValue();
						return Long.valueOf(l);
					} else if (left instanceof Double  &&  right instanceof Double) {
						final double d = ((Double) left).doubleValue() * ((Double) right).doubleValue();
						return Double.valueOf(d);
					} else {
						throw new IllegalStateException("Unable to multiply an instance of " + left.getClass().getName()
								+ " and an instance of " + right.getClass().getName());
					}
				case DIVIDE:
					if (left instanceof Long  &&  right instanceof Long) {
						final long l = ((Long) left).longValue() / ((Long) right).longValue();
						return Long.valueOf(l);
					} else if (left instanceof Double  &&  right instanceof Double) {
						final double d = ((Double) left).doubleValue() / ((Double) right).doubleValue();
						return Double.valueOf(d);
					} else {
						throw new IllegalStateException("Unable to divide an instance of " + left.getClass().getName()
								+ " by an instance of " + right.getClass().getName());
					}
				case MODULUS:
					if (left instanceof Long  &&  right instanceof Long) {
						final long l = ((Long) left).longValue() % ((Long) right).longValue();
						return Long.valueOf(l);
					} else if (left instanceof Double  &&  right instanceof Double) {
						final double d = ((Double) left).doubleValue() % ((Double) right).doubleValue();
						return Double.valueOf(d);
					} else {
						throw new IllegalStateException("Unable to build modulus for an instance of " + left.getClass().getName()
								+ " by an instance of " + right.getClass().getName());
					}
				default:
					throw new IllegalStateException("Invalid multiply operation: " + op);
				}
			}
		}

		/** Evaluates the given unary expression, applying the evaluators
		 * {@link #getModel()}.
		 * @param pExpression The expression, that is being evaluated.
		 * @return The evaluations result object.
		 */
		protected Object evaluate(UnaryExpression pExpression) {
			final ValueExpression valueExp = pExpression.getValue();
			final Object value = evaluate(valueExp);
			final UnaryExpression.Op op = pExpression.getOp();
			if (op == null) {
				return value;
			} else {
				switch (op) {
				case NOT:
				  {
					if (value == null) {
						throw new IllegalStateException("Unable to evaluate NOT on a NULL object");
					} else if (value instanceof Boolean) {
						final boolean b = ((Boolean) value).booleanValue();
						return Boolean.valueOf(!b);
					} else {
						throw new IllegalStateException("Unable to evaluate NOT on an instance of " + value.getClass().getName());
					}
				  }
				case MINUS:
				  {
					if (value == null) {
						throw new IllegalStateException("Unable to evaluate MINUS on a NULL object");
					} else if (value instanceof Double) {
						final double d = ((Double) value).doubleValue();
						return Double.valueOf(-d);
					} else if (value instanceof Long) {
						final long l = ((Long) value).longValue();
						return Long.valueOf(-l);
					} else {
						throw new IllegalStateException("Unable to evaluate MINUS on an instance of " + value.getClass().getName());
					}
				  }
				case EMPTY:
				  {
					if (value == null) {
						throw new IllegalStateException("Unable to evaluate EMPTY on a NULL object");
					} else if (value instanceof String) {
						return Boolean.valueOf(((String) value).length() == 0);
					} else {
						throw new IllegalStateException("Unable to evaluate MINUS on an instance of " + value.getClass().getName());
					}
				  }
				default:
					throw new IllegalStateException("Invalid unary operation: " + op);
				}
			}
		}
		
		/** Evaluates the given value expression, applying the evaluators
		 * {@link #getModel()}.
		 * @param pExpression The expression, that is being evaluated.
		 * @return The evaluations result object.
		 */
		protected Object evaluate(ValueExpression pExpression) {
			final Boolean b = pExpression.getBooleanValue();
			if (b == null) {
				final Double d = pExpression.getDoubleValue();
				if (d == null) {
					final Long l = pExpression.getLongValue();
					if (l == null) {
						final String s = pExpression.getStringValue();
						if (s == null) {
							final Integer placeholderIndex = pExpression.getPlaceholderIndex();
							if (placeholderIndex == null) {
								final VariableReferenceExpression vre = pExpression.getVariableReference();
								if (vre == null) {
									final ElExpression e = pExpression.getElExpression();
									if (e == null) {
										return null;
									} else {
										return evaluate(e);
									}
								} else {
									return evaluate(vre);
								}
							} else {
								final int index = placeholderIndex.intValue();
								if (index >= parameters.size()) {
									throw new IllegalStateException("Expected at least " + (index+1)
											+ " parameters, got " + parameters.size());
								}
								return canonify(parameters.get(index));
							}
						} else {
							return s;
						}
					} else {
						return l;
					}
				} else {
					return d;
				}
			} else {
				return b;
			}
		}
		
		/** Evaluates the given variable reference expression, applying the evaluators
		 * {@link #getModel()}.
		 * @param pExpression The expression, that is being evaluated.
		 * @return The evaluations result object.
		 */
		protected Object evaluate(VariableReferenceExpression pExpression) {
			final String property = pExpression.getVar();
			if (property.endsWith(".toString")) {
				final Object object = resolver.getValue(model, property.substring(0, property.length()-9));
				return String.valueOf(object);
			} else if (property.endsWith(".toInt")) {
				final Object object = resolver.getValue(model, property.substring(0, property.length()-6));
				if (object == null) {
					throw new IllegalStateException("Unable to convert a NULL value to an integer.");
				} else if (object instanceof Long) {
					return object;
				} else if (object instanceof Integer
						    ||  object instanceof Short
						    ||  object instanceof Byte) {
					return Long.valueOf(((Number) object).longValue());
				} else if (object instanceof String) {
					try {
						return Long.valueOf((String) object);
					} catch (NumberFormatException e) {
						throw new IllegalStateException("Invalid integer value for property " + property + ": " + object);
					}
				} else {
					throw new IllegalStateException("Unable to convert an instance of " + object.getClass().getName()
							+ " to an integer.");
				}
			} else if (property.endsWith(".toFloat")) {
				final Object object = resolver.getValue(model, property.substring(0, property.length()-8));
				if (object == null) {
					throw new IllegalStateException("Unable to convert a NULL value to a float.");
				} else if (object instanceof Double) {
					return object;
				} else if (object instanceof Long
						     ||  object instanceof Integer
						     ||  object instanceof Short
						     ||  object instanceof Byte
						     ||  object instanceof Float) {
					return Double.valueOf(((Number) object).doubleValue());
				} else if (object instanceof String) {
					try {
						return Double.valueOf((String) object);
					} catch (NumberFormatException e) {
						throw new IllegalStateException("Invalid float value for property " + property + ": " + object);
					}
				} else {
					throw new IllegalStateException("Unable to convert an instance of " + object.getClass().getName()
							+ " to a floating point number.");
				}
			} else {
				return canonify(resolver.getValue(model, property));
			}
		}

		/** Converts the given value to a canonical representation
		 * of the same value. For example, integer values (bytes, shorts, 32 bit integers)
		 * are being converted to 64 bit integers.
		 * @param pValue The value, that is being converted.
		 * @return The evaluations result object.
		 */
		protected Object canonify(Object pValue) {
			if (pValue == null  ||  pValue instanceof Long  ||  pValue instanceof Double  ||  pValue instanceof String) {
				return pValue;
			} else if (pValue instanceof Byte
					     ||  pValue instanceof Short
					     ||  pValue instanceof Integer
					     ||  pValue instanceof Long
					     ||  pValue instanceof AtomicInteger
					     ||  pValue instanceof AtomicLong) {
				return Long.valueOf(((Number) pValue).longValue());
			} else if (pValue instanceof Float) {
				return Double.valueOf(((Number) pValue).doubleValue());
			} else if (pValue instanceof Boolean) {
				return pValue;
			} else {
				throw new IllegalStateException("Invalid value type: " + pValue.getClass().getName());
			}
		}
	}

	/**
	 * Creates a new instance with the given property resolver.
	 * @param pResolver The property resolver, which is being used
	 *   to evaluate properties in the model.
	 */
	public ElEvaluator(PropertyResolver pResolver) {
		propertyResolver = pResolver;
	}
	
	private final PropertyResolver propertyResolver;

	/** Called to evaluate the given EL expression, applying the
	 * given model.
	 * @param pExpression The EL expression, which is being evaluated.
	 * @param pModel The data model, which is being applied.
	 * @return The evaluated expressions result.
	 */
	public Object evaluate(ElExpression pExpression, Object pModel) {
		return new Evaluator(propertyResolver, pModel).evaluate(pExpression);
	}

	/** Called to evaluate the given EL expression (which must not use any
	 * placeholders), applying the given model.
	 * @param pExpression The EL expression, which is being evaluated.
	 * @param pModel The data model, which is being applied.
	 * @param pParameters A 
	 * @return The evaluated expressions result.
	 * @see #evaluate(ElExpression, Object, List)
	 */
	public Object evaluate(ElExpression pExpression, Object pModel, Object... pParameters) {
		if (pParameters == null) {
			return new Evaluator(propertyResolver, pModel).evaluate(pExpression);
		} else {
			return new Evaluator(propertyResolver, pModel, Arrays.asList(pParameters)).evaluate(pExpression);
		}
	}

	/** Called to evaluate the given EL expression (which may use
	 * placeholders), applying the given model.
	 * @param pExpression The EL expression, which is being evaluated.
	 * @param pModel The data model, which is being applied.
	 * @param pParameters The values, which are being inserted for placeholders.
	 * @return The evaluated expressions result.
	 * @see #evaluate(ElExpression, Object)
	 */
	public Object evaluate(ElExpression pExpression, Object pModel, List<Object> pParameters) {
		if (pParameters == null) {
			return new Evaluator(propertyResolver, pModel).evaluate(pExpression);
		} else {
			return new Evaluator(propertyResolver, pModel, pParameters).evaluate(pExpression);
		}
	}
}
