package net.mtrop.doomy.commands.idgames;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.Deque;

import net.mtrop.doomy.DoomyCommand;
import net.mtrop.doomy.IOHandler;
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
	public int call(IOHandler handler)
	{
		try {
			IdGamesManager.get().ping();
		} catch (SocketTimeoutException e) {
			handler.errln("ERROR: Call to idGames timed out.");
			return ERROR_SOCKET_TIMEOUT;
		} catch (IOException e) {
			handler.errln("ERROR: Could not read from idGames: " + e.getMessage());
			return ERROR_IO_ERROR;
		}
		
		handler.outln("Successfully pinged idGames.");
		return ERROR_NONE;
	}

}
