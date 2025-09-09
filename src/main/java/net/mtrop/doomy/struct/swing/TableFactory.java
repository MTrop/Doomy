/*******************************************************************************
 * Copyright (c) 2025 Black Rook Software
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doomy.struct.swing;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.function.Predicate;

import javax.swing.DefaultListSelectionModel;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

/**
 * 
 */
public final class TableFactory 
{
	private TableFactory() {}

	/* ==================================================================== */
	/* ==== Tables                                                     ==== */
	/* ==================================================================== */
	
	/**
	 * Creates a new table model.
	 * @param <T> the object type that the model contains.
	 * @param objectType the object type.
	 * @param objects the objects in the model.
	 * @param listener the listener for model changes.
	 * @return a new model.
	 */
	public static <T> JObjectTableModel<T> objectTableModel(Class<T> objectType, Collection<T> objects, JObjectTableModelListener<T> listener)
	{
		JObjectTableModel<T> model = new JObjectTableModel<>(objectType, objects);
		model.addTableModelListener(listener);
		return model;
	}
	
	/**
	 * Creates a new table model.
	 * @param <T> the object type that the model contains.
	 * @param objectType the object type.
	 * @param objects the objects in the model.
	 * @return a new model.
	 */
	public static <T> JObjectTableModel<T> objectTableModel(Class<T> objectType, Collection<T> objects)
	{
		return new JObjectTableModel<>(objectType, objects);
	}

	/**
	 * Creates a new table model.
	 * @param <T> the object type that the model contains.
	 * @param objectType the object type.
	 * @return a new model.
	 */
	public static <T> JObjectTableModel<T> objectTableModel(Class<T> objectType)
	{
		return new JObjectTableModel<T>(objectType, Collections.emptySet());
	}
	
	/**
	 * Creates a new table.
	 * @param <T> the object type that the table contains.
	 * @param policy the selection policy.
	 * @param model the object model.
	 * @param listener the selection listener.
	 * @return a new table.
	 */
	public static <T> JObjectTable<T> objectTable(SelectionPolicy policy, JObjectTableModel<T> model, JObjectTableSelectionListener<T> listener)
	{
		JObjectTable<T> table = new JObjectTable<>(model, policy);
		table.getSelectionModel().addListSelectionListener(listener);
		return table;
	}
	
	/**
	 * Creates a new table.
	 * @param <T> the object type that the table contains.
	 * @param policy the selection policy.
	 * @param model the object model.
	 * @return a new table.
	 */
	public static <T> JObjectTable<T> objectTable(SelectionPolicy policy, JObjectTableModel<T> model)
	{
		return new JObjectTable<>(model, policy);
	}
	
	/**
	 * An annotation that describes how a field is used in JObjectTables.
	 * Must be attached to "getters" or public fields.
	 */
	@Target({ElementType.METHOD, ElementType.FIELD})
	@Retention(RetentionPolicy.RUNTIME)
	public @interface Column
	{
		/** Nice name of column. If not specified (or is empty string), uses field name or getter name minus "get". */
		String name() default "";
		
		/** Hidden field. If true, do not consider for display. */
		boolean hidden() default false;
		
		/** Order index. Lower values are placed first in the table row. Larger values later. */
		int order() default 0;
		
		/** Column tool tip text. If not specified (or is empty string), no tip is shown. */
		String tip() default ""; 
		
		/** Sortable? If true, column is sortable in the table. */
		boolean sortable() default true;
		
		/** Editable? If false, disable editing. If true, edit if setter exists. */
		boolean editable() default true;
		
	}
	
	/**
	 * Selection policy.
	 */
	public static enum SelectionPolicy
	{
		SINGLE(ListSelectionModel.SINGLE_SELECTION),
		SINGLE_INTERVAL(ListSelectionModel.SINGLE_INTERVAL_SELECTION),
		MULTIPLE_INTERVAL(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		
		private final int intern;
		private SelectionPolicy(int intern)
		{
			this.intern = intern;
		}
	}
	
