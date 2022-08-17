package net.mtrop.doomy.gui;

import java.util.function.Supplier;

import javax.swing.JFrame;

/**
 * The main Doomy window.
 * @author Matthew Tropiano
 */
public class DoomyMainWindow extends JFrame
{
	private static final long serialVersionUID = -263888696943476371L;
	
	// TODO: Finish this.
	
	private Supplier<Boolean> onClose;
	
	public DoomyMainWindow(Supplier<Boolean> onClose)
	{
		this.onClose = onClose;
	}
	
}
