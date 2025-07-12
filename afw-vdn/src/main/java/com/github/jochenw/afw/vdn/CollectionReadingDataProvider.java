package com.github.jochenw.afw.vdn;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import com.github.jochenw.afw.core.vdn.Filters;
import com.vaadin.flow.data.provider.CallbackDataProvider.CountCallback;
import com.vaadin.flow.data.provider.CallbackDataProvider.FetchCallback;
import com.vaadin.flow.data.provider.ConfigurableFilterDataProviderWrapper;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.QuerySortOrder;


public class CollectionReadingDataProvider<T,F> extends ConfigurableFilterDataProviderWrapper<T,Predicate<T>,F,Predicate<T>> {
	private static final long serialVersionUID = -365437585313982461L;
	private final Function<F,Predicate<T>> filterCreator;

	public CollectionReadingDataProvider(DataProvider<T, Predicate<T>> pDataProvider,
			                             Function<F,Predicate<T>> pFilterCreator) {
		super(pDataProvider);
		filterCreator = pFilterCreator;
	}
	
	public static <T,F> CollectionReadingDataProvider<T,F> of(Supplier<Iterable<T>> pSupplier,
			                                                  Function<F,Predicate<T>> pFilterCreator,
			                                                  Function<List<QuerySortOrder>,Comparator<T>> pComparatorCreator) {
		final FetchCallback<T,Predicate<T>> fcb = (q) -> {
			final Optional<Predicate<T>> optionalPredicate = q.getFilter();
			final Predicate<T> predicate = optionalPredicate.isPresent() ? optionalPredicate.get() : null;
			final List<T> list = new ArrayList<>();
			for (Iterator<T> iter = pSupplier.get().iterator();  iter.hasNext();  ) {
				final T t = iter.next();
				if (predicate == null  ||  predicate.test(t)) {
					list.add(t);
				}
			}
			final List<QuerySortOrder> qsoList = q.getSortOrders();
			if (qsoList != null  &&  !qsoList.isEmpty()) {
				final Comparator<T> comparator = pComparatorCreator.apply(qsoList);
				if (comparator != null) {
					list.sort(comparator);
				}
			}
			final int offset = q.getOffset();
			final int limit = q.getLimit();
			if (offset > 0  ||  limit > 0) {
				return list.stream().filter(Filters.limit(offset, limit));
			} else {
				return list.stream();
			}
		};
		final CountCallback<T,Predicate<T>> ccb = (q) -> {
			final Optional<Predicate<T>> optionalPredicate = q.getFilter();
			final Predicate<T> predicate = optionalPredicate.isPresent() ? optionalPredicate.get() : null;
			int i = 0;
			for (Iterator<T> iter = pSupplier.get().iterator();  iter.hasNext();  ) {
				final T t = iter.next();
				if (predicate == null  ||  predicate.test(t)) {
					++i;
				}
			}
			return i;
		};
		final DataProvider<T,Predicate<T>> dp = DataProvider.fromFilteringCallbacks(fcb,ccb);
		return new CollectionReadingDataProvider<>(dp, pFilterCreator);
	}



	@Override
	protected Predicate<T> combineFilters(Predicate<T> pQueryFilter, F pConfiguredFilter) {
		final Predicate<T> predicate = filterCreator.apply(pConfiguredFilter);
		if (predicate == null) {
			return pQueryFilter;
		} else {
			if (pQueryFilter == null) {
				return predicate;
			} else {
				return predicate.and(pQueryFilter);
			}
		}
	}
}