	/* ==================================================================== */
	/* ==== Classes                                                    ==== */
	/* ==================================================================== */

	/**
	 * A JTable that stores objects in whole.
	 * @param <T> the object type.
	 */
	public static class JObjectTable<T> extends JTable
	{
		private static final long serialVersionUID = 5074576660391248661L;
		
		private JObjectTable(JObjectTableModel<T> model, SelectionPolicy policy)
		{
			super(model);
			setSelectionMode(policy.intern);
			setRowSorter(new JObjectTableRowSorter<T>(model));
		}
		
		/** 
		 * Sets the selected row in the table.
		 * @param index the index to select in the table.
		 */
		public void setSelectedRow(int index)
		{
			clearSelection();
			setRowSelectionInterval(index, index);
		}

		/** 
		 * Sets the selected rows in the table.
		 * @param index the indices to select in the table.
		 */
		public void setSelectedRows(int ... index)
		{
			Arrays.sort(index);
			int start = -1;
			int end = -1;
			for (int i = 0; i < index.length; i++)
			{
				if (start < 0)
				{
					start = index[i];
					end = index[i];
				}
				else if (index[i] > end + 1)
				{
					setRowSelectionInterval(start, end);
					start = -1;
					end = -1;
				}
				else
				{
					index[i] = end;
				}
				
				if (i == index.length - 1 && start >= 0)
					setRowSelectionInterval(start, end);
			}
				
		}

		/**
		 * Sets a renderer for item cells in the table for a particular column.
		 * See TableColumn.setCellRenderer().
		 * @param columnIndex the column index to set the renderer for.
		 * @param renderer the renderer to set.
		 * @return itself, for chaining.
		 */
		public JObjectTable<T> setColumnRenderer(int columnIndex, TableCellRenderer renderer)
		{
			TableColumn col = getColumnModel().getColumn(columnIndex);
			if (col != null)
				col.setCellRenderer(renderer);
			return this;
		}

		/**
		 * Sets an editor for item cells in the table for a particular column.
		 * See TableColumn.setCellEditor().
		 * @param columnIndex the column index to set the editor for.
		 * @param editor the editor to set.
		 * @return itself, for chaining.
		 */
		public JObjectTable<T> setColumnEditor(int columnIndex, TableCellEditor editor)
		{
			TableColumn col = getColumnModel().getColumn(columnIndex);
			if (col != null)
				col.setCellEditor(editor);
			return this;
		}

		/**
		 * Sets a filter for rows in the table.
		 * @param filter the filter for the rows.
		 * @return itself, for chaining.
		 */
		@SuppressWarnings("unchecked")
		public JObjectTable<T> setRowFilter(final Predicate<T> filter)
		{
			((TableRowSorter<JObjectTableModel<T>>)getRowSorter()).setRowFilter(new RowFilter<JObjectTableModel<T>, Integer>()
				{
					@Override
					public boolean include(Entry<? extends JObjectTableModel<T>, ? extends Integer> entry) 
					{
						return filter.test(entry.getModel().getRow(entry.getIdentifier()));
					}
				}
			);
			return this;
		}

		/**
		 * Gets the list of selected list indices, translated to the model.
		 * @return the list of model-converted indices.
		 */
		public int[] getModelConvertedSelectedRows()
		{
			int[] indices = getSelectedRows();
			int[] out = new int[indices.length];
			for (int i = 0; i < indices.length; i++)
				out[i] = convertRowIndexToModel(indices[i]);
			return out;
		}
		
		/**
		 * Gets the list of selected objects.
		 * The indices from {@link #getSelectedRows()} are converted to the model.
		 * @return the list of selected objects in the table.
		 * @see #convertRowIndexToModel(int)
		 */
		@SuppressWarnings("unchecked")
		public List<T> getSelectedObjects()
		{
			int[] indices = getSelectedRows();
			ArrayList<T> outList = new ArrayList<>(indices.length);
			for (int i = 0; i < indices.length; i++)
			{
				JObjectTableModel<T> model = (JObjectTableModel<T>)getModel();
				outList.add(model.getRow(convertRowIndexToModel(indices[i])));
			}
			
			return Collections.unmodifiableList(outList);
		}
		
