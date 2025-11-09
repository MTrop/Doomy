/*******************************************************************************
 * Copyright (c) 2019-2025 Matt Tropiano
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doomy.managers;

import java.util.regex.Pattern;

import com.blackrook.sql.SQLConnection;
import com.blackrook.sql.SQLRow;

import net.mtrop.doomy.DoomySetupException;
import net.mtrop.doomy.managers.EngineManager.Engine;
import net.mtrop.doomy.struct.SingletonProvider;
import net.mtrop.doomy.struct.util.ObjectUtils;

/**
 * Engine config manager singleton.
 * @author Matthew Tropiano
 */
public final class EngineConfigManager
{
	/** Engine executable path. */
	public static final String SETTING_EXEPATH = 			"exe.path";
	/** [OPT] If present (and not empty), starts DOSBox, mounts temp, and calls the EXE via it. */
	public static final String SETTING_DOSBOXPATH =			"dosbox.path";
	/** [OPT] If present (and not empty), run DOSBox with these additional command line switches. */
	public static final String SETTING_DOSBOXCOMMANDLINE =	"dosbox.cmdline";
	/** [OPT] If present, "engine setup" will run this executable in the engine's parent directory. */
	public static final String SETTING_SETUPFILENAME = 		"setup.exe.name";
	/** [OPT] If present, "run --server" will run this executable in the engine's parent directory. */
	public static final String SETTING_SERVERFILENAME =		"server.exe.name";
	/** [OPT] If NOT present (or empty), set to either DOSBox dir or the EXE parent. */
	public static final String SETTING_WORKDIRPATH = 		"work.dir";
	/** The switch to use for loading PWAD data (might be "-merge" if Chocolate Doom). */
	public static final String SETTING_FILESWITCH = 		"switch.file";
	/** [OPT] If present (and not empty), this engine requires an IWAD and this is the switch for loading it. */
	public static final String SETTING_IWADSWITCH = 		"switch.iwad";
	/** [OPT] The switch to use for loading DeHackEd patches (blank for unsupported). */
	public static final String SETTING_DEHSWITCH = 			"switch.dehacked";
	/** [OPT] The switch to use for loading DeHackEd lumps (blank for unsupported). */
	public static final String SETTING_DEHLUMPSWITCH = 		"switch.dehlump";
	/** [OPT] If present (and not empty), this switch is used to map to preset directories for saves. */
	public static final String SETTING_SAVEDIRSWITCH = 		"switch.save.dir";
	/** [OPT] If present (and not empty), this switch is used to map to preset directories for screenshots. */
	public static final String SETTING_SHOTDIRSWITCH = 		"switch.screenshots.dir";
	/** [OPT] If present (and not empty), this regex pattern is used to find savegame files in the Engine Dir to move to the preset folder on exit, or preset to Engine dir. */
	public static final String SETTING_SAVEPATTERN = 		"regex.saves";
	/** [OPT] If present (and not empty), this regex pattern is used to find screenshot files in the Engine Dir to move to the preset folder on exit, or preset to Engine dir. */
	public static final String SETTING_SHOTPATTERN = 		"regex.screenshots";
	/** [OPT] If present (and not empty), this regex pattern is used to find demo files in the Engine Dir to move to the preset folder on exit, or preset to Engine dir. */
	public static final String SETTING_DEMOPATTERN = 		"regex.demos";
	/** [OPT] If present (and not empty), this command line is appended (but before the as-is passed-in options). */
	public static final String SETTING_COMMANDLINE = 		"cmdline";
	
	// ============================== QUERIES ================================
	
	private static final String QUERY_SETTING_GET
		= "SELECT value FROM EngineSettings WHERE engineId = ? AND name = ?";
	private static final String QUERY_SETTING_LIST
		= "SELECT engineId, name, value FROM EngineSettings WHERE engineId = ? AND name LIKE ? ORDER BY name ASC";
	private static final String QUERY_SETTING_EXIST
		= "SELECT EXISTS (SELECT 1 FROM EngineSettings WHERE engineId = ? AND name = ?)";
	private static final String QUERY_SETTING_SET
		= "UPDATE EngineSettings SET value = ? WHERE engineId = ? AND name = ?";
	private static final String QUERY_SETTING_ADD
		= "INSERT INTO EngineSettings (engineId, name, value) VALUES (?, ?, ?)";
	private static final String QUERY_SETTING_REMOVE
		= "DELETE FROM EngineSettings WHERE engineId = ? AND name = ?";

	
	// =======================================================================
	
	// Singleton instance.
	private static final SingletonProvider<EngineConfigManager> INSTANCE = new SingletonProvider<>(() -> new EngineConfigManager());

	/**
	 * Initializes/Returns the singleton manager instance.
	 * @return the single manager.
	 * @throws DoomySetupException if the manager could not be set up.
	 */
	public static EngineConfigManager get()
	{
		return INSTANCE.get();
	}

	// =======================================================================
	
	/** Open database connection. */
	private SQLConnection connection;
	/** Engine manager. */
	private EngineManager engineManager;
	
	private EngineConfigManager()
	{
		this.connection = DatabaseManager.get().getConnection();
		this.engineManager = EngineManager.get();
	}

