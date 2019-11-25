package net.mtrop.doomy.managers;

import com.blackrook.sql.SQLConnection;
import com.blackrook.sql.SQLRow;

import net.mtrop.doomy.DoomySetupException;

/**
 * Config manager singleton.
 * @author Matthew Tropiano
 */
public final class ConfigManager
{
	public static final String SETTING_IDGAMES_API_URL = "idgames.api.url";
	public static final String SETTING_IDGAMES_MIRROR_BASE_URL = "idgames.mirror.base.url";
	public static final String SETTING_IDGAMES_TIMEOUT_MILLIS = "idgames.timeout.millis";

	public static final String SETTING_WADARCHIVE_API_URL = "wadarchive.api.url";
	public static final String SETTING_WADARCHIVE_WADSEEKER_API_URL = "wadarchive.wadseeker.api.url";
	public static final String SETTING_WADARCHIVE_TIMEOUT_MILLIS = "wadarchive.timeout.millis";
	
	// ============================== QUERIES ================================
	
	private static final String QUERY_GET
		= "SELECT value FROM Config WHERE name = ?";
	private static final String QUERY_LIST
		= "SELECT name, value FROM Config WHERE name LIKE ? ORDER BY name ASC";
	private static final String QUERY_EXIST
		= "SELECT EXISTS (SELECT 1 FROM Config WHERE name = ?)";
	private static final String QUERY_SET
		= "UPDATE Config SET value = ? WHERE name = ?";
	private static final String QUERY_ADD
		= "INSERT INTO Config (name, value) VALUES (?, ?)";
	private static final String QUERY_REMOVE
		= "DELETE FROM Config WHERE name = ?";
	
	// =======================================================================
	
	// Singleton instance.
	private static ConfigManager INSTANCE;

	/**
	 * Initializes/Returns the singleton manager instance.
	 * @return the single manager.
	 * @throws DoomySetupException if the manager could not be set up.
	 */
	public static ConfigManager get()
	{
		if (INSTANCE == null)
			return INSTANCE = new ConfigManager(DatabaseManager.get());
		return INSTANCE;
	}

	// =======================================================================
	
	/** Open database connection. */
	private SQLConnection connection;
	
	private ConfigManager(DatabaseManager db)
	{
		this.connection = db.getConnection();
	}

	/**
	 * Checks if a config value exists by name.
	 * @param name the value name.
	 * @return true if so, false if not.
	 */
	public boolean containsValue(String name)
	{
		return connection.getRow(QUERY_EXIST, name).getBoolean(0);
	}
	
	/**
	 * Sets a config value by name.
	 * @param name the value name.
	 * @param value the value.
	 * @return true if added/updated, false if not.
	 */
	public boolean setValue(String name, String value)
	{
		if (containsValue(name))
			return connection.getUpdateResult(QUERY_SET, value, name).getRowCount() > 0;
		else
			return connection.getUpdateResult(QUERY_ADD, name, value).getRowCount() > 0;
	}
	
	/**
	 * Gets a config value by name.
	 * @param name the value name.
	 * @return the resultant value, or null if it doesn't exist.
	 */
	public String getValue(String name)
	{
		SQLRow row = connection.getRow(QUERY_GET, name);
		return row != null ? row.getString("value") : null;
	}

	/**
	 * Gets a config value by name, or a default value if it doesn't exist.
	 * @param name the value name.
	 * @param def the default value, if it doesn't exist.
	 * @return the resultant value, or the default if it doesn't exist.
	 */
	public String getValue(String name, String def)
	{
		String value = getValue(name); 
		return value != null ? value : def;
	}

	/**
	 * Removes a config value by name.
	 * @param name the value name.
	 * @return true if removed, false if not.
	 */
	public boolean removeValue(String name)
	{
		return connection.getUpdateResult(QUERY_REMOVE, name).getRowCount() > 0;
	}

	/**
	 * Gets a set of config values by name.
	 * @param containingPhrase the phrase to search for.
	 * @return the list of settings found.
	 */
	public ConfigSettingEntry[] getAllValues(String containingPhrase)
	{
		return connection.getResult(ConfigSettingEntry.class, QUERY_LIST, DatabaseManager.toSearchPhrase(containingPhrase));
	}

	/**
	 * Each config setting entry. 
	 */
	public static class ConfigSettingEntry
	{
		/** Setting name. */
		public String name;
		/** Setting value. */
		public String value;
	}

}