		/**
		 * Gets the list of selected objects.
		 * The indices from {@link #getSelectedRows()} are converted to the model.
		 * @return the list of selected objects in the table.
		 * @see #convertRowIndexToModel(int)
		 */
		public T getSelectedObject()
		{
			if (getSelectedRow() == -1)
				return null;
			return getSelectedObjects().get(0);
		}
		
		/**
		 * @return the {@link JObjectTableModel} that backs this table.
		 */
		@SuppressWarnings("unchecked")
		public JObjectTableModel<T> getTableModel()
		{
			return (JObjectTableModel<T>)getModel();
		}
		
	}
	
	/** 
	 * Row sorter implementation. 
	 * @param <T> the stored class type.
	 */
	public static class JObjectTableRowSorter<T> extends TableRowSorter<JObjectTableModel<T>>
	{
		/** 
		 * Comparator for Enumerations. 
		 */
		@SuppressWarnings("rawtypes")
		public static final Comparator<Enum> ENUM_COMPARATOR = (obj1, obj2) ->
		{
			if (obj1 == obj2)
				return 0;
			else if (obj1 == null && obj2 != null)
				return -1;
			else if (obj1 != null && obj2 == null)
				return 1;
			else if (obj1.equals(obj2))
				return 0;
			else
				return obj1.ordinal() - obj2.ordinal();
		};
		
		/** 
		 * Comparator for Booleans. 
		 */
		public static final Comparator<Boolean> BOOLEAN_COMPARATOR = (obj1, obj2) ->
		{
			if (obj1 == obj2)
				return 0;
			else if (obj1 == null && obj2 != null)
				return -1;
			else if (obj1 != null && obj2 == null)
				return 1;
			else if (obj1.equals(obj2))
				return 0;
			else
				return obj1.compareTo(obj2);
		};
		
		/** 
		 * Comparator for Numbers. 
		 */
		public static final Comparator<Number> NUMBER_COMPARATOR = (obj1, obj2) ->
		{
			if (obj1 == obj2)
				return 0;
			else if (obj1 == null && obj2 != null)
				return -1;
			else if (obj1 != null && obj2 == null)
				return 1;
			else if (obj1.equals(obj2))
				return 0;
			else
				return obj1.doubleValue() > obj2.doubleValue() ? 1 : -1;
		};
		
		/** 
		 * Comparator for Dates. 
		 */
		public static final Comparator<Date> DATE_COMPARATOR = (obj1, obj2) ->
		{
			if (obj1 == obj2)
				return 0;
			else if (obj1 == null && obj2 != null)
				return -1;
			else if (obj1 != null && obj2 == null)
				return 1;
			else if (obj1.equals(obj2))
				return 0;
			else
				return obj1.getTime() > obj2.getTime() ? 1 : -1;
		};

		/** Class comparator map. */
		private HashMap<Class<?>, Comparator<?>> classComparatorMap; 	
		/** Column comparator map. */
		private HashMap<Integer, Comparator<?>> columnComparatorMap;
		
		/**
		 * Creates a row sorter from a table model.
		 * @param model the model to use.
		 */
		public JObjectTableRowSorter(JObjectTableModel<T> model)
		{
			super(model);
			columnComparatorMap = new HashMap<Integer, Comparator<?>>();
			classComparatorMap = new HashMap<Class<?>, Comparator<?>>();
			setClassComparator(Enum.class, ENUM_COMPARATOR);
			setClassComparator(Boolean.class, BOOLEAN_COMPARATOR);
			setClassComparator(Number.class, NUMBER_COMPARATOR);
			setClassComparator(Date.class, DATE_COMPARATOR);
		}
		
		@Override
		public boolean isSortable(int column)
		{
			ColumnDescriptor col = getModel().getColumns()[column];
			if (col != null)
				return col.sortable;
			return false;
		}
		