	private EngineSettings createSettings(EngineSettingEntry[] settings)
	{
		if (settings == null)
			return null;
		
		EngineSettings out = new EngineSettings();
		for (EngineSettingEntry entry : settings)
		{
			if (!ObjectUtils.isEmpty(entry.value))
			{
				switch (entry.name)
				{
					default:
						break;
					case SETTING_EXEPATH:
						out.exePath = entry.value;
						break;
					case SETTING_DOSBOXPATH:
						out.dosboxPath = entry.value;
						break;
					case SETTING_DOSBOXCOMMANDLINE:
						out.dosboxCommandLine = entry.value;
						break;
					case SETTING_SETUPFILENAME:
						out.setupFileName = entry.value;
						break;
					case SETTING_SERVERFILENAME:
						out.serverFileName = entry.value;
						break;
					case SETTING_WORKDIRPATH:
						out.workingDirectoryPath = entry.value;
						break;
					case SETTING_IWADSWITCH:
						out.iwadSwitch = entry.value;
						break;
					case SETTING_FILESWITCH:
						out.fileSwitch = entry.value;
						break;
					case SETTING_DEHSWITCH:
						out.dehackedSwitch = entry.value;
						break;
					case SETTING_DEHLUMPSWITCH:
						out.dehlumpSwitch = entry.value;
						break;
					case SETTING_SAVEDIRSWITCH:
						out.saveDirectorySwitch = entry.value;
						break;
					case SETTING_SHOTDIRSWITCH:
						out.screenshotDirectorySwitch = entry.value;
						break;
					case SETTING_SAVEPATTERN:
						out.saveGameRegex = Pattern.compile(entry.value, Pattern.CASE_INSENSITIVE);
						break;
					case SETTING_SHOTPATTERN:
						out.screenshotRegex = Pattern.compile(entry.value, Pattern.CASE_INSENSITIVE);
						break;
					case SETTING_DEMOPATTERN:
						out.demoRegex = Pattern.compile(entry.value, Pattern.CASE_INSENSITIVE);
						break;
					case SETTING_COMMANDLINE:
						out.commandLine = entry.value;
						break;
				}	
			}
		}
		return out;
	}
	
	/**
	 * @param name the template name.
	 * @return the id or null.
	 */
	private Long getEngineId(String name)
	{
		Engine e = engineManager.getEngine(name);
		return e != null ? e.id : null;
	}
	
	/**
	 * Fetches a full engine settings.
	 * @param id the id of the engine.
	 * @return an engine's settings, or null if engine not found.
	 */
	public EngineSettings getEngineSettings(long id)
	{
		return createSettings(connection.getResult(EngineSettingEntry.class, QUERY_SETTING_LIST, id, "%"));
	}
	
	/**
	 * Fetches a full engine settings.
	 * @param name the name of the engine.
	 * @return an engine's settings, or null if engine not found.
	 */
	public EngineSettings getEngineSettings(String name)
	{
		Long id = getEngineId(name);
		if (id == null)
			return null;
		return getEngineSettings(id);
	}
	
	/**
	 * Checks if a engine config value exists by name.
	 * @param engineName the engine name.
	 * @param name the setting name.
	 * @return true if so, false if not.
	 */
	public boolean containsSetting(String engineName, String name)
	{
		Long id = getEngineId(engineName);
		if (id == null)
			return false;
		return connection.getRow(QUERY_SETTING_EXIST, id, name).getBoolean(0);
	}
	
	/**
	 * Sets a engine config value by name.
	 * @param engineName the engine name.
	 * @param name the setting name.
	 * @param value the value.
	 * @return true if added/updated, false if not.
	 */
	public boolean setSetting(String engineName, String name, String value)
	{
		Long id = getEngineId(engineName);
		if (id == null)
			return false;
		if (containsSetting(engineName, name))
		{
			if (ObjectUtils.isEmpty(value))
				return removeSetting(engineName, name);
			else
				return connection.getUpdateResult(QUERY_SETTING_SET, value, id, name).getRowCount() > 0;
		}
		else if (!ObjectUtils.isEmpty(value))
		{
			return connection.getUpdateResult(QUERY_SETTING_ADD, id, name, value).getRowCount() > 0;
		}
		else
		{
			return true;
		}
	}
	
