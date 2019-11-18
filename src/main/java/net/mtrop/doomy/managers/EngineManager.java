package net.mtrop.doomy.managers;

import java.sql.SQLException;
import java.util.regex.Pattern;

import com.blackrook.sql.SQLConnection;
import com.blackrook.sql.SQLResult;
import com.blackrook.sql.SQLConnection.TransactionLevel;
import com.blackrook.sql.util.SQLRuntimeException;

import net.mtrop.doomy.DoomySetupException;
import net.mtrop.doomy.struct.ObjectUtils;

/**
 * Engine manager singleton.
 * @author Matthew Tropiano
 */
public final class EngineManager
{
	// ============================== QUERIES ================================
	
	private static final String QUERY_GET_BY_ID
		= "SELECT id, name, templateSource FROM Engines WHERE id = ?"; 
	private static final String QUERY_GET_BY_NAME 
		= "SELECT id, name, templateSource FROM Engines WHERE name = ?"; 
	private static final String QUERY_LIST
		= "SELECT id, name, templateSource FROM Engines WHERE name LIKE ? ORDER BY name ASC";
	private static final String QUERY_EXIST
		= "SELECT EXISTS (SELECT 1 FROM Engines WHERE name = ?)";
	private static final String QUERY_ADD 
		= "INSERT INTO Engine name, templateSource VALUES (?, ?)"; 
	private static final String QUERY_REMOVE
		= "DELETE FROM Engines WHERE name = ?";
	private static final String QUERY_RENAME
		= "UPDATE Engines SET name = ? WHERE name = ?";
	
	private static final String QUERY_COPY_TEMPLATE_SETTINGS 
		= "INSERT INTO EngineSettings (engineId, name, value) " 
			+ "SELECT ? AS engineId, EngineTemplateSettings.name, EngineTemplateSettings.value FROM EngineTemplateSettings "
				+ "LEFT JOIN EngineTemplates ON "
					+ "EngineTemplateSettings.engineId = EngineTemplates.id "
				+ "WHERE EngineTemplates.name = ?"; 

	private static final String QUERY_GET_SETTINGS_BY_ID
		= "SELECT engineId, name, value FROM EngineSettings WHERE engineId = ?";
	private static final String QUERY_GET_SETTINGS_BY_NAME
		= "SELECT engineId, name, value FROM EngineSettings LEFT JOIN Engine ON EngineSettings.engineId = Engine.id WHERE Engine.name = ?";

	
	// =======================================================================
	
	// Singleton instance.
	private static EngineManager INSTANCE;

	/**
	 * Initializes/Returns the singleton config manager instance.
	 * @return the single config manager.
	 * @throws DoomySetupException if the config manager could not be set up.
	 */
	public static EngineManager get()
	{
		if (INSTANCE == null)
			return INSTANCE = new EngineManager(DatabaseManager.get());
		return INSTANCE;
	}

	// =======================================================================
	
	/** Open database connection. */
	private SQLConnection connection;
	
