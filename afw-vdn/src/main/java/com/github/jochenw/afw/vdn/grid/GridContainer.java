package com.github.jochenw.afw.vdn.grid;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import org.jspecify.annotations.NonNull;

import com.github.jochenw.afw.core.function.Functions;
import com.github.jochenw.afw.core.function.Functions.FailableBiConsumer;
import com.github.jochenw.afw.core.function.Functions.FailableSupplier;
import com.github.jochenw.afw.core.function.TriFunction;
import com.github.jochenw.afw.core.log.ILog;
import com.github.jochenw.afw.core.log.ILogFactory;
import com.github.jochenw.afw.core.util.Objects;
import com.github.jochenw.afw.core.util.Objects.DuplicateElementException;
import com.github.jochenw.afw.core.util.Reflection;
import com.github.jochenw.afw.di.api.IComponentFactory;
import com.github.jochenw.afw.di.util.Exceptions;
import com.github.jochenw.afw.vdn.grid.GridContainer.Builder.Column;
import com.github.jochenw.afw.vdn.grid.Grids.IColumn;
import com.github.jochenw.afw.vdn.grid.Grids.IFilterHandler;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.NativeLabel;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.Binder.BindingBuilder;
import com.vaadin.flow.data.binder.Setter;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.binder.ValidationResult;
import com.vaadin.flow.data.binder.Validator;
import com.vaadin.flow.function.ValueProvider;


public class GridContainer<T> extends VerticalLayout {
	/** A builder object for instances of {@link GridContainer}.
	 * @param <T> Bean type of the grid, that is being displayed within the
	 * created {@link GridContainer}.
	 */
	public static class Builder<T> {
		public static class Column<T,V> implements Grids.IColumn<T,V> {
			private final @NonNull Builder<T> builder;
			private final @NonNull String id;
			private final @NonNull Function<T,V> mapper;
			private BiConsumer<T,V> setter;
			private TriFunction<IColumn<T,V>,T,V,String> validator;
			private final @NonNull Class<V> type;
			private Supplier<String> filterValueSupplier;
			private IFilterHandler<V> filterHandler;
			private final String header;

			public Column(@NonNull Builder<T> pBuilder, @NonNull String pId, @NonNull Function<T,V> pMapper,
					      String pHeader, @NonNull Class<V> pType) {
				builder = pBuilder;
				id = pId;
				mapper = pMapper;
				header = pHeader;
				type = pType;
			}
			@Override public @NonNull String getId() { return id; }
			@Override public @NonNull Function<T, V> getMapper() { return mapper; }
			@Override public String getHeader() { return header; }
			@Override public IFilterHandler<V> getFilterHandler() { return filterHandler; }
			@Override public @NonNull Class<V> getValueType() { return type; } 
			public BiConsumer<T,V> getSetter() { return setter; }
			public TriFunction<IColumn<T,V>,T,V,String> getValidator() { return validator; }

			/** Sets the columns {@link IFilterHandler filter handler}. The
			 * filter handler is responsible for handling of sorting, and
			 * filtering, based on column values, in the grid.
			 * @param pFilterHandler The columns filter handler.
			 * @return This column.
			 */
			public Column<T,V> filterHandler(IFilterHandler<V> pFilterHandler) {
				filterHandler = pFilterHandler;
				return this;
			}

			/** Sets the columns setter.
			 * @param pSetter The columns setter.
			 * @return This column.
			 */
			public Column<T,V> setter(BiConsumer<T,V> pSetter) {
				setter = pSetter;
				return this;
			}

			/** Sets the columns validator. The validator takes as input a
			 * {@link IColumn column object}, and the actual column value.
			 * If the column value is valid, the validator must return null.
			 * Otherwise, the validator must return an error message, which
			 * is being displayed with the column value.
			 * @param The column validator.
			 * @return This column.
			 */
			public Column<T,V> validator(TriFunction<IColumn<T,V>,T,V,String> pValidator) {
				validator = pValidator;
				return this;
			}

			@Override
			public String getFilterValue() {
				if (filterValueSupplier == null) {
					return null;
				} else {
					return filterValueSupplier.get();
				}
			}