		@Override
		public Comparator<?> getComparator(int column)
		{
			if (columnComparatorMap.containsKey(column))
				return columnComparatorMap.get(column);
			
			Class<?> clazz = getModel().getColumnClass(column);
			Comparator<?> out = null;
			while (out == null && clazz != null)
			{
				out = classComparatorMap.get(clazz);
				if (out == null)
					clazz = clazz.getSuperclass();
			}
			return out;
		}

		/**
		 * Sets a comparator to use when sorting a column.
		 * <p>These comparators are resolved by the column's primary class first,
		 * and then its hierarchy is recursively searched if it is not found.
		 * @param <E> the item type.
		 * @param clazz the class to assign a comparator to.
		 * @param comparator the comparator.
		 */
		public <E> void setClassComparator(Class<E> clazz, Comparator<E> comparator)
		{
			classComparatorMap.put(clazz, comparator);
		}

		/**
		 * Sets a comparator to use when sorting a <i>specific</i> column.
		 * <p>These comparators are resolved FIRST, before the class comparator is.
		 * @param columnIndex the column index to assign a comparator to.
		 * @param comparator the comparator.
		 */
		public void setColumnComparator(int columnIndex, Comparator<?> comparator)
		{
			columnComparatorMap.put(columnIndex, comparator);
		}

	}

	/**
	 * A generated table model.
	 * @param <T> the object type that the table contains.
	 */
	public static class JObjectTableModel<T> implements TableModel
	{
		private ArrayList<T> objects;
		private ColumnDescriptor[] columns;
		private ArrayList<TableModelListener> listeners;
		
		private JObjectTableModel(Class<T> objectClass, Collection<T> objects)
		{
			this.objects = new ArrayList<>(objects);
			this.listeners = new ArrayList<>();
			
			List<ColumnDescriptor> columnList = new ArrayList<>();
			for (Field field : objectClass.getFields())
			{
				String fieldName = field.getName();
				
				TableFactory.Column td = field.getAnnotation(TableFactory.Column.class);
				if (td == null || td.hidden())
					continue;
				ColumnDescriptor col = new ColumnDescriptor();
				col.dataType = upgradePrimitiveType(field.getType());
				col.publicField = field;
				String tname = td.name().trim();
				col.name = tname.length() > 0 ? tname : fieldName;
				col.order = td.order();
				col.sortable = td.sortable();
				col.editable = td.editable();
				columnList.add(col);
			}
			
			for (Method method : objectClass.getMethods())
			{
				if (!isGetter(method))
					continue;

				String fieldName = getFieldName(method.getName());
				
				TableFactory.Column td = method.getAnnotation(TableFactory.Column.class);
				if (td == null || td.hidden())
					continue;
				ColumnDescriptor col = new ColumnDescriptor();
				col.dataType = upgradePrimitiveType(method.getReturnType());
				col.getterMethod = method;
				try {
					col.setterMethod = objectClass.getMethod(getSetterName(fieldName), method.getReturnType());
				} catch (NoSuchMethodException ex) {
					col.setterMethod = null;
				}
				String tname = td.name().trim();
				col.name = tname.length() > 0 ? tname : fieldName;
				col.order = td.order();
				col.sortable = td.sortable();
				col.editable = td.editable() && col.setterMethod != null;
				columnList.add(col);
			}
			
			columnList.sort((c1, c2) -> c1.order - c2.order);
			
			this.columns = columnList.toArray(new ColumnDescriptor[columnList.size()]);
		}
		
		private ColumnDescriptor[] getColumns()
		{
			return columns;
		}
		
		@Override
		public int getRowCount()
		{
			return objects.size();
		}

		@Override
		public int getColumnCount() 
		{
			return columns.length;
		}

		@Override
		public String getColumnName(int columnIndex)
		{
			return columns[columnIndex].name;
		}

		@Override
		public Class<?> getColumnClass(int columnIndex) 
		{
			return columns[columnIndex].dataType;
		}

