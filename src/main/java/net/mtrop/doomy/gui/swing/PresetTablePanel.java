/*******************************************************************************
 * Copyright (c) 2019-2025 Matt Tropiano
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doomy.gui.swing;

import static net.mtrop.doomy.struct.swing.ComponentFactory.label;
import static net.mtrop.doomy.struct.swing.ContainerFactory.containerOf;
import static net.mtrop.doomy.struct.swing.ContainerFactory.dimension;
import static net.mtrop.doomy.struct.swing.ContainerFactory.node;
import static net.mtrop.doomy.struct.swing.ContainerFactory.scroll;
import static net.mtrop.doomy.struct.swing.FormFactory.stringField;
import static net.mtrop.doomy.struct.swing.LayoutFactory.borderLayout;
import static net.mtrop.doomy.struct.swing.TableFactory.objectTable;
import static net.mtrop.doomy.struct.swing.TableFactory.objectTableModel;

import java.awt.BorderLayout;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import javax.swing.JPanel;
import javax.swing.event.MouseInputAdapter;

import net.mtrop.doomy.managers.LanguageManager;
import net.mtrop.doomy.managers.PresetManager;
import net.mtrop.doomy.managers.PresetManager.PresetInfo;
import net.mtrop.doomy.struct.swing.FormFactory.JFormField;
import net.mtrop.doomy.struct.swing.TableFactory.JObjectTable;
import net.mtrop.doomy.struct.swing.TableFactory.JObjectTableSelectionListener;
import net.mtrop.doomy.struct.swing.TableFactory.SelectionPolicy;

/**
 * The preset table panel. It's a doozy.
 * @author Matthew Tropiano
 */
public class PresetTablePanel extends JPanel
{
	private static final long serialVersionUID = 912870847147062936L;

	private final PresetManager presetManager;
	private final LanguageManager language;
	
	/** The Preset filter field. */
	private JFormField<String> filterField;
	/** The Preset table. */
	private JObjectTable<PresetInfo> presetTable;

	/**
	 * Creates the new preset panel.
	 * @param selectionPolicy this table's selection policy.
	 * @param selectionListener the listener to call when a selection changes.
	 * @param doubleClickListener what to call on a double-click.
	 */
	public PresetTablePanel(
		SelectionPolicy selectionPolicy, 
		final JObjectTableSelectionListener<PresetInfo> selectionListener,
		final Consumer<MouseEvent> doubleClickListener
	){
		this.presetManager = PresetManager.get();
		this.language = LanguageManager.get();

		this.filterField = stringField(this::onFilterChange);
		this.presetTable = objectTable(selectionPolicy, 
			objectTableModel(PresetInfo.class, Arrays.asList(presetManager.getAllPresets())), 
			selectionListener
		);
		this.presetTable.addMouseListener(new MouseInputAdapter() 
		{
			@Override
			public void mouseClicked(MouseEvent e) 
			{
				if (e.getClickCount() == 2)
					doubleClickListener.accept(e);
			}
		});
			
		this.presetTable.getColumnModel().getColumn(0).setPreferredWidth(150);
		this.presetTable.getColumnModel().getColumn(1).setPreferredWidth(100);
		this.presetTable.getColumnModel().getColumn(2).setPreferredWidth(100);
		this.presetTable.getColumnModel().getColumn(3).setPreferredWidth(200);
		
		containerOf(this, dimension(650, 300), borderLayout(0, 8),
			node(BorderLayout.NORTH, containerOf(borderLayout(8, 0),
				node(BorderLayout.LINE_START, label(language.getText("wads.filter"))),
				node(BorderLayout.CENTER, filterField)
			)),
			node(BorderLayout.CENTER, scroll(presetTable))
		);
	}
	
	private void onFilterChange(String filter)
	{
		final String filterLower = filter.toLowerCase();
		presetTable.setRowFilter((preset) -> preset.name.toLowerCase().contains(filterLower));
	}
	
	/**
	 * @return the current selected Presets.
	 */
	public List<PresetInfo> getSelectedPresets()
	{
		return presetTable.getSelectedObjects();
	}
	
	/**
	 * Reloads and re-populates the table with Presets.
	 */
	public void refreshPresets()
	{
		presetTable.getTableModel().setRows(Arrays.asList(presetManager.getAllPresets()));
	}
	
}