			/** Terminates configuration of this column, and returns the builder,
			 * for further configuration.
			 * @return The builder, that created this column.
			 */
			public @NonNull Builder<T> end() { return builder; }
		}
		public static class StringColumn<T> extends Column<T,String> {
			private boolean caseSensitive;

			public StringColumn(@NonNull Builder<T> pBuilder, @NonNull String pId, @NonNull Function<T, String> pMapper,
					String pHeader, boolean pCaseSensitive) {
				super(pBuilder, pId, pMapper, pHeader, String.class);
				caseSensitive = pCaseSensitive;
			}

			/** Returns, whether this column is case sensitive. Case sensitivity affects
			 * sorting, and filtering. (Default is case insensitive.)
			 * @return True, if this column is case sensitive. Case
			 * sensitivity affects sorting, and filtering. (Default is case insensitive.)
			 */
			public boolean isCaseSensitive() { return caseSensitive; }
		}
	
		private final @NonNull IComponentFactory componentFactory;
		private final @NonNull Class<T> beanType;
		private final Map<String,Column<T,Object>> columns = new LinkedHashMap<>();
		private Persistor<T,?> persistor;
		private FailableBiConsumer<Persistor<T,?>,T,?> editor;
		private String idPrefix, noFilterText;

		/** Creates a new instance. Typically, one would use
		 * {@link GridContainer#builder(IComponentFactory, Class)}, or
		 * {@link Grids#container(IComponentFactory, Class)}, instead of
		 * using this constructor directly.
		 * 
		 * @param pComponentFactory The component factory, which is being
		 *   used to configure this builder, and the created container.
		 * @param pBeanType Bean type of the grid, that is being displayed within the
	     *   created {@link GridContainer}.
		 */
		protected Builder(@NonNull IComponentFactory pComponentFactory, @NonNull Class<T> pBeanType) {
			componentFactory = Objects.requireNonNull(pComponentFactory, "ComponentFactory");
			beanType = pBeanType;
			componentFactory.init(this);
		}

		/** Sets the detail editor. If an editor is configured, then the following
		 * changes will be done:
		 * <ul>
		 *   <li>A "New" button will be added to the status layout, above the grid.
		 *     Pressing the button will invoke the editor with a null argument.
		 *   </li>
		 *   <li>Double clicking on a grid row will be assumed to be a request to
		 *     update the item, which is being displayed in the row. Technically,
		 *     this will also invoke the editor, with the rows item as an argument.</li>
		 * </ul>
		 * @param pEditor The editor; typically, this is a listener, which opens a detail
		 *   window, in which the item details can be edited.
		 * @return This builder.
		 */
		public Builder<T> editor(FailableBiConsumer<Persistor<T,?>,T,?> pEditor) {
			editor = pEditor;
			return this;
		}

		/** Sets the persistor. If a persistor is present, then the
		 * generated detail viewer will permit editing, and updating the bean.
		 * @param pPersistor The persistor, which is being used.
		 * @return The persistor.
		 */
		public Builder<T> persistor(Persistor<T,?> pPersistor) {
			@SuppressWarnings("unchecked")
			final Persistor<T,Object> p = (Persistor<T,Object>) pPersistor;
			persistor = p;
			return this;
		}

		/** Sets the prefix, which is being used for generated HTML id's.
		 *  The prefix should be unique within all the grid containers of
		 *  a Vaadin application.
		 *  @param pIdPrefix The prefix, which is being used for generated HTML id's.
		 *  The prefix should be unique within all the grid containers of
		 *  a Vaadin application.
		 *  @return This builder.
		 */
		public Builder<T> idPrefix(@NonNull String pIdPrefix) {
			idPrefix = Objects.requireNonNull(pIdPrefix, "IdPrefix");
			return this;
		}

		/** Sets the text, which is being displayed as filter status, if no filters are
		 *   active. A typical example would be "All items".
		 * @param pNoFiltersText The text, which is being displayed as filter status,
		 *   if no filters are active. A typical example would be "All items".
		 * @return This builder.
		 */
		public Builder<T> noFilterText(@NonNull String pNoFilterText) {
			noFilterText = Objects.requireNonNull(pNoFilterText, "NoFilterText");
			return this;
		}

