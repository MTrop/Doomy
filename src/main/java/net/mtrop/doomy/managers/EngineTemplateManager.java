/*******************************************************************************
 * Copyright (c) 2019-2025 Matt Tropiano
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doomy.managers;

import java.sql.SQLException;

import com.blackrook.sql.SQLConnection;
import com.blackrook.sql.SQLConnection.Transaction;
import com.blackrook.sql.SQLConnection.TransactionLevel;
import com.blackrook.sql.SQLResult;
import com.blackrook.sql.util.SQLRuntimeException;

import net.mtrop.doomy.DoomySetupException;
import net.mtrop.doomy.struct.SingletonProvider;

/**
 * Engine template manager singleton.
 * @author Matthew Tropiano
 */
public final class EngineTemplateManager
{
	// ============================== QUERIES ================================
	
	private static final String QUERY_GET_BY_ID
		= "SELECT id, name FROM EngineTemplates WHERE id = ?"; 
	private static final String QUERY_GET_BY_NAME 
		= "SELECT id, name FROM EngineTemplates WHERE name = ?";
	private static final String QUERY_LIST
		= "SELECT id, name FROM EngineTemplates WHERE name LIKE ? ORDER BY name ASC";
	private static final String QUERY_EXIST
		= "SELECT EXISTS (SELECT 1 FROM EngineTemplates WHERE name = ?)";
	private static final String QUERY_ADD
		= "INSERT INTO EngineTemplates (name) VALUES (?)"; 
	private static final String QUERY_REMOVE
		= "DELETE FROM EngineTemplates WHERE name = ?"; 
	private static final String QUERY_REMOVE_SETTINGS
		= "DELETE FROM EngineTemplateSettings WHERE engineTemplateId = ?"; 
	
	private static final String QUERY_COPY_SETTINGS 
		= "INSERT INTO EngineTemplateSettings (engineTemplateId, name, value) " 
			+ "SELECT ? AS engineTemplateId, EngineTemplateSettings.name, EngineTemplateSettings.value FROM EngineTemplateSettings "
				+ "LEFT JOIN EngineTemplates ON "
					+ "EngineTemplateSettings.engineTemplateId = EngineTemplates.id "
				+ "WHERE EngineTemplates.name = ?"; 
	
	// =======================================================================
	
	// Singleton instance.
	private static final SingletonProvider<EngineTemplateManager> INSTANCE = new SingletonProvider<>(() -> new EngineTemplateManager());

	/**
	 * Initializes/Returns the singleton manager instance.
	 * @return the single manager.
	 * @throws DoomySetupException if the manager could not be set up.
	 */
	public static EngineTemplateManager get()
	{
		return INSTANCE.get();	
	}

	// =======================================================================
	
	/** Open database connection. */
	private SQLConnection connection;
	
	private EngineTemplateManager()
	{
		this.connection = DatabaseManager.get().getConnection();
	}

	/**
	 * Fetches a full engine template entry (and its settings).
	 * @param id the id of the engine.
	 * @return an engine,or null if not found.
	 */
	public EngineTemplate getTemplate(long id)
	{
		return connection.getRow(EngineTemplate.class, QUERY_GET_BY_ID, id);
	}
	
	/**
	 * Fetches a full engine template entry (and its settings).
	 * @param name the name of the engine.
	 * @return an engine, or null if not found.
	 */
	public EngineTemplate getTemplate(String name)
	{
		return connection.getRow(EngineTemplate.class, QUERY_GET_BY_NAME, name);
	}
	
	/**
	 * Gets a set of engine templates by name.
	 * @param containingPhrase the phrase to search for.
	 * @return the found templates.
	 */
	public EngineTemplate[] getAllTemplates(String containingPhrase)
	{
		return connection.getResult(EngineTemplate.class, QUERY_LIST, DatabaseManager.toSearchPhrase(containingPhrase));
	}

	/**
	 * Adds a new engine template.
	 * @param name the name of the new engine.
	 * @return the id of the new engine created, or null if not created.
	 */
	public Long addTemplate(String name)
	{
		SQLResult result = connection.getUpdateResult(QUERY_ADD, name);
		if (result.getRowCount() > 0)
			return (Long)result.getId();
		else
			return null;
	}
	
	/**
	 * Adds a new engine template from an existing one.
	 * @param name the name of the new engine template.
	 * @param sourceName the name of the existing template.
	 * @return the id of the new engine created, or null if not created.
	 */
	public Long addTemplateFrom(String name, String sourceName)
	{
		Long out = null;
		try (SQLConnection.Transaction trn = connection.startTransaction(TransactionLevel.READ_UNCOMMITTED))
		{
			out = (Long)trn.getUpdateResult(QUERY_ADD, name).getId();
			if (out == null)
				trn.abort();
			else if (trn.getUpdateResult(QUERY_COPY_SETTINGS, out, sourceName).getRowCount() == 0)
				trn.abort();
			else
				trn.complete();
		} 
		catch (SQLException e) 
		{
			throw new SQLRuntimeException(e);
		}
		return out;
	}
	
	/**
	 * Removes an engine template.
	 * @param name the name of the template.
	 * @return true if deleted, false otherwise.
	 */
	public boolean removeTemplate(String name)
	{
		EngineTemplate template = getTemplate(name);
		if (template == null)
			return false;
		
		try (Transaction trn = connection.startTransaction(TransactionLevel.READ_UNCOMMITTED))
		{
			trn.getUpdateResult(QUERY_REMOVE_SETTINGS, template.id);
			trn.getUpdateResult(QUERY_REMOVE, template.name);
			trn.complete();
		} 
		catch (SQLException e) 
		{
			throw new SQLRuntimeException(e);
		}
		
		return true;
	}
	
	/**
	 * Checks if an engine template exists.
	 * @param name the name of the template.
	 * @return true if found, false otherwise.
	 */
	public boolean containsTemplate(String name)
	{
		return connection.getRow(QUERY_EXIST, name).getBoolean(0);
	}
	
	/**
	 * Each engine entry. 
	 */
	public static class EngineTemplate
	{
		/** Entry id. */
		public long id;
		/** Engine name. */
		public String name;
	}

}
