package net.mtrop.doomy.commands.idgames.search;

import java.io.IOException;
import java.net.SocketTimeoutException;

import net.mtrop.doomy.managers.IdGamesManager;
import net.mtrop.doomy.managers.IdGamesManager.IdGamesSearchResponse;
import net.mtrop.doomy.managers.IdGamesManager.SortDirection;
import net.mtrop.doomy.managers.IdGamesManager.SortType;

/**
 * An idGames search function for text file contents.
 * @author Matthew Tropiano
 */
public class IdGamesSearchTextFileCommand extends IdGamesCommonSearchCommand 
{

	@Override
	public IdGamesSearchResponse search(String query, int limit) throws SocketTimeoutException, IOException
	{
		return IdGamesManager.get().searchByTextFile(query, SortType.FILENAME, SortDirection.ASC);
	}

}