		/** Returns the detail editor. If an editor is configured, then the following
		 * changes will be done:
		 * <ul>
		 *   <li>A "New" button will be added to the status layout, above the grid.
		 *     Pressing the button will invoke the editor with a null argument.
		 *   </li>
		 *   <li>Double clicking on a grid row will be assumed to be a request to
		 *     update the item, which is being displayed in the row. Technically,
		 *     this will also invoke the editor, with the rows item as an argument.</li>
		 * </ul>
		 * @return The editor; typically, this is a listener, which opens a detail
		 *   window, in which the item details can be edited.
		 */
		public FailableBiConsumer<Persistor<T,?>,T,?> getEditor() { return editor; }

		/** Returns the persistor. If a persistor is present, then the
		 * generated detail viewer will permit editing, and updating the bean.
		 * @return The persistor.
		 */
		public <O> Persistor<T,O> getPersistor() {
			@SuppressWarnings("unchecked")
			final Persistor<T,O> p = (Persistor<T,O>) persistor;
			return p;
		}
		
		/** Returns the text, which is being displayed as filter status, if no filters are
		 *   active. A typical example would be "All items".
		 * @return The text, which is being displayed as filter status,
		 *   if no filters are active. A typical example would be "All items".
		 */
		public String getNoFilterText() {
			return noFilterText;
		}

		/** Adds a new column to the {@link Grid}.
		 * @param pColumn The column, which is being added to the {@link Grid}.
		 * @return The column, which has just been added, fot further configuration.
		 */
		public <V> Column<T,V> column(Column<T,V> pColumn) {
			@SuppressWarnings("unchecked")
			final Column<T,Object> col = (Column<T,Object>) Objects.requireNonNull(pColumn, "Column");
			if (columns.put(pColumn.getId(), col) != null) {
				throw new DuplicateElementException("Column id already registered: " + pColumn.getId());
			}
			return pColumn;
		}

		/** Creates a new column, and adds it to the {@link Grid}.
		 * @param pId The column id, must be unique within the {@link Grid}.
		 * @param pMapper A mapper, which extracts the columns value from the
		 *   data bean, that is being displayed in a {@link Grid} row.
		 * @param pHeader The grid columns header, or null, if the column
		 *   is supposed to be invisible. 
		 * @param pType Type of the column values. (Result type of the
		 *   {@code pMapper}.
		 */
		public <V> Column<T,V> column(@NonNull String pId, @NonNull Function<T,V> pMapper,
				                  String pHeader, @NonNull Class<V> pType) {
			return new Column<T,V>(this, pId, pMapper, pHeader, pType);
		}

		/** Creates a new string column, and adds it to the {@link Grid}.
		 * The created column is case insensitive.
		 * @param pId The column id, must be unique within the {@link Grid}.
		 * @param pMapper A mapper, which extracts the columns value from the
		 *   data bean, that is being displayed in a {@link Grid} row.
		 * @param pHeader The grid columns header, or null, if the column
		 *   is supposed to be invisible. 
		 * @param pType Type of the column values. (Result type of the
		 *   {@code pMapper}.
		 */
		public StringColumn<T> stringColumn(@NonNull String pId, @NonNull Function<T,String> pMapper,
                String pHeader) {
			return stringColumn(pId, pMapper, pHeader, false);
		}

		/** Creates a new string column, and adds it to the {@link Grid}.
		 * @param pId The column id, must be unique within the {@link Grid}.
		 * @param pMapper A mapper, which extracts the columns value from the
		 *   data bean, that is being displayed in a {@link Grid} row.
		 * @param pHeader The grid columns header, or null, if the column
		 *   is supposed to be invisible. 
		 * @param pType Type of the column values. (Result type of the
		 *   {@code pMapper}.
		 * @param pCaseSensitive True, if the created column should be
		 *   case sensitive.
		 */
		public StringColumn<T> stringColumn(@NonNull String pId, @NonNull Function<T,String> pMapper,
				                      String pHeader, boolean pCaseSensitive) {
			final StringColumn<T> col = new StringColumn<T>(this, pId, pMapper, pHeader, pCaseSensitive);
			col.filterHandler(Grids.stringFilterHandler(pCaseSensitive));
			column(col);
			return col;
		}

