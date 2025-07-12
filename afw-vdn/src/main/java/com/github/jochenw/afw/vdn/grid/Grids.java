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
import com.github.jochenw.afw.di.api.IComponentFactory;
import com.github.jochenw.afw.vdn.grid.GridContainer.Builder.Column;
import com.vaadin.flow.data.provider.CallbackDataProvider.CountCallback;
import com.vaadin.flow.data.provider.CallbackDataProvider.FetchCallback;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.QuerySortOrder;
import com.vaadin.flow.data.provider.SortDirection;

public class Grids {
	public interface IFilterHandler<V> {
		public boolean isNullsafe();
		public boolean isFiltering(String pValue);
		public String getDescription(String pColumnId, String pValue);
		public Predicate<V> getPredicate(String pValue);
		public Comparator<V> getComparator();
	}
	public interface IColumn<T,V> {
		public @NonNull String getId();
		public @NonNull Function<T,V> getMapper();
		public String getHeader();
		public IFilterHandler<V> getFilterHandler();
		public String getFilterValue();
	}
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

	public static <T,F> DataProvider<T,F> getDataProvider(FailableSupplier<Collection<T>,?> pValuesSupplier,
			                                       Map<String,IColumn<T,?>> pColumns) {
		final Predicate<T> predicate = getPredicate(pColumns.values().stream());
		final FetchCallback<T,F> fetchCallback = (q) -> {
			final Comparator<T> comparator = getComparator(q.getSortOrders(), pColumns::get);
			final List<T> list = new ArrayList<>();
			final Stream<T> stream = Functions.get(pValuesSupplier).stream();
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
     * @return The created builder.
	 */
	public static <T>  GridContainer.Builder<T> builder(@NonNull IComponentFactory pComponentFactory, @NonNull Class<T> pBeanType) {
		return new GridContainer.Builder<T>(pComponentFactory, pBeanType);
	}

	/** Returns a description of the current filter status.
	 * @param pColumns The grid columns.
	 * @param pNoFilterText The text, which is being displayed, if no filter is active,
	 *   and 
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
