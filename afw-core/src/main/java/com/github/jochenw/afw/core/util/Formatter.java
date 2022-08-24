package com.github.jochenw.afw.core.util;

import java.lang.reflect.Array;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

import javax.annotation.Nonnull;


/** A Formatter is an object, that takes as input a format string,
 * interspersed with variable references like {}, {n}, or {var}.
 * These references are being replaced with values, that are 
 * also supplied as input.
 */
@SuppressWarnings("unchecked") public class Formatter {
	public static interface Resolver {
		Object resolve(int pNumber);
		Object resolve(String pKey);
	}

	public static class DefaultBriefSerializer implements BiConsumer<Object,StringBuilder> {
		private final int maxStringLen;
		private final int maxCollectionElements;
		private final String elementSeparator;

		public DefaultBriefSerializer(int pMaxStringLen, int pMaxCollectionElements, String pElementSeparator) {
			maxStringLen = pMaxStringLen;
			maxCollectionElements = pMaxCollectionElements;
			elementSeparator = pElementSeparator;
		}
		public DefaultBriefSerializer() {
			this(20,3,",");
		}
	
		protected void append(StringBuilder pDest, CharSequence pValue) {
			if (maxStringLen < 0  ||  pValue.length() <= maxStringLen) {
				pDest.append(pValue);
			} else {
				for (int i = 0;  i < maxStringLen;  i++) {
					pDest.append(pValue.charAt(i));
				}
				pDest.append("...");
			}
		}
		protected void appendArray(StringBuilder pDest, Object pArray) {
			pDest.append("[");
			int index = 0;
			while (index < Array.getLength(pArray)) {
				final Object o = Array.get(pArray, index);
				if (index > 0) {
					pDest.append(elementSeparator);
				}
				accept(o, pDest);
				if (maxCollectionElements >= 0) {
					if (++index == maxCollectionElements) {
						if (index < Array.getLength(pArray)) {
							if (index > 0) {
								pDest.append(elementSeparator);
							}
							pDest.append("...");
						}
						break;
					}
				}
			}
			pDest.append("]");
		}
		protected void appendMap(StringBuilder pDest, Map<String,Object> pMap) {
			pDest.append("[");
			int index = 0;
			final Iterator<Map.Entry<String,Object>> iter = pMap.entrySet().iterator();
			while (iter.hasNext()) {
				final Map.Entry<String,Object> en = iter.next();
				if (index > 0) {
					pDest.append(elementSeparator);
				}
				accept(en.getKey(), pDest);
				pDest.append(" => ");
				accept(en.getValue(), pDest);
				if (index >= 0) {
					if (++index == maxCollectionElements) {
						pDest.append(elementSeparator);
						pDest.append("...");
						break;
					}
				}
			}
			pDest.append("]");
		}
		protected void appendList(StringBuilder pDest, Object pIterable) {
			final String prefix, suffix;
			final Iterator<Object> iter;
			if (pIterable instanceof List) {
				prefix = "(";
				suffix = ")";
				final List<Object> list = ((List<Object>) pIterable);
				iter = list.iterator();
			} else if (pIterable instanceof Iterable) {
				prefix = "<";
				suffix = ">";
				final Iterable<Object> iterable = ((Iterable<Object>) pIterable);
				iter= iterable.iterator();
			} else if (pIterable instanceof Iterator) {
				prefix = "<<";
				suffix = ">>";
				final Iterator<Object> it = (Iterator<Object>) pIterable;
				iter = it;
			} else {
				throw new IllegalStateException("Invalid list object: " + pIterable.getClass().getName());
			}
			pDest.append(prefix);
			int index = 0;
			while (iter.hasNext()) {
				final Object o = iter.next();
				if (index > 0) {
					pDest.append(elementSeparator);
				}
				accept(o, pDest);
				if (index >= 0) {
					if (++index == maxCollectionElements) {
						pDest.append(elementSeparator);
						pDest.append("...");
					}
				}
			}
			pDest.append(suffix);
		}

