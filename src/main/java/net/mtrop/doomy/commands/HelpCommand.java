package net.mtrop.doomy.commands;

import java.util.Deque;

import net.mtrop.doomy.DoomyCommand;
import net.mtrop.doomy.DoomyCommon;
import net.mtrop.doomy.IOHandler;

/**
 * A command that prints the help output and exits.
 * @author Matthew Tropiano
 */
public class HelpCommand implements DoomyCommand
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
		DoomyCommon.help(handler, null);
		return ERROR_NONE;
	}
	
}
