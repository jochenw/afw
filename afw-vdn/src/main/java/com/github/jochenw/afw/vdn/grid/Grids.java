package com.github.jochenw.afw.vdn.grid;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.jspecify.annotations.NonNull;

import com.github.jochenw.afw.core.function.Functions;
import com.github.jochenw.afw.core.function.Functions.FailableSupplier;
import com.github.jochenw.afw.core.function.Predicates;
import com.github.jochenw.afw.core.util.Strings;
import com.github.jochenw.afw.core.vdn.Filters;
import com.github.jochenw.afw.di.api.IComponentFactory;
import com.github.jochenw.afw.vdn.grid.GridContainer.Builder.Column;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.data.provider.CallbackDataProvider.CountCallback;
import com.vaadin.flow.data.provider.CallbackDataProvider.FetchCallback;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.QuerySortOrder;
import com.vaadin.flow.data.provider.SortDirection;

/** Utility class for working with {@link Grid grids}.
 */
public class Grids {
	/** Private constructor, because this class provides only static methods.
+	 */
	private Grids() {}

	/** A filter handler for grid columns. This object
	 * controls filtering, and sorting, based on the
	 * column values.
	 * @param <V> Type of the column values.
	 */
	public interface IFilterHandler<V> {
		/** Returns, whether the filter handler supports null values.
		 * If so, the value null may be passed to the generated
		 * {@link #getPredicate(String) predicate}, and the
		 * {@link #getComparator() comparator}.
		 * @return True, if the filter handler supports
		 *   null values.
		 */
		public boolean isNullsafe();
		/** Returns, whether the given filter value specifies,
		 * that filtering is active on the filter handlers
		 * column. Typically, filtering is not done, if the
		 * filter value is null, or empty.
		 * @param pValue The filter value.
		 * @return True, if the filter value suggests
		 *   filtering.
		 */
		public boolean isFiltering(String pValue);
		/** Creates a description of the filter, which would
		 * be created by invoking {@link #getPredicate(String)}
		 * with the given filter value.
		 * @param pColumnId Id of the column, which is
		 *   being filtered.
		 * @param pValue The filter value 
		 * @return The requested description, if any, or null,
		 *   if the filter value suggests no filtering.
		 */
		public String getDescription(String pColumnId, String pValue);
		/** Returns a predicate, which implements filtering
		 * for the filter handlers column, or null.
		 * Typically, the result is null, if
		 * {@link #isFiltering(String)} returns false,
		 * and vice versa.
		 * @param pValue The filter value.
		 * @return The generated predicate
		 */
		public Predicate<V> getPredicate(String pValue);
		/** Returns a comparator, which can be used to sort grid rows
		 * by this column.
		 * @return The requested comparator, or null, if sorting
		 * by this column is not supported.
		 */
		public Comparator<V> getComparator();
	}

