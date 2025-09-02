package net.mtrop.doomy.managers;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Properties;

import net.mtrop.doomy.DoomyEnvironment;
import net.mtrop.doomy.DoomySetupException;
import net.mtrop.doomy.struct.SingletonProvider;
import net.mtrop.doomy.struct.swing.SwingUtils;
import net.mtrop.doomy.struct.util.EnumUtils;

public class GUIManager 
{
	public static final String PROPERTY_THEMENAME = "gui.theme";
	
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
		
		public final String className;
		
		private GUIThemeType(String className)
		{
			this.className = className;
		}
	}
	
	// =======================================================================
	
	// Singleton instance.
	private static final SingletonProvider<GUIManager> INSTANCE = new SingletonProvider<>(() -> new GUIManager());

	/**
	 * Initializes/Returns the singleton manager instance.
	 * @return the single manager.
	 * @throws DoomySetupException if the manager could not be set up.
	 */
	public static GUIManager get()
	{
		return INSTANCE.get();
	}

	// =======================================================================

	private Properties properties;
	
	private GUIManager()
	{
		this.properties = new Properties();
		File propsFile = new File(DoomyEnvironment.getPropertiesPath());
		if (propsFile.exists())
		{
			try (Reader reader = new BufferedReader(new InputStreamReader(new FileInputStream(propsFile), StandardCharsets.UTF_8)))
			{
				properties.load(reader);
			} 
			catch (FileNotFoundException e) 
			{
				SwingUtils.error(e.getLocalizedMessage());
			} 
			catch (IOException e) 
			{
				SwingUtils.error(e.getLocalizedMessage());
			}
		}
	}
	
	private void commit()
	{
		File propsFile = new File(DoomyEnvironment.getPropertiesPath());
		try (Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(propsFile), StandardCharsets.UTF_8)))
		{
			properties.store(writer, "Written by Doomy");
		} 
		catch (IOException e) 
		{
			SwingUtils.error(e.getLocalizedMessage());
		}
	}
	
	/**
	 * Gets the theme type to use for the GUI theme.
	 * @return the theme type.
	 */
	public GUIThemeType getThemeType()
	{
		String value = properties.getProperty(PROPERTY_THEMENAME);
		if (value == null)
			return GUIThemeType.LIGHT;
		GUIThemeType theme = GUIThemeType.MAP.get(value);
		return theme != null ? theme : GUIThemeType.LIGHT;
	}
	
	/**
	 * Sets the theme type to use for the GUI theme.
	 * @param type the theme type.
	 */
	public void setThemeType(GUIThemeType type)
	{
		properties.setProperty(PROPERTY_THEMENAME, type.name());
		commit();
	}
	
}
