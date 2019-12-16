package net.mtrop.doomy.managers;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;

import com.blackrook.sql.SQLConnection;
import com.blackrook.sql.SQLConnection.Transaction;
import com.blackrook.sql.SQLConnection.TransactionLevel;
import com.blackrook.sql.util.SQLRuntimeException;
import com.blackrook.sql.SQLResult;

import net.mtrop.doomy.DoomySetupException;

/**
 * Preset manager singleton.
 * @author Matthew Tropiano
 */
public final class PresetManager
{
	// ============================== QUERIES ================================
	
	private static final String QUERY_GET_BY_ID
		= "SELECT * FROM Presets WHERE id = ?"; 
	private static final String QUERY_GET_ITEMS_BY_ID
		= "SELECT * FROM PresetItems WHERE presetId = ? ORDER BY sort ASC";

	private static final String QUERY_GET_BY_NAME 
		= "SELECT * FROM Presets WHERE name = ?"; 
	private static final String QUERY_GET_BY_HASH
		= "SELECT * FROM Presets WHERE hash LIKE ?"; 

	private static final String QUERY_LIST_INFO = (new StringBuilder())
		.append("SELECT").append('\n')
			.append("p.id,").append('\n')
			.append("p.hash,").append('\n')
			.append("p.name,").append('\n')
			.append("e.name as engineName,").append('\n')
			.append("i.name as iwadName").append('\n')
		.append("FROM Presets p").append('\n')
		.append("LEFT JOIN Engines e ON p.engineId = e.id").append('\n')
		.append("LEFT JOIN IWADs i ON p.iwadId = i.id AND p.iwadId IS NOT NULL").append('\n')
	.toString();

	private static final String QUERY_SEARCH
		= QUERY_LIST_INFO + "WHERE p.name LIKE ? OR p.hash LIKE ? ORDER BY p.name";
	private static final String QUERY_LIST_NAME 
		= QUERY_LIST_INFO + "WHERE p.name LIKE ? ORDER BY p.name";
	private static final String QUERY_LIST_HASH
		= QUERY_LIST_INFO + "WHERE p.hash LIKE ? ORDER BY p.name";
	private static final String QUERY_LIST_WADS_BY_ID
		= "SELECT PresetItems.*, WADs.name as wadName FROM PresetItems LEFT JOIN WADs ON PresetItems.wadId = WADs.id WHERE PresetItems.presetId = ? ORDER BY sort ASC";

	private static final String QUERY_EXIST_BY_NAME
		= "SELECT EXISTS (SELECT 1 FROM Presets WHERE name = ?)"; 
	private static final String QUERY_COUNT_BY_HASH
		= "SELECT COUNT(*) FROM Presets WHERE hash LIKE ?"; 

	private static final String QUERY_ADD 
		= "INSERT INTO Presets (hash, name, engineId, iwadId) VALUES (?, ?, ?, ?)"; 
	private static final String QUERY_ADD_ITEM
		= "INSERT INTO PresetItems (presetId, wadId, sort) VALUES (?, ?, ?)";

	private static final String QUERY_UPDATE_NAME
		= "UPDATE Presets SET name = ? WHERE id = ?"; 

	private static final String QUERY_DELETE
		= "DELETE FROM Presets WHERE id = ?";
	private static final String QUERY_DELETE_ITEMS
		= "DELETE FROM PresetItems WHERE presetId = ?";

	// =======================================================================
	
	// Singleton instance.
	private static PresetManager INSTANCE;

	/**
	 * Initializes/Returns the singleton manager instance.
	 * @return the single manager.
	 * @throws DoomySetupException if the manager could not be set up.
	 */
	public static PresetManager get()
	{
		if (INSTANCE == null)
			return INSTANCE = new PresetManager(DatabaseManager.get());
		return INSTANCE;
	}

	/** Hex nybbles. */
	public static final String HEX_NYBBLES = "0123456789abcdef";
	/** The size of a long in bytes. */
	public static final int SIZEOF_LONG = Long.SIZE/Byte.SIZE;
	
	/**
	 * Converts a long to a series of bytes.
	 * @param l the long to convert.
	 * @param endianMode the endian mode of the bytes.
	 * @param out the output array.
	 * @param offset the offset into the array to write.
	 * @return the next array offset after the write. 
	 */
	private static int longToBytes(long l, boolean endianMode, byte[] out, int offset)
	{
		for (int x = endianMode ? 0 : SIZEOF_LONG-1; endianMode ? (x < SIZEOF_LONG) : (x >= 0); x += endianMode ? 1 : -1)
			out[offset + (endianMode ? x : SIZEOF_LONG-1 - x)] = (byte)((l & (0xFFL << Byte.SIZE*x)) >> Byte.SIZE*x); 
		return offset + SIZEOF_LONG;
	}

	// =======================================================================
	