	/** Interface of a column specification.
	 * @param <T> Type of the data bean, which is being displayed in the grid.
	 * @param <V> Type of the data bean attribute, which is being displayed
	 *   in this column.
	 */
	public interface IColumn<T,V> {
		/** The column id, must be unique within the grid.
		 * @return The columns id.
		 */
		public @NonNull String getId();
		/** A getter, which maps a data bean to the
		 * columns attribute value.
		 * @return The columns attribute value getter.
		 */
		public @NonNull Function<T,V> getMapper();
		/** The header string, which is being displayed in the
		 * grids top row for this column. Null, if the column
		 * should not be visible.
		 * @return The columns header string.
		 */
		public String getHeader();
		/** Returns the columns filter handler, which controls
		 * filtering, and sorting on this column.
		 * @return The filter handler, which has bee configured
		 *   for this column.
		 */
		public IFilterHandler<V> getFilterHandler();
		/** Returns the columns current filter value.
		 * @return The columns current filter value.
		 */
		public String getFilterValue();
		/** Returns the type of the column values.
		 * @return The type of the column values.
		 */
		public @NonNull Class<V> getValueType();
	}
	/** Creates a default filter handler for a string valued column.
	 * @param pCaseSensitive True, if the filter should be case sensitive.
	 *   Otherwise false.
	 * @return The created default filter hamdler.
	 */
	public final static IFilterHandler<String> stringFilterHandler(boolean pCaseSensitive) {
		return new IFilterHandler<String>() {
			@Override
			public boolean isNullsafe() {
				return true;
			}

			@Override
			public boolean isFiltering(String pValue) {
				return pValue != null  &&  pValue.length() > 0;
			}

			protected boolean isMatching(String pValue) {
				if (pValue != null) {
					for (int i = 0;  i < pValue.length();  i++) {
						final char c = pValue.charAt(i);
						switch(c) {
						case '?': return true;
						case '*': return true;
						case '%': return true;
						default: break;
						}
					}
				}
				return false;
			}

			@Override
			public String getDescription(String pColumnId, String pValue) {
				if (isFiltering(pValue)) {
					final String value = pValue.replace('*', '%');
					if (isMatching(pValue)) {
						return pColumnId + " LIKE '" + value + "'";
					} else {
						return pColumnId + " CONTAINS '" + value + "'";
					}
				}
				return null;
			}

			@Override
			public Predicate<String> getPredicate(String pValue) {
				if (isFiltering(pValue)) {
					final String value;
					if (pCaseSensitive) {
						value = pValue.replace('%', '*');
					} else {
						value = pValue.toLowerCase().replace('%', '*');
					}
					final Predicate<String> predicate;
					if (isMatching(value)) {
						predicate = Strings.matcher(value);
					} else {
						predicate = (s) -> s.contains(value);
					}
					return (s) -> {
						if (s == null) {
							return false;
						} else {
							if (pCaseSensitive) {
								return predicate.test(s);
							} else {
								return predicate.test(s.toLowerCase());
							}
						}
					};
				} else {
					return null;
				}
			}

			public Comparator<String> getComparator() {
				return (s1, s2) -> {
					if (s1 == null) {
						if (s2 == null) {
							return 0;
						} else {
							return 1;
						}
					} else {
						if (s2 == null) {
							return -1;
						} else {
							if (pCaseSensitive) {
								return s1.compareTo(s2);
							} else {
								return s1.toLowerCase().compareTo(s2.toLowerCase());
							}
						}
					}
				};
			}
		};
	}

	/** Creates a filter predicate for a grids rows, based on the given columns.
	 * @param <T> Type of the beans, which are being displayed in the grid.
	 * @param pColumns The grids columns.
	 * @return A predicate, which filters the grids beans, based on the
	 *   {@link IColumn#getFilterValue() filter values}.
	 */
	public static <T> Predicate<T> getPredicate(Stream<IColumn<T,?>> pColumns) {
		final List<Predicate<T>> list = new ArrayList<>();
		pColumns.forEach((col) -> {
			@SuppressWarnings("unchecked")
			final Function<T,Object> mapper = (Function<T, Object>) col.getMapper();
			@SuppressWarnings("unchecked")
			final IFilterHandler<Object> fh = (IFilterHandler<Object>) col.getFilterHandler();
			if (fh != null) {
				final Predicate<Object> oPredicate = fh.getPredicate(col.getFilterValue());
				if (oPredicate != null) {
					final Predicate<T> predicate = (t) -> {
						final Object o = mapper.apply(t);
						if (o == null) {
							if (fh.isNullsafe()) {
								return oPredicate.test(o);
							} else {
								return false;
							}
						} else {
							return oPredicate.test(o);
						}
					};
					list.add(predicate);
				}
			}
		});
		if (list.isEmpty()) {
			return null;
		} else {
			return Predicates.allOf(list);
		}
	}

	/** Creates a comparator for sorting the grids content by applying the sort
	 * order given by {@code pQsoList} on the columns given by
	 * {@code pColumnLookup}
	 * @param <T> Type of the data beans, which are being displayed.
	 * @param pQsoList The sort order, basically a list of column id's, and
	 * ascending/descending flags.
	 * @param pColumnLookup A lookup for the columns, based on the column
	 *   id's in {@code pQsoList}.
	 * @return The created comparator.
	 */
	public static <T> Comparator<T> getComparator(List<QuerySortOrder> pQsoList, Function<String, IColumn<T,?>> pColumnLookup) {
		final List<Comparator<T>> list = new ArrayList<>();
		for (QuerySortOrder qso : pQsoList) {
			final String colId = qso.getSorted();
			@SuppressWarnings("unchecked")
			final IColumn<T,Object> col = (IColumn<T,Object>) pColumnLookup.apply(colId);
			if (col == null) {
				throw new NullPointerException("Invalid column id for sort order: " + colId);
			}
			final Function<T,Object> mapper = (Function<T, Object>) col.getMapper();
			final IFilterHandler<Object> fh = col.getFilterHandler();
			if (fh == null) {
				throw new IllegalStateException("Sorting requested for column id " + colId + ", but no IFilterHandler available.");
			}
			final Comparator<Object> oComp = fh.getComparator();
			if (oComp == null) {
				throw new IllegalStateException("Sorting requested for column id " + colId + ", but no comparator available.");
			}
			final boolean ascending = qso.getDirection() == SortDirection.ASCENDING;
			final Comparator<T> comp = (t1,t2) -> {
				final Object o1 = t1 == null ? null : mapper.apply(t1);
				final Object o2 = t2 == null ? null : mapper.apply(t2);
				if (fh.isNullsafe()) {
					return oComp.compare(o1, o2);
				} else if (o1 == null) {
					if (o2 == null) {
						return 0;
					} else {
						return -1;
					}
				} else {
					if (o2 == null) {
						return 1;
					} else {
						return oComp.compare(o1, o2);
					}
				}
			};
			if (ascending) {
				list.add(comp);
			} else {
				list.add(comp.reversed());
			}
		}
		if (list.isEmpty()) {
			return null;
		} else {
			return (t1, t2) -> {
				for (Comparator<T> comp : list) {
					final int res = comp.compare(t1, t2);
					if (res != 0) {
						return res;
					}
				}
				return 0;
			};
		}
	}

