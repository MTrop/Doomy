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
	// ============================== QUERIES ================================
	
	private static final String QUERY_GET_VALUE = "SELECT value FROM Config WHERE name = ?";
	private static final String QUERY_SET_VALUE = "UPDATE Config SET value = ? WHERE name = ?";
	private static final String QUERY_ADD_VALUE = "INSERT INTO Config (name, value) VALUES (?, ?)";
	private static final String QUERY_EXIST_VALUE = "SELECT EXISTS (SELECT 1 FROM Config WHERE name = ?)";
	private static final String QUERY_CLEAR_VALUE = "DELETE FROM Config WHERE name = ?";
	
	// =======================================================================
	
	// Singleton instance.
	private static ConfigManager INSTANCE;

	/**
	 * Initializes/Returns the singleton config manager instance.
	 * @return the single config manager.
	 * @throws DoomySetupException if the config manager could not be set up.
	 */
	public static ConfigManager get()
	{
		if (INSTANCE == null)
			return INSTANCE = new ConfigManager(DatabaseManager.get());
		return INSTANCE;
	}
	
	/** Open database connection. */
	private SQLConnection connection;
	
	private ConfigManager(DatabaseManager db)
	{
		this.connection = db.getConnection();
	}

	/**
	 * Clears a config value by name.
	 * @param name the value name.
	 * @return true if removed, false if not.
	 */
	public boolean clearValue(String name)
	{
		return connection.getUpdateResult(QUERY_CLEAR_VALUE, name).getRowCount() > 0;
	}
	
	/**
	 * Sets a config value by name.
	 * @param name the value name.
	 * @param value the value.
	 * @return true if added/updated, false if not.
	 */
	public boolean setValue(String name, String value)
	{
		if (connection.getRow(QUERY_EXIST_VALUE, name).getBoolean(0))
			return connection.getUpdateResult(QUERY_SET_VALUE, value, name).getRowCount() > 0;
		else
			return connection.getUpdateResult(QUERY_ADD_VALUE, name, value).getRowCount() > 0;
	}
	
	/**
	 * Gets a config value by name.
	 * @param name the value name.
	 * @return the resultant value, or null if it doesn't exist.
	 */
	public String getValue(String name)
	{
		SQLRow row = connection.getRow(QUERY_GET_VALUE, name);
		return row != null ? row.getString("value") : null;
	}
	
}
