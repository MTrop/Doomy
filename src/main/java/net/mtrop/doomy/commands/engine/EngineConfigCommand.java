package net.mtrop.doomy.commands.engine;

import java.util.Deque;

import net.mtrop.doomy.DoomyCommand;
import net.mtrop.doomy.DoomyCommon;
import net.mtrop.doomy.IOHandler;

/**
 * A command that handles engine configuration.
 * @author Matthew Tropiano
 */
public class EngineConfigCommand implements DoomyCommand
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
	 * @param badCommand the unknown command.
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
