package net.mtrop.doomy.gui;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JPanel;

import net.mtrop.doomy.DoomyCommon;
import net.mtrop.doomy.gui.swing.EngineTableControlPanel;
import net.mtrop.doomy.gui.swing.IwadTableControlPanel;
import net.mtrop.doomy.gui.swing.WadTableControlPanel;
import net.mtrop.doomy.managers.GUIManager;
import net.mtrop.doomy.managers.LanguageManager;
import net.mtrop.doomy.struct.swing.LayoutFactory.BoxAxis;
import net.mtrop.doomy.struct.util.ObjectUtils;

import java.awt.BorderLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import static net.mtrop.doomy.struct.swing.ComponentFactory.*;
import static net.mtrop.doomy.struct.swing.ContainerFactory.*;
import static net.mtrop.doomy.struct.swing.ModalFactory.*;
import static net.mtrop.doomy.struct.swing.LayoutFactory.*;


/**
 * The main window for Doomy.
 */
public class DoomyGUIMainWindow extends JFrame
{
	private static final long serialVersionUID = -7341160374312252193L;
	
	/** Doomy Version */
	public static final String VERSION = DoomyCommon.getVersionString("doomy");

	private final GUIManager gui;
	private final LanguageManager language;
	
	public DoomyGUIMainWindow()
	{
		this.gui = GUIManager.get();
		this.language = LanguageManager.get();
		
		setTitle("Doomy v" + VERSION);
		setJMenuBar(createMenuBar());
		setResizable(true);
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		setLocationByPlatform(true);

		addWindowListener(new WindowAdapter() 
		{
			@Override
			public void windowClosing(WindowEvent e) 
			{
				attemptClose();
			}
		});
		
		setContentPane(containerOf(dimension(640, 480), borderLayout(8, 8),
			node(BorderLayout.CENTER, tabs(TabPlacement.TOP, TabLayoutPolicy.WRAP, 
				tab(language.getText("tab.engines"), containerOf(
					node(new EngineTableControlPanel(), (panel) -> panel.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0)))
				)),
				tab(language.getText("tab.iwads"), containerOf(
					node(new IwadTableControlPanel(), (panel) -> panel.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0)))
				)),
				tab(language.getText("tab.wads"), containerOf(
					node(new WadTableControlPanel(), (panel) -> panel.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0)))
				))
			), (tabs) -> tabs.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8)))
		));
		
		pack();
	}
	
	private JMenuBar createMenuBar()
	{
		return menuBar(
			gui.createMenuFromLanguageKey("menu.file",
				gui.createItemFromLanguageKey("menu.file.exit", (i) -> attemptClose())
			),
			gui.createMenuFromLanguageKey("menu.help",
				gui.createItemFromLanguageKey("menu.help.about", (i) -> onAbout())
			)
		);
	}
	
	private void onAbout()
	{
		String labelHTML = (new StringBuilder()).append("<html>")
			.append("Doomy v" + VERSION).append("<br>")
		.append("</html>").toString();
		
		modal(this, language.getText("about.title"), 
			ObjectUtils.apply(new JPanel(), (panel) -> containerOf(panel, dimension(350, 250), boxLayout(panel, BoxAxis.Y_AXIS),
				node(label(labelHTML))
			)), 
			choice(language.getText("choice.ok"), (Boolean)true))
		.openThenDispose();
	}
	
	private void attemptClose()
	{
		if (onCloseAttempt())
		{
			setVisible(false);
			dispose();
		}
	}

	private boolean onCloseAttempt()
	{
		// TODO: Finish.
		return true;
	}
	
}