	private EngineManager(DatabaseManager db)
	{
		this.connection = db.getConnection();
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
					case EngineSettings.SETTING_EXEPATH:
						out.exePath = entry.value;
						break;
					case EngineSettings.SETTING_DOSBOXPATH:
						out.dosboxPath = entry.value;
						break;
					case EngineSettings.SETTING_SETUPFILENAME:
						out.setupFileName = entry.value;
						break;
					case EngineSettings.SETTING_SERVERFILENAME:
						out.serverFileName = entry.value;
						break;
					case EngineSettings.SETTING_WORKDIRPATH:
						out.workingDirectoryPath = entry.value;
						break;
					case EngineSettings.SETTING_IWADSWITCH:
						out.iwadSwitch = entry.value;
						break;
					case EngineSettings.SETTING_FILESWITCH:
						out.fileSwitch = entry.value;
						break;
					case EngineSettings.SETTING_DEHSWITCH:
						out.dehackedSwitch = entry.value;
						break;
					case EngineSettings.SETTING_DEHLUMPSWITCH:
						out.dehlumpSwitch = entry.value;
						break;
					case EngineSettings.SETTING_SAVEDIRSWITCH:
						out.saveDirectorySwitch = entry.value;
						break;
					case EngineSettings.SETTING_SHOTDIRSWITCH:
						out.screenshotDirectorySwitch = entry.value;
						break;
					case EngineSettings.SETTING_SAVEPATTERN:
						out.savegameRegex = Pattern.compile(entry.value);
						break;
					case EngineSettings.SETTING_SHOTPATTERN:
						out.screenshotRegex = Pattern.compile(entry.value);
						break;
					case EngineSettings.SETTING_DEMOPATTERN:
						out.demoRegex = Pattern.compile(entry.value);
						break;
					case EngineSettings.SETTING_COMMANDLINE:
						out.commandLine = entry.value;
						break;
				}	
			}
		}
		return out;
	}
	
	/**
	 * Fetches a full engine entry (and its settings).
	 * @param id the id of the engine.
	 * @return an engine,or null if not found.
	 */
	public Engine getEngine(long id)
	{
		return connection.getRow(Engine.class, QUERY_GET_BY_ID, id);
	}
	
	/**
	 * Fetches a full engine entry (and its settings).
	 * @param name the name of the engine.
	 * @return an engine, or null if not found.
	 */
	public Engine getEngine(String name)
	{
		return connection.getRow(Engine.class, QUERY_GET_BY_NAME, name);
	}
	
	/**
	 * Adds a new engine.
	 * @param name the name of the new engine.
	 * @return the id of the new engine created, or null if not created.
	 */
	public Long addEngine(String name)
	{
		SQLResult result = connection.getUpdateResult(QUERY_ADD, name, null);
		if (result.getRowCount() > 0)
			return result.getId();
		else
			return null;
	}
	
	/**
	 * Adds a new engine by copying a template.
	 * @param name the name of the engine.
	 * @param templateName the template to copy from.
	 * @return the id of the new engine created, or null if not created.
	 */
	public Long addEngineUsingTemplate(String name, String templateName)
	{
		Long out = null;
		try (SQLConnection.Transaction trn = connection.startTransaction(TransactionLevel.READ_UNCOMMITTED))
		{
			out = trn.getUpdateResult(QUERY_ADD, name, templateName).getId();
			if (out == null)
				trn.abort();
			else if (trn.getUpdateResult(QUERY_COPY_TEMPLATE_SETTINGS, out, templateName).getRowCount() == 0)
				trn.abort();
			else
				trn.complete();
		} catch (SQLException e) {
			throw new SQLRuntimeException(e);
		}
		return out;
	}
	
	/**
	 * Fetches a full engine settings.
	 * @param id the id of the engine.
	 * @return an engine,or null if not found.
	 */
	public EngineSettings getEngineSettings(long id)
	{
		return createSettings(connection.getResult(EngineSettingEntry.class, QUERY_GET_SETTINGS_BY_ID, id));
	}
	
	/**
	 * Fetches a full engine settings.
	 * @param name the name of the engine.
	 * @return an engine, or null if not found.
	 */
	public EngineSettings getEngineSettings(String name)
	{
		return createSettings(connection.getResult(EngineSettingEntry.class, QUERY_GET_SETTINGS_BY_NAME, name));
	}
	
	/**
	 * Each engine entry. 
	 */
	public static class Engine
	{
		/** Entry id. */
		public long id;
		/** Engine name. */
		public String name;
		/** Engine's template source name. */
		public String templateSource;
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
		/** Engine executable path. */
		public static final String SETTING_EXEPATH = 		"exe.path";
		//**  OPT] If present (and not empty), starts DOSBox, mounts temp, and calls the EXE via it. */
		public static final String SETTING_DOSBOXPATH =		"dosbox.path";
		/** [OPT] If present, "engine setup" will run this executable in the engine's parent directory. */
		public static final String SETTING_SETUPFILENAME = 	"setup.exe.name";
		/** [OPT] If present, "run --server" will run this executable in the engine's parent directory. */
		public static final String SETTING_SERVERFILENAME = "server.exe.name";
		/** [OPT] If NOT present (or empty), set to either DOSBox dir or the EXE parent. */
		public static final String SETTING_WORKDIRPATH = 	"work.dir";
		/** [OPT] If present (and not empty), this engine requires an IWAD and this is the switch for loading it. */
		public static final String SETTING_IWADSWITCH = 	"switch.iwad";
		/** The switch to use for loading PWAD data (might be "-merge" if Chocolate Doom). */
		public static final String SETTING_FILESWITCH = 	"switch.file";
		/** [OPT] The switch to use for loading DeHackEd patches (blank for unsupported). */
		public static final String SETTING_DEHSWITCH = 		"switch.dehacked";
		/** [OPT] The switch to use for loading DeHackEd lumps (blank for unsupported). */
		public static final String SETTING_DEHLUMPSWITCH = 	"switch.dehlump";
		/** [OPT] If present (and not empty), this switch is used to map to preset directories for saves. */
		public static final String SETTING_SAVEDIRSWITCH = 	"switch.save.dir";
		/** [OPT] If present (and not empty), this switch is used to map to preset directories for screenshots. */
		public static final String SETTING_SHOTDIRSWITCH = 	"switch.screenshots.dir";
		/** [OPT] If present (and not empty), this regex pattern is used to find savegame files in the Engine Dir to move to the preset folder on exit, or preset to Engine dir. */
		public static final String SETTING_SAVEPATTERN = 	"regex.saves";
		/** [OPT] If present (and not empty), this regex pattern is used to find screenshot files in the Engine Dir to move to the preset folder on exit, or preset to Engine dir. */
		public static final String SETTING_SHOTPATTERN = 	"regex.screenshots";
		/** [OPT] If present (and not empty), this regex pattern is used to find demo files in the Engine Dir to move to the preset folder on exit, or preset to Engine dir. */
		public static final String SETTING_DEMOPATTERN = 	"regex.demos";
		/** [OPT] If present (and not empty), this command line is appended (but before the as-is passed-in options). */
		public static final String SETTING_COMMANDLINE = 	"cmdline";
		
		/** Engine id. */
		public long engineId;
		/** EXE Path. */
		public String exePath;
		/** DOSBOX Path. */
		public String dosboxPath;
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
		public Pattern savegameRegex;
		/** RegEx for screenshot cleanup. */
		public Pattern screenshotRegex;
		/** RegEx for demo cleanup. */
		public Pattern demoRegex;
		/** Extra command line options (passed in before literal options). */
		public String commandLine;
	}

}