		@Override
		public boolean isCellEditable(int rowIndex, int columnIndex)
		{
			return columns[columnIndex].editable;
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex)
		{
			ColumnDescriptor col = columns[columnIndex];
			T instance = objects.get(rowIndex);
			
			if (col.publicField != null)
				return getFieldValue(instance, col.publicField);
			else if (col.getterMethod != null)
				return invokeBlind(col.getterMethod, instance); 
			else
				return null;
		}

		@Override
		public void setValueAt(Object aValue, int rowIndex, int columnIndex)
		{
			ColumnDescriptor col = columns[columnIndex];
			T instance = objects.get(rowIndex);
			
			if (col.publicField != null)
				setFieldValue(instance, col.publicField, aValue);
			else if (col.setterMethod != null)
				invokeBlind(col.setterMethod, instance, aValue);
			fireUpdateEvent(rowIndex, columnIndex);
		}
		
		@Override
		public void addTableModelListener(TableModelListener listener)
		{
			synchronized (listeners)
			{
				listeners.add(listener);
			}
		}

		@Override
		public void removeTableModelListener(TableModelListener listener) 
		{
			synchronized (listeners)
			{
				listeners.remove(listener);
			}
		}
		
		/**
		 * Sets all the rows in this model to the provided collection of rows.
		 * @param collection the collection.
		 */
		public void setRows(Collection<T> collection)
		{
			objects.clear();
			addAllRowsAt(collection, 0);
		}
		
		/**
		 * Adds a row to the end of this table.
		 * @param row the row to add.
		 */
		public void addRow(T row)
		{
			addRowAt(row, objects.size());
		}
		
		/**
		 * Adds a row to this table.
		 * @param row the row to add.
		 * @param index the destination index.
		 */
		public void addRowAt(T row, int index)
		{
			objects.add(index, row);
			fireInsertEvent(index, index);
		}
		
		/**
		 * Adds a series of rows to the end of this table.
		 * @param collection the collection of items.
		 */
		public void addAllRows(Collection<T> collection)
		{
			addAllRowsAt(collection, objects.size());
		}
		
		/**
		 * Adds a series of rows to this table.
		 * @param collection the collection of items.
		 * @param index the destination index.
		 */
		public void addAllRowsAt(Collection<T> collection, int index)
		{
			objects.addAll(index, collection);
			fireInsertEvent(index, collection.size() + index - 1);
		}
		
		/**
		 * Removes a row from this table.
		 * @param row the row to add.
		 */
		public void removeRow(T row)
		{
			int index = objects.indexOf(row);
			removeRowAt(index);
		}
		
		/**
		 * Removes a row from this table.
		 * @param index the row index.
		 */
		public void removeRowAt(int index)
		{
			objects.remove(index);
			fireDeleteEvent(index, index);
		}
		
		/**
		 * Retrieves a row from this model.
		 * @param index the index.
		 * @return the corresponding object.
		 */
		public T getRow(int index)
		{
			return objects.get(index);
		}
		
		protected void fireInsertEvent(int minRow, int maxRow)
		{
			TableModelEvent e = new TableModelEvent(this, minRow, maxRow, TableModelEvent.ALL_COLUMNS, TableModelEvent.INSERT);
			
			synchronized (listeners)
			{
				for (int i = 0; i < listeners.size(); i++)
					listeners.get(i).tableChanged(e);
			}
		}
		
		protected void fireDeleteEvent(int minRow, int maxRow)
		{
			TableModelEvent e = new TableModelEvent(this, minRow, maxRow, TableModelEvent.ALL_COLUMNS, TableModelEvent.DELETE);
			
			synchronized (listeners)
			{
				for (int i = 0; i < listeners.size(); i++)
					listeners.get(i).tableChanged(e);
			}
		}
		
		protected void fireUpdateEvent(int row, int column)
		{
			TableModelEvent e = new TableModelEvent(this, row, row, column, TableModelEvent.UPDATE);
			
			synchronized (listeners)
			{
				for (int i = 0; i < listeners.size(); i++)
					listeners.get(i).tableChanged(e);
			}
		}
		
	}

