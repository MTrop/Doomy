/*******************************************************************************
 * Copyright (c) 2019-2025 Matt Tropiano
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doomy.managers;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;

import com.blackrook.sql.SQLConnection;
import com.blackrook.sql.SQLConnector;

import net.mtrop.doomy.DoomyEnvironment;
import net.mtrop.doomy.DoomySetupException;
import net.mtrop.doomy.struct.SingletonProvider;
import net.mtrop.doomy.struct.util.FileUtils;
import net.mtrop.doomy.struct.util.IOUtils;
import net.mtrop.doomy.struct.util.ObjectUtils;

/**
 * Database manager singleton.
 * @author Matthew Tropiano
 */
public final class DatabaseManager
{
	// Singleton instance.
	private static final SingletonProvider<DatabaseManager> INSTANCE = new SingletonProvider<>(() -> initializeDatabase());
	
	private static final String QUERY_META_EXISTS
		= "SELECT 1 WHERE EXISTS (SELECT name FROM sqlite_master WHERE type='table' AND name='Meta')"; 
	private static final String QUERY_SET_TABLEVERSION
		= "UPDATE Meta SET value = ? WHERE name = 'table.version'"; 
	private static final String QUERY_GET_TABLEVERSION
		= "SELECT value FROM Meta WHERE name = 'table.version'"; 

	// Query resources.
	private static final String[] INIT_QUERIES = {
		"sql/v1/init/0001-create-meta.sql",
		"sql/v1/init/0002-create-config.sql",
		"sql/v1/init/0003-create-engines.sql",
		"sql/v1/init/0004-create-enginesettings.sql",
		"sql/v1/init/0005-create-iwads.sql",
		"sql/v1/init/0006-create-wads.sql",
		"sql/v1/init/0007-create-waddata.sql",
		"sql/v1/init/0008-create-preset.sql",
		"sql/v1/init/0009-create-presetitem.sql",
		"sql/v1/init/0010-insert-meta-defaults.sql",
		"sql/v1/init/0011-insert-config-defaults.sql",
		"sql/v1/init/0012-create-enginetemplates.sql",
		"sql/v1/init/0013-create-enginetemplatessettings.sql",
		"sql/v1/init/0014-insert-enginetemplates.sql",
		"sql/v1/init/0015-insert-enginetemplatessettings.sql"
	};

	// Initializes/creates the connector.
	private static SQLConnector createConnector(File databaseFile)
	{
		return new SQLConnector("org.sqlite.JDBC", "jdbc:sqlite:" + databaseFile.getPath().replaceAll("\\\\", "/"));	
	}

	// Initializes/creates the database.
	private static void initDatabaseFile(File databaseFile)
	{
		if (!FileUtils.createPathForFile(databaseFile))
			throw new DoomySetupException("Could not create database: " + databaseFile.toString());
		
		try 
		{
			createConnector(databaseFile).getConnectionAnd((conn) ->
			{
				int tableVersion;
				if (conn.getRow(QUERY_META_EXISTS) != null)
					tableVersion = conn.getRow(QUERY_GET_TABLEVERSION).getInt(0);
				else 
					tableVersion = 0;
				
				for (; tableVersion < INIT_QUERIES.length; tableVersion++)
				{
					String resource = INIT_QUERIES[tableVersion];
					try (InputStream in = IOUtils.openResource(resource))
					{
						conn.getUpdateResult(IOUtils.getTextualContents(in, "UTF-8"));
					} 
					catch (IOException e) 
					{
						throw new DoomySetupException("Internal error: Could not open resource: " + resource, e);
					}
				}
				conn.getUpdateResult(QUERY_SET_TABLEVERSION, String.valueOf(INIT_QUERIES.length));
			});
		} 
		catch (SQLException e) 
		{
			throw new DoomySetupException("Internal error!", e);
		}
		
	}

	/**
	 * Converts an input search phrase to a query phrase (for LIKE).
	 * @param phrase the input phrase.
	 * @return the converted phrase.
	 */
	public static String toSearchPhrase(String phrase)
	{
		if (ObjectUtils.isEmpty(phrase))
			return "%";
		else if (phrase.indexOf('*') < 0)
			return "%" + phrase + "%";
		else
			return phrase.replace('*', '%');
	}
	
	/**
	 * @return true if the database exists, false if not.
	 */
	public static boolean databaseExists()
	{
		return (new File(DoomyEnvironment.getDatabasePath())).exists();
	}
	
	/**
	 * Initializes/Returns the singleton database instance.
	 * @return the single database manager.
	 * @throws DoomySetupException if the database could not be set up.
	 */
	private static DatabaseManager initializeDatabase()
	{
		File dbFile = null;
		try {
			dbFile = new File(DoomyEnvironment.getDatabasePath());
			initDatabaseFile(dbFile);
			return new DatabaseManager(dbFile);
		} catch (SQLException e) {
			if (dbFile.exists())
				dbFile.delete();
			throw new DoomySetupException("Could not set up database: " + e.getMessage(), e);
		}
	}
	
	public static DatabaseManager get()
	{
		return INSTANCE.get();
	}
	
	// =======================================================================

	/** Open database connection. */
	private SQLConnection connection;
	
	private DatabaseManager(File databaseFile) throws SQLException
	{
		SQLConnector connector = createConnector(databaseFile);
		this.connection = connector.getConnection();
	}
	
	/**
	 * @return the open connection.
	 */
	SQLConnection getConnection() 
	{
		return connection;
	}
	
}
