/*******************************************************************************
 * Copyright (c) 2020-2022 Matt Tropiano
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doomy.managers;

import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.util.Locale;
import java.util.Objects;
import java.util.Properties;
import java.util.function.Function;

import javax.swing.KeyStroke;

import net.mtrop.doomy.gui.swing.struct.SwingUtils;
import net.mtrop.doomy.struct.IOUtils;
import net.mtrop.doomy.struct.OSUtils;
import net.mtrop.doomy.struct.SingletonProvider;

/**
 * DoomTools Language Manager.
 * @author Matthew Tropiano
 */
public final class LanguageManager
{
	/** Default language. */
	private static final String DEFAULT_LANGUAGE = "default";
	/** OS: Windows. */
	private static final String OS_WINDOWS = "win";
	/** OS: OSX/macOS. */
	private static final String OS_MACOS = "mac";
	/** OS: Default Linux. */
	private static final String OS_LINUX = "linux";
	
	/** That dang option key on MacOS. */
	private static final int OPTION_MASK = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
	
	/** Missing text. */
	private static final String MISSING_TEXT = "[[NO LANGUAGE MATCH]]";
	
	/** Class loader. */
	private static final ClassLoader LOADER = ClassLoader.getSystemClassLoader();
	
    private static final SingletonProvider<LanguageManager> INSTANCE = new SingletonProvider<>(() -> new LanguageManager());

	/**
	 * @return the singleton instance of this settings object.
	 */
	public static LanguageManager get()
	{
		return INSTANCE.get();
	}

	/* ==================================================================== */
	
	/** Resource path. */
	private String resourcePath;
	/** Key map. */
	private Properties languageMap;
	
	/**
	 * Creates the language manager with the specific language.
	 */
	private LanguageManager()
	{
		final String iso3language = Locale.getDefault().getISO3Language().toLowerCase();
		final String operatingSystem = 
			OSUtils.isWindows() ? OS_WINDOWS :
			OSUtils.isOSX()     ? OS_MACOS :
			OSUtils.isLinux()   ? OS_LINUX : 
			null;
		
		this.resourcePath = "gui/language/";
		this.languageMap = new Properties();
		
		loadIntoMap(DEFAULT_LANGUAGE, null);
		if (operatingSystem != null)
			loadIntoMap(DEFAULT_LANGUAGE, operatingSystem);
		loadIntoMap(iso3language, null);
		if (operatingSystem != null)
			loadIntoMap(iso3language, operatingSystem);
	}

	private void loadIntoMap(String iso3language, String operatingSystem)
	{
		Objects.requireNonNull(iso3language);
		
		InputStream in = LOADER.getResourceAsStream(
			resourcePath + 
			iso3language + 
			(operatingSystem != null ? "." + operatingSystem : "") + 
			".properties"
		);
		if (in != null)
		{
			try {
				languageMap.load(new InputStreamReader(in));
			} catch (IOException e) {
				SwingUtils.error("Could not load language file! Language " + iso3language);
			} finally {
				IOUtils.close(in);
			}
		}
	}
	
	/**
	 * Checks if a key is present in the language map.
	 * @param key the language key.
	 * @return true if so, false if not.
	 */
	public boolean hasKey(String key)
	{
		return languageMap.getProperty(key) != null;
	}
	
	/**
	 * Gets an integer value using a language key.
	 * @param key the language key.
	 * @param defaultValue the value to use if the key isn't found.
	 * @param modifier the function called after fetch to optionally modify the incoming value. Incoming value may be null!
	 * @return the desired value, or the provided default value if not found or parseable as an integer.
	 */
	public Integer getInteger(String key, Integer defaultValue, Function<Integer, Integer> modifier)
	{
		Integer out;
		String str = languageMap.getProperty(key);
		if (str == null)
		{
			out = defaultValue;
		}
		else
		{
			try {
				out = Integer.parseInt(str);
			} catch (NumberFormatException e) {
				out = defaultValue;
			}
		}
		
		return modifier != null ? modifier.apply(out) : out;
	}
	
	/**
	 * Gets an integer value using a language key.
	 * @param key the language key.
	 * @param modifier the function called after fetch to optionally modify the incoming value. Incoming value may be null!
	 * @return the desired value, after the modifier is called.
	 */
	public Integer getInteger(String key, Function<Integer, Integer> modifier)
	{
		return getInteger(key, null, modifier);
	}
	
	/**
	 * Gets an integer value using a language key.
	 * @param key the language key.
	 * @param defaultValue the value to use if the key isn't found.
	 * @return the desired value, or the provided default value if not found or parseable as an integer.
	 */
	public Integer getInteger(String key, Integer defaultValue)
	{
		return getInteger(key, defaultValue, null);
	}
	
	/**
	 * Gets an integer value using a language key.
	 * @param key the language key.
	 * @return the desired value, or null if not found.
	 */
	public Integer getInteger(String key)
	{
		return getInteger(key, null, null);
	}
	
	/**
	 * Gets text using a text key.
	 * @param key the language key.
	 * @param args string formatter arguments. 
	 * @return the desired text, or a macro if it is not found. 
	 */
	public String getText(String key, Object ... args)
	{
		String out = languageMap.getProperty(key);
		return String.format(out != null ? out : MISSING_TEXT, args);
	}
	
	/**
	 * Attempts to parse a mnemonic value from the results of a language lookup.
	 * @param key the language key.
	 * @return the corresponding {@link KeyEvent} VK value, or {@link KeyEvent#VK_UNDEFINED} if not found.
	 */
	public int getMnemonicValue(String key)
	{
		if (!hasKey(key))
			return KeyEvent.VK_UNDEFINED;
		
		char keyname = Character.toUpperCase(getText(key).charAt(0));
		try {
			Field f;
			if ((f = KeyEvent.class.getField("VK_" + keyname)) == null)
				return KeyEvent.VK_UNDEFINED;
			else
				return f.getInt(null);
		} catch (NoSuchFieldException | SecurityException e) {
			return KeyEvent.VK_UNDEFINED;
		} catch (IllegalArgumentException e) {
			return KeyEvent.VK_UNDEFINED;
		} catch (IllegalAccessException e) {
			return KeyEvent.VK_UNDEFINED;
		}
	}
	
	/**
	 * Attempts to parse a keystroke value from the results of a language lookup.
	 * @param key the language key.
	 * @param args string formatter arguments. 
	 * @return the corresponding keystroke, or null if not found.
	 */
	public KeyStroke getKeyStroke(String key, Object ... args)
	{
		if (!hasKey(key))
			return null;
		
		String value = getText(key, args);
		
		KeyStroke out;
		
		// Special handling for MacOS because of course
		if (OSUtils.isOSX())
		{
			if (value.contains("option"))
			{
				value = value.replace("option", "").trim();
				KeyStroke temp = KeyStroke.getKeyStroke(value);
				out = KeyStroke.getKeyStroke(temp.getKeyCode(), temp.getModifiers() | OPTION_MASK);
			}
			else
			{
				out = KeyStroke.getKeyStroke(value);
			}
		}
		else
		{
			out = KeyStroke.getKeyStroke(value);
		}
		
		return out;
	}
	
	/**
	 * Gets text wrapped in HTML tags using a text key.
	 * @param key the language key.
	 * @param args string formatter arguments. 
	 * @return the desired text, or a macro if it is not found. 
	 */
	public String getHTML(String key, Object ... args)
	{
		return "<html>" + getText(key, args) + "<html>";
	}
	
}