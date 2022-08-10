package net.mtrop.doomy.commands.idgames;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.Deque;

import net.mtrop.doomy.DoomyCommand;
import net.mtrop.doomy.IOHandler;
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
		IdGamesAboutResponse response;
		try {
			response = IdGamesManager.get().about();
		} catch (SocketTimeoutException e) {
			handler.errln("ERROR: Call to idGames timed out.");
			return ERROR_SOCKET_TIMEOUT;
		} catch (IOException e) {
			handler.errln("ERROR: Could not read from idGames: " + e.getMessage());
			return ERROR_IO_ERROR;
		}
		
		handler.outln("Received from idGames:");
		handler.outln("Copyright: " + response.content.copyright);
		handler.outln("Credits: " + response.content.credits);
		handler.outln("Info: " + response.content.info);
		return ERROR_NONE;
	}

}
