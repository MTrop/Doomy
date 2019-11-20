package net.mtrop.doomy.managers;

import java.sql.SQLException;

import com.blackrook.sql.SQLConnection;
import com.blackrook.sql.SQLResult;
import com.blackrook.sql.SQLConnection.Transaction;
import com.blackrook.sql.SQLConnection.TransactionLevel;
import com.blackrook.sql.util.SQLRuntimeException;

import net.mtrop.doomy.DoomySetupException;

/**
 * WAD manager singleton.
 * @author Matthew Tropiano
 */
public final class WADManager
{
	// ============================== QUERIES ================================
	
	private static final String QUERY_GET_BY_ID
		= "SELECT * FROM WADs WHERE id = ?"; 
	private static final String QUERY_GET_BY_NAME 
		= "SELECT * FROM WADs WHERE name = ?"; 
	private static final String QUERY_LIST
		= "SELECT * FROM WADs WHERE name LIKE ? ORDER BY name ASC";
	private static final String QUERY_EXIST
		= "SELECT EXISTS (SELECT 1 FROM WADs WHERE name = ?)";
	private static final String QUERY_ADD 
		= "INSERT INTO WADs (name, path, sourceURL) VALUES (?, ?, ?)"; 
	private static final String QUERY_REMOVE
		= "DELETE FROM WADs WHERE name = ?";
	private static final String QUERY_RENAME
		= "UPDATE WADs SET name = ? WHERE name = ?";
	private static final String QUERY_UPDATE
		= "UPDATE WADs SET path = ? WHERE name = ?";
	private static final String QUERY_UPDATE_URL
		= "UPDATE WADs SET url = ? WHERE name = ?";
	
	// =======================================================================
	
	// Singleton instance.
	private static WADManager INSTANCE;

	/**
	 * Initializes/Returns the singleton manager instance.
	 * @return the single manager.
	 * @throws DoomySetupException if the manager could not be set up.
	 */
	public static WADManager get()
	{
		if (INSTANCE == null)
			return INSTANCE = new WADManager(DatabaseManager.get());
		return INSTANCE;
	}

	// =======================================================================
	
	/** Open database connection. */
	private SQLConnection connection;
	
	private WADManager(DatabaseManager db)
	{
		this.connection = db.getConnection();
	}

	/**
	 * Checks for a WAD entry.
	 * @param name the name of the WAD.
	 * @return a WAD, or null if not found.
	 */
	public boolean containsWAD(String name)
	{
		return connection.getRow(QUERY_EXIST, name).getBoolean(0);
	}
	
	/**
	 * Fetches a WAD entry.
	 * @param id the id of the WAD.
	 * @return a WAD,or null if not found.
	 */
	public WAD getWAD(long id)
	{
		return connection.getRow(WAD.class, QUERY_GET_BY_ID, id);
	}
	
	/**
	 * Fetches a WAD entry.
	 * @param name the name of the WAD.
	 * @return a WAD, or null if not found.
	 */
	public WAD getWAD(String name)
	{
		return connection.getRow(WAD.class, QUERY_GET_BY_NAME, name);
	}
	
	/**
	 * Gets a set of WAD templates by name.
	 * @param containingPhrase the phrase to search for.
	 * @return the found templates.
	 */
	public WAD[] getAllWADs(String containingPhrase)
	{
		return connection.getResult(WAD.class, QUERY_LIST, DatabaseManager.toSearchPhrase(containingPhrase));
	}
	
	/**
	 * Adds a new WAD.
	 * @param name the name of the new WAD.
	 * @param path the path to the WAD.
	 * @return the id of the new WAD created, or null if not created.
	 */
	public Long addWAD(String name, String path)
	{
		return addWAD(name, path, null);
	}
	
	/**
	 * Adds a new WAD.
	 * @param name the name of the new WAD.
	 * @param path the path to the WAD.
	 * @param sourceURL the source URL of the WAD.
	 * @return the id of the new WAD created, or null if not created.
	 */
	public Long addWAD(String name, String path, String sourceURL)
	{
		SQLResult result = connection.getUpdateResult(QUERY_ADD, name, path, sourceURL);
		return result.getRowCount() > 0 ? result.getId() : null;
	}
	
	/**
	 * Removes a WAD.
	 * @param name the name of the WAD.
	 * @return true if removed, false if not.
	 */
	public boolean removeWAD(String name)
	{
		WAD wad = getWAD(name);
		if (wad == null)
			return false;
		
		try (Transaction trn = connection.startTransaction(TransactionLevel.READ_UNCOMMITTED))
		{
			// TODO: Remove other dependent data.
			trn.getUpdateResult(QUERY_REMOVE, wad.name);
			trn.complete();
		} 
		catch (SQLException e) 
		{
			throw new SQLRuntimeException(e);
		}

		return connection.getUpdateResult(QUERY_REMOVE, name).getRowCount() > 0;
	}
	
	/**
	 * Renames a WAD.
	 * @param oldName the name of the WAD.
	 * @param newName the new name of the WAD.
	 * @return true if renamed, false if not.
	 */
	public boolean renameWAD(String oldName, String newName)
	{
		return connection.getUpdateResult(QUERY_RENAME, newName, oldName).getRowCount() > 0;
	}
	
	/**
	 * Changes a WAD path.
	 * @param name the name of the WAD.
	 * @param path the new path to the WAD.
	 * @return true if updated, false if not.
	 */
	public boolean setWADPath(String name, String path)
	{
		return connection.getUpdateResult(QUERY_UPDATE, path, name).getRowCount() > 0;
	}
	
	/**
	 * Changes a WAD's source URL.
	 * @param name the name of the WAD.
	 * @param url the new download URL (can be null).
	 * @return true if updated, false if not.
	 */
	public boolean setWADSourceURL(String name, String url)
	{
		return connection.getUpdateResult(QUERY_UPDATE_URL, url, name).getRowCount() > 0;
	}
	
	/**
	 * Each WAD entry. 
	 */
	public static class WAD
	{
		/** Entry id. */
		public long id;
		/** WAD name. */
		public String name;
		/** WAD path. */
		public String path;
		/** Source URL. */
		public String sourceURL;
	}

}