	/**
	 * Sets a full engine's settings.
	 * @param name the name of the engine.
	 * @param settings the engine's settings.
	 * @return true if successful, false if not.
	 */
	public boolean setEngineSettings(String name, EngineSettings settings)
	{
		return setSetting(name, SETTING_EXEPATH, settings.exePath)
			&& setSetting(name, SETTING_DOSBOXPATH, settings.dosboxPath)
			&& setSetting(name, SETTING_DOSBOXCOMMANDLINE, settings.dosboxCommandLine)
			&& setSetting(name, SETTING_SETUPFILENAME, settings.setupFileName)
			&& setSetting(name, SETTING_SERVERFILENAME, settings.serverFileName)
			&& setSetting(name, SETTING_WORKDIRPATH, settings.workingDirectoryPath)
			&& setSetting(name, SETTING_IWADSWITCH, settings.iwadSwitch)
			&& setSetting(name, SETTING_FILESWITCH, settings.fileSwitch)
			&& setSetting(name, SETTING_DEHSWITCH, settings.dehackedSwitch)
			&& setSetting(name, SETTING_DEHLUMPSWITCH, settings.dehlumpSwitch)
			&& setSetting(name, SETTING_SAVEDIRSWITCH, settings.saveDirectorySwitch)
			&& setSetting(name, SETTING_SHOTDIRSWITCH, settings.screenshotDirectorySwitch)
			&& setSetting(name, SETTING_SAVEPATTERN, settings.saveGameRegex != null ? settings.saveGameRegex.pattern() : "")
			&& setSetting(name, SETTING_SHOTPATTERN, settings.screenshotRegex != null ? settings.screenshotRegex.pattern() : "")
			&& setSetting(name, SETTING_DEMOPATTERN, settings.demoRegex != null ? settings.demoRegex.pattern() : "")
			&& setSetting(name, SETTING_COMMANDLINE, settings.commandLine)
		;
	}
	
	/**
	 * Gets a engine config value by name.
	 * @param engineName the engine name.
	 * @param name the setting name.
	 * @return the resultant value, or null if it doesn't exist.
	 */
	public String getSetting(String engineName, String name)
	{
		Long id = getEngineId(engineName);
		if (id == null)
			return null;
		SQLRow row = connection.getRow(QUERY_SETTING_GET, id, name);
		return row != null ? row.getString("value") : null;
	}

	/**
	 * Removes a engine config value by name.
	 * @param engineName the engine name.
	 * @param name the setting name.
	 * @return true if removed, false if not.
	 */
	public boolean removeSetting(String engineName, String name)
	{
		Long id = getEngineId(engineName);
		if (id == null)
			return false;
		return connection.getUpdateResult(QUERY_SETTING_REMOVE, id, name).getRowCount() > 0;
	}

	/**
	 * Gets a set of engine config values by name.
	 * @param engineName the engine name.
	 * @param containingPhrase the phrase to search for.
	 * @return the list of settings found, or null if the template name is not found.
	 */
	public EngineSettingEntry[] getAllSettings(String engineName, String containingPhrase)
	{
		Long id = getEngineId(engineName);
		if (id == null)
			return null;
		return connection.getResult(EngineSettingEntry.class, QUERY_SETTING_LIST, id, DatabaseManager.toSearchPhrase(containingPhrase));
	}
	
	/**
	 * Each engine config setting entry. 
	 */
	public static class EngineSettingEntry
	{
		/** Engine id. */
		public long id;
		/** Setting name. */
		public String name;
		/** Setting value. */
		public String value;
	}

	/**
	 * An object mapping of known engine setting values.
	 * Settings that are empty or null are coerced to null.
	 * Each of its fields could be null. Check for it!
	 */
	public static class EngineSettings
	{
		/** Engine id. */
		public long engineId;
		/** EXE Path. */
		public String exePath;
		/** DOSBOX Path. */
		public String dosboxPath;
		/** DOSBOX command line. */
		public String dosboxCommandLine;
		/** SETUP EXE name. */
		public String setupFileName;
		/** Server EXE name. */
		public String serverFileName;
		/** Working directory override for engine. */
		public String workingDirectoryPath;
		/** IWAD switch. */
		public String iwadSwitch;
		/** File switch. */
		public String fileSwitch;
		/** Dehacked switch. */
		public String dehackedSwitch;
		/** Use DEHACKED lump switch. */
		public String dehlumpSwitch;
		/** Savegame directory switch. */
		public String saveDirectorySwitch;
		/** Screenshot directory switch. */
		public String screenshotDirectorySwitch;
		/** RegEx for savegame cleanup. */
		public Pattern saveGameRegex;
		/** RegEx for screenshot cleanup. */
		public Pattern screenshotRegex;
		/** RegEx for demo cleanup. */
		public Pattern demoRegex;
		/** Extra command line options (passed in before literal options). */
		public String commandLine;

		public EngineSettings()
		{
			// defaults
		}
		
		public EngineSettings(EngineSettings original)
		{
			this.commandLine = original.commandLine;
			this.dehackedSwitch = original.dehackedSwitch;
			this.dehlumpSwitch = original.dehlumpSwitch;
			this.demoRegex = original.demoRegex;
			this.dosboxCommandLine = original.dosboxCommandLine;
			this.dosboxPath = original.dosboxPath;
			this.engineId = original.engineId;
			this.exePath = original.exePath;
			this.fileSwitch = original.fileSwitch;
			this.iwadSwitch = original.iwadSwitch;
			this.saveDirectorySwitch = original.saveDirectorySwitch;
			this.saveGameRegex = original.saveGameRegex;
			this.screenshotDirectorySwitch = original.screenshotDirectorySwitch;
			this.screenshotRegex = original.screenshotRegex;
			this.serverFileName = original.serverFileName;
			this.setupFileName = original.setupFileName;
			this.workingDirectoryPath = original.workingDirectoryPath;
		}
		
	}

}
