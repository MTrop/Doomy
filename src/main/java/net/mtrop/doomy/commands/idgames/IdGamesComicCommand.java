package net.mtrop.doomy.commands.idgames;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;
import java.net.SocketTimeoutException;
import java.util.Deque;

import net.mtrop.doomy.DoomyCommand;
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
	public int call(PrintStream out, PrintStream err, BufferedReader in)
	{
		IdGamesComicResponse response;
		try {
			response = IdGamesManager.get().comic();
		} catch (SocketTimeoutException e) {
			err.println("ERROR: Call to idGames timed out.");
			return ERROR_SOCKET_TIMEOUT;
		} catch (IOException e) {
			err.println("ERROR: Could not read from idGames: " + e.getMessage());
			return ERROR_IO_ERROR;
		}
		
		out.println("Received from idGames:");
		out.println("Quote: " + response.content.quote);
		out.println("Order: " + response.content.order);
		return ERROR_NONE;
	}

}