		/** Creates a  new {@link GridContainer}, which is an instance
		 * of the given {@code pType}.
		 */
		public <GC extends GridContainer<T>> GC build(Class<GC> pType) {
			final Constructor<GC> constructor;
			try {
				constructor = pType.getConstructor();
			} catch (Throwable t) {
				throw Exceptions.show(t);
			}
			final GC gc;
			try {
				gc = constructor.newInstance();
			} catch (Throwable t) {
				throw Exceptions.show(t);
			}
			gc.configure(this);
			gc.init();
			return gc;
		}

		/** Returns the bean type, which is the class of the item type {@code &lt;T&gt;}.
		 * @return The bean type.
		 */
		public Class<T> getBeanType() { return beanType; }
	}

	private static final long serialVersionUID = 4989299971825455391L;
	/** The component factory.
	 */
	private IComponentFactory componentFactory;
	/** The logger.
	 */
	private ILog log;
	/** Class of the beans, which are being displayed in the grid.
	 */
	private Class<T> beanType;
	/** A map with the filter values, by column id.
	 */
	private final Map<String,String> filterValueMap = new HashMap<>();
	/** The map of grid columns, by column id.
	 */
	private Map<String,Builder.Column<T,Object>> columns;
	/** The persistor; basically the liaison with persistent
	 * storage.
	 */
	private Persistor<T,Object> persistor;

	/** The layout, which contains the filter fields.
	 */
	private HorizontalLayout filtersLayout;
	/** The component, which contains the filters layout,
	 * including the filters layout. Typically,  this
	 * would be a vertical layout.
	 */
	private Component filtersContainer;
	/** The component, which displays the grids filter status,
	 * typically a read-only text field, or something the like.
	 */
	private Component statusField;
	/** The component, which includes the status field,
	 * typically a horizontal layout.
	 */
	private Component statusContainer;
	/** The actual grid.
	 */
	private Grid<T> grid;
	/** The text, which is being displayed, if no filters
	 * are active, and the grid displays all items.
	 */
	private String noFilterText;
	/** The prefix, which is used for generated HTML id's.
	 * The purpose of the prefix is to ensure uniqueness
	 * over all grids in the application.
	 */
	private String idPrefix;
	/** The editor is a consumer, which is being invoked
	 * as a detail viewer/editor for the bean, that it
	 * receives. Typically, this will open a dialog,
	 * which displays the bean details.
	 */
	private FailableBiConsumer<Persistor<T,?>,T,?> editor;

	public IComponentFactory getComponentFactory() { return componentFactory; }
	public Class<T> getBeanType() { return beanType; }
	public Map<String, String> getFilterValueMap() { return filterValueMap; }
	public Map<String, Builder.Column<T, Object>> getColumns() { return columns; }
	public Grid<T> getGrid() { return grid; }
	public String getNoFilterText() { return noFilterText; }
	public String getIdPrefix() { return idPrefix; }
	public FailableBiConsumer<Persistor<T,?>,T,?> getEditor() { return editor; }
	public <O> Persistor<T,O> getPersistor() {
		@SuppressWarnings("unchecked")
		final Persistor<T,O> p = (Persistor<T,O>) persistor;
		return p;
	}

	/** Initializes this {@link GridContainer} by applying the
	 * configuration, that has been set via
	 * {@link #configure(Builder)}.
	 */
	protected void init() {
		log.entering("init: ->");
		filtersLayout = newFiltersLayout();
		filtersContainer = newFiltersContainer(filtersLayout);
		final Component sf = statusField = newStatusField();
		statusContainer = newStatusComponent(sf);
		grid = newGrid();
		filtersChanged();
		add(filtersContainer, statusContainer, grid);
		log.exiting("init: <-");
	}

