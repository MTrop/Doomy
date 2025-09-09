package net.mtrop.doomy.gui.swing;

import javax.swing.JPanel;

import net.mtrop.doomy.managers.WADManager;
import net.mtrop.doomy.managers.WADManager.WAD;
import net.mtrop.doomy.struct.swing.FormFactory.JFormField;
import net.mtrop.doomy.struct.swing.TableFactory.JObjectTable;

import static net.mtrop.doomy.struct.swing.ContainerFactory.*;
import static net.mtrop.doomy.struct.swing.ComponentFactory.*;
import static net.mtrop.doomy.struct.swing.FormFactory.*;
import static net.mtrop.doomy.struct.swing.TableFactory.*;
import static net.mtrop.doomy.struct.swing.LayoutFactory.*;

import java.awt.BorderLayout;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

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
	 * @param selectionListener the listener to call when a selection changes.
	 */
	public WadTablePanel(final JObjectTableSelectionListener<WAD> selectionListener)
	{
		this.wadManager = WADManager.get();
		this.filterField = stringField(this::onFilterChange);
		this.wadTable = objectTable(SelectionPolicy.SINGLE_INTERVAL, 
			objectTableModel(WAD.class, Arrays.asList(wadManager.getAllWADs(""))), 
			selectionListener
		);
		
		this.wadTable.getColumnModel().getColumn(0).setPreferredWidth(150);
		this.wadTable.getColumnModel().getColumn(1).setPreferredWidth(300);
		
		containerOf(this, dimension(450, 250), borderLayout(0, 8),
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
	
	/**
	 * @return the current selected WADs.
	 */
	public List<WAD> getSelectedWADs()
	{
		return wadTable.getSelectedObjects();
	}
	
	/**
	 * Reloads and re-populates the table with WADs.
	 */
	public void refreshWADs()
	{
		wadTable.getTableModel().setRows(Arrays.asList(wadManager.getAllWADs("")));
	}
	
}
