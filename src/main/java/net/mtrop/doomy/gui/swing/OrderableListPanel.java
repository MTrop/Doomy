/*******************************************************************************
 * Copyright (c) 2020-2024 Matt Tropiano
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doomy.gui.swing;

import java.awt.BorderLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.swing.Action;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import static net.mtrop.doomy.struct.swing.ContainerFactory.*;
import static net.mtrop.doomy.struct.swing.ComponentFactory.*;
import static net.mtrop.doomy.struct.swing.LayoutFactory.*;


/**
 * A panel that stores object paths.
 * @author Matthew Tropiano
 * @param <T> what this list contains.
 */
public abstract class OrderableListPanel<T> extends JPanel 
{
	private static final long serialVersionUID = 5877295175127350109L;
	
	private JList<T> list;
	private OrderableListModel<T> model;
	
	private Action addAction;
	private Action removeAction;
	private Action moveUpAction;
	private Action moveDownAction;
	
	/**
	 * Creates a new object list panel.
	 * @param titleLabel the label.
	 * @param selectionMode the selection mode, or null for no selection.
	 * @param allowReordering if true, show reordering buttons. If false, no reordering, and objects are sorted on add.
	 * @param mutable if true, allow adding and removing objects from the list.
	 * @param reorderComparator if not reorderable, how to sort the list. 
	 */
	public OrderableListPanel(
		String titleLabel, 
		ListSelectionMode selectionMode, 
		boolean allowReordering,
		boolean mutable, 
		Comparator<T> reorderComparator
	){
		this.model = new OrderableListModel<>();
		this.list = new JList<>(model);
		
		this.model.addListDataListener(new ListDataListener() 
		{
			@Override
			public void intervalRemoved(ListDataEvent e) 
			{
				list.repaint();
			}
			
			@Override
			public void intervalAdded(ListDataEvent e) 
			{
				list.repaint();
			}
			
			@Override
			public void contentsChanged(ListDataEvent e) 
			{
				list.repaint();
			}
		});
		
		this.list.addKeyListener(new KeyAdapter()
		{
			@Override
			public void keyPressed(KeyEvent e)
			{
				if (e.getKeyCode() == KeyEvent.VK_DELETE)
					onRemoveSelected();
				else if ((e.getModifiersEx() & KeyEvent.ALT_DOWN_MASK) != 0)
				{
					if (e.getKeyCode() == KeyEvent.VK_UP)
						onMoveUp();
					else if (e.getKeyCode() == KeyEvent.VK_DOWN)
						onMoveDown();
				}
			}
		});
		
		if (selectionMode != null)
			this.list.setSelectionMode(selectionMode.getSwingId());
		
		List<Node> buttonNodes = new LinkedList<>();
		
		this.addAction = actionItem("+", (e) -> onAdd());
		this.removeAction = actionItem("-", (e) -> onRemoveSelected());
		this.moveUpAction = actionItem("^", (e) -> onMoveUp());
		this.moveDownAction = actionItem("v", (e) -> onMoveDown());
		
		if (mutable)
		{
			buttonNodes.add(node(button(addAction)));
			buttonNodes.add(node(button(removeAction)));
		}
		
		if (allowReordering)
		{
			buttonNodes.add(node(button(moveUpAction)));
			buttonNodes.add(node(button(moveDownAction)));
		}
		else
		{
			this.model.comparator = reorderComparator;
		}

		containerOf(this, borderLayout(0, 4),
			node(BorderLayout.NORTH, containerOf(
				node(BorderLayout.CENTER, label(titleLabel)),
				node(BorderLayout.LINE_END, containerOf(flowLayout(Flow.LEADING, 0, 0), 
					buttonNodes.toArray(new Node[buttonNodes.size()])
				))
			)),
			node(BorderLayout.CENTER, containerOf(
				node(BorderLayout.CENTER, scroll(this.list))
			))
		);
	}
	
	/**
	 * Adds an object to the model.
	 * If the object is in the model, this does nothing.
	 * @param object the object to add.
	 */
	public void addObject(T object)
	{
		model.addObjectAt(model.getSize(), object);
	}

	/**
	 * Adds a set of objects to the model.
	 * If an object is in the model, it is not added.
	 * @param objects the objects to add.
	 */
	public void addObjects(Collection<T> objects)
	{
		model.addObjectsAt(model.getSize(), objects);
	}

