package net.mtrop.doomy.managers;

import java.util.Deque;
import java.util.LinkedList;

import com.blackrook.sql.SQLConnection;
import com.blackrook.sql.SQLResult;
import com.blackrook.sql.SQLRow;

import net.mtrop.doomy.DoomySetupException;
import net.mtrop.doomy.managers.WADManager.WAD;

/**
 * WAD dependency manager singleton.
 * @author Matthew Tropiano
 */
public final class WADDependencyManager
{
	// ============================== QUERIES ================================
	
	private static final String QUERY_LIST 
		= "SELECT n.name FROM WADDependencies wd " +
		  "LEFT JOIN WADs n ON wd.needsWadId = n.id " + 
		  "WHERE wd.wadId = ?"; 
	private static final String QUERY_EXIST
		= "SELECT EXISTS (SELECT 1 FROM WADDependencies WHERE wadId = ? AND needsWadId = ?)";
	private static final String QUERY_ADD 
		= "INSERT INTO WADDependencies (wadId, needsWadId) VALUES (?, ?)"; 
	private static final String QUERY_REMOVE
		= "DELETE FROM WADDependencies WHERE wadId = ? AND needsWadId = ?";
	private static final String QUERY_CLEAR
		= "DELETE FROM WADDependencies WHERE wadId = ?";

	private static final String[] NO_NAMES = new String[0];

	// =======================================================================
	
	// Singleton instance.
	private static WADDependencyManager INSTANCE;

	/**
	 * Initializes/Returns the singleton manager instance.
	 * @return the single manager.
	 * @throws DoomySetupException if the manager could not be set up.
	 */
	public static WADDependencyManager get()
	{
		if (INSTANCE == null)
			return INSTANCE = new WADDependencyManager(DatabaseManager.get(), WADManager.get());
		return INSTANCE;
	}

	// =======================================================================
	
	/** Open database connection. */
	private SQLConnection connection;
	/** WAD Manager. */
	private WADManager wadManager;
	
	private WADDependencyManager(DatabaseManager db, WADManager wadManager)
	{
		this.connection = db.getConnection();
		this.wadManager = wadManager;
	}

	/**
	 * @param name the template name.
	 * @return the id or null.
	 */
	private Long getWADId(String name)
	{
		WAD w = wadManager.getWAD(name);
		return w != null ? w.id : null;
	}
	
	/**
	 * Gets the names of the WADs that another WAD immediately depends on.
	 * @param name the name of the dependent WAD.
	 * @return an array of all WAD dependencies (names).
	 * @see WADManager#getWAD(String)
	 */
	public String[] getDependencies(String name)
	{
		Long id = getWADId(name);
		if (id == null)
			return NO_NAMES;
		
		SQLResult result = connection.getResult(QUERY_LIST, id);
		String[] out = new String[result.getRowCount()];
		int i = 0;
		for (SQLRow row : result)
			out[i++] = row.getString(0);
		return out;
	}

	private void addFullDependencies(String name, Deque<String> accumulator)
	{
		String[] deps = getDependencies(name);
		for (int i = deps.length - 1; i >= 0; i++)
		{
			String dep = deps[i];
			if (!accumulator.contains(dep))
				accumulator.addFirst(dep);
			addFullDependencies(dep, accumulator);
		}
	}
	
	/**
	 * Gets all the names of the WADs that another WAD depends on.
	 * @param name the name of the dependent WAD.
	 * @return an array of all WAD dependencies (names).
	 * @see WADManager#getWAD(String)
	 */
	public String[] getFullDependencies(String... name)
	{
		Deque<String> accumulator = new LinkedList<String>();
		for (int i = name.length - 1; i >= 0; i--)
		{
			String dep = name[i];
			if (!accumulator.contains(dep))
				accumulator.addFirst(dep);
			addFullDependencies(dep, accumulator);
		}
		String[] out = new String[accumulator.size()];
		accumulator.toArray(out);
		return out;
	}
	
	/**
	 * Checks if a dependency to a WAD exists.
	 * @param wad the WAD that has dependencies.
	 * @param dependency the WAD it would depend on.
	 * @return true if the dependency exists, false otherwise.
	 */
	public boolean containsDependency(String wad, String dependency)
	{
		Long wadId = getWADId(wad);
		if (wadId == null)
			return false;
		Long needsWadId = getWADId(dependency);
		if (needsWadId == null)
			return false;
		
		return connection.getRow(QUERY_EXIST, wadId, needsWadId).getBoolean(0);
	}
	
	/**
	 * Adds a dependency to a WAD.
	 * @param wad the WAD to add a dependency to.
	 * @param dependency the WAD it depends on.
	 * @return true if both are valid and added, false otherwise.
	 */
	public boolean addDependency(String wad, String dependency)
	{
		Long wadId = getWADId(wad);
		if (wadId == null)
			return false;
		Long needsWadId = getWADId(dependency);
		if (needsWadId == null)
			return false;
		
		return connection.getUpdateResult(QUERY_ADD, wadId, needsWadId).getRowCount() > 0;
	}
	
	/**
	 * Removes a dependency from a WAD.
	 * @param wad the WAD to remove a dependency from.
	 * @param dependency the WAD it depends on.
	 * @return true if both are valid and removed, false otherwise.
	 */
	public boolean removeDependency(String wad, String dependency)
	{
		Long wadId = getWADId(wad);
		if (wadId == null)
			return false;
		Long needsWadId = getWADId(dependency);
		if (needsWadId == null)
			return false;
		
		return connection.getUpdateResult(QUERY_REMOVE, wadId, needsWadId).getRowCount() > 0;
	}
	
	/**
	 * Clears all dependencies from a WAD.
	 * @param wad the WAD to remove all dependencies from.
	 * @return true if both are valid and removed, false otherwise.
	 */
	public boolean clearDependency(String wad)
	{
		Long wadId = getWADId(wad);
		if (wadId == null)
			return false;
		
		return connection.getUpdateResult(QUERY_CLEAR, wadId).getRowCount() > 0;
	}
	
}