	/** Configures the grid container by copying the settings
	 * from the given builder. Initialization of the grid container
	 * is left to a call of {@link #init()}, which is taking
	 * place thereafter.
	 * @param pBuilder The builder, which is creating this
	 *   grid container.
	 */
	protected void configure(Builder<T> pBuilder) {
		componentFactory = pBuilder.componentFactory;
		log = componentFactory.requireInstance(ILogFactory.class).getLog(GridContainer.class);
		beanType = pBuilder.beanType;
		columns = pBuilder.columns;
		noFilterText = pBuilder.noFilterText;
		idPrefix = pBuilder.idPrefix;
		editor = pBuilder.getEditor();
		persistor = pBuilder.getPersistor();
	}

	/** Creates a component, which contains the status field.
	 * The purpose of the component is making the status field
	 * visible.
	 */
	protected Component newStatusComponent(Component pStatusField) {
		final HorizontalLayout hl = new HorizontalLayout();
		hl.setJustifyContentMode(JustifyContentMode.CENTER);
		hl.setAlignItems(Alignment.CENTER);
		hl.setWidthFull();
		hl.add(pStatusField);
		if (editor == null) {
			return hl;
		} else {
			final Button newButton = newNewButton();
			final HorizontalLayout outerHl = new HorizontalLayout();
			outerHl.setAlignItems(Alignment.CENTER);
			outerHl.setJustifyContentMode(JustifyContentMode.BETWEEN);
			outerHl.setWidthFull();
			outerHl.add(hl, newButton);
			return outerHl;
		}
	}

	/** Creates a "New" button, which uses the given
	 * {@link Persistor} to create a new instance,
	 * which will then be edited in the editor.
	 * @param pPersistor The persistor, which is
	 * being used by the detail editor to create
	 * a new, blank instance.
	 * @return The created button,
	 */
	protected Button newNewButton() {
		final Persistor<T,Object> persistor = getPersistor();
		final Button button = new Button("New");
		button.addClickListener((e) -> {
			final T bean = persistor.newBean();
			if (persistor.getId(bean) != null) {
				throw new IllegalStateException("The persistor has created a bean with a non-null id.");
			}
			final Component editor = newEditor(bean);
			final Dialog dialog = new Dialog(editor);
			dialog.setModal(true);
			dialog.setCloseOnEsc(true);
			dialog.open();
		});
		return button;
	}

	/** Generates a detail viewer/editor for the given bean.
	 * @param pBean The bean, which is being displayed/edited.
	 * If the bean's id is null, then the created editor
	 * whould be in read/write mode immediately.
	 * Otherwise, the editor may be initially in read-only
	 * mode.
	 * @param pStorer A callback, which may be invoked to
	 *   persist the bean. If the callback is null, then the
	 *   created editor is supposed to be in read-only
	 *   mode, and provide no means of entering read/write
	 *   mode.
	 * @return The created component, which is being displayed
	 *   in a modal dialog.
	 */
	protected Component newEditor(T pBean) {
		final Binder<T> binder = new Binder<>(getBeanType());
		final VerticalLayout vl = new VerticalLayout();
		final List<Consumer<Boolean>> readOnlyConsumers = new ArrayList<>();
		final FormLayout fl = new FormLayout();
		columns.values().forEach((col) -> {
			final TextField tf = newAttributeEditor(binder, pBean, col);
			fl.add(tf);
			if (col.getSetter() == null) {
				tf.setReadOnly(true);
			} else {
				final Consumer<Boolean> readOnlyConsumer = (b) -> {
					tf.setReadOnly(b.booleanValue());
				};
				readOnlyConsumers.add(readOnlyConsumer);
			}
		});
		final Persistor<T,?> persistor = getPersistor();
		if (!readOnlyConsumers.isEmpty()) {
			final boolean editable = !persistor.isReadOnly();
			final Boolean initialState;
			if (editable) {
				initialState = Boolean.valueOf(persistor.getId(pBean) != null);
			} else {
				initialState = Boolean.TRUE;
			}
			readOnlyConsumers.forEach((cns) -> cns.accept(initialState));
			if (editable && initialState.booleanValue()) {
				// Create a button, which enables making the text fields editable.
				final Button editButton = new Button("Edit");
				editButton.addClickListener((e) -> {
					readOnlyConsumers.forEach((cns) -> {
						cns.accept(Boolean.FALSE);
					});
					editButton.setEnabled(false);
				});
				final HorizontalLayout hl = new HorizontalLayout();
				hl.setJustifyContentMode(JustifyContentMode.END);
				hl.setWidthFull();
				hl.add(editButton);
				vl.add(hl);
			}
		}
		final Button saveButton = new Button("Save");
		saveButton.addClickListener((e) -> {
			try {
				binder.writeChangedBindingsToBean(pBean);
			} catch (ValidationException ve) {
				/* Validation failed, do nothing.
				 * (Dialog remains open, with error messages
				 * being displayed, so the 
				 */
				return;
			}
			if (persistor.getId(pBean) == null) {
				persistor.insert(pBean);
			} else {
				persistor.update(pBean);
			}
		});
		final HorizontalLayout hl = new HorizontalLayout();
		hl.setJustifyContentMode(JustifyContentMode.END);
		hl.setWidthFull();
		hl.add(saveButton);
		vl.add(hl);
		return vl;
	}