	/**
	 * Adds an object to a specific index in the model.
	 * If the object is in the model, this does nothing.
	 * @param index the target index.
	 * @param object the object to add.
	 */
	public void addObjectAt(int index, T object)
	{
		model.addObjectAt(index, object);
	}

	/**
	 * Adds a set of objects to a specific index in the model.
	 * If an object is in the model, it is not added.
	 * @param index the target index.
	 * @param objects the objects to add.
	 */
	public void addObjectsAt(int index, Collection<T> objects)
	{
		model.addObjectsAt(index, objects);
	}

	/**
	 * Removes the provided indices of objects.
	 * @param indices the indices of the objects in the list.
	 * @return the amount of objects removed.
	 */
	public int removeObjects(int ... indices)
	{
		List<T> out = new LinkedList<>();
		
		for (int i = 0; i < indices.length; i++) 
		{
			int index = indices[i];
			if (index >= 0 && index < count())
				out.add(model.getElementAt(index));
		}
		
		return removeObjects(out);
	}
	
	/**
	 * Removes the provided objects.
	 * @param objects the objects to remove.
	 * @return the amount of objects removed.
	 */
	public int removeObjects(Collection<T> objects)
	{
		return model.removeObjects(objects);
	}

	/**
	 * Clears the object list.
	 */
	public void clear()
	{
		model.clear();
	}
	
	/**
	 * Gets a specific object in the list.
	 * @param index the 
	 * @return the object.
	 */
	public T getObject(int index)
	{
		return model.getElementAt(index);
	}

	/**
	 * Gets all objects in the list in the order that they are currently in the list.
	 * @return the objects, or empty list if no objects.
	 */
	public List<T> getObjects()
	{
		List<T> out = new ArrayList<>(model.objectList.size());
		out.addAll(model.objectList);
		return Collections.unmodifiableList(out);
	}

	/**
	 * Sets the objects in the list in the order provided.
	 * The old list is erased.
	 * @param objects the objects to set.
	 */
	public void setObjects(Collection<T> objects)
	{
		clear();
		addObjects(objects);
	}
	
	/**
	 * Gets the first or only currently selected object.
	 * @return the currently selected object, or null if no object.
	 */
	public T getSelectedObject()
	{
		int index = list.getSelectedIndex();
		return index < 0 || index >= model.getSize() ? null : model.getElementAt(index);
	}

	/**
	 * Gets the currently selected objects, in list order.
	 * @return the currently selected objects, or empty list if no objects.
	 */
	public List<T> getSelectedObjects()
	{
		int[] indices = list.getSelectedIndices();
		List<T> out = new ArrayList<>(indices.length);
		for (int i = 0; i < indices.length; i++)
		{
			int index = indices[i];
			if (index >= 0 && index < model.getSize())
				out.add(model.getElementAt(indices[i]));
		}
		return Collections.unmodifiableList(out);
	}
	
	/**
	 * Gets an immutable set of objects that are in the list.
	 * NOTE: The set may not reflect list order!
	 * @return the set of objects, or empty set if no objects.
	 */
	public Set<T> getObjectSet()
	{
		Set<T> set = new HashSet<>();
		set.addAll(getObjects());
		return Collections.unmodifiableSet(set);
	}
	
	/**
	 * Gets an immutable set of objects that are currently selected in the list.
	 * NOTE: The set may not reflect list order!
	 * @return the set of objects, or empty set if no objects.
	 */
	public Set<T> getSelectedObjectSet()
	{
		Set<T> set = new HashSet<>();
		set.addAll(getSelectedObjects());
		return Collections.unmodifiableSet(set);
	}
	
	/**
	 * Gets the index of a specific object in the list.
	 * @param object the object to search for.
	 * @return the index, or -1 if not found.
	 */
	public int indexOf(T object)
	{
		return model.objectList.indexOf(object);
	}
	
	/**
	 * Called when the user wants to add objects.
	 */
	public abstract void onAdd();
	
	/**
	 * Moves the selected objects up one position in the list.
	 */
	public void onMoveUp()
	{
		int firstIndex = list.getMinSelectionIndex();
		if (firstIndex < 1 || firstIndex >= model.getSize())
			return;

		List<T> objects = getSelectedObjects();
		removeObjects(objects);
		
		// Use an anchor object because a lot of ordering may change.
		T anchor = getObject(firstIndex - 1);
		int addIndex = indexOf(anchor);
		
		addObjectsAt(addIndex, objects);
		list.getSelectionModel().setSelectionInterval(addIndex, addIndex + (objects.size() - 1));
	}
	
