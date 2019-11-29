package net.mtrop.doomy.commands.idgames.search;

import java.io.IOException;
import java.net.SocketTimeoutException;

import net.mtrop.doomy.commands.idgames.IdGamesCommonSearchCommand;
import net.mtrop.doomy.managers.IdGamesManager;
import net.mtrop.doomy.managers.IdGamesManager.IdGamesSearchResponse;
import net.mtrop.doomy.managers.IdGamesManager.SortDirection;
import net.mtrop.doomy.managers.IdGamesManager.SortType;

/**
 * A command that fetches the idGames "comic" info.
 * @author Matthew Tropiano
 */
public class IdGamesSearchFileCommand extends IdGamesCommonSearchCommand 
{

	@Override
	public IdGamesSearchResponse search(String query, int limit) throws SocketTimeoutException, IOException
	{
		return IdGamesManager.get().searchByFileName(query, SortType.FILENAME, SortDirection.ASC);
	}

}
