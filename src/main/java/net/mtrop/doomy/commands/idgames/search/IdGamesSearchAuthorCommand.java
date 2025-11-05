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
 * An idGames search function for authors.
 * @author Matthew Tropiano
 */
public class IdGamesSearchAuthorCommand extends IdGamesCommonSearchCommand 
{

	@Override
	public IdGamesSearchResponse search(IOHandler handler, String query, int limit) throws SocketTimeoutException, IOException
	{
		IdGamesSearchResponse response;
		try {
			response = IdGamesManager.get().searchByAuthor(query, SortType.FILENAME, SortDirection.ASC).get();
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