	@FunctionalInterface
	public interface JObjectTableSelectionListener<T> extends ListSelectionListener
	{
		@Override
		default void valueChanged(ListSelectionEvent e) 
		{
			onSelectionChange((DefaultListSelectionModel)e.getSource(), e);
		}
		
		/**
		 * Called on a table selection change event.
		 * @param model the model it happened on.
		 * @param event the event itself.
		 */
		void onSelectionChange(DefaultListSelectionModel model, ListSelectionEvent event);
	}

	@FunctionalInterface
	public interface JObjectTableModelListener<T> extends TableModelListener
	{
		@Override
		@SuppressWarnings("unchecked")
		default void tableChanged(TableModelEvent e)
		{
			onTableChange((JObjectTableModel<T>)e.getSource(), e);
		}
		
		/**
		 * Called on a table change event.
		 * @param model the model it happened on.
		 * @param event the event itself.
		 */
		void onTableChange(JObjectTableModel<T> model, TableModelEvent event);
	}

	/**
	 * Descriptor column.
	 */
	private static class ColumnDescriptor
	{
		private ColumnDescriptor() {}
		
		/** Nice name. */
		private String name;
		/** Order key. */
		private int order;
		/** Sortable? */
		private boolean sortable;
		/** Editable? */
		private boolean editable;
		/** Data type class. */
		private Class<?> dataType;
		/** Field type. */
		private Field publicField;
		/** Setter Method (can be null). */
		private Method setterMethod;
		/** Getter Method. */
		private Method getterMethod;
	}

	/** 
	 * Hash of primitive types to promoted/boxed classes. 
	 */
	private static final HashMap<Class<?>, Class<?>> PRIMITIVE_TO_CLASS_MAP = new HashMap<Class<?>, Class<?>>()
	{
		private static final long serialVersionUID = -2547995516963695295L;
		{
			put(Void.TYPE, Void.class);
			put(Boolean.TYPE, Boolean.class);
			put(Byte.TYPE, Byte.class);
			put(Short.TYPE, Short.class);
			put(Character.TYPE, Character.class);
			put(Integer.TYPE, Integer.class);
			put(Float.TYPE, Float.class);
			put(Long.TYPE, Long.class);
			put(Double.TYPE, Double.class);
		}
	};

	// Some fields may be primitive. Swing JTable has no cell renderers
	// for these, so they need boxing.
	private static Class<?> upgradePrimitiveType(Class<?> clazz)
	{
		if (PRIMITIVE_TO_CLASS_MAP.containsKey(clazz))
			return PRIMITIVE_TO_CLASS_MAP.get(clazz);
		return clazz;
	}

	/**
	 * Checks if a method is a "getter" method.
	 * This checks its name, if it returns a non-void value, takes no arguments, and if it is <b>public</b>.
	 * @param method the method to inspect.
	 * @return true if so, false if not.
	 */
	private static boolean isGetter(Method method)
	{
		return isGetterName(method.getName()) 
			&& method.getParameterTypes().length == 0
			&& !(method.getReturnType() == Void.TYPE || method.getReturnType() == Void.class) 
			&& (method.getModifiers() & Modifier.PUBLIC) != 0;
	}

	/**
	 * Checks if a method name describes a "setter" method. 
	 * @param methodName the name of the method.
	 * @return true if so, false if not.
	 */
	private static boolean isSetterName(String methodName)
	{
		if (methodName.startsWith("set"))
		{
			if (methodName.length() < 4)
				return false;
			else
				return Character.isUpperCase(methodName.charAt(3));
		}
		return false;
	}

	/**
	 * Checks if a method name describes a "getter" method (also detects "is" methods). 
	 * @param methodName the name of the method.
	 * @return true if so, false if not.
	 */
	private static boolean isGetterName(String methodName)
	{
		if (methodName.startsWith("is"))
		{
			if (methodName.length() < 3)
				return false;
			else
				return Character.isUpperCase(methodName.charAt(2));
		}
		else if (methodName.startsWith("get"))
		{
			if (methodName.length() < 4)
				return false;
			else
				return Character.isUpperCase(methodName.charAt(3));
		}
		return false;
	}