	protected TextField newAttributeEditor(Binder<T> pBinder, T pBean, Column<T,Object> pColumn) {
		final Class<?> stringClass = String.class;
		final Class<?> colClass = pColumn.getValueType();
		if (stringClass != colClass) {
			throw new IllegalStateException("Invalid column type: " + colClass.getName());
		}
		final TextField tf = new TextField();
		BindingBuilder<T,String> bb = pBinder.forField(tf);
		final Column<T,String> strCol = Reflection.cast(pColumn);
		final IColumn<T,String> stringCol = strCol;
		TriFunction<IColumn<T,String>, T, String, String> validator = strCol.getValidator();
		if (validator != null) {
			final Validator<String> vaadinValidator = (s, vc) -> {
				final String msg = validator.apply(stringCol, pBean, s);
				if (msg == null) {
					return ValidationResult.ok();
				} else {
					return ValidationResult.error(msg);
				}
			};
			bb = bb.withValidator(vaadinValidator);
		}
		final Function<T,String> getter = strCol.getMapper();
		final BiConsumer<T,String> setter = strCol.getSetter();
		final ValueProvider<T,String> vp = (t) -> getter.apply(t);
		if (setter == null) {
			bb.bindReadOnly(vp);
		} else {
			final Setter<T,String> vdnSetter = (t,s) -> setter.accept(t,  s);
			bb.bind(vp, vdnSetter);
		}
		return tf;
	}

	/** Creates the grid, which is being displayed by the container.
	 */
	protected Grid<T> newGrid() {
		final Grid<T> grid = new Grid<>();
		for (Column<T,Object> col : columns.values()) {
			@NonNull
			Function<T, Object> mapper = col.getMapper();
			final IFilterHandler<Object> fh = col.getFilterHandler();
			final Grid.Column<T> gCol = grid.addColumn((t) -> mapper.apply(t)).setHeader(col.getHeader())
					.setSortable(fh != null);
			gCol.setId(col.getId());
		}
		if (editor != null) {
			grid.addItemClickListener((e) -> {
				final T item = e.getItem();
				if (item != null) {
					Functions.accept(editor, getPersistor(), item);
				}
			});
		}
		final Map<String,IColumn<T,?>> columnMap = new HashMap<>();
		columns.forEach((id, col) -> columnMap.put(id, col));
		final FailableSupplier<Collection<T>,?> valuesSupplier = () -> getPersistor().getAllItems();
		grid.setDataProvider(Grids.getDataProvider(valuesSupplier, columnMap));
		return grid;
	}
	
	/** Creates a new status field. The purpose of the status field is
	 * the ability to display the current
	 * {@link Grids#getFilterDiscription(Collection, String)}.
	 * If you override this method, you should consider overriding
	 * {@link #setFilterStatus(Component, String)}, too.
	 * @see #setFilterStatus(Component, String).
	 * @return The created status field.
	 */
	protected Component newStatusField() {
		final TextField tf = new TextField();
		tf.setReadOnly(true);
		tf.setVisible(true);
		tf.setAriaLabel("Filter status");
		tf.setLabel("Filter status");
		return tf;
	}

