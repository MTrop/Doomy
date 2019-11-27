package net.mtrop.doomy.commands.idgames;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;
import java.net.SocketTimeoutException;
import java.util.Deque;

import net.mtrop.doomy.DoomyCommand;
import net.mtrop.doomy.managers.IdGamesManager;
import net.mtrop.doomy.managers.IdGamesManager.IdGamesAboutResponse;

/**
 * A command that fetches the idGames "about" info.
 * @author Matthew Tropiano
 */
public class IdGamesAboutCommand implements DoomyCommand
{

	@Override
	public void init(Deque<String> args) throws BadArgumentException
	{
		// Do nothing.
	}

	@Override
	public int call(PrintStream out, PrintStream err, BufferedReader in)
	{
		IdGamesAboutResponse response;
		try {
			response = IdGamesManager.get().about();
		} catch (SocketTimeoutException e) {
			err.println("ERROR: Call to idGames timed out.");
			return ERROR_SOCKET_TIMEOUT;
		} catch (IOException e) {
			err.println("ERROR: Could not read from idGames: " + e.getMessage());
			return ERROR_IO_ERROR;
		}
		
		out.println("Received from idGames:");
		out.println("Copyright: " + response.content.copyright);
		out.println("Credits: " + response.content.credits);
		out.println("Info: " + response.content.info);
		return ERROR_NONE;
	}

}
