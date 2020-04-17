package net.mtrop.doomy.managers;

import java.sql.SQLException;

import com.blackrook.sql.SQLConnection;
import com.blackrook.sql.SQLResult;
import com.blackrook.sql.SQLConnection.Transaction;
import com.blackrook.sql.SQLConnection.TransactionLevel;
import com.blackrook.sql.util.SQLRuntimeException;

import net.mtrop.doomy.DoomySetupException;

/**
 * IWAD singleton.
 * @author Matthew Tropiano
 */
public final class IWADManager
{
	// ============================== QUERIES ================================
	
	private static final String QUERY_GET_BY_ID
		= "SELECT * FROM IWADs WHERE id = ?"; 
	private static final String QUERY_GET_BY_NAME 
		= "SELECT * FROM IWADs WHERE name = ?"; 
	private static final String QUERY_LIST
		= "SELECT * FROM IWADs WHERE name LIKE ? ORDER BY name ASC";
	private static final String QUERY_EXIST
		= "SELECT EXISTS (SELECT 1 FROM IWADs WHERE name = ?)";
	private static final String QUERY_ADD 
		= "INSERT INTO IWADs (name, path) VALUES (?, ?)"; 
	private static final String QUERY_REMOVE
		= "DELETE FROM IWADs WHERE name = ?";
	private static final String QUERY_RENAME
		= "UPDATE IWADs SET name = ? WHERE name = ?";
	private static final String QUERY_UPDATE
		= "UPDATE IWADs SET path = ? WHERE name = ?";
	
	private static final String QUERY_REMOVE_PRESET
		= "DELETE FROM Presets WHERE iwadId = ?";

	// =======================================================================
	
	// Singleton instance.
	private static IWADManager INSTANCE;

	/**
	 * Initializes/Returns the singleton manager instance.
	 * @return the single manager.
	 * @throws DoomySetupException if the manager could not be set up.
	 */
	public static IWADManager get()
	{
		if (INSTANCE == null)
			return INSTANCE = new IWADManager(DatabaseManager.get());
		return INSTANCE;
	}

	// =======================================================================
	
	/** Open database connection. */
	private SQLConnection connection;
	
	private IWADManager(DatabaseManager db)
	{
		this.connection = db.getConnection();
	}

	/**
	 * Checks for an IWAD entry.
	 * @param name the name of the IWAD.
	 * @return an IWAD, or null if not found.
	 */
	public boolean containsIWAD(String name)
	{
		return connection.getRow(QUERY_EXIST, name).getBoolean(0);
	}
	
	/**
	 * Fetches an IWAD entry.
	 * @param id the id of the IWAD.
	 * @return an IWAD,or null if not found.
	 */
	public IWAD getIWAD(long id)
	{
		return connection.getRow(IWAD.class, QUERY_GET_BY_ID, id);
	}
	
	/**
	 * Fetches an IWAD entry.
	 * @param name the name of the IWAD.
	 * @return an IWAD, or null if not found.
	 */
	public IWAD getIWAD(String name)
	{
		return connection.getRow(IWAD.class, QUERY_GET_BY_NAME, name);
	}
	
	/**
	 * Gets a set of IWAD templates by name.
	 * @param containingPhrase the phrase to search for.
	 * @return the found templates.
	 */
	public IWAD[] getAllIWADs(String containingPhrase)
	{
		return connection.getResult(IWAD.class, QUERY_LIST, DatabaseManager.toSearchPhrase(containingPhrase));
	}
	
	/**
	 * Adds a new IWAD.
	 * @param name the name of the new IWAD.
	 * @param path the path to the IWAD.
	 * @return the id of the new IWAD created, or null if not created.
	 */
	public Long addIWAD(String name, String path)
	{
		SQLResult result = connection.getUpdateResult(QUERY_ADD, name, path);
		return result.getRowCount() > 0 ? (Long)result.getId() : null;
	}
	
	/**
	 * Removes an IWAD.
	 * @param name the name of the IWAD.
	 * @return true if removed, false if not.
	 */
	public boolean removeIWAD(String name)
	{
		IWAD iwad = getIWAD(name);
		if (iwad == null)
			return false;
		
		try (Transaction trn = connection.startTransaction(TransactionLevel.READ_UNCOMMITTED))
		{
			trn.getUpdateResult(QUERY_REMOVE_PRESET, iwad.id);
			trn.getUpdateResult(QUERY_REMOVE, iwad.name);
			trn.complete();
		} 
		catch (SQLException e) 
		{
			throw new SQLRuntimeException(e);
		}

		return true;
	}
	
	/**
	 * Renames an IWAD.
	 * @param oldName the name of the IWAD.
	 * @param newName the new name of the IWAD.
	 * @return true if renamed, false if not.
	 */
	public boolean renameIWAD(String oldName, String newName)
	{
		return connection.getUpdateResult(QUERY_RENAME, newName, oldName).getRowCount() > 0;
	}
	
	/**
	 * Changes an IWAD path.
	 * @param name the name of the IWAD.
	 * @param path the new path to the IWAD.
	 * @return true if updated, false if not.
	 */
	public boolean setIWADPath(String name, String path)
	{
		return connection.getUpdateResult(QUERY_UPDATE, path, name).getRowCount() > 0;
	}
	
	/**
	 * Each IWAD entry. 
	 */
	public static class IWAD
	{
		/** Entry id. */
		public long id;
		/** IWAD name. */
		public String name;
		/** IWAD path. */
		public String path;
	}

}
