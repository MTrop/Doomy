package net.mtrop.doomy.gui;

import java.awt.Rectangle;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.util.Map;

import javax.swing.JFrame;

import net.mtrop.doomy.gui.swing.struct.SwingUtils;
import net.mtrop.doomy.managers.ConfigManager;
import net.mtrop.doomy.managers.LanguageManager;
import net.mtrop.doomy.struct.EnumUtils;
import net.mtrop.doomy.struct.OSUtils;
import net.mtrop.doomy.struct.ProcessCallable;
import net.mtrop.doomy.struct.SingletonProvider;

/**
 * Main entry for Doomy GUI.
 * @author Matthew Tropiano
 */
public final class DoomyGUIMain
{
    /** Instance socket. */
	private static final int INSTANCE_SOCKET_PORT = 54886;
    /** The instance encapsulator. */
    private static final SingletonProvider<DoomyGUIMain> INSTANCE = new SingletonProvider<>(() -> new DoomyGUIMain());

    /**
	 * Supported GUI Themes
	 */
	public enum GUIThemeType
	{
		LIGHT("com.formdev.flatlaf.FlatLightLaf"),
		DARK("com.formdev.flatlaf.FlatDarkLaf"),
		INTELLIJ("com.formdev.flatlaf.FlatIntelliJLaf"),
		DARCULA("com.formdev.flatlaf.FlatDarculaLaf");
		
		public static final Map<String, GUIThemeType> MAP = EnumUtils.createCaseInsensitiveNameMap(GUIThemeType.class);
		
		private final String className;
		
		private GUIThemeType(String className)
		{
			this.className = className;
		}
	}

    /** Instance socket. */
    @SuppressWarnings("unused")
	private static ServerSocket instanceSocket;
    
	/**
	 * @return the singleton instance of this settings object.
	 */
	public static DoomyGUIMain get()
	{
		return INSTANCE.get();
	}

	/**
	 * @return true if already running, false if not.
	 */
	public static boolean isAlreadyRunning()
	{
		try {
			instanceSocket = new ServerSocket(INSTANCE_SOCKET_PORT, 50, InetAddress.getByName(null));
			return false;
		} catch (IOException e) {
			return true;
		}
	}
	
	/**
	 * Starts an orphaned main GUI Application.
	 * Inherits the working directory and environment.
	 * @return the process created.
	 * @throws IOException if the application could not be created.
	 */
	public static Process startGUIAppProcess() throws IOException
	{
		return ProcessCallable.java(DoomyGUIMain.class, "-Xms64M", "-Xmx768M").exec();
	}
	
	// Sets the exception handler.
	private static void setExceptionHandler()
	{
		Thread.setDefaultUncaughtExceptionHandler((thread, exception) -> {
			System.err.printf("Thread [%s] threw an uncaught exception!\n", thread.getName());
			exception.printStackTrace(System.err);
		});
	}

	/**
	 * Sets the preferred Look And Feel.
	 */
	public static void setLAF() 
	{
		if (OSUtils.isOSX())
			System.setProperty("apple.laf.useScreenMenuBar", "true");
		GUIThemeType theme = GUIThemeType.MAP.get(ConfigManager.get().getValue(ConfigManager.SETTING_THEME_NAME));
		SwingUtils.setLAF(theme != null ? theme.className : GUIThemeType.LIGHT.className);
	}

    /* ==================================================================== */

	/**
	 * Main method - check for running local instance. If running, do nothing.
	 * @param args command line arguments.
	 */
	public static void main(String[] args) 
	{
		setLAF();
		setExceptionHandler();
		
    	if (isAlreadyRunning())
    	{
    		System.err.println("Doomy is already running.");
    		System.exit(1);
    		return;
    	}
		get().createAndDisplayMainWindow();
	}
	
    /* ==================================================================== */

	/** The main window. */
    private DoomyMainWindow window;
    
    private DoomyGUIMain()
    {
    	this.window = null;
    }

    /**
     * Creates and displays the main window.
     */
    public void createAndDisplayMainWindow()
    {
    	window = new DoomyMainWindow(this::attemptShutDown);
    	window.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
    	window.addWindowListener(new WindowAdapter()
    	{
    		@Override
    		public void windowClosing(WindowEvent e) 
    		{
    			attemptShutDown();
    		}
		});
    	
    	ConfigManager settings = ConfigManager.get();
    	
    	Rectangle windowBounds;
    	if ((windowBounds = settings.getRectangleValue(ConfigManager.SETTING_WINDOW, new Rectangle(0, 0, 800, 600))) != null)
    		window.setBounds(windowBounds);
    	
    	window.setVisible(true);
		if ("true".equals(settings.getValue(ConfigManager.SETTING_WINDOW_MAX)))
			window.setExtendedState(window.getExtendedState() | DoomyMainWindow.MAXIMIZED_BOTH);
		
    }

    // Attempts a shutdown, prompting the user first.
    private boolean attemptShutDown()
    {
		if (SwingUtils.yesTo(window, LanguageManager.get().getText("doomy.quit")))
		{
			shutDown();
			return true;
		}
		return false;
    }

    // Saves and quits.
    private void shutDown()
    {
    	ConfigManager.get().setRectangleValue(ConfigManager.SETTING_WINDOW, window.getBounds());
    	ConfigManager.get().setValue(ConfigManager.SETTING_WINDOW_MAX, String.valueOf((window.getExtendedState() & DoomyMainWindow.MAXIMIZED_BOTH) != 0));
    	window.setVisible(false);
    	window.dispose();
    	System.exit(0);
    }

}
