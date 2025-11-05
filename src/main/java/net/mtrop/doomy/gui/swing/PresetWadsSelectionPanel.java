package net.mtrop.doomy.gui.swing;

import java.awt.BorderLayout;

import net.mtrop.doomy.managers.GUIManager;
import net.mtrop.doomy.managers.LanguageManager;
import net.mtrop.doomy.managers.WADManager.WAD;
import net.mtrop.doomy.struct.swing.ComponentFactory.ListSelectionMode;
import net.mtrop.doomy.struct.swing.TableFactory.SelectionPolicy;

import static net.mtrop.doomy.struct.swing.ContainerFactory.*;
import static net.mtrop.doomy.struct.swing.LayoutFactory.*;
import static net.mtrop.doomy.struct.swing.ModalFactory.*;

/**
 * This is a WAD selection panel used for preset creation.
 * @author Matthew Tropiano
 */
public class PresetWadsSelectionPanel extends OrderableListPanel<WAD>
{
	private static final long serialVersionUID = -4240791475393819682L;
	
	/**
	 * Creates a new PWAD list panel.
	 */
	public PresetWadsSelectionPanel()
	{
		super(
			LanguageManager.get().getText("wads.title"),
			ListSelectionMode.MULTIPLE_INTERVAL,
			true,
			true,
			null
		);
	}

	@Override
	public void onAdd()
	{
		final WadTablePanel wadTablePanel = new WadTablePanel(SelectionPolicy.MULTIPLE_INTERVAL, (model, event) -> {}, (event) -> {});
		final GUIManager gui = GUIManager.get();
		final LanguageManager language = LanguageManager.get();

		Boolean ok = modal(this, language.getText("wads.select"), 
			containerOf(dimension(350, 200), borderLayout(),
				node(BorderLayout.CENTER, wadTablePanel)
			),
			gui.createChoiceFromLanguageKey("choice.ok", (Boolean)true),
			gui.createChoiceFromLanguageKey("choice.cancel", (Boolean)false)
		).openThenDispose();
		
		if (ok != Boolean.TRUE)
			return;
		
		addObjects(wadTablePanel.getSelectedWADs());
	}
	
}