	/** Creates a {@link DataProvider}, which supports filtering, and sorting the grid
	 * contents.
	 * @param <T> Type of the grids data bean.
	 * @param <F> Type of the filter configuration, which is being used.
	 * @param pValuesSupplier A supplier for the unfiltered collection of
	 *   beans. The data provider is supposed to apply proper filtering,
	 *   and sorting.
	 * @param pColumns The columns, that are being displayed in the grid.
	 * @return The created data provider.
	 */
	public static <T,F> DataProvider<T,F> getDataProvider(FailableSupplier<Collection<T>,?> pValuesSupplier,
			                                       Map<String,IColumn<T,?>> pColumns) {
		final Predicate<T> predicate = getPredicate(pColumns.values().stream());
		final FetchCallback<T,F> fetchCallback = (q) -> {
			final int limit = q.getLimit();
			final int offset = q.getOffset();
			final Comparator<T> comparator = getComparator(q.getSortOrders(), pColumns::get);
			final List<T> list = new ArrayList<>();
			Stream<T> stream = Functions.get(pValuesSupplier).stream();
			if (limit > 0  ||  offset > 0) {
				final Predicate<T> limitFilter = Filters.limit(offset, limit);
				stream = stream.filter(limitFilter);
			}
			if (predicate == null) {
				stream.forEach(list::add);
			} else {
				stream.filter(predicate).forEach(list::add);
			}
			if (comparator != null) {
				list.sort(comparator);
			}
			return list.stream();
		};
		final CountCallback<T,F> countCallback = (q) -> {
			final Collection<T> list = Functions.get(pValuesSupplier);
			if (predicate == null) {
				return list.size();
			} else {
				return (int) list.stream().count();
			}
		};
		return DataProvider.fromFilteringCallbacks(fetchCallback, countCallback);
	}

	/** Creates a new instance of {@link GridContainer.Builder}.
	 * 
	 * @param pComponentFactory The component factory, which is being
	 *   used to configure this builder, and the created container.
	 * @param pBeanType Bean type of the grid, that is being displayed within the
     *   created {@link GridContainer}.
	 * @param <T> Type of the grids data beans.
     * @return The created builder.
	 */
	public static <T>  GridContainer.Builder<T> builder(@NonNull IComponentFactory pComponentFactory, @NonNull Class<T> pBeanType) {
		return new GridContainer.Builder<T>(pComponentFactory, pBeanType);
	}

	/** Returns a description of the current filter status.
	 * @param pColumns The grid columns.
	 * @param pNoFilterText The text, which is being displayed, if no filter is active,
	 *   and all data beans are being displayed.
	 * @param <T> Type of the grids data beans.
	 * @param <V> Type of the grid columns.
	 * @return The current filter status description.
	 */
	public static <T,V> String getFilterDescription(Collection<Column<T,V>> pColumns, String pNoFilterText) {
		final List<String> filterValueDescriptions = new ArrayList<>();
		for (Column<T,V> col : pColumns) {
			final IFilterHandler<V> fh = col.getFilterHandler();
			if (fh != null) {
				final String filterValueDescription = fh.getDescription(col.getId(), col.getFilterValue());
				if (!Strings.isEmpty(filterValueDescription)) {
					filterValueDescriptions.add(filterValueDescription);
				}
			}
		}
		if (filterValueDescriptions.isEmpty()) {
			return pNoFilterText;
		} else {
			return String.join(" AND ", filterValueDescriptions);
		}
	}
}