	/** Open database connection. */
	private SQLConnection connection;
	
	private PresetManager(DatabaseManager db)
	{
		this.connection = db.getConnection();
	}

	private void getPresetWADIds(Preset preset) 
	{
		PresetItem[] items = connection.getResult(PresetItem.class, QUERY_GET_ITEMS_BY_ID, preset.id);
		preset.wadIds = new long[items.length];
		for (int i = 0; i < items.length; i++)
			preset.wadIds[i] = items[i].wadId;
	}

	private void getPresetWADNames(PresetInfo preset) 
	{
		SQLResult rows = connection.getResult(QUERY_LIST_WADS_BY_ID, preset.id);
		preset.wads = new String[rows.getRowCount()];
		for (int i = 0; i < rows.getRowCount(); i++)
			preset.wads[i] = rows.getRows().get(i).getString("wadName");
	}

	/**
	 * Calculates a preset hash.
	 * @param engineId the engine record id.
	 * @param iwadId the IWAD record id.
	 * @param wadIds the WAD record ids.
	 * @return a new hash string.
	 */
	public static String calculatePresetHash(long engineId, Long iwadId, long... wadIds) 
	{
		MessageDigest sha1;
		try {
			sha1 = MessageDigest.getInstance("SHA-1");
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException("NON-STANDARD JVM! JVM does not support SHA-1.");
		}
		
		byte[] buffer = new byte[SIZEOF_LONG];
		longToBytes(engineId, true, buffer, 0);
		sha1.update(buffer);
		longToBytes(iwadId != null ? iwadId : -1, true, buffer, 0);
		sha1.update(buffer);
		for (long id : wadIds)
		{
			longToBytes(id, true, buffer, 0);
			sha1.update(buffer);
		}
		
		StringBuilder hashsb = new StringBuilder();
		for (byte b : sha1.digest())
		{
			hashsb.append(HEX_NYBBLES.charAt((b & 0x0f0) >>> 4));
			hashsb.append(HEX_NYBBLES.charAt(b & 0x00f));
		}
		return hashsb.toString();
	}
	
	/**
	 * Checks if a preset exists by name.
	 * @param name the name to search for.
	 * @return true if found, false if not.
	 */
	public boolean containsPreset(String name)
	{
		return connection.getRow(QUERY_EXIST_BY_NAME, name).getBoolean(0);
	}

	/**
	 * Counts the presets that start with a hash sequence.
	 * @param startingHash the hash or hash prefix.
	 * @return the amount of records found.
	 */
	public int countPreset(String startingHash)
	{
		return connection.getRow(QUERY_COUNT_BY_HASH, startingHash.replace("%", "") + "%").getInt(0);
	}

	/**
	 * Gets a saved preset.
	 * If the directory for the preset data is not present, it is created.
	 * @param id the id to fetch.
	 * @return the found preset, or null if not found.
	 */
	public Preset getPreset(long id)
	{
		Preset out = connection.getRow(Preset.class, QUERY_GET_BY_ID, id);
		if (out == null)
			return null;

		getPresetWADIds(out);
		
		return out;
	}

	/**
	 * Gets a saved preset by name.
	 * @param name the name.
	 * @return the found presets, or null if not found.
	 */
	public Preset getPresetByName(String name)
	{
		Preset out = connection.getRow(Preset.class, QUERY_GET_BY_NAME, name);
		if (out == null)
			return null;

		getPresetWADIds(out);

		return out;
	}

	/**
	 * Gets a saved preset by partial or full hash. Can return more than one.
	 * @param startingHash the hash or hash prefix.
	 * @return the found presets.
	 */
	public Preset[] getPresetByHash(String startingHash)
	{
		Preset[] out = connection.getResult(Preset.class, QUERY_GET_BY_HASH, startingHash.replace("%", "") + "%");

		for (Preset p : out)
			getPresetWADIds(p);
		
		return out;
	}

	/**
	 * Searches for all presets.
	 * @param containingPhrase the name phrase or hash phrase.
	 * @return the found presets.
	 */
	public PresetInfo[] getPresetInfoByName(String containingPhrase)
	{
		PresetInfo[] out = connection.getResult(PresetInfo.class, QUERY_LIST_NAME, DatabaseManager.toSearchPhrase(containingPhrase));

		for (PresetInfo p : out)
			getPresetWADNames(p);

		return out;
	}

	/**
	 * Searches for all presets.
	 * @param containingPhrase the name phrase or hash phrase.
	 * @return the found presets.
	 */
	public PresetInfo[] getPresetInfoByHash(String containingPhrase)
	{
		PresetInfo[] out = connection.getResult(PresetInfo.class, QUERY_LIST_HASH, containingPhrase.replace("%", "") + "%");

		for (PresetInfo p : out)
			getPresetWADNames(p);

		return out;
	}

