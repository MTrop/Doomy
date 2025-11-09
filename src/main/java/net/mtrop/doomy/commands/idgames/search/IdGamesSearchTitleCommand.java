/*******************************************************************************
 * Copyright (c) 2019-2025 Matt Tropiano
 * This program and the accompanying materials are made available under 
 * the terms of the MIT License, which accompanies this distribution.
 ******************************************************************************/
package net.mtrop.doomy.commands.idgames.search;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;

import net.mtrop.doomy.IOHandler;
import net.mtrop.doomy.managers.IdGamesManager;
import net.mtrop.doomy.managers.IdGamesManager.IdGamesSearchResponse;
import net.mtrop.doomy.managers.IdGamesManager.SortDirection;
import net.mtrop.doomy.managers.IdGamesManager.SortType;

/**
 * An idGames search function for titles.
 * @author Matthew Tropiano
 */
public class IdGamesSearchTitleCommand extends IdGamesCommonSearchCommand 
{

	@Override
	public IdGamesSearchResponse search(IOHandler handler, String query, int limit) throws SocketTimeoutException, IOException
	{
		IdGamesSearchResponse response;
		try {
			response = IdGamesManager.get().searchByTitle(query, SortType.FILENAME, SortDirection.ASC).get();
		} catch (CancellationException e) {
			handler.errln("ERROR: Service call was cancelled.");
			return null;
		} catch (InterruptedException e) {
			handler.errln("ERROR: Service was interrupted.");
			return null;
		} catch (ExecutionException e) {
			handler.errln("ERROR: Service error: " + e.getCause().getLocalizedMessage());
			return null;
		}
		
		return response;
	}

}