	/** Sets the text, which is being displayed by the status field.
	 * @param pStatusField The status field, which has been created
	 *   by {@link #newStatusField()}.
	 * @see #newStatusField()
	 */
	protected void setFilterStatus(Component pStatusField, String pText) {
		((TextField) pStatusField).setValue(pText);
	}

	/** Generates the current filter status, and updates the
	 * status field by invoking {@link #setFilterStatus(Component,String)}.
	 */
	protected void setFilterStatus() {
		final String filterDescription = Grids.getFilterDescription(columns.values(), noFilterText);
		setFilterStatus(statusField, filterDescription);
	}

	/**
	 * Creates a new {@link HorizontalLayout}, which contains all the
	 * filter fields.
	 * @see GridContainer.Builder#idPrefix(String)
	 * @return The created layout.
	 */
	protected HorizontalLayout newFiltersLayout() {
		final HorizontalLayout hl = new HorizontalLayout();
		hl.setJustifyContentMode(JustifyContentMode.BETWEEN);
		hl.setWidthFull();
		columns.forEach((id, col) -> {
			final IFilterHandler<Object> fh = col.getFilterHandler();
			if (fh != null) {
				final Component[] colFilterComponents = newFilterField(col, idPrefix);
				hl.add(colFilterComponents);
			}
		});
		return hl;
	}

	/** Creates a new {@link NativeLabel label}, and a new
	 * {@link TextField filter text field},
	 * as a means to enter a filter value for the given column.
	 * @param pCol The column, for which a filter field is
	 *   being created.
	 * @param pIdPrefix The grid's id prefix. This must be unique among
	 *   all grids in the application.
	 * @see GridContainer.Builder#idPrefix(String)
	 * @return An array, which contains the created label, and the
	 *   given filter text field, suitable for being added to the
	 *   {@link #newFilterLayout() filters layout}.
	 */
	protected Component[] newFilterField(Column<T,Object> pColumn, String pIdPrefix) {
		final TextField tf = new TextField();
		tf.setAriaLabel(pColumn.getHeader());
		tf.setLabel(pColumn.getHeader());
		tf.addValueChangeListener((e) -> {
			this.filterValueMap.put(pColumn.getId(), e.getValue());
			filtersChanged();
		});
		return new Component[] {tf};
	}

	/** Called, if a filter value has changed. Regenerates the filter
	 * status, and refreshes the grid contents.
	 */
	protected void filtersChanged() {
		setFilterStatus();
		if (grid != null) {
			grid.getDataProvider().refreshAll();
		}
	}

	/** Generates a new HTML id for the filter field of the given column.
	 * @param pColumn The column, for which a filter field is being
	 * generated.
	 * @param pIdPrefix The grid's id prefix. This must be unique among
	 *   all grids in the application.
	 * @see GridContainer.Builder#idPrefix(String)
	 */
	protected String newFilterFieldId(Column<T,Object> pColumn, String pIdPrefix) {
		String prefix = Objects.notNull(pIdPrefix, "");
		while (prefix.endsWith(".")) {
			prefix = prefix.substring(0,  prefix.length()-1);
		}
		String suffix = pColumn.getId();
		while (suffix.startsWith(".")) {
			suffix = suffix.substring(1);
		}
		if (prefix.length() > 0) {
			return prefix + "." + suffix;
		} else {
			return suffix;
		}
	}

	/** Creates a new component, which contains the given filters layout,
	 * and a title.
	 * @param pFiltersLayout The horizontal layout, which has been created
	 *   by a previous invocation of {@link #newFiltersLayout(String)}.
	 * @return The created component, which includes the given filters
	 *   layout.
	 */
	protected Component newFiltersContainer(HorizontalLayout pFiltersLayout) {
		final VerticalLayout vl = new VerticalLayout();
		final H3 h3 = new H3("Filters");
		vl.add(h3, pFiltersLayout);
		return vl;
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
}
