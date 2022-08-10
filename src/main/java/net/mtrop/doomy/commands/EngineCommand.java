package net.mtrop.doomy.commands;

import java.util.Deque;

import net.mtrop.doomy.DoomyCommand;
import net.mtrop.doomy.DoomyCommon;
import net.mtrop.doomy.IOHandler;

/**
 * A command that prints the engine help output and exits.
 * @author Matthew Tropiano
 */
public class EngineCommand implements DoomyCommand
{
	private String badCommand;

	@Override
	public void init(Deque<String> args) throws BadArgumentException
	{
		badCommand = !args.isEmpty() ? args.pop() : null;
	}

	@Override
	public int call(IOHandler handler)
	{
		return execute(handler, badCommand);
	}

	/**
	 * Executes this command.
	 * @param handler the handler to use for I/O.
	 * @param badCommand the name of the bad command, if any. Can be null.
	 * @return the return code from running the command.
	 */
	public static int execute(IOHandler handler, String badCommand)
	{
		if (badCommand != null)
			handler.errln("ERROR: Unknown command: " + badCommand);
			
		DoomyCommon.help(handler, ENGINE);
		return ERROR_NONE;
	}
	
}
