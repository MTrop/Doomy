package net.mtrop.doomy.gui;

import javax.swing.JFrame;
import javax.swing.JMenuBar;

import net.mtrop.doomy.managers.GUIManager;
import net.mtrop.doomy.managers.LanguageManager;

/**
 * The main window for Doomy.
 */
public class DoomyGUIMainWindow extends JFrame
{
	private GUIManager gui;
	private LanguageManager language;
	
	public DoomyGUIMainWindow()
	{
		this.gui = GUIManager.get();
		this.language = LanguageManager.get();
	}
	
	private JMenuBar createMenuBar()
	{
		// TODO: Finish.
		return null;
	}
	
	private boolean onCloseAttempt()
	{
		// TODO: Finish.
		return true;
	}
	
}
