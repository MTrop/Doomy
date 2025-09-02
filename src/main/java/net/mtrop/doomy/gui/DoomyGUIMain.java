package net.mtrop.doomy.gui;

import net.mtrop.doomy.managers.DatabaseManager;
import net.mtrop.doomy.managers.GUIManager;
import net.mtrop.doomy.struct.swing.ModalFactory.Modal;
import net.mtrop.doomy.struct.swing.SwingUtils;
import net.mtrop.doomy.struct.util.OSUtils;

import java.awt.Dialog.ModalityType;
import java.awt.BorderLayout;

import static net.mtrop.doomy.struct.swing.ModalFactory.modal;
import static net.mtrop.doomy.struct.swing.ComponentFactory.label;
import static net.mtrop.doomy.struct.swing.ContainerFactory.*;
import static net.mtrop.doomy.struct.swing.LayoutFactory.*;

/**
 * Main entry for Doomy GUI.
 * @author Matthew Tropiano
 */
public final class DoomyGUIMain
{
	/**
	 * Sets the preferred Look And Feel.
	 */
	public static void setLAF() 
	{
		if (OSUtils.isOSX())
			System.setProperty("apple.laf.useScreenMenuBar", "true");
		SwingUtils.setLAF(GUIManager.get().getThemeType().className);
	}
	
	public static void main(String[] args)
	{
		setLAF();
		
		if (!DatabaseManager.databaseExists())
		{
			Modal<Void> dbModal = modal("Preparing for First Use", ModalityType.MODELESS, containerOf(dimension(256, 32), borderLayout(),
				node(BorderLayout.CENTER, label("Creating database..."))
			));
			dbModal.setVisible(true);
			DatabaseManager.get();
			dbModal.dispose();
		}

	}

}