		@Override
		public void accept(Object pValue, StringBuilder pSb) {
			if (pValue == null) {
				pSb.append("null");
			} else if (pValue instanceof String) {
				pSb.append('\"');
				append(pSb, (String) pValue);
				pSb.append('\"');
			} else if (pValue instanceof Number  ||  pValue instanceof Boolean) {
				append(pSb, pValue.toString());
			} else if (pValue instanceof Iterable) {
				final Iterable<Object> iterable = (Iterable<Object>) pValue;
				appendList(pSb, iterable);
			} else if (pValue.getClass().isArray()) {
				appendArray(pSb, pValue);
			} else if (pValue instanceof Map) {
				final Map<String,Object> map = (Map<String, Object>) pValue;
				appendMap(pSb, map);
			}
		}
		
	}
	public static class DefaultVerboseSerializer extends DefaultBriefSerializer {
		public DefaultVerboseSerializer() {
			super(-1, -1, ", ");
		}
	}
	public static DefaultBriefSerializer DEFAULT_BRIEF_SERIALIZER = new DefaultBriefSerializer();
	public static DefaultVerboseSerializer DEFAULT_VERBOSE_SERIALIZER = new DefaultVerboseSerializer();
	private final Resolver resolver;
	private final BiConsumer<Object, StringBuilder> terseSerializer, verboseSerializer;
	public Formatter(Resolver pResolver) {
		this(pResolver, DEFAULT_BRIEF_SERIALIZER, DEFAULT_VERBOSE_SERIALIZER);
	}
	public Formatter(Resolver pResolver, BiConsumer<Object, StringBuilder> pBriefSerializer,
	         BiConsumer<Object, StringBuilder> pDetailedSerializer) {
		resolver = pResolver;
		terseSerializer = pBriefSerializer;
		verboseSerializer = pDetailedSerializer;
	}
	public Resolver getResolver() { return resolver; }
	public BiConsumer<Object, StringBuilder> getTerseSerializer() { return terseSerializer; }
	public BiConsumer<Object, StringBuilder> getVerboseSerializer() { return verboseSerializer; }

	public String format(@Nonnull CharSequence pFormatString) {
		final @Nonnull CharSequence formatString = Objects.requireNonNull(pFormatString, "Format string");
		final StringBuilder value = new StringBuilder();
		StringBuilder reference = null;
		boolean verboseReference = false;
		int currentReferenceNumber = 0;
		for (int i = 0;  i <formatString.length();  ) {
			final char c1 = formatString.charAt(i);
			if (reference == null) {
				// We're within plain text. Let's see, whether a reference starts here...
				switch (c1) {
				case '{':
					if (i+1 < formatString.length()) {
						final char c2 = formatString.charAt(i+1);
						verboseReference = c2 == '{';
					} else {
						verboseReference = false;
					}
					if (verboseReference) {
						i += 2;
					} else {
						i++;
					}
					reference = new StringBuilder();
					break;
				case '\\':
					if (i+1 < formatString.length()) {
						final char c2 = formatString.charAt(i+1);
						value.append(c2);
						i += 2;
					} else {
						throw new IllegalArgumentException("Invalid format string (last character is '\\'):" + pFormatString);
					}
					break;
				default:
					value.append(c1);
					i++;
					break;
				}
			} else {
				if (c1 == '}') {
					if (verboseReference) {
						if (i+1 < formatString.length()) {
							final char c2 = formatString.charAt(i+1);
							if (c2 == '}') {
								String ref = reference.toString();
								int referenceNumber;
								if (ref.length() == 0) {
									referenceNumber = currentReferenceNumber++;
								} else {
									referenceNumber = -1;
								}
								append(value, referenceNumber, ref, true);
								reference = null;
								i += 2;
								break;
							} else {
								throw new IllegalArgumentException("Invalid format string"
										+ " (verbose reference terminated with \"}\", not \"}}\"):" + pFormatString);
							}
						}
					} else {
						String ref = reference.toString();
						int referenceNumber;
						if (ref.length() == 0) {
							referenceNumber = currentReferenceNumber++;
						} else {
							referenceNumber = -1;
						}
						append(value, referenceNumber, ref, false);
						reference = null;
						i++;
					}
				} else {
					reference.append(c1);
					i++;
				}
			}
		}
		return value.toString();
	}

	protected void append(StringBuilder pDest, int pReferenceNumber, String pReference, boolean pVerbose) {
		int refNumber = pReferenceNumber;
		String reference = null;
		Object value;
		if (pReferenceNumber == -1) {
			try {
				refNumber = Integer.parseInt(pReference);
				value = resolver.resolve(refNumber);
			} catch (NumberFormatException nfe) {
				reference = pReference;
				value = resolver.resolve(pReference);
			}
		} else {
			value = resolver.resolve(pReferenceNumber);
		}
		if (value == null) {
			if (reference == null) {
				throw new NullPointerException("Variable reference is null for number: " + refNumber);
			} else {
				throw new NullPointerException("Variable reference is null for key: " + reference);
			}
		}
		if (pVerbose) {
			verboseSerializer.accept(value, pDest);
		} else {
			terseSerializer.accept(value, pDest);
		}
	}
}
