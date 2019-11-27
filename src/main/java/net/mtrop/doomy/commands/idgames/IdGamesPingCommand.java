package net.mtrop.doomy.commands.idgames;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;
import java.net.SocketTimeoutException;
import java.util.Deque;

import net.mtrop.doomy.DoomyCommand;
import net.mtrop.doomy.managers.IdGamesManager;

/**
 * A command that pings the idGames Archive service.
 * @author Matthew Tropiano
 */
public class IdGamesPingCommand implements DoomyCommand
{

	@Override
	public void init(Deque<String> args) throws BadArgumentException
	{
		// Do nothing.
	}

	@Override
	public int call(PrintStream out, PrintStream err, BufferedReader in)
	{
		try {
			IdGamesManager.get().ping();
		} catch (SocketTimeoutException e) {
			err.println("ERROR: Call to idGames timed out.");
			return ERROR_SOCKET_TIMEOUT;
		} catch (IOException e) {
			err.println("ERROR: Could not read from idGames: " + e.getMessage());
			return ERROR_IO_ERROR;
		}
		
		out.println("Successfully pinged idGames.");
		return ERROR_NONE;
	}

}
