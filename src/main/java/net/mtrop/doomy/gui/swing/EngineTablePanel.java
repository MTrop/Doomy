/*******************************************************************************
 * Copyright (c) 2019-2025 Matt Tropiano
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doomy.gui.swing;

import javax.swing.JPanel;
import javax.swing.event.MouseInputAdapter;

import net.mtrop.doomy.managers.EngineManager;
import net.mtrop.doomy.managers.EngineManager.Engine;
import net.mtrop.doomy.managers.LanguageManager;
import net.mtrop.doomy.struct.swing.FormFactory.JFormField;
import net.mtrop.doomy.struct.swing.TableFactory.JObjectTable;
import net.mtrop.doomy.struct.swing.TableFactory.SelectionPolicy;

import static net.mtrop.doomy.struct.swing.ContainerFactory.*;
import static net.mtrop.doomy.struct.swing.ComponentFactory.*;
import static net.mtrop.doomy.struct.swing.FormFactory.*;
import static net.mtrop.doomy.struct.swing.TableFactory.*;
import static net.mtrop.doomy.struct.swing.LayoutFactory.*;

import java.awt.BorderLayout;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

/**
 * The WAD table panel.
 */
public class EngineTablePanel extends JPanel
{
	private static final long serialVersionUID = 4263494603409552234L;

	private final EngineManager engineManager;
	private final LanguageManager language;
	
	/** The filter field. */
	private JFormField<String> filterField;
	/** The engine table. */
	private JObjectTable<Engine> engineTable;
	
	/**
	 * Creates a new Engine Table panel.
	 * @param selectionPolicy this table's selection policy.
	 * @param selectionListener the listener to call when a selection changes.
	 * @param doubleClickListener what to call on a double-click.
	 */
	public EngineTablePanel(
		SelectionPolicy selectionPolicy, 
		final JObjectTableSelectionListener<Engine> selectionListener,
		final Consumer<MouseEvent> doubleClickListener
	){
		this.engineManager = EngineManager.get();
		this.language = LanguageManager.get();
		
		this.filterField = stringField(this::onFilterChange);
		this.engineTable = objectTable(SelectionPolicy.SINGLE_INTERVAL, 
			objectTableModel(Engine.class, Arrays.asList(engineManager.getAllEngines())), 
			selectionListener
		);
		this.engineTable.addMouseListener(new MouseInputAdapter() 
		{
			@Override
			public void mouseClicked(MouseEvent e) 
			{
				if (e.getClickCount() == 2)
					doubleClickListener.accept(e);
			}
		});
		
		this.engineTable.getColumnModel().getColumn(0).setPreferredWidth(300);
		
		containerOf(this, dimension(300, 250), borderLayout(0, 8),
			node(BorderLayout.NORTH, containerOf(borderLayout(8, 0),
				node(BorderLayout.LINE_START, label(language.getText("iwads.filter"))),
				node(BorderLayout.CENTER, filterField)
			)),
			node(BorderLayout.CENTER, scroll(engineTable))
		);
	}
	
	private void onFilterChange(String filter)
	{
		final String filterLower = filter.toLowerCase();
		engineTable.setRowFilter((engine) -> engine.name.toLowerCase().contains(filterLower));
	}
	
	/**
	 * @return the current selected IWADs.
	 */
	public List<Engine> getSelectedEngines()
	{
		return engineTable.getSelectedObjects();
	}
	
	/**
	 * Reloads and re-populates the table with IWADs.
	 */
	public void refreshEngines()
	{
		engineTable.getTableModel().setRows(Arrays.asList(engineManager.getAllEngines()));
	}
	
}
