package net.mtrop.doomy.gui.swing;

import javax.swing.JPanel;

import net.mtrop.doomy.managers.IWADManager;
import net.mtrop.doomy.managers.IWADManager.IWAD;
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
import java.util.Arrays;
import java.util.List;

/**
 * The WAD table panel.
 */
public class IwadTablePanel extends JPanel
{
	private static final long serialVersionUID = 5567427378826188364L;
	
	private final IWADManager iwadManager;
	private final LanguageManager language;
	
	/** The WAD filter field. */
	private JFormField<String> filterField;
	/** The WAD table. */
	private JObjectTable<IWAD> iwadTable;
	
	/**
	 * Creates a new WAD Table panel.
	 * @param selectionPolicy this table's selection policy.
	 * @param selectionListener the listener to call when a selection changes.
	 */
	public IwadTablePanel(SelectionPolicy selectionPolicy, final JObjectTableSelectionListener<IWAD> selectionListener)
	{
		this.iwadManager = IWADManager.get();
		this.language = LanguageManager.get();
		
		this.filterField = stringField(this::onFilterChange);
		this.iwadTable = objectTable(selectionPolicy, 
			objectTableModel(IWAD.class, Arrays.asList(iwadManager.getAllIWADs())), 
			selectionListener
		);
		
		this.iwadTable.getColumnModel().getColumn(0).setPreferredWidth(150);
		this.iwadTable.getColumnModel().getColumn(1).setPreferredWidth(300);
		
		containerOf(this, dimension(450, 250), borderLayout(0, 8),
			node(BorderLayout.NORTH, containerOf(borderLayout(8, 0),
				node(BorderLayout.LINE_START, label(language.getText("iwads.filter"))),
				node(BorderLayout.CENTER, filterField)
			)),
			node(BorderLayout.CENTER, scroll(iwadTable))
		);
	}
	
	private void onFilterChange(String filter)
	{
		iwadTable.setRowFilter((iwad) -> iwad.name.contains(filter));
	}
	
	/**
	 * @return the current selected IWADs.
	 */
	public List<IWAD> getSelectedIWADs()
	{
		return iwadTable.getSelectedObjects();
	}
	
	/**
	 * Reloads and re-populates the table with IWADs.
	 */
	public void refreshIWADs()
	{
		iwadTable.getTableModel().setRows(Arrays.asList(iwadManager.getAllIWADs()));
	}
	
}
