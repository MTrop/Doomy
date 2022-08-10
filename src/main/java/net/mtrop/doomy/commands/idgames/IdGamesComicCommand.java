package net.mtrop.doomy.commands.idgames;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.Deque;

import net.mtrop.doomy.DoomyCommand;
import net.mtrop.doomy.IOHandler;
import net.mtrop.doomy.managers.IdGamesManager;
import net.mtrop.doomy.managers.IdGamesManager.IdGamesComicResponse;

/**
 * A command that fetches the idGames "comic" info.
 * @author Matthew Tropiano
 */
public class IdGamesComicCommand implements DoomyCommand
{

	@Override
	public void init(Deque<String> args) throws BadArgumentException
	{
		// Do nothing.
	}

	@Override
	public int call(IOHandler handler)
	{
		return execute(handler);
	}

	/**
	 * Executes this command.
	 * @param handler the handler to use for I/O.
	 * @return the return code from running the command.
	 */
	public static int execute(IOHandler handler)
	{
		IdGamesComicResponse response;
		try {
			response = IdGamesManager.get().comic();
		} catch (SocketTimeoutException e) {
			handler.errln("ERROR: Call to idGames timed out.");
			return ERROR_SOCKET_TIMEOUT;
		} catch (IOException e) {
			handler.errln("ERROR: Could not read from idGames: " + e.getMessage());
			return ERROR_IO_ERROR;
		}
		
		handler.outln("Received from idGames:");
		handler.outln("Quote: " + response.content.quote);
		handler.outln("Order: " + response.content.order);
		return ERROR_NONE;
	}

}