	/**
	 * Searches for all presets.
	 * @param containingPhrase the name phrase or hash phrase.
	 * @return the found presets.
	 */
	public PresetInfo[] getAllPresetsByNameOrHash(String containingPhrase)
	{
		PresetInfo[] out = connection.getResult(PresetInfo.class, QUERY_SEARCH, DatabaseManager.toSearchPhrase(containingPhrase), containingPhrase != null ? containingPhrase.replace("%", "") + "%" : "%");

		for (PresetInfo p : out)
			getPresetWADNames(p);

		return out;
	}

	/**
	 * Adds a preset and preset directory.
	 * @param name the name of the preset (can be null).
	 * @param engineId the id of the engine to use.
	 * @param iwadId the id of the IWAD to use (can be null).
	 * @param wadIds the wad ids to use (already assumed to be expanded out in least-to-most dependent order).
	 * @return the id of the created preset record.
	 */
	public Long addPreset(String name, long engineId, Long iwadId, long... wadIds)
	{
		String hash = calculatePresetHash(engineId, iwadId, wadIds);
		
		long addedId;
		try (Transaction trn = connection.startTransaction(TransactionLevel.READ_UNCOMMITTED))
		{
			SQLResult added = trn.getUpdateResult(QUERY_ADD, hash, name, engineId, iwadId);
			if (added.getRowCount() == 0)
				return null;
			addedId = added.getId();

			int sort = 0;
			for (long w : wadIds)
			{
				added = trn.getUpdateResult(QUERY_ADD_ITEM, addedId, w, sort);
				if (added.getRowCount() == 0)
					return null;
				sort += 10;
			}
			trn.complete();
		} 
		catch (SQLException e) 
		{
			throw new SQLRuntimeException(e);
		}
		
		return addedId;
	}

	/**
	 * Sets a preset's name.
	 * @param startingHash the hash or hash prefix.
	 * @param name the name of the preset.
	 * @return true if deleted, false if not.
	 */
	public boolean setPresetName(String startingHash, String name)
	{
		Preset[] preset = getPresetByHash(startingHash);
		if (preset.length != 1)
			return false;
		return connection.getUpdateResult(QUERY_UPDATE_NAME, name, preset[0].id).getRowCount() > 0;
	}
	
	private void deletePresetData(Preset preset) 
	{
		try (Transaction trn = connection.startTransaction(TransactionLevel.READ_UNCOMMITTED))
		{
			trn.getUpdateResult(QUERY_DELETE_ITEMS, preset.id);
			trn.getUpdateResult(QUERY_DELETE, preset.id);
			trn.complete();
		} 
		catch (SQLException e) 
		{
			throw new SQLRuntimeException(e);
		}
	}
	
	/**
	 * Deletes a preset by id (but not the directory contents).
	 * @param id the id of the preset.
	 * @return true if deleted, false if not.
	 */
	public boolean deletePresetById(long id)
	{
		Preset preset = getPreset(id);
		if (preset == null)
			return false;
		deletePresetData(preset);
		return true;
	}

	/**
	 * Deletes a preset by name (but not the directory contents).
	 * @param name the name of the preset.
	 * @return true if deleted, false if not.
	 */
	public boolean deletePresetByName(String name)
	{
		Preset preset = getPresetByName(name);
		if (preset == null)
			return false;
		deletePresetData(preset);
		return true;
	}

	/**
	 * Deletes a preset by starting hash (but not the directory contents).
	 * Only deletes if this refers to ONE hash.
	 * @param startingHash the Hash or hash prefix.
	 * @return true if deleted, false if not.
	 */
	public boolean deletePresetByHash(String startingHash)
	{
		Preset[] preset = getPresetByHash(startingHash);
		if (preset.length != 1)
			return false;
		deletePresetData(preset[0]);
		return true;
	}
	
	/**
	 * Each preset info. 
	 */
	public static class PresetInfo
	{
		/** Preset id. */
		public long id;
		/** Preset hash. */
		public String hash;
		/** Preset name. */
		public String name;
		/** Preset engine. */
		public String engineName;
		/** Preset IWAD. */
		public String iwadName;
		/** Preset WADs. */
		public String[] wads;
	}
	
	/**
	 * Each preset. 
	 */
	public static class Preset
	{
		/** Preset id. */
		public long id;
		/** Preset hash. */
		public String hash;
		/** Preset name. */
		public String name;

		// In hash
		
		/** Preset engine. */
		public long engineId;
		/** Preset IWAD. */
		public Long iwadId;
		/** Preset WADs (in the sort order). */
		public long[] wadIds;
	}

	public static class PresetItem
	{
		/** Preset item id. */
		public long id;
		/** Preset id. */
		public long presetId;
		/** WAD entry id. */
		public long wadId;
		/** Sort order. */
		public long sort;
	}
	
}
