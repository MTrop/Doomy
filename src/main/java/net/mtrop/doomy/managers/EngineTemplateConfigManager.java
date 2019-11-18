package net.mtrop.doomy.managers;

import com.blackrook.sql.SQLConnection;
import com.blackrook.sql.SQLRow;

import net.mtrop.doomy.DoomySetupException;
import net.mtrop.doomy.managers.EngineTemplateManager.EngineTemplate;

/**
 * Engine template config manager singleton.
 * @author Matthew Tropiano
 */
public final class EngineTemplateConfigManager
{
	// ============================== QUERIES ================================
	
	private static final String QUERY_GET
		= "SELECT value FROM EngineTemplateSettings WHERE engineTemplateId = ? AND name = ?";
	private static final String QUERY_LIST
		= "SELECT engineTemplateId, name, value FROM EngineTemplateSettings WHERE engineTemplateId = ? AND name LIKE ? ORDER BY name ASC";
	private static final String QUERY_EXIST
		= "SELECT EXISTS (SELECT 1 FROM EngineTemplateSettings WHERE engineTemplateId = ? AND name = ?)";
	private static final String QUERY_SET
		= "UPDATE EngineTemplateSettings SET value = ? WHERE engineTemplateId = ? AND name = ?";
	private static final String QUERY_ADD
		= "INSERT INTO EngineTemplateSettings (engineTemplateId, name, value) VALUES (?, ?, ?)";
	private static final String QUERY_REMOVE
		= "DELETE FROM EngineTemplateSettings WHERE engineTemplateId = ? AND name = ?";
	
	// =======================================================================
	
	// Singleton instance.
	private static EngineTemplateConfigManager INSTANCE;

	/**
	 * Initializes/Returns the singleton config manager instance.
	 * @return the single config manager.
	 * @throws DoomySetupException if the config manager could not be set up.
	 */
	public static EngineTemplateConfigManager get()
	{
		if (INSTANCE == null)
			return INSTANCE = new EngineTemplateConfigManager(DatabaseManager.get(), EngineTemplateManager.get());
		return INSTANCE;
	}

	// =======================================================================
	
	/** Open database connection. */
	private SQLConnection connection;
	/** Engine template manager. */
	private EngineTemplateManager templateManager;
	
	private EngineTemplateConfigManager(DatabaseManager db, EngineTemplateManager templateManager)
	{
		this.connection = db.getConnection();
		this.templateManager = templateManager;
	}

	/**
	 * @param name the template name.
	 * @return the id or null.
	 */
	private Long getTemplateId(String name)
	{
		EngineTemplate t = templateManager.getTemplate(name);
		return t != null ? t.id : null;
	}
	
	/**
	 * Checks if a engine template config value exists by name.
	 * @param templateName the template name.
	 * @param name the setting name.
	 * @return true if so, false if not.
	 */
	public boolean containsValue(String templateName, String name)
	{
		Long id = getTemplateId(templateName);
		if (id == null)
			return false;
		return connection.getRow(QUERY_EXIST, id, name).getBoolean(0);
	}
	
	/**
	 * Sets a engine template config value by name.
	 * @param templateName the template name.
	 * @param name the setting name.
	 * @param value the value.
	 * @return true if added/updated, false if not.
	 */
	public boolean setValue(String templateName, String name, String value)
	{
		Long id = getTemplateId(templateName);
		if (id == null)
			return false;
		if (containsValue(templateName, name))
			return connection.getUpdateResult(QUERY_SET, id, value, name).getRowCount() > 0;
		else
			return connection.getUpdateResult(QUERY_ADD, id, name, value).getRowCount() > 0;
	}
	
	/**
	 * Gets a engine template config value by name.
	 * @param templateName the template name.
	 * @param name the setting name.
	 * @return the resultant value, or null if it doesn't exist.
	 */
	public String getValue(String templateName, String name)
	{
		Long id = getTemplateId(templateName);
		if (id == null)
			return null;
		SQLRow row = connection.getRow(QUERY_GET, id, name);
		return row != null ? row.getString("value") : null;
	}

	/**
	 * Removes a engine template config value by name.
	 * @param templateName the template name.
	 * @param name the setting name.
	 * @return true if removed, false if not.
	 */
	public boolean removeValue(String templateName, String name)
	{
		Long id = getTemplateId(templateName);
		if (id == null)
			return false;
		return connection.getUpdateResult(QUERY_REMOVE, id, name).getRowCount() > 0;
	}

	/**
	 * Gets a set of engine template config values by name.
	 * @param templateName the template name.
	 * @param containingPhrase the phrase to search for.
	 * @return the list of settings found, or null if the template name is not found.
	 */
	public EngineTemplateSettingEntry[] getAllValues(String templateName, String containingPhrase)
	{
		Long id = getTemplateId(templateName);
		if (id == null)
			return null;
		return connection.getResult(EngineTemplateSettingEntry.class, QUERY_LIST, id, DatabaseManager.toSearchPhrase(containingPhrase));
	}

	/**
	 * Each engine engine template config template setting entry. 
	 */
	public static class EngineTemplateSettingEntry
	{
		/** Engine id. */
		public long engineTemplateId;
		/** Setting name. */
		public String name;
		/** Setting value. */
		public String value;
	}

}