	// truncator method
	private static String truncateMethodName(String methodName, boolean is)
	{
		return is 
			? Character.toLowerCase(methodName.charAt(2)) + methodName.substring(3)
			: Character.toLowerCase(methodName.charAt(3)) + methodName.substring(4);
	}

	/**
	 * Returns the "setter name" for a field.
	 * <p>
	 * For example, the field name "color" will return "setColor" 
	 * (note the change in camel case).
	 * @param name the field name.
	 * @return the setter name.
	 * @throws StringIndexOutOfBoundsException if name is the empty string.
	 * @throws NullPointerException if name is null.
	 */
	private static String getSetterName(String name)
	{
		return "set" + Character.toUpperCase(name.charAt(0)) + name.substring(1);
	}

	/**
	 * Returns the field name for a getter/setter method.
	 * If the method name is not a getter or setter name, then this will return <code>methodName</code>
	 * <p>
	 * For example, the field name "setColor" will return "color" and "isHidden" returns "hidden". 
	 * (note the change in camel case).
	 * @param methodName the name of the method.
	 * @return the modified method name.
	 */
	private static String getFieldName(String methodName)
	{
		if (isGetterName(methodName))
		{
			if (methodName.startsWith("is"))
				return truncateMethodName(methodName, true);
			else if (methodName.startsWith("get"))
				return truncateMethodName(methodName, false);
		}
		else if (isSetterName(methodName))
			return truncateMethodName(methodName, false);
		
		return methodName;
	}

	/**
	 * Sets the value of a field on an object.
	 * @param instance the object instance to set the field on.
	 * @param field the field to set.
	 * @param value the value to set.
	 * @throws NullPointerException if the field or object provided is null.
	 * @throws ClassCastException if the value could not be cast to the proper type.
	 * @throws RuntimeException if anything goes wrong (bad field name, 
	 * bad target, bad argument, or can't access the field).
	 * @see Field#set(Object, Object)
	 */
	private static void setFieldValue(Object instance, Field field, Object value)
	{
		try {
			field.set(instance, value);
		} catch (ClassCastException ex) {
			throw ex;
		} catch (SecurityException e) {
			throw new RuntimeException(e);
		} catch (IllegalArgumentException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Gets the value of a field on an object.
	 * @param instance the object instance to get the field value of.
	 * @param field the field to get the value of.
	 * @return the current value of the field.
	 * @throws NullPointerException if the field or object provided is null.
	 * @throws RuntimeException if anything goes wrong (bad target, bad argument, 
	 * or can't access the field).
	 * @see Field#set(Object, Object)
	 */
	private static Object getFieldValue(Object instance, Field field)
	{
		try {
			return field.get(instance);
		} catch (SecurityException e) {
			throw new RuntimeException(e);
		} catch (IllegalArgumentException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Blindly invokes a method, only throwing a {@link RuntimeException} if
	 * something goes wrong. Here for the convenience of not making a billion
	 * try/catch clauses for a method invocation.
	 * @param method the method to invoke.
	 * @param instance the object instance that is the method target.
	 * @param params the parameters to pass to the method.
	 * @return the return value from the method invocation. If void, this is null.
	 * @throws ClassCastException if one of the parameters could not be cast to the proper type.
	 * @throws RuntimeException if anything goes wrong (bad target, bad argument, or can't access the method).
	 * @see Method#invoke(Object, Object...)
	 */
	private static Object invokeBlind(Method method, Object instance, Object ... params)
	{
		Object out = null;
		try {
			out = method.invoke(instance, params);
		} catch (ClassCastException ex) {
			throw ex;
		} catch (InvocationTargetException ex) {
			throw new RuntimeException(ex);
		} catch (IllegalArgumentException ex) {
			throw new RuntimeException(ex);
		} catch (IllegalAccessException ex) {
			throw new RuntimeException(ex);
		}
		return out;
	}
	
}
