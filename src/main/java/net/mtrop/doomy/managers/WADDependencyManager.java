package net.mtrop.doomy.managers;

import java.sql.SQLException;
import java.util.Deque;
import java.util.LinkedList;

import com.blackrook.sql.SQLConnection;
import com.blackrook.sql.SQLResult;
import com.blackrook.sql.SQLRow;
import com.blackrook.sql.SQLConnection.Transaction;
import com.blackrook.sql.SQLConnection.TransactionLevel;
import com.blackrook.sql.util.SQLRuntimeException;

import net.mtrop.doomy.DoomySetupException;
import net.mtrop.doomy.managers.EngineManager.Engine;
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
		  "WHERE wd.id = ?"; 
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
		
		SQLResult result = connection.getResult(QUERY_LIST, name);
		String[] out = new String[result.getRowCount()];
		int i = 0;
		for (SQLRow row : result)
			out[i++] = row.getString(0);
		return out;
	}
	
	/**
	 * Gets all the names of the WADs that another WAD depends on.
	 * @param name the name of the dependent WAD.
	 * @return an array of all WAD dependencies (names).
	 * @see WADManager#getWAD(String)
	 */
	public String[] getFullDependencies(String name)
	{
		Deque<String> toLookup = new LinkedList<String>();
		Deque<String> accumulator = new LinkedList<String>();
		
		while (!toLookup.isEmpty())
		{
			for (String dep : getDependencies(toLookup.pollFirst()))
			{
				// Sequential search - not proud of it, but I don't see the list growing very large, anyway.
				if (!accumulator.contains(dep))
					toLookup.add(dep);
				accumulator.addFirst(dep);
			}
		}
		
		String[] out = new String[accumulator.size()];
		accumulator.toArray(out);
		return out;
	}
	
	// TODO: Finish.
	
}