	/**
	 * Moves the selected objects up one position in the list.
	 */
	public void onMoveDown()
	{
		int firstIndex = list.getMinSelectionIndex();
		int lastIndex = list.getMaxSelectionIndex();
		if (lastIndex < 0 || lastIndex >= model.getSize() - 1)
			return;

		List<T> objects = getSelectedObjects();
		removeObjects(objects);
		
		int addIndex = firstIndex + 1;
		
		addObjectsAt(addIndex, objects);
		list.getSelectionModel().setSelectionInterval(addIndex, addIndex + (objects.size() - 1));
	}

	private void onRemoveSelected()
	{
		removeObjects(getSelectedObjects());
	}
	
	/**
	 * @return the amount of objects in the list.
	 */
	public int count() 
	{
		return model.getSize();
	}
	
	private static class OrderableListModel<T> implements ListModel<T>
	{
		private final Object MUTEX;
		private final Set<T> objectSet;
		private final List<T> objectList;
		private final List<ListDataListener> listeners;
		
		private Comparator<T> comparator;

		
		private OrderableListModel() 
		{
			this.MUTEX = new Object();
			this.objectSet = new HashSet<>();
			this.objectList = new ArrayList<>();
			this.listeners = Collections.synchronizedList(new ArrayList<>(4));
			this.comparator = null;
		}
		
		/**
		 * Adds an object to a specific index in the model.
		 * If the object is in the model, this does nothing.
		 * @param index the target index.
		 * @param object the object to add.
		 */
		public void addObjectAt(int index, T object)
		{
			if (objectSet.contains(object))
				return;
			
			synchronized (MUTEX) 
			{
				// Early out.
				if (objectSet.contains(object))
					return;
				
				objectSet.add(object);
				objectList.add(index, object);
			}
			
			if (comparator != null)
				objectList.sort(comparator);
			
			listeners.forEach((listener) -> listener.intervalAdded(
				new ListDataEvent(this, ListDataEvent.INTERVAL_ADDED, index, index)
			));
		}

		/**
		 * Adds a set of objects to a specific index in the model.
		 * If an object is in the model, it is not added.
		 * @param index the target index.
		 * @param objects the objects to add.
		 */
		public void addObjectsAt(int index, Collection<T> objects)
		{
			// Add backwards.
			synchronized (MUTEX) 
			{
				for (T object : objects)
				{
					if (objectSet.contains(object))
						return;
		
					objectSet.add(object);
					objectList.add(index, object);
					index++;
				}
			}

			if (comparator != null)
				objectList.sort(comparator);
			
			listeners.forEach((listener) -> listener.intervalAdded(
				new ListDataEvent(this, ListDataEvent.CONTENTS_CHANGED, 0, 0)
			));
		}

		
		
		/**
		 * Removes the provided objects.
		 * @param objects the objects to remove.
		 * @return the amount of objects removed.
		 */
		public int removeObjects(Collection<T> objects)
		{
			int deleted = 0;

			synchronized (MUTEX) 
			{
				for (T object : objects) 
				{
					if (objectSet.remove(object))
					{
						objectList.remove(object);
						deleted++;
					}
				}
			}
			
			listeners.forEach((listener) -> listener.intervalRemoved(
				new ListDataEvent(this, ListDataEvent.CONTENTS_CHANGED, 0, 0)
			));
			return deleted;
		}

		/**
		 * Clears the list.
		 */
		public void clear()
		{
			synchronized (MUTEX)
			{
				objectSet.clear();
				objectList.clear();
			}
			
			listeners.forEach((listener) -> listener.intervalRemoved(
				new ListDataEvent(this, ListDataEvent.CONTENTS_CHANGED, 0, 0)
			));
		}
		
		@Override
		public T getElementAt(int index) 
		{
			return objectList.get(index);
		}
		
		@Override
		public void addListDataListener(ListDataListener l) 
		{
			listeners.add(l);
		}
		
		@Override
		public void removeListDataListener(ListDataListener l) 
		{
			listeners.remove(l);
		}

		@Override
		public int getSize() 
		{
			return objectSet.size();
		}
	}
	
}
