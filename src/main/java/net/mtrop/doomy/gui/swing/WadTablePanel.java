package net.mtrop.doomy.gui.swing;

import javax.swing.DefaultListSelectionModel;
import javax.swing.JPanel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableModelEvent;

import net.mtrop.doomy.managers.WADManager;
import net.mtrop.doomy.managers.WADManager.WAD;
import net.mtrop.doomy.struct.swing.FormFactory.JFormField;
import net.mtrop.doomy.struct.swing.TableFactory.JObjectTable;
import net.mtrop.doomy.struct.swing.TableFactory.JObjectTableModel;

import static net.mtrop.doomy.struct.swing.ContainerFactory.*;
import static net.mtrop.doomy.struct.swing.ComponentFactory.*;
import static net.mtrop.doomy.struct.swing.FormFactory.*;
import static net.mtrop.doomy.struct.swing.TableFactory.*;
import static net.mtrop.doomy.struct.swing.LayoutFactory.*;

import java.awt.BorderLayout;
import java.util.Arrays;

/**
 * The WAD table panel.
 */
public class WadTablePanel extends JPanel
{
	private static final long serialVersionUID = 5567427378826188364L;
	
	private WADManager wadManager;
	
	/** The WAD filter field. */
	private JFormField<String> filterField;
	/** The WAD table. */
	private JObjectTable<WAD> wadTable;
	
	/**
	 * Creates a new WAD Table panel.
	 */
	public WadTablePanel()
	{
		this.wadManager = WADManager.get();
		this.filterField = stringField(this::onFilterChange);
		this.wadTable = objectTable(SelectionPolicy.SINGLE_INTERVAL, 
			objectTableModel(WAD.class, Arrays.asList(wadManager.getAllWADs("")), this::onTableChange), 
			this::onSelectionChange
		);
		
		containerOf(this, dimension(350, 250), borderLayout(0, 8),
			node(BorderLayout.NORTH, containerOf(borderLayout(8, 0),
				node(BorderLayout.LINE_START, label("Filter: ")),
				node(BorderLayout.CENTER, filterField)
			)),
			node(BorderLayout.CENTER, scroll(wadTable))
		);
	}
	
	private void onFilterChange(String filter)
	{
		wadTable.setRowFilter((wad) -> wad.name.contains(filter));
	}
	
	private void onTableChange(JObjectTableModel<WAD> model, TableModelEvent event)
	{
		// TODO: Make better.
		if (event.getType() == TableModelEvent.UPDATE)
		{
			System.out.println(model.getRow(event.getFirstRow()).name);
		}
	}
	
	private void onSelectionChange(DefaultListSelectionModel model, ListSelectionEvent event)
	{
		// TODO: Make better.
		if (!event.getValueIsAdjusting())
			System.out.println(wadTable.getSelectedRow());
	}
	
}
