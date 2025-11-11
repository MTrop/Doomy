/*******************************************************************************
 * Copyright (c) 2019-2025 Matt Tropiano
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doomy.gui.swing;

import javax.swing.JPanel;
import javax.swing.event.MouseInputAdapter;

import net.mtrop.doomy.managers.EngineTemplateManager;
import net.mtrop.doomy.managers.EngineTemplateManager.EngineTemplate;
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
public class EngineTemplateTablePanel extends JPanel
{
	private static final long serialVersionUID = 8939443133646566163L;
	
	private final EngineTemplateManager engineTemplateManager;
	private final LanguageManager language;
	
	/** The filter field. */
	private JFormField<String> filterField;
	/** The engine table. */
	private JObjectTable<EngineTemplate> engineTemplateTable;
	
	/**
	 * Creates a new Engine Template Table panel.
	 * @param selectionPolicy this table's selection policy.
	 * @param selectionListener the listener to call when a selection changes.
	 * @param doubleClickListener what to call on a double-click.
	 */
	public EngineTemplateTablePanel(
		SelectionPolicy selectionPolicy, 
		final JObjectTableSelectionListener<EngineTemplate> selectionListener,
		final Consumer<MouseEvent> doubleClickListener
	){
		this.engineTemplateManager = EngineTemplateManager.get();
		this.language = LanguageManager.get();
		
		this.filterField = stringField(this::onFilterChange);
		this.engineTemplateTable = objectTable(SelectionPolicy.SINGLE_INTERVAL, 
			objectTableModel(EngineTemplate.class, Arrays.asList(engineTemplateManager.getAllTemplates())), 
			selectionListener
		);
		this.engineTemplateTable.addMouseListener(new MouseInputAdapter() 
		{
			@Override
			public void mouseClicked(MouseEvent e) 
			{
				if (e.getClickCount() == 2)
					doubleClickListener.accept(e);
			}
		});
		
		this.engineTemplateTable.getColumnModel().getColumn(0).setPreferredWidth(300);
		
		containerOf(this, dimension(300, 250), borderLayout(0, 8),
			node(BorderLayout.NORTH, containerOf(borderLayout(8, 0),
				node(BorderLayout.LINE_START, label(language.getText("template.filter"))),
				node(BorderLayout.CENTER, filterField)
			)),
			node(BorderLayout.CENTER, scroll(engineTemplateTable))
		);
	}
	
	private void onFilterChange(String filter)
	{
		final String filterLower = filter.toLowerCase();
		engineTemplateTable.setRowFilter((engine) -> engine.name.toLowerCase().contains(filterLower));
	}
	
	/**
	 * @return the current selected IWADs.
	 */
	public List<EngineTemplate> getSelectedTemplates()
	{
		return engineTemplateTable.getSelectedObjects();
	}
	
	/**
	 * Reloads and re-populates the table with IWADs.
	 */
	public void refreshEngines()
	{
		engineTemplateTable.getTableModel().setRows(Arrays.asList(engineTemplateManager.getAllTemplates()));
	}
	
}
