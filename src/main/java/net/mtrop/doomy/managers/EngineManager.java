package net.mtrop.doomy.managers;

import java.sql.SQLException;

import com.blackrook.sql.SQLConnection;
import com.blackrook.sql.SQLResult;
import com.blackrook.sql.SQLConnection.Transaction;
import com.blackrook.sql.SQLConnection.TransactionLevel;
import com.blackrook.sql.util.SQLRuntimeException;

import net.mtrop.doomy.DoomySetupException;

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
		= "INSERT INTO Engines (name, templateSource) VALUES (?, ?)"; 
	private static final String QUERY_REMOVE
		= "DELETE FROM Engines WHERE name = ?";
	private static final String QUERY_REMOVE_SETTINGS
		= "DELETE FROM EngineSettings WHERE engineId = ?";
	private static final String QUERY_RENAME
		= "UPDATE Engines SET name = ? WHERE name = ?";
	
	private static final String QUERY_COPY_SETTINGS 
		= "INSERT INTO EngineSettings (engineId, name, value) " 
			+ "SELECT ? AS engineId, EngineSettings.name, EngineSettings.value FROM EngineSettings "
				+ "LEFT JOIN Engines ON "
					+ "EngineSettings.engineId = Engines.id "
				+ "WHERE Engines.name = ?"; 

	private static final String QUERY_COPY_TEMPLATE_SETTINGS 
		= "INSERT INTO EngineSettings (engineId, name, value) " 
			+ "SELECT ? AS engineId, EngineTemplateSettings.name, EngineTemplateSettings.value FROM EngineTemplateSettings "
				+ "LEFT JOIN EngineTemplates ON "
					+ "EngineTemplateSettings.engineTemplateId = EngineTemplates.id "
				+ "WHERE EngineTemplates.name = ?"; 

	// =======================================================================
	
	// Singleton instance.
	private static EngineManager INSTANCE;

	/**
	 * Initializes/Returns the singleton manager instance.
	 * @return the single manager.
	 * @throws DoomySetupException if the manager could not be set up.
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

	/**
	 * Checks for an engine entry.
	 * @param name the name of the engine.
	 * @return an engine, or null if not found.
	 */
	public boolean containsEngine(String name)
	{
		return connection.getRow(QUERY_EXIST, name).getBoolean(0);
	}
	
	/**
	 * Fetches an engine entry.
	 * @param id the id of the engine.
	 * @return an engine,or null if not found.
	 */
	public Engine getEngine(long id)
	{
		return connection.getRow(Engine.class, QUERY_GET_BY_ID, id);
	}
	
	/**
	 * Fetches an engine entry.
	 * @param name the name of the engine.
	 * @return an engine, or null if not found.
	 */
	public Engine getEngine(String name)
	{
		return connection.getRow(Engine.class, QUERY_GET_BY_NAME, name);
	}
	
	/**
	 * Gets a set of engine templates by name.
	 * @param containingPhrase the phrase to search for.
	 * @return the found templates.
	 */
	public Engine[] getAllEngines(String containingPhrase)
	{
		return connection.getResult(Engine.class, QUERY_LIST, DatabaseManager.toSearchPhrase(containingPhrase));
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
	 * @param engineName the name of the engine to copy from.
	 * @return the id of the new engine created, or null if not created.
	 */
	public Long addEngineUsingEngine(String name, String engineName)
	{
		Long out = null;
		try (SQLConnection.Transaction trn = connection.startTransaction(TransactionLevel.READ_UNCOMMITTED))
		{
			out = trn.getUpdateResult(QUERY_ADD, name, null).getId();
			if (out == null)
				trn.abort();
			trn.getUpdateResult(QUERY_COPY_SETTINGS, out, engineName);
			trn.complete();
		} catch (SQLException e) {
			throw new SQLRuntimeException(e);
		}
		return out;
	}
	
	/**
	 * Adds a new engine by copying a template.
	 * @param name the name of the engine.
	 * @param templateName the name of the template to copy from.
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
			trn.getUpdateResult(QUERY_COPY_TEMPLATE_SETTINGS, out, templateName);
			trn.complete();
		} catch (SQLException e) {
			throw new SQLRuntimeException(e);
		}
		return out;
	}
	
	/**
	 * Removes an engine.
	 * @param name the name of the engine.
	 * @return true if removed, false if not.
	 */
	public boolean removeEngine(String name)
	{
		Engine engine = getEngine(name);
		if (engine == null)
			return false;
		
		try (Transaction trn = connection.startTransaction(TransactionLevel.READ_UNCOMMITTED))
		{
			trn.getUpdateResult(QUERY_REMOVE_SETTINGS, engine.id);
			trn.getUpdateResult(QUERY_REMOVE, engine.name);
			trn.complete();
		} 
		catch (SQLException e) 
		{
			throw new SQLRuntimeException(e);
		}
		
		return true;
	}
	
	/**
	 * Renames an engine.
	 * @param oldName the name of the engine.
	 * @param newName the new name of the engine.
	 * @return true if renamed, false if not.
	 */
	public boolean renameEngine(String oldName, String newName)
	{
		return connection.getUpdateResult(QUERY_RENAME, newName, oldName).getRowCount() > 0;
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

}
